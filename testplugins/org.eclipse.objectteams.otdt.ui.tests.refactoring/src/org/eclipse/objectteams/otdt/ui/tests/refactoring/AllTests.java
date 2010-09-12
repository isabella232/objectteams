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


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author brcan
 * 
 * Runs all OT-specific refactoring tests and corresponding eclipse refactoring tests
 */
public class AllTests
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(
                "All Refactoring Tests");

        // util
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.util.AllTests.suite());
        
        // OTDT refactoring tests
        // rename
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.rename.RenameMethodInInterfaceTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.rename.RenamePrivateMethodTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.rename.RenameStaticMethodTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.rename.RenameVirtualMethodInClassTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.rename.RenamePrivateFieldTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.rename.RenameTypeTests.suite());
        
        // move
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.move.MoveInstanceMethodTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.move.MoveStaticMethodTests.suite());
        
        //extract
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.extractmethod.ExtractMethodTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.extractmethod.ExtractMethodRefactoringUtilTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.extractmethod.ExtractTempTests.suite());

        //copy & paste, delete (cut)
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.reorg.OTCopyToClipboardTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.reorg.OTDeleteTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.reorg.OTPasteActionTest.suite());
        
        // pull & push
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.pullup.PullUpTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.pushdown.PushDownTests.suite());
        
        // ot refactorings
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.inlinecallin.InlineCallinTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.extractcallin.ExtractCallinTests.suite());
        
        return suite;
    }
}
