/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Constants.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox;

/**
 * Constants used for OT/Equinox.
 * 
 * @author stephan
 * @since 1.2.7
 */
public interface Constants {
	
	/** ID of this plugin. */
	public static final String TRANSFORMER_PLUGIN_ID         = "org.eclipse.objectteams.otequinox" ; //$NON-NLS-1$
	
	// === Extension point elements: ===
	
	/** Simple name of the extension point org.eclipse.objectteams.otequinox.aspectBindings. */
	static final String ASPECT_BINDING_EXTPOINT_ID    = "aspectBindings";
	static final String ASPECT_BINDING_FQEXTPOINT_ID  = TRANSFORMER_PLUGIN_ID+'.'+ASPECT_BINDING_EXTPOINT_ID;
	
	/** Simple name of the extension point org.eclipse.objectteams.otequinox.liftingParticipant. */
	static final String LIFTING_PARTICIPANT_EXTPOINT_ID    = "liftingParticipant";

	/** Attribute of "team" and "basePlugin" elements. */
	static final String ID = "id";

	/** Element of EP aspectBindings denoting a team class. */
	static final String TEAM = "team";
	/** Attribute of a "team" element denoting the fully qualified class name. */
	static final String CLASS = "class";
	/** Attribute of a "team" element denoting the team's superclass. */
	static final String SUPERCLASS = "superclass";
	/** Attribute of a "team" element denoting the requested activation: one of "NONE", "THREAD", "ALL_THREADS". */
	static final String ACTIVATION = "activation";
	
	/** Element of EP aspectBindings denoting an adapted base plugin. */
	static final String BASE_PLUGIN = "basePlugin";
	/** Subelement of a "basePlugin" denoting a framgment of the base plugin that is required by the aspect. */
	static final String REQUIRED_FRAGMENT = "requiredFragment";
	/** Pseudo ID of a basePlugin specifying that the team(s) adapt base classes from their own plugin. */
	static final String SELF = "SELF";

	/** Element of EP aspectBinding - child of basePlugin node - requesting exports forced on the given base plug-in. */
	public static final String FORCED_EXPORTS_ELEMENT = "forcedExports";
	
	/** Simple name of the extension point org.eclipse.objectteams.otequinox.aspectBindingNegotiators. */
	public static final String ASPECT_NEGOTIATOR_EXTPOINT_ID = "aspectBindingNegotiators";

}
