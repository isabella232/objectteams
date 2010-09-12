/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MonitorView.java 23462 2010-02-04 22:13:22Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.eclipse.monitor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.objectteams.Team;

// Copy&Paste: the GUI-stuff is partly inspired by the AboutPluginsDialog.
public class MonitorView extends ViewPart {
	
	private static final String IMG_AUTO_REFRESH = "icons/auto_refresh.png"; //$NON-NLS-1$
	private static final String IMG_REFRESH = "icons/refresh_nav.gif"; //$NON-NLS-1$
	private static final String IMG_REFRESH_DISABLED = "icons/refresh_nav_dis.gif"; //$NON-NLS-1$
	
	/** The main widget: */
	private Table table;
	// actions:
	private Action refreshAction;
	private Action toggleAutoRefreshAction;
	private boolean autoRefresh = true;

	/** Stored texts for tooltips: */
	private HashMap<Widget, String> hoverTexts = new HashMap<Widget, String>();

	/**
	 * Setup the GUI.
	 */
	public void createPartControl(Composite parent) {
		Activator.registerMonitorView(this);

		table = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setFont(parent.getFont());
        

        // create table headers
        String[] columnTitles = { MonitorMessages.heading_team_class, MonitorMessages.heading_num_roles };
        int   [] columnWidths = { 500,          50        };
        for (int i = 0; i < columnTitles.length; i++) {
            TableColumn column = new TableColumn(table, SWT.NULL);
            column.setWidth(columnWidths[i]);
            column.setText(columnTitles[i]);
        }
        
        readTeams();
        
        table.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		printDetails(e.item);
        	}
        });
        table.addMouseTrackListener(new MouseTrackAdapter() {
        	@Override
        	public void mouseHover(MouseEvent e) {
        		TableItem item = table.getItem(new Point(e.x, e.y));
        		String hoverText = hoverTexts.get(item);
        		if (hoverText != null)
        			table.setToolTipText(hoverText);
        		else
        			table.setToolTipText(MonitorMessages.tooltip_not_computed);
        	}
        });

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gridData);

        final Display display = parent.getDisplay();
        makeActions(display);      
		contributeToActionBars();
	}

	private void makeActions(final Display display) {
		refreshAction = new Action() {
			public void run() {
				refreshAll();
			}
		};
		refreshAction.setText(MonitorMessages.button_text_refresh);
		refreshAction.setToolTipText(MonitorMessages.button_tooltip_refresh);
		refreshAction.setDisabledImageDescriptor(Activator.getImageDescriptor(IMG_REFRESH_DISABLED));
		refreshAction.setImageDescriptor(Activator.getImageDescriptor(IMG_REFRESH));
		
		toggleAutoRefreshAction = new Action() {
			public void run() {
				autoRefresh = !autoRefresh;
				if (autoRefresh)
					startAutoRefreshJob(display);					
			}
		};
		toggleAutoRefreshAction.setChecked(true); // make this a checkbox.
		toggleAutoRefreshAction.setText(MonitorMessages.button_text_auto_refresh);
		toggleAutoRefreshAction.setToolTipText(MonitorMessages.button_tooltip_auto_refresh);
		toggleAutoRefreshAction.setImageDescriptor(Activator.getImageDescriptor(IMG_AUTO_REFRESH));
		
        startAutoRefreshJob(display);  // on by default.
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(refreshAction);
		bars.getToolBarManager().add(toggleAutoRefreshAction);
	}
	
	/**
	 * Start a background job that will call back into the UI every second for refresh.
	 */
	void startAutoRefreshJob(final Display display) {
		Job job = new Job(MonitorMessages.job_name_refresh) {
			protected IStatus run(IProgressMonitor monitor) {
				// need asyncExec to call GUI code from a non-GUI thread:
				display.asyncExec(new Runnable() {
					public void run() {
						refreshTableData();
					}
				});        		
				if (autoRefresh)
					this.schedule(1000); // keep going.
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule(1000);
	}

	public void stop() {
		this.autoRefresh = false;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		table.setFocus();
	}

	// ====  contents computations only below: ====
	
	void readTeams() {
		// fetch instantiated teams:
		List<Team> teamList = new ArrayList<Team>();
		TransformerPlugin.getTeamInstances(teamList);
	
		// create rows and fill with data:
	    for (int i = 0; i < teamList.size(); ++i) {
	    	Team      aTeam = teamList.get(i);
	        TableItem item  = new TableItem(table, SWT.NULL);
	        item.setData(aTeam);
	        item.setChecked(aTeam.isActive(Team.ALL_THREADS));
	        setupData(item);
	    }
	}
	
	void refreshAll() {
		this.table.removeAll();
		readTeams();
		refreshTableData();
	}

	/** Set the texts from the contained Team instance. */
	private boolean setupData(TableItem item) {
		Team aTeam = (Team)item.getData();
		item.setText(0, aTeam.getClass().getName());
		String oldLength = item.getText(1);
		String newLength = String.valueOf(aTeam.getAllRoles().length);
		item.setText(1, newLength);
		return oldLength == null || !oldLength.equals(newLength);
	}
	
	/** re-read the role statistics from all known teams 
	 */
	void refreshTableData() {
		if (table.isDisposed()) {
			autoRefresh = false;
			return;
		}			
		for(TableItem item : table.getItems()) {
			if (setupData(item))
				hoverTexts.remove(item);
		}
	}
	
	/** Print to std out details about the selected team:
	 *  How many roles of which class are registered?
	 *  Also store these infos for display in tooltips.
	 */
	void printDetails(Widget item) {
		StringBuilder buf = new StringBuilder();
		
		Team   aTeam    = (Team)item.getData();
		String teamName = aTeam.getClass().getName();
		buf.append(MonitorMessages.tooltip_roles_of+teamName);
		
		Object[] allRoles = aTeam.getAllRoles();
		if (allRoles.length == 0) {
			buf.append('\n');
			buf.append(MonitorMessages.tooltip_no_roles);
		} else {
			// sort roles by class:
			HashMap<Class<?>, Integer> stats = new HashMap<Class<?>, Integer>();
			for (Object aRole : allRoles) {
				Integer i =  stats.get(aRole.getClass());
				if (i != null)
					i = i+1;
				else 
					i = 1;
				stats.put(aRole.getClass(), i);
			}
			// append stats per class:
			for (Entry<Class<?>, Integer> stat: stats.entrySet()) {
				int prefixLen = teamName.length() + "$__OT__".length(); // single name for roles //$NON-NLS-1$
				String roleName = stat.getKey().getName().substring(prefixLen);
				buf.append("\n"+stat.getValue()+"\t"+roleName); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		System.out.println(buf.toString());
		hoverTexts.put(item, buf.toString());
		
		boolean checked = ((TableItem)item).getChecked();
		if (checked !=  aTeam.isActive(Team.ALL_THREADS)) {
			if (checked)
				aTeam.activate(Team.ALL_THREADS);
			else
				aTeam.deactivate(Team.ALL_THREADS);
		}
	}
}