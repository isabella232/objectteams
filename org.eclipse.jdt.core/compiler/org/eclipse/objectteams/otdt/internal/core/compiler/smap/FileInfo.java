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
 * $Id: FileInfo.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/** Represents the filesection part, specified in JSR-045-Spec. Almost a copy of
 * org.eclipse.jdi.internal.ReferenceTypeImpl.FileInfo
 *
 * @author ike
 */
public class FileInfo
{
    /**
     * The id.
     */
    private int _fileId;

    /**
     * The name of the source file.
     */
    private String _fileName;

    /**
     * The path of the source file.
     */
    private String _absoluteFileName;

    /**
     * Map line number in the input source file ->
     * list of [ InputStartLine, RepeatCount, OutputStartLine, OutputLineIncrement].
     * (Integer -> List of lineInfos).
     *
     * non-null
     */
    private TreeMap <Integer,List<LineInfo>> _lineInfos;

    /**
     * FileInfo constructor.
     *
     * @param fileId
     *            the id.
     * @param fileName
     *            the name of the source file.
     * @param absoluteFileName
     *            the path of the source file (can be <code>null</code>).
     */
    public FileInfo(int fileId, String fileName, String absoluteFileName)
    {
        this._fileId = fileId;
        this._fileName = fileName;
        this._absoluteFileName = absoluteFileName;
        this._lineInfos = new TreeMap<Integer, List<LineInfo>>();
    }

    public void addLineInfo(LineInfo lineInfo)
    {
        Integer key = new Integer(lineInfo.getInputStartLine());
        List <LineInfo>lineInfosForInputStartLine = this._lineInfos.get(key);
        if (lineInfosForInputStartLine == null)
        {
        	lineInfosForInputStartLine = new ArrayList<LineInfo>();
            this._lineInfos.put(key, lineInfosForInputStartLine);
        }

        lineInfosForInputStartLine.add(lineInfo);

    }

    public void addLineInfo(List<LineInfo> lineInfos)
    {
        for (Iterator<LineInfo> iter = lineInfos.iterator(); iter.hasNext();)
            addLineInfo(iter.next());
    }

    /**
     * Return a list of line information about the code in the output source
     * file associated to the given line in the input source file.
     *
     * @param intputStartLine
     *            the line number in the input source file.
     * @return a List lineInfos.
     */
    public List<LineInfo> getLinesForInputStartLine(int intputStartLine)
    {
       return this._lineInfos.get(new Integer(intputStartLine));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        if (!(object instanceof FileInfo))
        {
            return false;
        }

        FileInfo fileInfo = (FileInfo)object;

        if (this._fileId != fileInfo._fileId)
        {
            return false;
        }

        if (this._absoluteFileName != null)
        {
            if (!this._absoluteFileName.equals(fileInfo._absoluteFileName))
            {
                return false;
            }
        }

        if (!this._fileName.equals(fileInfo._fileName))
        {
            return false;
        }

        TreeMap<Integer,List<LineInfo>> map = fileInfo._lineInfos;

        // both lineInfos are non-null
       	if (map.size() != this._lineInfos.size())
       		return false;


        for (Iterator<Integer> iter = this._lineInfos.keySet().iterator(); iter.hasNext();)
        {
            Integer key =  iter.next();
            List<LineInfo> value = this._lineInfos.get(key);

            List<LineInfo> mapValue = map.get(key);

            if ((value == null) != (mapValue == null)) {
            	return false; // null vs. non-null
            } else if (value == null || mapValue == null) { // second check is redundant, helps null-analysis below
                return true;  // both null
            }

            if (value.size() != mapValue.size())
            {
                return false;
            }

            for (int idx = 0; idx < value.size(); idx++)
            {
            	LineInfo listEntries1 = value.get(idx);
            	LineInfo listEntries2 = mapValue.get(idx);

                if (!listEntries1.equals(listEntries2))
                {
                    return false;
                }
            }
        }

        return true;
    }

    @SuppressWarnings("nls")
	public String getFileInfoDataAsString()
    {
        StringBuffer out = new StringBuffer();
        for (Iterator<Integer> iter = this._lineInfos.keySet().iterator(); iter.hasNext();)
        {
            Integer inputStartLine = iter.next();
            List<LineInfo> lineInfos = this._lineInfos.get(inputStartLine);

            for (int idx = 0; idx < lineInfos.size(); idx++)
            {
                LineInfo singleLineInfo = lineInfos.get(idx);

                out.append(inputStartLine + "#" + this._fileId);

                if (singleLineInfo.hasRepeatCount())
                {
                    out.append(",");
                    out.append(singleLineInfo.getRepeatCount());
                }

                out.append(":");
                out.append(singleLineInfo.getOutputStartLine());

                if (singleLineInfo.hasOutputLineIncrement())
                	out.append("," + singleLineInfo.getOutputLineIncrement());

                out.append("\n");
            }
        }

        return out.toString();
    }

    public String toString()
    {
        return getFileInfoDataAsString();
    }

    public int getFileId()
    {
        return this._fileId;
    }

    public String getFileName()
    {
        return this._fileName;
    }

    public String getAbsoluteFileName()
    {
        return this._absoluteFileName;
    }

    public boolean hasLineInfos()
    {
    	if ( this._lineInfos.isEmpty())
    		return false;

    	return true;
    }

    public void optimizeLineInfos()
    {
    	TreeMap<Integer, List<LineInfo>> optimizedLineInfos = new TreeMap<Integer, List<LineInfo>>();

    	for (Iterator<Integer> iter = this._lineInfos.keySet().iterator(); iter.hasNext();)
    	{
    		Integer key = iter.next();
			List <LineInfo> listofLineInfos =  this._lineInfos.get(key);
			if (listofLineInfos.size() > 1)
			{
				List <LineInfo> optimizedSimliarLineInfos = optimizeListOfSimilarLineInfos(listofLineInfos);
				this._lineInfos.put(key, optimizedSimliarLineInfos);
			}
		}

    	Integer firstKey = this._lineInfos.firstKey();
    	List <LineInfo> listofFirstLineInfos =  cloneLineInfos(this._lineInfos.get(firstKey));
		LineInfo firstLineInfo = listofFirstLineInfos.get(0);

		optimizedLineInfos.put(firstKey, listofFirstLineInfos);

		Integer[] keys = this._lineInfos.keySet().toArray(new Integer[this._lineInfos.size()]);
		for (int idx = 1; idx < keys.length; idx++)
    	{
    		Integer iterKey1 = keys[idx];
			List <LineInfo> iterKey1ListofLineInfos =  this._lineInfos.get(iterKey1);
			LineInfo iterKey1LineInfo = iterKey1ListofLineInfos.get(0);

			Integer iterKey2 = keys[idx-1];
			List <LineInfo> iterKey2ListofLineInfos =  this._lineInfos.get(iterKey2);
			LineInfo iterKey2LineInfo = iterKey2ListofLineInfos.get(0);

			if(iterKey1LineInfo.isPreviousLineInfo(firstLineInfo))
			{
				int oldRepeatCount = firstLineInfo.getRepeatCount();
				int newRepeatCount = oldRepeatCount == -1 ? 2 : oldRepeatCount +1;
				firstLineInfo.setRepeatCount(newRepeatCount);
			}
			else
			{
				if (iterKey1LineInfo.isPreviousLineInfo(iterKey2LineInfo))
				{
					int oldRepeatCount = firstLineInfo.getRepeatCount();
					int newRepeatCount = oldRepeatCount == -1 ? 1 : oldRepeatCount + 1;
					firstLineInfo.setRepeatCount(newRepeatCount);
				}
				else
				{
					firstKey = iterKey1;
					listofFirstLineInfos =  cloneLineInfos(this._lineInfos.get(firstKey));
					firstLineInfo = listofFirstLineInfos.get(0);

					optimizedLineInfos.put(firstKey, listofFirstLineInfos);
				}
			}
    	}

    	this._lineInfos = optimizedLineInfos;
    }

    private List<LineInfo> cloneLineInfos(List<LineInfo> listofLineInfos)
    {
    	List <LineInfo> clonedList = new ArrayList<LineInfo>();
    	for (int idx = 0; idx < listofLineInfos.size(); idx++)
    	{
    		LineInfo lineInfo = listofLineInfos.get(idx);
    		LineInfo copiedLineInfo = lineInfo.clone();
    		clonedList.add(copiedLineInfo);
		}

    	return clonedList;
	}

	/** Filter doubled entries in smap.
     *
     */
	private List<LineInfo> optimizeListOfSimilarLineInfos(List<LineInfo> listofLineInfos)
	{
		List<LineInfo> optimizedListOfSimilarLineInfos = new ArrayList<LineInfo>();

		LineInfo lineInfo = listofLineInfos.get(0);
		optimizedListOfSimilarLineInfos.add(lineInfo);

		for (Iterator<LineInfo> iter = listofLineInfos.iterator(); iter.hasNext();)
		{
			LineInfo tmpLineInfo = iter.next();

			if (!lineInfo.equals(tmpLineInfo) && !optimizedListOfSimilarLineInfos.contains(tmpLineInfo))
				optimizedListOfSimilarLineInfos.add(tmpLineInfo);
		}

		return optimizedListOfSimilarLineInfos;
	}

}