package org.eclipse.objectteams.otdt.tests.otmodel.role.unbound;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

public class Test5d extends FileBasedModelTest {
	
	final static String R1 = "SampleRole1";
	final static String R2 = "SampleRole2";
	final static String INNER = "InnerRole";
	
	public Test5d(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test5d.class);
        }
        junit.framework.TestSuite suite = new Suite(Test5d.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(getTestSetting().getTestProject());
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_5d");
    }
    
    protected void setUp() throws Exception
    {
		super.setUp();
        getTestSetting().setUp();
    }
    
    public void testFlagsOfRoFi2() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());
        
        IType r2 = getTestSetting().getTeamJavaElement().getType(R2);
        int flags = r2.getFlags();
        assertEquals("Unexpected flags", Flags.AccPublic|Flags.AccTeam|Flags.AccRole, flags);
    }

    public void testFlagsOfRoFi2Nested() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());
        
        IType r2 = getTestSetting().getTeamJavaElement().getType(R2);
        IType inner = r2.getType(INNER);
        int flags = inner.getFlags();
        assertEquals("Unexpected flags", Flags.AccProtected|Flags.AccRole, flags);
    }
 
}
