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
 * $Id: EnterPassengerDialog.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.eclipse.objectteams.example.flightbooking.FlightBookingSystem;
import org.eclipse.objectteams.example.flightbooking.model.Passenger;


@SuppressWarnings("serial")
public class EnterPassengerDialog extends JDialog {
    
	private FlightBookingSystem 	_system;
	private FlightBookingGUI 		_parentFrame;
	private String 					_customerName = "";
	private int 					_customerBudget = 0;
	
	private JTextField _nameTextField;
	private JTextField _budgetTextField;
	
    public EnterPassengerDialog(
    			FlightBookingGUI parent, 
			boolean modal,
			FlightBookingSystem system) {
		super(parent, modal);
		_system = system;
		_parentFrame = parent;
		initComponents();
		setResizable(false);
		setVisible(true);
	}
    
    public void clearButtonClicked() {
		_nameTextField.setText("");
		_budgetTextField.setText("");		
	}
    
    public void okButtonClicked() {    	
	    	_customerName = _nameTextField.getText();
	    	
	    	if (_customerName.equals("")) {
			showErrDialog("Please enter the passenger's name");
			return;
		}	
    	
		try {
			_customerBudget = Integer.parseInt(_budgetTextField.getText());	
		} 
		catch (NumberFormatException ex) {
			showErrDialog("The characters in the budget string must be decimal digits");
			
			return;
		}
		
		Passenger prev = _system.getCurrentPassenger();
		if (prev != null) {
			_system.disableCurrentPassenger(prev.getName());
		}
		
		if (!_system.containsPassenger(_customerName)) {
			_system.registerPassenger(_customerName, _customerBudget);
			_system.setCurrentPassenger(_customerName);
			_parentFrame.getCustomerModel().addElement(_system.getCurrentPassenger());
		} else {			
			_system.setCurrentPassenger(_customerName);
			showInfoDialog("Passenger exists already");	
		}
		
		_parentFrame.setEnabled(true);
		setVisible(false);		
	}
    
    
    private void showErrDialog(final String message) {    	
    		JOptionPane.showMessageDialog(
    						this, 
						message, 
						"Error",
						JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfoDialog (final String message) {
    		JOptionPane.showMessageDialog(
    				this, 
				message, 
				"Message",
				JOptionPane.INFORMATION_MESSAGE);
    }
    
    /** 
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents(	) {
        GridBagConstraints gridBagConstraints = null;

        this.setTitle("New Passenger Dialog");
        JPanel jPanel1 = new JPanel();
        JLabel nameLabel = new JLabel();
        _nameTextField = new JTextField();
		JLabel budgetLabel = new JLabel();
        _budgetTextField = new JTextField();
		JPanel jPanel2 = new JPanel();
        JButton okButton = new JButton();
        JButton clearButton = new JButton();
        JButton cancelButton = new JButton();

        getContentPane().setLayout(new GridBagLayout());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jPanel1.setLayout(new GridBagLayout());

        nameLabel.setText("Passenger's Name: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 0, 6, 0);
        jPanel1.add(nameLabel, gridBagConstraints);

        _nameTextField.setColumns(12);
        _nameTextField.setText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.insets = new Insets(0, 6, 4, 0);
        jPanel1.add(_nameTextField, gridBagConstraints);

        budgetLabel.setText("Passenger's Budget: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        jPanel1.add(budgetLabel, gridBagConstraints);

        _budgetTextField.setText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        jPanel1.add(_budgetTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

	    clearButton.setText("Clear");
	    clearButton.addActionListener(new java.awt.event.ActionListener() {
	    	public void actionPerformed(java.awt.event.ActionEvent evt) {
	    		clearButtonClicked();
	      	}
	    });
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
	    jPanel2.add(clearButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
	    	public void actionPerformed(java.awt.event.ActionEvent evt) {
	    			okButtonClicked();
	      	}
	    });
        gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 30, 0, 0);
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel2.add(okButton, gridBagConstraints);
        
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
    		public void actionPerformed(java.awt.event.ActionEvent evt) {
    			_parentFrame.setEnabled(true);
    			setVisible(false);
	      	}
	    });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel2.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }    
}
