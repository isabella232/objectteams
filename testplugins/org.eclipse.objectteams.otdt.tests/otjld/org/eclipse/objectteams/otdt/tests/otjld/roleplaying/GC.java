/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.roleplaying;

import java.util.Map;

import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;

public class GC extends AbstractOTJLDTest {
	
	public GC(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testA120_enumInTeam1", "testA120_enumInTeam2"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return GC.class;
	}
    // neither base nor role are ever referenced
    // 2.5.1-otjld-garbage-collecting-unreferenced-1
    public void test251_garbageCollectingUnreferenced1() {
       
       runConformTest(
            new String[] {
		"Team251gcu1.java",
			    "\n" +
			    "public team class Team251gcu1 {\n" +
			    "    int count = 0;\n" +
			    "    public class R playedBy T251gcu1 {\n" +
			    "        void countCallins() { Team251gcu1.this.count++; }\n" +
			    "        countCallins <- after t;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team251gcu1 t = new Team251gcu1();\n" +
			    "        t.activate();\n" +
			    "        for (int i=0; i<100; i++) {\n" +
			    "            new T251gcu1().t(i);\n" +
			    "        }        \n" +
			    "        System.gc();\n" +
			    "        System.out.print(t.count+\":\"+t.getAllRoles().length);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T251gcu1.java",
			    "\n" +
			    "public class T251gcu1 {\n" +
			    "    void t(int i) {\n" +
			    "    }    \n" +
			    "}    \n" +
			    "    \n"
            },
            "100:0");
    }

    // neither base nor role are ever referenced - role method is static, lifting never takes place
    // 2.5.1-otjld-garbage-collecting-unreferenced-2
    public void test251_garbageCollectingUnreferenced2() {
       
       runConformTest(
            new String[] {
		"Team251gcu2.java",
			    "\n" +
			    "public team class Team251gcu2 {\n" +
			    "    static int count = 0;\n" +
			    "    public class R playedBy T251gcu2 {\n" +
			    "        static void countCallins() { Team251gcu2.count++; }\n" +
			    "        countCallins <- after t;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team251gcu2 t = new Team251gcu2();\n" +
			    "        t.activate();\n" +
			    "        for (int i=0; i<200; i++) {\n" +
			    "            new T251gcu2().t(i);\n" +
			    "        }        \n" +
			    "        System.gc();\n" +
			    "        System.out.print(count+\":\"+t.getAllRoles().length);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T251gcu2.java",
			    "\n" +
			    "public class T251gcu2 {\n" +
			    "    void t(int i) {\n" +
			    "    }    \n" +
			    "}    \n" +
			    "    \n"
            },
            "200:0");
    }

    // base objects are stored then discarded
    // 2.5.2-otjld-garbage-collecting-referenced-1
    public void test252_garbageCollectingReferenced1() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"Team252gcr1.java",
			    "\n" +
			    "public team class Team252gcr1 {\n" +
			    "    public class R playedBy T252gcr1 {\n" +
			    "        int val;\n" +
			    "        void nop(int i) {\n" +
			    "            val = i;\n" +
			    "        }\n" +
			    "        void print() {\n" +
			    "            System.out.println(\"val=\"+val);\n" +
			    "        }\n" +
			    "        nop <- after t;\n" +
			    "        print <- after print;\n" +
			    "        public void testBase() {\n" +
			    "            System.out.println((baseI() == val) ? \"OK\": \"NOK\");\n" +
			    "        }\n" +
			    "        int baseI() -> get int i;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team252gcr1 t = new Team252gcr1();\n" +
			    "        t.activate();\n" +
			    "        final int n = 150;\n" +
			    "        T252gcr1[] bs = new T252gcr1[n];\n" +
			    "        for (int i=0; i<n; i++) {\n" +
			    "            bs[i] = new T252gcr1();\n" +
			    "            bs[i].t(i);\n" +
			    "        }        \n" +
			    "        System.gc(); // at this point in time only bs hold on to objects\n" +
			    "        \n" +
			    "        Object[] rs = t.getAllRoles();\n" +
			    "        System.out.println(rs.length);\n" +
			    "        ((R<@t>)rs[42]).testBase();\n" +
			    "        bs[42].print();\n" +
			    "        \n" +
			    "        // null out most and expect most to be gone:\n" +
			    "        Object keepBase = bs[42]; // unused local..\n" +
			    "        Object keepRole = rs[0];\n" +
			    "        rs = null;\n" +
			    "        bs = null;        \n" +
			    "        System.gc();\n" +
			    "        System.out.print(t.getAllRoles().length);\n" +
			    "        \n" +
			    "        // null out remaining and expect everything to be gone:\n" +
			    "        keepBase = null;\n" +
			    "        keepRole = null;\n" +
			    "        System.gc();\n" +
			    "        System.out.print(\"->\"+t.getAllRoles().length);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T252gcr1.java",
			    "\n" +
			    "public class T252gcr1 {\n" +
			    "    int i;\n" +
			    "    void t(int i) {\n" +
			    "        this.i = i;\n" +
			    "    }    \n" +
			    "    void print() {\n" +
			    "        System.out.print(\"B\"+i);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "150\n" +
            "OK\n" +
            "B42val=42\n" +
            "2->0",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // role are first stored then discarded
    // 2.5.2-otjld-garbage-collecting-referenced-2
    public void test252_garbageCollectingReferenced2() {
       
       runConformTest(
            new String[] {
		"Team252gcr2.java",
			    "\n" +
			    "public team class Team252gcr2 {\n" +
			    "    static final int n = 73;\n" +
			    "    R[] rs = new R[n];\n" +
			    "    public class R playedBy T252gcr2 {\n" +
			    "        int val;\n" +
			    "        void recordRole(int i) {\n" +
			    "            rs[i] = this;\n" +
			    "        }\n" +
			    "        void print() {\n" +
			    "            System.out.println(\"val=\"+val);\n" +
			    "        }\n" +
			    "        recordRole <- after t;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team252gcr2 t = new Team252gcr2();\n" +
			    "        t.activate();\n" +
			    "        for (int i=0; i<n; i++) {\n" +
			    "            new T252gcr2().t(i);\n" +
			    "        }        \n" +
			    "        System.gc();\n" +
			    "        System.out.print(t.getAllRoles().length);\n" +
			    "        t.rs = new R<@t>[0];\n" +
			    "        System.gc();\n" +
			    "        System.out.print(\"->\"+t.getAllRoles().length);        \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T252gcr2.java",
			    "\n" +
			    "public class T252gcr2 {\n" +
			    "    int i;\n" +
			    "    void t(int i) {\n" +
			    "        this.i = i;\n" +
			    "    }    \n" +
			    "    void print() {\n" +
			    "        System.out.print(\"B\"+i);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "73->0");
    }

    // a role is unregistered, after it can be gargabe collected
    // 2.5.3-otjld-garbage-collecting-unregistered-1
    public void test253_garbageCollectingUnregistered1() throws InterruptedException {
       
       runConformTest(
            new String[] {
		"Team253gcu1.java",
			    "\n" +
			    "import java.lang.ref.WeakReference;\n" +
			    "public team class Team253gcu1 {\n" +
			    "    static boolean running=true;\n" +
			    "    WeakReference<R> r = null;\n" +
			    "    protected class R playedBy T253gcu1 {\n" +
			    "        int v;\n" +
			    "        void detect(int i) {\n" +
			    "            this.v = i;\n" +
			    "            if (i == 42) {\n" +
			    "                Team253gcu1.this.unregisterRole(this);\n" +
			    "                Team253gcu1.this.r = new WeakReference<R>(this);\n" +
			    "            }\n" +
			    "        }\n" +
			    "        detect <- after t;\n" +
			    "        public void finalize() {\n" +
			    "            if (running)\n" +
			    "                System.out.print(\"f\"+v+\"!\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team253gcu1 t = new Team253gcu1();\n" +
			    "        t.activate();\n" +
			    "        int n = 80;\n" +
			    "        T253gcu1[] bs = new T253gcu1[n];\n" +
			    "        for (int i=0; i<n; i++) {\n" +
			    "            bs[i] = new T253gcu1();\n" +
			    "            bs[i].t(i);\n" +
			    "        }\n" +
			    "        System.gc();\n" +
			    "        try {\n" +
			    "            Thread.sleep(100); // wait for output from finalize\n" +
			    "        } catch (InterruptedException ie) {}\n" +
			    "        System.out.print(t.r.get()+\".\");\n" +
			    "        System.out.print(t.getAllRoles().length);\n" +
			    "        running = false;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T253gcu1.java",
			    "\n" +
			    "public class T253gcu1 {\n" +
			    "    void t(int i) {}\n" +
			    "}    \n" +
			    "    \n"
            },
            "f42!null.79");
    }
}
