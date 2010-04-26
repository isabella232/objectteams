/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ControllerAdapterTeam.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.objectteams.samples.ordersystem.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;

import org.objectteams.Team;
import org.objectteams.samples.ordersystem.data.Customer;
import org.objectteams.samples.ordersystem.order.StockOrder;
import org.objectteams.samples.ordersystem.store.StockItem;
import org.objectteams.samples.ordersystem.store.Storage;

/**
 * @author Dehla Sokenou
 *
 * Team implementing controller of order system.
 * 
 */
public team class ControllerAdapterTeam {
    
        
    public ControllerAdapterTeam() {
        this.activate(Team.ALL_THREADS);
    }
    
    /**
     * Role that implements a controller logic for each button.
     */
    public class ButtonController implements ILowerable playedBy OrderSystemMainFrame {
                
    	// by this hook all elementary controllers are registered at startup:
        void setButtonControllers() <- after void setVisible(boolean aVisibility);
        
        // store buttons (callout to field):
        @SuppressWarnings("decapsulation")
		JButton getAddItemButton()        -> get JButton addItemButton; 
        JButton getRemoveItemButton()     -> get JButton removeItemButton;
        JButton getIncreaseStockButton()  -> get JButton increaseStockButton;
        JButton getDecreaseStockButton()  -> get JButton decreaseStockButton;
        
        // customer management buttons
        JButton getAddCustomerButton()    -> get JButton addCustomerButton;
        JButton getRemoveCustomerButton() -> get JButton removeCustomerButton;
        
        // reservation management buttons
        // none
        
        // order management buttons
        JButton getNewOrderButton()       -> get JButton newOrderButton;
        JButton getRemoveOrderButton()    -> get JButton removeOrderButton;
        JButton getChangeOrderButton()    -> get JButton changeOrderButton;
                
        // tables
        JTable getStoreTable()            -> get JTable storeTable;
        JTable getCustomerTable()         -> get JTable customerTable;
        JTable getReservationTable()      -> get JTable reservationTable;
        JTable getOrderTable()            -> get JTable orderTable;

        /**
         * Assign controllers for all buttons
         */
        protected void setButtonControllers() {
            final OrderSystemMainFrame tempOrderFrame = (OrderSystemMainFrame) this.lower();
            
            // store buttons
            getAddItemButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    StockItem tempItem = InputDialog.showAddItemDialog(tempOrderFrame);
                    if (tempItem != null) {
                        Storage.theInstance().add(tempItem);
                    }
                    updateModel(Tab.STORE);
                    updateModel(Tab.RESERVATION);
                }                
            });
            
            getRemoveItemButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    try {
                       StockItem tempItem = (StockItem) getSelectedIndex(Tab.STORE);
                       if (tempItem != null) {
                           Storage.theInstance().delete(tempItem);
                           updateModel(Tab.STORE);
                           updateModel(Tab.RESERVATION);
                           updateModel(Tab.ORDER);
                       }
                    }
                    catch (Exception e) {
                        // debug output
                        e.printStackTrace();
                    }
                }                
            });
            
            getIncreaseStockButton().addActionListener(
                            getChangeItemCountActionListener(tempOrderFrame, true));
            
            getDecreaseStockButton().addActionListener(
                            getChangeItemCountActionListener(tempOrderFrame, false));

            // reservation management buttons
            // none
            
            // customer management buttons
            getAddCustomerButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    try {
                        Customer tempCustomer = InputDialog.showAddCustomerDialog(tempOrderFrame);
                        if (tempCustomer != null) {
                            //  TODO: better use callin from constructor in ModelAdapterTeam as soon as it is available
                            ModelAdapterTeam.getModelAdapterTeam().getCustomerAdapter().addElement(tempCustomer);
                            updateModel(Tab.CUSTOMER);
                        }
                    }
                    catch (Exception e) {
                        // debug output
                        e.printStackTrace();
                    }
                }
            });
            
            getRemoveCustomerButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    try {
                        Customer tempCustomer = (Customer) getSelectedIndex(Tab.CUSTOMER);
                        ModelAdapterTeam.getModelAdapterTeam().getCustomerAdapter().removeElement(tempCustomer);
                        updateModel(Tab.CUSTOMER);
                    }
                     catch (Exception e) {
                         // debug output
                         e.printStackTrace();
                     }
                }
            });
            
            // order management buttons
            getNewOrderButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    Vector<Customer> tempCustomerElements = 
                            ModelAdapterTeam.getModelAdapterTeam().getCustomerAdapter().getElements();
                    Customer[] tempCustomers = new Customer[tempCustomerElements.size()];
                    for (int i = 0; i < tempCustomers.length; i++) {
                        tempCustomers[i] = tempCustomerElements.elementAt(i);
                    }
                    Vector<StockItem> tempItemElements = 
                            ModelAdapterTeam.getModelAdapterTeam().getStorageAdapter().getElements();
                    StockItem[] tempItems = new StockItem[tempItemElements.size()];
                    for (int i = 0; i < tempItems.length; i++) {
                        tempItems[i] = (StockItem) tempItemElements.elementAt(i);                        
                    }
                    StockOrder tempOrder = InputDialog.showAddOrderDialog(
                                                tempOrderFrame, tempCustomers, tempItems);
                    if (tempOrder != null) {
                        //  TODO: better use callin from constructor in ModelAdapterTeam as soon as it is available
                        ModelAdapterTeam.getModelAdapterTeam().getOrderAdapter().addElement(tempOrder);
                        updateModel(Tab.ORDER);
                    }
                }
            });
            
            getRemoveOrderButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    // TODO:
                }
            });
            
            getChangeOrderButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    // TODO:
                }
            });
                        
        }

        /**
         * Updates model after changes.
         */
        protected void updateModel(Tab aTab) {
            ModelAdapterTeam.getModelAdapterTeam().update(aTab);
        }
        
        /**
         * 
         */
        protected Object getSelectedIndex(Tab aTab) {
            JTable tempTable;
            switch (aTab) {
                case STORE      : tempTable = getStoreTable(); break;
                case CUSTOMER   : tempTable = getCustomerTable(); break;
                case RESERVATION: tempTable = getReservationTable(); break;
                case ORDER      : tempTable = getOrderTable(); break; 
                default: 
                    throw new IllegalArgumentException("Method not defined for argument " + aTab);
            }
            int tempSelectedIndex = tempTable.getSelectedRow();
            if (tempSelectedIndex >= 0) {
                //FIXME: remove if-statement later, workaround
                if (aTab == Tab.STORE) {
                    final ModelAdapterTeam tempModelAdapterTeam = ModelAdapterTeam.getModelAdapterTeam();
                    return ((StorageAdapter<@tempModelAdapterTeam>) tempTable.getModel()).getElementAt(tempSelectedIndex);
                }
                AbstractTableModelTemplate<?> tempModel = (AbstractTableModelTemplate<?>) tempTable.getModel();
                return tempModel.getElementAt(tempSelectedIndex);
            }
            else {
                return null;
            }
        }

        /**
         * @param tempOrderFrame the main frame
         * @param anInc indicates whether the count is increased (decreased otherwise)
         * @return
         */
        private ActionListener getChangeItemCountActionListener(
                    final OrderSystemMainFrame tempOrderFrame, final boolean anInc) {
            return new ActionListener() {
                public void actionPerformed(ActionEvent anEvent) {
                    try {
                        StockItem tempItem = (StockItem) getSelectedIndex(Tab.STORE);
                        Integer tempCount = InputDialog.showChangeStockDialog(tempOrderFrame, anInc);
                        if (tempItem != null && tempCount != null && tempCount.intValue() > 0) {
                            if (! anInc) {
                                tempCount = -tempCount;
                            }
                            Storage.theInstance().changeCount(tempItem, tempCount);
                            updateModel(Tab.STORE);
                            updateModel(Tab.RESERVATION);
                            updateModel(Tab.ORDER);
                        }
                    }
                    catch (Exception e) {
                        // debug output
                        e.printStackTrace();
                    }
                }                
            };
        }
    }
}
