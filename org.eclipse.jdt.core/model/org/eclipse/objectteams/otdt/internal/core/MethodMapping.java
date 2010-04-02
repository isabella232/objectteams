/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodMapping.java 23401 2010-02-02 23:56:05Z stephan $
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.Annotation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.TypeParameter;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.exceptions.ExceptionHandler;
import org.eclipse.objectteams.otdt.core.util.MethodData;


/**
 * OT_COPY_PASTE from Member.getCategories()
 *               from Member.getJavadocRange()
 *               from SourceRefElement.getOpenableParent()
 *               
 * Generic Method Mapping, needs to be subclassed to add missing behaviour
 * for IMethodMapping.getMappingKind()
 *  
 * @author jwloka
 */
public abstract class MethodMapping extends OTJavaElement implements IMethodMapping
{
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    protected static final ITypeParameter[] NO_TYPE_PARAMETERS = new ITypeParameter[0];

    private int              _declarationSourceStart;
    private int              _sourceStart;
    private int              _sourceEnd;
    private int              _declarationSourceEnd;
    private IMethod          _roleMethod;
	private MethodData       _roleMethodHandle;
	private boolean          _hasSignature;

    public MethodMapping(int        declarationSourceStart,
                         int        sourceStart,
                         int        sourceEnd,
                         int        declarationSourceEnd,
                         int        type,
                         IMethod	correspondingJavaElem,
                         IType  	parent,
                         MethodData roleMethodHandle, 
						 boolean    hasSignature)
    {
        super(type, correspondingJavaElem, parent);
        _roleMethodHandle       = roleMethodHandle;
        _declarationSourceStart = declarationSourceStart;
        _sourceStart            = sourceStart;
        _sourceEnd			    = sourceEnd;
        _declarationSourceEnd   = declarationSourceEnd;
        _hasSignature           = hasSignature;
    }
    
    public MethodMapping(int            declarationSourceStart,
			             int        	sourceStart,
			             int			sourceEnd,
			             int        	declarationSourceEnd,
			             int        	type,
			             IMethod		correspondingJavaElem,
			             IType  		parent,
			             MethodData 	roleMethodHandle, 
			             boolean    	hasSignature,
			             boolean    	addAsChild)
	{
		super(type, correspondingJavaElem, parent, addAsChild);
		_roleMethodHandle       = roleMethodHandle;
		_declarationSourceStart = declarationSourceStart;
		_sourceStart        	= sourceStart;
		_sourceEnd				= sourceEnd;
		_declarationSourceEnd   = declarationSourceEnd;
		_hasSignature    		= hasSignature;
	}
        
    // ==== memento generation: ====
    @Override
    public String getHandleIdentifier() {
    	StringBuffer buff = new StringBuffer();
    	IJavaElement parent = getParent();
    	if (parent instanceof IOTJavaElement)
    		parent = ((IOTJavaElement)parent).getCorrespondingJavaElement();
    	// prefix
		buff.append(((JavaElement)parent).getHandleMemento());
    	char delimiter = OTJavaElement.OTEM_METHODMAPPING;
    	// start:
    	buff.append(delimiter);
    	// mapping kind:
    	buff.append(getMappingKindChar());
    	// long or short?
    	buff.append(this._hasSignature ? 'l' : 's');
    	buff.append(delimiter);
    	// mapping name (if any);
    	getNameForHandle(buff);
    	// role method:
    	getMethodForHandle(this._roleMethodHandle, buff);
		// base methods:
    	getBaseMethodsForHandle(buff);
    	buff.append(delimiter);
		return buff.toString();
    }
    protected void getNameForHandle(StringBuffer buff) { /* default: mapping has no name. */ }
    /** 
     * Answer a char encoding the mapping kind with this information:
     * callin: a=after, b=before, r=replace; 
     * callout: o=regular, g=getter, s=setter [capital=isOverride].
     */
    abstract protected char getMappingKindChar();
    abstract protected void getBaseMethodsForHandle(StringBuffer buff);
	
    protected void getMethodForHandle(MethodData method, StringBuffer buff) {
    	JavaElement.escapeMementoName(buff, method.getSelector());
    	if (this._hasSignature) {
    		for (String argType : method.getArgumentTypes()) {
    			buff.append(JavaElement.JEM_METHOD);
    			JavaElement.escapeMementoName(buff, argType);
    		}
    		buff.append(JavaElement.JEM_METHOD);
    		JavaElement.escapeMementoName(buff, method.getReturnType());
    	}
    	buff.append(OTJavaElement.OTEM_METHODMAPPING);
    }
    // ==== retreive method spec from memento: === 
    public static MethodData createMethodData(MementoTokenizer memento, String selector) {
    	String cur = memento.nextToken();
    	if (cur.charAt(0) == JavaElement.JEM_METHOD)
    		cur = memento.nextToken(); // skip initial separator
    	List<String> argTypes = new ArrayList<String>(); 
    	while (cur.charAt(0) != OTJavaElement.OTEM_METHODMAPPING) {
			StringBuffer buffer = new StringBuffer();
			while (cur.length() == 1 && Signature.C_ARRAY == cur.charAt(0)) { // backward compatible with 3.0 mementos
				buffer.append(Signature.C_ARRAY);
				if (!memento.hasMoreTokens())
					break;
				cur = memento.nextToken();
			}
			buffer.append(cur);
			argTypes.add(buffer.toString());
    		if (memento.nextToken().charAt(0) != JavaElement.JEM_METHOD)
    			break;
    		cur = memento.nextToken();
    	}
    	String returnType = null;
    	if (argTypes.size() > 0)
    		returnType = argTypes.remove(argTypes.size()-1);
    	return new MethodData(selector, argTypes.toArray(new String[argTypes.size()]), null, returnType, false);
    }
    // ====
    
	public IMethod getRoleMethod()
    {
    	if (_roleMethod == null)
    	{
    		try
            {
                _roleMethod = findRoleMethod();
                assert (_roleMethod != null);
            }
            catch (JavaModelException ex)
            {
            	log("Failed to lookup original role method element!", ex); //$NON-NLS-1$
            }
    	}
    	
        return _roleMethod;
    }

	// added for the SourceTypeConverter
    public MethodData getRoleMethodHandle()
    {
    	return _roleMethodHandle;
    }
    
    public IMethod getRoleMethodThrowingException() throws JavaModelException
    {
    	if (_roleMethod == null)
    	{
			_roleMethod = findRoleMethod();
    	}
    	
        return _roleMethod;
    }

    public void setRoleMethod(IMethod meth)
	{
		_roleMethod = meth;
	}

    public IType getRoleClass()
    {
        // TODO(jwl): simplify later
    	IOTType owningType = (IOTType)getParent();
    	
    	return (IType)owningType.getCorrespondingJavaElement();
    }
    
    /**
     * Only returns the role-methods part -- subclasses must override and 
     * construct the whole element name!
     */
	public String getElementName()
	{
	    if (_hasSignature)
	    {
	        return _roleMethodHandle.toString();
	    }

	    return _roleMethodHandle.getSelector();
	}

	public int getDeclarationSourceStart()
    {
        return _declarationSourceStart;
    }
	
	public int getSourceStart()
	{
		return _sourceStart;
	}
	
	public int getSourceEnd()
	{
		return _sourceEnd;
	}
	
	public int getDeclarationSourceEnd()
	{
		return _declarationSourceEnd;
	}

	public boolean equals(Object obj)
	{
		MethodMapping other = (MethodMapping)obj;
		
		return super.equals(other)
//				&& _declarationSourceStart == other.getDeclarationSourceStart()
//				&& _declarationSourceEnd == other.getDeclarationSourceEnd()
				&& getElementName().equals(other.getElementName());
	}
	
    @SuppressWarnings("nls")
	public String toString()
	{
		return "methodmapping: " + getElementName();
	}

    /**
	 * Tries to find JavaElement method on demand for a given method from
	 * current binding. Lookup is using role hierarchy (implicit and explicit).
	 */
	protected IMethod findRoleMethod() throws JavaModelException
	{
		IType[]    implicitParents = TypeHelper.getImplicitSuperTypes((IRoleType)getParent());
		HashSet<IType> allParents      = new HashSet<IType>();

		// collect all parents in role type hierarchy
		for (int idx = 0; idx < implicitParents.length; idx++)
		{
			IType elem = implicitParents[idx];        	

			// build super class hierarchy for element
			ITypeHierarchy hierarchy =
						elem.newSupertypeHierarchy( new NullProgressMonitor() );        				
			IType[] superTypes       = hierarchy.getAllSuperclasses(elem);

			// add implicit parent...
			allParents.add(elem);
			// ...and all "extends" parents
			if (superTypes.length > 0)
			{
				allParents.addAll(Arrays.asList(superTypes));
			} 
		}
		return findMethod(allParents.toArray(new IType[allParents.size()]),
						  _roleMethodHandle);
	}

	/**
	 * Tries to find an IMethod matching the given methodHandle in a set
	 * of types.
	 * @return the first matching IMethod in the set of types or null if
	 * 		   nothing found
	 */
	protected IMethod findMethod(IType[] types, MethodData methodHandle)
		throws JavaModelException
	{
		// cycle through types...
		for (int parIdx = 0; parIdx < types.length; parIdx++)
		{
			IMethod[] methods = types[parIdx].getMethods();
            // ... and compare with each method defined in current type
			for (int methIdx = 0; methIdx < methods.length; methIdx++)
			{
				IMethod tmpMethod = methods[methIdx];
				// check for equal method name and signature            	
				if (tmpMethod.getElementName().equals(methodHandle.getSelector())
					&& 
						(methodHandle.isIncomplete() ||
						Util.equalArraysOrNull(tmpMethod.getParameterTypes(), methodHandle.getArgumentTypes())))
				{
					// return immediately on first match
					return tmpMethod;
				}
			}            
		}
		IMethod methodReference= SourceMethod.createHandle((JavaElement)types[0], methodHandle);
		// failure might be due to mismatching qualified/simple types
		// this variant only uses the simple types:
		for (int parIdx = 0; parIdx < types.length; parIdx++) {
			IMethod[] methods= types[parIdx].findMethods(methodReference);
			if (methods != null && methods.length == 1)
				return methods[0];
		}
		return null;		
	}
    
	protected void log(String msg, JavaModelException ex)
	{
		ExceptionHandler.getOTDTCoreExceptionHandler().logException(msg, ex);
	}
//{OT_COPY_PASTE: SourceRefElement, STATE: 3.4 M7
	/**
	 * Return the first instance of IOpenable in the hierarchy of this
	 * type (going up the hierarchy from this type);
	 */
	public IOpenable getOpenableParent() 
	{
		IJavaElement current = getParent();
		while (current != null){
			if (current instanceof IOpenable)
			{
				return (IOpenable) current;
			}
//{ObjectTeams : Teams have no parents in the ot-hierarchy.
			if(current.getElementType() == IOTJavaElement.TEAM)
			{
				IOTType otElement = (IOTType) current;
				current = otElement.getCorrespondingJavaElement();
			}
//haebor}			
			current = current.getParent();
		}
		return null;
	}
	
//{OTModelUpdate : many of this methods shouldn't be delegated
//                 to the corresponding method. Started with these three.
//	public String getSource() throws JavaModelException
//	{
//	    return getIMethod().getSource();
//	}
//	
//	public ISourceRange getSourceRange() throws JavaModelException
//	{
//	    return getIMethod().getSourceRange();
//	}
//	public ISourceRange getNameRange() throws JavaModelException
//	{
//	    return getIMethod().getNameRange();
//	}
	
//haebor}
	/**
	 * @see ISourceReference
	 */
	public String getSource() throws JavaModelException 
	{
		IOpenable openable = getOpenableParent();
		IBuffer buffer = openable.getBuffer();
		if (buffer == null) 
		{
			return null;
		}
		ISourceRange range = getSourceRange();
		int offset = range.getOffset();
		int length = range.getLength();
		if (offset == -1 || length == 0 ) 
		{
			return null;
		}
		try 
		{
			return buffer.getText(offset, length);
		} 
		catch(RuntimeException ex) 
		{
			return null;
		}
	}
	/**
	 * @see ISourceReference
	 */
	public ISourceRange getSourceRange() throws JavaModelException 
	{
//{ObjectTeams: we don't have an ElementInfo but we know sourcestart, sourceend
		return new SourceRange(_declarationSourceStart, _declarationSourceEnd - _declarationSourceStart + 1);
//haebor}		
//orig:		
//		SourceRefElementInfo info = (SourceRefElementInfo) getElementInfo();
//		return info.getSourceRange();
	}
	
//haebor}
	public ISourceRange getNameRange() throws JavaModelException
	{
		ISourceRange range = new SourceRange(_sourceStart, _sourceEnd-_sourceStart+1);
	    return range;
	}
	/** Answer the name that represents this mapping. */
	protected String getSourceName() {
		return super.getElementName();
	}
	
//delegates	
	IMethod getIMethod()
	{
	    return (IMethod) getCorrespondingJavaElement();
	}
	
	public String[] getExceptionTypes() throws JavaModelException
	{
	    return getIMethod().getExceptionTypes();
	}
	
	/**
	 * @deprecated (cf. IMethod.getTypeParameterSignatures())
	 */
	public String[] getTypeParameterSignatures() throws JavaModelException
	{
	    return getIMethod().getTypeParameterSignatures();
	}
	
	public int getNumberOfParameters()
	{
	    return getIMethod().getNumberOfParameters();
	}
	
	public String[] getParameterNames() throws JavaModelException
	{
		if (   this._roleMethodHandle != null
			&& !this._roleMethodHandle.isIncomplete())
				return this._roleMethodHandle.getArgumentNames();
	    return getIMethod().getParameterNames();
	}
	
	public String[] getParameterTypes()
	{
	    return getIMethod().getParameterTypes();
	}
	
	public String getReturnType() throws JavaModelException
	{
		if (   this._roleMethodHandle != null
			&& !this._roleMethodHandle.isIncomplete())
			return this._roleMethodHandle.getReturnType();
	    return getIMethod().getReturnType();
	}
	
	public String getSignature() throws JavaModelException
	{
	    return getIMethod().getSignature();
	}
	
	public boolean isConstructor() throws JavaModelException
	{
	    return getIMethod().isConstructor();
	}
	
	public boolean isMainMethod() throws JavaModelException
	{
	    return getIMethod().isMainMethod();
	}
	
	public boolean isSimilar(IMethod method)
	{
	    return getIMethod().isSimilar(method);
	}
	
	public IClassFile getClassFile()
	{
	    return getIMethod().getClassFile();
	}
	
	public ICompilationUnit getCompilationUnit()
	{
	    return getIMethod().getCompilationUnit();
	}
	
	public IType getDeclaringType()
	{
	    return getIMethod().getDeclaringType();
	}
	
	public int getFlags() throws JavaModelException
	{
	    return 0; // SH: method mappings have no regular flags. orig: getIMethod().getFlags();
	}
	
	public IType getType(String name, int occurrenceCount)
	{
	    return getIMethod().getType(name, occurrenceCount);
	}
	
	public boolean isBinary()
	{
	    return getIMethod().isBinary();
	}
	public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().copy(container, sibling, rename, replace, monitor);
	}
	
	public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().delete(force, monitor);
	}
	
	public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().move(container, sibling, rename, replace, monitor);
	}
	
	public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().rename(name, replace, monitor);
	}
	
    public boolean hasSignature()
    {
        return _hasSignature;
    }
    
	public boolean exists()
	{
		IJavaElement parent = getParent();
		if (!parent.exists())
			return false;
		try {
			for (IJavaElement child : ((IType)parent).getChildren())
				if (this.equals(child)) {
					// side-effect: fetch source range:
					if (this != child && this._declarationSourceStart == 0) {
						MethodMapping other = (MethodMapping) child;
						this._declarationSourceStart = other._declarationSourceStart;
						this._declarationSourceEnd   = other._declarationSourceEnd;
						this._sourceStart = other._sourceStart;
						this._sourceEnd   = other._sourceEnd;
					}
					return true;
				}
		} catch (JavaModelException e) { /* nop, will return false */ }
		return false;
	}

	public boolean isStructureKnown() throws JavaModelException
	{
        // See exists()
		return getParent().isStructureKnown();
	}
	
	public String getKey() {
		// km: perhaps: calculating own key would be better
		return getIMethod().getKey();
	}
	
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return NO_TYPE_PARAMETERS; // must not return null!
	}
	
	public String[] getRawParameterNames() throws JavaModelException {
		return EMPTY_STRING_ARRAY;
	}

	public ITypeParameter getTypeParameter(String name) {
		return new TypeParameter((JavaElement) getCorrespondingJavaElement(), name);
	}

	public boolean isResolved() {
		return false;
	}
	
	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	//OT_COPY_PASTE from Member.getCategories(). STATE: 3.4 M7, checked at 3.5 M7
	@SuppressWarnings("unchecked")
	public String[] getCategories() throws JavaModelException {
		IType type = (IType) getAncestor(IJavaElement.TYPE);
		if (type == null) return CharOperation.NO_STRINGS;
		if (type.isBinary()) {
			return CharOperation.NO_STRINGS;
		} else {
			SourceTypeElementInfo info = (SourceTypeElementInfo) ((SourceType) type).getElementInfo();
			HashMap map = info.getCategories();
			if (map == null) return CharOperation.NO_STRINGS;
			String[] categories = (String[]) map.get(this);
			if (categories == null) return CharOperation.NO_STRINGS;
			return categories;
		}
	}
	
	//OT_COPY_PASTE from Member.getJavadocRange(). STATE: 3.4 M7
	public ISourceRange getJavadocRange() throws JavaModelException {
		ISourceRange range= this.getSourceRange();
		if (range == null) return null;
		IBuffer buf= null;
		if (this.isBinary()) {
			buf = this.getClassFile().getBuffer();
		} else {
			ICompilationUnit compilationUnit = this.getCompilationUnit();
			if (!compilationUnit.isConsistent()) {
				return null;
			}
			buf = compilationUnit.getBuffer();
		}
		final int start= range.getOffset();
		final int length= range.getLength();
		if (length > 0 && buf.getChar(start) == '/') {
			IScanner scanner= ToolFactory.createScanner(true, false, false, false);
			scanner.setSource(buf.getText(start, length).toCharArray());
			try {
				int docOffset= -1;
				int docEnd= -1;
				
				int terminal= scanner.getNextToken();
				loop: while (true) {
					switch(terminal) {
						case ITerminalSymbols.TokenNameCOMMENT_JAVADOC :
							docOffset= scanner.getCurrentTokenStartPosition();
							docEnd= scanner.getCurrentTokenEndPosition() + 1;
							terminal= scanner.getNextToken();
							break;
						case ITerminalSymbols.TokenNameCOMMENT_LINE :
						case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
							terminal= scanner.getNextToken();
							continue loop;
						default :
							break loop;
					}
				}
				if (docOffset != -1) {
					return new SourceRange(docOffset + start, docEnd - docOffset + 1);
				}
			} catch (InvalidInputException ex) {
				// try if there is inherited Javadoc
			}
		}
		return null;
	}

//{CRIPPLE:
	public int getOccurrenceCount() {
		// TODO Auto-generated method stub
		return 0;
	}
// km}

	@Override
	public IJavaElement getParent() {
		// the parent of a method mapping must be a role.
		IJavaElement parent = super.getParent();
		if (parent instanceof IRoleType)
			return parent;
		return OTModelManager.getOTElement((IType)parent);
	}

	/**
	 * Copied from Member.
	 * @see IMember#getTypeRoot()
	 */
	public ITypeRoot getTypeRoot() {
		IJavaElement element = getParent();
		while (element instanceof IMember) {
			element= element.getParent();
		}
		return (ITypeRoot) element;
	}

	public OTJavaElement resolved(Binding binding) {
		char[] uniqueKey = binding.computeUniqueKey();
		if (uniqueKey == null)
			throw new AbortCompilation(); // better than NPE below
		return resolved(uniqueKey);
	}

	public abstract OTJavaElement resolved(char[] uniqueKey);
	
	
	public IAnnotation getAnnotation(String name) {
		return new Annotation((JavaElement)this.getCorrespondingJavaElement(), name);
	}
	
	public IAnnotation[] getAnnotations() throws JavaModelException {
		return getIMethod().getAnnotations();
	}
}
