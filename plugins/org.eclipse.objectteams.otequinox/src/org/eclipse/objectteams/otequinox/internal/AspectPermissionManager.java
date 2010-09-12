/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Germany and Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AspectPermissionManager.java 23468 2010-02-04 22:34:27Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal;

import static org.eclipse.objectteams.otequinox.hook.AspectPermission.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.objectteams.otequinox.ActivationKind;
import org.eclipse.objectteams.otequinox.AspectBindingRequestAnswer;
import org.eclipse.objectteams.otequinox.Constants;
import org.eclipse.objectteams.otequinox.IAspectRequestNegotiator;
import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.eclipse.objectteams.otequinox.internal.MasterTeamLoader.TeamClassRecord;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.objectteams.otequinox.hook.AspectPermission;
import org.eclipse.objectteams.otequinox.hook.HookConfigurator;
import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Manage permissions of aspect bundles requesting to apply aspectBindings and forcedExports.
 * The following pieces of information are checked:
 * <ul>
 * <li>properties set in installation-wide config.ini or as command line args (handled by {@link HookConfigurator} (plus internal class OTStorageHook))</li>
 * <li>defaults set per workspace (file negotiationDefaults.txt)</li>
 * <li>individual GRANT/DENY per workspace (files grantedForcedExports.txt, deniedForcedExports.txt)</li>
 * <li>answers from registered negotiators (extension point org.eclipse.objectteams.otequinox.aspectBindingNegotiators, see {@link IAspectRequestNegotiator})</li>
 * </ul>
 *
 * <p>
 * The final answer for a given request is combined from all sources where the priority of any {@link #DENY} answer is highest, 
 * of {@link #UNDEFINED} is lowest.
 * </p>
 * <p>
 * If a negotiator has determined a decision and its answer has the <code>persistent</code> flag set,
 * this particular aspect permission is stored as per-workspace configuration.
 * </p>
 * @author stephan
 * @since 1.2.6
 */
@SuppressWarnings("restriction")
public class AspectPermissionManager {

	// property names for default configuration:
	private static final String FORCED_EXPORT_DEFAULT  = "forced.export.default";
	private static final String ASPECT_BINDING_DEFAULT = "aspect.binding.default";
	
	// workspace files where negotiation configuration is stored:
	private static final String NEGOTIATION_DEFAULTS_FILE   = "negotiationDefaults.txt";  
	private static final String GRANTED_FORCED_EXPORTS_FILE = "grantedForcedExports.txt";
	private static final String DENIED_FORCED_EXPORTS_FILE  = "deniedForcedExports.txt";


	// set of aspect plug-ins for which some permission has been denied:
	private Set<String> deniedAspects = new HashSet<String>();
	// default permission for aspect bindings:
	private AspectPermission defaultAspectBindingPermission = GRANT;
	// default permission for forced exports:
	private AspectPermission defaultForcedExportPermission = UNDEFINED; // not yet granted, but open for receiving a GRANT
	// for negotiation of aspect binding requests (incl. forced export):
	private List<IAspectRequestNegotiator> negotiators = new ArrayList<IAspectRequestNegotiator>();
	
	
	// collect all forced exports (denied/granted), granted should balance to an empty structure.
	// structure is: aspect-id -> (base bundle x base package)*
	private HashMap<String, ArrayList<String[]>> deniedForcedExportsByAspect= new HashMap<String, ArrayList<String[]>>();
	private HashMap<String, ArrayList<String[]>> grantedForcedExportsByAspect= new HashMap<String, ArrayList<String[]>>();
	
	// key is aspectId+"->"+baseId, value is array of team names
	private HashMap<String, Set<String>> deniedTeamsByAspectBinding = new HashMap<String, Set<String>>();
	private HashMap<String, Set<String>> grantedTeamsByAspectBinding = new HashMap<String, Set<String>>();

	// the workspace directory for storing the state of this plugin
	private IPath otequinoxState;
	// back link needed for accessing the state location:
	private Bundle transformerBundle;
	// helper instance needed to stop bundles by name
	private PackageAdmin packageAdmin;
	// shared logger:
	private ILogger log;
	
	public AspectPermissionManager(ILogger log, Bundle bundle, PackageAdmin packageAdmin) {
		this.log = log;
		this.transformerBundle = bundle;
		this.packageAdmin = packageAdmin;
	}
		
	/* local cache for isReady(): */
	private boolean isWaitingForLocation = true;
	
	/** Before using this permission manager a client must check whether we're ready (instance location set). */
	public boolean isReady() {
		if (!isWaitingForLocation)
			return true;
		try {
			InternalPlatform platform = InternalPlatform.getDefault();
			Location instanceLocation = platform.getInstanceLocation();
			if (!instanceLocation.isSet())
				return false; // not yet capable
			this.isWaitingForLocation = false;
			fetchAspectBindingPermssionsFromWorkspace();
		} catch (NoClassDefFoundError ncdfe) {
			this.log.log(ILogger.WARNING, "Optional class InternalPlatform not found, cannot access workspace location");
			this.isWaitingForLocation = false;
			return true;
		}
		if (!this.obligations.isEmpty())
			for (Runnable job : this.obligations)
				job.run();
		return true;
	}

	/** 
	 * Fetch stored permissions from this plugin's workspace state.
	 * 
	 * @pre instance location should be set (see {@link #isReady()}),
	 *    otherwise will silently return without accessing workspace settings. 
	 */
	private void fetchAspectBindingPermssionsFromWorkspace() 
	{		
		try {
			this.otequinoxState = InternalPlatform.getDefault().getStateLocation(this.transformerBundle, true);
		} catch (NoClassDefFoundError ncdfe) {
			this.log.log(ILogger.WARNING, "Optional class InternalPlatform not found, cannot access workspace location");
			return;
		}

		// defaults:
		IPath configFilePath = this.otequinoxState.append(NEGOTIATION_DEFAULTS_FILE);
		File configFile = new File(configFilePath.toOSString());		
		if (configFile.exists()) {
			Properties props = new Properties();
			try {
				boolean migrated = false; // TODO(SH): remove this migration support in 1.3.0
				props.load(new FileInputStream(configFile));
				String value = (String) props.get(ASPECT_BINDING_DEFAULT);
				if (value != null)
					try {
						defaultAspectBindingPermission = AspectPermission.valueOf(value);
					} catch (IllegalArgumentException iae) {
						if ("DONT_CARE".equals(value)) {
							defaultAspectBindingPermission = AspectPermission.UNDEFINED;
							migrated = true;
						} else {
							// this code should remain even after 1.3.0:
							defaultAspectBindingPermission = AspectPermission.DENY;
							log(iae, "Cannot set default aspect permission from file "+NEGOTIATION_DEFAULTS_FILE+", assuming DENY.");
						}
					}
				value = (String) props.get(FORCED_EXPORT_DEFAULT);
				if (value != null)
					try {
						defaultForcedExportPermission = AspectPermission.valueOf(value);
					} catch (IllegalArgumentException iae) {
						if ("DONT_CARE".equals(value)) {
							defaultForcedExportPermission = AspectPermission.UNDEFINED;
							migrated = true;
						} else {
							// this code should remain even after 1.3.0:
							defaultForcedExportPermission = AspectPermission.DENY;
							log(iae, "Cannot set default forced exports permission from file "+NEGOTIATION_DEFAULTS_FILE+", assuming DENY.");
						}
					}
				if (migrated)
					writeNegotiationDefaults(configFile);
			} catch (IOException ioex) {
				log(ioex, "Failed to read configuration file "+configFilePath.toOSString());
			}
		} else {
			try {
				File stateDir = new File(this.otequinoxState.toOSString());
				if (!stateDir.exists())
					stateDir.mkdirs();
				configFile.createNewFile();
				writeNegotiationDefaults(configFile);
			} catch (IOException ioex) {
				log(ioex, "Failed to create configuration file "+configFilePath.toOSString());
			}
		}
		
		// explicitly denied:
		configFilePath = this.otequinoxState.append(DENIED_FORCED_EXPORTS_FILE);
		configFile = new File(configFilePath.toOSString());
		if (configFile.exists())
			HookConfigurator.parseForcedExportsFile(configFile, DENY);
		
		// explicitly granted:
		configFilePath = this.otequinoxState.append(GRANTED_FORCED_EXPORTS_FILE);
		configFile = new File(configFilePath.toOSString());
		if (configFile.exists())
			HookConfigurator.parseForcedExportsFile(configFile, GRANT);
	}

	private void writeNegotiationDefaults(File configFile)
			throws IOException 
	{
		FileWriter writer = new FileWriter(configFile);
		writer.append(ASPECT_BINDING_DEFAULT+'='+defaultAspectBindingPermission.toString()+'\n');
		writer.append(FORCED_EXPORT_DEFAULT+'='+defaultForcedExportPermission.toString()+'\n');
		writer.flush();
		writer.close();
		log(ILogger.INFO, "Created aspect binding defaults file "+configFile.getCanonicalPath());
	}

		
	/** Load extensions for EP org.eclipse.objectteams.otequinox.aspectBindingNegotiators. */
	public void loadAspectBindingNegotiators(BundleContext context) {
		IConfigurationElement[] aspectBindingNegotiatorsConfigs = RegistryFactory.getRegistry().getConfigurationElementsFor(
				Constants.TRANSFORMER_PLUGIN_ID, Constants.ASPECT_NEGOTIATOR_EXTPOINT_ID);		
		for (int i = 0; i < aspectBindingNegotiatorsConfigs.length; i++) {
			IConfigurationElement currentNegotiatorConfig = aspectBindingNegotiatorsConfigs[i];
			try {
				Object negotiator = currentNegotiatorConfig.createExecutableExtension("class");
				if (negotiator != null)
					this.negotiators.add(((IAspectRequestNegotiator)negotiator));
			} catch (CoreException e) {
				log(e, "Failed to instantiate extension "+currentNegotiatorConfig);
			}
		}
	}

	/** Delegatee of internal API {@link TransformerPlugin#isDeniedAspectPlugin(String)}. */
	public boolean isDeniedAspectPlugin(String symbolicName) {
		return this.deniedAspects.contains(symbolicName);
	}

	
	/**
	 * Check whether a given aspect requests forced exports from base, 
	 * and whether these requests are granted/denied by checking all available sources.
	 * 
	 * Clients should ask {@link #isReady()} (ie., instance location is set) before calling this method,
	 * otherwise workspace settings have to be silently ignored (any error should be signaled by client).
	 * 
	 * @param aspectId      symbolic name of the aspect bundle
	 * @param baseBundleId  symbolic name of the bound base bundle
     * @param forcedExports any forced exports requested in this aspect binding.
	 * @return whether all requests (if any) have been granted
	 */
	public boolean checkForcedExports(String aspectId, String baseBundleId, IConfigurationElement[] forcedExports) 
	{
		if (forcedExports == null || forcedExports.length == 0)
			return true;
		
		ArrayList<String[]> deniedForcedExports = getConfiguredForcedExports(aspectId, DENY,  deniedForcedExportsByAspect);
		ArrayList<String[]> grantedForcedExports= getConfiguredForcedExports(aspectId, GRANT, grantedForcedExportsByAspect);

		// iterate all requested forcedExports to search for a matching permission:
		for (IConfigurationElement forcedExport : forcedExports) { // [0..1] (as defined in the schema)
			String forcedExportsRequest = forcedExport.getValue();
			if (forcedExportsRequest == null)
				continue;
			for (String singleForcedExportRequest : forcedExportsRequest.split(",")) 
			{
				singleForcedExportRequest = singleForcedExportRequest.trim();

				String[] listEntry;
				boolean grantReported = false;
				AspectPermission negotiatedPermission = this.defaultForcedExportPermission;
				
				// DENY by default?
				if (negotiatedPermission == DENY) {
					log(ILogger.ERROR, "Default denial of forced export regarding package "+singleForcedExportRequest+
									   " from bundle "+baseBundleId+" as requested by bundle "+aspectId+"; bundle not activated");
					this.deniedAspects.add(aspectId); // keep for answering the TransformerHook.
					return false; // NOPE!					
				}
				
				// DENY from configuration?
				listEntry = findRequestInList(baseBundleId, singleForcedExportRequest, deniedForcedExports);
				if (listEntry != null) {
					log(ILogger.ERROR, "Explicit denial of forced export regarding package "+singleForcedExportRequest+
									   " from bundle "+baseBundleId+" as requested by bundle "+aspectId+"; bundle not activated");
					this.deniedAspects.add(aspectId); // keep for answering the TransformerHook.
					return false; // NOPE!
				}

				// GRANT from configuration?
				listEntry = findRequestInList(baseBundleId, singleForcedExportRequest, grantedForcedExports);
				if (listEntry != null) {
					log(ILogger.INFO, "Forced export granted for "+aspectId+": "+singleForcedExportRequest+" (from bundle "+baseBundleId+")");
					grantReported = true;
					grantedForcedExports.remove(listEntry);
					negotiatedPermission = GRANT;
				}

				// default and persistent configuration did not DENY, proceed to the negotiators:
				boolean shouldPersist = false;
				for (IAspectRequestNegotiator negotiator : this.negotiators) {
					AspectBindingRequestAnswer answer = negotiator.checkForcedExport(aspectId, baseBundleId, singleForcedExportRequest, negotiatedPermission);
					if (answer != null) {
						if (answer.permission.compareTo(negotiatedPermission) > 0) // increasing priority of answer?
						{ 
							shouldPersist = answer.persistent;
							negotiatedPermission = answer.permission;
							// locally store as default for subsequent requests (not persistent, see below):
							if (answer.allRequests)
								this.defaultForcedExportPermission = negotiatedPermission;
							if (negotiatedPermission == DENY)
								break; // end of discussion.
						}
					}
				}

				// make decision persistent?
				if (shouldPersist && negotiatedPermission != UNDEFINED)
					// FIXME(SH): handle "allRequests":
					persistForcedExportsAnswer(aspectId, baseBundleId, singleForcedExportRequest, negotiatedPermission);
				
				// report:
				if (negotiatedPermission == GRANT) {
					if (!grantReported)
						log(ILogger.INFO, "Negotiation granted forced export for "+aspectId+
										  ": "+singleForcedExportRequest+" (from bundle "+baseBundleId+')');
				} else {
					String verb = "did not grant";
					if (negotiatedPermission == DENY)
						verb = "denied";
					log(ILogger.ERROR, "Negotiation "+verb+" forced export for "+aspectId+
									   ": "+singleForcedExportRequest+" (from bundle "+baseBundleId+")"+
									   ". Aspect is not activated.");
					this.deniedAspects.add(aspectId); // keep for answering the TransformerHook.
					return false; // don't install illegal aspect
				}
			}
		}
		if (!grantedForcedExports.isEmpty())
			reportUnmatchForcedExports(aspectId, grantedForcedExports);
		return true;
	}

	/**
	 * Get the forced exports configured for a given aspect bundle with permission <code>perm</code>.
	 * Consult {@link HookConfigurator} and store the result in <code>map</code>.
	 * 
	 * @param aspectId  symbolic name of the aspect in focus
	 * @param perm		are we asking about DENY or GRANT?
	 * @param map		in/out param for storing results from OTStorageHook
	 * @return		 	list of pairs (base bundle x base package)
	 */
	private ArrayList<String[]> getConfiguredForcedExports(String                               aspectId, 
														   AspectPermission 				    perm, 
														   HashMap<String, ArrayList<String[]>> map) 
    {
		ArrayList<String[]> forcedExports= map.get(aspectId);
		if (forcedExports == null) {
			// fetch declarations from config.ini or other locations.
			forcedExports= HookConfigurator.getForcedExportsByAspect(aspectId, perm);
			map.put(aspectId, forcedExports);
		}
		return forcedExports;
	}

	private String[] findRequestInList(String baseBundleId, String basePackage, ArrayList<String[]> list) {
		if (list != null)
			for (String[] singleExport : list)
				if (   singleExport[0].equals(baseBundleId)
					&& singleExport[1].equals(basePackage))
				{
					return singleExport;
				}
		return null;
	}

	/**
	 * If the structure of grantedForcedExports is not empty we have mismatches between forced-export declarations.
	 * Report these mismatches as warnings.
	 */
	void reportUnmatchForcedExports(String aspectId, ArrayList<String[]> unmatchedForcedExports) 
	{
		for (String[] export: unmatchedForcedExports) {
			String baseId = export[0];
			String pack   = export[1];
			log(ILogger.WARNING, "Aspect "+aspectId+
							  " does not declare forced export of package "+
							  pack+" from bundle "+baseId+
							  " as declared in config.ini (or system property)");
		}
	}

	/* Simple strategy to append a forced export to a file (existing or to be created). */
	private void persistForcedExportsAnswer(String aspectId, String baseBundleId, String basePackage, AspectPermission negotiatedPermission) 
	{
		if (this.otequinoxState == null) {
			log(ILogger.ERROR, "Can't persist forcedExports permission, no workspace location accessable.");
			return;
		}
		try {
			String fileName = (negotiatedPermission == DENY) ? DENIED_FORCED_EXPORTS_FILE : GRANTED_FORCED_EXPORTS_FILE;
			IPath forcedExportsPath = this.otequinoxState.append(fileName);
			File forcedExportsFile = new File(forcedExportsPath.toOSString());
			if (!forcedExportsFile.exists())
				forcedExportsFile.createNewFile();
			FileWriter writer = new FileWriter(forcedExportsFile, true); // FIXME(SH): consider merge (after decision about file format)
			writer.append('\n');
			writer.append(baseBundleId);
			writer.append("\n[\n\t");
			writer.append(basePackage);
			writer.append(";x-friends:=\"");
			writer.append(aspectId);
			writer.append("\"\n]\n");
			writer.flush();
			writer.close();
		} catch (IOException ioe) {
			log(ioe, "Failed to persist negotiation result");
		}
	}
	
	/**
	 * Check permission for the aspect binding of one specific team.
	 * 
	 * Clients should ask {@link #isReady()} (ie., instance location is set) before calling this method,
	 * otherwise workspace settings have to be silently ignored (any error should be signaled by client).
 	 * 
	 * @param aspectBundleId
	 * @param baseBundleId
	 * @param teamClass
	 * @return whether this team is permitted to adapt classes from the given base bundle.
	 */
	boolean checkTeamBinding(String aspectBundleId, String baseBundleId, String teamClass) 
	{
		boolean shouldReportGrant = false; // grant by default should not be reported
		AspectPermission negotiatedPermission = this.defaultAspectBindingPermission;

		// DENY by default?
		if (negotiatedPermission == DENY) {
			log(ILogger.ERROR, "Default denial of aspect binding regarding base bundle "+baseBundleId+
							   " as requested by bundle "+aspectBundleId+"; bundle not activated");
			this.deniedAspects.add(aspectBundleId); // keep for answering the TransformerHook.
			return false; // NOPE!					
		}

		
		String key = aspectBundleId+"->"+baseBundleId;
		
		// denied from configuration?
		Set<String> deniedTeams = deniedTeamsByAspectBinding.get(key);
		if (deniedTeams != null && !deniedTeams.isEmpty()) {
			if (deniedTeams.contains(teamClass)) {
				deniedAspects.add(aspectBundleId);
				return false;
			}
		}
		
		// granted from configuration?
		Set<String> grantedTeams = grantedTeamsByAspectBinding.get(key);
		if (grantedTeams != null && grantedTeams.contains(teamClass)) {
			negotiatedPermission = GRANT;
			shouldReportGrant = true;
		}
		
		// default and persistent configuration did not DENY, proceed to the negotiators:
		boolean shouldPersist = false;
		for (IAspectRequestNegotiator negotiator : this.negotiators) {
			AspectBindingRequestAnswer answer = negotiator.checkAspectBinding(aspectBundleId, baseBundleId, teamClass, negotiatedPermission);
			if (answer != null) {
				if (answer.permission.compareTo(negotiatedPermission) > 0) // increasing priority of answer?
				{ 
					shouldPersist = answer.persistent;
					negotiatedPermission = answer.permission;
					shouldReportGrant = negotiatedPermission == GRANT;
					// locally store as default for subsequent requests:
					if (answer.allRequests)
						this.defaultAspectBindingPermission = negotiatedPermission;

					if (negotiatedPermission == DENY)
						break; // end of discussion.
				}
			}
		}

		// make decision persistent?
		if (shouldPersist && negotiatedPermission != UNDEFINED)
			persistTeamBindingAnswer(aspectBundleId, baseBundleId, teamClass, negotiatedPermission);
		
		// report:
		if (negotiatedPermission == GRANT) {
			if (shouldReportGrant)
				log(ILogger.INFO, "Negotiation granted aspect binding for "+aspectBundleId+
								  " to base bundle "+baseBundleId+" by means of team "+teamClass+'.');
		} else {
			String verb = "did not grant";
			if (negotiatedPermission == DENY)
				verb = "denied";
			log(ILogger.ERROR, "Negotiation "+verb+" aspect binding for "+aspectBundleId+
							   " to base bundle "+baseBundleId+" by means of team "+teamClass+
							   ". Aspect is not activated.");
			this.deniedAspects.add(aspectBundleId); // keep for answering the TransformerHook.
			return false; // don't install illegal aspect
		}
		return true;
	}

	private void persistTeamBindingAnswer(String aspectBundleId, String baseBundleId, String teamClass, AspectPermission negotiatedPermission) 
	{
		// FIXME(SH): implement persisting these!		
	}

	void log (Throwable ex, String msg) {
		msg = "OT/Equinox: "+msg;
		this.log.log(Constants.TRANSFORMER_PLUGIN_ID, ex, msg);
	}
		
	void log(int status, String msg) {
		if (status >= TransformerPlugin.WARN_LEVEL)
			this.log.log(Constants.TRANSFORMER_PLUGIN_ID, status, "OT/Equinox: "+msg);
	}

	List<Runnable> obligations = new ArrayList<Runnable>();
	public void addBaseBundleObligations(final List<Object> teamInstances, final ArrayList<TeamClassRecord> teamClasses, final Bundle baseBundle) {
		schedule(new Runnable() {
			public void run() {
				List<TeamClassRecord> teamsToRevert = new ArrayList<TeamClassRecord>();
				// aspect bindings:
				for (TeamClassRecord teamClass : teamClasses)
					if (!checkTeamBinding(teamClass.aspectBundle.getSymbolicName(), baseBundle.getSymbolicName(), teamClass.teamName))
						teamsToRevert.add(teamClass);
				if (!teamsToRevert.isEmpty())
					revert(teamsToRevert);
			}
			void revert(List<TeamClassRecord> teamsToRevert) {
				try {
					Set<Bundle> bundlesToStop = new HashSet<Bundle>();
					Class<?>[] deactivationArgumentTypes = new Class[]{Thread.class};
					Object[] deactivationArguments       = new Object[] {TeamClassRecord.get_ALL_THREADS()};
					for (int i=0, c=0; c< teamsToRevert.size(); c++) {
						TeamClassRecord teamClass = teamClasses.get(c);
						if (teamClass.activation != ActivationKind.NONE) {
							Object teamInstance = teamInstances.get(i++);
							Method deactivationMethod = teamClass.clazz.getMethod("deactivate", deactivationArgumentTypes);
							deactivationMethod.invoke(teamInstance, deactivationArguments);
							// could also check if roles are present already ...
						}
						bundlesToStop.add(teamClass.aspectBundle);
					}
					for (Bundle bundle : bundlesToStop) {
						log(ILogger.ERROR, "Stopping aspect bundle "+bundle.getSymbolicName()+" with denied aspect binding(s)");
						bundle.stop();
					}
				} catch (Exception e) {
					log(e, "Failed to revert aspect bundle with denied aspect bindings.");
				}
			}
		});
	}

	public void addForcedExportsObligations(final List<AspectBinding> aspects, final Bundle baseBundle) {
		schedule(new Runnable () {
			public void run() {
				for (AspectBinding aspectBinding : aspects)
					if (!checkForcedExports(aspectBinding.aspectPlugin, baseBundle.getSymbolicName(), aspectBinding.forcedExports))
						stopIllegalBundle(aspectBinding.aspectPlugin);
			}
		});
	}

	void schedule(Runnable job) {
		if (isReady()) // became ready since last query?
			job.run();
		else
			synchronized(obligations) {
				obligations.add(job);
			}
	}
		
	void stopIllegalBundle(String symbolicName) {
		String msgCore = "stop bundle "+symbolicName+" whose requests for forced exports have been denied";
		if (this.packageAdmin == null) {
			log(ILogger.ERROR, "Needing to "+msgCore+" but package admin is not available");
		} else {
			Bundle[] bundles = this.packageAdmin.getBundles(symbolicName, null);
			if (bundles == null)
				log(ILogger.ERROR, "Needing to "+msgCore+" but bundle cannot be retrieved");
			else
				try {
					bundles[0].stop();
				} catch (BundleException e) {
					log(e, "Failed to " + msgCore);
				}
		}
	}
	
}
