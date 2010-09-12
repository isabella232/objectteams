/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ButtonListLayout.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.util.ArrayList;

/**
 * 
 * @author Dehla Sokenou
 *
 * Class for laying out are typical constellation: a button panel and a list (or a similar
 * e.g. table) panel.
 * <p>
 * 
 * Buttons can be placed on top or below (vertical orientation) or on the left or right side
 * (horizontal orientation) of the list.
 * Standard orientation is horizontal.
 * Standard placement of buttons is -depending on orientation- below or on the right side
 * of the list panel (buttons after list).
 * 
 */
public class ButtonListLayout implements LayoutManager2 {
        
    /**
     * Enum for the orientation. 
     * Possibly values are
     * <ul>
     * <li> HORIZONTAL: button panel ist displayed at the left or the right side of list panel.
     * <li> VERTICAL: button panel is dispayed on top or below list panel.
     * </ul>
     * 
     * Orientation and placement cannot be changed during lifecycle of this class' objects.
     */
    public enum Orientation {
        VERTICAL, HORIZONTAL;
    }
    
    public enum Placement {
        BUTTONS_AFTER_LIST, BUTTONS_BEFORE_LIST;
    }
    
    public enum ComponentType {
        BUTTON_PANEL, LIST_OR_TABLE_PANEL;
    }
    
    /**
     * Orientation of the actual layout manager. Default is horizontal.
     */
    protected final Orientation orientation;
    
    /**
     * Placement of the actual layout manager. Default is the placement of buttons after list.
     */
    protected final Placement placement;
    
    /**
     * Reference to button panel that is managed by this layout manager.
     */
    protected ArrayList<Component> buttons = new ArrayList<Component>();
    
    /**
     * Reference to list panel that is managed by this layout manager.
     */
    protected Component listOrTablePanel = null;
    
    /**
     * The spacing between buttons. Default is 5.
     */
    protected int buttonSpacing = 5;
    
    /**
     * The spacing between panels and panel to frame. Default is .
     */
    protected int panelSpacing = 0;
    
    /**
     * Size of button panel depending from button panel's orientation. 
     * If orientation is horizontal, size means width. 
     * If orientation is vertical, size means height.
     * Default is preferred size of included buttons (null).
     */
    protected Dimension fixedButtonSize = null;

    
    public ButtonListLayout() {
        this(null, null);
    }
    
    public ButtonListLayout(Orientation anOrientation) {
        this(anOrientation, null);       
    }
    
    public ButtonListLayout(Placement aPlacement) {
        this(null, aPlacement);
    }

    public ButtonListLayout(Orientation anOrientation, Placement aPlacement) {
        orientation = anOrientation != null ? anOrientation : Orientation.HORIZONTAL;
        placement = aPlacement != null ? aPlacement : Placement.BUTTONS_AFTER_LIST;
    }
    
    /**
     * @param aPanelSpacing the panelSpacing to set
     */
    public void setPanelSpacing(int aPanelSpacing) {
        panelSpacing = aPanelSpacing;
    }

    /**
     * @param aButtonSpacing the buttonSpacing to set
     */
    public void setButtonSpacing(int aButtonSpacing) {
        if (aButtonSpacing >= 0) {
            buttonSpacing = aButtonSpacing;
        }
    }
    
    /**
     * @param aFixedButtonPanelSize the fixedButtonPanelSize to set
     */
    public void setFixedButtonSize(Dimension aFixedButtonPanelSize) {
        fixedButtonSize = aFixedButtonPanelSize;
    }
    
    /**
     * @see LayoutManager2#addLayoutComponent(Component, Object)
     */
    public void addLayoutComponent(Component aComponent, Object aConstraints) {
        if (aConstraints instanceof ComponentType) {
            switch ((ComponentType) aConstraints) {
                case BUTTON_PANEL: 
                    buttons.add(aComponent); break;
                case LIST_OR_TABLE_PANEL:
                    listOrTablePanel = aComponent; break;
                default:
                    throw new IllegalArgumentException("Unsupported layout constraint: " + aConstraints.toString());
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported layout constraint: " + aConstraints.toString());
        }
    }

    /**
     * @see LayoutManager2#maximumLayoutSize(Container)
     */
    public Dimension maximumLayoutSize(Container aTarget) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * @see LayoutManager2#getLayoutAlignmentX(Container)
     */
    public float getLayoutAlignmentX(Container aTarget) {
        // do nothing
        return 0;
    }

    /**
     * @see LayoutManager2#getLayoutAlignmentY(Container)
     */
    public float getLayoutAlignmentY(Container aTarget) {
        // do nothing
        return 0;
    }

    /**
     * @see LayoutManager2#invalidateLayout(Container)
     */
    public void invalidateLayout(Container aTarget) {
        // do nothing
    }

    /**
     * @see LayoutManager#addLayoutComponent(String, Component)
     */
    public void addLayoutComponent(String aName, Component aComponent) {
        throw new UnsupportedOperationException("Sdding layout components based on String constraints is not supported." +
                                                "Use add(Component, Object) instead.");
    }

    /**
     * @see LayoutManager#removeLayoutComponent(Component)
     */
    public void removeLayoutComponent(Component aComponent) {
        if (listOrTablePanel == aComponent) {
            listOrTablePanel = null;
        }
        else if (buttons.contains(aComponent)) {
            buttons.remove(aComponent);
        }
        // else do nothing
    }

    /**
     * @see LayoutManager#preferredLayoutSize(Container)
     */
    public Dimension preferredLayoutSize(Container aParent) {
        return getLayoutSize(aParent, false);
    }

    /**
     * @see LayoutManager#minimumLayoutSize(Container)
     */
    public Dimension minimumLayoutSize(Container aParent) {
        return getLayoutSize(aParent, true);
    }

    /**
     * @see LayoutManager#layoutContainer(Container)
     */
    public void layoutContainer(Container aParent) {
        // beachten: insets, orientation, fixedbuttonsize, placement
        // Note: we don't care about to small panel... if it doesn't fit, only a part is visible    
        
        // firstly, calculate button panel size
        Insets tempInsets = aParent.getInsets();
        int tempMaxWidth = aParent.getWidth() - (tempInsets.left + tempInsets.right)
                                              - 2 * panelSpacing;
        int tempMaxHeight = aParent.getHeight() - (tempInsets.top + tempInsets.bottom)
                                              - 2 * panelSpacing;
        
        Dimension tempPreferredButtonPanelSize = getButtonPanelLayoutSize(false);
        double tempPreferredButtonPanelWidth = tempPreferredButtonPanelSize.getWidth();
        double tempPreferredButtonPanelHeight = tempPreferredButtonPanelSize.getHeight();

        // secondly, give remaining space to list
        int tempListPanelX = tempInsets.left + panelSpacing;
        if (orientation == Orientation.VERTICAL && placement == Placement.BUTTONS_BEFORE_LIST) {
            tempListPanelX += panelSpacing + tempPreferredButtonPanelWidth;
        }
        int tempListPanelY = tempInsets.top + panelSpacing;
        if (orientation == Orientation.HORIZONTAL && placement == Placement.BUTTONS_BEFORE_LIST) {
            tempListPanelY += panelSpacing + tempPreferredButtonPanelHeight;
        }
        int tempListPanelWidth = tempMaxWidth;
        if (orientation == Orientation.VERTICAL) {
            tempListPanelWidth -= (int) tempPreferredButtonPanelWidth + panelSpacing;
        }
        int tempListPanelHeight = tempMaxHeight;
        if (orientation == Orientation.HORIZONTAL) {
            tempListPanelHeight -= (int) tempPreferredButtonPanelHeight + panelSpacing;
        }
        // check, if panel is unvisible
        if (tempListPanelWidth < 0) {
            tempListPanelWidth = 0;
        }
        if (tempListPanelHeight < 0) {
            tempListPanelHeight = 0;
        }
        listOrTablePanel.setBounds(tempListPanelX, tempListPanelY, tempListPanelWidth, tempListPanelHeight);

        // thirdly, layout buttons in button panel based on preferred size
        int tempButtonX = tempInsets.left + panelSpacing;
        if (orientation == Orientation.VERTICAL && placement == Placement.BUTTONS_AFTER_LIST) {
            tempButtonX += panelSpacing + tempListPanelWidth;
        }
        int tempButtonY = tempInsets.top + panelSpacing; 
        if (orientation == Orientation.HORIZONTAL && placement == Placement.BUTTONS_AFTER_LIST) {
            tempButtonY += panelSpacing + tempListPanelHeight;
        }
        for (Component tempButton : buttons) {
            Dimension tempButtonSize = fixedButtonSize != null ? fixedButtonSize : tempButton.getPreferredSize();
            int tempButtonWidth = tempButtonSize.width; 
            int tempButtonHeight = tempButtonSize.height;   
            tempButton.setBounds(tempButtonX, tempButtonY, tempButtonWidth, tempButtonHeight);
            // calculate next button position
            switch (orientation) {
                case VERTICAL: 
                    tempButtonY += tempButtonHeight + buttonSpacing;
                    break;
                case HORIZONTAL: 
                    tempButtonX += tempButtonWidth + buttonSpacing;
                    break;
            }
        }
        
    }
    
    /**
     * 
     * @param aParent the container to manage
     * @param minimum indicates whether minimum (true) or preferred (false) layout 
     *        will be returned
     * @return the preferred or (if minimum) minimum layout size
     */
    protected Dimension getLayoutSize(Container aParent, boolean minimum) {
        Dimension tempButtonPanelDimension = getButtonPanelLayoutSize(minimum);
        
        Insets tempInsets = aParent.getInsets();
        double tempLayoutWidth = 2 * panelSpacing + tempInsets.left + tempInsets.right;
        double tempLayoutHeight = 2 * panelSpacing + tempInsets.top + tempInsets.bottom;
        Dimension tempListDimension = minimum ? listOrTablePanel.getMinimumSize()
                                              : listOrTablePanel.getPreferredSize();
        switch (orientation) {
            case HORIZONTAL:
                tempLayoutWidth += tempListDimension.getWidth() + panelSpacing 
                                                                + tempButtonPanelDimension.getWidth();
                tempLayoutHeight += Math.max(tempListDimension.getHeight(), 
                                             tempButtonPanelDimension.getHeight());
                break;
            case VERTICAL:
                tempLayoutHeight += tempListDimension.getHeight() + panelSpacing 
                                                                  + tempButtonPanelDimension.getHeight();
                tempLayoutWidth += Math.max(tempListDimension.getWidth(), 
                                            tempButtonPanelDimension.getWidth());
                break;
        }            
                
        Dimension tempLayoutDimension = new Dimension();
        tempLayoutDimension.setSize(tempLayoutWidth, tempLayoutHeight);
        return tempLayoutDimension;
    }

    /**
     * Returns the layout size of button panel.
     * 
     * @param minimum indicates whether minimum (true) or preferred (false) layout 
     *        will be returned
     * @return the preferred or (if minimum) minimum layout size
     */
    protected Dimension getButtonPanelLayoutSize(boolean minimum) {
        // calculate sum of space between buttons dependent from orientation
        double tempButtonsLayoutWidth = ((orientation == Orientation.HORIZONTAL) ?
                                          (buttons.size() - 1) * buttonSpacing :  
                                           0);
        double tempButtonsLayoutHeight = ((orientation == Orientation.VERTICAL) ?
                                          (buttons.size() - 1) * buttonSpacing : 
                                           0);
        if (fixedButtonSize != null) {
            // if fixed button size, use fixed size instead of preferred or minimum size
            switch (orientation) {
                case VERTICAL: 
                    tempButtonsLayoutWidth += fixedButtonSize.getWidth();
                    tempButtonsLayoutHeight += buttons.size() * fixedButtonSize.getHeight();
                    break;
                case HORIZONTAL:     
                    tempButtonsLayoutWidth += buttons.size() * fixedButtonSize.getWidth();
                    tempButtonsLayoutHeight += fixedButtonSize.getHeight();
                    break;
            }
        }
        else {
            // if not fixed size, use each buttons preferred resp. minimum size
            for (Component tempComponent : buttons) {
                Dimension tempButtonDimension = minimum ? tempComponent.getMinimumSize() 
                                                        : tempComponent.getPreferredSize();
                switch (orientation) {
                    case VERTICAL:
                        if (tempButtonsLayoutWidth < tempButtonDimension.getWidth()) {
                            tempButtonsLayoutWidth = tempButtonDimension.getWidth();
                        }
                        tempButtonsLayoutHeight += tempButtonDimension.getHeight();
                        break;
                    case HORIZONTAL:
                        tempButtonsLayoutWidth += tempButtonDimension.getWidth();
                        if (tempButtonsLayoutHeight < tempButtonDimension.getHeight()) {
                            tempButtonsLayoutHeight = tempButtonDimension.getHeight();
                        }
                        break;
                }
            }
        }
        
        Dimension tempButtonPanelDimension = new Dimension();
        tempButtonPanelDimension.setSize(tempButtonsLayoutWidth, tempButtonsLayoutHeight);
        return tempButtonPanelDimension;
    }

}
