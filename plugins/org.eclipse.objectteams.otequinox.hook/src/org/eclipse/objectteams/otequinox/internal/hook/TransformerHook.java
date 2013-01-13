/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.CRC32;

import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingStatsHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.BundleWatcher;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegateHook;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.eclipse.objectteams.otequinox.hook.ClassScanner;
import org.eclipse.objectteams.otequinox.hook.HookConfigurator;
import org.eclipse.objectteams.otequinox.hook.IByteCodeAnalyzer;
import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.eclipse.objectteams.otequinox.hook.IOTEquinoxService;
import org.eclipse.objectteams.otequinox.hook.IOTTransformer;
import org.eclipse.objectteams.otequinox.hook.ITeamLoader;
import org.eclipse.objectteams.otequinox.internal.hook.Util.ProfileKind;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * This class intercepts class loading in order to perform load time weaving for OT/J.
 * 
 * Install using these properties (either from commandline or config.ini):
 * <pre>
 * osgi.framework.extensions=org.eclipse.objectteams.eclipse.transformer.hook
 * osgi.hook.configurators.include=org.eclipse.objectteams.eclipse.transformer.hook.HookConfigurator  
 * </pre>
 * 
 * <h2>class byte transformation</h2>
 * The principal method is {@link processClass()} which delegates to an ObjectTeamsTransformer (OTRE).
 * 
 * <h2>bundle life-cycle</h2>
 * <p>
 * The bundle life-cycle is monitored by methods {@link #initializedClassLoader()} and 
 * {@link #watchBundle()}, from where loading and instantiation of teams is triggered.
 * </p>
 * If a bundle has no activation policy
 * (detected in {@link OTStorageHook#checkActivationPolicy()} and stored in {@link #pendingNonLazyActivationBundles})
 * use the event of loading the first class from that bundle (observed in {@link #recordClassDefine()}
 * to check if teams adapting this bundle exist that need activating.
 * 
 * <h2>finding classes</h2>
 * <p>
 * The woven code of a base class needs access to the bound team(s) plus to IBoundBase, 
 * which are not on the base bundle's classpath. 
 * Here method {@link #postFindClass()} helps
 * by manually delegating to the aspect bundles' class loaders in turn.
 * </p><p>
 * Similarly, class <code>TeamThreadManager</code> needs to be accessed from any classes extending
 * Thread (or implementing Runnable - <i>not currently supported</i>). This specific class is
 * stored in {@link #recordClassDefine()}
 * and directly answered in {@link #postFindClass()}.
 * </p>
 * 
 * <h2>aspect permission</h2>
 * <p>
 * If a weaving request has been denied for a given aspect bundle,
 * method {@link #preFindClass()} 
 * will throw an exception to avoid loading of any class from that bundle.
 * </p>
 * @author stephan
 * @version $Id$
 */
@SuppressWarnings("nls")
public class TransformerHook implements ClassLoadingHook, BundleWatcher, ClassLoaderDelegateHook, ClassLoadingStatsHook
{
	// As an OSGI extension bundle, we can't depend on the transformer plugin, so we have to hardcode this
	public static final String  TRANSFORMER_PLUGIN_ID           = "org.eclipse.objectteams.otequinox";
	
	private static final String TRANSFORMER_HOOK_ID           = "org.eclipse.objectteams.otequinox.hook";
	private static final String WORKSPACE_INITIALIZER_PLUGIN_ID = "org.eclipse.objectteams.otdt.earlyui";
	private static final String OTDT_QUALIFIER = "OTDT";

	// another non-transformable bundle:
	private static final String ASM_PLUGIN_ID = "org.objectweb.asm";
	
	// this one requires hot-fixing:
	private static final String BCEL_PLUGIN_ID = "org.apache.bcel";
	private static final String BCEL_PATH_DIR = "bcelpatch/";

	// specific action may be required when this class is loaded:
	private static final String ORG_OBJECTTEAMS_TEAM = "org.objectteams.Team";
	// intercept and store this class:
	private static final String ORG_OBJECTTEAMS_TEAMTHREADMANAGER = "org.objectteams.TeamThreadManager";
	private Class<?> teamThreadManagerClass;

	
	private static final HashSet<String> WEAVE_BUNDLES = new HashSet<String>();

	
	static {
		String value = System.getProperty("otequinox.weave");
		if (value != null && value.length() > 0) {
			StringTokenizer st = new StringTokenizer(value, ",");
			while (st.hasMoreTokens()) 
				WEAVE_BUNDLES.add(st.nextToken());
		}
	}
	
	/** kinds of classes needing transformation. */
	enum ClassKind { BASE, ROLE, TEAM; }

	
	/** A class loader which knows about OTRE but cannot transform byte code. *
	 *  All plugins may require class org.objectteams.ITeam and related.       */
	private ClassLoader parentClassLoader;
	
	
	// ==== STATE ====

	/* storage for additional information regarding base and aspect bundles. */
	final private BundleRegistry bundleRegistry = new BundleRegistry();

	/* Local registry of all classes needing adaptation.
     * Note that the ClassKind is recorded only for logging/profiling,
     * so it doesn't hurt that we can't assign several kinds to the same class.
	 */
	final HashMap<String,ClassKind> transformableClasses= new HashMap<String, ClassKind>();
	private void addTransformableClass(String className, ClassKind kind) {
		this.transformableClasses.put(className, kind);
		String ifcName= className.replace("__OT__", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (ifcName != className)
			this.transformableClasses.put(ifcName, kind); // for role classes also enter the ifc-part
	}
	
	/**
	 * @param classBytes array containing the class' byte code
	 * @param className '.'-separated fully qualified class name
	 * @param resourceLoader class loader that should be used to find further .class files.
	 * @param bundleName name of the bundle from which the class is loaded.
	 */
	private ClassKind fetchTransformationKind(byte[] classBytes, final String className, ClassLoader resourceLoader, final Bundle bundle) {
		ClassKind kind = this.transformableClasses.get(className);
		if (kind == ClassKind.BASE && !this.bundleRegistry.isAdaptedBaseBundle(bundle.getSymbolicName()))
			kind = null; // don't use false info
		if (   kind != null                     // found 
			|| this.byteCodeAnalyzer == null )  // can't do better
		{
			return kind;
		}
		String superName = this.byteCodeAnalyzer.getSuperclass(classBytes, className);
		if (superName != null)
			return fetchInheritedTransformationKind(superName, resourceLoader, bundle.getSymbolicName());
		return kind; 
	}
	private ClassKind fetchInheritedTransformationKind(String className, ClassLoader resourceLoader, String bundleName) {
		//TODO(SH): change to use bundle here, too (rather than bundleName)?
		ClassKind kind = this.transformableClasses.get(className); 
		if (kind == ClassKind.BASE && !this.bundleRegistry.isAdaptedBaseBundle(bundleName))
			kind = null; // don't use false info
		if (kind != null) {
			return kind;
		}

		if ("java.lang.Object".equals(className))
			return null; // shortcut, have no super
		String superName = null;
		InputStream is = resourceLoader.getResourceAsStream(className.replace('.', '/')+".class");
		if (is != null)
			try {
				superName = this.byteCodeAnalyzer.getSuperclass(is, className);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					// nothing we can do
				}
			}
		if (superName != null) {
			if ("java.lang.Thread".equals(superName))
				return ClassKind.BASE; // ensure TeamActivation will weave the calls to TeamThreadManager
			return fetchInheritedTransformationKind(superName, resourceLoader, bundleName);
		}
		return kind; 
	}
	
	/** stored between method calls, indexed by plugin ID (unspecific, base or aspect): */
	final protected HashMap<BundleData,ProtectionDomain> domains= new HashMap<BundleData,ProtectionDomain>();

	// ==== access to the PackageAdmin service (for resolving bundles etc.) ====
	private PackageAdmin packageAdmin; 

	// ==== A Bridge to the TransformerPlugin ====
	
	/** The bundle org.eclipse.objectteams.otequinox. */
	Bundle otEquinoxBundle;

	/** Gateway to the class org.eclipse.objectteams.otequinox.TransformerPlugin. */
	// loading and instantiating teams:
	private ITeamLoader teamLoadingService;    
	// asking about aspectBindings:
	final private SafeAspectRegistry aspectRegistry= new SafeAspectRegistry(this);

	private IByteCodeAnalyzer byteCodeAnalyzer;
	
	// logging (console or via FrameworkLog):
	final private ILogger logger;

	// gateway to the OTRE proper:
	private IOTTransformer transformerService;

	final private HashSet<Bundle> uninstalling = new HashSet<Bundle>();
	
	// Class loaders for which initializedClassLoader is currently executing (usable, but not yet registered):
	private HashMap<Bundle,BaseClassLoader> pendingClassLoaders = new HashMap<Bundle, BaseClassLoader>(); 
	
	// -- The next three collections manage activation of teams adapting base bundles without an activation policy --
	
	// bundles that do not have a lazy activation policy and have not yet loaded a class:
	public HashSet<Bundle> pendingNonLazyActivationBundles = new HashSet<Bundle>();
	
	// stack of classes currently being defined (used to avoid activation while a class is still being defined -> ClassCircularityError)
	Stack<String> currentlyDefiningClasses = new Stack<String>();
	
	// bundles that formerly were in pendingNonLazyActivationBundles which wait for currentlyDefiningClasses to become empty
	List<Bundle> activatableBundles = new ArrayList<Bundle>(); 

	// --
	
	/** Constructor invoked by the framework (via {@link HookConfigurator#addHooks(org.eclipse.osgi.baseadaptor.HookRegistry)}). */
	public TransformerHook(BaseAdaptor adaptor) {
		this.logger = HookConfigurator.getLogger();

		// make OTRE available to all bundles via framework classpath
		// which contains the fragment org.eclipse.objectteams.otequinox.runtime:
		this.parentClassLoader= getClass().getClassLoader();
		
		this.logger.log(Util.INFO, "Created equinox adaptor hook: "+getClass().getName()+
				".\n \t(To disable this and subsequent INFO messages from OT/Equinox set otequinox.debug to WARN or ERROR).");
	}


	/** Invoked when the TransformerPlugin has registered itself as a service. */
	void connectOTEquinoxService(IOTEquinoxService otEquinoxService) {
		this.teamLoadingService= otEquinoxService;
		this.aspectRegistry.connectOTEquinoxService(otEquinoxService, this.logger);
		this.byteCodeAnalyzer= otEquinoxService.getByteCodeAnalyzer();
		this.logger.log(Util.INFO, "OT/Equinox: connected the transformer service");
	}
	
	/** Invoked when the TransformerPlugin has registered the OTRE as a service. */
	void connectOTTransformerService(IOTTransformer transformerService) {
		this.transformerService = transformerService;
	}
	
	void connectPackageAdmin(PackageAdmin packageAdmin) {
		this.packageAdmin = packageAdmin;
	}
	
// SH: extra safety against recursion (see Trac #173)
	ThreadLocal<String> currentlyProcessedClassName = new ThreadLocal<String>();
// :HS
	/**
	 * Delegate some classes to the ObjectTeamsTransformer for byte code weaving.
	 */
	public byte[] processClass(String name, byte[] classbytes,
			ClasspathEntry classpathEntry, BundleEntry entry,
			ClasspathManager manager) 
	{	
		synchronized (this.currentlyDefiningClasses) {			
			this.currentlyDefiningClasses.add(name); // block aspect activation while defining a class
		}

		String previousClassName = currentlyProcessedClassName.get();
		this.currentlyProcessedClassName.set(name);
		try {
		
			Bundle bundle = manager.getBaseData().getBundle();
			ProtectionDomain domain = domains.get(manager.getBaseData());
			if (bundle == this.otEquinoxBundle)
				return classbytes; // don't transform the adaptor ;-)
			if (ASM_PLUGIN_ID.equals(bundle.getSymbolicName())) // name comparison since multiple instances of this bundle could exist
				return classbytes; // don't transform ASM
			if (BCEL_PLUGIN_ID.equals(bundle.getSymbolicName()))
				return fixBCEL(name, classbytes);
					
// SH: extra safety against recursion (see Trac #173)
			if (name.equals(previousClassName)) {
				logger.log(new Exception("unexpected loading situation"), "Recursion occurred loading class "+name+" from bundle "+bundle.getSymbolicName());
				return null;				
			}
// :HS
			
			// NB: we must use a fresh instance of ObjectTeamsTransformer
			// on each invocation because the OTRE is _not_ thread safe, 
			// specifically the field ObjectTeamsTransformation.factory 
			// MUST NOT be accessed or even assigned concurrently.
			ClassFileTransformer objectTeamsTransformer= null;

			ClassLoader resourceLoader = null;
			if (ClassScanner.REPOSITORY_USE_RESOURCE_LOADER)
				resourceLoader = (ClassLoader) manager.getBaseClassLoader(); 
			
			// ==== loading an adapted base class? 
			long time = 0;
			if (Util.PROFILE) time= System.nanoTime();
			ClassKind classKind= fetchTransformationKind(classbytes, name, resourceLoader, bundle);
			if (Util.PROFILE) Util.profile(time, ProfileKind.SuperClassFetching, "", this.logger);
			if (classKind == ClassKind.BASE) {
				objectTeamsTransformer= this.transformerService.getNewTransformer();
				classbytes= transformClass(objectTeamsTransformer, resourceLoader,
										   name, classbytes, domain, 
								           "potential base", ProfileKind.BaseTransformation);
			// ==== loading a role class? 
			} else if (classKind == ClassKind.ROLE) {
				objectTeamsTransformer= this.transformerService.getNewTransformer();
				classbytes= transformClass(objectTeamsTransformer, resourceLoader,
										   name, classbytes, domain, 
										   "role", ProfileKind.AspectTransformation); //$NON-NLS-1$
			} else  {
				// ==== loading a team class?
				boolean isLoading = AspectBundleRole.isLoadingTeams(bundleRegistry, bundle.getSymbolicName());
				if (isLoading || ByteCodeAnalyzer.isTeam(classbytes)) {
					try {
						if (bundle.getState() < Bundle.STARTING && this.aspectRegistry.hasInternalTeams(bundle)) {
							this.logger.doLog(Util.ERROR, "Illegal state: loading team class "+name+" from bundle "+bundle.getSymbolicName()+" \n"+
														  "while this bundle has not been activated yet (state="+bundle.getState()+").\n"+
														  "Aspect bindings will not be woven.\n"+
														  "Note: Perhaps the bundle lacks an activation policy (lazy)?");
							return null;
						}
						AspectBundleRole.markLoadingTeams(bundleRegistry, bundle.getSymbolicName(), true);
						// transform the aspect:
						objectTeamsTransformer= this.transformerService.getNewTransformer();
						classbytes= transformClass(objectTeamsTransformer, resourceLoader,
												   name, classbytes, domain, 
												   "team", ProfileKind.AspectTransformation);				 //$NON-NLS-1$
					} finally {
						if (!isLoading) 
							AspectBundleRole.markLoadingTeams(bundleRegistry, bundle.getSymbolicName(), false);
					}
				} else if (   this.otEquinoxBundle != null 
						   && WEAVE_BUNDLES.contains(bundle.getSymbolicName())
						   && this.aspectRegistry.isAdaptedBasePlugin(bundle.getSymbolicName()))
				{
					objectTeamsTransformer= this.transformerService.getNewTransformer();
					classbytes= transformClass(objectTeamsTransformer, resourceLoader,
											   name, classbytes, domain, 
									           "ordinary class (could be sub base class)", ProfileKind.BaseTransformation);
				}
			}
		}
		catch (IllegalClassFormatException icfe) {
			icfe.printStackTrace();
		}
		finally {
			this.currentlyProcessedClassName.set(previousClassName);
		}
		return classbytes;
	}

	private byte[] fixBCEL(String name, byte[] classbytes) {
		boolean shouldPatch = false;
		if ("org.apache.bcel.generic.InstructionHandle".equals(name)) {
			CRC32 crc32 = new CRC32();
			crc32.update(classbytes);
			long crc = crc32.getValue();
			String detail = "";
			if (classbytes.length != 0x1623)
				detail+="\n\tlength="+classbytes.length;	// identify original class
			if (crc != 0x42132087 && crc != 0xdb1b9859L)	// --""-- (I've seen two versions of this class file, semantically equivalent though)
				detail+="\n\tcrc="+crc;
			if (classbytes[0x0F00] != 0x18)
				detail+="\n\tmodifiers of getInstructionHandle="+classbytes[0xF00];	// modifiers of method getInstructionHandle at "static final"
			if (classbytes[0x105C] != 0x04)
				detail+="\n\tmodifiers of addHandle="+classbytes[0x105C];			// modifiers of method getHandle at "protected"
			if (detail.length() == 0) {
				shouldPatch = true;
			} else {
				this.logger.log(Util.WARNING, "Class org.apache.bcel.generic.InstructionHandle needs a hot-patch but has unexpected byte code:"+detail);
			}
		} else if ("org.apache.bcel.generic.BranchHandle".equals(name)) {
			CRC32 crc32 = new CRC32();
			crc32.update(classbytes);
			long crc = crc32.getValue();
			String detail = "";
			if (classbytes.length != 0x09F1)
				detail+="\n\tlength="+classbytes.length;	// identify original class
			if (crc != 0xd3c37c19L && crc != 0x74bee71eL)	// --""-- (I've seen two versions of this class file, semantically equivalent though)
				detail+="\n\tcrc="+crc;
			if (classbytes[0x067E] != 0x18)
				detail+="\n\tmodifiers of getBranchHandle="+classbytes[0x067E];	// modifiers of method getBranchHandle at "static final"
			if (classbytes[0x06F8] != 0x04)
				detail+="\n\tmodifiers of addHandle="+classbytes[0x06F8];		// modifiers of method getHandle at "protected"
			if (detail.length() == 0) {
				shouldPatch = true;
			} else {
				this.logger.log(Util.WARNING, "Class org.apache.bcel.generic.BranchHandle needs a hot-patch but has unexpected byte code:"+detail);
			}
		}
		if (shouldPatch) {
			Bundle transformer = this.packageAdmin.getBundles(TRANSFORMER_HOOK_ID, null)[0];
			URL entry = transformer.getEntry(BCEL_PATH_DIR+name+".class");
			InputStream stream = null;
			try {
				stream = entry.openStream();
				int len = stream.available();
				byte[] newBytes = new byte[len];
				stream.read(newBytes);
				this.logger.log(Util.INFO, "hot-patched a bug in class "+name+"\n"+
						"\tsee https://bugs.eclipse.org/bugs/show_bug.cgi?id=344350");
				return newBytes;
			} catch (IOException e) {
				this.logger.log(e, "Failed to hot-patch bcel class "+name);
				return classbytes;
			} finally {
				if (stream != null)
					try {
						stream.close();
					} catch (IOException e) {
						// ignore
					}
			}
		}
		return classbytes;
	}

	// transform, log and profile:
	private byte[] transformClass(ClassFileTransformer objectTeamsTransformer, ClassLoader resourceLoader,
								  String name, byte[] classbytes, ProtectionDomain domain, 
								  String kind, ProfileKind profileKind)
			throws IllegalClassFormatException 
	{
		this.logger.log(Util.OK, "about to transform "+kind+" class "+name);
		long time= 0;
		if (Util.PROFILE) time= System.nanoTime();
		classbytes = objectTeamsTransformer.transform(resourceLoader, name.replace('.', '/'), null, domain, classbytes);
		if (Util.PROFILE) Util.profile(time, profileKind, name, this.logger);
		return classbytes;
	}
	
	// hook method, no specific action
	public boolean addClassPathEntry(
			ArrayList<ClasspathEntry> cpEntries, 
			String cp,
			ClasspathManager hostmanager, 
			BaseData sourcedata,
			ProtectionDomain sourcedomain) 
	{
		return false;
	}

	// hook method, no specific action
	public String findLibrary(BaseData data, String libName) {
		return null;
	}
	
	/** Make all bundles share the system classloader as their parent,
	 *  in order to expose the OTRE to all.
	 */
	public ClassLoader getBundleClassLoaderParent() {
		return parentClassLoader;
	}

	public BaseClassLoader createClassLoader(
			final ClassLoader parent,
			ClassLoaderDelegate delegate, 
			BundleProtectionDomain domain,
			BaseData data, 
			String[] bundleclasspath) 
	{
		Bundle bundle = data.getBundle();

		synchronized (this.pendingClassLoaders) {			
			if (this.pendingClassLoaders.containsKey(bundle))
				// a class loader is being announced via initializedClassloader,
				// yet, before that method returns, the Bundle isn't yet wired to the class loader,
				// so grab it from our intermediate storage:
				return this.pendingClassLoaders.get(bundle);
		}
		
		// some paranoid sanity checks (shouldn't trigger any more)
		if (   bundle != this.otEquinoxBundle
			&& !uninstalling.contains(bundle) // perhaps can't query transformer yet (uninstalling unused during launch)
			&& this.aspectRegistry.isAdaptedBasePlugin(bundle.getSymbolicName())) 
		{
			BaseBundleRole baseBundle= this.bundleRegistry.adaptedBaseBundles.get(bundle.getSymbolicName());
			if (baseBundle != null && baseBundle.state != BaseBundleRole.State.INITIAL) {
				
				synchronized (this.pendingClassLoaders) {
					if (!this.pendingClassLoaders.containsKey(bundle)) {
						this.logger.log(ILogger.WARNING, "False alarm regarding circular class path for "+bundle.getSymbolicName()+", bundle state is "+baseBundle.state);
						return null; // false alarm
					}
				}
				// defensive:
				this.logger.log(new ClassCircularityError(), "Circular class path for "+bundle.getSymbolicName()+", bundle state is "+baseBundle.state);
				// return a dummy class loader that rather fails than dead-locking.
				return new DefaultClassLoader(parent, delegate, domain, data, bundleclasspath) 
				{
					@Override
					public Class<?> findLocalClass(String classname) throws ClassNotFoundException {
						throw new ClassNotFoundException("Has already reported class path circularity");
					}
					@Override public URL findLocalResource(String resource) { return null; }

					@Override public void initialize() { /* nop to avoid infinite recursion */ }

					@Override
					public Class<?> loadClass(String name) throws ClassNotFoundException {
						throw new ClassNotFoundException("Has already reported class path circularity");
					}
				};
			}
		}
		
		// normally we let the Framework create classloaders:
		return null;
	}

	public void initializedClassLoader(final BaseClassLoader bundleClassLoader, BaseData data) 
	{
// DEBUG:
//		if (isKnowID(data.getSymbolicName()))
//			System.out.println(">>1>>"+data.getSymbolicName());
		
	
 		ProtectionDomain domain = bundleClassLoader.getDomain();
		if (domain != null)
			domains.put(data, domain);
	
		Bundle bundle = data.getBundle();		
		if (bundle == otEquinoxBundle) // don't adapt the transformer. 
			return;
		
		if (this.uninstalling.contains(bundle)) // don't adapt bundle that is being uninstalled
			return;

		if (this.pendingClassLoaders.containsKey(bundle))
			return; // already initialized (in this very call-stack?)
		
		// ==== perhaps it's an aspect adapting some base bundle(s): 

		// register class loaders for teams per plugin ID of adapted base
		String[] adaptedBaseBundles = this.aspectRegistry.getAdaptedBasePlugins(bundle);
		if (adaptedBaseBundles != null) 
			for (String baseID : adaptedBaseBundles) {
				Bundle baseBundle = null;
				if (baseID.toUpperCase().equals(BaseBundleRole.SELF)) {
					baseBundle = bundle;
				} else {
					Bundle[] baseBundles = packageAdmin.getBundles(baseID, null);
					if (baseBundles == null) {
						this.logger.log(Util.ERROR, "Adapted base bundle "+baseID+" not found.");
					} else if (baseBundles.length > 1) {
						baseBundle = baseBundles[0];
						for (int i=1; i<baseBundles.length; i++)
							if (baseBundle.getVersion().compareTo(baseBundles[i].getVersion()) < 0) // smaller version than next
								baseBundle = baseBundles[i];
						this.logger.log(Util.WARNING, "Found more than one version of adapted base bundle "+baseID+", picked version "+baseBundle.getVersion()+".");
					} else {
						baseBundle = baseBundles[0];
					}
				}
				if (baseBundle != null)
					BaseBundleRole.createBaseBundleRole(this.bundleRegistry, baseBundle, bundle);
			}
		
		// ==== perhaps it's a base bundle:
		
		// Trigger PHASE 1 of aspect activation:
		if (this.teamLoadingService != null)
			checkLoadTeams(bundleClassLoader, bundle);
		else if (!Util.isPlatformBundle(bundle.getSymbolicName()))
			this.logger.log(Util.WARNING, "Not loading teams for bundle "+bundle.getSymbolicName()+
					           " (transformerPlugin not yet initialized)");
	}
	
	private void checkLoadTeams(BaseClassLoader bundleClassLoader, Bundle bundle) 
	{
		synchronized (this.pendingClassLoaders) {			
			this.pendingClassLoaders.put(bundle,bundleClassLoader);
		}
		ClassScanner scanner = new ClassScanner(this.transformerService);
		this.bundleRegistry.checkLoadTeams(bundle, this.aspectRegistry, this.teamLoadingService, scanner);
		recordRolesAndBases(scanner);
//		synchronized (this.pendingClassLoaders) {			
//			this.pendingClassLoaders.remove(bundle);
//		}
	}
	
	private void recordRolesAndBases(ClassScanner scanner) {
		Collection<String> baseClassNames = scanner.getCollectedBaseClassNames();
		if (baseClassNames != null)
			for (String baseClassName : baseClassNames)
				addTransformableClass(baseClassName, ClassKind.BASE);
		Collection<String> roleClassNames = scanner.getCollectedRoleClassNames();						
		if (roleClassNames != null)
			for (String roleClassName : roleClassNames)
				addTransformableClass(roleClassName, ClassKind.ROLE);
	}
	
	/**
	 * This method watches the life-cycle of plugins, and implements 2-phase loading for the 
	 * transformer bundle org.eclipse.objectteams.otequinox:
	 * <pre>
	 * STEP 1. as soon as possible (may need to try more than once):
	 * 			-> retrieve and store bundle org.eclipse.objectteams.otequinox in "transformerBundle" 
	 * 			-> activate org.eclipse.objectteams.otequinox
	 * STEP 2. after activating org.eclipse.objectteams.otequinox
	 * 			-> load class TransformerPlugin and store in "transformerPluginClass"
	 * </pre>
	 * 
	 * For all other plugins check whether PHASE-2 of loading has to be performed when
	 * the plugin's activation finishes.
	 */
	public void watchBundle(final Bundle bundle, int type) {
// DEBUG:		
//		if (isKnownID(bundle.getSymbolicName()))
//			System.out.println(">>2>>"+bundle.getSymbolicName());
		
		if (type == BundleWatcher.START_ACTIVATION) {
			if (TRANSFORMER_PLUGIN_ID.equals(bundle.getSymbolicName()))
				otEquinoxBundle = bundle;
			checkJdtCoreOrig(bundle);
			checkInternalTeams(bundle);
		}

		if (type == BundleWatcher.END_ACTIVATION) {
			Util.reportBundleStateChange(bundle, type, this.logger);

			// start the transformerBundle as early as possible:
			// (runtime must be activated for initialization of locations).
			if ("org.eclipse.core.runtime".equals(bundle.getSymbolicName())) 
				startTransformerBundle();
			
			if (bundle != otEquinoxBundle) {
				// ==== trigger remaining actions now that the plugin is activated:
				BaseBundleRole.endActivation(bundleRegistry, bundle, this.aspectRegistry, this.teamLoadingService);
			}
		} else if (type == BundleWatcher.START_UNINSTALLING) {
			this.uninstalling.add(bundle);
		} else if (type == BundleWatcher.END_UNINSTALLING) {
			this.uninstalling.remove(bundle);
		}
	}
	
	/** Load all internal teams of bundle, if any. */
	private void checkInternalTeams(Bundle bundle) {
		if (this.aspectRegistry.hasInternalTeams(bundle)) {
			// aspectRole needed to record 'aspectRole.isLoading' from processClass().
			this.bundleRegistry.createAspectRole(bundle.getSymbolicName());
			this.logger.log(Util.OK, "Will load internal teams of "+bundle.getSymbolicName());
			ClassScanner scanner = new ClassScanner(this.transformerService);
			if (this.teamLoadingService.loadInternalTeams(bundle, scanner)) {
				recordRolesAndBases(scanner);
				BaseBundleRole baseRole= BaseBundleRole.createBaseBundleRole(bundleRegistry, bundle, bundle); // self-adapting
				baseRole.state= BaseBundleRole.State.TEAMS_LOADED;
				this.logger.log(Util.INFO, "Loaded internal teams of "+bundle.getSymbolicName());
			} else {
				this.logger.log(Util.ERROR, "Failed to load internal teams of "+bundle.getSymbolicName());
			}
		}
	}

	/** Loading an original jdt.core is fatal for us: */
	private void checkJdtCoreOrig(Bundle bundle) {
		if (!this.aspectRegistry.isOTDT()) return; // nothing to check
		if ("org.eclipse.jdt.core".equals(bundle.getSymbolicName())) {
			if (bundle instanceof BundleHost) {
				BundleHost host = (BundleHost)bundle;
				if (!host.getVersion().getQualifier().contains(OTDT_QUALIFIER))
					throw new Error("Fatal dependency problem: loading wrong version '"+host.getVersion()+"' of plug-in "+bundle.getSymbolicName());
			}
		}
	}

	private void startTransformerBundle() {
		if (this.otEquinoxBundle != null)
			return;		
		this.otEquinoxBundle = startBundle(TRANSFORMER_PLUGIN_ID, true);
		// check if we want to set a workspace location (earliest opportunity):
		startBundle(WORKSPACE_INITIALIZER_PLUGIN_ID, false); // ignore if not present
	}

	private Bundle startBundle(String bundleID, boolean logError) {
		Bundle[] candidates = packageAdmin.getBundles(bundleID, null);
		if (candidates == null) {
			if (logError)
				this.logger.log(ILogger.ERROR, "Bundle "+bundleID+" not found");
			return null;
		}
		Bundle bundle = candidates[0]; // [0] corresponds to the highest version 
		try {
			bundle.start();
		} catch (Exception e) {
			if (logError)
				this.logger.log(e, "Error starting the OT/Equinox transformer plug-in");
		}
		return bundle;
	}
	
	public Class<?> preFindClass(String name, BundleClassLoader classLoader, BundleData data) 
			throws ClassNotFoundException 
	{
		if (aspectRegistry.isDeniedAspectPlugin(data.getSymbolicName()))
			throw new ClassNotFoundException(name+" from denied aspect bundle "+data.getSymbolicName());
		
		BaseBundleRole baseBundle= this.bundleRegistry.adaptedBaseBundles.get(data.getSymbolicName());
		if (baseBundle == null)
			return null; // nothing to contribute
		
		synchronized (baseBundle) {
			if (baseBundle.missingClassNames.contains(name))
				throw new ClassNotFoundException(name); // already failed before
		}
		return baseBundle.knownAlienClasses.get(name); // check for previous success
	}
	
	ThreadLocal<String> currentlySearchedClassName = new ThreadLocal<String>();
	
	public Class<?> postFindClass(String name, BundleClassLoader classLoader, BundleData data) {
		if (name.equals(currentlySearchedClassName.get()))
			return null;
		// special case: any class subclassing Thread or implementing Runnable may depend on this class:
		if (name.equals(ORG_OBJECTTEAMS_TEAMTHREADMANAGER))
			return teamThreadManagerClass;
		String bundleSymbolicName = data.getSymbolicName();
		BaseBundleRole baseBundle= this.bundleRegistry.adaptedBaseBundles.get(bundleSymbolicName);
		if (baseBundle == null)
			return null; // not a registered base plugin, nothing to contribute
		if (baseBundle.state == BaseBundleRole.State.WAIT_FOR_TEAM) {
			logger.log(Util.WARNING, "Base plugin "+bundleSymbolicName+" is not yet ready for delegated classloading");
		} else if (baseBundle.aspectBundles.isEmpty()) {
			logger.log(Util.WARNING, ">>> adapting aspect bundles not yet wired when loading: "+name);
		} else {
			// a team class may be alien to the current plugin, yet the woven plugin
			// may depend on the team. That's why we need this ClassLoaderDelegateHook.
			
			// avoid infinite recursion:
			currentlySearchedClassName.set(name);
			try {
				for (Bundle aspectBundle: baseBundle.aspectBundles) {
					if (aspectBundle.getBundleId() == data.getBundleID())
						continue; // self adaptation, nothing to contribute
					try {
						Class<?> result= aspectBundle.loadClass(name);
						if (result != null) {
							baseBundle.knownAlienClasses.put(name, result);
							logger.log(Util.OK, "loaded alien class from aspect plugin: "+name);
							return result;
						}
					} catch (ClassNotFoundException e) { 
						// noop, keep traversing aspect bundles
					}
				}
			} finally {
				currentlySearchedClassName.set(null);
			}
			synchronized(baseBundle) {
				// remember this class so we never again need to search
				baseBundle.missingClassNames.add(name); 
			}
		}
		return null;
	}
	public String postFindLibrary(String name, BundleClassLoader classLoader, BundleData data) {
		return null;
	}
	public URL postFindResource(String name, BundleClassLoader classLoader, BundleData data) {
		return null;
	}
	public Enumeration<URL> postFindResources(String name, BundleClassLoader classLoader, BundleData data) {
		return null;
	}
	public String preFindLibrary(String name, BundleClassLoader classLoader, BundleData data) {
		return null;
	}
	public URL preFindResource(String name, BundleClassLoader classLoader, BundleData data) {
		return null;
	}
	public Enumeration<URL> preFindResources(String name, BundleClassLoader classLoader, BundleData data) {
		return null;
	}

	public void preFindLocalClass(String name, ClasspathManager manager) throws ClassNotFoundException {
		// noop		
	}

	public synchronized void postFindLocalClass(String name, Class<?> clazz, ClasspathManager manager) throws ClassNotFoundException {
		// noop
	}

	public void preFindLocalResource(String name, ClasspathManager manager) {
		// noop
	}

	public void postFindLocalResource(String name, URL resource, ClasspathManager manager) {
		// noop		
	}
	
	public void recordClassDefine(String name, Class<?> clazz, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry, ClasspathManager manager) 
	{
		// is this the first class from a pendingNonLazyActivationBundle?
		synchronized (this.activatableBundles) {
			Bundle bundle = manager.getBaseData().getBundle();
			if (pendingNonLazyActivationBundles.remove(bundle))
				activatableBundles.add(bundle); // schedule for activation when currentlyDefiningClasses is empty
		}
		// is the stack of currentlyDefiningClasses empty?
		boolean shouldTrigger = false;
		synchronized (this.currentlyDefiningClasses) {			
			this.currentlyDefiningClasses.remove(name);
			if (this.currentlyDefiningClasses.isEmpty())
				shouldTrigger = true;
		}
		// perform scheduled activations:
		if (shouldTrigger) {
			Bundle[] copy;
			synchronized (this.activatableBundles) {
				copy = this.activatableBundles.toArray(new Bundle[this.activatableBundles.size()]);
				this.activatableBundles.clear();
			}
			for (Bundle bundle : copy)
				BaseBundleRole.endActivation(this.bundleRegistry, bundle, this.aspectRegistry, this.teamLoadingService);
		}

		if (name.startsWith("org.objectteams")) {
			if (name.equals(ORG_OBJECTTEAMS_TEAM))
				this.teamLoadingService.initializeOOTeam(clazz);
			else if (name.equals(ORG_OBJECTTEAMS_TEAMTHREADMANAGER))
				this.teamThreadManagerClass = clazz;
		}
	}

}