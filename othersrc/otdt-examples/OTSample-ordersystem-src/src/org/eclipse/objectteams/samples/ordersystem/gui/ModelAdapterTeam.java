/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ModelAdapterTeam.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.eclipse.objectteams.samples.ordersystem.data.Address;
import org.eclipse.objectteams.samples.ordersystem.data.Customer;
import org.eclipse.objectteams.samples.ordersystem.order.StockOrder;
import org.eclipse.objectteams.samples.ordersystem.reservation.Reservations;
import org.eclipse.objectteams.samples.ordersystem.store.StockItem;
import org.objectteams.Team;

import base org.eclipse.objectteams.samples.ordersystem.reservation.StockReservations;
import base org.eclipse.objectteams.samples.ordersystem.store.Storage;



/**
 * Team providing the model of the order system. 
 * It does so by binding to base classes from the packages data and store.
 * 
 * Three different kinds of roles exist:
 * - CustomerRole provides a specific view to its underlying base object.
 * - Different adapters present a collection of base objects as a table:
 *   CustomerAdapter, OrderAdapter, ReservationAdapter, StorageAdaptor
 * - ModelInitializer intercepts switching between tabs in order to switch models accordingly.
 * 
 * @author Dehla Sokenou
 */
public team class ModelAdapterTeam {
    
    
    protected static ModelAdapterTeam theModelAdapterTeam;

    public static ModelAdapterTeam getModelAdapterTeam() {
        if (theModelAdapterTeam == null) {
            theModelAdapterTeam = new ModelAdapterTeam();
        }
        return theModelAdapterTeam;
    }
    
    protected ModelAdapterTeam() {
        this.activate(Team.ALL_THREADS);
        theModelAdapterTeam = this;
        initialize();
    }
    
    // some things to fill storage, customer and addresses
    protected StockItem[] initItems = new StockItem[] {
            new StockItem("screw driver", 1500),
            new StockItem("tool box pro", 5000),
            new StockItem("stepladder", 7999),
            new StockItem("philips screw", 155),
            new StockItem("wall paint", 1680),
            new StockItem("paint brush", 800)
        };
    
    protected Address[] initAddresses = new Address[] {
            new Address("22, october avenue", "2345", "Chicago", "Il,USA"),
            new Address("Blaumeisenweg 5", "12345", "Berlin", "Germany"),
            new Address("10, Lundy Avenue", "513648", "Paris", "France"),
            new Address("Venus/Mars", "135754", "Io", "Jupiter"),
            new Address("Dan Tokpa", "14440", "Cotonou", "Benin")
        };

    protected Customer[] initCustomers = new Customer[] {
            new Customer("Joe", "Jojo", initAddresses[0]),
            new Customer("Jim", "Beann", initAddresses[1]),
            new Customer("Jan", "Tatam", initAddresses[2]),
            new Customer("Paulchen", "Panther", initAddresses[3])
        };

    
    protected void initialize() {
        fillStorage();
        createCustomers();
        createOrders();
    }
    
    protected void fillStorage() {
        StorageAdapter storage = StorageAdapter.theInstance();
        storage.add(initItems[0]);
        storage.add(initItems[1]);
        initItems[1].put(20);
        storage.add(initItems[2]);
        storage.add(initItems[3]);
        initItems[3].put(1);
        storage.add(initItems[4]);
        initItems[4].put(200);
        storage.add(initItems[5]);
        initItems[5].put(3);
    }

    protected void createCustomers() {
        CustomerAdapter tempCustomerAdapter = getCustomerAdapter();
        //  TODO: better use callin from constructor as soon as it is available
        tempCustomerAdapter.addElement(initCustomers[0]);     
        tempCustomerAdapter.addElement(initCustomers[1]);
        tempCustomerAdapter.addElement(initCustomers[2]);
        tempCustomerAdapter.addElement(initCustomers[3]);
    }
    
    protected void createOrders() {
        //  TODO: better use callin from constructor as soon as it is available
        OrderAdapter tempOrderAdapter = getOrderAdapter();
        StockOrder tempOrder1 = new StockOrder(initCustomers[0]);
        tempOrder1.order(initItems[4], 100);
        tempOrder1.order(initItems[5], 2);
        tempOrder1.deliver();
        tempOrderAdapter.addElement(tempOrder1);

        StockOrder tempOrder2 = new StockOrder(initCustomers[1]);
        tempOrder2.order(initItems[4], 200);
        tempOrderAdapter.addElement(tempOrder2);

        StockOrder tempOrder3 = new StockOrder(initCustomers[2], initAddresses[4]);
        tempOrder3.setDiscount(initItems[4], 0.10);
        tempOrder3.order(initItems[4], 200);
        tempOrder3.order(initItems[5], 2);
        tempOrderAdapter.addElement(tempOrder3);
    }
    
    
    // the adapters implementing AbstractModelTemplate
    protected StorageAdapter theStorageAdapter;
    protected CustomerAdapter theCustomerAdapter;
    protected ReservationAdapter theReservationAdapter;
    protected OrderAdapter theOrderAdapter;
    
    
    /**
     * @return the store item list model
     */
    public StorageAdapter getStorageAdapter() {
        if (theStorageAdapter == null) {
            theStorageAdapter = StorageAdapter.theInstance();
        }
        return theStorageAdapter;
    }
    
    /**
     * @return the customer list model
     */
    public CustomerAdapter getCustomerAdapter() {
        if (theCustomerAdapter == null) {
            theCustomerAdapter = new CustomerAdapter();
        }
        return theCustomerAdapter;
    }
    
    /**
     * @return the reservation list model
     */
    public ReservationAdapter getReservationAdapter() {
        if (theReservationAdapter == null) {
            theReservationAdapter = ReservationAdapter.theInstance();
        }
        return theReservationAdapter;
    }
    
    /**
     * @return the customer list model
     */
    public OrderAdapter getOrderAdapter() {
        if (theOrderAdapter == null) {
            theOrderAdapter = new OrderAdapter();
        }
        return theOrderAdapter;
    }
    
    /**
     * Updates table model in the given tab. 
     * @param aTab the model tab to update
     */
    public void update(Tab aTab) {
        switch (aTab) {
            case STORE : 
                getStorageAdapter().fireTableDataChanged(); 
                break;
            case CUSTOMER : 
                getCustomerAdapter().fireTableDataChanged();
                break;
            case RESERVATION : 
                getReservationAdapter().fireTableDataChanged();
                break;
            case ORDER : 
                getOrderAdapter().fireTableDataChanged();
                break; 
            default: 
                throw new IllegalArgumentException("Method not defined for argument " + aTab);
        }
    }
    
    /**
     * Lifts a customer to its role in this team.
     * @param aCustomer the customer to lift
     */
    protected void liftCustomerToRole(Customer as CustomerRole aCustomer) {
        // nop, lifting is all we want
    }
    
    
    /**
     * Initializes model.
     */
    protected class ModelInitializer playedBy OrderSystemMainFrame {
        
        // callouts to tables
        JTable getStoreTable()       -> get JTable storeTable;
        JTable getCustomerTable()    -> get JTable customerTable;
        JTable getReservationTable() -> get JTable reservationTable;
        JTable getOrderTable()       -> get JTable orderTable;
        
        // callins to set models
        void setModels(Tab aTab) <- after JPanel getStoreTab()       with { aTab <- Tab.STORE }
        void setModels(Tab aTab) <- after JPanel getCustomerTab()    with { aTab <- Tab.CUSTOMER }
        void setModels(Tab aTab) <- after JPanel getReservationTab() with { aTab <- Tab.RESERVATION }
    setOrderModel: 
        void setModels(Tab aTab)       <- after JPanel getOrderTab() with { aTab <- Tab.ORDER }
    setOrderRenderer: 
        void setCellRenderer(Tab aTab) <- after JPanel getOrderTab() with { aTab <- Tab.ORDER }
      
        // two callins to the same base method exist, must declare precedence:
        precedence after setOrderRenderer, setOrderModel;
        
        
        protected void setModels(Tab aTab) {
            JTable tempTable;
            AbstractTableModel tempTableModel;
            switch (aTab) {
                case STORE: 
                    tempTable      = getStoreTable();
                    tempTableModel = getStorageAdapter();
                    break;
                case CUSTOMER: 
                    tempTable      = getCustomerTable();
                    tempTableModel = getCustomerAdapter();
                    break;
                case RESERVATION: 
                    tempTable      = getReservationTable();
                    tempTableModel = getReservationAdapter();
                    break;
                case ORDER: 
                    tempTable      = getOrderTable();
                    tempTableModel = getOrderAdapter(); 
                    break;
                default: 
                    throw new IllegalArgumentException("Method not defined for argument " + aTab);
            }
            if (tempTableModel != null) {
                tempTable.setModel(tempTableModel);
            }
            tempTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        }
        
        @SuppressWarnings("serial") // regarding nested anonymous class
		protected void setCellRenderer(Tab aTab) {
            final JTable tempTable;
            switch (aTab) {
                case ORDER: 
                    tempTable = getOrderTable();
                    TableColumn tempColumn = tempTable.getColumnModel().getColumn(0);
                    tempColumn.setCellRenderer(new DefaultTableCellRenderer() {
                        public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn) {
                            JLabel tempLabel = (JLabel) super.getTableCellRendererComponent(aTable, aValue, aIsSelected, aHasFocus, aRow, aColumn);
                            String tempText = "<html>" + aValue.toString().replace("\n", "<br>").replace("Total", "<b>Total") +  "</b></html>";
                            tempLabel.setText(tempText);
                            tempTable.setRowHeight(aRow, tempLabel.getPreferredSize().height);
                            return tempLabel;
                        }
                    });
                    break;
                default: 
                    // do nothing, allows overriding by subroles
                    break;
            }            
        }
    }
    
    
    /**
     * Manages store item via store class.
     */
    @SuppressWarnings("serial")
    public class StorageAdapter extends AbstractTableModelTemplate<StockItem> playedBy Storage {
        
        protected StorageAdapter(Storage myBase) {
            ModelAdapterTeam.this.theStorageAdapter = this;
        }
        
        protected String[] columnNames = new String[] {
                                        "Id", "Name", "Price", "Count on Stock"
                                   };
        
        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getColumnNames()
         */
        protected String[] getColumnNames() {
            return columnNames;
        }

        /**
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int aRowIndex, int aColumnIndex) {
            Vector<StockItem> tempElements = getElements();
            StockItem tempItem = tempElements.elementAt(aRowIndex);
            switch (aColumnIndex) {
                case 0 : 
                    return tempItem.getId();
                case 1 : 
                    return tempItem.getName();
                case 2 : 
                    return tempItem.getPriceString();
                case 3 : 
                    return tempItem.getCount();
                default : 
                    return null;
            }
        }
        
        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getElements()
         */
        // Note: abstract method is implemented using callout binding:
        @SuppressWarnings("decapsulation")
		public Vector<StockItem> getElements() -> get HashMap<Integer,StockItem> items 
        	with {
                    result <- new Vector<StockItem>(items.values())
            }

        StorageAdapter theInstance() -> Storage theInstance();
        
		void add(StockItem item) -> void add(StockItem item);
		
		void delete(StockItem item) -> void delete(StockItem item);

    }
    
    
    /**
     * CustomerAdapter is not a real role class (unbound). Manages the customers (see CustomerRoles).
     */
    @SuppressWarnings("serial")
    public class CustomerAdapter extends AbstractTableModelTemplate<Customer> {

        protected String[] columnNames = new String[] {
                                    "First Name", "Last Name", "Street", "Postal Code", "City", "Country"};
        
        protected Vector<Customer> elements = new Vector<Customer>();
        
        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getColumnNames()
         */
        protected String[] getColumnNames() {
            return columnNames;
        }

        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getValueAt(int, int)
         */
        public Object getValueAt(int aRowIndex, int aColumnIndex) {
            Vector<Customer> tempElements = getElements();
            Customer tempCustomer = tempElements.elementAt(aRowIndex);
            CustomerRole tempCustomerRole = ModelAdapterTeam.this.getRole(tempCustomer, CustomerRole.class);
            switch (aColumnIndex) {
                case 0 : 
                    return tempCustomerRole.getFirstName();
                case 1 : 
                    return tempCustomerRole.getLastName();
                case 2 : 
                    return tempCustomerRole.getStreet(); 
                case 3 : 
                    return tempCustomerRole.getPostalcode();
                case 4 : 
                    return tempCustomerRole.getCity();
                case 5 : 
                    return tempCustomerRole.getCountry();
                default : 
                    return null;
            }
        }
        
        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getElements()
         */
        public Vector<Customer> getElements() {
            return elements;
        }
        
        /**
         * Adds an element (customer) to the list of customers.
         * @param aCustomer the customer to add
         */
        public void addElement(Customer aCustomer) {
            if (! ModelAdapterTeam.this.hasRole(aCustomer, CustomerRole.class)) {
                ModelAdapterTeam.this.liftCustomerToRole(aCustomer);
            }
            elements.add(aCustomer);
        }
        
        /**
         * Removes an element (customer) from the list of customers.
         * @param aCustomer the customer to remove
         */
        public void removeElement(Customer aCustomer) {
            elements.remove(aCustomer);
        }

    }
    
    
    /**
     * Role played by customers in this team.
     * 
     * This role applies the "Virtual Restructuring" pattern:
     *   Although at the base level, Customer and Address are distinct entities,
     *   this role provides a combined view as if both classes had been 
     *   restructured into one.
     */
    @SuppressWarnings("bindingconventions") // can't use base import for Customer, because it is also used directly within this team.
    public class CustomerRole implements ILowerable playedBy Customer {
        
        // callouts to different fields in customer and address.
        // fields of customer are only included to have a uniform interface to customers.
        String getFirstName()  -> String getFirstname();
        String getLastName()   -> String getLastname();
        String getStreet()     -> Address getAddress()  with {result <- result.getStreet()};
        String getPostalcode() -> Address getAddress()  with {result <- result.getPostalcode()};
        String getCity()       -> Address getAddress()  with {result <- result.getCity()};
        String getCountry()    -> Address getAddress()  with {result <- result.getCountry()};        
    }
    
    
    /**
     * Manages the reservations via team StockReservations.
     */
    @SuppressWarnings("serial")
	public class ReservationAdapter extends AbstractTableModelTemplate<StockItem> playedBy StockReservations {

        protected String[] columnNames = new String[] {
                "Id", "Name", "Count on Stock", "# Reserved", "# Available"
           };
        
        // callouts
        int getNumAvail(StockItem anItem) -> int numAvail(StockItem anItem);

		ReservationAdapter theInstance() -> StockReservations theInstance();		
        
        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getColumnNames()
         */
        protected String[] getColumnNames() {
            return columnNames;
        }

        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getElements()
         */
        public Vector<StockItem> getElements() {
			return getStorageAdapter().getElements();
        }

        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getValueAt(int, int)
         */
        public Object getValueAt(int aRowIndex, int aColumnIndex) {
            Vector<StockItem> tempElements = getElements();
            StockItem tempItem = tempElements.elementAt(aRowIndex);
            boolean hasRole = Reservations.theInstance().hasRole(tempItem);
            switch (aColumnIndex) {
                case 0 : 
                    return tempItem.getId();
                case 1 : 
                    return tempItem.getName();
                case 2 : 
                    return tempItem.getCount();
                case 3 : 
                    if (hasRole) {
                        return tempItem.getCount() - getNumAvail(tempItem);
                    }
                    else {
                        return 0;
                    }
                case 4 : 
                    if (hasRole) {
                        return getNumAvail(tempItem);
                    }
                    else {
                        return tempItem.getCount();
                    }
                default : 
                    return null;
            }
        }
        
    }
    
    
    /**
     * Manages the reservations via team StockReservations.
     */
    @SuppressWarnings("serial")
	public class OrderAdapter extends AbstractTableModelTemplate<StockOrder> {
        
        protected Vector<StockOrder> elements = new Vector<StockOrder>();
        
        protected String[] columnNames = new String[] {
                "Order Information"
           };
        
        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getColumnNames()
         */
        protected String[] getColumnNames() {
            return columnNames;
        }
        
        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getElements()
         */
        public Vector<StockOrder> getElements() {
            return elements;
        }

        /**
         * @see org.eclipse.objectteams.samples.ordersystem.gui.AbstractTableModelTemplate#getValueAt(int, int)
         */
        public Object getValueAt(int aRowIndex, int aColumnIndex) {
            return getElements().elementAt(aRowIndex).toString();
        }

        /**
         * Adds an element (customer) to the list of customers.
         * @param aCustomer the customer to add
         */
        public void addElement(StockOrder anOrder) {
            elements.add(anOrder);
        }
        
        /**
         * Removes an element (customer) from the list of customers.
         * @param aCustomer the customer to remove
         */
        public void removeElement(StockOrder anOrder) {
            elements.remove(anOrder);
        }

    }
     
    
    // don't need these roles anymore, but was not deleted because they show another kind of MVC behaviour 
//    /**
//     * Role listening to changes that occur directly in observed model of reservations.
//     */
//    protected team class ReservationListener playedBy StockReservations {
//        
//        protected class ReservationItemListener playedBy base.Reservable {
//
//            // callins
//            update <- after reserve, release, check, invalidate, remove;
//            
//            protected void  update() {
//                ModelAdapterTeam.this.update(Tab.RESERVATION);
//            }
//            
//        }
//        
//    }
//    
//
//    /**
//     * Role listening to changes that occur directly in observed model of orders.
//     */
//    protected team class OrderListener playedBy StockOrder {
//        
//        protected class OrderItemListener playedBy base.Item {
//            
//        // callins
//        update <- after check, alarm, doReserve, doDeliver;
//        
//            protected void update() {
//                ModelAdapterTeam.this.update(Tab.RESERVATION);
//                ModelAdapterTeam.this.update(Tab.ORDER);
//            }
//        
//        }
//        
//        
//    }

    
    
}
