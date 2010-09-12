/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OrderSystemMainFrame.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;



/**
 * 
 * @author Dehla Sokenou
 *
 * Class implementing view of order system.
 *
 */
@SuppressWarnings("serial")
public class OrderSystemMainFrame extends JFrame {
    
    // store buttons
    private JButton addItemButton = new JButton("Add Item");
    protected JButton removeItemButton = new JButton("Remove Item");
    protected JButton increaseStockButton = new JButton("Increase Stock");
    protected JButton decreaseStockButton = new JButton("Decrease Stock");
    
    // customer management buttons
    protected JButton addCustomerButton = new JButton("Add Customer");
    protected JButton removeCustomerButton = new JButton("Remove Customer");
    
    // reservation management buttons
    // none
    
    // order management buttons
    protected JButton newOrderButton = new JButton("New Order");
    protected JButton removeOrderButton = new JButton("Remove Order");
    protected JButton changeOrderButton = new JButton("Change Order");
    
    // close application button
    protected JButton closeButton = new JButton("Exit");
    
    // table
    protected JTable storeTable = new JTable();
    protected JTable customerTable = new JTable();
    protected JTable reservationTable = new JTable();
    protected JTable orderTable = new JTable();
    
    // preferred size of buttons: get greatest button size
    protected Dimension preferredButtonSize = round(removeCustomerButton.getPreferredSize());
    
    
    public OrderSystemMainFrame() {
        super();
        setTitle("TOPPrax Ordersystem");
        setSize(800, 600);
        setLayout(new BorderLayout());
                
        initializeContents();
                
        setVisible(true);
    }

    /**
     * Initializes components in main frame. Necessary to provide hook for adaption.
     */
    private void initializeContents() {
        JTabbedPane tempTabbedPane = new JTabbedPane();
        getContentPane().add("Center", tempTabbedPane);
        tempTabbedPane.addTab("Store", getStoreTab());
        tempTabbedPane.addTab("Customers", getCustomerTab());
        tempTabbedPane.addTab("Reservations", getReservationTab());
        tempTabbedPane.addTab("Orders", getOrderTab());
        
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aE) {
                closeApp();
            }
        });
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        getContentPane().add("South", closeButton);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent aE) {
                closeApp();
            }
        });
    }
    
    protected JPanel getStoreTab() {
        return createPanel(storeTable, new JButton[] {addItemButton,
                                                     removeItemButton,
                                                     increaseStockButton,
                                                     decreaseStockButton,
                           });
    }

    protected JPanel getCustomerTab() {
        return createPanel(customerTable, new JButton[] {addCustomerButton,
                                                     removeCustomerButton,
                           });
    }
    
    protected JPanel getReservationTab() {
        return createPanel(reservationTable, new JButton[0]);
    }

    protected JPanel getOrderTab() {
        return createPanel(orderTable, new JButton[] {newOrderButton,
//                                                     removeOrderButton,
//                                                     changeOrderButton,
                           });
    }

    protected JPanel createPanel(JTable aTable, JButton[] aButtonArray) {
        JPanel tempPanel = new JPanel();

        ButtonListLayout tempButtonListLayout = new ButtonListLayout(ButtonListLayout.Orientation.VERTICAL);
        tempButtonListLayout.setFixedButtonSize(preferredButtonSize);
        tempButtonListLayout.setButtonSpacing(10);
        tempButtonListLayout.setPanelSpacing(10);
        tempPanel.setLayout(tempButtonListLayout);
        
        tempPanel.add(new JScrollPane(aTable), ButtonListLayout.ComponentType.LIST_OR_TABLE_PANEL);
        
        for (JButton tempButton : aButtonArray) {
            tempPanel.add(tempButton, ButtonListLayout.ComponentType.BUTTON_PANEL);
        }
        return tempPanel;
    }
    
    protected Dimension round(Dimension aDimension) {
        // round Dimension to the next whole 5 or 10
        int tempAddWidth = 5 - (aDimension.width % 5);
        int tempAddHeight = 5 - (aDimension.height % 5);
        aDimension.width += tempAddWidth; 
        aDimension.height += tempAddHeight;
        return aDimension;
    }
    
    protected void closeApp() {
        System.exit(0); 
    }
    
       
}
