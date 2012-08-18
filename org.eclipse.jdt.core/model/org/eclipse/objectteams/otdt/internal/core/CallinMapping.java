/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CallinMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IMethodSpec;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.util.MethodData;


/**
 * Callin Mapping implementation
 * @author jwloka
 * @version $Id: CallinMapping.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class CallinMapping extends MethodMapping implements ICallinMapping
{
	private int          callinKind;

	private MethodData[] baseMethodHandles;
	private String       name;
	

	public CallinMapping(
			int          declarationSourceStart,
			int          sourceStart,
			int			 sourceEnd,
			int          declarationSourceEnd,
			IRoleType    parent, 
			IMethod 	 corrJavaMeth,
			char[]       name,
			int 	     callinKind,
			MethodData   roleMethodHandle,
			MethodData[] baseMethodHandles, 
			boolean 	 hasSignature)
	{
		this(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, 
		     parent, corrJavaMeth, 
		     name, callinKind, roleMethodHandle, baseMethodHandles, 
		     hasSignature, /*addAsChild*/true);
	}
	
    // for use by sub-class ResolvedCallinMapping 
    public CallinMapping(
        	int          declarationSourceStart,
			int          sourceStart,
			int          sourceEnd,
        	int          declarationSourceEnd,
            IRoleType    parent, 
        	IMethod 	 corrJavaMeth,
            char[]       name,
            int 	     callinKind,
            MethodData   roleMethodHandle,
            MethodData[] baseMethodHandles, 
            boolean 	 hasSignature,
            boolean 	 addAsChild)
    {
        super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, 
        	  CALLIN_MAPPING, corrJavaMeth, parent, 
        	  roleMethodHandle, 
        	  hasSignature, addAsChild);
        this.name              = new String(name); 
        this.callinKind        = callinKind;
		this.baseMethodHandles = baseMethodHandles;        
    }

    // ==== memento generation: ====
    @Override
    protected void getNameForHandle(StringBuffer buff) {
   		escapeMementoName(buff, this.name);
   		buff.append(OTJavaElement.OTEM_METHODMAPPING);
    }
    protected char getMappingKindChar() {
    	switch (this.callinKind) {
    	case ICallinMapping.KIND_AFTER:   return 'a';
    	case ICallinMapping.KIND_BEFORE:  return 'b';
    	case ICallinMapping.KIND_REPLACE: return 'r';
    	default: throw new InternalCompilerError("Unexpected callin kind"); //$NON-NLS-1$
    	}
    }
    protected void getBaseMethodsForHandle(StringBuffer buff) {
    	for (IMethodSpec baseMethod : this.baseMethodHandles)
    		getMethodForHandle(baseMethod, buff);
    }    
    // ====
    
    @Override
    protected String getSourceName() {
    	if (hasName())
    		return this.name;
    	return super.getSourceName();
    }
    
    @Override
    @SuppressWarnings("nls")
	public String getElementName()
	{
		StringBuffer elementName = new StringBuffer(super.getElementName());
		elementName.append(" <- ");
		
		if (this.baseMethodHandles.length > 1)
		{
		    elementName.append("{");
		}
		for (int idx = 0; idx < this.baseMethodHandles.length; idx++)
        {
			if (idx != 0)
			{
				elementName.append(",");
			}
			if (hasSignature())
			{
			    elementName.append(this.baseMethodHandles[idx].toString());
			}
			else
			{
			    elementName.append(this.baseMethodHandles[idx].getSelector());
			}
        }
		if (this.baseMethodHandles.length > 1)
		{
		    elementName.append("}");
		}
		ITypeParameter[] typeParameters = this.getRoleMethodHandle().typeParameters;
		if (typeParameters.length > 0)
		{
			elementName.append(" <");
			for (int i = 0; i < typeParameters.length; i++) {
				elementName.append(typeParameters[i].getElementName());
				if (i+1<typeParameters.length)
					elementName.append(", ");
			}
			elementName.append(">");
		}
		return elementName.toString();
	}
	
	/**
	 * @return Does this mapping have a name (aka callin label) in the source code?
	 */
	public boolean hasName() {
		return this.name != null && !this.name.startsWith("<"); // generated names start with '<'. //$NON-NLS-1$
	}
	
	public String getName() {
		return this.name;
	}

    public int getMappingKind()
    {
        return CALLIN_MAPPING;
    }

    /**
     * ICallinMapping.KIND_BEFORE,AFTER,REPLACE
     */
    public int getCallinKind()
    {
        return this.callinKind;
    }
    
    public boolean hasCovariantReturn() {
    	if (this.baseMethodHandles != null)
    		for (IMethodSpec baseMethod : this.baseMethodHandles) 
				if (baseMethod.hasCovariantReturn())
					return true;
		return false;
    }

    public IMethod[] getBoundBaseMethods() throws JavaModelException
    {
        return findBaseMethods();
    }
    
    public boolean equals(Object obj)
    {
		if(!(obj instanceof CallinMapping))
		{
		    return false;
		}

		CallinMapping other = (CallinMapping)obj;
		if (   (this.name.charAt(0) != '<' && other.name.charAt(0) != '<')
			&& !this.name.equals(other.name)) // require only source level names to be equal, not generated ones
			return false;
		if (!super.equals(obj))
			return false;
		if (this.name.equals(other.name)) // if names are equal ignore changes in callin kind
			return true;
		return this.callinKind == other.callinKind;
    }
    
    @Override
    @SuppressWarnings("nls")
	public String toString()
    {
    	return "callin " + super.toString();
    }
    
    /**
     * Performs resolving of all bound base methods
     */
	private IMethod[] findBaseMethods() throws JavaModelException
	{
		IType     baseClass   = ((IRoleType)getParent()).getBaseClass();
		IType[]   typeParents = TypeHelper.getSuperTypes(baseClass);
		if (OTModelManager.hasOTElementFor(baseClass)) {
			IOTType otType = OTModelManager.getOTElement(baseClass);
			if (otType.isRole()) {
				IType[] implicitSupers = TypeHelper.getImplicitSuperTypes((IRoleType)otType);
				int len1 = typeParents.length;
				int len2 = implicitSupers.length;
				System.arraycopy(
						typeParents, 0,
						typeParents = new IType[len1+len2], 0,
						len1);
				System.arraycopy(
						implicitSupers, 0,
						typeParents, len1,
						len2);
			}
		}
		List<IMethod> baseMethods = new LinkedList<IMethod>();

		for (int idx = 0; idx < this.baseMethodHandles.length; idx++)
        {
            IMethod baseMethod = findMethod(typeParents, this.baseMethodHandles[idx]);
			
			// TODO(jwl): A warning from the compiler should be given to the developer, elsewhere!
			// TODO(jwl): Do we really want an inconsistant OT model??
			// Only existing base methods are added, if an assigned base method 
			// doesn't exist, it just will be ignored
			if (baseMethod != null)
			{
				baseMethods.add(baseMethod);
			}			
        }
    	    	
		return baseMethods.toArray(new IMethod[baseMethods.size()]);
	}
	
	// added for the SourceTypeConverter
	public IMethodSpec[] getBaseMethodHandles()
	{
		return this.baseMethodHandles;
	}

	/**
	 * Converts the ICallinMapping.KIND_ constants to TerminalTokens constants.
	 */
	public static int convertModelToTerminalToken(int icallinmappingKind) 
	{
		switch (icallinmappingKind)
		{
			case ICallinMapping.KIND_BEFORE:
				return TerminalTokens.TokenNamebefore;
			case ICallinMapping.KIND_AFTER:
				return TerminalTokens.TokenNameafter;
			default:
			case ICallinMapping.KIND_REPLACE:
				return TerminalTokens.TokenNamereplace;
		}
	}

	public static int convertTerminalTokenToModel(int terminalTokensCallinKind) 
	{
		switch (terminalTokensCallinKind)
		{
			case TerminalTokens.TokenNamebefore:
				return ICallinMapping.KIND_BEFORE;
			case TerminalTokens.TokenNameafter:
				return ICallinMapping.KIND_AFTER;
			default:
			case TerminalTokens.TokenNamereplace:
				return ICallinMapping.KIND_REPLACE;
		}
	}

	// implementation and alternate API of resolved(Binding)
	public OTJavaElement resolved(char[] uniqueKey) {
		ResolvedCallinMapping resolvedHandle = 
			new ResolvedCallinMapping(
					getDeclarationSourceStart(),
					getSourceStart(),
					getSourceEnd(),
			    	getDeclarationSourceEnd(),
			        (IRoleType) getParent(), 
			    	getIMethod(),
			        this.name.toCharArray(),
			        this.callinKind,
			        getRoleMethodHandle(),
			        this.baseMethodHandles, 
			        hasSignature(), 					
					new String(uniqueKey));
		
		return resolvedHandle;
	}
}
