/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AdaptorActivator.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import static org.eclipse.core.resources.IncrementalProjectBuilder.FULL_BUILD;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.ClasspathAccessRule;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.pde.internal.core.RequiredPluginsClasspathContainer;
import org.objectteams.LiftingVetoException;
import org.objectteams.Team;

import base org.eclipse.jdt.core.JavaCore;
import base org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import base org.eclipse.jdt.internal.core.JavaProject;
import base org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder;
import base org.eclipse.jdt.internal.core.builder.JavaBuilder;
import base org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;

/**
 * This team is activated via the OT/Equinox transformer plugin.
 * It controls the activation of the BuildManager and BaseImportChecker teams.
 * 
 * Control entry for BuildManager: IncrementalImageBuilder.
 * Control entries for BaseImportChecker: JavaBuilder and CompilationUnitProblemFinder
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class AdaptorActivator 
{
	/**
	 * Name of classpath attributes denoting the base bundle from which a classpath entry originates.
	 * Persistently stored so we can later associate aspect binding data to that classpath entry. 
	 */
	public static final String CPENTRY_ATTR_ORIGIN_BASE_BUNDLE = "org.eclipse.objectteams.originBaseBundle"; //$NON-NLS-1$

	/**
	 * Copy of a constant from PDECore to reduce dependencies (must be compared with .equals())).
	 */
	public static final IPath REQUIRED_PLUGINS_CONTAINER_PATH = new Path("org.eclipse.pde.core.requiredPlugins"); //$NON-NLS-1$

	
	public static AdaptorActivator instance;
	public AdaptorActivator() {
		instance= this;
	}

	/** 
	 * Pass the AspectBindingReader from a JavaProject to its RequiredPluginsClasspathContainer,
	 * and pass aspect binding data from the reader to individual classpath entries having a 
	 * originBaseBundle attribute.
	 */
	@SuppressWarnings("decapsulation") // base class is final
	protected class JavaCore playedBy JavaCore 
	{
		IClasspathAttribute newClasspathAttribute(String name, String value) -> IClasspathAttribute newClasspathAttribute(String name, String value);
		
// (see TODO above) Attention: 
// this solution would require partial array lifting if an element throws LiftingVetoException:
//		void setClasspathContainer(OTEquinoxProject[] javaProjects, IClasspathContainer[] container) 
		void setClasspathContainer(IJavaProject[] javaProjects, IClasspathContainer[] containers) 
		<- before void setClasspathContainer(IPath container, IJavaProject[] affectedProjects, IClasspathContainer[] respectiveContainers, IProgressMonitor pm)
		   with { javaProjects <- affectedProjects, 
			      containers   <- respectiveContainers
		   }
//		static void setClasspathContainer(OTEquinoxProject[] javaProject, IClasspathContainer[] container) 
		static void setClasspathContainer(IJavaProject[] javaProjects, IClasspathContainer[] containers) 
		{
			try {
				for (int i=0; i<javaProjects.length; i++) {
					Project project= (Project)javaProjects[i].getProject();
					if (   containers[i] instanceof RequiredPluginsClasspathContainer // checking the name is not enough, could still be a UpdatedClasspathContainer
						&& ProjectUtil.isOTPluginProject(project)) // avoid LiftingVetoException
					{
						AspectBindingReader aspectBindingReader = ResourceProjectAdaptor.getDefault().getAspectBindingReader(project);
						if (PDEAdaptor.instance != null)
							//otherwise PDEAdaptor has not been activated yet (PDE neither) hoping this can be ignored.
							PDEAdaptor.instance.setAspectBindingReader(aspectBindingReader, (RequiredPluginsClasspathContainer)containers[i]);
						else
							org.eclipse.jdt.core.JavaCore.getJavaCore().getLog().log(new Status(Status.WARNING, "org.eclipse.objectteams.otdt.internal.compiler.adaptor",  //$NON-NLS-1$
																				     "PDEAdaptor not yet initialized while setting classpath for "+project.getName())); //$NON-NLS-1$
					}
				}
			} catch (LiftingVetoException lve) {
				// ignore, aspect just didn't apply
			} catch (Throwable t) {
				org.eclipse.jdt.core.JavaCore.getJavaCore().getLog().log(new Status(Status.ERROR, "org.eclipse.objectteams.otdt.internal.compiler.adaptor", "Error initializing AspectBindingReader", t)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		void addBaseBundleAttribute(IAccessRule[] accessRules, IClasspathAttribute[] extraAttributes) 
			<- replace IClasspathEntry newLibraryEntry(IPath path1, IPath path2, IPath path3, IAccessRule[] accessRules, IClasspathAttribute[] extraAttributes, boolean isExported)
			when (accessRules != null && accessRules.length > 0)
			with { accessRules <- accessRules, extraAttributes <- extraAttributes }

		void addBaseBundleAttribute(IAccessRule[] accessRules, IClasspathAttribute[] extraAttributes) 
			<- replace IClasspathEntry newProjectEntry(IPath path, IAccessRule[] accessRules, boolean combineAccessRules, IClasspathAttribute[] extraAttributes, boolean isExported)
			when (accessRules != null && accessRules.length > 0)
			with { accessRules <- accessRules, extraAttributes <- extraAttributes }

		/**
		 * when creating a new classpath entry for a require plugin, check whether this plugin is affected by aspect bindings.
		 * If so, add a classpath attribute to denote the base plugin from which this entry originates.
		 * If a package is split among several plugins, a rule may carry one aspectBindingData for each part of the split.
		 */
		static callin void addBaseBundleAttribute(IAccessRule[] accessRules, IClasspathAttribute[] extraAttributes) {
			if (accessRules[0] instanceof ClasspathAccessRule) {
				int len = extraAttributes.length;
				int count = 0;
				// filter existing attributes:
				for (int i = 0; i < len; i++) {
					if (!extraAttributes[i].getName().startsWith(CPENTRY_ATTR_ORIGIN_BASE_BUNDLE)) {
						if (count != i)
							extraAttributes[count] = extraAttributes[i];
						count++;
					}
				}
				// add new extraAttribute(s):
				Object[] datas = ((ClasspathAccessRule)accessRules[0]).aspectBindingData;
				int i = 0;
				if (datas != null)
					for (Object data : datas)
						if (data instanceof AdaptedBaseBundle) {
							// note: more than one data is rather infrequent, no need to optimize array copying
							int idx = 0;
							if (count < len) {
								idx = count++;
							} else {
								System.arraycopy(extraAttributes, 0, extraAttributes=new IClasspathAttribute[len+1], 1, len);
							}
							String baseBundleName = ((AdaptedBaseBundle) data).getSymbolicName();
							extraAttributes[idx] = newClasspathAttribute(CPENTRY_ATTR_ORIGIN_BASE_BUNDLE+(i++), baseBundleName);
						}
				// compact the array, if existing extraAttributes are being removed:
				if (count < len)
					System.arraycopy(extraAttributes, 0, extraAttributes=new IClasspathAttribute[count], 0, count);
			}
			base.addBaseBundleAttribute(accessRules, extraAttributes);
		}		
	}

	/** 
	 * When a JavaProject computes its package fragment roots from its classpath entries,
	 * enhance the classpath entries with aspect binding data, using the classpath attribute
	 * to determine the corresponding base bundle.
	 * 
	 * @author stephan
	 * @since 1.2.5
	 */
	protected class CPEntryEnhancer playedBy JavaProject {

		IProject getProject() -> IProject getProject();

		@SuppressWarnings("rawtypes")
		void enhanceCPEntries(IClasspathEntry[] resolvedEntries) 
			<- before void computePackageFragmentRoots(IClasspathEntry[] resolvedEntries, ObjectVector accumulatedRoots,
													   HashSet rootIDs, IClasspathEntry referringEntry,
													   boolean retrieveExportedRoots, Map rootToResolvedEntries);

		/**
		 * @param entries the entries whose access rules to enhance
		 * @throws LiftingVetoException when the project is not an OT/Equinox project.
		 */
		void enhanceCPEntries(IClasspathEntry[] entries) throws LiftingVetoException 
		{
			IProject project = getProject();
			AspectBindingReader reader = ResourceProjectAdaptor.getDefault().getAspectBindingReader((Project)project);
			for (IClasspathEntry entry : entries) {
				IClasspathAttribute[] attributes = entry.getExtraAttributes();
				attributes_loop:
				for (IClasspathAttribute attribute : attributes) {
					if (attribute.getName().startsWith(CPENTRY_ATTR_ORIGIN_BASE_BUNDLE)) {
						AdaptedBaseBundle aspectBindingData = reader.getAdaptationInfo(attribute.getValue());
						if (aspectBindingData == null) continue; // means reader and attr are inconsistent
						rules_loop:
						for (IAccessRule rule : entry.getAccessRules())
							if (rule instanceof ClasspathAccessRule) 
								if (!PDEAdaptor.addAspectBindingData((ClasspathAccessRule)rule, aspectBindingData))
									break rules_loop; // when not adding assume all rules share the same aspect data
						break attributes_loop;
					}
				}
			}
		}
	}
	
	/** 
	 * Interface to the controlling builder. Tasks:
	 * <ul>
	 * <li>Life-cycle management for the BuildManager</li>
	 * <li>cflow like bracket [initializeBuilder,cleanup] 
	 *     for temporary activation of BaseImportChecker.
	 *     Note, that build() is not a suitable join point, 
	 *     because javaProject is not yes assigned.</li>     
	 * </ul>
	 */
	protected class JavaBuilderObserver playedBy JavaBuilder 
	{	
		@SuppressWarnings("decapsulation")
		Project getProject() -> get JavaProject javaProject
			with { result <- (Project)javaProject.getProject() }
		
		Team projectWatcher= null;
		
		// initialize local data structures when a full build is started: 
		void initialize(int kind) <- after int initializeBuilder(int kind, boolean forBuild);
		void initialize(int kind) {
			if (kind == FULL_BUILD)
				manager.initializeDependencyStorage();
			try {
				this.projectWatcher= ResourceProjectAdaptor.getDefault().getChecker(getProject());
			} catch (LiftingVetoException lve) {
				this.projectWatcher= new PlainProjectWatcher();
			}
			this.projectWatcher.activate();
		}
		
		cleanup <- after cleanup;
		void cleanup() {
			if (this.projectWatcher != null)
				this.projectWatcher.deactivate();
			this.projectWatcher= null;
		}
		
		// need to recompute the classpath if aspect binding data have changed:
		@SuppressWarnings("decapsulation")
		boolean aspectBindingHasChanged() <- replace boolean hasClasspathChanged();
		@SuppressWarnings("basecall")
		callin boolean aspectBindingHasChanged() {
			try {
				if (ResourceProjectAdaptor.getDefault().hasAspectDataChanged(getProject()))
					return true;
			} catch (LiftingVetoException lve) {
				// thrown while lifting project, means that javaProject is not OT-Project.
			}
			return base.aspectBindingHasChanged();
		}

	}

	/** 
	 * This role observes another entry into the compiler to activate a BaseImportChecker if needed.
	 */
	protected class CompilationUnitProblemFinder playedBy CompilationUnitProblemFinder 
	{
		@SuppressWarnings("rawtypes")
		void activateChecker(ICompilationUnit unitElement)
		<- replace CompilationUnitDeclaration process(CompilationUnit unitElement, 
													  SourceElementParser parser,
													  WorkingCopyOwner workingCopyOwner,
													  HashMap problems,
													  boolean creatingAST,
													  int reconcileFlags,
													  IProgressMonitor monitor)
			with { unitElement <- unitElement }
	
		static callin void activateChecker(ICompilationUnit unitElement)
				throws JavaModelException
		{	
			within (getChecker(unitElement)) 
			 	base.activateChecker(unitElement);
		}

		static Team getChecker(ICompilationUnit unitElement) {			
			try {
				IProject project= ProjectUtil.safeGetOTPluginProject(unitElement);
				if (project != null) {
					Team baseChecker= ResourceProjectAdaptor.getDefault().getChecker(project);
					if (baseChecker != null)
						return baseChecker;
				}
			} catch (LiftingVetoException lve) {
				// shouldn't happen, have checked above.
			}
			return new PlainProjectWatcher(); // fallback for non OT-Plugin projects
		}
	}

	private BuildManager manager = new BuildManager();
	
	/**
	 * This role observes the IncrementalImageBuilder.
	 * It enables all callins of BuildManager only while 
	 * performing an incremental build (method build(deltas)). 
	 */
	protected class BuilderGuard playedBy IncrementalImageBuilder 
	{
		build <- replace build;
		callin boolean build(SimpleLookupTable deltas) 
		{
			// Activation only for this thread/control flow:
			within (manager) {
				return base.build(deltas);
			}
		}
	}
	
	/**
	 * This role class simply tracks all executions of CopyInheritance.copyRole(..)
	 * 
	 * Purpose: collect data for recompiling sub-teams if tsuper roles have been changed.
	 */
	protected class CopyInheritanceObserver playedBy CopyInheritance 
	{
		// This trigger applies to source and binary tsupers being copied into source:
		void observeCopyRole(ReferenceBinding superRole, char[] subTeamFileName)
			<- after TypeDeclaration copyRole(ReferenceBinding tsuperRole, 
											  boolean isNestedType, 
							                  TypeDeclaration subTeamDecl, 
							                  boolean isTsuperTeam)
		base when (result != null)
		with  { superRole       <- tsuperRole, 
		        subTeamFileName <- subTeamDecl.compilationResult.getFileName() }

		// static for optimization: avoid lifting.
		static void observeCopyRole(ReferenceBinding superRole, char[] subTeamFileName) 
		{
			if (superRole.enclosingType().id == IOTConstants.T_OrgObjectTeamsTeam)
				return;
			if (subTeamFileName == null || subTeamFileName[0] != '/')
				return; // only useful if an absolute path is given.
			
			// no need to recompile these: 
			char[] superRoleName = superRole.internalName();
			if (   BuildManager.isPredefinedRole(superRoleName) 
				|| OTNameUtils.isTSuperMarkerInterface(superRoleName))
				return;
			AdaptorActivator.this.manager.recordCopiedRole(superRole.attributeName(), subTeamFileName); 
		}
		
		@SuppressWarnings("decapsulation")
		boolean shouldPreserveBinary(ReferenceBinding role, CompilationResult cResult) 
			<-replace boolean shouldPreserveBinaryRole(ReferenceBinding role, CompilationResult cResult);
		static callin boolean shouldPreserveBinary(ReferenceBinding role, CompilationResult cResult) 
		{
			if (!base.shouldPreserveBinary(role, cResult))
				return false;
			if (!AdaptorActivator.this.manager.isActive())
				return true;
			return AdaptorActivator.this.manager.shouldPreserveBinaryRole(role, cResult);
		}
	}
}
