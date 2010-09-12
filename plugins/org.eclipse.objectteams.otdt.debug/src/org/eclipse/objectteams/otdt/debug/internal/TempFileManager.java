/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TempFileManager.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author gis
 */
public class TempFileManager
{
    private Map _tempFiles = new HashMap();

    /**
     * If you created a temporary file yourself and want TempFileManager to manage the
     * deletion, use this method to register the file with the given key. 
     * Call deleteTempFile with the same key to delete it.
     */
    public void registerTempFile(Object key, File file)
    {
        synchronized(_tempFiles) {
            _tempFiles.put(key, file);
        }
    }
    
    /**
     * Creates a temporary file via java.io.File.createTempFile and registers it for later
     * deletion via the given key. Call deleteTempFile with the same key to delete it.
     * @throws IOException
     */
    public File createTempFile(Object key, String prefix, String suffix) throws IOException
    {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        synchronized(_tempFiles) {
            _tempFiles.put(key, tempFile);
        }
        
        return tempFile;
    }

    public void deleteTempFile(Object key)
    {
        synchronized(_tempFiles) {
            File file = (File) _tempFiles.get(key);
            if (file != null)
                file.delete();
        }
    }

    public void deleteAll()
    {
        synchronized(_tempFiles) {
            for (Iterator iter = _tempFiles.values().iterator(); iter.hasNext();)
            {
                File file = (File) iter.next();
                file.delete();
            }
        }
    }
}
