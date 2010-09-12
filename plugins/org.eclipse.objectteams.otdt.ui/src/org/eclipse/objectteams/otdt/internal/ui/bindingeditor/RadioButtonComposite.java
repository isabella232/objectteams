/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RadioButtonComposite.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.bindingeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This composite imitates the radiobutton-behavior.
 * All children have to be buttons(toggleButtons recommended).
 * 
 * Created on Mar 21, 2005
 * 
 * @author ike
 * @version $Id: RadioButtonComposite.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class RadioButtonComposite extends Composite
{
	
	Button _oldSelectedButton;
	
	public RadioButtonComposite(Composite parent, int style)
	{
		super(parent, style);
	}
	
	public void doRadioButtonBehavior(Button selectedButton)
	{

		if (_oldSelectedButton != null && _oldSelectedButton.equals(selectedButton))
		{
			selectedButton.setSelection(true);
			_oldSelectedButton = selectedButton; 
			return;
		}

		Button [] radioButtons = getButtons();
		for (int idx = 0; idx < radioButtons.length; idx++)
		{
			if (!radioButtons[idx].equals(selectedButton))
			{
				radioButtons[idx].setSelection(false);
			}
		}
		
		_oldSelectedButton = selectedButton; 
	}
	
	public Button getSelectedButton()
	{
		Button [] radioButtons = getButtons();
		for (int idx = 0; idx < radioButtons.length; idx++)
		{
			if (radioButtons[idx].getSelection())
				return radioButtons[idx];
		}
		
		return null;
	}
	
	public void setSelectionButton(Button button)
	{
		Button [] radioButtons = getButtons();
		for (int idx = 0; idx < radioButtons.length; idx++)
		{
			if (radioButtons[idx].equals(button))
			{
				radioButtons[idx].setSelection(true);
				_oldSelectedButton = button;
			}
			else
			{
				radioButtons[idx].setSelection(false);
			}
		}
	}
	
	public void removeSelectionButton(Button button)
	{
		Button [] radioButtons = getButtons();
		for (int idx = 0; idx < radioButtons.length; idx++)
		{
			if (radioButtons[idx].equals(button) && radioButtons[idx].getSelection())
			{
				radioButtons[idx].setSelection(false);
				_oldSelectedButton = null;
			}
		}
	}
	
	public void deselectAll()
	{
		Button [] radioButtons = getButtons();
		for (int idx = 0; idx < radioButtons.length; idx++)
		{
			if(radioButtons[idx].getSelection())
				radioButtons[idx].setSelection(false);
		}
	}
	
	public void enableAll()
	{
		Button [] radioButtons = getButtons();
		for (int idx = 0; idx < radioButtons.length; idx++)
		{
			if(!radioButtons[idx].isEnabled())
				radioButtons[idx].setEnabled(true);
		}
	}
	
	public void disableAll()
	{
		Button [] radioButtons = getButtons();
		for (int idx = 0; idx < radioButtons.length; idx++)
		{
			if(radioButtons[idx].isEnabled())
				radioButtons[idx].setEnabled(false);
		}
	}

	private Button[] getButtons()
	{
		Control [] controls = this.getChildren();
		List<Button> buttons = new ArrayList<Button>();
		for (int idx = 0; idx < controls.length; idx++)
		{
			if (controls[idx] instanceof Button)
				buttons.add((Button)controls[idx]);
		}
		return buttons.toArray(new Button[buttons.size()]);
	}
}
