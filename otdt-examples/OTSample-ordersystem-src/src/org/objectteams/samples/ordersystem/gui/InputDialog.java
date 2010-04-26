/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: InputDialog.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.objectteams.samples.ordersystem.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.objectteams.samples.ordersystem.data.Address;
import org.objectteams.samples.ordersystem.data.Customer;
import org.objectteams.samples.ordersystem.order.StockOrder;
import org.objectteams.samples.ordersystem.store.StockItem;



@SuppressWarnings("serial")
public class InputDialog extends JDialog {
    
    /**
     * Options that can be selected by user: <br>
     * OK - proceed <br>
     * CANCEL - cancel actual dialog
     */
    protected static enum InputOption {
        OK, CANCEL
    }
    
    /**
     * The textfields for user input.
     */
    protected JTextField[] inputFields;
    
    /**
     * Option that the user selected (InputOption.OK or InputOption.CANCEL).
     */
    protected InputOption selectedOption = InputOption.CANCEL;
    
    
    /**
     * Creates a new input dialog. Constructor is protected because only static methods of this
     * class should be used to create an input dialog.
     * 
     * @param aParent the dialog's parent
     * @param aTitle the dialog's title
     * @param aInputNames the names of input fields
     */
    protected InputDialog(JFrame aParent, String aTitle, String[] aInputNames) {
        this(aParent, aTitle);
        
        int tempInputCount = aInputNames.length;        
        JPanel tempInputPanel = new JPanel();
        tempInputPanel.setLayout(new GridLayout(tempInputCount, 2));
        inputFields = new JTextField[tempInputCount];        
        for (int i = 0; i < aInputNames.length; i++) {
            String tempInput = aInputNames[i];
            tempInputPanel.add(new JLabel(tempInput + ": "));
            inputFields[i] = new JTextField(100);
            tempInputPanel.add(inputFields[i]);
        }
        add("Center", tempInputPanel);
        
        setSize();
        setVisible(true);
    }

    /**
     * Creates a new input dialog based on the given components.
     * Components are simply layout using a vertical Box.
     * 
     * @param aParent the dialog's parent
     * @param aTitle the dialog's title
     * @param aDisplayedComponents
     */
    protected InputDialog(JFrame aParent, String aTitle, Component[] aDisplayedComponents) {
        this(aParent, aTitle);
        
        Box tempInputPanel = Box.createVerticalBox();
        for (Component tempComponent : aDisplayedComponents) {
            tempInputPanel.add(tempComponent);
        }
        add("Center", tempInputPanel);
        
        setSize();
        setVisible(true);
    }
    
    /**
     * Creates an empty unvisible input dialog (only buttons visible).
     * 
     * @param aParent the dialog's parent
     * @param aTitle the dialog's title
     */
    protected InputDialog(JFrame aParent, String aTitle) {
        super(aParent, aTitle, true);
        setLayout(new BorderLayout());
        
        Box tempButtonPanel = Box.createHorizontalBox();
        JButton tempOkayButton = new JButton("Okay");
        tempOkayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent anEvent) {
                selectedOption = InputOption.OK;
                setVisible(false);
            }
        });
        tempButtonPanel.add(tempOkayButton);
        tempButtonPanel.add(Box.createHorizontalGlue());
        JButton tempCancelButton = new JButton("Cancel");
        tempCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent anEvent) {
                selectedOption = InputOption.CANCEL;
                setVisible(false);
            }
        });
        tempButtonPanel.add(tempCancelButton);
        add("South", tempButtonPanel);
    }
    
    protected InputDialog(final JFrame aParent, 
                final JComboBox aCustomerComboBox, final JComboBox anAddressComboBox,
                final JList aSelectItemList, final JList anOrderItemList) {
        this(aParent, "New Order");
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent aE) {
                super.windowOpened(aE);
                setSize();
                repaint();
            }
        });
        
        // create customer combobox
        JPanel tempCustomerPanel = new JPanel();
        tempCustomerPanel.setLayout(new GridLayout(3,2));
        tempCustomerPanel.add(new JLabel("Select Customer: "));
        aCustomerComboBox.setRenderer(new CustomerRenderer());
        tempCustomerPanel.add(aCustomerComboBox);
        // TODO: order address
        tempCustomerPanel.add(new JLabel("Order Address: "));
        anAddressComboBox.addItem("NONE");
        anAddressComboBox.addItem("New Order Address");
        anAddressComboBox.setRenderer(new AddressRenderer());
        anAddressComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aE) {
                if (anAddressComboBox.getSelectedItem().equals("New Order Address")) {
                    Address tempAddress = showAddAddressDialog(aParent);
                    if (tempAddress != null) {
                        anAddressComboBox.removeAllItems();
                        anAddressComboBox.addItem(tempAddress);
                        anAddressComboBox.addItem("NONE");
                        anAddressComboBox.addItem("New Order Address");
                    }
                    anAddressComboBox.setSelectedIndex(0);
                }
            }
        });
        tempCustomerPanel.add(anAddressComboBox);
        // fill with empty components
        tempCustomerPanel.add(new JPanel());
        tempCustomerPanel.add(new JPanel());
        add("North", tempCustomerPanel);

        // create item lists
        anOrderItemList.setCellRenderer(new OrderItemCellRenderer());
        anOrderItemList.setModel(new DefaultListModel());
        
        aSelectItemList.setCellRenderer(new ItemCellRenderer());
        
        JButton tempButton = new JButton("->");
        tempButton.setBackground(new Color(238, 238, 238));
        tempButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent anEvent) {
                try {
                    StockItem tempSelectedItem = (StockItem) aSelectItemList.getSelectedValue();
                    Integer tempAmount = InputDialog.showChangeAmountDialog(aParent, "Item Amount in Order");
                    if (tempSelectedItem != null && tempAmount != null) {
                        DefaultListModel tempModel = (DefaultListModel) anOrderItemList.getModel();
                        OrderItem tempOrderItem = new OrderItem(tempSelectedItem, tempAmount);                        
                        if (! tempModel.contains(tempOrderItem)) {
                            tempModel.addElement(tempOrderItem);
                        }
                    }
                }
                catch (Exception ex) {
                    // error message output
                    ex.printStackTrace();
                }
            }
        });

        JPanel tempListPanel = new JPanel();
        tempListPanel.setLayout(new GridLayout(1,3));
        JScrollPane tempSelectItemScrollPane = new JScrollPane(aSelectItemList);
        JScrollPane tempOrderItemScrollPane = new JScrollPane(anOrderItemList);
        tempListPanel.add(tempSelectItemScrollPane);
        tempListPanel.add(tempButton);
        tempListPanel.add(tempOrderItemScrollPane);
        
        add("Center", tempListPanel);
        
        setSize();
        setVisible(true);
    }
    
    
    /**
     * Shows dialog that requests stock item data.
     * 
     * @param aParent the dialogs parents or null, if no parent should be set.
     * @return the stock item that is created from user data or null if any error occured or the 
     * user has canceled dialog
     */
    public static StockItem showAddItemDialog(JFrame aParent) {
        String[] data = createInputDialog(aParent, "New StockItem", new String[] {"Name", "Price (in Cent)"});
        try{
            if (data != null) {
                return new StockItem(data[0], Integer.valueOf(data[1]));
            }
            else {
                return null;
            }
        }
        catch (NumberFormatException e) { 
            // can't convert to integer, don't create item (not a user-friendly way ;-)
            return null;
        }
    }
    
    /**
     * Shows dialog that requests stock item data.
     * 
     * @param aParent the dialog's parents or null, if no parent should be set.
     * @return the stock item that is created from user data or null if any error occured or the 
     * user has canceled dialog
     */
    public static Customer showAddCustomerDialog(JFrame aParent) {
        String[] data = createInputDialog(aParent, "New Customer", 
                new String[] {"First Name", "Last Name", "Street", "Postal Code", "City", "Country"});
        if (data != null) {
            Address tempAddress = new Address(data[2], data[3], data[4], data[5]);
            return new Customer(data[0], data[1], tempAddress);
        }
        else {
            return null;
        }
    }
    
    /**
     * Shows dialog that requests stock item data.
     * 
     * @param aParent the dialog's parents or null, if no parent should be set.
     * @return the stock item that is created from user data or null if any error occured or the 
     * user has canceled dialog
     */
    public static Address showAddAddressDialog(JFrame aParent) {
        String[] data = createInputDialog(aParent, "New Address", 
                new String[] {"Street", "Postal Code", "City", "Country"});
        if (data != null) {
            return new Address(data[0], data[1], data[2], data[3]);
        }
        else {
            return null;
        }
    }
    
    /**
     * Shows dialog that requests item change count.
     * 
     * @param aParent the dialog's parents or null, if no parent should be set.
     * @param increase indicates if stock item count will be increased, if false decreasing is selected 
     * @return the amount to increase resp. decrease stock, null if none
     */
    public static Integer showChangeStockDialog(JFrame aParent, boolean increase) {
        String tempTitle = increase ? "Increase Item Count" : "Decrease Item Count";
        return showChangeAmountDialog(aParent, tempTitle);
    }

    /**
     * Shows dialog that requests an amount.
     * 
     * @param aParent the dialog's parent (null, if none)
     * @param aTitle the dialog's title
     * @return the user's input amount, null if none
     */
    public static Integer showChangeAmountDialog(JFrame aParent, String aTitle) {
        String[] data = createInputDialog(aParent, aTitle, new String[] {"Enter Amount"});
        if (data != null && ! data[0].equals("")) {
            return Integer.valueOf(data[0]);
        }
        else {
            return null;
        }
    }
    
    /**
     * Creates an input dialog that requests user data based on the given input names.
     * For each name, a field is provided that delivers a string. 
     * These strings are collected by a data array.
     * 
     * @param aParent the dialog's parent (null, if none)
     * @param aTitle the dialog's title
     * @param aInputNames the names of input
     * @return the data array referencing the user input as string; note, values can be null or empty strings
     */
    public static String[] createInputDialog(JFrame aParent, String aTitle, String[] aInputNames) {
        InputDialog tempDialog = new InputDialog(aParent, aTitle, aInputNames);
        if (tempDialog.selectedOption == InputOption.OK) {
            String[] tempInput = new String[aInputNames.length];
            for (int i = 0; i < tempInput.length; i++) {
                tempInput[i] = tempDialog.inputFields[i].getText();
            }
            for (String tempString : tempInput) {
                if (tempString.equals("")) {
                    return null;
                }
            }
            return tempInput;
        }
        else {
            return null;            
        }
    }
    
    /**
     * Shows dialog that requests input for creating a new order.
     * 
     * @return an order
     */
    public static StockOrder showAddOrderDialog(final JFrame aParent, Customer[] aCustomers, StockItem[] aItems) {
        
        JComboBox tempCustomerComboBox = new JComboBox(aCustomers);
        JComboBox tempAddressComboBox = new JComboBox();
        JList tempSelectItemList = new JList(aItems);
        JList tempOrderItemList = new JList();
        
        InputDialog tempDialog = new InputDialog(aParent, tempCustomerComboBox, tempAddressComboBox, 
                                                 tempSelectItemList, tempOrderItemList);
        
        // collect input and return new Order
        if (tempDialog.selectedOption == InputOption.OK) {
            // get Customer and order address and create new order
            Customer tempChoosenCustomer = (Customer) tempCustomerComboBox.getSelectedObjects()[0];
            if (tempChoosenCustomer != null) { 
                // TODO: distinguish between orders with and without order address 
                StockOrder tempOrder;
                Object tempAddressElement = tempAddressComboBox.getItemAt(0);
                if (tempAddressElement instanceof Address) {
                    if (tempAddressComboBox.getSelectedIndex() > 0) {
                        tempOrder = new StockOrder(tempChoosenCustomer, (Address) tempAddressElement, true);
//                        tempOrder = new StockOrder(tempChoosenCustomer, (Address) tempAddressElement);
                    }
                    else {
                        tempOrder = new StockOrder(tempChoosenCustomer, (Address) tempAddressElement);
                    }
                }
                else {
                    tempOrder = new StockOrder(tempChoosenCustomer);
                }
                // get order items and add them to order
                DefaultListModel tempModel = (DefaultListModel) tempOrderItemList.getModel();
                for (int i = 0; i < tempModel.size(); i++) {
                    OrderItem tempItem = (OrderItem) tempModel.getElementAt(i);
                    tempOrder.order(tempItem.getItem(), tempItem.getAmount());
                }
                return tempOrder;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }
    
    
    /**
     * Sets size of dialog to (nearly) preferred size.
     */
    private void setSize() {
        setSize(400, getPreferredSize().height+50);
    }
    
    
    /**
     * 
     * Class implementing a cell renderer for customers
     */
    private static class CustomerRenderer extends DefaultListCellRenderer {

        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList aList, Object aValue, int anIndex, boolean aIsSelected, boolean aCellHasFocus) {
            JLabel tempLabel = (JLabel) super.getListCellRendererComponent(aList, aValue, anIndex, aIsSelected, aCellHasFocus);
            if (aValue instanceof Customer) {
                Customer aCustomer = (Customer) aValue;
                tempLabel.setText(aCustomer.getFirstname() + " " + aCustomer.getLastname());
            }
            else {
                tempLabel.setText("");
            }
            return tempLabel;
        }
        
    }
    
    
    /**
     * 
     * Class implementing a cell renderer for addresses
     */
    private static class AddressRenderer extends DefaultListCellRenderer {
        
        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList aList, Object aValue, int anIndex, boolean aIsSelected, boolean aCellHasFocus) {
            JLabel tempLabel = (JLabel) super.getListCellRendererComponent(aList, aValue, anIndex, aIsSelected, aCellHasFocus);
            if (aValue instanceof Address || aValue instanceof String) {
                tempLabel.setText(aValue.toString());
            }
            else {
                tempLabel.setText("");
            }
            return tempLabel;
        }
        
    }
       
    
    /**
     * 
     * Class implementing a cell renderer for items
     */
    private static class ItemCellRenderer extends DefaultListCellRenderer {

        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList aList, Object aValue, int anIndex, boolean aIsSelected, boolean aCellHasFocus) {
            JLabel tempLabel = (JLabel) super.getListCellRendererComponent(aList, aValue, anIndex, aIsSelected, aCellHasFocus);
            if (aValue instanceof StockItem) {
                StockItem anItem = (StockItem) aValue;
                tempLabel.setText(anItem.getId() + " - " + anItem.getName());
            }
            else {
                tempLabel.setText("");
            }
            return tempLabel;
        }
        
    }
    
    /**
     * 
     * Class implementing a cell renderer for order items
     */
    private static class OrderItemCellRenderer extends DefaultListCellRenderer {

        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList aList, Object aValue, int anIndex, boolean aIsSelected, boolean aCellHasFocus) {
            JLabel tempLabel = (JLabel) super.getListCellRendererComponent(aList, aValue, anIndex, aIsSelected, aCellHasFocus);
            if (aValue instanceof OrderItem) {
                OrderItem anItem = (OrderItem) aValue;
                tempLabel.setText(anItem.getId() + " - " + anItem.getName() + " (# " + anItem.getAmount() + ")");
            }
            else {
                tempLabel.setText("");
            }
            return tempLabel;
        }
        
    }
    
    /**
     * 
     * Class representing order items.
     */
    private static class OrderItem {
        
        private StockItem item;
        private int amount;
        
        OrderItem(StockItem anItem, int anAmount) {
            item = anItem;
            amount = anAmount;            
        }
        
        /**
         * @return the amount
         */
        int getAmount() {
            return amount;
        }
        
        /**
         * @return the item's id
         */
        Integer getId() {
            return item.getId();
        }
        
        /**
         * @return the item's name
         */
        String getName() {
            return item.getName();
        }
        
        /**
         * @return the item
         */
        StockItem getItem() {
            return item;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object anotherObject) {
            if (anotherObject instanceof OrderItem) {
                OrderItem anotherItem = (OrderItem) anotherObject;
                return item.equals(anotherItem.item);
            }
            else {
                return false;
            }
        }

    }
    

}
