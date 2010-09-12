/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: GUIAdapterTeam.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.gui;

import java.awt.Container;
import java.awt.Dimension;

import org.objectteams.Team;


/**
 * This aspect ensures that each window will appear in the middle
 * of the screen.
 * 
 * @author Dehla Sokenou
 */
public team class GUIAdapterTeam  {
    
    public GUIAdapterTeam() { 
        activate(Team.ALL_THREADS);
    }

    // note: we need to individually adapt two GUI-classes 
    // as long as class java.awt.Window cannot be adapted 
    // (restriction to be removed in the future).
    
    public class MainFrameAdapter playedBy OrderSystemMainFrame {

        // callouts
        public abstract Container getParent();
        getParent -> getParent;
        
        public abstract void setCentered(Container parent);
        setCentered -> setLocationRelativeTo;
        
        // callins
        protected void centerRelativeToParent() {
            setCentered(getParent());
        }
        centercallin:
        void centerRelativeToParent() <- after void setSize(Dimension aDimension), 
                                               void setSize(int aWidth, int aHeight);
        
    }
    
    public class DialogAdapter playedBy InputDialog {
        
        // callouts
        public abstract Container getParent();
        getParent -> getParent;
        
        public abstract void setCentered(Container parent);
        setCentered -> setLocationRelativeTo;
        
        // callins
        protected void centerRelativeToParent() {
            setCentered(getParent());
        }
        centercallin:
        void centerRelativeToParent() <- after void setSize(Dimension aDimension), 
                                               void setSize(int aWidth, int aHeight);
    }
}
