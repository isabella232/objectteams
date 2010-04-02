/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;

/** This class provides linenumbers which are greater than maximal linenumber of given type.
 *  Holds also mapping form linenumber -> mappend linenumber and its origin.
 *  Used in Rolemodel.
 *
 * @author ike
 */
public class LineNumberProvider
{
    private Hashtable <ReferenceBinding, Vector<LineInfo>>_sourceToLineInfos;
    private int _sourceEndLineNumber;
    private int _currentEndLineNumber;
	private ReferenceBinding _referenceBinding;

	// ensures each foreign line number is always mapped to the same synthetic line number above max:
	HashMap<ReferenceBinding, HashMap<Integer,Integer>> remapped = new HashMap<ReferenceBinding, HashMap<Integer,Integer>>();

    public LineNumberProvider(ReferenceBinding referenceBinding, int sourceEndLineNumber)
    {
    	this._referenceBinding = referenceBinding;
        this._sourceEndLineNumber = sourceEndLineNumber;
        this._currentEndLineNumber = sourceEndLineNumber;
        this._sourceToLineInfos = new Hashtable<ReferenceBinding, Vector<LineInfo>>();
    }

    public int getRemappedLineNumberValue(ReferenceBinding copySrc, Integer line, int repeatCount)
    {
    	HashMap<Integer, Integer> srcRemapped = this.remapped.get(copySrc);
    	if (srcRemapped != null) {
    		Integer exist = srcRemapped.get(line);
    		if (exist != null)
    			return exist;
    	} else {
    		srcRemapped = new HashMap<Integer, Integer>();
    		this.remapped.put(copySrc, srcRemapped);
    	}
        int remappedLineNumber = this._currentEndLineNumber + 1;
        this._currentEndLineNumber = remappedLineNumber;
        if (repeatCount > 1)
        	this._currentEndLineNumber += (repeatCount - 1);
        srcRemapped.put(line, remappedLineNumber);

        return remappedLineNumber;
    }

    /**
     * Add a line info that maps input lines (source code) to output lines (used in byte codes LNT)
     * @param copySrc        the type being translated
     * @param inputStartLine the input line number, i.e., source position within copySrc
     * @param repeatCount    the number of lines being handled
     * @return the (perhaps mapped) line number to be used in the byte code
     */
    public LineInfo addLineInfo(ReferenceBinding copySrc, int inputStartLine, int repeatCount)
    {
    	int outputStartLine;
    	if (copySrc != this._referenceBinding && inputStartLine < ISMAPConstants.STEP_INTO_LINENUMBER)
    		// map "foreign" lines to numbers above the current file's max:
    		outputStartLine = getRemappedLineNumberValue(copySrc, inputStartLine, repeatCount);
    	else
    		outputStartLine = inputStartLine;

        LineInfo lineInfo = new LineInfo(inputStartLine, outputStartLine);

        if (repeatCount > -1)
        	lineInfo.setRepeatCount(repeatCount);
        // TODO(SH): is outputLineIncrement relevant here?

        if (!this._sourceToLineInfos.containsKey(copySrc))
            this._sourceToLineInfos.put(copySrc, new Vector<LineInfo>());

        List <LineInfo>lineInfos = this._sourceToLineInfos.get(copySrc);
        lineInfos.add(lineInfo);
        return lineInfo;
    }

    public Hashtable<ReferenceBinding, Vector<LineInfo>> getLineInfos()
    {
        return this._sourceToLineInfos;
    }

    public List<LineInfo> getLineInfosForType(Object key)
    {
        return this._sourceToLineInfos.get(key);
    }

    public int getSourceEndLineNumber()
    {
        return this._sourceEndLineNumber;
    }

    public boolean containsLineInfos()
    {
        return this._sourceToLineInfos.size() > 0;
    }

	public void setRepeatCount(LineInfo lineInfo, int count) {
		lineInfo.setRepeatCount(count);
		this._currentEndLineNumber = Math.max(this._currentEndLineNumber, lineInfo.getOutputStartLine()+count-1);
	}
}
