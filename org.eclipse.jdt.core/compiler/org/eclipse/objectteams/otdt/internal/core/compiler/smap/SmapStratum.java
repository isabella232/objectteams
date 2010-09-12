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
 * $Id: SmapStratum.java 23417 2010-02-03 20:13:55Z stephan $
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

/** Represents a Source Map (SMAP), specified in JSR-045-Spec.
 *
 * @author ike
 */
@SuppressWarnings("nls")
public class SmapStratum
{

    private String _stratumName;
    private List <FileInfo>_fileInfos;
    private int _actualfileId;

    public SmapStratum(String stratumName)
    {
        this._stratumName = stratumName;
        this._actualfileId = 0;
        this._fileInfos = new ArrayList<FileInfo>();
    }

    public String getStratumName()
    {
        return this._stratumName;
    }

    public void optimize()
    {
        for (Iterator<FileInfo> iter = this._fileInfos.iterator(); iter.hasNext();)
        {
            FileInfo fileInfo = iter.next();

            if (fileInfo.hasLineInfos())
            	fileInfo.optimizeLineInfos();
        }
    }

    public FileInfo getOrCreateFileInfo(String fileName, String absoluteFileName)
    {
        FileInfo fileInfo = getFileInfo(fileName, absoluteFileName);

        if (fileInfo != null)
            return fileInfo;

        fileInfo = new FileInfo(getNextFileId(), fileName, absoluteFileName);
        this._fileInfos.add(fileInfo);
        return fileInfo;
    }

    private FileInfo getFileInfo(String fileName, String absoluteFileName)
    {
        if (this._fileInfos.isEmpty())
            return null;

        for (Iterator<FileInfo> iter = this._fileInfos.iterator(); iter.hasNext();)
        {
            FileInfo fileInfo = iter.next();
            if (fileInfo.getAbsoluteFileName() != null && fileInfo.getAbsoluteFileName().equals(absoluteFileName))
            {
                return fileInfo;
            }
            if (fileInfo.getFileName().equals(fileName))
            {
                return fileInfo;
            }
        }
        return null;
    }

    private int getNextFileId()
    {
        this._actualfileId++;
        return this._actualfileId;
    }

    public List<FileInfo> getFileInfos()
    {
        return this._fileInfos;
    }

    public String getSmapAsString()
    {

        StringBuffer out = new StringBuffer();

        // begin StratumSection
        out.append("*S " + this._stratumName + "\n");

        // print FileSection
        out.append("*F" + "\n");
        for(int idx = 0; idx < this._fileInfos.size(); idx++)
        {
            FileInfo fileInfo = this._fileInfos.get(idx);

            if (fileInfo.getAbsoluteFileName() != null)
                out.append("+ " );

            out.append(fileInfo.getFileId() + " " + fileInfo.getFileName() + "\n");

            if (fileInfo.getAbsoluteFileName() != null)
                out.append(fileInfo.getAbsoluteFileName() + "\n");
        }

        // print LineSection
        out.append("*L" + "\n");
        for(int idx = 0; idx < this._fileInfos.size(); idx++)
        {
            FileInfo fileInfo = this._fileInfos.get(idx);
            out.append(fileInfo.getFileInfoDataAsString());
        }

        return out.toString();
    }

    public String toString()
    {
        return getSmapAsString();
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof SmapStratum))
        {
            return false;
        }

        SmapStratum stratum = (SmapStratum)obj;

        if (!getStratumName().equals(stratum.getStratumName()))
            return false;

        if (this._fileInfos.size() != stratum.getFileInfos().size())
            return false;

        if (! this._fileInfos.equals(stratum.getFileInfos()))
            return false;

        return true;
    }

    public boolean hasFileInfos()
    {
        return (this._fileInfos != null || this._fileInfos.size() > 0);
    }
}
