/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BundleValidation.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.validation;

import static org.eclipse.objectteams.otequinox.Constants.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.objectteams.otdt.internal.pde.ui.OTPDEUIMessages;
import org.eclipse.objectteams.otequinox.ActivationKind;
import org.eclipse.pde.internal.core.builders.CompilerFlags;
import org.eclipse.pde.internal.core.builders.IHeader;
import org.eclipse.pde.internal.core.builders.PDEMarkerFactory;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.BundleActivationPolicyHeader;
import org.eclipse.pde.internal.core.text.bundle.BundleModel;
import org.eclipse.pde.internal.ui.correction.AbstractManifestMarkerResolution;
import org.eclipse.pde.internal.ui.correction.AbstractPDEMarkerResolution;
import org.eclipse.ui.IMarkerResolution;
import org.osgi.framework.Constants;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import base org.eclipse.pde.internal.core.builders.BundleErrorReporter;
import base org.eclipse.pde.internal.core.builders.ExtensionsErrorReporter;
import base org.eclipse.pde.internal.core.builders.ManifestConsistencyChecker;
import base org.eclipse.pde.internal.ui.correction.ResolutionGenerator;

/**
 * Enhanced validation of bundle manifests.
 * <ul>
 * <li>Check whether all bundles with aspectBindings have a proper activation policy set,<br>
 *    Provide suitable quick assist if activation policy is wrong/missing.</li>
 * </ul>
 * 
 * @author stephan
 * @since 1.2.7
 */
@SuppressWarnings("restriction")
public team class BundleValidation 
{
	/** Constant for a problem that can be resolved by adding an activation policy to the manifest. */
	static final int ADD_ACTIVATION_POLICY = 0x1801; // must not overlap with any constant in org.eclipse.pde.internal.core.builders.PDEMarkerFactory. 
	
	ThreadLocal<BundleCheckingContext> bundleContext = new ThreadLocal<BundleCheckingContext>();
	
	/** 
	 * Defines the context of validating one bundle. 
	 * One instance of this role exists per control flow (= per thread) 
	 * for retrieval in downstream callin bindings. 
	 */
	protected class BundleCheckingContext playedBy ManifestConsistencyChecker 
	{
		// flags set during validation of one bundle:
		protected boolean isAspectBundle = false;
		protected boolean hasTeamActivation = false;

		@SuppressWarnings("decapsulation")
		spanContext <- replace validateFiles;
		
		callin void spanContext() {
			BundleValidation.this.bundleContext.set(this);
			try {
				base.spanContext();
			} finally {
				// withdraw role, is for one-time use only:
				BundleValidation.this.bundleContext.set(null);
				BundleValidation.this.unregisterRole(this, BundleCheckingContext.class);
			}
		}
	}
	
	/**
	 * Detects aspectBindings declared in plugin.xml and records information in the current {@link BundleCheckingContext}.
	 */
	protected class ExtensionAnalyzer playedBy ExtensionsErrorReporter 
			base when (BundleValidation.this.bundleContext.get() != null)
	{	
		void checkAspectBinding(Element element) <- after void validateExtension(Element element);

		protected void checkAspectBinding(Element element) 
		{
			Object pointID = element.getAttribute("point"); //$NON-NLS-1$
			if (ASPECT_BINDING_FQEXTPOINT_ID.equals(pointID)) 
			{
				BundleCheckingContext context = BundleValidation.this.bundleContext.get();
				// it's an aspect bundle
				context.isAspectBundle = true;
				
				// does it have elements with relevant activation?
				NodeList baseNodes = element.getElementsByTagName(BASE_PLUGIN);
				if (baseNodes.getLength() > 0) { 
					String baseId = ((Element)baseNodes.item(0)).getAttribute(ID);
					if (baseId != null && baseId.toUpperCase().equals(SELF))
						return; // missing bundle activation is not fatal in this case
				}
				
				// check the teams for activation ALL_THREADS or THREAD:
				NodeList teamNodes = element.getElementsByTagName(TEAM);
				for (int t=0; t<teamNodes.getLength(); t++) {
					Object activation = ((Element)teamNodes.item(t)).getAttribute(ACTIVATION);
					if (ActivationKind.ALL_THREADS.toString().equals(activation)) {
						context.hasTeamActivation = true;
						break;
					} else if (ActivationKind.THREAD.toString().equals(activation)) {
						context.hasTeamActivation = true;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Validates whether activation policy is set if needed.
     * This role is only active for bundles with one or more aspect bindings.
	 */
	protected class BundleErrorReporter playedBy BundleErrorReporter 
			base when (BundleValidation.this.bundleContext.get().isAspectBundle)
	{			
		@SuppressWarnings("decapsulation")
		IHeader getHeader(String key) -> IHeader getHeader(String key);
		void report(String message, int line, int severity, int resolution, String category) 
			-> IMarker report(String message, int line, int severity, int resolution, String category);
		
		void validateBundleActivatorPolicy() <- after void validateBundleActivatorPolicy();
		
		void validateBundleActivatorPolicy() 
		{
			IHeader header = getHeader(Constants.BUNDLE_ACTIVATIONPOLICY);
			int lineNo = 1;
			if (header != null) {
				if (Constants.ACTIVATION_LAZY.equals(header.getValue()))
					return; // OK!
				lineNo = header.getLineNumber()+1;
			}
			boolean hasTeamActivation = BundleValidation.this.bundleContext.get().hasTeamActivation;
			report(OTPDEUIMessages.Validation_MissingActivationPolicy_error, 
				   lineNo, 
				   hasTeamActivation ? CompilerFlags.ERROR : CompilerFlags.WARNING, 	// only severe if relevant team activation is requested. 
				   ADD_ACTIVATION_POLICY, 
				   PDEMarkerFactory.CAT_FATAL);
		}		
	}
	
	/** Unbound role: simple rewriting of the manifest to add or correct an activation policy header. */
	protected class SetActivationPolicyResolution extends AbstractManifestMarkerResolution 
	{	
		public SetActivationPolicyResolution(int type) {
			super(type);
		}
	
		protected void createChange(BundleModel model) {
	
			IManifestHeader header = model.getBundle().getManifestHeader(Constants.BUNDLE_ACTIVATIONPOLICY);
	
			if (header != null && header instanceof BundleActivationPolicyHeader)
				((BundleActivationPolicyHeader) header).setLazyStart(true);
			else
				model.getBundle().setHeader(Constants.BUNDLE_ACTIVATIONPOLICY, Constants.ACTIVATION_LAZY);
		}
	
		public String getLabel() {
			return OTPDEUIMessages.Resolution_AddBundleActivationPolicy_label;
		}
	}
	
	/**
	 * Advise the base class for handling missing/incorrect activation policy 
	 * (code {@link BundleValidation#ADD_ACTIVATION_POLICY}).
	 */
	protected class ResolutionGenerator playedBy ResolutionGenerator {

		IMarkerResolution[] getResolutions(IMarker marker) <- replace IMarkerResolution[] getResolutions(IMarker marker);
		
		callin IMarkerResolution[] getResolutions(IMarker marker) {
			IMarkerResolution[] result = base.getResolutions(marker);
			if (result.length == 0) {
				int problemID = marker.getAttribute("id", PDEMarkerFactory.NO_RESOLUTION); //$NON-NLS-1$
				switch (problemID) {
					case BundleValidation.ADD_ACTIVATION_POLICY :
						return new IMarkerResolution[] {new SetActivationPolicyResolution(AbstractPDEMarkerResolution.CREATE_TYPE)};
				}
			}
			return result;
		}
	}
}
