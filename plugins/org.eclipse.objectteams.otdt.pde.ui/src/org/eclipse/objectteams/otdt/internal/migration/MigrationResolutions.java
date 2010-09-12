/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
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
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.migration;

import static org.eclipse.objectteams.otequinox.Constants.ASPECT_BINDING_FQEXTPOINT_ID;
import static org.eclipse.objectteams.otequinox.Constants.TRANSFORMER_PLUGIN_ID;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.internal.pde.ui.OTPDEUIPlugin;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.IPluginParent;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.IDocumentElementNode;
import org.eclipse.pde.internal.core.text.bundle.BundleModel;
import org.eclipse.pde.internal.core.text.bundle.PDEManifestElement;
import org.eclipse.pde.internal.core.text.bundle.RequireBundleHeader;
import org.eclipse.pde.internal.ui.correction.AbstractManifestMarkerResolution;
import org.eclipse.pde.internal.ui.correction.AbstractXMLMarkerResolution;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.osgi.framework.Constants;

/**
 * Provide quick-fixes ("resolutions") for updating old project setups.
 */
@SuppressWarnings("restriction")
public class MigrationResolutions implements IMarkerResolutionGenerator {

	/** 
	 * {@inheritDoc}
	 * Handles all problems relating to the migration org.objectteams -> org.eclipse.objectteams 
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		switch (MigrationMarkers.getProblemID(marker)) {
		case MigrationMarkers.PROBLEM_ID_BUILDER:
		case MigrationMarkers.PROBLEM_ID_NATURE:
			return new IMarkerResolution[] { new ProjectSetupResolution() };
		case MigrationMarkers.PROBLEM_ID_OTEQUINOX_BUNDLE:
			return new IMarkerResolution[] { new UpdateOTEquinoxDependency() };
		case MigrationMarkers.PROBLEM_ID_EXTENSIONPOINT:
			return new IMarkerResolution[] { new UpdateOTEquinoxExtensionpoint(marker) };
		}
		return new IMarkerResolution[0];
	}

	/** Fix .project: nature and builder. */
	private class ProjectSetupResolution implements IMarkerResolution 
	{
		public String getLabel() {
			return Messages.MigrationResolutions_update_nature_and_builder_label;
		}
		
		public void run(IMarker marker) {
			try {
				IProject project = (IProject) marker.getResource();
				IProjectDescription prjDesc = project.getDescription();
				
				prjDesc.setNatureIds(replaceOrAddOTJavaNature(prjDesc));
				
				prjDesc.setBuildSpec(replaceOrAddOTBuilder(prjDesc));
				
				project.setDescription(prjDesc, null);
			} catch (CoreException ce) {
				JavaCore.getJavaCore().getLog().log(new Status(IStatus.ERROR, OTPDEUIPlugin.PLUGIN_ID, 
												    "Error updating .project", ce)); //$NON-NLS-1$ 
			}
		}

		private String[] replaceOrAddOTJavaNature(IProjectDescription prjDesc) throws CoreException {
		    String[] natures = prjDesc.getNatureIds();
		    for(int i=0; i<natures.length; i++) {
		    	if (OTJProjectMigration.OLD_OT_NATURE.equals(natures[i])) {
		    		natures[i] = JavaCore.OTJ_NATURE_ID;
		    		return natures;
		    	} else if (JavaCore.OTJ_NATURE_ID.equals(natures[i])) {
		    		// already present, don't add again
		    		return natures;
		    	}
		    }
			return OTDTPlugin.createProjectNatures(prjDesc);
		}

		private ICommand[] replaceOrAddOTBuilder(IProjectDescription prjDesc) {
			ICommand[] buildSpecs = prjDesc.getBuildSpec();
			ICommand otBuildCmd = OTDTPlugin.createProjectBuildCommand(prjDesc);
			
			for(int i=0; i<buildSpecs.length; i++) {
				if (OTJProjectMigration.OLD_OT_BUILDER.equals(buildSpecs[i].getBuilderName())) {
					buildSpecs[i] = otBuildCmd;
					return buildSpecs;
				} else if (JavaCore.OTJ_BUILDER_ID.equals(buildSpecs[i].getBuilderName())) {
					// already present, don't add again
					return buildSpecs;
				}
			}
			int len = buildSpecs.length;
			System.arraycopy(buildSpecs, 0, buildSpecs = new ICommand[len+1], 0, len);
			buildSpecs[len] = otBuildCmd;
			return buildSpecs;
		}
	}
	
	/** Fix MANIFEST.MF: update dependency on org.[eclipse.]objectteams.otequinox. */
	protected class UpdateOTEquinoxDependency extends AbstractManifestMarkerResolution
	{	
		public UpdateOTEquinoxDependency() {
			super(-1); // arg 'type' is not used
		}
	
		public String getLabel() {
			return Messages.MigrationResolutions_update_bundle_name_label;
		}

		protected void createChange(BundleModel model) {
	
			IManifestHeader header = model.getBundle().getManifestHeader(Constants.REQUIRE_BUNDLE);
			if (header == null || ! (header instanceof RequireBundleHeader))
				return;

			for (PDEManifestElement required : ((RequireBundleHeader)header).getElements()) {
				if (OTEquinoxMigration.OLD_OTEQUINOX_NAME.equals(required.getValue())) {
					required.setValue(TRANSFORMER_PLUGIN_ID);
					header.update(true);
					return;
				}
			}			
		}
	}

	/** Fix plugin.xml: update aspectBindings extension. */
	protected class UpdateOTEquinoxExtensionpoint extends AbstractXMLMarkerResolution
	{	
		static final String ICON = "icon";   //$NON-NLS-1$
		static final String ORG_OBJECTTEAMS = "org.objectteams"; //$NON-NLS-1$
		static final String ORG_ECLIPSE_OBJECTTEAMS = "org.eclipse.objectteams"; //$NON-NLS-1$
		
		public UpdateOTEquinoxExtensionpoint(IMarker marker) {
			super(-1, marker); // type is not used
		}
		
		public String getLabel() {
			return Messages.MigrationResolutions_update_extension_label;
		}
	
		protected void createChange(IPluginModelBase model) {
	
			IExtensions extensions = model.getExtensions();
			if (extensions == null)
				return;
			
			for (IPluginExtension extension : extensions.getExtensions()) {
				if (OTEquinoxMigration.OLD_OTEQUINOX_EXTENSIONPOINT.equals(extension.getPoint())) {
					try {
						extension.setPoint(ASPECT_BINDING_FQEXTPOINT_ID);
						// also update icons (in aspectBinding, basePlugin and team elements):
						for (IPluginObject elem : extension.getChildren())
							if (elem instanceof IPluginParent) {
								updateIcon(elem);
								for (IPluginObject grandChild : ((IPluginParent) elem).getChildren())
									updateIcon(grandChild);
							}
					} catch (CoreException ce) {
						JavaCore.getJavaCore().getLog().log(new Status(IStatus.ERROR, OTPDEUIPlugin.PLUGIN_ID, 
																	   "Error updating plugin.xml", ce)); //$NON-NLS-1$
					}
				}
			}
		}

		// icons are specified relative to the otdt.ui plugin, update FQN
		private void updateIcon(IPluginObject element) {
			if (element instanceof IDocumentElementNode) {
				IDocumentElementNode node = (IDocumentElementNode)element;
				String icon = node.getXMLAttributeValue(ICON); 
				if (icon.contains(ORG_OBJECTTEAMS))
					node.setXMLAttribute(ICON, icon.replace(ORG_OBJECTTEAMS, ORG_ECLIPSE_OBJECTTEAMS));
			}
		}
	}
}
