/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: GUITest.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.objectteams.samples.ordersystem.gui;


public class GUITest {

    public static void main(String[] args) {
        GUITest tempGUITest = new GUITest();
        tempGUITest.init();
    }
    
    private void init() {
        // hook for security application
        // TODO: remove this when OT can adapt constructors
        new OrderSystemMainFrame();
    }
    
}
