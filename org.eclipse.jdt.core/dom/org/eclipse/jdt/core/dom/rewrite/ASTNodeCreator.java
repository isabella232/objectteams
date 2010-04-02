/*
 * Created on 01.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.jdt.core.dom.rewrite;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.GuardPredicateDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

/**
 * New for OTDT.
 * 
 * @author ikeman
 *
 * This class provides helperMethods for the creation of DOMAST-Nodes. 
 *
 * @version $Id: ASTNodeCreator.java 22581 2009-09-24 10:26:48Z stephan $
 */
@SuppressWarnings("unchecked") // assigning from parameterless lists :(
public class ASTNodeCreator
{
    
    public static CompilationUnit createCU(AST ast, PackageDeclaration pack, List importList, List typeList)
    {
        CompilationUnit newCU = ast.newCompilationUnit();
        
        if (pack != null)
            newCU.setPackage(pack);
        
        if (importList != null && importList.size()!= 0)
        {
            List<ImportDeclaration> cuImportList = newCU.imports();
            for (int idx = 0; idx < importList.size(); idx++)
            {
                ImportDeclaration tmp =  (ImportDeclaration)importList.get(idx);
                cuImportList.add(tmp);                
            }
        }
        
        if (typeList != null && typeList.size()!= 0)
        {
            List<TypeDeclaration> cuTypeList = newCU.types();
            for (int idx = 0; idx < typeList.size(); idx++)
            {
                TypeDeclaration tmp =  (TypeDeclaration)typeList.get(idx);
                cuTypeList.add(tmp);                
            }
        }
        return newCU;
    }
 
 /* km: not needed anymore? 
    public static TypeDeclaration createTeam(AST ast, Javadoc javadoc, int modifiers, boolean isInterface, boolean isRole,
            String teamClassName, String superClassName, List superInterfaces, List bodyDeclarations)
    {
        TypeDeclaration newTypeDecl = ast.newTypeDeclaration();
        newTypeDecl.setName(ast.newSimpleName(teamClassName));
        newTypeDecl.setTeam(true);
        if(ast.apiLevel() == AST.JLS2)
        	newTypeDecl.setModifiers(modifiers);
        else 
        	newTypeDecl.modifiers().addAll(ast.newModifiers(modifiers));
        
        newTypeDecl.setRole(isRole);
        newTypeDecl.setInterface(isInterface);

        if (javadoc != null)
            newTypeDecl.setJavadoc(javadoc);

        if(ast.apiLevel() == AST.JLS2) {
	        if (superClassName != null) 
	            newTypeDecl.setSuperclass(ast.newName(superClassName));
	        
	        if (superInterfaces != null && superInterfaces.size()!= 0)
	        {
	            List superInterfacesList = newTypeDecl.superInterfaces();
	            for (int idx = 0; idx < superInterfaces.size(); idx++)
	            {
	                SimpleName tmp =  (SimpleName)superInterfaces.get(idx);
	                superInterfacesList.add(tmp);                
	            }
	        }
        }
        else {
        	
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
 
    
    public static RoleTypeDeclaration createRole(AST ast, Javadoc javadoc, int modifier, String roleName, String baseClassName, 
            String teamClassName, String superClassName, List superInterfaces, boolean isTeam,
            List bodyDeclarations)
    {
        RoleTypeDeclaration role = ast.newRoleTypeDeclaration();
        
        if(ast.apiLevel() == AST.JLS2)
        	role.setModifiers(modifier);
        else 
        	role.modifiers().addAll(ast.newModifiers(modifier));
        
        role.setName(ast.newName(roleName));
        role.setTeamClass(ast.newName(teamClassName));
        role.setTeam(isTeam);
        role.setRole(true);        

        if (javadoc != null)
            role.setJavadoc(javadoc);
        
        if (baseClassName != null) 
            role.setBaseClass(ast.newName(baseClassName));
        
        if (superClassName != null) 
            role.setSuperclass(ast.newName(superClassName));
        
        if (superInterfaces != null && superInterfaces.size()!= 0)
        {
            List superInterfacesList = role.superInterfaces();
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
*/
    public static CallinMappingDeclaration createCallinMappingDeclaration
    (AST ast, Javadoc javadoc, int modifier, MethodSpec roleMethod, List baseMethods, List paramMappings)
    {
        CallinMappingDeclaration newCallinMapping = ast.newCallinMappingDeclaration();
        newCallinMapping.setCallinModifier(modifier);
        
        newCallinMapping.setRoleMappingElement(roleMethod);
        
        if (javadoc != null)
            newCallinMapping.setJavadoc(javadoc);
        
        if (baseMethods != null && baseMethods.size()!= 0)
        {
            List<MethodSpec> baseMappingList = newCallinMapping.getBaseMappingElements();
            for (int idx = 0; idx < baseMethods.size(); idx++)
            {
                MethodSpec tmp = (MethodSpec)baseMethods.get(idx);
                baseMappingList.add(tmp);
            }
        }
        if (paramMappings != null && paramMappings.size()!= 0)
        {
            List<ParameterMapping> parameterMappingList = newCallinMapping.getParameterMappings();
            while(!paramMappings.isEmpty())
            {
                ParameterMapping tmp =  (ParameterMapping)paramMappings.remove(0);
                parameterMappingList.add(tmp);                
            }
        }
        return newCallinMapping;
    }
    
    @SuppressWarnings("deprecation")
	public static CalloutMappingDeclaration createCalloutMappingDeclaration(
                       AST ast, 
                       Javadoc javadoc, 
                       int modifier, 
                       MethodMappingElement roleMethod,
                       MethodMappingElement baseMethod,
                       int bindingModifier,
                       List paramMappings, 
                       boolean calloutOverride, 
                       boolean signatureFlag)
    {
        CalloutMappingDeclaration newCalloutMapping = ast.newCalloutMappingDeclaration();
        
        if (javadoc != null)
            newCalloutMapping.setJavadoc(javadoc);
        
        if(ast.apiLevel() == AST.JLS2)
       		newCalloutMapping.setModifiers(modifier);
        else 
        	newCalloutMapping.modifiers().addAll(ast.newModifiers(modifier));

        
        newCalloutMapping.setSignatureFlag(signatureFlag);
        newCalloutMapping.setRoleMappingElement(roleMethod);
        newCalloutMapping.setBaseMappingElement(baseMethod);
        
        ModifierKeyword keyword = null;
        switch (bindingModifier) {
        case Modifier.OT_SET_CALLOUT: keyword = ModifierKeyword.SET_KEYWORD; break;
        case Modifier.OT_GET_CALLOUT: keyword = ModifierKeyword.GET_KEYWORD; break;
        }
        int calloutKind = calloutOverride ? MethodBindingOperator.KIND_CALLOUT_OVERRIDE : MethodBindingOperator.KIND_CALLOUT;
        newCalloutMapping.setBindingOperator(ast.newMethodBindingOperator(keyword, calloutKind));
        
        if (paramMappings != null && paramMappings.size()!= 0)
        {
            List<ParameterMapping> parameterMapping = newCalloutMapping.getParameterMappings();
            while(!paramMappings.isEmpty())
            {
                ParameterMapping tmp =  (ParameterMapping)paramMappings.remove(0);
                parameterMapping.add(tmp);                
            }
        }
        return newCalloutMapping;
    }
    
    public static FieldAccessSpec createFieldAccSpec(AST ast, boolean isSetter, String fieldName, PrimitiveType.Code simpleType, String type, boolean hasSignature)
    {
        FieldAccessSpec newFieldAcc = ast.newFieldAccessSpec();
        newFieldAcc.setName(ast.newSimpleName(fieldName));
        
        if (simpleType!= null)
            newFieldAcc.setFieldType(ast.newPrimitiveType(simpleType));
            
        if (type!= null)
            newFieldAcc.setFieldType(ast.newSimpleType(ast.newName(type)));
        
        newFieldAcc.setSignatureFlag(hasSignature);
        return newFieldAcc;
    }
    public static FieldAccessSpec createFieldAccSpec(AST ast, String fieldName, Type type) {
    	FieldAccessSpec newFieldAcc= ast.newFieldAccessSpec();
    	newFieldAcc.setName(ast.newSimpleName(fieldName));
    	newFieldAcc.setFieldType(type);
    	newFieldAcc.setSignatureFlag(true);
    	return newFieldAcc;
    }
    public static MethodSpec createMethodSpec(AST ast, String methodName, String returnType, List<String> argumentNames, List<String> argumentTypes, List<Integer> dimensions, boolean signatureFlag)
    {
        MethodSpec methodSpec = ast.newMethodSpec();
        methodSpec.setName(ast.newSimpleName(methodName));
        methodSpec.setReturnType2(createType(ast, returnType));
        methodSpec.setSignatureFlag(signatureFlag);
        
        if (argumentTypes !=null && argumentTypes.size()!=0)
        {
            List<VariableDeclaration> methodParameters = methodSpec.parameters();
            for (int idx = 0; idx < argumentTypes.size(); idx++)
            {
            	String argumentName = (argumentNames != null)
            						  ? argumentNames.get(idx)
            						  : "arg"+idx; //$NON-NLS-1$
                VariableDeclaration tmp = createArgument(ast, 0, createType(ast, argumentTypes.get(idx)), argumentName, dimensions.get(idx), null);
                methodParameters.add(tmp);                
            }
        }
        return methodSpec;
    }

    public static MethodSpec createMethodSpec(AST ast, String methodName, Type returnType, List parameters, boolean signatureFlag)
    {
        MethodSpec methodSpec = ast.newMethodSpec();
        methodSpec.setName(ast.newSimpleName(methodName));
        methodSpec.setSignatureFlag(signatureFlag);        
        methodSpec.setReturnType2(returnType);
        
        if (parameters!=null && parameters.size()!=0)
        {
            List<VariableDeclaration> methodParameters = methodSpec.parameters();
            for (int idx = 0; idx < parameters.size(); idx++)
            {
                VariableDeclaration tmp =  (VariableDeclaration)parameters.get(idx);
                methodParameters.add(tmp);                
            }
        }
        return methodSpec;
    }

    public static GuardPredicateDeclaration createGuardPredicate(AST ast, boolean isBase, Expression expression) {
		GuardPredicateDeclaration result = ast.newGuardPredicateDeclaration();
		result.setBase(isBase);
		result.setExpression(expression);
		return result;
	}

	public static Type createType(AST ast, String typeName)
    {
        Type primType = getPrimitveType(ast, typeName);
        if (primType != null)
        return primType;
        
        if (typeName.endsWith("[]")) //$NON-NLS-1$
        {
            String [] name = typeName.split("\\[\\]");  //$NON-NLS-1$
            if (name[0] != null)
            {
                Type primitiveType = getPrimitveType(ast, name[0]);
                if (primitiveType!= null)
                    return ast.newArrayType(primitiveType);
                else
                {
                    Name nameNode = ast.newName(name[0]);
                    Type simpleType  = ast.newSimpleType(nameNode);
                    return ast.newArrayType(simpleType);
                }
            }
        }
        
        Name name = ast.newName(typeName);
        Type simpleType  = ast.newSimpleType(name);
        
        return simpleType;
    }
    
    private static Type getPrimitveType(AST ast, String typeName)
    {
        if (typeName.equals(PrimitiveType.BOOLEAN.toString()))
            return ast.newPrimitiveType(PrimitiveType.BOOLEAN);            

        if (typeName.equals(PrimitiveType.BYTE.toString()))
            return ast.newPrimitiveType(PrimitiveType.BYTE);            
     
        if (typeName.equals(PrimitiveType.CHAR.toString()))
            return ast.newPrimitiveType(PrimitiveType.CHAR);            

        if (typeName.equals(PrimitiveType.DOUBLE.toString()))
            return ast.newPrimitiveType(PrimitiveType.DOUBLE);            

        if (typeName.equals(PrimitiveType.FLOAT.toString()))
            return ast.newPrimitiveType(PrimitiveType.FLOAT);            

        if (typeName.equals(PrimitiveType.INT.toString()))
            return ast.newPrimitiveType(PrimitiveType.INT);            

        if (typeName.equals(PrimitiveType.LONG.toString()))
            return ast.newPrimitiveType(PrimitiveType.LONG);            

        if (typeName.equals(PrimitiveType.SHORT.toString()))
            return ast.newPrimitiveType(PrimitiveType.SHORT);            

        if (typeName.equals(PrimitiveType.VOID.toString()))
            return ast.newPrimitiveType(PrimitiveType.VOID);
        
        return null;
    }
    
    @SuppressWarnings("deprecation")
	public static SingleVariableDeclaration createArgument(AST ast, int modifier, Type parameterType, String parameterName, int dimension,
            Expression initializer)
    {
        SingleVariableDeclaration methodSpecParameter = ast.newSingleVariableDeclaration();
        if(ast.apiLevel() == AST.JLS2)
        	methodSpecParameter.setModifiers(modifier);
        else 
        	methodSpecParameter.modifiers().addAll(ast.newModifiers(modifier));
        
        methodSpecParameter.setType(parameterType);
        methodSpecParameter.setName(ast.newSimpleName(parameterName));
        methodSpecParameter.setExtraDimensions(dimension);
        methodSpecParameter.setInitializer(initializer);
        return methodSpecParameter;
    }
    
    public static ParameterMapping createParameterMapping(AST ast, Expression expression, SimpleName identName,
            String direction, boolean resultFlag)
    {
        ParameterMapping newParameterMapping = ast.newParameterMapping();
        newParameterMapping.setExpression(expression);
        newParameterMapping.setDirection(direction);
        newParameterMapping.setIdentifier(identName);
        newParameterMapping.setResultFlag(resultFlag);
        return newParameterMapping;
    }
    
    public static Expression createExpression(AST ast, String expressionName, String methodName)
    {
        MethodInvocation expression = ast.newMethodInvocation();
        expression.setExpression(ast.newSimpleName(expressionName));
        expression.setName(ast.newSimpleName(methodName));
        return expression;
    }
    
    
    /**
     * Parses the source string of an expression and returns a dom ast expression,
     * that belongs to the given ast, or <code>null</code> if something goes wrong.
     */
    public static Expression createExpression(AST ast, String expr)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_EXPRESSION);
        parser.setSource(expr.toCharArray());
        ASTNode parsedNode = parser.createAST(null);
        if (parsedNode instanceof Expression)
        {
            Expression exprNode = (Expression)parsedNode;

            // copy the parsed expression node to the current AST
            Expression newExprNode = (Expression)ASTNode.copySubtree(ast, exprNode);
            return newExprNode;
        }
        else
        {
            return null;
        }
    }

    public static Expression createExpression(AST ast, Expression innerExpression, String className,
            List arguments, AnonymousClassDeclaration anonymousClass)
    {
        ClassInstanceCreation expression = ast.newClassInstanceCreation();
        expression.setType(ast.newSimpleType(ast.newName(className)));
        expression.setExpression(innerExpression);
        expression.setAnonymousClassDeclaration(anonymousClass);

        if (arguments!=null && arguments.size()!=0)
        {
            List<Expression> classArguments = expression.arguments();
            for (int idx = 0; idx < arguments.size(); idx++)
            {
                Expression tmp =  (Expression)arguments.get(idx);
                classArguments.add(tmp);                
            }
        }
        return expression;
    }
}

