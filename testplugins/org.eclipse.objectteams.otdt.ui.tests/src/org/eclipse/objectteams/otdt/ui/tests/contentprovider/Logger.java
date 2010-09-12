/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Logger.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.contentprovider;

/**
 * @author kaschja
 * @version $Id: Logger.java 23495 2010-02-05 23:15:16Z stephan $
 */
public class Logger
{
    public static void println(Object obj)
    {
        if (obj instanceof String)
        {
            System.out.println(obj);
        }
        else
        {
            System.out.println(obj.toString());
        }
    }
    
//    public static void println(String text)
//    {
//        System.out.println(text);
//    }
    
    public static void println(int number)
    {
        System.out.println(number);
    }    
}
