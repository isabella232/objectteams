/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ImageConstants.java 23434 2010-02-03 23:52:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui;

/**
 * This interface holds plugin image keys for the OTDTUIPlugin. You need them
 * when requesting images from the plugin ImageRegistry.
 * 
 * @author kaiser
 * @version $Id: ImageConstants.java 23434 2010-02-03 23:52:31Z stephan $
 */
@SuppressWarnings("nls")
public interface ImageConstants
{
	public static final String IMG_PATH                 = "icons/ot/";

	public static final String DEFAULT_PATH				= "default/";
	public static final String HOVER_PATH				= "hover/";
	public static final String DISABLED_PATH			= "disabled/";

	// Method binding icons:
    public static final String CALLINBINDING_AFTER_IMG  = "callinbindingafter_obj.gif";
    public static final String CALLINBINDING_BEFORE_IMG = "callinbindingbefore_obj.gif";
    public static final String CALLINBINDING_REPLACE_IMG= "callinbindingreplace_obj.gif";
    public static final String CALLOUTBINDING_IMG       = "calloutbinding_obj.gif";

    public static final String CALLINMETHOD_IMG         = "callinmethod_co.gif";
    public static final String BOUNDROLE_IMG	        = "boundrole_co.gif";
    
    // Guard predicates:
    public static final String GUARD_IMG 				= "guard_obj.gif";
    public static final String BASEGUARD_IMG 			= "baseguard_obj.gif";
    
    // Class icons:
    public static final String ROLECLASS_IMG 			= "role_obj.png";
    public static final String ROLECLASS_PROTECTED_IMG	= "role_protected_obj.png";

    public static final String TEAM_IMG 				= "team_obj.gif";
    public static final String TEAM_ROLE_IMG			= "team_role_obj.gif";
    public static final String TEAM_ROLE_PROTECTED_IMG	= "team_role_protected_obj.gif";

    // Overlay icons:
    public static final String ROLE_OVR					= "role_ovr.png";
    public static final String TEAM_OVR 				= "team_ovr.gif";
    public static final String TEAM_ROLE_OVR			= "team_role_ovr.gif";

    // action (default/disabled/hover):
    public static final String HIDE_TEAMPACKAGE			= "hide_team_package.gif";
	
	// decorations on wizard pages:
	public static final String NEW_TEAM					= "wizard/newteam_wiz.png";
	public static final String NEW_ROLE					= "wizard/newrole_wiz.png";
	public static final String NEW_OT_PROJECT			= "wizard/newotjprj_wiz.png";
	
}