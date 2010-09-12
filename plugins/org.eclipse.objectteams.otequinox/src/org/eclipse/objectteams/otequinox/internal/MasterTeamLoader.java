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
 * $Id: MasterTeamLoader.java 23468 2010-02-04 22:34:27Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.objectteams.otequinox.ActivationKind;
import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.eclipse.objectteams.otequinox.hook.ClassScanner;
import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Each instance of this class is responsible for loading all teams adapting a specific base plugin.
 * It stores the team classes loaded successfully.
 * After team loading a call to instantiateLoadedTeams() shall be issued to instantiate
 * those teams, but that call pre-assumes that adapted base classes have already been loaded.
 * 
 * @author stephan
 * @version $Id: MasterTeamLoader.java 23468 2010-02-04 22:34:27Z stephan $
 */
@SuppressWarnings("restriction")
public class MasterTeamLoader {
	
	/** A simple record describing a configured and loaded team class: */
	static class TeamClassRecord {
		// store these info to check consistency among different aspectBindings mentioning the same team class:
		static Set<Class<?>> allInstantiatedTeams = new HashSet<Class<?>>();
		static Map<Class<?>, ActivationKind> allActivations = new HashMap<Class<?>, ActivationKind>();
		
		/** The bundle providing this team classs. */
		Bundle aspectBundle;
		/** The qualified team name. */
		String teamName;
		/** The loaded team class. */
		Class<?> clazz;
		/** The activation kind which was requested from the extension. */
		ActivationKind activation;
		/**
		 * @param clazz      the loaded team class
		 * @param activation the activation kind which was requested from the extension
		 */
		TeamClassRecord(Class<?> clazz, ActivationKind activation) {
			this.clazz = clazz;
			this.activation = activation;
		}

		public TeamClassRecord(Bundle aspectBundle, String teamName, ActivationKind activation) throws Exception {
			this.activation = activation;
			this.aspectBundle = aspectBundle;
			this.teamName = teamName;
			// enable to revert to early loading:
			//this.clazz = this.aspectBundle.loadClass(this.teamName);
		}
		
		/** Constant from org.objectteams.Team acquired via reflection. */
		static Thread ALL_THREADS = null;
		
		/** Retrieve org.objectteams.Team.ALL_THREADS. */
		static synchronized Thread get_ALL_THREADS()
			throws Exception
		{
			if (ALL_THREADS == null) {
				Class<?> ooTeam = Class.forName("org.objectteams.Team");
				Field constantField = ooTeam.getDeclaredField("ALL_THREADS");
				ALL_THREADS = (Thread)constantField.get(null);
			}
			return ALL_THREADS;
		}
		void readOTAttributes(ClassScanner scanner)
				throws Exception, IOException
		{
			readOTAttributes(scanner, this.teamName);
		}
		
		void readOTAttributes(ClassScanner scanner, String teamName)
				throws Exception, IOException 
		{
			TransformerPlugin.getDefault().log(IStatus.OK, "reading attributes of team "+teamName);
			ClassLoader loader = (ClassLoader) ((BundleHost)aspectBundle).getLoaderProxy().getBundleLoader().createClassLoader();
			trying: {
				for(String candidateName : possibleTeamNames(teamName)) {
					try {
						scanner.readOTAttributes(aspectBundle, candidateName, loader);
						break trying;
					} catch (ClassNotFoundException e) {
						// keep going if we still have candidates
					}				
				}
				throw new ClassNotFoundException(teamName);
			}
			Collection<String> baseClassNames = scanner.getCollectedBaseClassNames(teamName);
			if (baseClassNames != null && !baseClassNames.isEmpty())
				TransformerPlugin.getDefault().storeAdaptedBaseClassNames(this.aspectBundle.getSymbolicName(), teamName, baseClassNames);
		}
		
		public boolean isAlreadyHandled() throws ClassNotFoundException {
			if (this.clazz == null)
				loadClass();
			return allInstantiatedTeams.contains(this.clazz);
		}

		public Object newInstance() throws Exception {
			if (this.clazz == null)
				loadClass();
			allInstantiatedTeams.add(this.clazz); // do this before accessing the constructor to avoid circularity (see Trac #257).
			Object newInstance = this.clazz.newInstance();
			return newInstance;
		}
		
		private void loadClass() throws ClassNotFoundException {
			for (String candidateName : possibleTeamNames(this.teamName)) {
				try {
					this.clazz = this.aspectBundle.loadClass(candidateName);
					return; 
				} catch (ClassNotFoundException ex) {
					// keep going if we still have candidates
				}
			}
			throw new ClassNotFoundException(this.teamName);
		}

		/** 
		 * Starting from currentName compute a list of potential binary names of (nested) teams
		 * using "$__OT__" as the separator, to find class parts of nested teams.  
		 */
		private List<String> possibleTeamNames(String currentName) {
			List<String> result = new ArrayList<String>();
			result.add(currentName);
			char sep = '.'; // assume source name
			if (currentName.indexOf('$') > -1)
				// binary name
				sep = '$';
			int from = currentName.length()-1;
			while (true) {
				int pos = currentName.lastIndexOf(sep, from);
				if (pos == -1)
					break;
				String prefix = currentName.substring(0, pos); 
				String postfix = currentName.substring(pos+1);
				if (sep=='$') {
					if (!postfix.startsWith("__OT__"))
						result.add(0, currentName = prefix+"$__OT__"+postfix);
				} else {
					// heuristic: 
					// only replace if parent element looks like a class (expected to start with uppercase)
					int prevDot = prefix.lastIndexOf('.');
					if (prevDot > -1 && Character.isUpperCase(prefix.charAt(prevDot+1))) 
						result.add(0, currentName = prefix+"$__OT__"+postfix);
					else 
						break;
				}
				from = pos-1;
			}
			return result;
		}

		public void markAsActivated() {
			allActivations.put(this.clazz, this.activation);			
		}
		
		public ActivationKind getActualActivation() {
			ActivationKind kind = allActivations.get(this.clazz);
			if (kind == null)
				return ActivationKind.NONE;
			return kind;
		}		
	}
	
	Bundle baseBundle;
	
	/** Team classes waiting for activation. */
	private List<TeamClassRecord> teamClasses = new ArrayList<TeamClassRecord>();
	
	private HashMap<Bundle,List<String>> baseBundleToAspectName = new HashMap<Bundle, List<String>>(); 

	public MasterTeamLoader(Bundle baseBundle) {
		this.baseBundle = baseBundle;
	}

	/** 
	 * Load all teams adapting baseBundle and the adapted base classes.
	 * TODO(SH): No checks are yet performed, whether the teams found in extensions actually
	 * match the given baseBundle.
	 *
	 * @param baseBundle   an adaptable base bundle which was just activated.
	 * @param classScanner helper for reading OT bytecode attributes.
	 * @param bindings     declared aspect bindings for this bundle
	 * @return whether or not teams have been loaded successfully
	 */
	public boolean loadTeams(Bundle baseBundle, ClassScanner classScanner, ArrayList<AspectBinding> bindings) 
	{
		for (final AspectBinding binding : bindings) {
			String aspectBundleName = binding.aspectPlugin;
			
			log(IStatus.OK, ">>> TransformerPlugin loading aspect plugin "+aspectBundleName+" <<<");
			
			final Bundle aspectBundle = OSGIUtils.getDefault().getBundle(aspectBundleName);
			if (aspectBundle == null) {
				Throwable t = new Exception("Aspect bundle "+aspectBundleName+" does not exist.");
				t.fillInStackTrace();
				log(t, "Failed to load teams for an aspect plugin.");
			}

			// load and store the team classes:
			for (int i = 0; i < binding.teamClasses.length; i++) {
				try {
					TeamClassRecord teamClassRecord = new TeamClassRecord(
												aspectBundle, binding.teamClasses[i],
												binding.activations[i]
										);
					this.teamClasses.add(teamClassRecord);
					teamClassRecord.readOTAttributes(classScanner); // disable to revert to early loading
					if (binding.subTeamClasses[i] != null)
						for (String subTeamName : binding.subTeamClasses[i])
							teamClassRecord.readOTAttributes(classScanner, subTeamName); // FIXME(SH): really use the same TeamClassRecord??

				} catch (Throwable t) {
					log(t, "Exception occurred while loading team class"); //$NON-NLS-1$				
				}
			}
			
			// store aspectBundleName for PHASE 2:
			List<String> aspectNames = baseBundleToAspectName.get(baseBundle);
			if (aspectNames == null) 
				baseBundleToAspectName.put(baseBundle, aspectNames = new ArrayList<String>()); 
			aspectNames.add(aspectBundleName);			
		}
		return !this.teamClasses.isEmpty();
	}

	/**
	 * Instantiate all team classes loaded before. 
	 * Before doing so all adapted base classes have to be loaded, too.
     * 
	 * @pre the AspectPermissionManager should be ready (ie., instance location is set), otherwise
	 * 	workspace settings have to be ignored (error should be signaled by client).
	 * 
	 * @param  baseBundle		 only teams adapting this base bundle should be instantiated
	 * @param  triggerClassname  loading of this class triggered this instantiation (may be null)
     * @param  permissionManager helper for checking permissions of aspectBinding / forcedExport
	 * @return list of team instances (maybe empty). Null signal that instantiation had to be deferred.
	 */
	public List<Object> instantiateLoadedTeams(Bundle baseBundle, String triggerClassname, AspectPermissionManager permissionManager) 
	{
		List<Object> teamInstances = new ArrayList<Object>(this.teamClasses.size());

		// permission checking can be performed now or later, depending on readiness:
		boolean permissionManagerReady = permissionManager.isReady();
		
		// ==== check permissions before we start activating:
		if (permissionManagerReady) { // otherwise we will register pending obligations below.
			boolean hasDenial = false;
			for (TeamClassRecord teamClass : this.teamClasses)
				if (!permissionManager.checkTeamBinding(teamClass.aspectBundle.getSymbolicName(), baseBundle.getSymbolicName(), teamClass.teamName)) 
				{
					hasDenial = true;
					try {
						teamClass.aspectBundle.stop();
						log(ILogger.ERROR, "Stopped bundle "+teamClass.aspectBundle.getSymbolicName()+" which requests unconfirmed aspect binding(s).");
					} catch (BundleException e) {
						log(e, "Failed to stop bundle "+teamClass.aspectBundle.getSymbolicName()+" which requests unconfirmed aspect binding(s).");
					}
				}
			if (hasDenial)
				return teamInstances; // still empty list
		}

		// ==== instantiate the teams:
		for (TeamClassRecord teamClass : this.teamClasses) {
			if (teamClass.activation ==  ActivationKind.NONE)
				continue;
			try {
				if (teamClass.isAlreadyHandled()) { // previously instantiated due to a different aspectBinding/basePlugin?
					ActivationKind actualActivation = teamClass.getActualActivation();
					if (   actualActivation     != teamClass.activation
						&& actualActivation     != ActivationKind.NONE
						&& teamClass.activation != ActivationKind.NONE)
					{
						log(IStatus.WARNING, "Conflicting activation requests in aspect bindings for team class "+teamClass.teamName);
					}
				} else {
					Object newTeam = teamClass.newInstance();
					teamInstances.add(newTeam);
				
					Class<?>[] activationArgumentTypes = new Class[0];
					Object[] activationArguments    = null;
					switch(teamClass.activation) {
					case ALL_THREADS:
						 activationArgumentTypes = new Class[]{Thread.class};
						 activationArguments = new Object[] {TeamClassRecord.get_ALL_THREADS()};
						 // fall through
					case THREAD:
						Method activationMethod = teamClass.clazz.getMethod("activate", activationArgumentTypes);
						activationMethod.invoke(newTeam, activationArguments);
						log(IStatus.OK, ">>> instantiated team: "+teamClass.clazz+", activation: "+teamClass.activation+" <<<");
						teamClass.markAsActivated();
						break;
					case NONE:
						// nothing ;-)
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (!permissionManagerReady)
			permissionManager.addBaseBundleObligations(teamInstances, new ArrayList<TeamClassRecord>(this.teamClasses), baseBundle);
		return teamInstances;
	}
	
	void log(int status, String msg) {
		TransformerPlugin.getDefault().log(status, msg);
	}
	void log(Throwable t, String msg) {
		TransformerPlugin.getDefault().log(t, msg);
	}
}
