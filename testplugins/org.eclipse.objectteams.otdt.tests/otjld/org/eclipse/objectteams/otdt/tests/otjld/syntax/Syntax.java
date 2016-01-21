/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 IT Service Omikron GmbH and others.
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
 * 	  Thomas Dudziak - Initial API and implementation
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.syntax;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

public class Syntax extends AbstractOTJLDTest {
	
	public Syntax(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test881_buggyRole3"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return Syntax.class;
	}

    // a class that is not a role class has a playedBy construct - scoped keywords per default
    // 8.1.1-otjld-non-roleclass-with-playedby-1
    public void test811_nonRoleclassWithPlayedby1() {
        runNegativeTestMatching(
            new String[] {
		"T811nrwp1_2.java",
			    "\n" +
			    "public class T811nrwp1_2 playedBy T811nrwp1_1 {}\n" +
			    "    \n",
		"T811nrwp1_1.java",
			    "\n" +
			    "public class T811nrwp1_1 {}\n" +
			    "    \n"
            },
            "A.0.1");
    }

    // a class that is not a role class has a playedBy construct
    // 8.1.1-otjld-non-roleclass-with-playedby-2
    // see also Bug 354976 - better reporting of playedBy inside plain class
    public void test811_nonRoleclassWithPlayedby2() {
        runNegativeTest(
            new String[] {
		"T811nrwp2_2.java",
			    "\n" +
			    "public class T811nrwp2_2 {\n" +
			    "    public class Inner811nrwp2_2 playedBy T811nrwp2_1 {}\n" +
			    "}\n" +
			    "    \n",
		"T811nrwp2_1.java",
			    "\n" +
			    "public class T811nrwp2_1 {}\n" +
			    "    \n"
            },
            "----------\n" +
            "1. ERROR in T811nrwp2_2.java (at line 3)\n" +
            "	public class Inner811nrwp2_2 playedBy T811nrwp2_1 {}\n" +
            "	                             ^^^^^^^^\n" +
            "Object Teams keyword not enabled in this scope (OTJLD A.0.1).\n" +
            "----------\n");
    }

    // a class that is not a role class has a playedBy construct
    // 8.1.1-otjld-non-roleclass-with-playedby-3
    public void test811_nonRoleclassWithPlayedby3() {
        runNegativeTestMatching(
            new String[] {
		"Team811nrwp3.java",
			    "\n" +
			    "public team class Team811nrwp3 playedBy T811nrwp3 {}\n" +
			    "    \n",
		"T811nrwp3.java",
			    "\n" +
			    "public class T811nrwp3 {}\n" +
			    "    \n"
            },
            "A.1.1(a)");
    }

    // a class that is not a role class has a playedBy construct
    // 8.1.1-otjld-non-roleclass-with-playedby-4
    public void test811_nonRoleclassWithPlayedby4() {
        runNegativeTestMatching(
            new String[] {
		"Team811nrwp4.java",
			    "\n" +
			    "public team class Team811nrwp4 {\n" +
			    "    public void test() {\n" +
			    "        class Local811nrwp4 playedBy T811nrwp4 {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T811nrwp4.java",
			    "\n" +
			    "public class T811nrwp4 {}\n" +
			    "    \n"
            },
            "A.1.1(a)");
    }

    // a class that is not a role class has a playedBy construct (also member of non-team role)
    // 8.1.1-otjld-non-roleclass-with-playedby-5
    public void test811_nonRoleclassWithPlayedby5() {
        runNegativeTestMatching(
            new String[] {
		"Team811nrwp5.java",
			    "\n" +
			    "public team class Team811nrwp5 {\n" +
			    "    public class Role811nrwp5 {\n" +
			    "        public class Innerl811nrwp5 playedBy T811nrwp5 {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T811nrwp5.java",
			    "\n" +
			    "public class T811nrwp5 {}\n" +
			    "    \n"
            },
            "1.5(a,b)");
    }

    // a normal class inherits from a team class
    // 8.2.1-otjld-class-inherits-from-team-class
    public void test821_classInheritsFromTeamClass() {
        runNegativeTestMatching(
            new String[] {
		"T821ciftc.java",
			    "\n" +
			    "public class T821ciftc extends Team821ciftc {\n" +
			    "    public class Role821ciftc {}\n" +
			    "}\n" +
			    "    \n",
		"Team821ciftc.java",
			    "\n" +
			    "public team class Team821ciftc {\n" +
			    "    public class Role821ciftc {}\n" +
			    "}\n" +
			    "    \n"
            },
            "1.3");
    }

    // a method with the callin modifier is defined outside of a role class
    // 8.3.1-otjld-nonrole-callin-method-1
    public void test831_nonroleCallinMethod1() {
        runNegativeTestMatching(
            new String[] {
		"T831ncm1.java",
			    "\n" +
			    "public class T831ncm1 {\n" +
			    "    public callin void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.0.1");
    }

    // a method with the callin modifier is defined outside of a role class
    // 8.3.1-otjld-nonrole-callin-method-2
    public void test831_nonroleCallinMethod2() {
        runNegativeTestMatching(
            new String[] {
		"Team831ncm2.java",
			    "\n" +
			    "public team class Team831ncm2 {\n" +
			    "    public callin void test() {\n" +
			    "        base.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.3.1-otjld-nonrole-callin-method-3
    public void test831_nonroleCallinMethod3() {
        runNegativeTestMatching(
            new String[] {
		"Team831ncm3.java",
			    "\n" +
			    "public team class Team831ncm3 {\n" +
			    "    public void test() {\n" +
			    "        class Local831ncm3 {\n" +
			    "            callin void doSomething() {\n" +
			    "                base.doSomething();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.3.1-otjld-nonrole-callin-method-4
    public void test831_nonroleCallinMethod4() {
        runNegativeTestMatching(
            new String[] {
		"Team831ncm4.java",
			    "\n" +
			    "public team class Team831ncm4 {\n" +
			    "    public void test() {\n" +
			    "        Object obj = new Object() {\n" +
			    "            callin void test() {}\n" +
			    "        };\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.3.1-otjld-nonrole-callin-method-5
    public void test831_nonroleCallinMethod5() {
        runNegativeTestMatching(
            new String[] {
		"Team831ncm5.java",
			    "\n" +
			    "public team class Team831ncm5 {\n" +
			    "    public class Role831ncm5 {\n" +
			    "        public class Inner831ncm5 {\n" +
			    "            public callin void test() {}\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.3.1-otjld-nonrole-callin-method-6
    public void test831_nonroleCallinMethod6() {
        runNegativeTestMatching(
            new String[] {
		"Team831ncm6.java",
			    "\n" +
			    "public team class Team831ncm6 {\n" +
			    "    public class Role831ncm6 playedBy T831ncm6 {\n" +
			    "        public void test() {\n" +
			    "            class Local831ncm6 {\n" +
			    "                public callin void doSomething() {\n" +
			    "                    base.doSomething();\n" +
			    "                }\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T831ncm6.java",
			    "\n" +
			    "public class T831ncm6 {}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.3.1-otjld-nonrole-callin-method-7
    public void test831_nonroleCallinMethod7() {
        runNegativeTestMatching(
            new String[] {
		"Team831ncm7.java",
			    "\n" +
			    "public team class Team831ncm7 {\n" +
			    "    public class Role831ncm7 playedBy T831ncm7 {\n" +
			    "        public void test() {\n" +
			    "            Object obj = new Object() {\n" +
			    "                public callin void doSomething() {\n" +
			    "                    base.doSomething();\n" +
			    "                }\n" +
			    "            };\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T831ncm7.java",
			    "\n" +
			    "public class T831ncm7 {}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method with the callin modifier is directly invoked
    // 8.4.1-otjld-callin-method-invocation-1
    public void test841_callinMethodInvocation1() {
        runNegativeTestMatching(
            new String[] {
		"Team841cmi1.java",
			    "\n" +
			    "public team class Team841cmi1 {\n" +
			    "    public class Role841cmi1 playedBy T841cmi1 {\n" +
			    "        callin void doSomething() {\n" +
			    "            base.doSomething();\n" +
			    "        }\n" +
			    "        doSomething <- replace test;\n" +
			    "\n" +
			    "        public void test() {\n" +
			    "            doSomething();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T841cmi1.java",
			    "\n" +
			    "public class T841cmi1 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method with the callin modifier is directly invoked
    // 8.4.1-otjld-callin-method-invocation-2
    public void test841_callinMethodInvocation2() {
        runNegativeTestMatching(
            new String[] {
		"Team841cmi2_2.java",
			    "\n" +
			    "public team class Team841cmi2_2 extends Team841cmi2_1 {\n" +
			    "    public class Role841cmi2 {\n" +
			    "        public void test() {\n" +
			    "            doSomething();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T841cmi2.java",
			    "\n" +
			    "public class T841cmi2 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team841cmi2_1.java",
			    "\n" +
			    "public team class Team841cmi2_1 {\n" +
			    "    public class Role841cmi2 playedBy T841cmi2 {\n" +
			    "        callin void doSomething() {\n" +
			    "            base.doSomething();\n" +
			    "        }\n" +
			    "        doSomething <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a method binding is defined outside of a role class
    // 8.5.1-otjld-nonrole-method-binding-1
    public void test851_nonroleMethodBinding1() {
        runNegativeTest(
            new String[] {
		"T851nmb1_1.java",
			    "\n" +
			    "public class T851nmb1_1 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"T851nmb1_2.java",
			    "\n" +
			    "public class T851nmb1_2 extends T851nmb1_1 {\n" +
			    "    void test() => void test();\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a method binding is defined outside of a role class
    // 8.5.1-otjld-nonrole-method-binding-2
    public void test851_nonroleMethodBinding2() {
        runNegativeTest(
            new String[] {
		"Team851nmb2.java",
			    "\n" +
			    "public team class Team851nmb2 {\n" +
			    "    public void test() {}\n" +
			    "    test <- before test;\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.5.1-otjld-nonrole-method-binding-3
    public void test851_nonroleMethodBinding3() {
        runNegativeTest(
            new String[] {
		"Team851nmb3.java",
			    "\n" +
			    "public team class Team851nmb3 {\n" +
			    "    public void test() {\n" +
			    "        class Local851nmb3 {\n" +
			    "            public callin void doSomething() {\n" +
			    "                base.doSomething();\n" +
			    "            }\n" +
			    "            test <- replace doSomething;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.5.1-otjld-nonrole-method-binding-4
    public void test851_nonroleMethodBinding4() {
        runNegativeTest(
            new String[] {
		"Team851nmb4.java",
			    "\n" +
			    "public team class Team851nmb4 {\n" +
			    "    public void test() {\n" +
			    "        Object obj = new Object() {\n" +
			    "            public abstract String test();\n" +
			    "            test -> toString;\n" +
			    "        };\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.5.1-otjld-nonrole-method-binding-5
    public void test851_nonroleMethodBinding5() {
        runNegativeTest(
            new String[] {
		"Team851nmb5.java",
			    "\n" +
			    "public team class Team851nmb5 {\n" +
			    "    public class Role851nmb5 {\n" +
			    "        public class Inner851nmb5 {\n" +
			    "            public void test() {}\n" +
			    "            void test() <- before String toString();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.5.1-otjld-nonrole-method-binding-6
    public void test851_nonroleMethodBinding6() {
        runNegativeTest(
            new String[] {
		"T851nmb6.java",
			    "\n" +
			    "public class T851nmb6 {}\n" +
			    "    \n",
		"Team851nmb6.java",
			    "\n" +
			    "public team class Team851nmb6 {\n" +
			    "    public class Role851nmb6 playedBy T851nmb6 {\n" +
			    "        public void test() {\n" +
			    "            class Local851nmb6 {\n" +
			    "                public callin String test() {\n" +
			    "                    return base.test();\n" +
			    "                }\n" +
			    "                test <- replace toString;\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a method with the callin modifier is not defined inside of a role class
    // 8.5.1-otjld-nonrole-method-binding-7
    public void test851_nonroleMethodBinding7() {
        runNegativeTest(
            new String[] {
		"T851nmb7.java",
			    "\n" +
			    "public class T851nmb7 {}\n" +
			    "    \n",
		"Team851nmb7.java",
			    "\n" +
			    "public team class Team851nmb7 {\n" +
			    "    public class Role851nmb7 playedBy T851nmb7 {\n" +
			    "        public void test() {\n" +
			    "            Object obj = new Object() {\n" +
			    "                public abstract String test();\n" +
			    "                test -> toString;\n" +
			    "            };\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a parameter mapping separates parts with semicolon
    // 8.6.1-otjld-parammap-illegal-semi-1
    public void test861_parammapIllegalSemi1() {
        runNegativeTest(
            new String[] {
		"T861pis1.java",
				"public class T861pis1 {}\n",
		"Team861pis1.java",
			    "\n" +
			    "public team class Team861pis1 {\n" +
			    "	protected class Role playedBy T861pis1{\n" +
			    "		abstract String getHashStr();\n" +
			    "		\n" +
			    "		String getHashStr() -> boolean equals(Object other) with {\n" +
			    "			this -> other;\n" +
			    "			result <- Integer.toString(result)\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
    		"----------\n" + 
    		"1. ERROR in Team861pis1.java (at line 7)\n" + 
    		"	this -> other;\n" + 
    		"	             ^\n" + 
    		"Syntax error on token \";\", , expected\n" + 
    		"----------\n");
    }

    // a parameter mapping is terminated by semicolon
    // 8.6.1-otjld-parammap-illegal-semi-2
    public void test861_parammapIllegalSemi2() {
        runNegativeTestMatching(
            new String[] {
		"Team861pis2.java",
			    "\n" +
			    "public team class Team861pis2 {\n" +
			    "	protected class Role playedBy Object {\n" +
			    "		abstract String getHashStr();\n" +
			    "		\n" +
			    "		String getHashStr() -> boolean equals(Object other) with {\n" +
			    "			this -> other,\n" +
			    "			result <- Integer.toString(result);\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "\";\"");
    }

    // a parameter mapping is terminated by semicolon
    // 8.6.1-otjld-parammap-illegal-semi-3
    public void test861_parammapIllegalSemi3() {
        runNegativeTestMatching(
            new String[] {
		"Team861pis3.java",
			    "\n" +
			    "public team class Team861pis3 {\n" +
			    "	protected class Role playedBy Object {\n" +
			    "		abstract String getHashStr();\n" +
			    "		\n" +
			    "		String getHashStr() -> int hashCode() with {\n" +
			    "			result <- Integer.toString(result);\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "\";\"");
    }

    // a parameter mapping is terminated with comma
    // 8.6.1-otjld-parammap-illegal-semi-4
    public void test861_parammapIllegalSemi4() {
        runConformTest(
            new String[] {
		"Team861pis4.java",
			    "public team class Team861pis4 {\n" +
			    "	protected class Role playedBy T861pis4 {\n" +
			    "		abstract String getHashStr();\n" +
			    "		\n" +
			    "		String getHashStr() -> int hashCode() with {\n" +
			    "			result <- Integer.toString(result),\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n",
		"T861pis4.java",
				"public class T861pis4 {}\n"
            });
    }

    // a local variable has the name "base"
    // 8.7.1-otjld-base-used-in-declaration-1
    public void test871_baseUsedInDeclaration1() {
        runConformTest(
            new String[] {
		"T871buid1.java",
			    "\n" +
			    "public class T871buid1 {\n" +
			    "    void foo() {\n" +
			    "        int base = 1;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            });
    }

    // a nested type has the name "base"
    // 8.7.1-otjld-base-used-in-declaration-2
    public void test871_baseUsedInDeclaration2() {
        runConformTest(
            new String[] {
		"T871buid2.java",
			    "\n" +
			    "public class T871buid2 {\n" +
			    "    class base {}\n" +
			    "}    \n" +
			    "    \n"
            });
    }

    // a method has the name "base"
    // 8.7.1-otjld-base-used-in-declaration-3
    public void test871_baseUsedInDeclaration3() {
        runConformTest(
            new String[] {
		"T871buid3.java",
			    "\n" +
			    "public class T871buid3 {\n" +
			    "    void base() {}\n" +
			    "}    \n" +
			    "    \n"
            });
    }

    // a base import refers to package 'base' - syntax error lateron
    // 8.7.1-otjld-base-used-in-declaration-4
    public void test871_baseUsedInDeclaration4() {
        runNegativeTestMatching(
            new String[] {
		"Team871buid4.java",
			    "\n" +
			    "import base base.T871buid4;\n" +
			    "public team class Team871buid4 {\n" +
			    "    protected class R playedBy T871buid4 {\n" +
			    "        i;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"base/T871buid4.java",
			    "\n" +
			    "package base;\n" +
			    "public class T871buid4 {}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team871buid4.java (at line 5)\n" + 
    		"	i;\n" + 
    		"	^\n" + 
    		"Syntax error, insert \"Identifier (\" to complete MethodHeaderName\n" + 
    		"----------\n");
    }

    // a "base" package is imported regularly from a team
    // 8.7.1-otjld-base-used-in-declaration-5
    public void test871_baseUsedInDeclaration5() {
       
       runConformTest(
            new String[] {
		"Team871buid5.java",
			    "\n" +
			    "import base.T871buid5;\n" +
			    "public team class Team871buid5 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new T871buid5().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"base/T871buid5.java",
			    "\n" +
			    "package base;\n" +
			    "public class T871buid5 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a "base" package is imported regularly from a role file
    // 8.7.1-otjld-base-used-in-declaration-6
    public void test871_baseUsedInDeclaration6() {
       
       runConformTest(
            new String[] {
		"Team871buid6.java",
			    "\n" +
			    "import base.T871buid6;\n" +
			    "public team class Team871buid6 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team871buid6().new R().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"base/T871buid6.java",
			    "\n" +
			    "package base;\n" +
			    "public class T871buid6 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team871buid6/R.java",
			    "\n" +
			    "team package Team871buid6;\n" +
			    "public class R {\n" +
			    "    public void test() {\n" +
			    "        new T871buid6().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // A role class header contains a bug, other roles have method mappings and guards
    // 8.8.1-otjld-buggy-role-1
    public void test881_buggyRole1() {
        runNegativeTest(
            new String[] {
		"Team881br1.java",
			    "\n" +
			    "public team class Team881br1 {\n" +
			    "    int x = 3;\n" +
			    "    int y;\n" +
			    "    public Team881br1 () {\n" +
			    "        y = 4;\n" +
			    "    }\n" +
			    "    void incrY(T881br1 as R1 r) {\n" +
			    "        y += r.getVal();\n" +
			    "    }\n" +
			    "    void incrX() { x++; }\n" +
			    "    \n" +
			    "    boolean xGTy() {\n" +
			    "        return x > y;\n" +
			    "    }\n" +
			    "    \n" +
			    "    public class R1 playedBy T881br1 \n" +
			    "        base when (hasRole(base, R1.class))\n" +
			    "    {\n" +
			    "        void perform()\n" +
			    "            when (xGTy())\n" +
			    "        {   \n" +
			    "            System.out.print(\"p\");\n" +
			    "        }\n" +
			    "        protected int getVal() { return 7; }\n" +
			    "        perform <- after test;\n" +
			    "    }\n" +
			    "    \n" +
			    "    public class R2 playedBy T881br1 \n" +
			    "        base when (hasRole(base, R2.class))\n" +
			    "    {\n" +
			    "        int v;\n" +
			    "        void go() -> void test();\n" +
			    "        // noType1 noName2() -> noType2 noName2();\n" +
			    "        String toString() => String toString();\n" +
			    "        // another comment\n" +
			    "    }\n" +
			    "    \n" +
			    "    public class R3 played // { lbrace is assumed by the parser\n" +
			    "        int i;\n" +
			    "        int getI() { return i; }\n" +
			    "    }\n" +
			    "    \n" +
			    "    void setX (int x) {\n" +
			    "        this.x = x;\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"ambiguouslowering\")" +
			    "    void goR2(T881br1 as R2 r2) {\n" +
			    "        System.out.print(r2);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T881br1.java",
			    "\n" +
			    "public class T881br1 {\n" +
			    "    public int w;\n" +
			    "    void test() {}\n" +
			    "}    \n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team881br1.java (at line 39)\n" + 
    		"	public class R3 played // { lbrace is assumed by the parser\n" + 
    		"	                ^^^^^^\n" + 
    		"Syntax error on token \"played\", { expected\n" + 
    		"----------\n");
    }

    // witness for TPX-284, 4 errors is not optimal but original JDT does not perform better
    // 8.8.1-otjld-buggy-role-2
    public void test881_buggyRole2() {
        runNegativeTest(
            new String[] {
		"Team881br2.java",
			    "\n" +
			    "public abstract team class Team881br2 {\n" +
			    "	public abstract class R playedBy T881br2 {\n" +
			    "		abstract int bar();\n" +
			    "		int bar() -> Integer foo() \n" +
			    "		   with { result <- result.intValue() };\n" +
			    "		   \n" +
			    "		abstract int wrong(noTypeGiven);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T881br2.java",
			    "\n" +
			    "public class T881br2 {\n" +
			    "	Integer foo() { return null; }\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team881br2.java (at line 6)\n" + 
    		"	with { result <- result.intValue() };\n" + 
    		"	      ^\n" + 
    		"Syntax error on token \";\", & expected before this token\n" + 
    		"----------\n" + 
    		"2. ERROR in Team881br2.java (at line 6)\n" + 
    		"	with { result <- result.intValue() };\n" + 
    		"	                       ^\n" + 
    		"Syntax error on token \".\", delete this token\n" + 
    		"----------\n" + 
    		"3. ERROR in Team881br2.java (at line 6)\n" + 
    		"	with { result <- result.intValue() };\n" + 
    		"	                                 ^\n" + 
    		"Syntax error on token \")\", { expected after this token\n" + 
    		"----------\n" + 
    		"4. ERROR in Team881br2.java (at line 8)\n" + 
    		"	abstract int wrong(noTypeGiven);\n" + 
    		"	             ^^^^^^\n" + 
    		"Abstract methods do not specify a body\n" + 
    		"----------\n" + 
    		"5. ERROR in Team881br2.java (at line 8)\n" + 
    		"	abstract int wrong(noTypeGiven);\n" + 
    		"	                   ^^^^^^^^^^^\n" + 
    		"Syntax error, insert \"... VariableDeclaratorId\" to complete FormalParameterList\n" + 
    		"----------\n" + 
    		"6. ERROR in Team881br2.java (at line 9)\n" + 
    		"	}\n" + 
    		"	^\n" + 
    		"Syntax error on token \"}\", delete this token\n" + 
    		"----------\n" + 
    		"7. ERROR in Team881br2.java (at line 10)\n" + 
    		"	}	\n" + 
    		"	^\n" + 
    		"Syntax error, insert \"}\" to complete ClassBody\n" + 
    		"----------\n");
    }

    // as previous but more method mappings
    // 8.8.1-otjld-buggy-role-3
    public void test881_buggyRole3() {
        runNegativeTest(
            new String[] {
		"Team881br3.java",
			    "\n" +
			    "public abstract team class Team881br3 {\n" +
			    "	public abstract class R playedBy T881br3 {\n" +
			    "		abstract int bar() throws Throwable;\n" +
			    "		callin String getString() { return \"\"; }\n" +
			    "		int bar() -> Integer foo() \n" +
			    "		   with { result <- result.intValue() };\n" +
			    "		int hashCode() => int hashCode();\n" +
			    "		String getString() <- replace String toString()\n" +
			    "			with {\n" +
			    "				result -> result\n" +
			    "			}\n" +
			    "		finalize <- after foo;\n" +
			    "		abstract int wrong(noTypeGiven);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T881br3.java",
			    "\n" +
			    "public class T881br3 {\n" +
			    "	Integer foo() throws Throwable { return null; }\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team881br3.java (at line 7)\n" + 
    		"	with { result <- result.intValue() };\n" + 
    		"	      ^\n" + 
    		"Syntax error on token \";\", & expected before this token\n" + 
    		"----------\n" + 
    		"2. ERROR in Team881br3.java (at line 7)\n" + 
    		"	with { result <- result.intValue() };\n" + 
    		"	                       ^\n" + 
    		"Syntax error on token \".\", delete this token\n" + 
    		"----------\n" + 
    		"3. ERROR in Team881br3.java (at line 7)\n" + 
    		"	with { result <- result.intValue() };\n" + 
    		"	                                 ^\n" + 
    		"Syntax error on token \")\", { expected after this token\n" + 
    		"----------\n" + 
    		"4. ERROR in Team881br3.java (at line 11)\n" + 
    		"	result -> result\n" + 
    		"			}\n" + 
    		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Syntax error on tokens, delete these tokens\n" + 
    		"----------\n" + 
    		"5. ERROR in Team881br3.java (at line 14)\n" + 
    		"	abstract int wrong(noTypeGiven);\n" + 
    		"	             ^^^^^^\n" + 
    		"Abstract methods do not specify a body\n" + 
    		"----------\n" + 
    		"6. ERROR in Team881br3.java (at line 14)\n" + 
    		"	abstract int wrong(noTypeGiven);\n" + 
    		"	                   ^^^^^^^^^^^\n" + 
    		"Syntax error, insert \"... VariableDeclaratorId\" to complete FormalParameterList\n" + 
    		"----------\n" + 
    		"7. ERROR in Team881br3.java (at line 15)\n" + 
    		"	}\n" + 
    		"	^\n" + 
    		"Syntax error on token \"}\", delete this token\n" + 
    		"----------\n" + 
    		"8. ERROR in Team881br3.java (at line 16)\n" + 
    		"	}	\n" + 
    		"	^\n" + 
    		"Syntax error, insert \"}\" to complete ClassBody\n" + 
    		"----------\n");
    }

    // calling an internal method
    // 8.9.1-otjld-internal-name-1
    public void test891_internalName1() {
        runNegativeTestMatching(
            new String[] {
		"T891in1.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T891in1 {\n" +
			    "	void foo() {\n" +
			    "		Team t = new Team();\n" +
			    "		t._OT$registerAtBases();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "A.0.3");
    }

    // referencing an internal field
    // 8.9.1-otjld-internal-name-2
    public void test891_internalName2() {
        runNegativeTestMatching(
            new String[] {
		"T891in2.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T891in2 {\n" +
			    "	void foo() {\n" +
			    "		Team t = new Team();\n" +
			    "		System.out.print(t._OT$activationState);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "A.0.3");
    }

    // calling an internal method from the team
    // 8.9.1-otjld-internal-name-3
    public void test891_internalName3() {
        runNegativeTestMatching(
            new String[] {
		"Team891in3.java",
			    "\n" +
			    "public team class Team891in3 {\n" +
			    "	void foo() {\n" +
			    "		_OT$registerAtBases();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "A.0.3");
    }

    // referencing an internal field from the team
    // 8.9.1-otjld-internal-name-4
    public void test891_internalName4() {
        runNegativeTestMatching(
            new String[] {
		"Team891in4.java",
			    "\n" +
			    "public team class Team891in4 {\n" +
			    "	void foo() {\n" +
			    "		System.out.print(_OT$activationState);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "A.0.3");
    }

    // return type is missing
    // 8.10.1-otjld-buggy-shorthand-callout-1
    public void test8101_buggyShorthandCallout1() {
        runNegativeTest(
            new String[] {
		"Team8101bsc1.java",
			    "\n" +
			    "public team class Team8101bsc1 {\n" +
			    "    protected class R playedBy T8101bsc1 {\n" +
			    "        setVal(String v) -> set String val;\n" +
			    "        String getVal() -> get String val;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T8101bsc1.java",
			    "\n" +
			    "public class T8101bsc1 {\n" +
			    "    String val;\n" +
			    "}    \n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team8101bsc1.java (at line 4)\n" + 
    		"	setVal(String v) -> set String val;\n" + 
    		"	^^^^^^^^^^^^^^^^\n" + 
    		"Syntax error: missing return type for method designator (OTJLD A.3.2). \n" + 
    		"----------\n");
    }
    
    String[] getClassLibraries() {
		if (this.verifier != null)
			this.verifier.shutDown();
        this.verifier = getTestVerifier(false);
        this.createdVerifier = true;

    	String jarFilename = "t8syntax.jar";
    	String destPath = this.outputRootDirectoryPath+"/regression";
    	// upload the jar:
		Util.copy(getTestResourcePath(jarFilename), destPath);
    	// setup classpath:
    	String[] classPaths = getDefaultClassPaths();
    	int l = classPaths.length;
    	System.arraycopy(classPaths, 0, classPaths=new String[l+1], 0, l);
		classPaths[l] = this.outputRootDirectoryPath+"/regression/"+jarFilename;
		return classPaths;
    }

    // importing from a package containing 'team' in its name
    // 8.11.1-otjld-importname-contains-team-1
    public void test8111_importnameContainsTeam1() {
        runConformTest(
            new String[] {
		"T8111ict1.java",
			    "\n" +
			    "import org.my.pack.team.SomeClass;\n" +
			    "public class T8111ict1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new SomeClass().test();    \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            getClassLibraries(),
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
    }

    // importing from a package containing 'team' in its name
    // 8.11.1-otjld-importname-contains-team-2
    public void test8111_importnameContainsTeam2() {
        runConformTest(
            new String[] {
		"T8111ict2.java",
			    "\n" +
			    "import org.my.pack.team.SomeClass;\n" +
			    "public class T8111ict2 extends Object {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new SomeClass().test();    \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            getClassLibraries(),
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
    }

    // importing from a package containing 'team' in its name
    // 8.11.1-otjld-importname-contains-team-3
    public void test8111_importnameContainsTeam3() {
       runConformTest(
            new String[] {
		"T8111ict3.java",
			    "\n" +
			    "import org.my.pack.team.sub.OtherClass;\n" +
			    "public class T8111ict3 extends Object {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new OtherClass().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            getClassLibraries(),
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
    }

    // a declared lifting lacks an argument name
    // 8.11.2-otjld-declared-lifting-syntax-error-1
    public void test8112_declaredLiftingSyntaxError1() {
        runNegativeTest(
            new String[] {
		"T8112dlse1.java",
				"public class T8112dlse1 {}\n",
		"Team8112dlse1.java",
			    "\n" +
			    "public team class Team8112dlse1 {\n" +
			    "	protected class R playedBy T8112dlse1 {\n" +
			    "		int bla;\n" +
			    "	}\n" +
			    "	void run(Object as R)  {\n" +
			    "		System.out.print(\"blub\");\n" +
			    "	}\n" +
			    "	void nop() {}\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team8112dlse1.java (at line 6)\n" + 
    		"	void run(Object as R)  {\n" + 
    		"	                   ^\n" + 
    		"Syntax error, insert \"... VariableDeclaratorId\" to complete FormalParameterList\n" + 
    		"----------\n");
    }

    // a declared lifting lacks an argument name, callin method of nested team
    // 8.11.2-otjld-declared-lifting-syntax-error-2
    public void test8112_declaredLiftingSyntaxError2() {
        runNegativeTestMatching(
            new String[] {
		"T8112dlse2.java",
				"public class T8112dlse2 {}\n",
		"Team8112dlse2.java",
			    "\n" +
			    "public team class Team8112dlse2 {\n" +
			    "	protected team class Mid {\n" +
			    "		protected class R playedBy T8112dlse2 {\n" +
			    "			int bla;\n" +
			    "		}\n" +
			    "		callin void run(Object as R)  {\n" +
			    "		}\n" +
			    "		// no further declarations -> recovery is less successful.\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team8112dlse2.java (at line 7)\n" + 
    		"	callin void run(Object as R)  {\n" + 
    		"	                          ^\n" + 
    		"Syntax error, insert \"... VariableDeclaratorId\" to complete FormalParameterList\n" + 
    		"----------\n" + 
    		"2. ERROR in Team8112dlse2.java (at line 10)\n" + 
    		"	}\n" + 
    		"	^\n" + 
    		"Syntax error on token \"}\", delete this token\n" + 
    		"----------\n" + 
    		"3. ERROR in Team8112dlse2.java (at line 11)\n" + 
    		"	}\n" + 
    		"	^\n" + 
    		"Syntax error, insert \"}\" to complete ClassBody\n" + 
    		"----------\n");
    }

    // a declared lifting lacks an argument name, callin method of nested team
    // 8.11.2-otjld-declared-lifting-syntax-error-3
    public void test8112_declaredLiftingSyntaxError3() {
        runNegativeTest(
            new String[] {
		"Team8112dlse3.java",
			    "\n" +
			    "public team class Team8112dlse3 {\n" +
			    "        public team class TR playedBy T8112dlse3_1 {\n" +
			    "                public class R playedBy T8112dlse3_2 {\n" +
			    "                        public void bla() {}\n" +
			    "                }\n" +
			    "                callin void m(T8112dlse3_2 as R) {\n" +
			    "                        base.m(b);\n" +
			    "                        b.bla();\n" +
			    "                }\n" +
			    "                m <- replace m;\n" +
			    "        }\n" +
			    "}\n" +
			    "	\n",
		"T8112dlse3_1.java",
			    "\n" +
			    "public class T8112dlse3_1 {\n" +
			    "	void m(T8112dlse3_2 other) {}\n" +
			    "}\n" +
			    "	\n",
		"T8112dlse3_2.java",
			    "\n" +
			    "public class T8112dlse3_2 {\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team8112dlse3.java (at line 7)\n" + 
    		"	callin void m(T8112dlse3_2 as R) {\n" + 
    		"	                              ^\n" + 
    		"Syntax error, insert \"... VariableDeclaratorId\" to complete FormalParameterList\n" + 
    		"----------\n");
    }

    // Fixed: a declared lifting lacks an argument name, callin method of nested team
    // 8.11.2-otjld-declared-lifting-syntax-error-4
    public void test8112_declaredLiftingSyntaxError4() {
       
       runConformTest(
            new String[] {
		"Team8112dlse4.java",
			    "\n" +
			    "public team class Team8112dlse4 {\n" +
			    "        public team class TR playedBy T8112dlse4_1 {\n" +
			    "                public class R playedBy T8112dlse4_2 {\n" +
			    "                        public void bla() {\n" +
			    "							System.out.print(\"K\");\n" +
			    "						}\n" +
			    "                }\n" +
			    "                callin void m(T8112dlse4_2 as R b) {\n" +
			    "                        base.m(b);\n" +
			    "                        b.bla();\n" +
			    "                }\n" +
			    "                m <- replace m;\n" +
			    "        }\n" +
			    "		Team8112dlse4() {\n" +
			    "			activate();\n" +
			    "		}\n" +
			    "		public static void main(String[] args) {\n" +
			    "			T8112dlse4_1 b1 = new T8112dlse4_1();\n" +
			    "			T8112dlse4_2 b2 = new T8112dlse4_2();\n" +
			    "			new Team8112dlse4();\n" +
			    "			b1.m(b2);\n" +
			    "		}\n" +
			    "}\n" +
			    "	\n",
		"T8112dlse4_1.java",
			    "\n" +
			    "public class T8112dlse4_1 {\n" +
			    "	void m(T8112dlse4_2 other) {\n" +
			    "		System.out.print(\"O\");\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T8112dlse4_2.java",
			    "\n" +
			    "public class T8112dlse4_2 {\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // team is used in a package name
    // 8.12.1-otjld-scoped-keyword-1
    public void test8121_scopedKeyword1() {
       
       runConformTest(
            new String[] {
		"T8121sk1Main.java",
			    "\n" +
			    "import org.team.T8121sk1;\n" +
			    "public class T8121sk1Main {\n" +
			    "  public static void main(String[] args) {\n" +
			    "    T8121sk1 o = new T8121sk1();\n" +
			    "    o.test();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"org/team/T8121sk1.java",
			    "\n" +
			    "package org.team;\n" +
			    "public class T8121sk1 {\n" +
			    "  public void test() {\n" +
			    "    System.out.print(\"OK\");\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a role guard is empty
    // 8.13.1-otjld-buggy-guard-1
    public void test8131_buggyGuard1() {
        runNegativeTestMatching(
            new String[] {
		"Team8131bg1.java",
			    "\n" +
			    "public team class Team8131bg1 {\n" +
			    "  protected class R when() {\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "A.7.1");
    }

    // a binding guard is empty
    // 8.13.1-otjld-buggy-guard-2
    public void test8131_buggyGuard2() {
        runNegativeTestMatching(
            new String[] {
		"Team8131bg2.java",
			    "\n" +
			    "public team class Team8131bg2 {\n" +
			    "  protected class R playedBy T8131bg2 {\n" +
			    "    toString <- after toString base when();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"T8131bg2.java",
			    "\n" +
			    "public class T8131bg2 {}\n" +
			    "  \n"
            },
            "A.7.1");
    }

    // a binding guard is empty - not even parens, see also B.1.1-otjld-oo-1
    // 8.13.1-otjld-buggy-guard-3
    public void test8131_buggyGuard3() {
        runNegativeTest(
            new String[] {
		"Team8131bg2.java",
			    "\n" +
			    "public team class Team8131bg2 {\n" +
			    "  protected class R playedBy T8131bg2 {\n" +
			    "    toString <- after toString base when;\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"T8131bg2.java",
			    "\n" +
			    "public class T8131bg2 {}\n" +
			    "  \n"
            },
            "----------\n" + 
    		"1. WARNING in Team8131bg2.java (at line 4)\n" + 
    		"	toString <- after toString base when;\n" +
    		"	^^^^^^^^\n" + 
    		"Callin after binding cannot return a value to the base caller, role method return value of type java.lang.String will be ignored (OTJLD 4.4(a)).\n" + 
    		"----------\n" + 
    		"2. ERROR in Team8131bg2.java (at line 4)\n" + 
    		"	toString <- after toString base when;\n" + 
    		"	                           ^^^^^^^^^\n" + 
    		"Syntax error on token(s), misplaced construct(s)\n" + 
    		"----------\n");
    }
    
    // illegal use of "readonly" on field declaration
    public void test8132_illegalModifier1() {
    	runNegativeTest(
    		new String[] {
    	"Team8132im1.java",
    			"public team class Team8132im1 {\n" +
    			"	public readonly String val = \"V\";\n" +
    			"}\n"
    		},
    		"----------\n" + 
    		"1. ERROR in Team8132im1.java (at line 2)\n" + 
    		"	public readonly String val = \"V\";\n" + 
    		"	                       ^^^\n" + 
    		"Illegal modifier for the field val; only public, protected, private, static, final, transient & volatile are permitted\n" + 
    		"----------\n");
    }
    
    // illegal order of callin label and annotation (long)
    public void test8133_callinWithNameAndAnnotation1() {
    	runNegativeTest(
    		new String[] {
    	"Team8133cwnaa1.java",
    			"public team class Team8133cwnaa1 {\n" +
    			"    protected class R playedBy T8133cwnaa1 {\n" +
    			"	     @SuppressWarnings(\"unused\")\n" +
    			"        name: void foo() <- after void bar();\n" +
    			"        void foo() {}\n" +
    			"    }\n" +
    			"}\n",
    	"T8133cwnaa1.java",
    			"public class T8133cwnaa1 { void bar() {} }\n"
    		},
    		"----------\n" + 
			"1. ERROR in Team8133cwnaa1.java (at line 3)\n" + 
			"	@SuppressWarnings(\"unused\")\n" + 
			"	^^^^^^^^^^^^^^^^^\n" + 
			"Syntax error: callin annotations must be specified after the callin name (OTJLD A.3.3).\n" + 
			"----------\n");
    }

    // illegal order of callin label and annotation (short)
    public void test8133_callinWithNameAndAnnotation2() {
    	runNegativeTest(
    		new String[] {
    	"Team8133cwnaa2.java",
    			"public team class Team8133cwnaa2 {\n" +
    			"    protected class R playedBy T8133cwnaa2 {\n" +
    			"	     @SuppressWarnings(\"unused\")\n" +
    			"        name: foo <- after bar;\n" +
    			"        void foo() {}\n" +
    			"    }\n" +
    			"}\n",
    	"T8133cwnaa2.java",
    			"public class T8133cwnaa2 { void bar() {} }\n"
    		},
    		"----------\n" + 
			"1. ERROR in Team8133cwnaa2.java (at line 3)\n" + 
			"	@SuppressWarnings(\"unused\")\n" + 
			"	^^^^^^^^^^^^^^^^^\n" + 
			"Syntax error: callin annotations must be specified after the callin name (OTJLD A.3.3).\n" + 
			"----------\n");
    }
    
    // correct order of callin label and annotation (long)
    public void test8133_callinWithNameAndAnnotation3() {
    	runConformTest(
    		new String[] {
    	"Team8133cwnaa3.java",
    			"public team class Team8133cwnaa3 {\n" +
    			"    protected class R playedBy T8133cwnaa3 {\n" +
    			"        name:\n" +
    			"	     @SuppressWarnings(\"all\")\n" +
    			"        void foo() <- after void bar();\n" +
    			"        void foo() {}\n" +
    			"    }\n" +
    			"}\n",
    	"T8133cwnaa3.java",
    			"public class T8133cwnaa3 { void bar() {} }\n"
    		},
    		"");
    }

    // correct order of callin label and annotation (short)
    public void test8133_callinWithNameAndAnnotation4() {
    	runConformTest(
    		new String[] {
    	"Team8133cwnaa4.java",
    			"public team class Team8133cwnaa4 {\n" +
    			"    protected class R playedBy T8133cwnaa4 {\n" +
    			"        name:\n" +
    			"	     @SuppressWarnings(\"all\")\n" +
    			"        foo <- after bar;\n" +
    			"        void foo() {}\n" +
    			"    }\n" +
    			"}\n",
    	"T8133cwnaa4.java",
    			"public class T8133cwnaa4 { void bar() {} }\n"
    		},
    		"");
    }

    public void testBug475634a() {
    	runNegativeTest(
    		new String[] {
    			"X.java",
    			"public team class X {\n" +
    			"    protected class R playedBy java.util.List<@Ann> {}\n" +
				"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	protected class R playedBy java.util.List<@Ann> {}\n" + 
			"	                                          ^^^^\n" + 
			"Illegal position for value parameter @Ann: must be a parameter of a single name type reference(OTJLD A.9(a)).\n" + 
			"----------\n");
    }

    public void testBug475634b() {
    	runNegativeTest(
    		new String[] {
    			"X.java",
    			"public team class X extends java.util.List<@Ann> {\n" +
				"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public team class X extends java.util.List<@Ann> {\n" + 
			"	                                           ^^^^\n" + 
			"Illegal position for value parameter @Ann: must be a parameter of a single name type reference(OTJLD A.9(a)).\n" + 
			"----------\n");
    }
}
