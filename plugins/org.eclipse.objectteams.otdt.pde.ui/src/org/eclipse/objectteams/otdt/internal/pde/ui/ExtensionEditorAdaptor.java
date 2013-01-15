/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ExtensionEditorAdaptor.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages;
import org.eclipse.objectteams.otdt.ui.ImageManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.objectteams.otequinox.Constants;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.internal.core.ischema.IMetaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchemaElement;
import org.eclipse.pde.internal.core.plugin.PluginAttribute;
import org.eclipse.pde.internal.core.schema.SchemaRegistry;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.parts.StructuredViewerPart;
import org.eclipse.pde.internal.ui.util.PDEJavaHelperUI;

import base org.eclipse.pde.internal.ui.editor.TreeSection;
import base org.eclipse.pde.internal.ui.editor.plugin.ExtensionsSection;
import base org.eclipse.pde.internal.ui.editor.plugin.JavaAttributeWizard;
import base org.eclipse.pde.internal.ui.editor.plugin.rows.ClassAttributeRow;
import base org.eclipse.pde.internal.ui.editor.text.XMLUtil;

/**
 * This team anticipates some fixes regarding the extension editor.
 * 
 * @author stephan
 */
@SuppressWarnings({"restriction","decapsulation"})
public team class ExtensionEditorAdaptor 
{
	// role for XMLInsertionComputer obsoleted by patch in https://bugs.eclipse.org/bugs/show_bug.cgi?id=195763
	
	// hot fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=374789
	protected class FixBug374789 playedBy TreeSection {

		void createViewerPart(String[] buttonLabels) <- replace StructuredViewerPart createViewerPart(String[] buttonLabels);

		callin void createViewerPart(String[] buttonLabels) {
			if (buttonLabels != null && buttonLabels.length > 0 
					&& PDEUIMessages.ManifestEditor_DetailExtension_new == buttonLabels[0] // "==" indeed, we don't want to muddle with other equal strings.
					&& ExtensionsSection.getButtonAdd() == 1) {
				int len = buttonLabels.length;
				System.arraycopy(buttonLabels, 0, buttonLabels = new String[len+1], 1, len);
			}
			base.createViewerPart(buttonLabels);
		}
	}

	/* avoid to use the icon attribute for the label, too. */
	protected class ExtensionsSection playedBy ExtensionsSection 
	{
		protected int getButtonAdd() -> get int BUTTON_ADD;
		
		String resolveObjectName(SchemaRegistry schemaRegistry, Object obj) <- replace String resolveObjectName(SchemaRegistry schemaRegistry, Object obj);
		@SuppressWarnings("basecall")
		callin static String resolveObjectName(SchemaRegistry schemaRegistry, Object obj) {
			boolean fullNames = PDEPlugin.isFullNameModeEnabled();
			if (obj instanceof IPluginExtension) {
				IPluginExtension extension = (IPluginExtension) obj;
				if (!fullNames) {
					return extension.getPoint();
				}
				if (extension.getName() != null)
					return extension.getTranslatedName();
				ISchema schema = schemaRegistry.getSchema(extension.getPoint());
				// try extension point schema definition
				if (schema != null) {
					// exists
					return schema.getName();
				}
				return extension.getPoint();		
			} else if (obj instanceof IPluginElement) {
				IPluginElement element = (IPluginElement) obj;
				String baseName = element.getName();			
				String fullName = null;
				ISchemaElement elementInfo = getSchemaElement(element);
				IPluginAttribute labelAtt = null;
				if (elementInfo != null && elementInfo.getLabelProperty() != null) {
					labelAtt = element.getAttribute(elementInfo.getLabelProperty());
				}
				if (labelAtt == null) {
					// try some hard-coded attributes that
					// are used frequently
					for (int i = 0; i < getCOMMON_LABEL_ATTRIBUTES().length; i++) {
						labelAtt = element.getAttribute(getCOMMON_LABEL_ATTRIBUTES()[i]);
						if (labelAtt != null)
							break;
					}
					if (labelAtt == null) {
						// Last try - if there is only one attribute,
						// use that
						if (element.getAttributeCount() == 1)
							labelAtt = element.getAttributes()[0];
//{ObjectTeams: not if it the icon property:
						if (isIconAttribute(labelAtt))
							labelAtt = null;
// SH}
					}
				}
				if (labelAtt != null && labelAtt.getValue() != null)
					fullName = stripShortcuts(labelAtt.getValue());
				fullName = element.getResourceString(fullName);
				if (fullNames)
					return fullName != null ? fullName : baseName;
				return fullName != null
				? (fullName + " (" + baseName + ")") //$NON-NLS-1$ //$NON-NLS-2$
						: baseName;
			}
			return obj.toString();
		}
		private static boolean isIconAttribute(IPluginAttribute labelAtt) {
			if (labelAtt == null || labelAtt.getName() == null)
				return false;
			if (!labelAtt.getName().equals("icon"))  //$NON-NLS-1$
				return false;
			if (labelAtt instanceof PluginAttribute) {
				PluginAttribute attribute = (PluginAttribute) labelAtt;
				ISchemaAttribute info = attribute.getAttributeInfo();
				if (info != null)
					return info.getKind() == IMetaAttribute.RESOURCE;
			}
			return true; 
		}
		String[] getCOMMON_LABEL_ATTRIBUTES() -> get String[] COMMON_LABEL_ATTRIBUTES;
		ISchemaElement getSchemaElement(IPluginElement element) -> ISchemaElement getSchemaElement(IPluginElement element);
		String stripShortcuts(String input) -> String stripShortcuts(String input);
	}

	
	protected class ClassAttributeRow playedBy ClassAttributeRow {
		doOpenSelectionDialog <- replace doOpenSelectionDialog;
		@SuppressWarnings({ "inferredcallout", "basecall" })
		callin void doOpenSelectionDialog() {
			IResource resource = getPluginBase().getModel().getUnderlyingResource();
			ISchemaAttribute attr = getAttribute();
			String superName = attr != null ? attr.getBasedOn() : null;
			int index = superName != null ? superName.indexOf(':') : -1;
			if (index > 0)
				// if the schema specifies a class and interface, then show only types that extend the class (currently can't search on both).
				superName = superName.substring(0, index);
//{ObjectTeams: consider the case where we only have an interface:			
			else if (index == 0)
				superName = superName.substring(1);
// SH}
			String filter = text.getText();
			if (filter.length() == 0 && superName != null)
				filter = "**"; //$NON-NLS-1$
			String type = PDEJavaHelperUI.selectType(resource, IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES, filter, superName);
			if (type != null)
				text.setText(type);			
		}
	}
	/**
	 * This role adapts the PDE/UI's version of a NewTypeWizard as to use our
	 * NewTeamWizardPage when appropriate.
	 * 
	 * @since 1.2.4
	 */
	protected class JavaAttributeWizard playedBy JavaAttributeWizard 
			base when (base.fAttInfo != null && isBasedOnOOTeam(base.fAttInfo)) 
	{
		static boolean isBasedOnOOTeam(ISchemaAttribute attInfo) {
			String basedOn = attInfo.getBasedOn();
			if (basedOn == null)
				return false;
			return    basedOn.equals(String.valueOf(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM))
				   || basedOn.equals(':'+String.valueOf(IOTConstants.STR_ORG_OBJECTTEAMS_ITEAM));
		}

		String getFClassName() 							-> get String fClassName;
		IProject getFProject() 							-> get IProject fProject;
		void setFMainPage(NewTypeWizardPage fMainPage)  -> set NewTypeWizardPage fMainPage;
		void addPage(IWizardPage arg0) 				    -> void addPage(IWizardPage arg0);
		
		void addNewTeamWizardPage() <- replace void addPages();
		
		@SuppressWarnings("basecall")
		callin void addNewTeamWizardPage() {
			// use a new team wizard:
			NewTeamWizardPage mainPage = new NewTeamWizardPage();
			
			// init similar to base method:
			setFMainPage(mainPage);
			addPage(mainPage);
			mainPage.init(null);
			
			// since our wizard doesn't handle attribute info these inits are different:
			initFields(mainPage, getFClassName(), getFProject());
		}

		void initFields(NewTeamWizardPage mainPage, String className, IProject project) 
		{
			// set package and class names:
			int loc = className.lastIndexOf('.');
			if (loc != -1) {
				mainPage.setPackageFragmentName(className.substring(0, loc));
				mainPage.setTypeName(className.substring(loc+1));
			} else {
				mainPage.setTypeName(className);
			}
			
			// set source folder (in the vein of org.eclipse.pde.internal.ui.editor.plugin.JavaAttributeWizardPage):
			IPackageFragmentRoot srcEntryDft = null;
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragmentRoot[] roots;
			try {
				roots = javaProject.getPackageFragmentRoots();
				for (int i = 0; i < roots.length; i++) {
					if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
						srcEntryDft = roots[i];
						break;
					}
				}
			} catch (JavaModelException e) {
				// can't find source folder
			}
			if (srcEntryDft != null)
				mainPage.setPackageFragmentRoot(srcEntryDft, true);
			else
				mainPage.setPackageFragmentRoot(javaProject.getPackageFragmentRoot(javaProject.getResource()), true);
		}

		// window title
		void setWindowTitle(String newTitle) <- replace void setWindowTitle(String newTitle);

		callin void setWindowTitle(String newTitle) {
			base.setWindowTitle(OTNewWizardMessages.NewTeamCreationWizard_title);
		}

		// header image
		void setDefaultPageImageDescriptor(ImageDescriptor imageDescriptor) 
			<- replace void setDefaultPageImageDescriptor(ImageDescriptor imageDescriptor);
		callin void setDefaultPageImageDescriptor(ImageDescriptor imageDescriptor) 
		{
			base.setDefaultPageImageDescriptor(OTDTUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageManager.NEW_TEAM));
		}		
	}
	
	/** This role makes sure that the PDE UI does not invent names that are illegal in their respective context. */
	protected class XMLUtil playedBy XMLUtil {

		/**
		 *  If the project name makes an illegal package name, 
		 *  and if the lower-cased class name is not legal either,
		 *  try appending 's' until it is legal.
		 *  (this occurred when the project name contained '-' and 
		 *  the class name was "Team" -> "team" is an illegal package name.
		 */
		String createDefaultPackageName(IProject project) <- replace String createDefaultPackageName(IProject project, String className);

		static callin String createDefaultPackageName(IProject project) {
			String result = base.createDefaultPackageName(project);
			IJavaProject javaProject = JavaCore.create(project);
			String optionSrc = javaProject.getOption(JavaCore.COMPILER_SOURCE,true);
			String optionCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			IStatus valid;
			for (int i=0; i<2; i++) { // two attempts to account for "throw" -> "throws" -> "throwss"
				valid = JavaConventions.validatePackageName(result, optionSrc, optionCompliance);
				if (valid.isOK())
					return result;
				result += 's';
			}
			return result;
		}

		/** 
		 * When generating a value for "basePlugin":
		 * if the project name contains illegal characters like '-' convert it to a valid bundle ID. 
		 */
		String createDefaultName() <- replace String createDefaultName(IProject project, ISchemaAttribute attInfo, int counter)
			base when (Constants.BASE_PLUGIN.equals(attInfo.getParent().getName()));

		static callin String createDefaultName() {
			return IdUtil.getValidId(base.createDefaultName());
		}		
		
	}
}
