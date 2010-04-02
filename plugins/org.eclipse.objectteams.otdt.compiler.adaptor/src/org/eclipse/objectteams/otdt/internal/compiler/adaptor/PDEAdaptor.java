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
 * $Id: PDEAdaptor.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.core.ClasspathAccessRule;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.StateHelper;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;

import base org.eclipse.pde.internal.core.PDEClasspathContainer;
import base org.eclipse.pde.internal.core.RequiredPluginsClasspathContainer;
import base org.eclipse.pde.internal.core.PDEClasspathContainer.Rule;
import base org.eclipse.pde.internal.core.bundle.BundlePluginModel;
import base org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;

/**
 * Adapt classes from the PDE core as to feed information about aspectBindings
 * into the compilation process (to be consumed by BaseImportChecker).
 * 
 * Final target as expected by the BaseImportChecker:
 *  + aspectBindingData (of type AdaptedBaseBundle) have been added to ClasspathAccessRules 
 *    and the problemID has been adjusted.
 * 
 * 
 * @author stephan
 * @since 1.1.5
 */
@SuppressWarnings("restriction")
public team class PDEAdaptor 
{
	static PDEAdaptor instance; 
	
	public PDEAdaptor() {
		instance= this;
	}

	/**
	 * <ul> 
	 * <li>Store aspectBinding info in Role objects.</li>
	 * <li>Add additional rules for forcedExports.</li>
	 * </ul> 
	 */
	protected class RequiredPluginsClasspathContainer
			extends PDEClasspathContainer
			playedBy RequiredPluginsClasspathContainer 
	{
		
		protected AspectBindingReader aspectBindingReader;
		
		void updateRule(String providingBundle, Rule rule) 
		 <- after Rule getRule(StateHelper helper, BundleDescription desc, ExportPackageDescription export)
			with { providingBundle <- export.getExporter().getSymbolicName(),
				   rule            <- result
			}
		/** Handles adaptation info for exported packages, Rule role created via regular lifting. */
		void updateRule(String providingBundle, Rule rule) {
			if (aspectBindingReader != null && aspectBindingReader.isAdaptedBase(providingBundle)) {
				// no merging because rule (base & role) are fresh instances
				rule.aspectBindingData= aspectBindingReader.getAdaptationInfo(providingBundle);
			}
		}
		
		@SuppressWarnings({ "decapsulation", "rawtypes" })
		Rule[] addForcedExports(BundleDescription desc) 
		 <- replace Rule[] getInclusions(Map map, BundleDescription desc)
		    with { desc <- desc }
		/** Handles adaptation info for non-exported packages, Rule role explicitly created. */		
		callin Rule[] addForcedExports(BundleDescription desc) 
		{
			Rule[] regularRules= base.addForcedExports(desc);
			if (aspectBindingReader == null)
				return regularRules; // done: no aspect bindings
			HashSet<String> forcedExports= aspectBindingReader.getForcedExports(desc.getSymbolicName());
			if (forcedExports == null)
				return regularRules; // done: no forced exports
			
			AdaptedBaseBundle aspectBindingData= aspectBindingReader.getAdaptationInfo(desc.getSymbolicName());			
			// create additional rules:
			Rule[] additionalRules= new Rule[forcedExports.size()];
			Iterator<String> exportIter= forcedExports.iterator();
			for (int i = 0; i < additionalRules.length; i++) 
				additionalRules[i]= new Rule(this, aspectBindingData, exportIter.next());
			
			// merge arrays:
			int len1= regularRules.length, len2= additionalRules.length;
			Rule[] result= new Rule[len1+len2];
			System.arraycopy(additionalRules, 0, result, 0, len2);
			System.arraycopy(regularRules, 0, result, len2, len1);
			
			return result;
		}
		
		@SuppressWarnings("decapsulation")
		protected
		BundleModel getBundleModel() -> get IPluginModelBase fModel
			with { result <- (BundlePluginModel)fModel }

		// -- debug: --		
		String baseToString() => String toString();
		
		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "Role for "+baseToString()+" with aspectBindingReader\n  "
				  + ((this.aspectBindingReader != null) ? this.aspectBindingReader.toString() : "null");
		}
	}
	
	/**
	 * Synthetic rules representing adapted or forcedExports.
	 */
	@SuppressWarnings("decapsulation")
	protected class Rule playedBy Rule 
	{
		void setPath(IPath path) -> set IPath path;

		// intermediate storage between AspectBindingReader and ClasspathAccessRule:
		protected AdaptedBaseBundle aspectBindingData; 
		protected boolean isForcedExport;
		
		/** Ctor for force-exported packages (merely adapted packages instantiate via lifting ctor). */
		protected Rule(RequiredPluginsClasspathContainer encl, AdaptedBaseBundle aspectBindingData, String packageName) 
		{
			encl.base();
			String pattern= packageName.replace('.', '/')+"/*"; //$NON-NLS-1$
			setPath(new Path(pattern));
			this.aspectBindingData= aspectBindingData;
			this.isForcedExport= true;
		}
		// -- debug: --
		String baseToString() => String toString();
		
		@SuppressWarnings("nls")
		@Override
		public String toString() {
			String result= baseToString();
			if (this.isForcedExport)
				result+= " (forced export)";
			return result+" with aspect data\n  "
				  + ((this.aspectBindingData == null) ? "null" : this.aspectBindingData.toString());
		}		
	}
	
	/** After converting Rules to IAccessRules transfer adaptation info and adjust problemId. */
	protected class PDEClasspathContainer playedBy PDEClasspathContainer 
	{
		void getAccessRules(Rule[] rules, IAccessRule[] accessRules) 
		  <- after IAccessRule[] getAccessRules(Rule[] rules)
			 with { rules <- rules, accessRules <- result }
		static void getAccessRules(Rule[] rules, IAccessRule[] accessRules) {
			for (int i = 0; i < rules.length; i++) {
				Rule rule = rules[i];
				if (rule.aspectBindingData != null) {
					ClasspathAccessRule classpathAccessRule = (ClasspathAccessRule)accessRules[i];
					if (rule.isForcedExport) {
						// don't let this rule leak to other clients
						classpathAccessRule = new ClasspathAccessRule(classpathAccessRule.pattern, IProblem.BaseclassDecapsulationForcedExport);
						classpathAccessRule.aspectBindingData = new Object[] { rule.aspectBindingData };
						accessRules[i] = classpathAccessRule;
					} else { 
						addAspectBindingData(classpathAccessRule, rule.aspectBindingData);
					}
				}
			}
		}
	}
	/**
     * Add the given aspect binding data to the given access rule.
     * @return: has data been added (vs. merged or already present)? 
	 */
	public static boolean addAspectBindingData(ClasspathAccessRule accessRule, AdaptedBaseBundle aspectBindingData) {
		// nothing present yet?
		if (accessRule.aspectBindingData == null) {
			accessRule.aspectBindingData = new Object[] { aspectBindingData };
			if (accessRule.problemId == 0)
				accessRule.problemId= IProblem.AdaptedPluginAccess;
			return true;
		}
		// exact binding data already present?
		for (Object data : accessRule.aspectBindingData)
			if (data == aspectBindingData)
				return false;
		// different binding data for the same base bundle present?
		for (Object data : accessRule.aspectBindingData)
			if (((AdaptedBaseBundle)data).merge(aspectBindingData))
				return false;
		// different base bundles, must be the case of split packages
		for (Object data : accessRule.aspectBindingData)
			((AdaptedBaseBundle)data).hasPackageSplit = true;
		aspectBindingData.hasPackageSplit = true;
		int len = accessRule.aspectBindingData.length;
		System.arraycopy(accessRule.aspectBindingData, 0, accessRule.aspectBindingData = new Object[len+1], 0, len);
		accessRule.aspectBindingData[len] = aspectBindingData;
		return true;
	}

	/** Helper role for updating aspect binding information. */
	protected class BundleModel playedBy BundlePluginModel {
		protected AspectBindingReader aspectBindingReader;
		
	}
	
	/** 
	 * This role listens to updates on its base.
	 * If the associated bundle model has a role with a registered
	 * aspect binding reader, trigger reloading when the model has changed. 
	 */
	protected class ModelListener playedBy WorkspaceExtensionsModel {
		void resetAspectReader() <- after void load(InputStream is, boolean reload);
		void resetAspectReader () throws CoreException {
			try {
				BundleModel bundle= getFBundleModel();
				if (bundle != null && bundle.aspectBindingReader != null)
					bundle.aspectBindingReader.reload();
			} catch (ClassCastException cce) {
				// CCE could be thrown by parameter mapping of getFBundleModel().
			}
		}
		/** This declaration is for documentation only: read the fBundleModel field.
		 * @return a BundleModel role
		 * @throws ClassCastException thrown when fBundleModel is not a BundlePluginModel.
		 */
		abstract BundleModel getFBundleModel() throws ClassCastException;
		@SuppressWarnings("decapsulation")
		BundleModel getFBundleModel() -> get IBundlePluginModelBase fBundleModel
			with { result <- (BundlePluginModel)fBundleModel }
	}

	/** Register an aspect binding reader for a given RequiredPluginsClasspathContainer. */
	void setAspectBindingReader(AspectBindingReader aspectBindingReader,
		RequiredPluginsClasspathContainer as RequiredPluginsClasspathContainer container) 
	{
		container.aspectBindingReader= aspectBindingReader;
		try {
			// link bundle model and reader for updating lateron:
			BundleModel bundle= container.getBundleModel();
			if (bundle != null)
				bundle.aspectBindingReader= aspectBindingReader;
		} catch (ClassCastException cce) {
			// can happen in param mapping of c-t-f, wrong model type, ignore.
		}
	}
}
