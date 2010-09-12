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
package org.eclipse.objectteams.otdt.ui.tests.core;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper;
import org.eclipse.objectteams.otdt.ui.tests.util.TestOptions;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This class contains tests that have been copied from the original class
 * <code>org.eclipse.jdt.ui.tests.core.ImportOrganizeTest</code> in the test suite
 * <code>org.eclipse.jdt.ui.tests.refactoring</code> provided by Eclipse as
 * well as new OT-specific tests.
 * 
 * @author brcan
 */
@SuppressWarnings("restriction")
public class OrganizeImportsTest extends TestCase
{
    private IJavaProject _project;

    public OrganizeImportsTest(String name)
    {
        super(name);
    }

    public static Test allTests()
    {
        return new ProjectTestSetup(new TestSuite(OrganizeImportsTest.class));
    }

    public static Test suite()
    {
        if (true)
        {
            return allTests();
        }
        else
        {
            TestSuite suite = new TestSuite();
            suite.addTest(new OrganizeImportsTest("testVisibility_bug56704"));
            return new ProjectTestSetup(suite);
        }
    }

    @SuppressWarnings("unchecked")
	protected void setUp() throws Exception
    {
        _project = ProjectTestSetup.getProject();

        Hashtable options = TestOptions.getFormatterOptions();
        options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE,
                String.valueOf(99));
        JavaCore.setOptions(options);
    }

    protected void tearDown() throws Exception
    {
        JavaProjectHelper.clear(_project, ProjectTestSetup.getDefaultClasspath());
        OTREContainer.initializeOTJProject(_project.getProject());
    }

    private IChooseImportQuery createQuery(
            final String name,
            final String[] choices,
            final int[] nEntries)
    {
        return new IChooseImportQuery() {
            public TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices, ISourceRange[] ranges)
            {
                assertTrue(
                        name + "-query-nchoices1",
                        choices.length == openChoices.length);
                assertTrue(
                        name + "-query-nchoices2",
                        nEntries.length == openChoices.length);
                if (nEntries != null)
                {
                    for (int i = 0; i < nEntries.length; i++)
                    {
                        assertTrue(
                                name + "-query-cnt" + i,
                                openChoices[i].length == nEntries[i]);
                    }
                }
                TypeNameMatch[] res = new TypeNameMatch[openChoices.length];
                for (int i = 0; i < openChoices.length; i++)
                {
                    TypeNameMatch[] selection = openChoices[i];
                    assertNotNull(name + "-query-setset" + i, selection);
                    assertTrue(name + "-query-setlen" + i, selection.length > 0);
                    TypeNameMatch found = null;
                    for (int k = 0; k < selection.length; k++)
                    {
                        if (selection[k].getFullyQualifiedName().equals(choices[i]))
                        {
                            found = selection[k];
                        }
                    }
                    assertNotNull(name + "-query-notfound" + i, found);
                    res[i] = found;
                }
                return res;
            }
        };
    }
    
    public static void assertEqualString(String actual, String expected)
    {
        StringAsserts.assertEqualString(actual, expected);
    }

    //OT-specific tests
    public void testTypeReferenceInRoleclass1() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);

        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }

    public void testTypeReferenceInRoleclass2() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);

        buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B2 {\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B2.java", buf.toString(), false, null);

        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("    }\n");
        buf.append("    public class R2 playedBy B2 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("import base basePkg.B2;\n");
        buf.append("\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("    }\n");
        buf.append("    public class R2 playedBy B2 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }

    public void testTypeReferenceInRoleclass3() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);

        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 {\n");
        buf.append("        callin void rm1(B1 b1) {\n");
        buf.append("            base.rm1(b1);\n");
        buf.append("        }\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("\n");
        buf.append("import basePkg.B1;\n");
        buf.append("\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 {\n");
        buf.append("        callin void rm1(B1 b1) {\n");
        buf.append("            base.rm1(b1);\n");
        buf.append("        }\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }

    public void testTypeReferenceInRoleclass4() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("    public void setBase(B1 b1) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);

        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("        void setBase(B1 b1) -> void setBase(B1 b1);\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("        void setBase(B1 b1) -> void setBase(B1 b1);\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }

    public void testTypeReferenceInRoleclass5() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public team class T2 {\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("T2.java", buf.toString(), false, null);

        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy T2 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("\n");
        buf.append("import base basePkg.T2;\n");
        buf.append("\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy T2 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }
    /** playedBy inner base class - role and base have same name. */
    public void testTypeReferenceInRoleclass6() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("    public class Inner {\n");
        buf.append("    }\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);

        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import base basePkg.B1;\n"); 
        buf.append("import base basePkg.B1.Inner;\n");
        buf.append("public team class T1 {\n");
        buf.append("    protected class Inner playedBy Inner {\n");
        buf.append("    }\n");
        buf.append("    protected class R0 playedBy B1 {\n");
        buf.append("        Inner other;\n");
        buf.append("    };\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import base basePkg.B1;\n"); 
        buf.append("import base basePkg.B1.Inner;\n");
        buf.append("public team class T1 {\n");
        buf.append("    protected class Inner playedBy Inner {\n");
        buf.append("    }\n");
        buf.append("    protected class R0 playedBy B1 {\n");
        buf.append("        Inner other;\n");
        buf.append("    };\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // base import for role file
    public void testTypeReferenceInRoleclass7() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);

        IPackageFragment aspectPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("public team class T1 {\n");
        buf.append("}\n");
        ICompilationUnit cu = aspectPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        // create the role file:
        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg.T1",
                false,
                null);
        buf = new StringBuffer();
        buf.append("team package teamPkg.T1;\n");
        buf.append("public class R1 playedBy B1 {\n");
        buf.append("}\n");
        teamPkg.createCompilationUnit("R1.java", buf.toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("\n");
        buf.append("public team class T1 {\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }
  
    public void testCalloutToStatic() throws Exception
    {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("   private static void testB1() { }");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);

        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("        abstract static void test();");
        buf.append("    	void test() -> void testB1();\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("        abstract static void test();");
        buf.append("    	void test() -> void testB1();\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());

        // invoke a second time to produce https://svn.objectteams.org/trac/ot/ticket/2
        op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);
        assertEqualString(cu.getSource(), buf.toString());
    }

    public void testStaticImportInGuard1() throws CoreException, BackingStoreException {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("   private static void testB1() { }");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);
        
        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import static java.lang.Math.max;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 when (max(1,2)==2) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import static java.lang.Math.max;\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 when (max(1,2)==2) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }

    public void testStaticImportInGuard2() throws CoreException, BackingStoreException {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("   void testB1() { }");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);
        
        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import static java.lang.Math.max;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("       void rm() { }\n");
        buf.append("       rm <- after test\n");
        buf.append("	      base when (max(1,2)==2);\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import static java.lang.Math.max;\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R1 playedBy B1 {\n");
        buf.append("       void rm() { }\n");
        buf.append("       rm <- after test\n");
        buf.append("	      base when (max(1,2)==2);\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // Trac 19: organize imports must preserve a base import,
    // even if role and base have the same name and the role
    // is also referenced from a sibling role.
    public void testRoleHidesBase1() throws CoreException, BackingStoreException {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper
                .addSourceContainer(_project, "src");

        IPackageFragment basePkg = sourceFolder.createPackageFragment(
                "basePkg",
                false,
                null);
        StringBuffer buf = new StringBuffer();
        buf.append("package basePkg;\n");
        buf.append("public class B1 {\n");
        buf.append("}\n");
        basePkg.createCompilationUnit("B1.java", buf.toString(), false, null);
        
        IPackageFragment teamPkg = sourceFolder.createPackageFragment(
                "teamPkg",
                false,
                null);
        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R2 {\n");
        buf.append("        B1 other;\n");
        buf.append("    }\n");
        buf.append("    public class B1 playedBy B1 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
                .toString(), false, null);

        String[] order = new String[0];
        IChooseImportQuery query = createQuery(
                "T1",
                new String[] {},
                new int[] {});

        OrganizeImportsOperation op = createOperation(cu, order,
                99, false, true, true, query);
        op.run(null);

        buf = new StringBuffer();
        buf.append("package teamPkg;\n");
        buf.append("import base basePkg.B1;\n");
        buf.append("public team class T1 {\n");
        buf.append("    public class R2 {\n");
        buf.append("        B1 other;\n");
        buf.append("    }\n");
        buf.append("    public class B1 playedBy B1 {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(cu.getSource(), buf.toString());
    }

    public void testImportRole() throws CoreException, BackingStoreException {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper.addSourceContainer(_project, "src");

		IPackageFragment basePkg = sourceFolder.createPackageFragment(
		        "basePkg",
		        false,
		        null);
		StringBuffer buf = new StringBuffer();
		buf.append("package basePkg;\n");
		buf.append("public team class BaseTeam {\n");
		buf.append("    public class BaseRole {\n");
		buf.append("    }\n");
		buf.append("}\n");
		basePkg.createCompilationUnit("BaseTeam.java", buf.toString(), false, null);
		
		IPackageFragment teamPkg = sourceFolder.createPackageFragment(
		        "teamPkg",
		        false,
		        null);
		buf = new StringBuffer();
		buf.append("package teamPkg;\n");
		buf.append("import basePkg.BaseTeam;\n");
		buf.append("public team class T1 {\n");
		buf.append("    BaseTeam t;\n");
		buf.append("    BaseRole r;\n");
		buf.append("}\n");
		ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
		        .toString(), false, null);
		
		String[] order = new String[0];
		IChooseImportQuery query = createQuery(
		        "T1",
		        new String[] {},
		        new int[] {});
		
		OrganizeImportsOperation op = createOperation(cu, order,
		        99, false, true, true, query);
		op.run(null);
		
		buf = new StringBuffer();
		buf.append("package teamPkg;\n");
		buf.append("import basePkg.BaseTeam;\n");
		buf.append("public team class T1 {\n");
		buf.append("    BaseTeam t;\n");
		buf.append("    BaseRole r;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
    }

    // Bug 311432 -  Inferred callouts to private static fields make OrganizeImports to import private fields
    public void testDontImportStaticField() throws CoreException, BackingStoreException {
        IPackageFragmentRoot sourceFolder = JavaProjectHelper.addSourceContainer(_project, "src");

		IPackageFragment basePkg = sourceFolder.createPackageFragment(
		        "basePkg",
		        false,
		        null);
		StringBuffer buf = new StringBuffer();
		buf.append("package basePkg;\n");
		buf.append("public class Base {\n");
		buf.append("    private static boolean field;\n");
		buf.append("    void m() {}\n");
		buf.append("}\n");
		basePkg.createCompilationUnit("Base.java", buf.toString(), false, null);
		
		IPackageFragment teamPkg = sourceFolder.createPackageFragment(
		        "teamPkg",
		        false,
		        null);
		buf = new StringBuffer();
		buf.append("package teamPkg;\n");
		buf.append("import base basePkg.Base;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy Base {\n");
		buf.append("    	m <-replace m;\n");
		buf.append("        @SuppressWarnings({ \"inferredcallout\", \"decapsulation\" })\n");
		buf.append("		callin void m(){\n");
		buf.append("			boolean v = field;\n");
		buf.append("		    System.out.println(v);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = teamPkg.createCompilationUnit("T1.java", buf
		        .toString(), false, null);
		
		String[] order = new String[0];
		IChooseImportQuery query = createQuery(
		        "T1",
		        new String[] {},
		        new int[] {});
		
		OrganizeImportsOperation op = createOperation(cu, order,
		        99, false, true, true, query);
		op.run(null);
		
		// buf remains unchanged
		assertEqualString(cu.getSource(), buf.toString());
    }
    
	private OrganizeImportsOperation createOperation(ICompilationUnit cu, String[] order, int threshold, boolean ignoreLowerCaseNames, boolean save, boolean doResolve, IChooseImportQuery chooseImportQuery) throws CoreException, BackingStoreException {
		setOrganizeImportSettings(order, threshold, threshold, cu.getJavaProject());
		return new OrganizeImportsOperation(cu, null, ignoreLowerCaseNames, save, doResolve, chooseImportQuery);
	}
	
	private  void setOrganizeImportSettings(String[] order, int threshold, int staticThreshold, IJavaProject project) throws BackingStoreException {
		IEclipsePreferences scope= new ProjectScope(project.getProject()).getNode(JavaUI.ID_PLUGIN);
		if (order == null) {
			scope.remove(PreferenceConstants.ORGIMPORTS_IMPORTORDER);
			scope.remove(PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD);
		} else {
			StringBuffer buf= new StringBuffer();
			for (int i= 0; i < order.length; i++) {
				buf.append(order[i]);
				buf.append(';');
			}
			scope.put(PreferenceConstants.ORGIMPORTS_IMPORTORDER, buf.toString());
			scope.put(PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD, String.valueOf(threshold));
		}
	}
    
}
