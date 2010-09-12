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

/** Represents a linesection part, specified in JSR-045-Spec.
 * * @author ike */
public class LineInfo
{
    private int _inputStartLine;
    private int _repeatCount;
    private int _outputStartLine;
    private int _outputLineIncrement;

	public LineInfo(int inputStartLine, int outputStartLine)
	{
	    if (inputStartLine < 0)
	        throw new IllegalArgumentException("Linenumber has to be greater than 0, not " + inputStartLine); //$NON-NLS-1$

	    if (outputStartLine < 0)
	        throw new IllegalArgumentException("Linenumber has to be greater than 0, not " + outputStartLine); //$NON-NLS-1$

	    this._inputStartLine = inputStartLine;
	    this._repeatCount = -1;
	    this._outputStartLine = outputStartLine;
	    this._outputLineIncrement = -1;
	}

	public void setRepeatCount(int repeatCount)
	{
	    if (repeatCount < 0)
        throw new IllegalArgumentException("RepeatCount has to be greater than 0, not " + repeatCount); //$NON-NLS-1$

	    this._repeatCount = repeatCount;
	}

	public void setOutputLineIncrement(int outputLineIncrement)
	{
	    if (outputLineIncrement < -1)
	        throw new IllegalArgumentException("Increment has to be greater than -1, not " + outputLineIncrement); //$NON-NLS-1$

	    this._outputLineIncrement = outputLineIncrement;
	}

    public int getInputStartLine()
    {
        return this._inputStartLine;
    }

    public int getOutputStartLine()
    {
    	return this._outputStartLine;
    }

    public int getOutputLineIncrement()
    {
    	return this._outputLineIncrement;
    }

    public boolean hasOutputLineIncrement()
    {
        return this._outputLineIncrement != -1;
    }

    public boolean hasRepeatCount()
    {
        return this._repeatCount > 1; // don't report useless repeat count of 1
    }

    public int getRepeatCount()
    {
        return this._repeatCount;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof LineInfo))
        {
            return false;
        }

        LineInfo lineInfo = (LineInfo)obj;

        if (this._inputStartLine != lineInfo._inputStartLine)
        {
            return false;
        }

        if (this._outputStartLine != lineInfo._outputStartLine)
        {
            return false;
        }

        if (hasRepeatCount() != lineInfo.hasRepeatCount())
        {
            return false;
        }

        if (getRepeatCount() != lineInfo.getRepeatCount())
        {
            return false;
        }

        if (getOutputStartLine() != lineInfo.getOutputStartLine())
        {
        	return false;
        }

        if (hasOutputLineIncrement() != lineInfo.hasOutputLineIncrement())
        {
        	return false;
        }

        if (getOutputLineIncrement() != lineInfo.getOutputLineIncrement())
        {
        	return false;
        }

        return true;
    }

	public boolean isPreviousLineInfo(LineInfo previousLineInfo)
	{

		if ((this._inputStartLine == (previousLineInfo.getInputStartLine() + 1)) &&
			(this._outputStartLine == (previousLineInfo.getOutputStartLine() + 1)) &&
			!hasRepeatCount() && !previousLineInfo.hasRepeatCount() &&
			!hasOutputLineIncrement() && !previousLineInfo.hasOutputLineIncrement())
		{
			return true;
		}

		if (this._inputStartLine  <=  (previousLineInfo.getInputStartLine() * previousLineInfo.getRepeatCount()) &&
		   (this._outputStartLine == (previousLineInfo.getOutputStartLine() + 1)))
		{
			return true;
		}


		return false;
	}

	public LineInfo clone()
	{
		LineInfo copiedLineInfo = new LineInfo(this._inputStartLine, this._outputStartLine);

		if (hasOutputLineIncrement())
			copiedLineInfo.setOutputLineIncrement(this._outputLineIncrement);

		if (hasRepeatCount())
			copiedLineInfo.setRepeatCount(this._repeatCount);

		return copiedLineInfo;
	}
}
