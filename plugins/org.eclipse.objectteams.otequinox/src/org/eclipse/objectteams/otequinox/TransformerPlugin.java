/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TransformerPlugin.java 23468 2010-02-04 22:34:27Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox;

import static org.eclipse.objectteams.otequinox.Constants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IConfigurationElement; //from: org.eclipse.equinox.registry
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.objectteams.otequinox.internal.ASMByteCodeAnalyzer;
import org.eclipse.objectteams.otequinox.internal.AspectBinding;
import org.eclipse.objectteams.otequinox.internal.AspectPermissionManager;
import org.eclipse.objectteams.otequinox.internal.MasterTeamLoader;
import org.eclipse.objectteams.otequinox.hook.ClassScanner;
import org.eclipse.objectteams.otequinox.hook.HookConfigurator;
import org.eclipse.objectteams.otequinox.hook.IAspectRegistry;
import org.eclipse.objectteams.otequinox.hook.IByteCodeAnalyzer;
import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.eclipse.objectteams.otequinox.hook.IOTEquinoxService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The main class (activator) of the transformer plugin.
 * It maintains the aspect registry of this plugin.
 * This class optionally uses  {@link InternalPlatform} for the purpose of accessing the workspace location,
 * i.e., if this class is not found, no workspace location will be used.
 * 
 * @author stephan
 * @version $Id: TransformerPlugin.java 23468 2010-02-04 22:34:27Z stephan $
 */
@SuppressWarnings("restriction") // accessing InternalPlatform
public class TransformerPlugin implements BundleActivator, IOTEquinoxService
{	
	private static List<String> KNOWN_OTDT_ASPECTS = new ArrayList<String>();
	static {
		KNOWN_OTDT_ASPECTS.add("org.eclipse.objectteams.otdt.jdt.ui");
		KNOWN_OTDT_ASPECTS.add("org.eclipse.objectteams.otdt.compiler.adaptor");
		KNOWN_OTDT_ASPECTS.add("org.eclipse.objectteams.otdt.refactoring.adaptor");
		KNOWN_OTDT_ASPECTS.add("org.eclipse.objectteams.otdt.pde.ui");
		KNOWN_OTDT_ASPECTS.add("org.eclipse.objectteams.otdt.samples");
	}
	/** main internal registry of aspect bindings. */
	private static HashMap<String, ArrayList<AspectBinding>> aspectBindingsByBasePlugin = 
			   new HashMap<String, ArrayList<AspectBinding>>();
	private static HashMap<String, ArrayList<AspectBinding>> aspectBindingsByAspectPlugin = 
		       new HashMap<String, ArrayList<AspectBinding>>();
	// map base bundle name to list of adapted base class names
	private static HashMap<String, ArrayList<String>> adaptedBaseClassNames =
		       new HashMap<String, ArrayList<String>>();
	// set of aspect plug-ins which have internal teams:
	private Set<String> selfAdaptingAspects= new HashSet<String>();
	
	/** Aspect Permissions and Negotiation (delegate to dedicated manager). */
	private AspectPermissionManager permissionManager;
	
	/** The instance that is created by the framework. */
	private static TransformerPlugin instance;
	private ServiceRegistration serviceRegistration;
	private ILogger log;
	
	/** instances which may have pending team classes waiting for instantiation. */
	private HashMap<Bundle,List<MasterTeamLoader>> masterTeamLoaders = new HashMap<Bundle, List<MasterTeamLoader>>();
	
	/** Service needed for handling fragments */
	private PackageAdmin packageAdmin;

	// note: actually List<Team> but Team cannot be mentioned in this plugin.
	private List<Object> teamInstances = new ArrayList<Object>();
	
	public static boolean IS_OTDT = false;
	
	public boolean isOTDT() {
		return IS_OTDT;
	}
	
	public TransformerPlugin() {
		instance = this;
	}
	public void start(BundleContext context) throws Exception {
		if (!HookConfigurator.OT_EQUINOX_ENABLED)
			throw new BundleException("Not starting the transformer plugin because OT/Equinox has not been enabled (set system property \"ot.equinox\").");

		this.log = HookConfigurator.getLogger();
		log(ILogger.INFO, "activating org.eclipse.objectteams.otequinox");
		
		ServiceReference ref= context.getServiceReference(PackageAdmin.class.getName());
		if (ref!=null)
			this.packageAdmin = (PackageAdmin)context.getService(ref);
		else
			this.log(ILogger.ERROR, "Failed to load PackageAdmin service. Will not be able to handle fragments.");

		this.permissionManager = new AspectPermissionManager(this.log, context.getBundle(), this.packageAdmin);
		this.permissionManager.loadAspectBindingNegotiators(context);
		loadAspectBindings();
		this.serviceRegistration = context.registerService(IOTEquinoxService.class.getName(), this, new Properties());		
	}
		
	/* be a good citizen: clean up. */
	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
	}

	public static TransformerPlugin getDefault() {
		return instance;
	}
	
	/** public API: Do we know about any team that has not yet been initiaized as requested? */
	public static boolean isWaitingForTeams() {
		synchronized (instance.masterTeamLoaders) {
			if (!instance.masterTeamLoaders.isEmpty())
				return true;
		}		
		synchronized (aspectBindingsByBasePlugin) {
			for (ArrayList<AspectBinding> aspects : aspectBindingsByBasePlugin.values())
				for (AspectBinding aspectBinding : aspects) 
					if (!aspectBinding.activated)
						return true;	
		}
		return false;
	}

	/** 
	 * Internal API for TransformerHook:
	 * see {@link IAspectRegistry#isDeniedAspectPlugin(String)}
	 */
	public boolean isDeniedAspectPlugin(String symbolicName) {
		return this.permissionManager.isDeniedAspectPlugin(symbolicName);
	}

	/* Load extensions for org.eclipse.objectteams.otequinox.aspectBindings and check aspect permissions. */
	private void loadAspectBindings() {
		IConfigurationElement[] aspectBindingConfigs = RegistryFactory.getRegistry().getConfigurationElementsFor(
				TRANSFORMER_PLUGIN_ID, ASPECT_BINDING_EXTPOINT_ID);
		
		for (int i = 0; i < aspectBindingConfigs.length; i++) {
			IConfigurationElement currentBindingConfig = aspectBindingConfigs[i];

			//aspect:
			String aspectBundleId= currentBindingConfig.getContributor().getName();
			IS_OTDT |= KNOWN_OTDT_ASPECTS.contains(aspectBundleId);
			
			//base:
			IConfigurationElement[] basePlugins = currentBindingConfig.getChildren(BASE_PLUGIN);
			if (basePlugins.length != 1) {
				log(ILogger.ERROR, "aspectBinding of "+aspectBundleId+" must declare exactly one basePlugin");
				continue;
			}
			String baseBundleId = basePlugins[0].getAttribute(ID);
			
			//base fragments?
			IConfigurationElement[] fragments = basePlugins[0].getChildren(REQUIRED_FRAGMENT);
			if (fragments != null && !checkRequiredFragments(aspectBundleId, baseBundleId, fragments)) // reported inside
				continue;
			
			AspectBinding binding = new AspectBinding(aspectBundleId, baseBundleId, basePlugins[0].getChildren(Constants.FORCED_EXPORTS_ELEMENT));
			// TODO(SH): maybe enforce that every bundle id is given only once?

			//teams:
			IConfigurationElement[] teams = currentBindingConfig.getChildren(TEAM);
			binding.initTeams(teams.length);
			try {
				for (int j = 0; j < teams.length; j++) {
					String teamClass = teams[j].getAttribute(CLASS);
					binding.teamClasses[j] = teamClass;
					String activation = teams[j].getAttribute(ACTIVATION);
					binding.setActivation(j, activation);
				}
				
				String realBaseBundleId = baseBundleId.toUpperCase().equals(SELF) ? aspectBundleId : baseBundleId;
				addBindingForBaseBundle(realBaseBundleId, binding);
				addBindingForAspectBundle(aspectBundleId, binding);
				
				// now that binding.teamClasses is filled connect to super team, if requested:
				for (int j = 0; j < teams.length; j++) {
					String superTeamName = teams[j].getAttribute(SUPERCLASS);
					if (superTeamName != null)
						addSubTeam(aspectBundleId, binding.teamClasses[j], superTeamName);
				}
				log(ILogger.INFO, "registered:\n"+binding);
			} catch (Throwable t) {
				log(t, "Invalid aspectBinding extension");
			}
		}
	}

	private boolean checkRequiredFragments(String aspectBundleId, String baseBundleId, IConfigurationElement[] fragments) 
	{
		// checking only, no real action needed.
		boolean hasError = false;
		for (IConfigurationElement fragment : fragments) {
			String fragId = fragment.getAttribute(ID);
			if (fragId == null) {
				log(ILogger.ERROR, "Mandatory attribute \"id\" missing from element \"requiredFragment\" of aspect binding in "+aspectBundleId);
				return false;
			} 
			if (packageAdmin == null) {
				log(ILogger.ERROR, "Not checking required fragment "+fragId+" in aspect binding of "+aspectBundleId+", package admin service not present");
				return false; // report only once.
			}
			
			Bundle[] fragmentBundles = packageAdmin.getBundles(fragId, null);
			if (fragmentBundles == null || fragmentBundles.length == 0) {
				log(ILogger.ERROR, "Required fragment "+fragId+" not found in aspect binding of "+aspectBundleId);
				hasError = true;
				continue;
			}
			Bundle fragmentBundle = fragmentBundles[0];
			String aspectBindingHint = " (aspect binding of "+aspectBundleId+")";
			if (packageAdmin.getBundleType(fragmentBundle) != PackageAdmin.BUNDLE_TYPE_FRAGMENT) {
				log(ILogger.ERROR, "Required fragment " + fragId + " is not a fragment" + aspectBindingHint);
				hasError = true;
				continue;
			}
			Bundle[] hosts = packageAdmin.getHosts(fragmentBundle);
			if (hosts == null || hosts.length == 0) {
				if (fragmentBundle.getState() < Bundle.RESOLVED) {
					log(ILogger.ERROR, "Required fragment " + fragId + " is not resolved" + aspectBindingHint);
					hasError = true;
					continue;
				}
				log(ILogger.ERROR, "Required fragment "+fragId+" has no host bundle"+aspectBindingHint);
				hasError = true;					
				continue;
			}
			Bundle host = hosts[0];
			if (!host.getSymbolicName().equals(baseBundleId)) {
				log(ILogger.ERROR, "Required fragment "+fragId+" has wrong host "+host.getSymbolicName()+aspectBindingHint);
				hasError = true;
			}
		}
		return !hasError;
	}


	private static void addBindingForBaseBundle(String baseBundleId, AspectBinding binding) {
		ArrayList<AspectBinding> bindingList = aspectBindingsByBasePlugin.get(baseBundleId);
		if (bindingList == null) {
			bindingList = new ArrayList<AspectBinding>();
			aspectBindingsByBasePlugin.put(baseBundleId, bindingList);
		}
		bindingList.add(binding);
	}

	private void addBindingForAspectBundle(String aspectBundleId, AspectBinding binding) {
		ArrayList<AspectBinding> bindingList = aspectBindingsByAspectPlugin.get(aspectBundleId);
		if (bindingList == null) {
			bindingList = new ArrayList<AspectBinding>();
			aspectBindingsByAspectPlugin.put(aspectBundleId, bindingList);
		}
		bindingList.add(binding);
		if (binding.basePlugin.toUpperCase().equals(SELF))
			selfAdaptingAspects.add(aspectBundleId);
	}
	
	/**
	 * Record a sub-class relationship of two teams within the same aspect bundle.
	 * 
	 * @param aspectBundleId
	 * @param subTeamName
	 * @param teamName
	 */
	private void addSubTeam(String aspectBundleId, String subTeamName, String teamName) {
		ArrayList<AspectBinding> bindingList = aspectBindingsByAspectPlugin.get(aspectBundleId);
		if (bindingList == null) {
			Exception e = new Exception("No such aspect binding");
			log(e, "Class "+teamName+" not registered (declared to be superclass of team "+subTeamName);
		} else {
			for (AspectBinding binding : bindingList)
				if (binding.teamClasses != null)
					for (int i=0; i < binding.teamClasses.length; i++) 
						if (binding.teamClasses[i].equals(teamName)) {
							if (binding.subTeamClasses[i] == null)
								binding.subTeamClasses[i] = new ArrayList<String>();
							binding.subTeamClasses[i].add(subTeamName);
							return;
						}
			Exception e = new Exception("No such aspect binding");
			log(e, "Class "+teamName+" not registered(2) (declared to be superclass of team "+subTeamName);
		}		
	}

	/**
	 * Internal API for TransformerHook:
	 * see {@link IAspectRegistry#getAdaptedBasePlugins(Bundle)}
	 */
	public String[] getAdaptedBasePlugins(Bundle aspectBundle) {
		ArrayList<AspectBinding> bindings = aspectBindingsByAspectPlugin.get(aspectBundle.getSymbolicName());
		if (bindings == null) return null;
		String[] basePlugins = new String[bindings.size()];
		for (int i=0; i<basePlugins.length; i++) {
			basePlugins[i] = bindings.get(i).basePlugin;
		}
		return basePlugins;
	}

	/** Is `symbolicName' the name of a base plugin for which an adapting team is registered? */
	public boolean isAdaptedBasePlugin(String symbolicName) {
		ArrayList<AspectBinding> list = aspectBindingsByBasePlugin.get(symbolicName);
		return list != null && !list.isEmpty();
	}
	
	/**
	 * public API:
	 * {@link IAspectRegistry#getAdaptingAspectPlugins(Bundle)} 
	 */
	public String[] getAdaptingAspectPlugins(Bundle basePlugin) {
		return getAdaptingAspectPlugins(basePlugin.getSymbolicName());
	}
	/**
	 * public API:
  	 * Get the names of aspect plugins adapting a given base plugin.
	 * @param basePluginName symbolic name of a base plugin.
	 * @return non-null array of symbolic names of aspect plugins.
	 */
	public String[] getAdaptingAspectPlugins(String basePluginName) {
		ArrayList<AspectBinding> list = aspectBindingsByBasePlugin.get(basePluginName);
		
		if (list == null)
			return new String[0];
		
		String[] aspects = new String[list.size()];
		for (int i=0; i< list.size(); i++) 
			aspects[i] = list.get(i).aspectPlugin;
		
		return aspects;
	}
	
	/**
     * Recored the names of base classes adapted by a given team from a given aspect bundle.
	 */
	public void storeAdaptedBaseClassNames(String aspectBundleName, String teamName, Collection<String> baseClassNames) 
	{
		// search the base plugin being adapted by the given team:
		String basePlugin = null;
		bindings:
		for (AspectBinding aspectBinding : aspectBindingsByAspectPlugin.get(aspectBundleName)) {
			for (String aspectTeamClass : aspectBinding.teamClasses)
				if (aspectTeamClass.equals(teamName)) {
					basePlugin = aspectBinding.basePlugin;
					break bindings;
				}
		}
		if (basePlugin == null && selfAdaptingAspects.contains(aspectBundleName))
			basePlugin = aspectBundleName;
		if (basePlugin == null) {
			log(ILogger.ERROR, "Base plugin for team "+teamName+" from "+aspectBundleName+" not found!");
			return;
		}
		// merge base class names into existing:
		synchronized (adaptedBaseClassNames) {
			ArrayList<String> baseBundleClassNames = adaptedBaseClassNames.get(basePlugin);
			if (baseBundleClassNames == null) {
				baseBundleClassNames = new ArrayList<String>();
				adaptedBaseClassNames.put(basePlugin, baseBundleClassNames);
			}
			baseBundleClassNames.addAll(baseClassNames);
		}
	}

	/**
	 * Internal API for TransformerHook:
	 * see {@link org.eclipse.objectteams.otequinox.hook.ITeamLoader#loadTeams(Bundle, IClassScanner)}
	 */
	public boolean loadTeams(Bundle baseBundle, ClassScanner classScanner) {
		ArrayList<AspectBinding> bindings = aspectBindingsByBasePlugin.get(baseBundle.getSymbolicName());
		return delegateToMasterTeamLoader(baseBundle, classScanner, bindings);
	}
	
	/**
	 * Internal API for TransformerHook:
	 * see {@link org.eclipse.objectteams.otequinox.hook.IAspectRegistry#hasInternalTeams(Bundle)}
	 */
	public boolean hasInternalTeams(Bundle bundle) {
		return selfAdaptingAspects.contains(bundle.getSymbolicName());
	}
	
	/**
	 * Internal API for TransformerHook:
	 * see {@link org.eclipse.objectteams.otequinox.hook.ITeamLoader#loadInternalTeams(Bundle,ClassScanner)}
	 */
	public boolean loadInternalTeams(Bundle bundle, ClassScanner scanner) {
		ArrayList<AspectBinding> selfBindings = new ArrayList<AspectBinding>();
		synchronized (aspectBindingsByAspectPlugin) {
			ArrayList<AspectBinding> bindings = aspectBindingsByAspectPlugin.get(bundle.getSymbolicName());
			if (bindings == null)
				return false;
			for (int i = 0; i < bindings.size(); )
				if (bindings.get(i).basePlugin.toUpperCase().equals(SELF))
					selfBindings.add(bindings.remove(i));
				else
					i++;
		}
		return delegateToMasterTeamLoader(bundle, scanner, selfBindings);
	}

	// this performs some work for load[Internal]Teams:
	// create&register a MasterTeamLoader and use it for loading the teams.
	private boolean delegateToMasterTeamLoader(Bundle baseBundle,
											   ClassScanner scanner,
											   ArrayList<AspectBinding> bindings) 
	{
		if (bindings == null || bindings.isEmpty())
			return false;
		MasterTeamLoader masterTeamLoader = new MasterTeamLoader(baseBundle);
		boolean success = masterTeamLoader.loadTeams(baseBundle, scanner, bindings);
		if (success)
			addMasterTeamLoader(baseBundle, masterTeamLoader);
		return success;
	}
	
	/**
	 * Internal API for TransformerHook:
	 * see {@link org.eclipse.objectteams.otequinox.hook.ITeamLoader#instantiateTeams(Bundle)}
	 */
	public void instantiateTeams(Bundle baseBundle, String triggerClassname) {
		instance.internalInstantiateTeams(baseBundle, triggerClassname);
	}
	
	/** Add a master team loader which may hold a list of teams waiting for instantiation.
	 */
	private void addMasterTeamLoader(Bundle baseBundle, MasterTeamLoader masterTeamLoader) {
		synchronized (this.masterTeamLoaders) {
			List<MasterTeamLoader> loaders = this.masterTeamLoaders.get(baseBundle);
			if (loaders == null) {
				loaders = new ArrayList<MasterTeamLoader>();
				this.masterTeamLoaders.put(baseBundle, loaders);
			}
			loaders.add(masterTeamLoader);
		}
	}
	

	/**
	 * Instantiate all teams affecting the given base bundle. Don't, however, load the class who's loading
	 * triggered this call.
	 * 
	 * This method checks whether the AspectPermissionManager.isReady(). If not, defer instantiation and 
	 * return the set of all affected base classes for use as a trigger for deferred instantiation. 
	 *  
	 * @param baseBundle
     * @param triggerClassname if non-null: the name of a base class who's loading triggered this instantiation.
	 */
	private void internalInstantiateTeams(Bundle baseBundle, String triggerClassname) 
	{			
		List<MasterTeamLoader> loaders = null;
		synchronized (this.masterTeamLoaders) {
			loaders= this.masterTeamLoaders.get(baseBundle);
			if (this.masterTeamLoaders.isEmpty() || loaders == null)
				return;
			
			// check permission for forcedExports of all adapting aspect bundles
			// (do this only if team loaders are found, but before any side effects occur)
			synchronized (aspectBindingsByBasePlugin) {
				List<AspectBinding> aspects= aspectBindingsByBasePlugin.get(baseBundle.getSymbolicName());
				if (aspects != null) {
					if (this.permissionManager.isReady()) {
						for (AspectBinding aspectBinding : aspects)
							if (!this.permissionManager.checkForcedExports(aspectBinding.aspectPlugin, baseBundle.getSymbolicName(), aspectBinding.forcedExports))
								return; // don't activate teams of rejected aspect bundle
					} else {
						this.permissionManager.addForcedExportsObligations(aspects, baseBundle);
					}
				}
			}

			loaders = new ArrayList<MasterTeamLoader>(loaders);
			this.masterTeamLoaders.remove(baseBundle);
		}
		while (!loaders.isEmpty()) {
			// be sure not to hold any lock during this statement:
			List<Object> newInstances = loaders.remove(0).instantiateLoadedTeams(baseBundle, triggerClassname, this.permissionManager);
			synchronized (this.teamInstances) {
				this.teamInstances.addAll(newInstances);
			}
		}
		// mark the fact that all teams adapting this base bundle have now been activated:
		synchronized (aspectBindingsByBasePlugin) {
			List<AspectBinding> aspects= aspectBindingsByBasePlugin.get(baseBundle.getSymbolicName());
			if (aspects != null)
				for (AspectBinding aspectBinding : aspects)
					aspectBinding.activated= true;
		}
	}

	/** Copy all registered team instances into the given list,
	 *  which must by of type List<Team>; (can't mention Team in this plugin).
     */
	@SuppressWarnings("unchecked")
	public static synchronized void getTeamInstances(List list) {
		list.addAll(instance.teamInstances);
	}
	
	// configure OT/Equinox debugging:
	public static int WARN_LEVEL = ILogger.ERROR;
	static {
		String level = System.getProperty("otequinox.debug");
		if (level != null) {
			level = level.toUpperCase();
			if (level.equals("OK"))
				WARN_LEVEL = ILogger.OK;
			else if (level.equals("INFO"))
				WARN_LEVEL = ILogger.INFO;
			else if (level.startsWith("WARN"))
				WARN_LEVEL = ILogger.WARNING;
			else if (level.startsWith("ERR"))
				WARN_LEVEL = ILogger.ERROR;
			else
				WARN_LEVEL = ILogger.OK;
		}
	}

	public void log (Throwable ex, String msg) {
		msg = "OT/Equinox: "+msg;
		System.err.println(msg);
		ex.printStackTrace();
		this.log.log(TRANSFORMER_PLUGIN_ID, ex, msg);
	}
	
	public void log(int status, String msg) {
		if (status >= WARN_LEVEL)
			doLog(status, msg);
	}

	public void doLog(int status, String msg) {
		msg = "OT/Equinox: "+msg;
		this.log.log(TRANSFORMER_PLUGIN_ID, status, msg);
	}

	public IByteCodeAnalyzer getByteCodeAnalyzer() {
		return new ASMByteCodeAnalyzer();
	}
}

