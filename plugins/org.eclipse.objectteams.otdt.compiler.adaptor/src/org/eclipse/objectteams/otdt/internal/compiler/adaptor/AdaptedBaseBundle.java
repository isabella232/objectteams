/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AdaptedBaseBundle.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Objects of this class encode the information about aspectBindings
 * affecting a given type/package within a base bundle.
 * 
 * @author stephan
 * @since 1.1.5
 */
public class AdaptedBaseBundle 
{
	// the Object-value is used as a time stamp for reloading after the reader has been reloaded:
	private HashMap<AspectBindingReader,Object> readers= new HashMap<AspectBindingReader, Object>();
	
	
	/** Symbolic name of this base bundle. */
	String symbolicName;
	/** Teams adapting this base bundle. */
	Set<String> adaptingTeams;
	
	/** Does this aspect bundle have one or more split packages? */
	public boolean hasPackageSplit = false; 

	public AdaptedBaseBundle(String symbolicName,
							 AspectBindingReader reader)
	{
		this.symbolicName = symbolicName;
		this.adaptingTeams= reader.getTeamsForBase(symbolicName);			
		this.readers.put(reader, reader.token);
	}
	public boolean isAdaptedBy(String teamName) {
		checkReload();
		for (String aTeam : adaptingTeams)
			if (aTeam.equals(teamName))
				return true;
		return false;
	}
	
	public String getSymbolicName() {
		return this.symbolicName;
	}
	
	public synchronized boolean merge(AdaptedBaseBundle otherData) {
		if (this.symbolicName.equals(otherData.symbolicName)) {
			this.adaptingTeams.addAll(otherData.adaptingTeams);
			this.readers.putAll(otherData.readers);
			return true;
		} else {
			// different base bundle but same package: split package
			return false;
		}
	}
	
	/** Check whether the AspectBindingReader has been reloaded, and if so,
	 *  also reload our data (adaptingTeams) from the reader. */
	private void checkReload() {
		synchronized (this.readers) {
			boolean reloadNeeded= false;
			for (Map.Entry<AspectBindingReader,Object> readerEntry : this.readers.entrySet()) {
				AspectBindingReader reader= readerEntry.getKey();
				if (readerEntry.getValue() != reader.token) {
					// token changed means: reader has reloaded.
					reloadNeeded= true;
					this.readers.put(reader, reader.token);
				}
			}
			if (!reloadNeeded)
				return;
			// perform the reload:
			HashSet<String> newSet= new HashSet<String>();
			for (AspectBindingReader reader: this.readers.keySet())
				newSet.addAll(reader.getTeamsForBase(this.symbolicName));
			this.adaptingTeams= newSet; // only now assign, because querying the readers might call back into this!
		}
	}
	
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		String result= "bundle "+this.symbolicName+" is adapted by";
		if (this.adaptingTeams == null)
			return result+" (null)";
		if (this.adaptingTeams.isEmpty())
			return result+" no teams";
		for (String aTeam : this.adaptingTeams) {
			result+= "\n\t"+aTeam;
		}
		return result;
	}
}
