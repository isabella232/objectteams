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
 * $Id: OTCompletionTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.model;

import java.util.Hashtable;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public class OTCompletionTests extends AbstractJavaModelCompletionTests implements RelevanceConstants {

public OTCompletionTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.5");
	}
	super.setUpSuite();
}
public void tearDownSuite() throws Exception {
	super.tearDownSuite();
}
static {
//	TESTS_NAMES = new String[] { "testCompletionCalloutToFieldDeclaration"};
}
public static Test suite() {
	return buildModelTestSuite(OTCompletionTests.class);
}

public void testCompletionBaseclass1() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionB");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "CompletionB";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"CompletionBaseclass[TYPE_REF]{CompletionBaseclass, , LCompletionBaseclass;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionBaseclass2() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam2.java",
            "public team class CompletionTeam2 {\n" +
            "  public class CompletionRoleA playedBy CompletionBaseclass {\n" +
    		"     String toString() => String toString();\n" +
    		"}\n" +
            "public class CompletionRoleB playedBy CompletionB");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "CompletionB";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"CompletionBaseclass[TYPE_REF]{CompletionBaseclass, , LCompletionBaseclass;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionSuperRole() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam2.java",
            "public team class CompletionTeam2 {\n" +
            "  public class ASuperRole playedBy CompletionBaseclass {\n" +
    		"     String toString() => String toString();\n" +
    		"  }\n" +
    		"  public interface ASuperRoleIfc {}\n" + // don't match this 
            "  public class CompletionRoleB extends ASuperR {}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "ASuperR";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"CompletionTeam2.ASuperRole[TYPE_REF]{ASuperRole, , LCompletionTeam2$ASuperRole;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionSuperRoleInterface() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam2.java",
            "public team class CompletionTeam2 {\n" +
            "  protected interface ISuperRole  {\n" +
    		"     String foo();\n" +
    		"  }\n" +
    		"  public class ISuperRoleClass {}\n" + // don't match this
            "  public class CompletionRoleB implements ISuperR {}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "ISuperR";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"CompletionTeam2.ISuperRole[TYPE_REF]{ISuperRole, , LCompletionTeam2$ISuperRole;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_INTERFACE+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionSuperRoles() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam2.java",
            "public team class CompletionTeam2 {\n" +
            "  protected class XRole  {\n" +
    		"     String foo();\n" +
    		"  }\n" +
    		"  public class XRoleClass {}\n" +
            "  public class XSubRole extends {}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "extends ";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	// dont't propose class part (__OT__...) nor current type nor team:
	assertResults(
		"CompletionTeam2.XRole[TYPE_REF]{XRole, , LCompletionTeam2$XRole;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n" +
		"CompletionTeam2.XRoleClass[TYPE_REF]{XRoleClass, , LCompletionTeam2$XRoleClass;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionBaseclass_Trac56() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/Trac56_SubTeam.java",
            "public team class Trac56_SubTeam extends Trac56_SuperTeam {\n" +
			"     public class SubRole playedBy SuperR" +
    		"}\n");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "SuperR";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
    
    // ensure that no illegal playedBy to role of the same (or super) team is proposed:
	assertResults("", requestor.getResults());
}

public void testCompletionBaseGuard1() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/BaseGuardTeam.java",
            "public team class BaseGuardTeam {\n" +
			"    public class MyRole playedBy CompletionBaseclass\n" +
			"        base when (true == base.ch)\n" +
			"    {\n" +
			"        void nothing() {}\n" +
			"    }\n" +
    		"}\n");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "base.ch";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
    
	
	assertResults(
		"check[METHOD_REF]{check(), LCompletionBaseclass;, ()Z, check, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_EXPECTED_TYPE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionBaseGuard2() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/BaseGuardTeam.java",
            "public team class BaseGuardTeam {\n" +
            "    protected class NoThisRole playedBy CompletionBaseclass\n" +
            "        base when (base==null) { }\n" +
			"    public class MyRole playedBy CompletionBaseclass\n" +
			"        base when (base.ch)" +
			"    {\n" +
			"    }\n" +
    		"}\n");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "base.ch";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
    
	
	assertResults(
		"check[METHOD_REF]{check(), LCompletionBaseclass;, ()Z, check, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}


@SuppressWarnings("unchecked")
public void testCompletionBaseclassDecapsulation() throws JavaModelException {
	Hashtable oldCurrentOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldCurrentOptions);

		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
	
		this.wc = getWorkingCopy(
	            "/Completion/src/CompletionTeam3.java",
	            "public team class CompletionTeam3 {\n" +
	            "  public class CompletionRoleA playedBy CompletionBaseclass {\n" +
	    		"     String toString() => String toString();\n" +
	    		"}\n" +
	            "public class CompletionRoleB playedBy CompletionI");
	    
	    
	    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	    String str = this.wc.getSource();
	    String completeBehind = "CompletionI";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
	
		assertResults(
			"CompletionInvisibleBaseclass[TYPE_REF]{p.CompletionInvisibleBaseclass, p, Lp.CompletionInvisibleBaseclass;, null, null, "+
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED)+"}",
			requestor.getResults());
	} finally {
		JavaCore.setOptions(oldCurrentOptions);
	}
}

//complete base class although it is equal to the role name
@SuppressWarnings("unchecked")
public void testCompletionBaseclassDecapsulation2() throws JavaModelException {
	Hashtable oldCurrentOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldCurrentOptions);

		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
	
		this.wc = getWorkingCopy(
	            "/Completion/src/CompletionTeam3.java",
	            "public team class CompletionTeam3 {\n" +
	            "  public class CompletionRoleA playedBy CompletionBaseclass {\n" +
	    		"     String toString() => String toString();\n" +
	    		"  }\n" +
	            "  public class CompletionInvisibleBaseclass playedBy CompletionI");
	    
	    
	    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	    String str = this.wc.getSource();
	    String completeBehind = "CompletionI";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
	
		assertResults(
			"CompletionInvisibleBaseclass[TYPE_REF]{p.CompletionInvisibleBaseclass, p, Lp.CompletionInvisibleBaseclass;, null, null, "+
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED)+"}",
			requestor.getResults());
	} finally {
		JavaCore.setOptions(oldCurrentOptions);
	}
}
// "playedBy" in a regular role:
public void testCompletionKeywordPlayedBy1() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamKW1.java",
            "public team class CompletionTeamKW1 {\n" +
            "  public class CompletionRoleA playedBy CompletionBaseclass {\n" +
    		"     String toString() => String toString();\n" +
    		"  }\n" +
            "  public class CompletionRoleB pla");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "pla";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"playedBy[KEYWORD]{playedBy, null, null, playedBy, null, "+
		(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE+ R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

//"playedBy" in a role file:
public void testCompletionKeywordPlayedBy2() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionSuperTeam/ExternalRole.java",
            "team package CompletionSuperTeam;\n"+
            "public class ExternalRole pla");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "pla";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"playedBy[KEYWORD]{playedBy, null, null, playedBy, null, "+
		(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE+ R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
// no "playedBy" in a regular class:
public void testCompletionKeywordPlayedBy3() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionClassKW3.java",
            "public class CompletionClassKW3 pla");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "pla";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"",
		requestor.getResults());
}
// no "playedBy" in a toplevel team:
public void testCompletionKeywordPlayedBy4() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamKW4.java",
            "public team class CompletionTeamKW4 pla");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "pla";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"",
		requestor.getResults());
}

// testing a reverted change in CompletionParser.attachOrphanCompletionNode()
public void testCompletionTypeReference1() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam2.java",
            "class CompletionTBase {}\n" +
            "public team class CompletionTeam2 {\n" +
            "  public class CompletionTRoleA playedBy CompletionTBaseclass {\n" +
    		"     String toString() => String toString();\n" +
    		"     protected CompletionT");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "CompletionT";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"CompletionTBase[TYPE_REF]{CompletionTBase, , LCompletionTBase;, null, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n" +
		"CompletionTeam2[TYPE_REF]{CompletionTeam2, , LCompletionTeam2;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n" +
		"CompletionTeam2.CompletionTRoleA[TYPE_REF]{CompletionTRoleA, , LCompletionTeam2$CompletionTRoleA;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

//testing a reverted change in CompletionParser.attachOrphanCompletionNode()
public void testCompletionTypeReference2() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam2.java",
            "public team class CompletionTeam2 {\n" +
            "  public class CompletionRoleA playedBy CompletionBaseclass {\n" +
    		"     String toString() => ;\n"+
    		"     protected Seriali");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "Seriali";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"Serializable[TYPE_REF]{java.io.Serializable, java.io, Ljava.io.Serializable;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

/** A base method spec with return type and beginning of the selector is searched. */
public void testCompletionMethodSpecLong1() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "  String toString() => String toStr");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "toStr";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"toString[METHOD_SPEC]{String toString();, LCompletionBaseclass;, ()Ljava.lang.String;, toString, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_EXACT_EXPECTED_TYPE+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

/** A base method spec with return type and beginning of the selector is searched - CALLIN. */
public void testCompletionMethodSpecLong2() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "  void foo() {}\n "+
            "  void foo() <- after String toStr ");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "toStr";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"toString[METHOD_SPEC]{toString();, LCompletionBaseclass;, ()Ljava.lang.String;, toString, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

/** A base method spec with the beginning of the selector is searched. */
public void testCompletionMethodSpecLong3() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "  int myHashCode() -> hash\n"+
            "}}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "hash";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"hashCode[METHOD_SPEC]{int hashCode();, Ljava.lang.Object;, ()I, hashCode, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

/** A base method spec with the beginning of the selector is searched - callin. */
public void testCompletionMethodSpecLong4() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "  int myHashCode() <- after hash");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "hash";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"hashCode[METHOD_SPEC]{int hashCode();, Ljava.lang.Object;, ()I, hashCode, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
/** A type in a RHS method spec signature is completed. */
public void testCompletionMethodSpecLong5() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "  public class CompletionRole playedBy CompletionBaseclass {\n" +
            "    int myHashCode() <- after void foo(CompletionB\n" +
            "  }\n"+
            "}\n");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "CompletionB";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"CompletionBaseclass[TYPE_REF]{CompletionBaseclass, , LCompletionBaseclass;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
/** A callin binding guard is completed. */
public void testCompletionBindingGuard() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "  public class CompletionRole playedBy CompletionBaseclass {\n" +
            "    int myHashCode() <- after void foo(CompletionBaseclass cb)\n" +
            "        when (CompletionB"+
            "  }\n"+
            "}\n");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "(CompletionB";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"CompletionBaseclass[TYPE_REF]{CompletionBaseclass, , LCompletionBaseclass;, null, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionMethodSpecShort1() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "  toString => toStri");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "toStri";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"toString[METHOD_SPEC]{toString;, LCompletionBaseclass;, ()Ljava.lang.String;, toString, null, "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

/* prefix of base method spec typed. */
public void testCompletionMethodSpecShort2() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "    void bar() {};\n" +
            "    bar <- after fub\n"+
            "}}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fubar[METHOD_SPEC]{fubar;, LCompletionBaseclass;, (ILjava.lang.String;)J, fubar, (fred, zork), "+
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}


public void testCompletionCalloutDeclaration1() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "fub \n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fub[POTENTIAL_METHOD_DECLARATION]{fub, LCompletionTeam1$CompletionRole;, ()V, fub, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_METHOD_OVERIDE)+"}\n"+
		"fubar[CALLOUT_DECLARATION]{long fubar(int fred, String zork) -> long fubar(int fred, String zork);, LCompletionBaseclass;, (ILjava.lang.String;)J, fubar, (fred, zork), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
// same as above but without a typed prefix
public void testCompletionCalloutDeclaration1a() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "/*here*/ \n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "/*here*/ ";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

    String[] results = requestor.getStringsResult();
    assertTrue("Expect more than one proposal", results.length > 1);
    for (int i = 0; i < results.length; i++) {
		if (results[i].startsWith("fubar")) {
			assertEquals("Expected proposal",  
					"fubar[CALLOUT_DECLARATION]{long fubar(int fred, String zork) -> long fubar(int fred, String zork);, LCompletionBaseclass;, (ILjava.lang.String;)J, fubar, (fred, zork), " +
					(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
					results[i]);
			return; // enough seen
		}
	}
    fail("Expected proposal not found");
}
// callout-override
public void testCompletionCalloutDeclaration2() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "   public class CompletionRoleA { long fubar(int x, String z) { return 0; } }\n"+
            "   public class CompletionRole extends CompletionRoleA playedBy CompletionBaseclass {\n" +
            "      fub \n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fub[POTENTIAL_METHOD_DECLARATION]{fub, LCompletionTeam1$CompletionRole;, ()V, fub, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_METHOD_OVERIDE)+"}\n"+
		"fubar[METHOD_DECLARATION]{long fubar(int x, String z), LCompletionTeam1$CompletionRoleA;, (ILjava.lang.String;)J, fubar, (x, z), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_METHOD_OVERIDE+R_NON_RESTRICTED)+"}\n"+
		"fubar[CALLOUT_OVERRIDE_DECLARATION]{long fubar(int fred, String zork) => long fubar(int fred, String zork);, LCompletionBaseclass;, (ILjava.lang.String;)J, fubar, (fred, zork), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}" ,
		requestor.getResults());
}
//callout-non-override (implicitly inherited abstract method)
public void testCompletionCalloutDeclaration3() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 extends CompletionSuperTeam {\n" +
            "   public class CompletionRole playedBy CompletionBaseclass {\n" +
            "      fub \n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fub[POTENTIAL_METHOD_DECLARATION]{fub, LCompletionTeam1$CompletionRole;, ()V, fub, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_METHOD_OVERIDE)+"}\n"+
		"fubar[CALLOUT_DECLARATION]{long fubar(int fred, String zork) -> long fubar(int fred, String zork);, LCompletionBaseclass;, (ILjava.lang.String;)J, fubar, (fred, zork), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n" +
		"fubar[METHOD_DECLARATION]{long fubar(int x, String z), LCompletionSuperTeam$CompletionRole;, (ILjava.lang.String;)J, fubar, (x, z), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_METHOD_OVERIDE+R_ABSTRACT_METHOD+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
// static methods
public void testCompletionCalloutDeclaration4() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy CompletionBaseclass {\n" +
            "mySt \n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "mySt";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"mySt[POTENTIAL_METHOD_DECLARATION]{mySt, LCompletionTeam1$CompletionRole;, ()V, mySt, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_METHOD_OVERIDE)+"}\n"+
		"myStaticBaseMethod[CALLOUT_DECLARATION]{void myStaticBaseMethod(Object a) -> void myStaticBaseMethod(Object a);, LCompletionBaseclass;, (Ljava.lang.Object;)V, myStaticBaseMethod, (a), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCompletionCalloutDeclarationTrac138() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "public class CompletionRole playedBy BaseTrac138 {\n" +
            "foo \n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "foo";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"foo[POTENTIAL_METHOD_DECLARATION]{foo, LCompletionTeam1$CompletionRole;, ()V, foo, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_METHOD_OVERIDE)+"}",
		requestor.getResults());
}

public void testCompletionCalloutToFieldDeclaration() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamCalloutToField.java",
            "public team class CompletionTeamCalloutToField {\n" +
            "  public class CompletionRole playedBy BaseTrac38 {\n" +
    		"    public void rm() {}\n"+
    		"    getS \n"+
    		"  }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);
    String str = this.wc.getSource();
    String completeBehind = "getS";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"get_str[CALLOUT_GET]{String get_str() -> get String _str;, LBaseTrac38;, ()Ljava.lang.String;, get_str, [129, 133], " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_SUBSTRING+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n"+
			"getS[POTENTIAL_METHOD_DECLARATION]{getS, LCompletionTeamCalloutToField$CompletionRole;, ()V, getS, [129, 133], "+
			+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_METHOD_OVERIDE)+"}\n"+
			"getStr[CALLOUT_GET]{String getStr() -> get String str;, LBaseTrac38;, ()Ljava.lang.String;, getStr, [129, 133], " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
			requestor.getResults());	
}

public void testCompletionCalloutToFieldDeclarationTrac138() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamCalloutToField.java",
            "public team class CompletionTeamCalloutToField {\n" +
            "  public class CompletionRole playedBy BaseTrac138 {\n" +
    		"    public void rm() {}\n"+
    		"    getBar \n"+
    		"  }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);
    String str = this.wc.getSource();
    String completeBehind = "getBar";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"getBar[POTENTIAL_METHOD_DECLARATION]{getBar, LCompletionTeamCalloutToField$CompletionRole;, ()V, getBar, [130, 136], "+
			+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_METHOD_OVERIDE)+"}",
			requestor.getResults());	
}

public void testCompletionParamMapping1() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamParamMapping1.java",
            "public team class CompletionTeamParamMapping1 {\n" +
            "  public class CompletionRole playedBy BaseTrac38 {\n" +
    		"    public void rm() {}\n"+
    		"    int strLen() -> get String str\n"+
    		"       with { result <- str.le\n"+
    		"       }\n"+
    		"  }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);
    String str = this.wc.getSource();
    String completeBehind = "str.le";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, [187, 189], "+
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
			requestor.getResults());	
}
public void testCompletionTrac38_1() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamTrac38_1.java",
            "public team class CompletionTeamTrac38_1 {\n" +
            "  public class CompletionRole playedBy BaseTrac38 {\n" +
    		"    public void rm() {}\n"+
    		"    callin void crm() {}\n"+
    		"    void _m() -> void _m();\n"+
    		"    void m() -> void m();\n"+
    		"    String str() -> get \n"+
    		"  }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);
    String str = this.wc.getSource();
    String completeBehind = "get ";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"_str[FIELD_SPEC]{String _str;, LBaseTrac38;, Ljava.lang.String;, _str, [222, 222], " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n"+
			"str[FIELD_SPEC]{String str;, LBaseTrac38;, Ljava.lang.String;, str, [222, 222], " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
			requestor.getResults());	
}
// same as above but without the space after "get" but still expect the same c-t-f proposals
public void testCompletionTrac38_2() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamTrac38_2.java",
            "public team class CompletionTeamTrac38_2 {\n" +
            "  public class CompletionRole playedBy BaseTrac38 {\n" +
    		"    public void rm() {}\n"+
    		"    callin void crm() {}\n"+
    		"    void _m() -> void _m();\n"+
    		"    void m() -> void m();\n"+
    		"    String str() -> get\n"+
    		"  }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);
    String str = this.wc.getSource();
    String completeBehind = "get";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"getClass[METHOD_SPEC]{java.lang.Class<?> getClass();, Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, [218, 221], "+
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n"+
			"_str[FIELD_SPEC]{ String _str;, LBaseTrac38;, Ljava.lang.String;, _str, [221, 221], " +  // yes, proposal starts with blank
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n"+
			"str[FIELD_SPEC]{ String str;, LBaseTrac38;, Ljava.lang.String;, str, [221, 221], " +     // yes, proposal starts with blank
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
			requestor.getResults());	
}
// indicate field name by role method spec naming convention:
public void testCompletionTrac38_3() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeamTrac38_2.java",
            "public team class CompletionTeamTrac38_2 {\n" +
            "  public class CompletionRole playedBy BaseTrac38 {\n" +
    		"    public void rm() {}\n"+
    		"    callin void crm() {}\n"+
    		"    void _m() -> void _m();\n"+
    		"    void m() -> void m();\n"+
    		"    String getStr() -> get \n"+
    		"  }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);
    String str = this.wc.getSource();
    String completeBehind = "get ";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"_str[FIELD_SPEC]{String _str;, LBaseTrac38;, Ljava.lang.String;, _str, [225, 225], " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_SUBSTRING+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n"+ // no R_CASE, R_EXACT_NAME !
			"str[FIELD_SPEC]{String str;, LBaseTrac38;, Ljava.lang.String;, str, [225, 225], " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_NAME+R_EXACT_EXPECTED_TYPE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
			requestor.getResults());	
}
public void testCompletionCallToCallout1() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "   public class CompletionRole playedBy CompletionBaseclass {\n" +
            "      long fubar(int fred, String zork) -> long fubar(int fred, String zork);\n"+
            "   }\n"+
            "   void test(CompletionRole r) {\n"+
            "       r.fub\n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fubar[METHOD_REF]{fubar(), LCompletionTeam1$CompletionRole;, (ILjava.lang.String;)J, fubar, (fred, zork), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
		requestor.getResults());	
}
// witness for https://svn.objectteams.org/trac/ot/ticket/22
public void testCompletionCallToCallout2() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/Main.java",
            "public class Main {\n"+
            "   void main() {\n"+
            "      CompletionRole<@CompletionCalloutTeam.INSTANCE> r= new CompletionRole<@CompletionCalloutTeam.INSTANCE>();\n"+
            "      r.fub\n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fubar[METHOD_REF]{fubar(), LCompletionCalloutTeam$CompletionRole;, (ILjava.lang.String;)J, fubar, (fred, zork), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
		requestor.getResults());	
}
public void testCompletionCallToCallout3() throws JavaModelException {
	// call to inferred callout?
	this.wc = getWorkingCopy(
			"/Completion/src/CompletionTeam1.java",
			"public team class CompletionTeam1 {\n" +
			"   public class CompletionRole playedBy CompletionBaseclass {\n" +
			"      void test() {\n" +
			"         fre\n" +
			"      }\n"+
			"   }\n"+
	"}");
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "fre";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
	
	assertResults(
			"fred[FIELD_REF]{fred, LCompletionBaseclass;, I, fred, null, " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
			requestor.getResults());		
}
public void testCompletionCallToCallout3this() throws JavaModelException {
	// call to inferred callout?
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "   public class CompletionRole playedBy CompletionBaseclass {\n" +
            "      void test() {\n" +
            "         this.fre\n" +
            "      }\n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fre";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fred[FIELD_REF]{fred, LCompletionBaseclass;, I, fred, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
		requestor.getResults());		
}
public void testCompletionCallToCallout3qualified() throws JavaModelException {
	// call to inferred callout? Not allowed on other role
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "   public class CompletionRole playedBy CompletionBaseclass {\n" +
            "      void test(CompletionRole other) {\n" +
            "         other.fre\n" +
            "      }\n"+
            "   }\n"+
            "}");
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fre";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"",
		requestor.getResults());		
}
public void testCompletionCallToCallout4() throws JavaModelException {
	// call to inferred callout?
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "   public class CompletionRole playedBy CompletionBaseclass {\n" +
            "      void test() {\n" +
            "         fub\n" +
            "      }\n"+
            "   }\n"+
            "}");
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fubar[METHOD_REF]{fubar(), LCompletionBaseclass;, (ILjava.lang.String;)J, fubar, (fred, zork), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());		
}
public void testCompletionCallToCallout5() throws JavaModelException {
	// indirect call via inferred callout
	this.wc = getWorkingCopy(
			"/Completion/src/CompletionTeam1.java",
			"public team class CompletionTeam1 {\n" +
			"   public class CompletionRole playedBy CompletionBaseclass {\n" +
			"      void test() {\n" +
			"         frood.endsW\n" +
			"      }\n"+
			"   }\n"+
	"}");
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "endsW";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
	
	assertResults(
			"endsWith[METHOD_REF]{endsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, endsWith, (postfix), " +
			(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
			requestor.getResults());		
}
public void testCompletionOverrideTSuper() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 extends CompletionSuperTeam {\n" +
            "   public class CompletionRole {\n" +
            "      noth \n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "noth";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"noth[POTENTIAL_METHOD_DECLARATION]{noth, LCompletionTeam1$CompletionRole;, ()V, noth, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_NON_RESTRICTED)+"}\n"+
		"nothing[METHOD_DECLARATION]{long nothing(int x, String z), LCompletionSuperTeam$CompletionRole;, (ILjava.lang.String;)J, nothing, (x, z), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_METHOD_OVERIDE+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
public void testCompletionOverrideAbstractTSuper() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 extends CompletionSuperTeam {\n" +
            "   public class CompletionRole {\n" +
            "      fub \n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fub[POTENTIAL_METHOD_DECLARATION]{fub, LCompletionTeam1$CompletionRole;, ()V, fub, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_NON_RESTRICTED)+"}\n"+
		"fubar[METHOD_DECLARATION]{long fubar(int x, String z), LCompletionSuperTeam$CompletionRole;, (ILjava.lang.String;)J, fubar, (x, z), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_METHOD_OVERIDE+R_ABSTRACT_METHOD+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
public void testCompletionDontOverrideLocal() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 extends CompletionSuperTeam {\n" +
            "   public class CompletionRole {\n" +
            "      long fubar(int a, String b) {}\n"+
            "      fub \n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fub[POTENTIAL_METHOD_DECLARATION]{fub, LCompletionTeam1$CompletionRole;, ()V, fub, null, " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
public void testCompletionCallAbstractTSuper() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 extends CompletionSuperTeam {\n" +
            "   public class CompletionRole {\n" +
            "      void m() { \n"+
            "         fub\n" + // unqualified
            "      }\n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "fub";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"fubar[METHOD_REF]{fubar(), LCompletionSuperTeam$CompletionRole;, (ILjava.lang.String;)J, fubar, (x, z), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
public void testCompletionCallTSuper() throws JavaModelException {		
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 extends CompletionSuperTeam {\n" +
            "   public class CompletionRole {\n" +
            "      void m(CompletionRole r) { \n"+
            "         r.noth\n" + // qualified
            "      }\n"+
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "noth";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"nothing[METHOD_REF]{nothing(), LCompletionSuperTeam$CompletionRole;, (ILjava.lang.String;)J, nothing, (x, z), " +
		(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_STATIC+R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
public void testCompletionOnConfined() throws JavaModelException {
	this.wc = getWorkingCopy(
            "/Completion/src/CompletionTeam1.java",
            "public team class CompletionTeam1 {\n" +
            "   void m(IConfined r) { \n"+
            "       r.\n" + 
            "   }\n"+
            "}");
    
    
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "r.";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"",
		requestor.getResults());	
}

}