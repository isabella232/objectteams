/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2013, 2015 GK Software AG
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.internal.osgi.weaving;

import static org.eclipse.objectteams.otequinox.Constants.LIFTING_PARTICIPANT_EXTPOINT_ID;
import static org.eclipse.objectteams.otequinox.Constants.TRANSFORMER_PLUGIN_ID;
import static org.eclipse.objectteams.otequinox.Constants.ORG_OBJECTTEAMS_TEAM;
import static org.eclipse.objectteams.otequinox.TransformerPlugin.log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.objectteams.internal.osgi.weaving.ASMByteCodeAnalyzer.ClassInformation;
import org.eclipse.objectteams.internal.osgi.weaving.AspectBinding.BaseBundle;
import org.eclipse.objectteams.internal.osgi.weaving.AspectBinding.TeamBinding;
import org.eclipse.objectteams.internal.osgi.weaving.Util.ProfileKind;
import org.eclipse.objectteams.internal.osgi.weaving.AspectPermissionManager;
import org.eclipse.objectteams.otequinox.Constants;
import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.eclipse.objectteams.runtime.IReweavingTask;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.framework.hooks.weaving.WovenClassListener;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Wire;

/**
 * This class integrates the OT/J weaver into OSGi using the standard API {@link WeavingHook}.
 * <p>
 * Additionally, we listen to events of woven classes changing to state {@link WovenClass#DEFINED}:
 * </p>
 * <ul>
 * <li>Given that {@link AspectBindingRegistry#addDeferredTeamClasses} was used to record
 * teams that could not be instantiated due to some required class being reported
 * as {@link NoClassDefFoundError}.</li>
 * <li>Assuming further that this error happened because the required class was in the process
 * of being loaded further down the call stack.</li>
 * <li>If later one of the not-found classes has been defined we use that trigger to
 * re-attempt instantiating the dependent team(s).</li>
 * </ul>
 */
public class OTWeavingHook implements WeavingHook, WovenClassListener {

	static @NonNull WeavingScheme DEFAULT_WEAVING_SCHEME = WeavingScheme.Unknown;
	enum WeavingScheme { Unknown, OTRE, OTDRE };

	static final ThreadWeaving WEAVE_THREAD_NOTIFICATION;
	enum ThreadWeaving { Never, Base, Always }
	
	static final Map<String,Set<String>> KNOWN_LOGGING_CLASSES = new HashMap<>();
	
	static {
		String weaving = System.getProperty("ot.weaving.force");
		if (weaving != null) {
			System.err.println("OT/Equinox: ot.weaving.force="+weaving);
			if ("otdre".equalsIgnoreCase(weaving))
				DEFAULT_WEAVING_SCHEME = WeavingScheme.OTDRE;
			else if ("otre".equalsIgnoreCase(weaving))
				DEFAULT_WEAVING_SCHEME = WeavingScheme.OTRE;
		}
		String threadWeaving = System.getProperty("otequinox.weave.thread");
		if ("base".equalsIgnoreCase(threadWeaving))
			WEAVE_THREAD_NOTIFICATION = ThreadWeaving.Base;
		else if ("false".equalsIgnoreCase(threadWeaving))
			WEAVE_THREAD_NOTIFICATION = ThreadWeaving.Never;
		else
			WEAVE_THREAD_NOTIFICATION = ThreadWeaving.Always;

		KNOWN_LOGGING_CLASSES.put("org.eclipse.equinox.common", Collections.singleton(
										"org.eclipse.core.runtime.ILogListener"));
		KNOWN_LOGGING_CLASSES.put("org.eclipse.ui.workbench", new HashSet<>(Arrays.asList(
										"org.eclipse.ui.statushandlers.StatusAdapter",
										"org.eclipse.ui.internal.misc.StatusUtil",
										"org.eclipse.ui.statushandlers.WorkbenchStatusDialogManager",
										"org.eclipse.ui.internal.statushandlers.StatusHandlerRegistry",
										"org.eclipse.ui.internal.statushandlers.StatusHandlerDescriptorsMap")));
		KNOWN_LOGGING_CLASSES.put("org.eclipse.ui.ide", Collections.singleton(
										"org.eclipse.ui.internal.ide.IDEWorkbenchErrorHandler$1"));
	}
	
	// TODO: temporary switch to fall back to coarse grain checking:
	boolean skipBaseClassCheck = "skip".equals(System.getProperty("otequinox.baseClassChecks"));

	// for installing a lifting participant:
	private static final String LIFTING_PARTICIPANT_FIELD = "_OT$liftingParticipant";

	enum WeavingReason { None, Aspect, Base, Thread }
	
	/** Interface to data about aspectBinding extensions. */
	private @NonNull AspectBindingRegistry aspectBindingRegistry = new AspectBindingRegistry();
	
	/** Map of trip wires to be fired when a particular base bundle is loaded. */
	private @NonNull HashMap<String, BaseBundleLoadTrigger> baseTripWires = new HashMap<>();

	/** Set of classes for which processing has started but which are not yet defined in the class loader. */
	private @NonNull Set<String> beingDefined = new HashSet<>();

	/** Map of tasks per qualified class name: reweaving requested during defineClass(). */
	private @NonNull Map<String, IReweavingTask> pendingReweavingTasks = new HashMap<>();

	/** Records of teams that have been deferred due to unresolved class dependencies: */
	private @NonNull List<WaitingTeamRecord> deferredTeams = new ArrayList<>();

	private @NonNull ASMByteCodeAnalyzer byteCodeAnalyzer = new ASMByteCodeAnalyzer();

	private AspectPermissionManager permissionManager;

	/** A registered lifting participant is directly handled by us. */
	private @Nullable IConfigurationElement liftingParticipantConfig;
	private @Nullable Class<?> ooTeam;

	/** Call-back once the extension registry is up and running. */
	public void activate(@NonNull BundleContext bundleContext, ServiceReference<IExtensionRegistry> serviceReference) {
		loadAspectBindingRegistry(bundleContext, serviceReference);
		TransformerPlugin.initialize(bundleContext, this.aspectBindingRegistry, this.permissionManager);
	}

	// ====== Aspect Bindings & Permissions: ======

	@SuppressWarnings("deprecation")
	private void loadAspectBindingRegistry(BundleContext context, ServiceReference<IExtensionRegistry> serviceReference) {
		org.osgi.service.packageadmin.PackageAdmin packageAdmin = null;
		
		ServiceReference<?> ref= context.getServiceReference(org.osgi.service.packageadmin.PackageAdmin.class.getName());
		if (ref!=null)
			packageAdmin = (org.osgi.service.packageadmin.PackageAdmin)context.getService(ref);
		else
			log(IStatus.ERROR, "Failed to load PackageAdmin service. Will not be able to handle fragments.");
		

		IExtensionRegistry extensionRegistry = context.getService(serviceReference);
		if (extensionRegistry == null) {
			log(IStatus.ERROR, "Failed to acquire ExtensionRegistry service, cannot load aspect bindings.");
		} else {
			permissionManager = new AspectPermissionManager(context.getBundle(), packageAdmin);
			permissionManager.loadAspectBindingNegotiators(extensionRegistry);

			aspectBindingRegistry.loadAspectBindings(extensionRegistry, packageAdmin, this);

			loadLiftingParticipant(extensionRegistry);
		}
	}

	public @NonNull AspectPermissionManager getAspectPermissionManager() {
		AspectPermissionManager manager = this.permissionManager;
		if (manager == null)
			throw new NullPointerException("Missing AspectPermissionManager");
		return manager;
	}

	/* Load extension for org.eclipse.objectteams.otequinox.liftingParticipant. */
	private void loadLiftingParticipant(IExtensionRegistry extensionRegistry) {
		IConfigurationElement[] liftingParticipantConfigs = extensionRegistry.getConfigurationElementsFor(
				TRANSFORMER_PLUGIN_ID, LIFTING_PARTICIPANT_EXTPOINT_ID);
		
		if (liftingParticipantConfigs.length != 1) {
			if (liftingParticipantConfigs.length > 1)
				log(IStatus.ERROR, "Cannot install more than one lifting participant.");
			return;
		}
		this.liftingParticipantConfig = liftingParticipantConfigs[0];
		installLiftingParticipant();
	}
	
	private void installLiftingParticipant() {
		Class<?> teamClass = this.ooTeam;
		IConfigurationElement config = this.liftingParticipantConfig;
		if (teamClass != null && config != null) {
			try {
				Field field = teamClass.getDeclaredField(LIFTING_PARTICIPANT_FIELD); // field name cannot be mentioned in source
				field.set(null, config.createExecutableExtension(Constants.CLASS));
				log(IStatus.INFO, "Registered Lifting Participant from "+config.getContributor().getName());
			} catch (Exception e) {
				log(e, "Failed to install lifting participant from "+config.getContributor().getName());
			}
			this.liftingParticipantConfig = null; // signal done
		}
	}

	// ====== Base Bundle Trip Wires: ======
	
	/**
	 * Callback during AspectBindingRegistry#loadAspectBindings():
	 * Set-up a trip wire to fire when the mentioned base bundle is loaded.
	 */
	void setBaseTripWire(@SuppressWarnings("deprecation") @Nullable org.osgi.service.packageadmin.PackageAdmin packageAdmin,
			@NonNull String baseBundleId, BaseBundle baseBundle) 
	{
		if (!baseTripWires.containsKey(baseBundleId))
			baseTripWires.put(baseBundleId, new BaseBundleLoadTrigger(baseBundleId, baseBundle, aspectBindingRegistry, packageAdmin));
	}

	/**
	 * Check if the given base bundle / base class mandate any loading/instantiation/activation of teams.
	 * @return true if all involved aspect bindings have been denied (permissions).
	 */
	boolean triggerBaseTripWires(@Nullable String bundleName, @NonNull WovenClass baseClass) {
		BaseBundleLoadTrigger activation = baseTripWires.get(bundleName);
		if (activation != null) {
			activation.fire(baseClass, beingDefined, this);
			if (activation.isDone())
				baseTripWires.remove(bundleName);
			return activation.areAllAspectsDenied();
		}
		return false;
	}

	// ====== Main Weaving Entry: ======

	@Override
	public void weave(WovenClass wovenClass) {
		beingDefined.add(wovenClass.getClassName());

		try {
			BundleWiring bundleWiring = wovenClass.getBundleWiring();
			String bundleName = bundleWiring.getBundle().getSymbolicName();
			String className = wovenClass.getClassName();
			
			if (bundleName.equals(Constants.TRANSFORMER_PLUGIN_ID)
					|| bundleName.startsWith("org.eclipse.objectteams.otre") // incl. otredyn
					|| bundleName.equals("org.objectweb.asm"))
				return;

			if (isEclipseLoggingClass(bundleName, className))
				return;
			if (isClassWithJSR(className))
				return;

			if (BCELPatcher.BCEL_PLUGIN_ID.equals(bundleName)) {
				BCELPatcher.fixBCEL(wovenClass);
				return;
			}

			byte[] bytes = wovenClass.getBytes();
			WeavingReason reason = requiresWeaving(bundleWiring, className, bytes);
			if (reason != WeavingReason.None) {
				// do whatever is needed *before* loading this class:
				boolean allAspectsAreDenied = triggerBaseTripWires(bundleName, wovenClass);
				if (reason == WeavingReason.Base && allAspectsAreDenied) {
					return; // don't weave for denied bindings
				} else if (reason == WeavingReason.Thread) {
					if (DEFAULT_WEAVING_SCHEME == WeavingScheme.Unknown) {
						log(IStatus.WARNING, "Not performing thread weaving for "+className+" (weaving scheme not yet knonw)");
						return;
					}
					BaseBundle baseBundle = this.aspectBindingRegistry.getBaseBundle(bundleName);
					BaseBundleLoadTrigger.addOTREImport(baseBundle, bundleName, wovenClass, DEFAULT_WEAVING_SCHEME == WeavingScheme.OTDRE);
				} 

				long time = 0;

				DelegatingTransformer transformer = DelegatingTransformer.newTransformer(DEFAULT_WEAVING_SCHEME, this, bundleWiring);
				Class<?> classBeingRedefined = null; // TODO prepare for otre-dyn
				try {
					String displayName = reason+" class "+className;
					log(IStatus.OK, "About to transform "+displayName);
					time = 0;
					if (Util.PROFILE) time= System.nanoTime();
					byte[] newBytes = transformer.transform(bundleWiring.getBundle(),
										className, classBeingRedefined, null/*protectionDomain*/, bytes);
					if (newBytes != null && newBytes != bytes && !Arrays.equals(newBytes, bytes)) {
						if (Util.PROFILE) Util.profile(time, ProfileKind.Transformation, className);
						log(IStatus.INFO, "Transformation performed on "+displayName);
						wovenClass.setBytes(newBytes);
						if (reason == WeavingReason.Aspect)
							recordBaseClasses(transformer, bundleName, className);
					} else {
						if (Util.PROFILE) Util.profile(time, ProfileKind.NoTransformation, className);
					}
				} catch (IllegalClassFormatException e) {
					log(e, "Failed to transform class "+className);
				}
			}
		} catch (ClassCircularityError cce) {
			log(cce, "Weaver encountered a circular class dependency");
		}
	}

	private boolean isClassWithJSR(String className) {
		// ASM cannot handle JSR instruction:
		//		java.lang.RuntimeException: JSR/RET are not supported with computeFrames option
		//        at org.objectweb.asm.Frame.execute(Frame.java:1178)
		//        at org.objectweb.asm.MethodWriter.visitJumpInsn(MethodWriter.java:984)
		//        at org.objectweb.asm.commons.AdviceAdapter.visitJumpInsn(AdviceAdapter.java:503)
		//        at org.objectweb.asm.ClassReader.readCode(ClassReader.java:1335)
		//        at org.objectweb.asm.ClassReader.readMethod(ClassReader.java:1017)
		//        at org.objectweb.asm.ClassReader.accept(ClassReader.java:693)
		// Hence any class known to contain that instruction is excluded:
		return "org.apache.felix.gogo.runtime.Pipe".equals(className);
	}

	private boolean isEclipseLoggingClass(String bundleName, String className) {
		Set<String> knownClasses = KNOWN_LOGGING_CLASSES.get(bundleName);
		if (knownClasses != null) {
			return knownClasses.contains(className);
		} else if (bundleName.equals("org.eclipse.ui.views.log")) {
			if (className.startsWith("org.eclipse.ui.internal.views.log.LogView$"))
				return true;
		} else if (bundleName.startsWith("org.eclipse.epp.logging.aeri")) { // ".core" & ".ide"
			return true; // not truly part of Eclipse but dangerously hooks in as a log listener
		}
		return false;
	}

	WeavingReason requiresWeaving(BundleWiring bundleWiring, @NonNull String className, byte[] bytes) {
		
		// 1. consult the aspect binding registry (for per-bundle info):
		@SuppressWarnings("null")@NonNull // FIXME: org.eclipse.osgi.internal.resolver.BundleDescriptionImpl.getBundle() can return null!
		Bundle bundle = bundleWiring.getBundle();
		if (aspectBindingRegistry.getAdaptedBasePlugins(bundle) != null)
			return WeavingReason.Aspect;

		if (aspectBindingRegistry.isBoundBaseClass(className))
			return WeavingReason.Base;
		boolean isBaseBundle = false;
		List<AspectBinding> aspectBindings = aspectBindingRegistry.getAdaptingAspectBindings(bundle.getSymbolicName());
		if (aspectBindings != null && !aspectBindings.isEmpty()) {
			isBaseBundle = true;
			// potential base class: look deeper:
			for (AspectBinding aspectBinding : aspectBindings) {
				if (!aspectBinding.hasScannedTeams && !aspectBinding.hasBeenDenied)
					return WeavingReason.Base; // we may be first, go ahead and trigger the trip wire
			}
			if (isAdaptedBaseClass(aspectBindings, className, bytes, bundleWiring.getClassLoader()))
				return WeavingReason.Base;					
		}
			
		// 2. test for implementation of Runnable / Thread (per class):
		long time = 0;
		if (Util.PROFILE) time= System.nanoTime();
		if (needsThreadNotificationCode(className, bytes, bundleWiring.getClassLoader(), isBaseBundle))
			return WeavingReason.Thread;
		if (Util.PROFILE) Util.profile(time, ProfileKind.SuperClassFetching, "");

		return WeavingReason.None;
	}
	
	boolean scheduleReweaving(String className, IReweavingTask task) {
		if (beingDefined.contains(className)) {
			pendingReweavingTasks.put(className, task);
			return true;
		}
		return false;
	}

	/** check need for weaving by finding an aspect binding affecting this exact base class or one of its supers. */
	boolean isAdaptedBaseClass(List<AspectBinding> aspectBindings, String className, byte[] bytes, ClassLoader resourceLoader) {
		if (skipBaseClassCheck) return true; // have aspect bindings, flag requests to transform *all* classes in this base bundle
		
		if ("java.lang.Object".equals(className))
			return false; // shortcut, not weavable nor do we have supers

		long start = 0;
		if (Util.PROFILE) start = System.nanoTime();

		try {
			for (AspectBinding aspectBinding : aspectBindings) {
				if (aspectBinding.allBaseClassNames.contains(className) && !aspectBinding.hasBeenDenied)
					return true;					
			}
			// attempt recursion to superclass (not superInterfaces atm):
			ClassInformation classInfo = null;
			if (bytes != null) {
				classInfo = this.byteCodeAnalyzer.getClassInformation(bytes, className);
			} else {
				try (InputStream is = resourceLoader.getResourceAsStream(className.replace('.', '/')+".class")) {
					if (is != null)
						classInfo = this.byteCodeAnalyzer.getClassInformation(is, className);
				} catch (IOException e) {
					return false;
				}
			}
			if (classInfo != null && !classInfo.isInterface()) {
				// TODO(performance): check common prefix to recognize when crossing the plugin-boundary?
				String superClassName = classInfo.getSuperClassName();
				if (superClassName != null)
					return isAdaptedBaseClass(aspectBindings, superClassName, null, resourceLoader);
			}
			return false;
		} finally {
			if (bytes != null && Util.PROFILE) { // only report at top invocation
				Util.profile(start, ProfileKind.SuperClassFetching, className);
			}
		}
	}

	private void recordBaseClasses(DelegatingTransformer transformer, @NonNull String aspectBundle, String className) {
		Collection<String> adaptedBases = transformer.fetchAdaptedBases();
		if (adaptedBases == null || adaptedBases.isEmpty()) return;
		aspectBindingRegistry.addBoundBaseClasses(adaptedBases);
		List<AspectBinding> aspectBindings = aspectBindingRegistry.getAspectBindings(aspectBundle);
		if (aspectBindings != null)
			for (AspectBinding aspectBinding : aspectBindings)
				if (!aspectBinding.hasScannedTeams)
					for (TeamBinding team : aspectBinding.teams)
						if (team.teamName.equals(className))
							if (!team.hasScannedBases) {
								for (TeamBinding equivalent : team.equivalenceSet) {
									equivalent.addBaseClassNames(adaptedBases);
									equivalent.hasScannedBases = true;
								}
								return; // done all equivalent teams
							}
	}

	// ===== handling deferred teams: ======

	/**
	 * Record the given team classes as waiting for instantiation/activation.
	 * Callback during {@link BaseBundleLoadTrigger#fire()}
	 */
	public void addDeferredTeamClasses(List<WaitingTeamRecord> teamClasses) {
		synchronized (deferredTeams) {
			deferredTeams.addAll(teamClasses);
		}
	}

	/**
	 * Try to instantiate/activate any deferred teams that may be unblocked
	 * by the definition of the given trigger class.
	 */
	public void instantiateScheduledTeams(String triggerClassName) {
		List<WaitingTeamRecord> scheduledTeams = null;
		synchronized(deferredTeams) {
			for (WaitingTeamRecord record : new ArrayList<>(deferredTeams)) {
				if (record.notFoundClass.equals(triggerClassName)) {
					if (scheduledTeams == null)
						scheduledTeams = new ArrayList<>();
					if (deferredTeams.remove(record))
						scheduledTeams.add(record);
				}
			}
		}
		if (scheduledTeams == null) return;
		for(WaitingTeamRecord record : scheduledTeams) {
			if (record.team.isActivated)
				continue;
			String teamName = record.team.teamName;
			log(IStatus.INFO, "Consider for instantiation/activation: team "+teamName);
			try {
				TeamLoader loader = new TeamLoader(deferredTeams, beingDefined, DEFAULT_WEAVING_SCHEME == WeavingScheme.OTDRE);
				// Instantiate (we only get here if activationKind != NONE)
				loader.instantiateAndActivate(record.aspectBinding, record.team, record.activationKind); // may re-insert to deferredTeams
			} catch (Exception e) {
				log(e, "Failed to instantiate team "+teamName);
				continue;
			}
		}
	}

	@Override
	public void modified(WovenClass wovenClass) {
		if (wovenClass.getState() == WovenClass.DEFINED) {
			@NonNull String className = wovenClass.getClassName();
			if (className.equals(ORG_OBJECTTEAMS_TEAM)) {
				this.ooTeam = wovenClass.getDefinedClass();
				installLiftingParticipant();
			}
			beingDefined.remove(className);
			IReweavingTask task = pendingReweavingTasks.remove(className);
			if (task != null) {
				try {
					task.reweave(wovenClass.getDefinedClass());
				} catch (IllegalClassFormatException e) {
					log(e, "Failed to reweave class "+wovenClass.getClassName());
					TransformerPlugin.flushLog();
					return;
				}
			}
			instantiateScheduledTeams(className);

			TransformerPlugin.flushLog();
		}
	}

	static boolean hasImport(WovenClass clazz, String packageName, String packageWithAttribute) {
		List<String> imports = clazz.getDynamicImports();
		for (String imp : imports)
			if (imp.equals(packageName) || imp.equals(packageWithAttribute))
				return true;
		for (Wire wire : clazz.getBundleWiring().getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE)) {
			Object packageValue = wire.getRequirement().getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
			if (packageName.equals(packageValue) || packageWithAttribute.equals(packageValue))
				return true;
		}
		return false;
	
	}

	private boolean needsThreadNotificationCode(String className, byte[] bytes, ClassLoader resourceLoader, boolean isBaseBundle) {

		if ("java.lang.Object".equals(className))
			return false; // shortcut, have no super

		switch (WEAVE_THREAD_NOTIFICATION) {
		case Never:
			return false;
		case Base: 
			if (!isBaseBundle)
				return false;
			break;
		default:
			ClassInformation classInfo = null;
			if (bytes != null) {
				classInfo = this.byteCodeAnalyzer.getClassInformation(bytes, className);
			} else {
				try (InputStream is = resourceLoader.getResourceAsStream(className.replace('.', '/')+".class")) {
					if (is != null) {
						classInfo = this.byteCodeAnalyzer.getClassInformation(is, className);
					}
				} catch (IOException e) {
					return false;
				}
			}
			if (classInfo != null && !classInfo.isInterface()) {
				String superClassName = classInfo.getSuperClassName();
				if ("java.lang.Thread".equals(superClassName))
					return true; // ensure TeamActivation will weave the calls to TeamThreadManager
				String[] superInterfaceNames = classInfo.getSuperInterfaceNames();
				if (superInterfaceNames != null)
					for (int i = 0; i < superInterfaceNames.length; i++) {
						if ("java.lang.Runnable".equals(superInterfaceNames[i]))
							return true; // ensure TeamActivation will weave the calls to TeamThreadManager
					}
			}
		}
		return false;
	}

}
