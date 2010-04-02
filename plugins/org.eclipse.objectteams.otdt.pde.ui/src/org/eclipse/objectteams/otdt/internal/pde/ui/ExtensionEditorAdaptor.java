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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.search.HierarchyScope;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
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
import org.eclipse.pde.internal.core.util.PDEJavaHelper;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages;
import org.eclipse.objectteams.otdt.ui.ImageManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.objectteams.otequinox.Constants;

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
	
	/* avoid to use the icon attribute for the label, too. */
	protected class ExtensionsSection playedBy ExtensionsSection 
	{
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
					for (int i = 0; i < getCOMMON_LABEL_PROPERTIES().length; i++) {
						labelAtt = element.getAttribute(getCOMMON_LABEL_PROPERTIES()[i]);
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
		String[] getCOMMON_LABEL_PROPERTIES() -> get String[] COMMON_LABEL_PROPERTIES;
		ISchemaElement getSchemaElement(IPluginElement element) -> ISchemaElement getSchemaElement(IPluginElement element);
		String stripShortcuts(String input) -> String stripShortcuts(String input);
	}
	/**
	 * This role anticipates a fix to https://bugs.eclipse.org/bugs/show_bug.cgi?id=61185
	 * Restricting type selection dialogs for extension details to the 'basedOn' type, if given.
	 * Although bug 61185 is partly fixed, this role also covers bug 215139.
	 */
	protected class ClassAttributeRow playedBy ClassAttributeRow 
	{
		IPluginBase getPluginBase() -> IPluginBase getPluginBase();
		Text getText() -> get Text text;
		
		doOpenSelectionDialog <- replace doOpenSelectionDialog;
		@SuppressWarnings("basecall")
		callin void doOpenSelectionDialog() {
			String superType= null;
			try {
				ISchemaAttribute att= getAttribute();
				superType= att.getBasedOn();
			} catch (Throwable t) {
				// e.g., a CCE in getAttribute()?
			}
			if (superType != null && superType.startsWith(":")) //$NON-NLS-1$
				superType= superType.substring(1);
			if (superType == null || "java.lang.Object".equals(superType)) { //$NON-NLS-1$
				// if no useful super type was found do the normal thing:
				base.doOpenSelectionDialog();
				return;
			}
			IResource resource = getPluginBase().getModel().getUnderlyingResource();
			String type = selectType(
					resource, 
					IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES, 
					getText().getText(),
					superType);
			if (type != null)
				getText().setText(type);
		}
		ISchemaAttribute getAttribute() -> get Object att
			with { result <- (ISchemaAttribute)att }
		
		// from PDEJavaHelperUI, added param supertype
		static String selectType(IResource resource, int scope, String filter, String superTypeName) 
		{
			if (resource == null) return null;
			IProject project = resource.getProject();
			try {
				// create new scope (hierarchy):
				IJavaSearchScope searchScope = null;
				if (superTypeName != null && !superTypeName.equals("java.lang.Object")) { //$NON-NLS-1$
					IJavaProject javaProject = JavaCore.create(project);
					IType superType = javaProject.findType(superTypeName);
					if (superType != null)
						searchScope= SearchEngine.createHierarchyScope(javaProject, superType, true, true, DefaultWorkingCopyOwner.PRIMARY);
					/* Eclipse version:
						searchScope = SearchEngine.createHierarchyScope(superType);
				     */
				}
				if (searchScope == null)
					searchScope = PDEJavaHelper.getSearchScope(project);
				
				SelectionDialog dialog = JavaUI.createTypeDialog(
						PDEPlugin.getActiveWorkbenchShell(),
						PlatformUI.getWorkbench().getProgressService(),
						searchScope,
						//orig: PDEJavaHelper.getSearchScope(project),
						scope, 
						false, "**"/*filter*/);  //$NON-NLS-1$
				dialog.setTitle(PDEUIMessages.ClassAttributeRow_dialogTitle); 
				if (dialog.open() == Window.OK) {
					IType type = (IType) dialog.getResult()[0];
					return type.getFullyQualifiedName('$');
				}
			} catch (JavaModelException e) {
			}
			return null;
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
			return basedOn.equals(String.valueOf(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM));
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
			while (true) {
				valid = JavaConventions.validatePackageName(result, optionSrc, optionCompliance);
				if (valid.isOK())
					return result;
				result += 's';
			}
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
