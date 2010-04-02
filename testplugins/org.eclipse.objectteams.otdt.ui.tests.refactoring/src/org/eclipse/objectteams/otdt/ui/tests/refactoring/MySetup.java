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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring;

import java.util.Hashtable;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper;
import org.eclipse.objectteams.otdt.ui.tests.util.TestOptions;

public class MySetup extends TestSetup
{
    private static IPackageFragmentRoot _root;
    private static IPackageFragment _packageP;
    private static IJavaProject _javaTestProject;
    
    public static final String CONTAINER = "src";

    
    public MySetup(Test test)
    {
        super(test);
    }

    public static IPackageFragmentRoot getDefaultSourceFolder() throws Exception
    {
        if (_root != null)
        {
            return _root;
        }
        throw new Exception("MySetup not initialized");
    }

    public static IJavaProject getProject() throws Exception
    {
        if (_javaTestProject != null)
        {
            return _javaTestProject;
        }
        throw new Exception("MySetup not initialized");
    }

    public static IPackageFragment getPackageP() throws Exception
    {
        if (_packageP != null)
        {
            return _packageP;
        }
        throw new Exception("MySetup not initialized");
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        JavaProjectHelper.setAutoBuilding(false);
        if (JavaPlugin.getActivePage() != null)
        {
            JavaPlugin.getActivePage().close();
        }
        _javaTestProject = JavaProjectHelper.createOTJavaProject("TestProject"
                + System.currentTimeMillis(), "bin");
        JavaProjectHelper.addRTJar(_javaTestProject);
        _root = JavaProjectHelper.addSourceContainer(_javaTestProject,
                CONTAINER);
        _packageP = _root.createPackageFragment("p", true, null);

        Hashtable options = TestOptions.getFormatterOptions();
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
        options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        JavaCore.setOptions(options);
        TestOptions.initializeCodeGenerationOptions();
        JavaPlugin.getDefault().getCodeTemplateStore().load();

        StringBuffer comment = new StringBuffer();
        comment.append("/**\n");
        comment.append(" * ${tags}\n");
        comment.append(" */");
        StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID, comment.toString(), null);
    }

    protected void tearDown() throws Exception
    {
        if (_packageP.exists())
        {
            _packageP.delete(true, null);
        }
        JavaProjectHelper.removeSourceContainer(_javaTestProject, CONTAINER);
        JavaProjectHelper.delete(_javaTestProject);
        super.tearDown();
    }

}

