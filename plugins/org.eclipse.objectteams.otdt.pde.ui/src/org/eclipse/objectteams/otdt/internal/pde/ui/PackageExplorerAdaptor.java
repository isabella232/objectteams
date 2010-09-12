/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PackageExplorerAdaptor.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.objectteams.otequinox.Constants;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.ISharedExtensionsModel;
import org.eclipse.pde.core.plugin.ISharedPluginModel;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.pde.internal.core.text.IDocumentAttributeNode;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.plugin.ExtensionsPage;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import base org.eclipse.jdt.internal.ui.packageview.PackageExplorerContentProvider;
import base org.eclipse.jdt.internal.ui.packageview.PackageExplorerLabelProvider;
import base org.eclipse.jdt.ui.JavaElementComparator;
import base org.eclipse.jdt.ui.actions.OpenAction;
import base org.eclipse.pde.internal.core.text.plugin.PluginElementNode;

/**
 * This team adds rendering for an "Aspect Bindings" subtree for each OT/Equinox project.
 * @author stephan
 * @since 1.3.2
 */
@SuppressWarnings("restriction")
public team class PackageExplorerAdaptor 
{
	
	/** Icon for base plugin with forced exports. */
	static final String PLUGIN_FORCED_EXPORTS = "plugin_forcedExports.png"; //$NON-NLS-1$

	protected class ContentProvider playedBy PackageExplorerContentProvider {

		@SuppressWarnings("decapsulation")
		TreeViewer getViewer() -> get TreeViewer fViewer;

		Object[] getChildren(Object parentElement) <- replace Object[] getChildren(Object parentElement);

		callin Object[] getChildren(Object parentElement) {
			Object[] result = base.getChildren(parentElement);
			if (parentElement instanceof IJavaProject) {
				// may want to add an "OT/Equinox Aspect Bindings" node to the project:
				PluginModelManager modelManager = PDECore.getDefault().getModelManager();
				IJavaProject javaProject = (IJavaProject)parentElement;
				IPluginModelBase pluginModel = modelManager.findModel(javaProject.getProject());
				if (pluginModel != null) {
					if (hasAspectBindings(pluginModel)) {
						List<Object> combined = new ArrayList<Object>(Arrays.asList(result));
						combined.add(createAspectBindingsNode(javaProject, pluginModel));
						return combined.toArray();
					}
				}
				// no plugin.xml or no aspect bindings, wait for changes re plugin.xml
				ResourcesPlugin.getWorkspace().addResourceChangeListener(new AspectBindingsRefresher(javaProject, null, getViewer()));
			}
			if (parentElement instanceof AspectBindingsRootNode) {
				// this is the "OT/Equinox Aspect Bindings" node, fill it with BasePluginNodes
				AspectBindingsRootNode aspectBindingsNode = (AspectBindingsRootNode)parentElement;
				IExtensions extensions = aspectBindingsNode.pluginModel.getExtensions();
				if (extensions != null) {
					List<Object> bases = new ArrayList<Object>();
					Map<String, BasePluginNode> perBaseBindings = new HashMap<String, BasePluginNode>();
					for (IPluginExtension extension : extensions.getExtensions())
						if(extension.getPoint().equals(Constants.ASPECT_BINDING_FQEXTPOINT_ID))
							for (IPluginObject element : extension.getChildren())
								if (element instanceof PluginElement) {
									PluginElement pluginElement = (PluginElement) element;
									BasePluginNode node = new BasePluginNode(aspectBindingsNode.javaProject, pluginElement);
									// collate multiple bindings for the same base plugin:
									if (perBaseBindings.containsKey(node.basePlugin)) {
										perBaseBindings.get(node.basePlugin).merge(node);
									} else {
										bases.add(node);
										perBaseBindings.put(node.basePlugin, node);
									}
								}

					return bases.toArray();
				}
			}
			if (parentElement instanceof BasePluginNode) {
				// fill the BasePluginNode with its adapting teams
				return ((BasePluginNode)parentElement).getTeams();
			}
			return result;
		}
		
		AspectBindingsRootNode createAspectBindingsNode (final IJavaProject javaProject, IPluginModelBase pluginModel) {
			final AspectBindingsRootNode aspectBindings = new AspectBindingsRootNode(javaProject, pluginModel);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(new AspectBindingsRefresher(javaProject, aspectBindings, getViewer()));
			return aspectBindings;
		}		
	}

	/** Does the given plugin have an aspectBindings extension? */
	boolean hasAspectBindings (IPluginModelBase pluginModel) {
		IExtensions extensions = pluginModel.getExtensions();
		for (IPluginExtension extension : extensions.getExtensions())
			if(extension.getPoint().equals(Constants.ASPECT_BINDING_FQEXTPOINT_ID))
				return true;
		return false;
	}

	/** This class is responsible for any updates on aspectBindings in a project's plugin.xml. */
	protected class AspectBindingsRefresher implements IResourceChangeListener {
		IJavaProject javaProject;
		AspectBindingsRootNode aspectBindings;
		TreeViewer viewer;
		
		protected AspectBindingsRefresher(IJavaProject javaProject, AspectBindingsRootNode aspectBindings, TreeViewer viewer) {
			this.javaProject = javaProject;
			this.aspectBindings = aspectBindings;
			this.viewer = viewer;
		}
		public void resourceChanged(IResourceChangeEvent event) {
			if (aspectBindings != null)
				refreshAspectBindings(event);
			else 
				detectPluginXmlChanges(event.getDelta());
		}
		/* Aspect bindings are present, may need to be refreshed or removed. */
		void refreshAspectBindings(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			if (delta != null)
				delta = delta.findMember(this.javaProject.getPath().append("plugin.xml")); //$NON-NLS-1$
			if (delta != null) {
				if (this.viewer == null || this.viewer.getControl().isDisposed() || !this.javaProject.isOpen()) {
					ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
					return;
				}
				final Object elementToRefresh;
				if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
					elementToRefresh = this.javaProject;
				} else {
					PluginModelManager modelManager = PDECore.getDefault().getModelManager();
					if (hasAspectBindings(modelManager.findModel(javaProject.getProject())))
						elementToRefresh = this.aspectBindings;
					else
						elementToRefresh = this.javaProject;
				}
				Display.getDefault().asyncExec(new Runnable() { public void run() {
					AspectBindingsRefresher.this.viewer.refresh(elementToRefresh);
				}});
				// during project refresh, getChildren will create a new listener
				if (elementToRefresh == this.javaProject)
					ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
			} else if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
				if (event.getResource().equals(javaProject.getProject()))
					ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
			}			
		}
		/* Aspect bindings are not yet present, wait for an event that could signal addition of aspect bindings. */
		void detectPluginXmlChanges(IResourceDelta delta) {
			if (delta != null)
				delta = delta.findMember(this.javaProject.getPath().append("plugin.xml")); //$NON-NLS-1$
			if (delta != null) {
				// this listener is done:
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
				
				if (this.viewer == null || this.viewer.getControl().isDisposed() || !javaProject.isOpen())
					return;

				Display.getDefault().asyncExec(new Runnable() { public void run() {
					AspectBindingsRefresher.this.viewer.refresh(AspectBindingsRefresher.this.javaProject);
				}});
			}
		}
	}

	/** 
	 * This role renders the elements of an "Aspect Bindings" subtree.
	 */
	protected class LabelProvider playedBy PackageExplorerLabelProvider {
		
		PDELabelProvider pdeLabelProvider;

		// ---------- Lifecycle: ---------- 

		// constructor:
		@SuppressWarnings("ambiguouslowering")
		LabelProvider(PackageExplorerLabelProvider baseLabelProvider) {
			this.pdeLabelProvider = PDEPlugin.getDefault().getLabelProvider();
			pdeLabelProvider.connect(this);
		}
		
		// finalizer:
		@Override
		@SuppressWarnings("ambiguouslowering")
		protected void finalize() {
			if (this.pdeLabelProvider != null) {
				this.pdeLabelProvider.disconnect(this);
				this.pdeLabelProvider = null;
			}
		}
		// second trigger for finalizer:
		finalize <- after dispose;
		
		// ---------- Domain behavior: ----------
		
		StyledString getStyledText(Object element) 
		<- replace StyledString getStyledText(Object element);

		@SuppressWarnings("basecall")
		callin StyledString getStyledText(Object element)
		{
			if (element instanceof AspectBindingsRootNode)
				return new StyledString(AspectBindingsRootNode.ASPECT_BINDINGS_NAME);
			
			if (element instanceof BasePluginNode)
				return new StyledString("Base Plugin "+((BasePluginNode)element).basePlugin);
			
			if (element instanceof TeamNode)
				return new StyledString(((TeamNode)element).teamName);
			
			return base.getStyledText(element);
		}

		Image getImage(Object element) <- replace Image getImage(Object element);


		@SuppressWarnings("basecall")
		callin Image getImage(Object element) 
		{
			if (element instanceof AspectBindingsRootNode)
				return org.eclipse.objectteams.otdt.ui.ImageManager.getSharedInstance().get(ImageManager.CALLOUTBINDING_IMG);
			
			if (element instanceof BasePluginNode) {
				if (((BasePluginNode)element).hasForcedExports)
					return ImageManager.getSharedInstance().get(PLUGIN_FORCED_EXPORTS);
				return pdeLabelProvider.get(PDEPluginImages.DESC_PLUGIN_OBJ);
			}
			
			if (element instanceof TeamNode)
				return org.eclipse.objectteams.otdt.ui.ImageManager.getSharedInstance().get(ImageManager.TEAM_IMG);
			
			return base.getImage(element);
		}
	}
	
	/** 
	 * sort the "Aspect Bindings" node below the list of package fragment roots,
	 * by pretending it were a compilation unit
	 */
	protected class Comparator playedBy JavaElementComparator {

		@SuppressWarnings("decapsulation")
		int getCOMPILATIONUNITS() -> get int COMPILATIONUNITS;
		
		final int COMPILATIONUNITS;
		Comparator(JavaElementComparator b) {
			COMPILATIONUNITS = getCOMPILATIONUNITS();
		}

		int category(Object element) <- replace int category(Object element)
			base when (element instanceof AspectBindingsRootNode);

		@SuppressWarnings("basecall")
		callin int category(Object element) {
			return COMPILATIONUNITS;
		}
		
	}
	
	protected class Open playedBy OpenAction {

		@SuppressWarnings("decapsulation")
		boolean checkEnabled(IStructuredSelection selection) <- replace boolean checkEnabled(IStructuredSelection selection);

		@SuppressWarnings("rawtypes") // selection.iterator() is raw type
		callin boolean checkEnabled(IStructuredSelection selection) {
			if (base.checkEnabled(selection))
				return true;
			// similar to base method:
			if (selection.isEmpty())
				return false;
			for (Iterator iter= selection.iterator(); iter.hasNext();) {
				Object element= iter.next();
				if (element instanceof BasePluginNode)
					continue;
				if (element instanceof TeamNode)
					continue;
				return false;
			}
			return true;
		}

		Object getNodeToOpen(Object object) <- replace Object getElementToOpen(Object object) 
			base when (object instanceof AspectBindingsTreeNode);

		@SuppressWarnings("basecall")
		callin Object getNodeToOpen(Object object) {
			if (object instanceof BasePluginNode) {
				registerListener(((BasePluginNode)object));
				return ((BasePluginNode)object).getPluginXml();
			}
			if (object instanceof TeamNode)
				return ((TeamNode)object).getTeamType();
			return object;
		}
		/** register a listener for deferred selection of the current base plugin element within the extension editor. */
		void registerListener (final BasePluginNode node) {
			IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i= 0, length= windows.length; i < length; i++) {
				final IPartService partService = windows[i].getPartService();
				partService.addPartListener(new IPartListener() {
					public void partOpened(IWorkbenchPart part) 		{ /* nop */ }
					public void partDeactivated(IWorkbenchPart part) 	{ /* nop */ }
					public void partClosed(IWorkbenchPart part) 		{ /* nop */ }
					public void partBroughtToTop(IWorkbenchPart part) 	{ /* nop */ }
					
					public void partActivated(IWorkbenchPart part) {
						selectBaseNode(node, part);
						partService.removePartListener(this); // immediately remove: this is a one-shot listener
					}
				});
			}
		}
		void selectBaseNode(BasePluginNode node, IWorkbenchPart part) {
			if (part instanceof ManifestEditor) {
				ManifestEditor editor = (ManifestEditor) part;
				ExtensionsPage page = (ExtensionsPage) editor.setActivePage(ExtensionsPage.PAGE_ID);
				ISharedExtensionsModel extensions = ((IBundlePluginModelBase)page.getModel()).getExtensionsModel();
				for (BasePluginNodeInterceptor viewNode : getAllRoles(BasePluginNodeInterceptor.class))
					if (   extensions == viewNode.getModel()		// only nodes of this editor's model
					    && node.basePlugin.equals(viewNode.basePluginName))
						page.selectReveal(viewNode.lower());
			}
		}
	}
	/** This role intercepts reading of PluginElementNodes that represent a basePlugin element of an aspect binding. */
	protected class BasePluginNodeInterceptor implements ILowerable playedBy PluginElementNode 
	{
		ISharedPluginModel getModel() -> ISharedPluginModel getModel();

		protected String basePluginName;

		// this callin filters applicable base objects
		void register(String tag) <- after void setXMLTagName(String tag)
			base when ("basePlugin".equals(tag)); //$NON-NLS-1$

		private void register(String tag) {
			// nop, just registered this role
		}

		// this callin collects further information from registered base objects:
		void setXMLAttribute(IDocumentAttributeNode attribute) <- after void setXMLAttribute(IDocumentAttributeNode attribute)
			base when (hasRole(base, BasePluginNodeInterceptor.class));
		
		void setXMLAttribute(IDocumentAttributeNode attribute) {
			if ("id".equals(attribute.getAttributeName())) //$NON-NLS-1$
				this.basePluginName = attribute.getAttributeValue();
		}
	}
}
