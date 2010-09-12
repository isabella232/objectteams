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
 * $Id: SamplesAdapter.java 23482 2010-02-05 20:16:19Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 * IBM Corporation - copies of individual methods from bound base classes.
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.samples;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.objectteams.otdt.ui.help.OTHelpPlugin;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.osgi.framework.Bundle;

import base org.eclipse.pde.internal.ui.samples.ProjectNamesPage;
import base org.eclipse.pde.internal.ui.samples.SampleEditor;
import base org.eclipse.pde.internal.ui.samples.SampleOperation;
import base org.eclipse.pde.internal.ui.samples.SampleStandbyContent;
import base org.eclipse.pde.internal.ui.samples.SampleWizard;
import base org.eclipse.pde.internal.ui.samples.ShowSampleAction;

/**
 * @author gis
 */
@SuppressWarnings("restriction")
public team class SamplesAdapter
{
	private Map<IConfigurationElement,SampleWizardAdapter> _wizards = new HashMap<IConfigurationElement, SampleWizardAdapter>();
	
	boolean _needsAdaptation = false;
	
	public boolean isOTSample(String sampleId)
	{
		return sampleId != null && sampleId.startsWith("org.eclipse.objectteams.otdt.samples."); //$NON-NLS-1$
	}
	
	public class ShowSampleActionAdapter playedBy ShowSampleAction
	{

		@SuppressWarnings("basecall")
		callin boolean ensureSamplesPresent()
		{
			String id = getSampleId();
			if (id != null) {
				if (SamplesAdapter.this.isOTSample(id))
					return true; //OT-samples are definitely present
			}
			
			return base.ensureSamplesPresent();
		}
		@SuppressWarnings("decapsulation")
		String getSampleId() -> get String sampleId;
		@SuppressWarnings("decapsulation")
		boolean ensureSamplesPresent() <- replace boolean ensureSampleFeaturePresent();
	}
	
	public class ProjectNamesPageAdapter playedBy ProjectNamesPage
//		base when (SamplesAdapter.this.needsAdaptation())
	{
		private boolean _isInitialized;

		// bugfix: avoid executing updateEntries more than once!
		@SuppressWarnings("basecall")
		callin void updateEntries() {
		    if (_isInitialized)
		        return;
		    
		    _isInitialized = (getWizard().getSelection() != null);
		    
		    base.updateEntries();
		}
		
		@SuppressWarnings("decapsulation")
		SampleWizardAdapter getWizard() -> get SampleWizard wizard;
		@SuppressWarnings("decapsulation")
		void updateEntries() <- replace void updateEntries();
	}
	
	public class SampleWizardAdapter playedBy SampleWizard
		when (needsAdaptation())
	{
		IProject[] _createdProjects;
		
		void initializationHook()
		{
			// override default behavior
			setSampleEditorNeeded(false);
		}
		
		protected void setCreatedProjects(IProject[] projects)
		{
			_createdProjects = projects;
		}
		
		callin boolean performFinish()
		{
			SamplesAdapter.this._wizards.put(getSelection(), this);
			boolean result = base.performFinish();
			SamplesAdapter.this._wizards.remove(getSelection());
			
			return result;
		}
		
		
		@SuppressWarnings("basecall")
		callin void selectReveal(Shell shell)
		{
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					SampleWizardAdapter.this.doSelectReveal();
				}
			});
		}
		
		// provide commented out method of SampleWizard
		void doSelectReveal() {
//{ObjectTeams: added local variables for fields in base class
			IConfigurationElement selection = getSelection();
			IProject[] createdProjects = _createdProjects;
//carp}
			
			if (selection == null || createdProjects==null)
				return;
			String viewId = selection.getAttribute("targetViewId"); //$NON-NLS-1$
			if (viewId == null)
				return;
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window == null)
				return;
			IWorkbenchPage page = window.getActivePage();
			if (page == null)
				return;
			IViewPart view = page.findView(viewId);
			if (view == null || !(view instanceof ISetSelectionTarget))
				return;
			ISetSelectionTarget target = (ISetSelectionTarget) view;
			IConfigurationElement[] projects = selection.getChildren("project"); //$NON-NLS-1$
	
			ArrayList<IResource> items = new ArrayList<IResource>();
			for (int i = 0; i < projects.length; i++) {
				String path = projects[i].getAttribute("selectReveal"); //$NON-NLS-1$
				if (path == null)
					continue;
				IResource resource = createdProjects[i].findMember(path);
				if (resource != null && resource.exists()) {
					if (resource.getName().equals("Intro0.html")) { //$NON-NLS-1$
						resource = setBaseTag(createdProjects[i], resource);
					}
					items.add(resource);
				}
			}
			if (items.size() > 0)
				target.selectReveal(new StructuredSelection(items));
			
//{ObjectTeams: reveal in editor as well
			revealInEditor(items, page);
//carp}
		}

		/** Insert a HTML base-tag to enable relative navigation to resources in OTHelp. */
 		private IResource setBaseTag(IProject project, IResource resource) {
 			if (resource instanceof IFile) { 				
 				IFile file = (IFile)resource;
 				try {
 					NullProgressMonitor npm = new NullProgressMonitor();
 					
 					// fetch path to help plugin hosting images/ and guide/otjld/def/:
 					Bundle helpBundle = OTHelpPlugin.getDefault().getBundle();
 					String absPath = FileLocator.resolve(helpBundle.getEntry("/")).toString();  //$NON-NLS-1$
 					String baseTag = "<base href=\""+absPath+"\">\n"; //$NON-NLS-1$ //$NON-NLS-2$
 					
 					// assemble new new file with new content:
 					IFile newFile = project.getFile("Intro.html"); //$NON-NLS-1$
 					ByteArrayInputStream stream = new ByteArrayInputStream(baseTag.getBytes("UTF8")); //$NON-NLS-1$
					newFile.create(stream, false, npm);
 					newFile.appendContents(file.getContents(), 0, npm);
 					stream.close();
 					resource.delete(false, npm);
 					return newFile;
 				} catch (Exception e) {
 					OTSamplesPlugin.getDefault().getLog().log(
 						OTSamplesPlugin.createErrorStatus("Failed to convert Intro.html", e)); //$NON-NLS-1$
 				}
 			}	
 			return resource;
 		}

		void revealInEditor(List<IResource> items, IWorkbenchPage page)
		{
			if (items.size() > 0)
			{
				for (Iterator<IResource> iter = items.iterator(); iter.hasNext();)
		        {
		            IResource resource = iter.next();
		            if (resource instanceof IFile)
		            {
		                try {
		                    IDE.openEditor(page, (IFile)resource);
		                }
		                catch (PartInitException ex)
		                { /* ignore, user will try to open it manually, then */ } 
		            }
		        }
			}
		}
		
		boolean needsAdaptation()
		{
			IConfigurationElement selection = getSelection();
			if (selection == null) return false; // no sample selected
			String id = selection.getAttribute("id"); //$NON-NLS-1$
			return SamplesAdapter.this.isOTSample(id);
		}
		
		void initializationHook()      <- before void addPages();
		boolean performFinish()        <- replace boolean performFinish();
		void selectReveal(Shell shell) <- replace void selectReveal(Shell shell);

		void setSampleEditorNeeded(boolean sampleEditorNeeded) 
		                                     -> void setSampleEditorNeeded(boolean sampleEditorNeeded);
		IConfigurationElement getSelection() -> IConfigurationElement getSelection();
		
	}
	
	public class SampleOperationAdaptor playedBy SampleOperation
	{
		void fetchCreatedProjects()
		{
			IProject[] projects = getCreatedProjects();
			SampleWizardAdapter wizardAdapter = SamplesAdapter.this._wizards.get(getSample());
			if (wizardAdapter != null && projects != null)
				wizardAdapter.setCreatedProjects(projects);
		}
		
		// Original implementation doesn't copy the "launchTarget" attribute from configuration element to manifest file
		void fixCreatedSampleManifestContent(String projectName, Properties properties) 
		{
			final String attr = "launchTarget"; //$NON-NLS-1$
			writeProperty(properties, attr, getSample().getAttribute(attr));
		}
		
		void fetchCreatedProjects() <- after void run(IProgressMonitor monitor);
		
		void fixCreatedSampleManifestContent(String projectName, Properties properties) 
									<- after void createSampleManifestContent(String projectName, Properties properties);

		
		@SuppressWarnings("decapsulation")
		void writeProperty(Properties properties, String name, String value)
										  -> void writeProperty(Properties properties, String name, String value);
		
		IProject[] getCreatedProjects()   -> IProject[] getCreatedProjects();
		@SuppressWarnings("decapsulation")
		IConfigurationElement getSample() -> get IConfigurationElement sample;
	}
	
	/** This role generalizes over unrelated base classes SampleStandbyContent and SampleEditor. */
	public abstract class SampleRunner
	{
		private ILaunchShortcut launchShortcut;

		@SuppressWarnings("basecall")
		callin void doRun(String launcher, String target, final boolean debug) 
		{
			if (target != null && target.startsWith("org.eclipse.objectteams.")) //$NON-NLS-1$
			{
				final ISelection selection;
				try
				{
					Object launchSelection = getLaunchSelection(target);
					if (launchSelection != null) {
						selection = new StructuredSelection(launchSelection);
					} else
						selection = new StructuredSelection();

					final ILaunchShortcut fshortcut = getLaunchShortcut(launcher);

					BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
						public void run() {
							fshortcut.launch(selection, debug
									? ILaunchManager.DEBUG_MODE
											: ILaunchManager.RUN_MODE);
						}
					});
				} 
				catch (CoreException ex)
				{
					ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							SampleMessages.SamplesAdapter_unable_to_run, SampleMessages.SamplesAdapter_cannot_run_selected,
							ex.getStatus());
					OTSamplesPlugin.getDefault().getLog().log(ex.getStatus());
				}
			}
			else
			{
				base.doRun(launcher, target, debug);
			}
		}

		// NEW: target is the "launchTarget" property from the launchTarget attribute in the sample.properties
		//      file or the <sample> configuration element.
		//      Note: target may be null.
		// falls back to search a main-class in the first src-folder available.
		private Object getLaunchSelection(String target) throws JavaModelException
		{
			IProject project = getProject();
			if (project != null)
			{
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject.exists())
				{
					if (target != null)
					{
						IType targetType = javaProject.findType(target);
						if (targetType != null)
							return targetType;
					}
					
					IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
					for (int i = 0; i < packageFragmentRoots.length; i++)
					{
						IPackageFragmentRoot root = packageFragmentRoots[i];
						if (root.getKind() == IPackageFragmentRoot.K_SOURCE)
							return root;
					}
				}
			}
			
			return null;
		}

		private ILaunchShortcut getLaunchShortcut(String launcher) throws CoreException
		{
			if (launchShortcut != null && launchShortcut.getClass().getName().equals(launcher))
				return launchShortcut;
			
			try {
				Class<?> launcherClass = Class.forName(launcher);
				launchShortcut = (ILaunchShortcut) launcherClass.newInstance();
				return launchShortcut;
			}
			catch (Exception ex)
			{
				IStatus status = OTSamplesPlugin.createErrorStatus("Unable to create launcher", ex); //$NON-NLS-1$
				throw new CoreException(status);
			} 
		}

		// OT_COPY_PASTE: STATE: 3.2: most parts copy&paste from SampleStandbyContent.doBrowse()
		private IProject getProject()
		{
			IWorkspaceRoot root = PDEPlugin.getWorkspace().getRoot();
			IProject[] projects = root.getProjects();
			String sid = getSampleID();
			if (sid == null)
				return null;
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				if (!project.exists() || !project.isOpen())
					continue;
				IFile pfile = project.getFile("sample.properties"); //$NON-NLS-1$
				if (pfile.exists()) {
					try {
						InputStream is = pfile.getContents();
						Properties prop = new Properties();
						prop.load(is);
						is.close();
						String id = prop.getProperty("id"); //$NON-NLS-1$
						if (id != null && id.equals(sid)) {
							return project;
						}
					} catch (IOException e) {
						PDEPlugin.logException(e);
					} catch (CoreException e) {
						PDEPlugin.logException(e);
					}
				}
			}
			
			return null;
		}
		
		protected abstract String getSampleID();
	}
	
	// stupid, stupid...
	public class SampleStandbyContentAdaptor extends SampleRunner playedBy SampleStandbyContent
	{
		@SuppressWarnings("decapsulation")
		void doRun(String launcher, String target, final boolean debug) <- replace void doRun(String launcher, String target, final boolean debug);
		
		@SuppressWarnings("decapsulation")
		String getSampleID() -> get IConfigurationElement sample with {
			result <- sample.getAttribute("id") //$NON-NLS-1$
		}
	}

	public class SampleEditorAdaptor extends SampleRunner playedBy SampleEditor
	{
		@SuppressWarnings("decapsulation")
		void doRun(String launcher, String target, final boolean debug) <- replace void doRun(String launcher, String target, final boolean debug);

		@SuppressWarnings("decapsulation")
		String getSampleID() -> Properties loadContent() with {
			result <- result.getProperty("id") //$NON-NLS-1$
		}
	}
}
