/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NEW for OTDT, built in analogy to TypeDeclaration (its superclass)
 *
 *
 * Represents the DOM-ASTNode for a SubType of TypeDeclaration. This node represents a role in a 
 * compilation unit (a "role file"). 
 * 
 * Contained AST elements: 
 * RoleTypeDeclaration is a Subtyp of TypeDeclaration, so it contains all properties and methods from
 * TypeDeclaration, like e.g. TEAM-PROPERTY and getRoles()-Method. This is mandatory, because a role can also
 * be a team see <a href="#codeexample1">CodeExample1</a>.<br>
 * RoleTypeDeclaration contains also rolespecific properties:
 * <dl>
 * <dt>1. BaseProperty    <dd> adapted baseclass -> TypeDeclaration
 * <dt>2. TeamProperty    <dd> Team, which includes this Role -> TypeDeclaration
 * <dt>3. CallinProperty  <dd> a list of all callins which exist inside a role -> List
 * <dt>4. CalloutProperty <dd> a list of all callouts which exist inside a role -> List
 * </dl> 
 * 
 * 
 * Locations in source code:
 * This node is used in BodyDeclaration. 
 * 
 * TODO(ike): javadoc is not completed yet... 
 * 
 * <a name="codeexample1">CodeExample1</a>
 * <pre>
 * public team MyTeam1 {
 *     public team MyRole playedBy MyBaseClass {
 *     }
 * }
 * </pre>
 * @author ike
 */
public class RoleTypeDeclaration extends TypeDeclaration {

    /**
     * The "javadoc" structural property of this node type.
     * @since 3.0
     */
    public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
        internalJavadocPropertyFactory(RoleTypeDeclaration.class);

    /**
     * The "modifiers" structural property of this node type (JLS2 API only).
     * @since 3.0
     * @deprecated Replaced by {@link #MODIFIERS2_PROPERTY} in the JLS3 API.
     */
    public static final SimplePropertyDescriptor MODIFIERS_PROPERTY = 
        internalModifiersPropertyFactory(RoleTypeDeclaration.class);
    
    /**
     * The "modifiers" structural property of this node type (added in JLS3 API).
     * @since 3.0
     */
    public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY = 
        internalModifiers2PropertyFactory(RoleTypeDeclaration.class);

    /**
     * The "interface" structural property of this node type.
     * @since 3.0
     */
    public static final SimplePropertyDescriptor INTERFACE_PROPERTY = 
        new SimplePropertyDescriptor(RoleTypeDeclaration.class, "interface", boolean.class, MANDATORY); //$NON-NLS-1$

    /**
     * The "team" structural property of this node type.
     */
    public static final SimplePropertyDescriptor TEAM_PROPERTY = 
        new SimplePropertyDescriptor(RoleTypeDeclaration.class, "team", boolean.class, MANDATORY); //$NON-NLS-1$

    /**
     * The "role" structural property of this node type.
     */
    public static final SimplePropertyDescriptor ROLE_PROPERTY = 
        new SimplePropertyDescriptor(RoleTypeDeclaration.class, "role", boolean.class, MANDATORY);   //$NON-NLS-1$

    /**
     * The "rolefile" structural property of this node type.
     */
    public static final SimplePropertyDescriptor ROLE_FILE_PROPERTY = 
        new SimplePropertyDescriptor(RoleTypeDeclaration.class, "rolefile", boolean.class, MANDATORY);   //$NON-NLS-1$

    /**
     * The "BaseClass" structural property of this node type.
     * @since 3.0
     * @deprecated Replaced by {@link #BASECLASS_TYPE_PROPERTY} in the JLS3 API.
     */
    public static final ChildPropertyDescriptor BASECLASS_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "baseClass", Name.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
    
    /**
     * The "baseClassType" structural property of this node type (added in JLS3 API).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor BASECLASS_TYPE_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "baseClassType", Type.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

    /**
     * The "BaseClass" structural property of this node type.
     * @since 3.0
     * @deprecated Replaced by {@link #TEAMCLASS_TYPE_PROPERTY} in the JLS3 API.
     */
    public static final ChildPropertyDescriptor TEAMCLASS_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "teamClass", Name.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
    
    /**
     * The "baseClassType" structural property of this node type (added in JLS3 API).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor TEAMCLASS_TYPE_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "teamClassType", Type.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
   
    /**
     * The "name" structural property of this node type.
     * @since 3.0
     */
    public static final ChildPropertyDescriptor NAME_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

    /**
     * The "superclass" structural property of this node type (JLS2 API only).
     * @since 3.0
     * @deprecated Replaced by {@link #SUPERCLASS_TYPE_PROPERTY} in the JLS3 API.
     */
    public static final ChildPropertyDescriptor SUPERCLASS_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "superclass", Name.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

    /**
     * The "superInterfaces" structural property of this node type (JLS2 API only).
     * @since 3.0
     * @deprecated Replaced by {@link #SUPER_INTERFACE_TYPES_PROPERTY} in the JLS3 API.
     */
    public static final ChildListPropertyDescriptor SUPER_INTERFACES_PROPERTY = 
        new ChildListPropertyDescriptor(RoleTypeDeclaration.class, "superInterfaces", Name.class, NO_CYCLE_RISK); //$NON-NLS-1$

    /**
     * The "superclassType" structural property of this node type (added in JLS3 API).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor SUPERCLASS_TYPE_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "superclassType", Type.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

    /**
     * The "superInterfaceTypes" structural property of this node type (added in JLS3 API).
     * @since 3.0
     */
    public static final ChildListPropertyDescriptor SUPER_INTERFACE_TYPES_PROPERTY = 
        new ChildListPropertyDescriptor(RoleTypeDeclaration.class, "superInterfaceTypes", Type.class, NO_CYCLE_RISK); //$NON-NLS-1$
    
    /**
     * The "typeParameters" structural property of this node type (added in JLS3 API).
     * @since 3.0
     */
    public static final ChildListPropertyDescriptor TYPE_PARAMETERS_PROPERTY = 
        new ChildListPropertyDescriptor(RoleTypeDeclaration.class, "typeParameters", TypeParameter.class, NO_CYCLE_RISK); //$NON-NLS-1$
    
    /**
     * The "guardPredicate" structural property of this node type (added in JLS3 API).
     * @since 0.9.25
     */
    public static final ChildPropertyDescriptor GUARD_PROPERTY = 
        new ChildPropertyDescriptor(RoleTypeDeclaration.class, "guardPredicate", GuardPredicateDeclaration.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

    /**
     * The "bodyDeclarations" structural property of this node type (added in JLS3 API).
     * @since 3.0
     */
    public static final ChildListPropertyDescriptor BODY_DECLARATIONS_PROPERTY = 
        internalBodyDeclarationPropertyFactory(RoleTypeDeclaration.class);
    
	/**
	 * The "precedence" structural property.
	 * @since 0.9.24
	 */
	public static final ChildListPropertyDescriptor PRECEDENCE_PROPERTY =
		new ChildListPropertyDescriptor(RoleTypeDeclaration.class, "precedence", Name.class, NO_CYCLE_RISK); //$NON-NLS-1$

    
    /**
     * A list of property descriptors (element type: 
     * {@link StructuralPropertyDescriptor}),
     * or null if uninitialized.
     * @since 3.0
     */
    @SuppressWarnings("rawtypes")
	private static final List PROPERTY_DESCRIPTORS_2_0;
    
    /**
     * A list of property descriptors (element type: 
     * {@link StructuralPropertyDescriptor}),
     * or null if uninitialized.
     * @since 3.0
     */
    @SuppressWarnings("rawtypes")
	private static final List PROPERTY_DESCRIPTORS_3_0;


    
    static {
    	List<StructuralPropertyDescriptor> propertyList = new ArrayList<StructuralPropertyDescriptor>(14);
        createPropertyList(RoleTypeDeclaration.class, propertyList);
        addProperty(JAVADOC_PROPERTY, propertyList);
        addProperty(MODIFIERS_PROPERTY, propertyList);
        addProperty(INTERFACE_PROPERTY, propertyList);
        addProperty(TEAM_PROPERTY, propertyList);
        addProperty(ROLE_PROPERTY, propertyList);
        addProperty(BASECLASS_PROPERTY, propertyList);
        addProperty(TEAMCLASS_PROPERTY, propertyList);
        addProperty(NAME_PROPERTY, propertyList);
        addProperty(SUPERCLASS_PROPERTY, propertyList);
        addProperty(SUPER_INTERFACES_PROPERTY, propertyList);
        addProperty(GUARD_PROPERTY, propertyList);
        addProperty(BODY_DECLARATIONS_PROPERTY, propertyList);
		addProperty(PRECEDENCE_PROPERTY, propertyList);
        PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
        
        propertyList = new ArrayList<StructuralPropertyDescriptor>(16);
        createPropertyList(RoleTypeDeclaration.class, propertyList);
        addProperty(JAVADOC_PROPERTY, propertyList);
        addProperty(MODIFIERS2_PROPERTY, propertyList);
        addProperty(INTERFACE_PROPERTY, propertyList);
        addProperty(TEAM_PROPERTY, propertyList);
        addProperty(ROLE_PROPERTY, propertyList);
        addProperty(ROLE_FILE_PROPERTY, propertyList);
        addProperty(BASECLASS_TYPE_PROPERTY, propertyList);
        addProperty(TEAMCLASS_TYPE_PROPERTY, propertyList);
        addProperty(NAME_PROPERTY, propertyList);
        addProperty(TYPE_PARAMETERS_PROPERTY, propertyList);
        addProperty(SUPERCLASS_TYPE_PROPERTY, propertyList);
        addProperty(SUPER_INTERFACE_TYPES_PROPERTY, propertyList);
        addProperty(GUARD_PROPERTY, propertyList);
        addProperty(BODY_DECLARATIONS_PROPERTY, propertyList);
		addProperty(PRECEDENCE_PROPERTY, propertyList);
        PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
    }

    /**
     * Returns a list of structural property descriptors for this node type.
     * Clients must not modify the result.
     * 
     * @param apiLevel the API level; one of the
     * <code>AST.JLS&ast;</code> constants

     * @return a list of property descriptors (element type: 
     * {@link StructuralPropertyDescriptor})
     * @since 3.0
     */
    @SuppressWarnings("rawtypes")
	public static List propertyDescriptors(int apiLevel)
    {
        if (apiLevel == AST.JLS2)
        {
            return PROPERTY_DESCRIPTORS_2_0;
        }
        else
        {
            return PROPERTY_DESCRIPTORS_3_0;
        }
    }

    /**
     * The optional baseClass name; <code>null</code> if none.
     * Defaults to none. Note that this field is not used for
     * interface declarations. Not used in 3.0.
     */
    private Name optionalBaseClassName = null;
    
    /**
     * The optional baseClass type; <code>null</code> if none.
     * Defaults to none. Note that this field is not used for
     * interface declarations. Null in JLS2. Added in JLS3.
     * @since 3.0
     */
    private Type optionalBaseClassType = null;
    
    private Name teamClassName = null;
    private Type teamClassType = null;

    /**
     * @since 1.1.7
     */
	private boolean isRoleFile = false;

    /**
     * Creates a new AST node for a type declaration owned by the given 
     * AST. By default, the type declaration is for a class of an
     * unspecified, but legal, name; no modifiers; no javadoc; 
     * no type parameters; no superclass or superinterfaces; and an empty list
     * of body declarations.
     * <p>
     * N.B. This constructor is package-private; all subclasses must be 
     * declared in the same package; clients are unable to declare 
     * additional subclasses.
     * </p>
     * 
     * @param ast the AST that is to own this node
     */
    RoleTypeDeclaration(AST ast) {
        super(ast);
        if (ast.apiLevel == AST.JLS2) {
            this.superInterfaceNames = new ASTNode.NodeList(SUPER_INTERFACES_PROPERTY);
        }
        if (ast.apiLevel >= AST.JLS3) {
            this.typeParameters = new ASTNode.NodeList(TYPE_PARAMETERS_PROPERTY);
            this.superInterfaceTypes = new ASTNode.NodeList(SUPER_INTERFACE_TYPES_PROPERTY);
        }
    }

    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     * @since 3.0
     */
    @SuppressWarnings("rawtypes")
	final List internalStructuralPropertiesForType(int apiLevel)
    {
        return propertyDescriptors(apiLevel);
    }    
    
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    final int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean get, int value) 
    {
        if (property == MODIFIERS_PROPERTY) 
        {
            if (get) {
                return getModifiers();
            } else {
                setModifiers(value);
                return 0;
            }
        }
        // allow default implementation to flag the error
        return super.internalGetSetIntProperty(property, get, value);
    }

    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value)
    {
        if (property == INTERFACE_PROPERTY)
        {
            if (get)
            {
                return isInterface();
            }
            else
            {
                setInterface(value);
                return false;
            }       
        }
        
        if (property == TEAM_PROPERTY)
        {
            if (get)
            {
                return isTeam();
            }
            else
            {
                setTeam(value);
                return false;
            }
        }
        if (property == ROLE_PROPERTY)
        {
            if (get)
            {
                return isRole();
            }
            else
            {
                setRole(value);
                return false;
            }       
        }
        if (property == ROLE_FILE_PROPERTY)
        {
            if (get)
            {
                return isRoleFile();
            }
            else
            {
                setRoleFile(value);
                return false;
            }       
        }
        // allow default implementation to flag the error
        return super.internalGetSetBooleanProperty(property, get, value);
    }
    
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    @Override
    final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) 
    {
        if (property == JAVADOC_PROPERTY) 
        {
            if (get) {
                return getJavadoc();
            } else {
                setJavadoc((Javadoc) child);
                return null;
            }
        }
        if (property == NAME_PROPERTY) {
            if (get) {
                return getName();
            } else {
                setName((SimpleName) child);
                return null;
            }
        }
        if (property == SUPERCLASS_PROPERTY) {
            if (get) {
                return getSuperclass();
            } else {
                setSuperclass((Name) child);
                return null;
            }
        }
        if (property == SUPERCLASS_TYPE_PROPERTY) {
            if (get) {
                return getSuperclassType();
            } else {
                setSuperclassType((Type) child);
                return null;
            }
        }

        if (property == BASECLASS_PROPERTY)
        {
            if (get)
            {
                return getBaseClass();
            }
            else
            {
                setBaseClass((Name) child);
                return null;
            }
        }

        if (property == BASECLASS_TYPE_PROPERTY)
        {
            if (get)
            {
                return getBaseClassType();
            }
            else
            {
                setBaseClassType((Type) child);
                return null;
            }
        }
        
        if (property == TEAMCLASS_PROPERTY)
        {
            if (get)
            {
                return getTeamClass();
            }
            else
            {
                setTeamClass((Name) child);
                return null;
            }
        }

        if (property == TEAMCLASS_TYPE_PROPERTY)
        {
            if (get)
            {
                return getTeamClassType();
            }
            else
            {
                setTeamClassType((Type) child);
                return null;
            }
        }

        // allow default implementation to flag the error
        return super.internalGetSetChildProperty(property, get, child);
    }

    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    @SuppressWarnings("rawtypes")
	final List internalGetChildListProperty(ChildListPropertyDescriptor property)
    {
        if (property == MODIFIERS2_PROPERTY)
        {
            return modifiers();
        }
        
        if (property == TYPE_PARAMETERS_PROPERTY)
        {
            return typeParameters();
        }
        
        if (property == SUPER_INTERFACES_PROPERTY)
        {
            return superInterfaces();
        }
        
        if (property == SUPER_INTERFACE_TYPES_PROPERTY)
        {
            return superInterfaceTypes();
        }
        
        if (property == BODY_DECLARATIONS_PROPERTY)
        {
            return bodyDeclarations();
        }
        
        // allow default implementation to flag the error
        return super.internalGetChildListProperty(property);
    }
    
    /* (omit javadoc for this method)
     * Method declared on BodyDeclaration.
     */
    final ChildPropertyDescriptor internalJavadocProperty()
    {
        return JAVADOC_PROPERTY;
    }

    /* (omit javadoc for this method)
     * Method declared on BodyDeclaration.
     */
    final ChildListPropertyDescriptor internalModifiers2Property()
    {
        return MODIFIERS2_PROPERTY;
    }

    /* (omit javadoc for this method)
     * Method declared on BodyDeclaration.
     */
    final SimplePropertyDescriptor internalModifiersProperty()
    {
        return MODIFIERS_PROPERTY;
    }

    /* (omit javadoc for this method)
     * Method declared on AbstractTypeDeclaration.
     */
    final ChildPropertyDescriptor internalNameProperty()
    {
        return NAME_PROPERTY;
    }

    /* (omit javadoc for this method)
     * Method declared on AbstractTypeDeclaration.
     */
    final ChildListPropertyDescriptor internalBodyDeclarationsProperty()
    {
        return BODY_DECLARATIONS_PROPERTY;
    }
    
    ChildPropertyDescriptor internalGuardPredicateProperty() {
    	return GUARD_PROPERTY;
    }

    ChildListPropertyDescriptor internalPrecedenceProperty() {
    	return PRECEDENCE_PROPERTY;
    }

    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    final int getNodeType0()
    {
        return ROLE_TYPE_DECLARATION;
    }

    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    @SuppressWarnings("unchecked")
	ASTNode clone0(AST target)
    {
        RoleTypeDeclaration result = new RoleTypeDeclaration(target);
        result.setSourceRange(this.getStartPosition(), this.getLength());
        result.setJavadoc(
            (Javadoc) ASTNode.copySubtree(target, getJavadoc()));
        if (this.ast.apiLevel == AST.JLS2)
        {
            result.setModifiers(getModifiers());
            result.setSuperclass(
                    (Name) ASTNode.copySubtree(target, getSuperclass()));
            result.superInterfaces().addAll(
                    ASTNode.copySubtrees(target, superInterfaces()));

            result.setBaseClass(
                    (Name) ASTNode.copySubtree(target, getBaseClass()));
            
            result.setTeamClass(
                    (Name) ASTNode.copySubtree(target, getTeamClass()));
        }
        result.setTeam(isTeam());
        result.setRole(isRole());
		result.setRoleFile(isRoleFile());
        result.setName((SimpleName) getName().clone(target));
        if (this.ast.apiLevel >= AST.JLS3)
        {
            result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
            result.typeParameters().addAll(
                    ASTNode.copySubtrees(target, typeParameters()));
            result.setSuperclassType(
                    (Type) ASTNode.copySubtree(target, getSuperclassType()));
            result.superInterfaceTypes().addAll(
                    ASTNode.copySubtrees(target, superInterfaceTypes()));
            
            result.setBaseClassType(
                    (Type) ASTNode.copySubtree(target, getBaseClassType()));
            
            result.setTeamClassType(
                    (Type) ASTNode.copySubtree(target, getTeamClassType()));
        }
        result.setGuardPredicate((GuardPredicateDeclaration)ASTNode.copySubtree(target, getGuardPredicate()));
        result.bodyDeclarations().addAll(
            ASTNode.copySubtrees(target, bodyDeclarations()));
		result.precedences().addAll(ASTNode.copySubtrees(target, precedences()));
        return result;
    }

	/* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    final boolean subtreeMatch0(ASTMatcher matcher, Object other)
    {
        // dispatch to correct overloaded match method
        return matcher.match(this, other);
    }
    
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    void accept0(ASTVisitor visitor)
    {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            // visit children in normal left to right reading order
            if (this.ast.apiLevel == AST.JLS2) {
                acceptChild(visitor, getJavadoc());
                acceptChild(visitor, getName());
                acceptChild(visitor, getSuperclass());
                acceptChild(visitor, getBaseClass());
                acceptChildren(visitor, this.superInterfaceNames);
                acceptChildren(visitor, this.bodyDeclarations);
            }
            if (this.ast.apiLevel >= AST.JLS3) {
                acceptChild(visitor, getJavadoc());
                acceptChildren(visitor, this.modifiers);
                acceptChild(visitor, getName());
                acceptChildren(visitor, this.typeParameters);
                acceptChild(visitor, getSuperclassType());
                acceptChild(visitor, getBaseClassType());
                acceptChildren(visitor, this.superInterfaceTypes);
                acceptChild(visitor, this.getGuardPredicate());
                acceptChildren(visitor, this.bodyDeclarations);
                acceptChildren(visitor, this._precedences);
            }
        }
        visitor.endVisit(this);
    }

	/**
	 * Sets wether this type declaration declares a team class or a class.
	 * 
	 * @param isTeam <code>true</code> if this is a team class
	 *    declaration, and <code>false</code> if this is a class declaration
	 */ 
	public void setTeam(boolean isTeam)
	{
		preValueChange(TEAM_PROPERTY);
		this._isTeam = isTeam;
		postValueChange(TEAM_PROPERTY);
	}

	/**
	 * Sets whether this type declaration declares a role class or a class.
	 *  
	 * @param isRole <code>true</code> if this is a role class
	 *    declaration, and <code>false</code> if this is a class declaration
	 */ 
	public void setRole(boolean isRole)
	{
		preValueChange(ROLE_PROPERTY);
		this._isRole = isRole;
		postValueChange(ROLE_PROPERTY);
	}

	/**
	 * Sets whether this type declaration resides in a role file.
	 */ 
	public void setRoleFile(boolean isRoleFile)
	{
		preValueChange(ROLE_FILE_PROPERTY);
		this.isRoleFile = isRoleFile;
		postValueChange(ROLE_FILE_PROPERTY);
	}
	
	/** 
	 * Answer whether this role is a role file.
	 */
	public boolean isRoleFile() {
		return this.isRoleFile;
	}
    
    /** @deprecated replaced by {@link #getBaseClassType()} in JLS3. */
    public Name getBaseClass()
    {
        supportedOnlyIn2();
        return this.optionalBaseClassName;
    }
    
    public Type getBaseClassType()
    {
        unsupportedIn2();
        return this.optionalBaseClassType;   
    }

    /** @deprecated replaced by {@link #getTeamClassType()} in JLS3. */
    public Name getTeamClass()
    {
        supportedOnlyIn2();
        return this.teamClassName;
    }    
    
    public Type getTeamClassType()
    {
        unsupportedIn2();
        return this.teamClassType;   
    }
    
    /** @deprecated use {@link #setBaseClassType(Type)} (JLS3) */
    public void setBaseClass(Name baseClassName)
    {
        supportedOnlyIn2();
        ASTNode oldChild = this.optionalBaseClassName;
        preReplaceChild(oldChild, baseClassName, BASECLASS_PROPERTY);
        this.optionalBaseClassName = baseClassName;
        postReplaceChild(oldChild, baseClassName, BASECLASS_PROPERTY);  
    }

    public void setBaseClassType(Type baseClassType)
    {
        unsupportedIn2();
        ASTNode oldChild = this.optionalBaseClassType;
        preReplaceChild(oldChild, baseClassType, BASECLASS_TYPE_PROPERTY);
        this.optionalBaseClassType = baseClassType;
        postReplaceChild(oldChild, baseClassType, BASECLASS_TYPE_PROPERTY);
    }  
 
    /** @deprecated use {@link #setTeamClassType(Type)} (JLS3) */
    public void setTeamClass(Name teamClassName)
    {
        supportedOnlyIn2();
        ASTNode oldChild = this.teamClassName;
        preReplaceChild(oldChild, teamClassName, TEAMCLASS_PROPERTY);
        this.teamClassName = teamClassName;
        postReplaceChild(oldChild, teamClassName, TEAMCLASS_PROPERTY);  
    }

    public void setTeamClassType(Type teamClassType)
    {
        unsupportedIn2();
        ASTNode oldChild = this.teamClassType;
        preReplaceChild(oldChild, teamClassType, TEAMCLASS_TYPE_PROPERTY);
        this.teamClassType = teamClassType;
        postReplaceChild(oldChild, teamClassType, TEAMCLASS_TYPE_PROPERTY);
    }
  
    /**
     * Returns the name of the superclass declared in this type
     * declaration, or <code>null</code> if there is none (JLS2 API only).
     * <p>
     * Note that this child is not relevant for interface 
     * declarations (although it does still figure in subtree
     * equality comparisons).
     * </p>
     * 
     * @return the superclass name node, or <code>null</code> if 
     *    there is none
     * @exception UnsupportedOperationException if this operation is used in
     * an AST later than JLS2
     * @deprecated In the JLS3 API, this method is replaced by <code>getSuperclassType</code>, which returns a <code>Type</code> instead of a <code>Name</code>.
     */
    public Name getSuperclass() {
        supportedOnlyIn2();
        return this.optionalSuperclassName;
    }

    /**
    * Returns the superclass declared in this type
    * declaration, or <code>null</code> if there is none (added in JLS3 API).
    * <p>
    * Note that this child is not relevant for interface 
    * declarations (although it does still figure in subtree
    * equality comparisons).
    * </p>
    * 
    * @return the superclass type node, or <code>null</code> if 
    *    there is none
    * @exception UnsupportedOperationException if this operation is used in
    * a JLS2 AST
    * @since 3.0
    */ 
    public Type getSuperclassType() {
        unsupportedIn2();
        return this.optionalSuperclassType;
    }

    /**
     * Sets or clears the name of the superclass declared in this type
     * declaration (JLS2 API only).
     * <p>
     * Note that this child is not relevant for interface 
     * declarations (although it does still figure in subtree
     * equality comparisons).
     * </p>
     * 
     * @param superclassName the superclass name node, or <code>null</code> if 
     *    there is none
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     * @exception UnsupportedOperationException if this operation is used in
     * an AST later than JLS2
     * @deprecated In the JLS3 API, this method is replaced by <code>setType</code>, which expects a <code>Type</code> instead of a <code>Name</code>.
     */
    public void setSuperclass(Name superclassName) {
        supportedOnlyIn2();
        ASTNode oldChild = this.optionalSuperclassName;
        preReplaceChild(oldChild, superclassName, SUPERCLASS_PROPERTY);
        this.optionalSuperclassName = superclassName;
        postReplaceChild(oldChild, superclassName, SUPERCLASS_PROPERTY);
    }

    /**
     * Sets or clears the superclass declared in this type
     * declaration (added in JLS3 API).
     * <p>
     * Note that this child is not relevant for interface declarations
     * (although it does still figure in subtree equality comparisons).
     * </p>
     * 
     * @param superclassType the superclass type node, or <code>null</code> if 
     *    there is none
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     * @exception UnsupportedOperationException if this operation is used in
     * a JLS2 AST
     * @since 3.0
     */ 
    public void setSuperclassType(Type superclassType) {
        unsupportedIn2();
        ASTNode oldChild = this.optionalSuperclassType;
        preReplaceChild(oldChild, superclassType, SUPERCLASS_TYPE_PROPERTY);
        this.optionalSuperclassType = superclassType;
        postReplaceChild(oldChild, superclassType, SUPERCLASS_TYPE_PROPERTY);
    }

    /**
     * Returns the ordered list of callout declarations of this type 
     * declaration.
     * <p>
     * This convenience method returns this node's body declarations
     * with non-methods filtered out. Unlike <code>bodyDeclarations</code>,
     * this method does not return a live result.
     * </p>
     * 
     * @return the (possibly empty) list of role type declarations
     */ 
    @SuppressWarnings("rawtypes")
	public CalloutMappingDeclaration[] getCallOuts()
    {
        List bd = bodyDeclarations();
        int callOutCount = 0;
        for (Iterator it = bd.listIterator(); it.hasNext(); )
        {
            if (it.next() instanceof CalloutMappingDeclaration)
            {
                callOutCount++;
            }
        }
        CalloutMappingDeclaration[] callOuts = new CalloutMappingDeclaration[callOutCount];
        int next = 0;
        for (Iterator it = bd.listIterator(); it.hasNext(); )
        {
            Object decl = it.next();
            if (decl instanceof CalloutMappingDeclaration)
            {
                callOuts[next++] = (CalloutMappingDeclaration) decl;
            }
        }
        return callOuts;
    }

    /**
     * Returns the ordered list of CallIn declarations of this type 
     * declaration.
     * <p>
     * This convenience method returns this node's body declarations
     * with non-methods filtered out. Unlike <code>bodyDeclarations</code>,
     * this method does not return a live result.
     * </p>
     * 
     * @return the (possibly empty) list of role type declarations
     */ 
    @SuppressWarnings("rawtypes")
	public CallinMappingDeclaration[] getCallIns()
    {
        List bd = bodyDeclarations();
        int callInCount = 0;
        for (Iterator it = bd.listIterator(); it.hasNext(); )
        {
            if (it.next() instanceof CallinMappingDeclaration)
            {
                callInCount++;
            }
        }
        CallinMappingDeclaration[] callIns = new CallinMappingDeclaration[callInCount];
        int next = 0;
        for (Iterator it = bd.listIterator(); it.hasNext(); )
        {
            Object decl = it.next();
            if (decl instanceof CallinMappingDeclaration)
            {
                callIns[next++] = (CallinMappingDeclaration) decl;
            }
        }
        return callIns;
    }
    
    
    
    
    
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    @SuppressWarnings({ "nls", "rawtypes" })
	void appendDebugString(StringBuffer buffer)
    {
        buffer.append("RoleTypeDeclaration[\n"); 
        buffer.append("class "); 
        buffer.append(getName().getIdentifier());
        buffer.append("\n"); 
        if (this.optionalGuardPredicate != null) {
        	getGuardPredicate().appendDebugString(buffer);
        	buffer.append("\n"); 
        }
        for (Iterator it = bodyDeclarations().iterator(); it.hasNext();) {
            BodyDeclaration d = (BodyDeclaration) it.next();
            d.appendDebugString(buffer);
            if (it.hasNext()) {
                buffer.append(";\n"); 
            }
        }
        buffer.append("]");
    }
        
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    int memSize()
    {
        return super.memSize() + 12 * 4;
    }
    
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    int treeSize()
    {
        return memSize()
            + (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
            + (this.modifiers == null ? 0 : this.modifiers.listSize())
            + (this.typeName == null ? 0 : getName().treeSize())
            + (this.typeParameters == null ? 0 : this.typeParameters.listSize())
            + (this.optionalSuperclassName == null ? 0 : getSuperclass().treeSize())
            + (this.optionalSuperclassType == null ? 0 : getSuperclassType().treeSize())
            + (this.superInterfaceNames == null ? 0 : this.superInterfaceNames.listSize())
            + (this.superInterfaceTypes == null ? 0 : this.superInterfaceTypes.listSize())
            + this.bodyDeclarations.listSize()
            + this._precedences.listSize()
            + (this.optionalBaseClassName == null  ? 0 : this.optionalBaseClassName.treeSize())
            + (this.optionalBaseClassType == null  ? 0 : this.optionalBaseClassType.treeSize())
            + (this.teamClassName== null           ? 0 : this.teamClassName.treeSize())
            + (this.teamClassType == null          ? 0 : this.teamClassType.treeSize())
            + (this.optionalGuardPredicate == null ? 0 : this.optionalGuardPredicate.treeSize());
    }
}
