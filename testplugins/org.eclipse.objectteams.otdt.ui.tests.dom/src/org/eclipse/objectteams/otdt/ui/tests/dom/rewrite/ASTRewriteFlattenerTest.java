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
 * $Id: ASTRewriteFlattenerTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;

/**
 * @author ikeman
 * $Id: ASTRewriteFlattenerTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class ASTRewriteFlattenerTest extends TestCase {
    
    public static final String TEST_PROJECT = "DOM_AST";
    private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;
    
    
    private ASTRewriteFlattener _rewriteFlattener;
    private AST _newAst;
    private RewriteEventStore _store;
    
    public ASTRewriteFlattenerTest(String name)
    {
        super(name);
    }
    
    protected void setUp() throws Exception 
    {
        super.setUp();
        _store = new RewriteEventStore();
        _rewriteFlattener = new ASTRewriteFlattener(_store);
        _newAst = AST.newAST(JAVA_LANGUAGE_SPEC_LEVEL);
    }
    
    public void testNewCuCreation()
    {
        PackageDeclaration newPack = _newAst.newPackageDeclaration();
        newPack.setName(_newAst.newQualifiedName(_newAst.newSimpleName("testmain"), _newAst.newSimpleName("testsub")));
        
        ImportDeclaration newImport = _newAst.newImportDeclaration();
        newImport.setName(_newAst.newQualifiedName(_newAst.newSimpleName("testpackage"), _newAst.newSimpleName("TestClass")));
        newImport.setOnDemand(false);
        List importList = new ArrayList();
        importList.add(newImport);
        
        CompilationUnit newCU = createCU(newPack, importList, null);
        
        newCU.accept(_rewriteFlattener);

        String actual = _rewriteFlattener.getResult();
        String expected = "package testmain.testsub;import testpackage.TestClass;";
        
        assertEquals("Wrong CU-Code", expected, actual);
    }

    public void testNewTeamCreation()
    {
        TypeDeclaration newTypeDecl = createTeam(null, (Modifier.PUBLIC | Modifier.OT_TEAM), 
                false, false, "GeneratedClass", null, null, null);        
       
        newTypeDecl.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "public team class GeneratedClass {}";
        
        assertEquals("Wrong TeamCode", expected, actual);
    }
    
    public void testNewRoleCreation()
    {
        
        RoleTypeDeclaration role = createRole(null, 1, "GeneratedRoleClass", "GeneratedBaseClass", 
                "GeneratedTeamClass", null, null , false, null);
        
        role.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "public class GeneratedRoleClass playedBy GeneratedBaseClass {}";
        
        assertEquals("Wrong RoleCode", expected, actual);
    }

    public void testNewParameterMappingCreation()
    {
        MethodInvocation methodInv = (MethodInvocation)createExpression("integer", "intValue");
        ParameterMapping newParameterMapping = 
            createParameterMapping(methodInv, "value", "->", false);
        newParameterMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "integer.intValue() -> value";
        
        assertEquals("Wrong ParameterMappingCode", expected, actual);
    }
    
    public void testNewParameterMappingCreation1()
    {

        SimpleName argumentName = _newAst.newSimpleName("val");
        List arguments = new ArrayList();
        arguments.add(argumentName);
        
        ClassInstanceCreation cIC = (ClassInstanceCreation)createExpression(null,"Integer", arguments, null);
        ParameterMapping newParameterMapping = 
            createParameterMapping(cIC, "integer", "->",false);
        newParameterMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "new Integer(val) -> integer";
        
        assertEquals("Wrong ParameterMappingCode", expected, actual);
    }

    public void testNewCalloutMappingCreation()
    {
        //Callout - roleMethodSpec - Parameter
        SingleVariableDeclaration roleMethodSpecParameter = 
            createMethodParameter(0, "Integer", null, "integer", 0, null);
        List roleMethodParameters = new ArrayList();
        roleMethodParameters.add(roleMethodSpecParameter);
       
        //Callout - roleMethodSpec
        MethodSpec roleMethodSpec = createMethodSpec("roleMethod0", PrimitiveType.VOID, roleMethodParameters, true);
        
        //Callout - baseMethodSpec - Parameter
        SingleVariableDeclaration baseMethodSpecParameter = 
            createMethodParameter(0, null, PrimitiveType.INT, "value", 0, null);
        List baseMethodParameters = new ArrayList();
        baseMethodParameters.add(baseMethodSpecParameter);
        
        //Callout - baseMethodSpec
        MethodSpec baseMethodSpec = createMethodSpec("baseMethod0", PrimitiveType.VOID, baseMethodParameters, true);
        
        //Callout - ParameterMapping 
        MethodInvocation methodInv = (MethodInvocation)createExpression("integer", "intValue");
        ParameterMapping newParameterMapping = 
            createParameterMapping(methodInv, "value", "->", false);
        List parameterMappingList = new ArrayList();
        parameterMappingList.add(newParameterMapping);
        
        //build CalloutMapping
        CalloutMappingDeclaration newCalloutMapping = createCalloutMapping(null, 0, roleMethodSpec, baseMethodSpec, parameterMappingList, false, true);
        
        newCalloutMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = 
        	"void roleMethod0(Integer integer) -> void baseMethod0(int value) with {\n" +
        	"    integer.intValue() -> value\n" +
        	"}";
        
        assertEquals("Wrong CalloutMappingCode", expected, actual);
    }
    
    public void testNewCalloutMappingCreation1()
    {
        //Callout - roleMethodSpec
        MethodSpec roleMethodSpec = createMethodSpec("roleMethod3", PrimitiveType.INT, null, false);
        
        //Callout - baseMethodSpec(s)
        MethodSpec baseMethodSpec = createMethodSpec("baseMethod3", PrimitiveType.INT, null, false);

        //build CalloutMapping
        CalloutMappingDeclaration newCalloutMapping = createCalloutMapping(null, 0, roleMethodSpec, baseMethodSpec, null, false, false);
        
        newCalloutMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "roleMethod3 -> baseMethod3;";
        
        assertEquals("Wrong CalloutMappingCode", expected, actual);
    }
    
    public void testNewCalloutMappingCreation2()
    {
        //Callout - roleMethodSpec
        MethodSpec roleMethodSpec = createMethodSpec("getTestString", PrimitiveType.VOID, null, false);
        
        //Callout - baseMethodSpec
        FieldAccessSpec newFieldAcc = createFieldAccSpec("_string", PrimitiveType.VOID, null, false);
        
        //build CalloutMapping
        CalloutMappingDeclaration newCalloutMapping = createCalloutMapping(null, 0, roleMethodSpec, newFieldAcc, null, false, false);
        newCalloutMapping.bindingOperator().setBindingModifier(Modifier.OT_GET_CALLOUT);
        
        newCalloutMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "getTestString -> get _string;";
        
        assertEquals("Wrong CalloutMappingCode", expected, actual);
    } 
    
    public void testNewCalloutMappingCreation3()
    {
        //Callout - roleMethodSpec - Parameter
        SingleVariableDeclaration roleMethodSpecParameter = 
            createMethodParameter(0, "Integer", null, "i", 0, null);
        List roleMethodParameters = new ArrayList();
        roleMethodParameters.add(roleMethodSpecParameter);
       
        
        //Callout - roleMethodSpec
        MethodSpec roleMethodSpec = createMethodSpec("setTestInteger", PrimitiveType.VOID, roleMethodParameters, true);
        
        //Callout - baseMethodSpec
        FieldAccessSpec newFieldAcc = createFieldAccSpec("_integer", null, "Integer", true);
        
        //build CalloutMapping
        CalloutMappingDeclaration newCalloutMapping = createCalloutMapping(null, 0, roleMethodSpec, newFieldAcc, null, false, true);
        newCalloutMapping.bindingOperator().setBindingModifier(Modifier.OT_SET_CALLOUT);
        
        newCalloutMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "void setTestInteger(Integer i) -> set Integer _integer;";
      
        assertEquals("Wrong CalloutMappingCode", expected, actual);
    }
    
    public void testNewCallinMappingCreation()
    {
        int callinMappingModifier = Modifier.OT_REPLACE_CALLIN;
        
        //Callin - roleMethodSpec - Parameter
        SingleVariableDeclaration roleMethodSpecParameter = 
            createMethodParameter(0, "Integer", null, "roleInteger", 0, null);
        List roleMethodParameters = new ArrayList();
        roleMethodParameters.add(roleMethodSpecParameter);
        
        //Callin - roleMethodSpec
        MethodSpec roleMethodSpec = createMethodSpec("roleMethod1", PrimitiveType.INT, roleMethodParameters, true);
        
        //Callin - baseMethodSpec - Parameter
        SingleVariableDeclaration baseMethodSpecParameter = 
            createMethodParameter(0, "Integer", null, "integer", 0, null);
        List baseMethodParameters = new ArrayList();
        baseMethodParameters.add(baseMethodSpecParameter);
        
        //Callin - baseMethodSpec(s)
        MethodSpec baseMethodSpec = createMethodSpec("baseMethod1", PrimitiveType.INT, baseMethodParameters, true);
        List baseMethods = new ArrayList();
        baseMethods.add(baseMethodSpec);
        
        //Callin - ParameterMapping
        SimpleName expressionName1 = (SimpleName)createExpression("integer");
        ParameterMapping newParameterMappingInt = 
            createParameterMapping(expressionName1, "roleInteger", "<-", false);
        
        SimpleName expressionName2 = (SimpleName)createExpression("result");
        ParameterMapping newParameterMappingRes = 
            createParameterMapping(expressionName2, "result", "->", true);
        List paramMappings = new ArrayList();
        paramMappings.add(newParameterMappingInt);
        paramMappings.add(newParameterMappingRes);
        
        //build CallinMapping
        CallinMappingDeclaration newCallinMapping = createCallinMappingDeclaration(null, callinMappingModifier, roleMethodSpec, baseMethods, paramMappings);
        
        newCallinMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = 
        	"int roleMethod1(Integer roleInteger) <- replace int baseMethod1(Integer integer) with {\n"+
        	"    roleInteger <- integer,\n"+ 
        	"    result -> result\n" +
        	"}";
        
        assertEquals("Wrong CallinMappingCode", expected, actual);
    }
    
    public void testNewCallinMappingCreation1()
    {
        //Callin - roleMethodSpec
        MethodSpec roleMethodSpec = createMethodSpec("roleMethod2", PrimitiveType.INT, null, false);
        
        //Callin - baseMethodSpec(s)
        MethodSpec baseMethodSpec = createMethodSpec("baseMethod2", PrimitiveType.INT, null, false);
        List baseMethods = new ArrayList();
        baseMethods.add(baseMethodSpec);
        
        //build CallinMapping
        CallinMappingDeclaration newCallinMapping = createCallinMappingDeclaration(null, Modifier.OT_BEFORE_CALLIN, roleMethodSpec, baseMethods, null);
        
        newCallinMapping.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "roleMethod2 <- before baseMethod2;";
        
        assertEquals("Wrong CallinMappingCode", expected, actual);
    }
    
    public void testNewFieldAccSpecCreation()
    {
        FieldAccessSpec newFieldAcc = createFieldAccSpec("_string", PrimitiveType.VOID, null, false);
        
        newFieldAcc.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "_string";
        
        assertEquals("Wrong FieldAccSpecCode", expected, actual);
    }
    
    public void testNewFieldAccSpecCreation1()
    {
        FieldAccessSpec newFieldAcc = createFieldAccSpec("_integer", null,"Integer", true);
        
        newFieldAcc.accept(_rewriteFlattener);
        
        String actual = _rewriteFlattener.getResult();
        String expected = "Integer _integer";
        
        assertEquals("Wrong FieldAccSpecCode", expected, actual);
    }
    
    public void testNewFullyCuCreation()
    {
        PackageDeclaration newPack = _newAst.newPackageDeclaration();
        newPack.setName(_newAst.newQualifiedName(_newAst.newSimpleName("testmain"), _newAst.newSimpleName("testsub")));
        
        ImportDeclaration newImport = _newAst.newImportDeclaration();
        newImport.setName(_newAst.newQualifiedName(_newAst.newSimpleName("testpackage"), _newAst.newSimpleName("TestClass")));
        newImport.setOnDemand(false);
        List importList = new ArrayList();
        importList.add(newImport);
        
        
        int callinMappingModifier = Modifier.OT_REPLACE_CALLIN;
        
        //Callin - roleMethodSpec - Parameter
        SingleVariableDeclaration roleMethodSpecParameter = 
            createMethodParameter(0, "Integer", null, "roleInteger", 0, null);
        List roleMethodParameters = new ArrayList();
        roleMethodParameters.add(roleMethodSpecParameter);
        
        //Callin - roleMethodSpec
        MethodSpec roleMethodSpec = createMethodSpec("roleMethod1", PrimitiveType.INT, roleMethodParameters, true);
        
        //Callin - baseMethodSpec - Parameter
        SingleVariableDeclaration baseMethodSpecParameter = 
            createMethodParameter(0, "Integer", null, "integer", 0, null);
        List baseMethodParameters = new ArrayList();
        baseMethodParameters.add(baseMethodSpecParameter);
        
        //Callin - baseMethodSpec(s)
        MethodSpec baseMethodSpec = createMethodSpec("baseMethod1", PrimitiveType.INT, baseMethodParameters, true);
        List baseMethods = new ArrayList();
        baseMethods.add(baseMethodSpec);
        
        //Callin - ParameterMapping
        SimpleName expressionName1 = (SimpleName)createExpression("integer");
        ParameterMapping newParameterMappingInt = 
            createParameterMapping(expressionName1, "roleInteger", "<-", false);
        
        SimpleName expressionName2 = (SimpleName)createExpression("result");
        ParameterMapping newParameterMappingRes = 
            createParameterMapping(expressionName2, "result", "->", true);
        List paramMappings = new ArrayList();
        paramMappings.add(newParameterMappingInt);
        paramMappings.add(newParameterMappingRes);
        
        //build CallinMapping
        CallinMappingDeclaration newCallinMapping = createCallinMappingDeclaration(null, callinMappingModifier, roleMethodSpec, baseMethods, paramMappings);
        List roleBodyDecl = new ArrayList();
        roleBodyDecl.add(newCallinMapping);
        
        RoleTypeDeclaration role = createRole(null, 1, "GeneratedRoleClass", "GeneratedBaseClass", 
                "GeneratedTeamClass", null, null , false, roleBodyDecl);
        List typeBodyDecl = new ArrayList();
        typeBodyDecl.add(role);
        
        TypeDeclaration newTypeDecl = createTeam(null, (Modifier.PUBLIC | Modifier.OT_TEAM), 
                false, false, "GeneratedClass", null, null, typeBodyDecl);
        List typeList = new ArrayList();
        typeList.add(newTypeDecl);

        CompilationUnit newCU = createCU(newPack, importList, typeList);
        
        newCU.accept(_rewriteFlattener);

        String cUString ="package testmain.testsub;import testpackage.TestClass;public team class GeneratedClass {";
        String roleString = "public class GeneratedRoleClass playedBy GeneratedBaseClass {";
        String bodyDeclString =  "int roleMethod1(Integer roleInteger) <- replace int baseMethod1(Integer integer)";
        String paramMappingString = 
        	" with {\n" +
        	"    roleInteger <- integer,\n" +
        	"    result -> result\n" +
        	"}}}";
        
        String actual = _rewriteFlattener.getResult();
        String expected =  cUString + roleString + bodyDeclString + paramMappingString;
        
        assertEquals("Wrong CU-Code", expected, actual);
    }
    
    
    //  form here there are only HELPER - METHODS
    
    private CompilationUnit createCU(PackageDeclaration pack, List importList, List typeList)
    {
        CompilationUnit newCU = _newAst.newCompilationUnit();
        
        if (pack != null)
            newCU.setPackage(pack);
        
        if (importList != null && importList.size()!= 0)
        {
            List cuImportList = newCU.imports();
            for (int idx = 0; idx < importList.size(); idx++)
            {
                ImportDeclaration tmp =  (ImportDeclaration)importList.get(idx);
                cuImportList.add(tmp);                
            }
        }
        
        if (typeList != null && typeList.size()!= 0)
        {
            List cuTypeList = newCU.types();
            for (int idx = 0; idx < typeList.size(); idx++)
            {
                TypeDeclaration tmp =  (TypeDeclaration)typeList.get(idx);
                cuTypeList.add(tmp);                
            }
        }
        return newCU;
    }
    
    private TypeDeclaration createTeam(Javadoc javadoc, int modifiers, boolean isInterface, boolean isRole,
            String teamClassName, String superClassName, List superInterfaces, List bodyDeclarations)
    {
        TypeDeclaration newTypeDecl = _newAst.newTypeDeclaration();
        newTypeDecl.setName(_newAst.newSimpleName(teamClassName));
        newTypeDecl.setTeam(true);
        newTypeDecl.modifiers().addAll(_newAst.newModifiers(modifiers));
        newTypeDecl.setRole(isRole);
        newTypeDecl.setInterface(isInterface);

        if (javadoc != null)
            newTypeDecl.setJavadoc(javadoc);

        if (superClassName != null) 
            newTypeDecl.setSuperclass(_newAst.newSimpleName(superClassName));
        
        if (superInterfaces != null && superInterfaces.size()!= 0)
        {
            List superInterfacesList = newTypeDecl.superInterfaces();
            for (int idx = 0; idx < superInterfaces.size(); idx++)
            {
                SimpleName tmp =  (SimpleName)superInterfaces.get(idx);
                superInterfacesList.add(tmp);                
            }
        }

        if (bodyDeclarations != null && bodyDeclarations.size()!= 0)
        {
            List bodyDeclarationList = newTypeDecl.bodyDeclarations();
            for (int idx = 0; idx < bodyDeclarations.size(); idx++)
            {
                Object tmp =  bodyDeclarations.get(idx);
                bodyDeclarationList.add(tmp);                
            }
        }
        return newTypeDecl;
    }
    
    private RoleTypeDeclaration createRole(Javadoc javadoc, int modifier, String roleName, String baseClassName, 
            String teamClassName, String superClassName, List superInterfaces, boolean isTeam,
            List bodyDeclarations)
    {
        RoleTypeDeclaration role = _newAst.newRoleTypeDeclaration();
        
        role.modifiers().addAll(_newAst.newModifiers(modifier));
        role.setName(_newAst.newSimpleName(roleName));
        role.setTeamClassType(_newAst.newSimpleType((_newAst.newSimpleName(teamClassName))));
        role.setTeam(isTeam);
        role.setRole(true);        

        if (javadoc != null)
            role.setJavadoc(javadoc);
        
        if (baseClassName != null) 
            role.setBaseClassType(_newAst.newSimpleType(_newAst.newSimpleName(baseClassName)));
        
        if (superClassName != null) 
            role.setSuperclass(_newAst.newSimpleName(superClassName));
        
        if (superInterfaces != null && superInterfaces.size()!= 0)
        {
            List superInterfacesList = role.superInterfaceTypes();
            for (int idx = 0; idx < superInterfaces.size(); idx++)
            {
                SimpleName tmp =  (SimpleName)superInterfaces.get(idx);
                superInterfacesList.add(tmp);                
            }
        }

        if (bodyDeclarations != null && bodyDeclarations.size()!= 0)
        {
            List bodyDeclarationList = role.bodyDeclarations();
            for (int idx = 0; idx < bodyDeclarations.size(); idx++)
            {
                Object tmp =  bodyDeclarations.get(idx);
                bodyDeclarationList.add(tmp);                
            }
        }
        return role;
    }

    private CallinMappingDeclaration createCallinMappingDeclaration
    (Javadoc javadoc, int callinModifier, MethodSpec roleMethod, List baseMethods, List paramMappings)
    {
        CallinMappingDeclaration newCallinMapping = _newAst.newCallinMappingDeclaration();
        newCallinMapping.setCallinModifier(callinModifier);
        newCallinMapping.setRoleMappingElement(roleMethod);
        
        if (javadoc != null)
            newCallinMapping.setJavadoc(javadoc);
        
        if (baseMethods != null && baseMethods.size()!= 0)
        {
            List baseMappingList = newCallinMapping.getBaseMappingElements();
            for (int idx = 0; idx < baseMethods.size(); idx++)
            {
                MethodSpec tmp = (MethodSpec)baseMethods.get(idx);
                baseMappingList.add(tmp);
            }
        }
        if (paramMappings != null && paramMappings.size()!= 0)
        {
            List parameterMappingList = newCallinMapping.getParameterMappings();
            for (int idx = 0; idx < paramMappings.size(); idx++)
            {
                ParameterMapping tmp =  (ParameterMapping)paramMappings.get(idx);
                parameterMappingList.add(tmp);                
            }
        }
        return newCallinMapping;
    }
    
    private CalloutMappingDeclaration createCalloutMapping(Javadoc javadoc, int modifier, MethodSpec roleMethod,
            MethodMappingElement baseMethod, List paramMappings, boolean calloutOverride, boolean signatureFlag)
    {
        CalloutMappingDeclaration newCalloutMapping = _newAst.newCalloutMappingDeclaration();
        
        if (javadoc != null)
            newCalloutMapping.setJavadoc(javadoc);
        
        newCalloutMapping.modifiers().addAll(_newAst.newModifiers(modifier));
        newCalloutMapping.setSignatureFlag(signatureFlag);
        newCalloutMapping.setRoleMappingElement(roleMethod);
        newCalloutMapping.setBaseMappingElement(baseMethod);
        if (calloutOverride)
        	newCalloutMapping.bindingOperator().setBindingKind(MethodBindingOperator.KIND_CALLOUT_OVERRIDE);
        
        if (paramMappings != null && paramMappings.size()!= 0)
        {
            List parameterMapping = newCalloutMapping.getParameterMappings();
            for (int idx = 0; idx < paramMappings.size(); idx++)
            {
                ParameterMapping tmp =  (ParameterMapping)paramMappings.get(idx);
                parameterMapping.add(tmp);                
            }
        }
        return newCalloutMapping;
    }
    
    private FieldAccessSpec createFieldAccSpec(String fieldName, PrimitiveType.Code simpleType, String type, boolean hasSignature)
    {
        FieldAccessSpec newFieldAcc = _newAst.newFieldAccessSpec();
        newFieldAcc.setName(_newAst.newSimpleName(fieldName));
        
        if (simpleType!= null)
            newFieldAcc.setFieldType(_newAst.newPrimitiveType(simpleType));
            
        if (type!= null)
            newFieldAcc.setFieldType(_newAst.newSimpleType(_newAst.newSimpleName(type)));
        
        newFieldAcc.setSignatureFlag(hasSignature);
        return newFieldAcc;
    }

    private MethodSpec createMethodSpec(String methodName, PrimitiveType.Code returnType, List parameters, boolean signatureFlag)
    {
        MethodSpec methodSpec = _newAst.newMethodSpec();
        methodSpec.setName(_newAst.newSimpleName(methodName));
        methodSpec.setReturnType2(_newAst.newPrimitiveType(returnType));
        methodSpec.setSignatureFlag(signatureFlag);
        
        if (parameters!=null && parameters.size()!=0)
        {
            List methodParameters = methodSpec.parameters();
            for (int idx = 0; idx < parameters.size(); idx++)
            {
                VariableDeclaration tmp =  (VariableDeclaration)parameters.get(idx);
                methodParameters.add(tmp);                
            }
        }
        return methodSpec;
    }
    
    private SingleVariableDeclaration createMethodParameter(int modifier, String parameterType, PrimitiveType.Code primitiveType, String parameterName, int dimension,
            Expression initializer)
    {
        SingleVariableDeclaration methodSpecParameter = _newAst.newSingleVariableDeclaration();
        methodSpecParameter.modifiers().addAll(_newAst.newModifiers(modifier));
        
        if(primitiveType!= null)
        {
            PrimitiveType primitiveRoleParameterType = _newAst.newPrimitiveType(primitiveType);
            methodSpecParameter.setType(primitiveRoleParameterType);
        }
        
        if (parameterType!= null)
        {
            SimpleType simpleRoleParameterType = _newAst.newSimpleType(_newAst.newSimpleName(parameterType));
            methodSpecParameter.setType(simpleRoleParameterType);
        }
        
        methodSpecParameter.setName(_newAst.newSimpleName(parameterName));
        methodSpecParameter.setExtraDimensions(dimension);
        methodSpecParameter.setInitializer(initializer);
        return methodSpecParameter;
    }
    
    private ParameterMapping createParameterMapping(Expression expression, String identName,
            String direction, boolean resultFlag)
    {
        ParameterMapping newParameterMapping = _newAst.newParameterMapping();
        newParameterMapping.setExpression(expression);
        newParameterMapping.setDirection(direction);
        newParameterMapping.setIdentifier(_newAst.newSimpleName(identName));
        newParameterMapping.setResultFlag(resultFlag);
        return newParameterMapping;
    }
    
    private Expression createExpression(String expressionName, String methodName)
    {
        MethodInvocation expression = _newAst.newMethodInvocation();
        expression.setExpression(_newAst.newSimpleName(expressionName));
        expression.setName(_newAst.newSimpleName(methodName));
        return expression;
    }

    private Expression createExpression(String expressionName)
    {
        Expression expression= _newAst.newSimpleName(expressionName);
        return expression;
    }
    
    private Expression createExpression(Expression innerExpression, String className,
            List arguments, AnonymousClassDeclaration anonymousClass)
    {
        ClassInstanceCreation expression = _newAst.newClassInstanceCreation();
        expression.setType(_newAst.newSimpleType(_newAst.newName(className)));
        expression.setExpression(innerExpression);
        expression.setAnonymousClassDeclaration(anonymousClass);

        if (arguments!=null && arguments.size()!=0)
        {
            List classArguments = expression.arguments();
            for (int idx = 0; idx < arguments.size(); idx++)
            {
                Expression tmp =  (Expression)arguments.get(idx);
                classArguments.add(tmp);                
            }
        }
        return expression;
    }
}