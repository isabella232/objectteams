/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FlightBookingGUI.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.objectteams.example.flightbooking.FlightBookingSystem;
import org.eclipse.objectteams.example.flightbooking.model.BookingException;
import org.eclipse.objectteams.example.flightbooking.model.Flight;
import org.eclipse.objectteams.example.flightbooking.model.Passenger;
import org.eclipse.objectteams.example.flightbooking.model.PassengerDB;
import org.eclipse.objectteams.example.flightbooking.util.FlightIterator;



/**
* This class implements a simple graphical user interface for the flight booking system.
*/
@SuppressWarnings("serial")
public class FlightBookingGUI extends JFrame {
   
	private JList 	_flightList;
	private JList 	_passengerList;
	
	private FlightBookingSystem _system;
	private FlightBookingGUI 	_frame = this;
	private DefaultListModel 	_flightModel;
	private DefaultListModel 	_customerModel;
	private Flight 			_selectedFlight;
	private Passenger 		_selectedCustomer;
	
	/**
	 * Creates new <code>FlightBookingGUI</code> object 
	 */
	public FlightBookingGUI(FlightBookingSystem system) {
		_system = system;
		_customerModel = new DefaultListModel();
		_flightModel = new DefaultListModel();
		initFlightModel();
		initCustomerModel();
		initComponents();
		this.setResizable(false);
	}
   
	/**
	 * Initializes the <code>FlightModel</code> with data contained 
	 * in the flight booking system,
	 */
	private void initFlightModel() {
		for (FlightIterator iter = _system.getOfferedFlights(); iter.hasNext();) {
			_flightModel.addElement(iter.getNext());
		}		
	}
	
	/**
	 * Initializes the <code>PassengerModel</code> with data contained 
	 * in the flight booking system,
	 */
	private void initCustomerModel() {
		for (PassengerDB passengers = _system.getRegisteredPassengers(); passengers.hasNext();) {
			_customerModel.addElement(passengers.getNext());
		}		
	}
	/**
	 * Returns the customer
	 */
	public DefaultListModel getCustomerModel() {
		return _customerModel;
	}
	
	/**
	 * Returns the flight model 
	 */
	public DefaultListModel getFlightModel() {
		return _flightModel;
	}
	
	/**
	 * Invoked when the bookButton is pressed
	 */
	public void bookButtonClicked() {
						
		if(_selectedFlight == null) {
			showErrDialog("Please select a flight!");
			return;
		}
		
		if(_selectedCustomer == null) {
			showErrDialog("Please select a passenger!");
			return;
		}
		
		Passenger prev = _system.getCurrentPassenger();
		if (prev != null) {
			_system.disableCurrentPassenger(prev.getName());
		}
		
		_system.setCurrentPassenger(_selectedCustomer.getName());
		
		try {
			_system.bookFlight(_flightList.getSelectedIndex());
		} catch (BookingException ex) {
			showErrDialog("Booking transaction failed: " + ex.getMessage());
			return;
		}
						
		showInfoDialog("Passenger " + _selectedCustomer.getName() + ":  " 
				+ _selectedFlight + "\n"
				+ _selectedCustomer.getName() + "'s balance :  " 
				+ _selectedCustomer.getBudget());	
	}
	
    
	/** 
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
        GridBagConstraints gridBagConstraints = null;
        
        JButton bookButton = new JButton();
        JButton customerButton = new JButton();
        JButton exitButton = new JButton();
        JScrollPane flightPane = new JScrollPane();
        JScrollPane passPane = new JScrollPane();
        _flightList = new JList(_flightModel);
        _passengerList = new JList(_customerModel);

        getContentPane().setLayout(new GridBagLayout());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Flight Booking System");

        JLabel flightsLabel = new JLabel();
        flightsLabel.setText("Offered Flights: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 19, 5, 17);
        getContentPane().add(flightsLabel, gridBagConstraints);

        JLabel passLabel = new JLabel();
        passLabel.setText("Passengers: ");
        passLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 43);
        getContentPane().add(passLabel, gridBagConstraints);
                
        _flightList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        flightPane.setPreferredSize(new Dimension(250, 250));
        flightPane.setViewportView(_flightList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 10);
        getContentPane().add(flightPane, gridBagConstraints);

        _passengerList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        passPane.setPreferredSize(new Dimension(100, 250));
        passPane.setViewportView(_passengerList);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        getContentPane().add(passPane, gridBagConstraints);

        
        customerButton.setText("New Passenger");
        customerButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		showEnterCustomerDialog();
        	}
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
//        gridBagConstraints.insets = new Insets(0, 20, 0, 0);
        getContentPane().add(customerButton, gridBagConstraints);

        bookButton.setText("Book");
        bookButton.setPreferredSize(customerButton.getPreferredSize());
        bookButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookButtonClicked();
            }
        });
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        getContentPane().add(bookButton, gridBagConstraints);

        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridBagLayout());

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        btnPanel.add(exitButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 10, 10);
        getContentPane().add(btnPanel, gridBagConstraints);
        
        
        _flightList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                flightListValueChanged(evt);
            }
        });
        
        _passengerList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                customerListValueChanged(evt);
            }
        });
              
        pack();
    }
    	
	/**
	 * Updates the selected flight reference when the flight list value is changed
	 */
	public void flightListValueChanged(ListSelectionEvent evt) {
		_selectedFlight = (Flight)_flightList.getSelectedValue();		
	}
	
	/**
	 * Updates the selected customer reference when the passenger list value is changed
	 */
	public void customerListValueChanged(ListSelectionEvent evt) {
		if(!_passengerList.isSelectionEmpty()) {
			_selectedCustomer = (Passenger)_passengerList.getSelectedValue();
		}
	}
	
	/**
	 * Shows the given message in a message dialog box 
	 */
    private void showInfoDialog(final String message){    	
    		JOptionPane.showMessageDialog(_frame, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Shows the given message in a error dialog box
     */
    private void showErrDialog(final String message){    	
    		JOptionPane.showMessageDialog(_frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Shows the <code>EnterPassengerDialog</code> for entering of new passenger data  
     */
    private void showEnterCustomerDialog() {
	    	this.setEnabled(false);
	    	new EnterPassengerDialog(_frame,false,_system);    
    }
}
