/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: StringLinkedModeProposal.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Simple string-based alternative for an element of a group of linked positions.
 * 
 * @author stephan
 *
 */
@SuppressWarnings("restriction")
class StringLinkedModeProposal extends LinkedProposalPositionGroup.Proposal
{

	private LinkedPositionGroup fLinkedPositionGroup;
	private final String val;
	private final String description;

	public StringLinkedModeProposal(String val) {
		this(val, null);
	}

	public StringLinkedModeProposal(String val, String description) {
		super(val, null, 0);
		this.val = val;
		this.description = description;
	}
	
	public void setLinkedPositionGroup(LinkedPositionGroup group) {
		fLinkedPositionGroup= group;
	}

	public String getAdditionalProposalInfo() {
		return description == null ? getDisplayString() : description;
	}

	public String getDisplayString() {
		return val;
	}

	public Image getImage() {
		return null;
	}

	private Position getCurrentPosition(int offset) {
		if (fLinkedPositionGroup != null) {
			LinkedPosition[] positions= fLinkedPositionGroup.getPositions();
			for (int i= 0; i < positions.length; i++) {
				Position position= positions[i];
				if (position.overlapsWith(offset, 0)) {
					return position;
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
	 */
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		Position currentPosition= getCurrentPosition(offset);
		if (currentPosition == null)
			return;
		try {
			IDocument document= viewer.getDocument();

			// first replace range then insert space (insert space can destroy empty position)
			document.replace(currentPosition.offset, currentPosition.length, this.val);
			
		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}
	}

	public int getRelevance()                          			  { return 0; 	}
	public IContextInformation getContextInformation()			  { return null; }
	public void apply(IDocument document) 			   			  { /* not called */ } 
	public Point getSelection(IDocument document) 				  { return null; }
	public void selected(ITextViewer viewer, boolean smartToggle) { /* nop */ }
	public void unselected(ITextViewer viewer) 					  { /* nop */ }
	public boolean validate(IDocument document, int offset, DocumentEvent event) { return false; }
}