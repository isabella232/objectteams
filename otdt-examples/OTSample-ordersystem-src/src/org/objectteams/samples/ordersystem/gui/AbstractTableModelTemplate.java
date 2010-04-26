/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AbstractTableModelTemplate.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.objectteams.samples.ordersystem.gui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;


@SuppressWarnings("serial")
public abstract class AbstractTableModelTemplate<T> extends AbstractTableModel {
    
    protected abstract String[] getColumnNames();

    public abstract Vector<T> getElements();
    
    public abstract Object getValueAt(int aRowIndex, int aColumnIndex);
    
    /**
     * @param aRowIndex an index
     * @return the element (row) at given index
     */
    public Object getElementAt(int aRowIndex) {
        if (aRowIndex >= 0) {
            return getElements().elementAt(aRowIndex);
        }
        else {
            return null;
        }
    }
    
    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return getElements().size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return getColumnNames().length;
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int aColumn) {
        return getColumnNames()[aColumn];
    }


}
