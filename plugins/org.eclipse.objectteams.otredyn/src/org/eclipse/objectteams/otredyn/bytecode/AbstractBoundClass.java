/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2009, 2015 Oliver Frank and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Oliver Frank - Initial API and implementation
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.objectteams.otredyn.runtime.ClassIdentifierProviderFactory;
import org.eclipse.objectteams.otredyn.runtime.IBinding;
import org.eclipse.objectteams.otredyn.runtime.IBoundClass;
import org.eclipse.objectteams.otredyn.runtime.IMethod;
import org.eclipse.objectteams.otredyn.runtime.TeamManager;
import org.eclipse.objectteams.otredyn.runtime.ISubclassWiringTask;
import org.eclipse.objectteams.otredyn.transformer.IWeavingContext;
import org.eclipse.objectteams.otredyn.transformer.jplis.ObjectTeamsTransformer;
import org.objectweb.asm.Opcodes;

/**
 * This class represents a java class.
 * It stores the information about a class parsed from the bytecode and
 * it handles callin and decapsulation bindings for the class
 * @author Oliver Frank
 */
public abstract class AbstractBoundClass implements IBoundClass {
	
	private static enum WeavingTaskType {
		WEAVE_BINDING_OF_SUBCLASS,
		WEAVE_BINDING,
		/**
		 * Weaving of bindings for overridden methods whose super version is callin-bound.
		 * Includes replacement of "wicked super calls" towards those bound super methods.
		 */
		WEAVE_INHERITED_BINDING,
		WEAVE_METHOD_ACCESS,
		WEAVE_FIELD_ACCESS,
		WEAVE_INHERITED_MEMBER_ACCESS,
		WEAVE_BASE_INFRASTRUCTURE
	}

	/**
	 * A structure that is internally used to store which methods are still woven
	 * and which has to be woven.
	 */
	private static class WeavingTask {
		
		private WeavingTaskType weavingTaskType;
		private String memberName;
		private String memberSignature;
		private int baseFlags;
		private boolean doAllTransformations;
		private boolean isHandleCovariantReturn;

		public WeavingTask(WeavingTaskType weavingTaskType, String memberName, String memberSignature, int baseFlags, boolean handleCovariantReturn) {
			this.weavingTaskType = weavingTaskType;
			this.memberName = memberName;
			this.memberSignature = memberSignature;
			this.isHandleCovariantReturn = handleCovariantReturn;
			this.baseFlags = baseFlags;
		}
		
		public WeavingTask(WeavingTaskType weavingTaskType, Method method, WeavingTask upstream) {
			this(weavingTaskType, method.getName(), method.getSignature(),
					upstream.getBaseFlags(), upstream.isHandleCovariantReturn());
		}

		public WeavingTask(WeavingTaskType type) {
			this(type, null, null, 0, false);
		}

		/**
		 * Returns the type of the WeavingTask
		 * @return
		 */
		public WeavingTaskType getType() {
			return weavingTaskType;
		}
		
		/**
		 * Returns the name of the member, that has to be woven
		 * @return
		 */
		public String getMemberName() {
			return memberName;
		}

		/**
		 * Returns the signature of the member, that has to be woven
		 * @return
		 */
		public String getMemberSignature() {
			return memberSignature;
		}
		
		public int getBaseFlags() {
			return baseFlags;
		}
		
		/**
		 * This information is only needed for callin bindings.
		 * @return If true, all transformation has to be done, if false,
		 * only callAllBindings has to be redefined
		 */
		public boolean doAllTransformations() {
			return doAllTransformations;
		}

		public void setDoAllTransformations(boolean doAllTransformations) {
			this.doAllTransformations = doAllTransformations;
		}

		public boolean isHandleCovariantReturn() {
			return this.isHandleCovariantReturn;
		}
	}

	// completed WeavingTasks for callin bindings mapped by the method,
	// that was woven
	private Map<Method, WeavingTask> completedBindingTasks;
	
	// not completed WeavingTasks for callin bindings mapped by the method,
	// that has to be woven
	public Map<Method, WeavingTask> openBindingTasks;
	
	// completed WeavingTasks for decapsulation bindings mapped by the member,
	// that was woven
	private Map<Member, WeavingTask> completedAccessTasks;
	
	// open WeavingTasks for decapsulation bindings mapped by the member,
	// that has to be woven
	private Map<Member, WeavingTask> openAccessTasks;
	
	private List<ISubclassWiringTask> wiringTasks;
	
	//FQN (e.g. "foo.bar.MyClass")
	private String name;
	
	//internal FQN (e.g. "foo/bar/MyClass.class")
	private String internalName;
	
	// A globally unique identifier for the class
	private String id;
	private String superClassName;
	private String internalSuperClassName;
	private String[] internalSuperInterfaces;
	private AbstractBoundClass superclass;
	private AbstractBoundClass enclosingClass;
	private Map<String, Method> methods;
	private Map<String, Field> fields;
	protected Map<AbstractBoundClass, Object> subclasses;
	
	// Is the java class, that was represented by the AbstractBoundClass
	// already loaded by a class loader or not
	private boolean isLoaded;
	
	private int modifiers;

	private int otClassFlags;
	private boolean implicitTeamActivationEnabled = false;
	private Set<String> methodsForImplicitActivation;

	protected ClassLoader loader;

	// callback
	protected IWeavingContext weavingContext;



	/**
	 * No public constructor, beacause only the ClassRepository should
	 * create AbstractBoundClasses
	 * @param name dot-separated class name
	 * @param id unique identifier, able to differentiate same-named classes from different classloaders
	 * @param loader classloader responsible for loading this class
	 */
	protected AbstractBoundClass(String name, String id, ClassLoader loader) {
		if (name.indexOf('/')!= -1)
			new RuntimeException(name).printStackTrace(System.out);
		this.name = name;
		this.internalName = name.replace('.', '/');
		this.id = id;
		this.loader = loader;
		completedBindingTasks = new IdentityHashMap<Method, WeavingTask>();
		openBindingTasks = new IdentityHashMap<Method, WeavingTask>();
		openAccessTasks = new IdentityHashMap<Member, WeavingTask>();
		completedAccessTasks = new IdentityHashMap<Member, WeavingTask>();
		methods = new HashMap<String, Method>();
		fields = new HashMap<String, Field>();
		subclasses = new IdentityHashMap<AbstractBoundClass, Object>();
		
		// don't fetch a anonymous subclass for a anonymous subclass
		if (!name.equals(ClassRepository.ANONYMOUS_SUBCLASS_NAME)) {
			AbstractBoundClass anonymousSubclass = ClassRepository
					.getInstance().getAnonymousSubclass(this);
			subclasses.put(anonymousSubclass, null);
		}
	}

	/**
	 * Returns the name of the Class
	 * @return
	 */
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public boolean isAnonymous() {
		return getName().equals(ClassRepository.ANONYMOUS_SUBCLASS_NAME);
	}
	
	/**
	 * Returns the classloader responsible for loading this class.
	 * @return
	 */
	public ClassLoader getClassLoader() {
		return this.loader;
	}
	
	/**
	 * Set the name of the super class of this class
	 * @param superClassName
	 */
	public void setSuperClassName(String superClassName) {
		if (superClassName == null) {
			return;
		}
		this.superClassName = superClassName.replace('/', '.');
		this.internalSuperClassName = superClassName.replace('.', '/');
	}

	public void setSuperInterfaces(String[] interfaces) {
		if (interfaces == null)
			return;
		this.internalSuperInterfaces = interfaces;
	}

	public boolean isJavaLangObject() {
		return this.name.equals("java.lang.Object");
	}

	/**
	 * Is the class a interface?
	 * @return
	 */
	public boolean isInterface() {
		parseBytecode();
		return (this.modifiers & Opcodes.ACC_INTERFACE) != 0;
	}
	
	/**
	 * Does this AbstractBoundClass represents a Team.
	 * @return if true, the method getTeam of the ClassRepository 
	 * could be called to get an instance of AbstractTeam 
	 */
	public boolean isTeam() {
		parseBytecode();
		return (this.modifiers & Types.TEAM) != 0;
	}
	
	/**
	 * Number of outer instances required in an instance of this class.
	 */
	public int nestingDepth() {
		parseBytecode();
		if ((this.modifiers & Opcodes.ACC_STATIC) != 0)
			return 0;
		if (this.enclosingClass != null)
			return this.enclosingClass.nestingDepth()+1;
		return 0;
	}

	/**
	 * Store class modifiers (incl. visibility, AccStatic, AccInterface, AccStatic
	 */
	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	public void setOTClassFlags(int flags) {
		this.otClassFlags = flags;		
	}
	
	public boolean isRole() {
		return (this.otClassFlags & Types.ROLE_FLAG) != 0;
	}

	public boolean isProtected() {
		return (this.modifiers & Opcodes.ACC_PROTECTED) != 0;
	}

	public void enableImplicitActivation() {
		this.implicitTeamActivationEnabled = true;		
	}

	public void registerMethodForImplicitActivation(String methodNameAndDesc) {
		if (this.methodsForImplicitActivation == null)
			this.methodsForImplicitActivation = new HashSet<String>();
		this.methodsForImplicitActivation.add(methodNameAndDesc);
	}

	public boolean hasMethodImplicitActivation(String methodNameAndDesc) {
		if (this.implicitTeamActivationEnabled) // for all methods
			return true;
		if (this.methodsForImplicitActivation == null)
			return false;
		return this.methodsForImplicitActivation.contains(methodNameAndDesc);
	}

	/**
	 * Do all needed transformations needed at load time:
	 * Add the interface IBoundBase2
	 * Add the empty method callOrig
	 * Add the empty method callOrigStatic
	 * Add the empty method access
	 * Add the empty method accessStatic
	 * Add the empty method callAllBindings
	 */
	public void transformAtLoadTime(IWeavingContext weavingContext) {
		this.weavingContext = weavingContext;
		handleTaskList();
	}

	/**
	 * Mark as loaded
	 */
	public void setLoaded() {
		this.isLoaded = true;
	}
	
	/**
	 * Returns an instance of AbstractBoundClass that represents 
	 * the super class of this class.
	 * It parses the bytecode, if that has not already been done 
	 * @return an instance of AbstractBoundClass that represents 
	 * the super class of this class
	 */
	public synchronized AbstractBoundClass getSuperclass() {
		parseBytecode();
		if (superClassName != null && superclass == null) {
			String superclassId = ClassIdentifierProviderFactory.getClassIdentifierProvider().getSuperclassIdentifier(id, internalSuperClassName);
			
			//if superclassId is null the class could be "Object" or an interface
			if (superclassId != null) {
				superclass = ClassRepository.getInstance().getBoundClass(superClassName, superclassId, loader);
				superclass.addSubclass(this);
				// FIXME(SH): can we avoid adding all subclasses to j.l.Object?
			}
		}
		return superclass;
	}
	
	/**
	 * Returns the internal names of all superInterfaces or null.
	 */
	public /*@Nullable*/ String[] getSuperInterfaceNames() {
		parseBytecode();
		return this.internalSuperInterfaces;
	}

	/**
	 * Returns an instance of AbstractBoundClass that represents 
	 * the enclosing class of this class.
	 * It parses the bytecode, if that has not already been done 
	 * @return an instance of AbstractBoundClass that represents 
	 * the enclosing class of this class
	 */
	public synchronized AbstractBoundClass getEnclosingClass() {
		parseBytecode();
		int pos = this.internalName.lastIndexOf('$');
		if (pos != -1) {
			String enclosingClassName = this.internalName.substring(0, pos); 
			// FIXME(SH): do we need a new getEnclosingClassIdentifier?
			String enclosingClassID = ClassIdentifierProviderFactory.getClassIdentifierProvider().getSuperclassIdentifier(id, enclosingClassName);
			
			if (enclosingClassID != null) {
				enclosingClass = ClassRepository.getInstance().getBoundClass(enclosingClassName, enclosingClassID, loader);
				enclosingClass.addSubclass(this);
			}
		}
		return enclosingClass;
	}

	public synchronized String getEnclosingClassName() {
		parseBytecode();
		int pos = this.internalName.lastIndexOf('$');
		if (pos != -1) {
			return this.internalName.substring(0, pos); 
		}
		return null;
	}

	/**
	 * This method parses the bytecode, if that has not already been done 
	 */
	public abstract void parseBytecode();
	
	/**
	 * Returns the internal name of the super class of this class
	 * It parses the bytecode, if that has not already been done
	 * @return
	 */
	public String getInternalSuperClassName() {
		parseBytecode();
		return internalSuperClassName;
	}

	/**
	 * Returns the internal name of this class
	 * @return
	 */
	public String getInternalName() {
		return internalName;
	}
	
	/**
	 * Returns the name of the super class of this class
	 * It parses the bytecode, if that has not already been done
	 * @return
	 */
	public String getSuperClassName() {
		parseBytecode();
		return superClassName;
	}

	/**
	 * Adds a method to this class.
	 * This method is intended to be called, 
	 * while parsing the bytecode
	 * @param name the name of the field
	 * @param desc the signature of the method
	 * @param isStatic is this method static
	 * @param isPrivate is this method private
	 */
	public void addMethod(String name, String desc, boolean isStatic, int accessFlags) {
		String methodKey = getMethodKey(name, desc);
		Method method = methods.get(methodKey);
		// Does this method already exists?
		// Methods are created by getMethod, if the class is not loaded
		if (method == null) {
			method = new Method(name, desc, isStatic, accessFlags);
			method.setImplemented(true);
			methods.put(methodKey, method);
		} else {
			// Yes, so set additional information.
			method.setImplemented(true);
			method.setStatic(isStatic);
		}
	}

	/** Method signature sauf the return type. */
	private String getMethodKey(String name, String desc) {
		int pos = desc.indexOf(')');
		return name+desc.substring(0,pos+1);
	}

	/**
	 * Adds a field to this class.
	 * This method is intended to be called, 
	 * while parsing the bytecode
	 * @param name the name of the field
	 * @param desc the signature of the field
	 * @param isStatic is this field static
	 * @param accessFlag ACC_PUBLIC, ACC_PROTECTED, ACC_PRIVATE or 0.
	 */
	public void addField(String name, String desc, boolean isStatic, int accessFlags) {
		Field field = fields.get(name);
		if (field == null) {
			field = new Field(name, desc, isStatic, accessFlags);
			fields.put(name, field);
		} else {
			field.setStatic(isStatic);
		}
	}

	public synchronized Method getMethod(String name, String desc, int flags, boolean allowCovariantReturn) {
		parseBytecode();
		String methodKey = getMethodKey(name, desc);
		Method method = methods.get(methodKey);
		if (!allowCovariantReturn && method != null && !method.getSignature().equals(desc))
			method = null; // don't use this
		if (method == null) {
			// class was not yet loaded
			method = new Method(name, desc, ((flags&IBinding.STATIC_BASE) != 0), 0/*accessFlags*/);
			methods.put(methodKey, method);
		}
		return method;
	}
	
	Method getMethod(WeavingTask task) {
		return getMethod(task.getMemberName(), task.getMemberSignature(), task.getBaseFlags(), task.isHandleCovariantReturn());
	}

	Method getMethod(Method method, WeavingTask task) {
		return getMethod(method.getName(), method.getSignature(), task.getBaseFlags(), task.isHandleCovariantReturn());
	}

	// same as above but specifically request a static/non-static method
	public synchronized Method getMethod(String name, String desc, boolean allowCovariantReturn, boolean isStatic) {
		parseBytecode();
		String methodKey = getMethodKey(name, desc);
		Method method = methods.get(methodKey);
		if (!allowCovariantReturn && method != null && !method.getSignature().equals(desc))
			method = null; // don't use this
		if (method == null) {
			// class was not yet loaded
			method = new Method(name, desc);
			method.setStatic(isStatic);
			methods.put(methodKey, method);
		}
		assert method.isStatic() == isStatic : "Mismatching static/non-static methods "+getName()+'.'+name+desc;
		return method;
	}

	public synchronized Field getField(String name, String desc) {
		parseBytecode();
		Field field = fields.get(name);
		if (field == null) {
			// class was not yet loaded
			field = new Field(name, desc);
			fields.put(name, field);
		}
		return field;
	}


	public String getMethodIdentifier(IMethod method) {
		return getId() + method.getName() + method.getSignature();
	}

	/**
	 * Adds a subclass to this class
	 * @param subclass
	 */
	protected void addSubclass(AbstractBoundClass subclass) {
		subclasses.put(subclass, null);
	}

	/**
	 * Remove subclass from this class. It's only needed
	 * to remove a anonymous subclass, if a real subclass is loaded
	 * @param subclass
	 */
	protected void removeSubclass(AbstractBoundClass subclass) {
		subclasses.remove(subclass);
	}

	/**
	 * Returns all subclasses of this class, 
	 * including the anonymous subclass
	 * @return
	 */
	private Collection<AbstractBoundClass> getSubclasses() {
		return subclasses.keySet();
	}

	/**
	 * Handle all open weaving tasks for this class.
	 * It redefines the class, if it is not called while loading
	 */
	public synchronized void handleTaskList() {
		if (isTransformationActive()) return;

		Set<Map.Entry<Method, WeavingTask>> bindingEntrySet = openBindingTasks
				.entrySet();

		Set<Map.Entry<Member, WeavingTask>> accessEntrySet = openAccessTasks
				.entrySet();

		// Are there not handled callin or decapsulation bindings 
		// for this class
		if (bindingEntrySet.size() > 0 || accessEntrySet.size() > 0) {
			// Yes, so start the transformation, parse the bytecode
			// and do load time transforming, if this method is called
			// at load time
			startTransformation();
			parseBytecode();
			prepareAsPossibleBaseClass();
			prepareTeamActivation();
			prepareLiftingParticipant();
		} else if (isFirstTransformation()) {
			// No, so only do load time transforming, if this method is called
			// at load time
			startTransformation();
			prepareAsPossibleBaseClass();
			prepareTeamActivation();
			prepareLiftingParticipant();
			endTransformation();
		}

		// collect other classes for which new tasks are recorded, to flush those tasks in bulk at the end
		Set<AbstractBoundClass> affectedClasses = new HashSet<AbstractBoundClass>();

		for (Map.Entry<Method, WeavingTask> entry : bindingEntrySet) {
			WeavingTask task = entry.getValue();
			Method method = entry.getKey();
			switch (task.getType()) {
			// Weave callin binding to a method of a subclass, that is not implemented
			// in the subclass
			case WEAVE_BINDING_OF_SUBCLASS:
				// Is the method implemented in this class?
				if (method.isImplemented()) {
					// Yes, so weave this class
					weaveBindingOfSubclass(task);
				} else {
					//No, so just delegate the weaving task to the superclass
					AbstractBoundClass superclass = getSuperclass();
					// If superclass is null, there is something wrong
					if (superclass != null) {
						superclass.addWeavingTask(task, true/*standBy*/);
						affectedClasses.add(superclass);
						weaveSuperCallInCallOrig(task); // ensure we're actually calling that super version
					}
				}
				break;
			// Weave callin binding to a method of this class
			case WEAVE_BINDING:
				if (method.isStatic()) {
					weaveBindingInStaticMethod(task);
				} else {
					// Is the method implemented in this class?
					if (method.isImplemented()) {
						// So redefine the method
						weaveBindingInImplementedMethod(task);
					} else {
						//No, so weave this class and delegate to the super class
						weaveBindingInNotImplementedMethod(task);
						AbstractBoundClass superclass = getSuperclass();
						if (weavingContext.isWeavable(getSuperClassName())) {
							Method superMethod = superclass.getMethod(method, task);
							if (superMethod != null) {
								WeavingTask newTask = new WeavingTask(WeavingTaskType.WEAVE_BINDING_OF_SUBCLASS, superMethod, task);
								superclass.addWeavingTask(newTask, true/*standBy*/);
								affectedClasses.add(superclass);
							}
						}
					}

// Original comment:
//   If this method is private, the callin binding is not
//   inherited by the subclasses
// However, this conflicts with test415_nonexistingBaseMethod3i,
// where an empty callAllBindings() was overriding a non-empty one.
// see also Method.getId()
//					if (!method.isPrivate()) {
						// Delegate the WeavingTask to the subclasses
						for (AbstractBoundClass subclass : getSubclasses()) {
							Method subMethod = subclass.getMethod(method, task);
							WeavingTask newTask = new WeavingTask(WeavingTaskType.WEAVE_INHERITED_BINDING, subMethod, task);
							subclass.addWeavingTask(newTask, true/*standBy*/);
							affectedClasses.add(subclass);
							TeamManager.mergeJoinpoints(this, subclass, method, subMethod, task.isHandleCovariantReturn());
						}
//					}
				}
				break;
			// Weave Binding inherited from the superclass
			case WEAVE_INHERITED_BINDING:
				if (method.isImplemented()) {
					weaveBindingInImplementedMethod(task);
				} else {
					weaveBindingInNotImplementedMethod(task);
				}
				replaceWickedSuperCalls(getSuperclass(), method);

				// Delegate the WeavingTask to the subclasses
				for (AbstractBoundClass subclass : getSubclasses()) {
					Method subMethod = subclass.getMethod(method, task);
					WeavingTask newTask = new WeavingTask(WeavingTaskType.WEAVE_INHERITED_BINDING, subMethod, task);
					subclass.addWeavingTask(newTask, true/*standBy*/);
					affectedClasses.add(subclass);
					TeamManager.mergeJoinpoints(this, subclass, method, subMethod, task.isHandleCovariantReturn());
				}

				break;
			}
			
			// Mark all WeavingTasks for callin bindings 
			// as completed
			completedBindingTasks.put(method, task);
		}
		
		//handle all WeavinTasks for decapsulation bindings
		for (Map.Entry<Member, WeavingTask> entry : accessEntrySet) {
			WeavingTask task = entry.getValue();
			Member member = entry.getKey();
			
			switch (task.getType()) {
			// handle decapsulation binding to a field
			case WEAVE_FIELD_ACCESS:
				prepareForFirstMemberAccess();
				Field field = getField(task.getMemberName(), task
						.getMemberSignature());
				weaveFieldAccess(field, field.getGlobalId(this));
				if (!field.isStatic()) {
					// If the field is not static it could be accessed through a subclass
					// so weave the subclass
					for (AbstractBoundClass subclass : getSubclasses()) {
						WeavingTask newTask = new WeavingTask(WeavingTaskType.WEAVE_INHERITED_MEMBER_ACCESS);
						subclass.addWeavingTask(newTask, true/*standBy*/);
						affectedClasses.add(subclass);
					}
				}
				break;
			// handle decaspulation binding to a method
			case WEAVE_METHOD_ACCESS:
				prepareForFirstMemberAccess();
				Method method = getMethod(task);
				weaveMethodAccess(method, method.getGlobalId(this));
				if (!method.isStatic()) {
					// If the method is not static it could be accessed through a subclass
					// so weave the subclass
					for (AbstractBoundClass subclass : getSubclasses()) {
						WeavingTask newTask = new WeavingTask(WeavingTaskType.WEAVE_INHERITED_MEMBER_ACCESS);
						subclass.addWeavingTask(newTask, true/*standBy*/);
						affectedClasses.add(subclass);
					}
				}
				break;
			case WEAVE_INHERITED_MEMBER_ACCESS:
				prepareForFirstMemberAccess();
				break;
			case WEAVE_BASE_INFRASTRUCTURE:
				prepareForFirstTransformation();
				break;
			}
			
			// Mark all WeavingTasks for decapsulation bindings 
			// as completed
			completedAccessTasks.put(member, task);
		}
		// flush collected tasks of other affected classes:
		for (AbstractBoundClass affected : affectedClasses)
			if (affected.isLoaded)
				affected.handleTaskList();

		if (openBindingTasks.size() > 0 || openAccessTasks.size() > 0) {
			// Weave the class, if the method is not called at load time
			endTransformation();
		}
		openBindingTasks.clear();
		openAccessTasks.clear();
	}

	public void handleAddingOfBinding(IBinding binding) {
		WeavingTaskType type = null;
		WeavingTask task = null;
		switch (binding.getType()) {
		case CALLIN_BINDING:
			type = WeavingTaskType.WEAVE_BINDING;
			break;
		case FIELD_ACCESS:
			type = WeavingTaskType.WEAVE_FIELD_ACCESS;
			break;
		case METHOD_ACCESS:
			type = WeavingTaskType.WEAVE_METHOD_ACCESS;
			break;
		case ROLE_BASE_BINDING:
			task = new WeavingTask(WeavingTaskType.WEAVE_BASE_INFRASTRUCTURE);
			break;
		default:
			throw new RuntimeException("Unknown binding type: "
					+ binding.getType().name());
		}
// TODO: once needed for test4141_dangerousCallinBinding3, but no longer.
// Instead it causes the need to redefine classes in OT/Equinox, hence disabled.
//		if (   binding.getType() == IBinding.BindingType.CALLIN_BINDING 
//			&& !binding.getBoundClass().equals(binding.getDeclaringBaseClassName()))
//			try {
//				// need to load the declaring base class outside the transform() callback:
//				this.loader.loadClass(binding.getDeclaringBaseClassName().replace('/', '.'));
//			} catch (ClassNotFoundException e) {
//				throw new NoClassDefFoundError(e.getMessage());
//			}
		if (task == null)
			task = new WeavingTask(type, binding.getMemberName(), binding.getMemberSignature(), 
											binding.getBaseFlags(), binding.isHandleCovariantReturn());
		addWeavingTask(task, false);
	}
	
	private void addWeavingTask(WeavingTask task, boolean standBy) {
		boolean isNewTask = addWeavingTaskLazy(task);

		if (this.isLoaded && isNewTask && !standBy) {
			handleTaskList();
		}
	}
	
	private boolean addWeavingTaskLazy(WeavingTask task) {
		WeavingTaskType type = task.getType();
		boolean isNewTask = false;
		if (type == WeavingTaskType.WEAVE_BINDING
				|| type == WeavingTaskType.WEAVE_BINDING_OF_SUBCLASS
				|| type == WeavingTaskType.WEAVE_INHERITED_BINDING) {
			isNewTask = addBindingWeavingTask(task);
		} else {
			isNewTask = addAccessWeavingTask(task);
		}
		
		return isNewTask;
	}

	/**
	 * Handle a new WeavingTask for decapsulation and 
	 * figures out if weaving is needed for this task 
	 * @param task
	 * @return
	 */
	private boolean addAccessWeavingTask(WeavingTask task) {
		WeavingTaskType type = task.getType();
		Member member = null;
		switch (type) {
		case WEAVE_FIELD_ACCESS:
			member = getField(task.getMemberName(), task.getMemberSignature());
			break;
		case WEAVE_METHOD_ACCESS:
			member = getMethod(task);
			break;
		case WEAVE_INHERITED_MEMBER_ACCESS:
		case WEAVE_BASE_INFRASTRUCTURE:
			openAccessTasks.put(null, task);
			return true;
		}
		
		synchronized (member) {
			WeavingTask prevTask = completedAccessTasks.get(member);
			// Is there a already completed task for the member?
			if (prevTask == null) {
				// No, so check the open tasks
				prevTask = openAccessTasks.get(member);
			}
	
			// Is there a open task for the member?
			if (prevTask == null) {
				// No, so weaving is needed
				openAccessTasks.put(member, task);
				return true;
			} else {
				//Yes, so weaving is not needed
				return false;
			}
		}
	}

	/**
	 * Handle a new WeavingTask for a callin binding and 
	 * figures out if weaving is needed for this task 
	 * @param task
	 * @return
	 */
	private boolean addBindingWeavingTask(WeavingTask task) {
		Method method = getMethod(task);
		synchronized (method) {
			WeavingTask prevTask = completedBindingTasks.get(method);
			// Is there a already completed task for the method?
			if (prevTask == null) {
				// No, so check the open tasks
				prevTask = openBindingTasks.get(method);
			}
		
			// Is there a open task for the member?
			if (prevTask == null) {
				//No, so weaving is needed
				task.setDoAllTransformations(true);
				openBindingTasks.put(method, task);
				return true;
			}
	
			switch (prevTask.getType()) {
			case WEAVE_BINDING:
				return false;
			case WEAVE_BINDING_OF_SUBCLASS:
				// In  this case only the callAllBings was redefined.
				if (task.getType() != WeavingTaskType.WEAVE_BINDING_OF_SUBCLASS) {
					// Do the other transformations, if the new WeavingTask is not the same
					// as already existing
					openBindingTasks.put(method, task);
					return true;
				}
				return false;
			case WEAVE_INHERITED_BINDING:
				return false;
			default:
				throw new RuntimeException("Unknown WeavingTaskType: "
						+ prevTask.getType().name());
			}
		}
	}
	
	public void addWeavingOfSubclassTask(String methodName, String signature, boolean isStatic) {
		int flags = isStatic ? IBinding.STATIC_BASE : 0;
		addBindingWeavingTask(new WeavingTask(WeavingTaskType.WEAVE_BINDING_OF_SUBCLASS, methodName, signature, flags, isStatic));
	}

	/**
	 * Merge tasks of two AbstractBoundClasses (this class and a other).
	 * This method is called if a currently loaded has to be merged 
	 * with a anonymous subclass
	 * @param clazz
	 * @return
	 */
	protected boolean mergeTasks(AbstractBoundClass clazz) {
		boolean isNewTask = false;
		for (Map.Entry<Method, WeavingTask> entry : clazz.openBindingTasks
				.entrySet()) {
			isNewTask |= addWeavingTaskLazy(entry.getValue());
		}
		
		for (Map.Entry<Member, WeavingTask> entry : clazz.openAccessTasks
				.entrySet()) {
			isNewTask |= addWeavingTaskLazy(entry.getValue());
		}
		
		return isNewTask;
	}

	private void weaveBindingInStaticMethod(WeavingTask task) {
		prepareForFirstStaticTransformation();
		Method method = getMethod(task);
		int joinpointId = TeamManager
				.getJoinpointId(getMethodIdentifier(method));
		int boundMethodId = method.getGlobalId(this);

		moveCodeToCallOrig(method, boundMethodId);
		createDispatchCodeInOrgMethod(method, joinpointId, boundMethodId);
	}

	/**
	 * Do all transformations for a method, that is not implemented
	 * in this class
	 * @param task
	 */
	private void weaveBindingInNotImplementedMethod(WeavingTask task) {
		if ((task.getBaseFlags() & IBinding.STATIC_BASE) == 0)
			prepareForFirstTransformation();
		else
			prepareForFirstStaticTransformation();

		Method method = getMethod(task);
		int joinpointId = TeamManager
				.getJoinpointId(getMethodIdentifier(method));
		int boundMethodId = method.getGlobalId(this);
		if (task.doAllTransformations()) {
			createDispatchCodeInCallAllBindings(joinpointId, boundMethodId);
			// TODO(SH): instead of iterating superclasses fetch it from the Binding
			boolean isWeavable = true; // weavable unless we find it to be declared in an unweavable super
			AbstractBoundClass superClass = getSuperclass();
			while (superClass != null) {
				if (superClass.isJavaLangObject()) {
					isWeavable = false;
					break;
				}
				Method superMethod = superClass.getMethod(task);
				if (superMethod.isImplemented()) {
					isWeavable = ObjectTeamsTransformer.isWeavable(superClass.getInternalName());
					break;
				}
				superClass = superClass.getSuperclass();
			}
			if (isWeavable)
				createSuperCallInCallOrig(boundMethodId);
			else
				// can't weave into the declaring class, add an override here:
				createCallAllBindingsCallInOrgMethod(method, boundMethodId, true/*needToAddMethod*/);
		} else {
			createDispatchCodeInCallAllBindings(joinpointId, boundMethodId);
		}
	}

	/** 
	 * While delegating a task from a sub class to the super class,
 	 * ensure that the super version is actually called.
	 */
	private void weaveSuperCallInCallOrig(WeavingTask task) {
		prepareForFirstTransformation();
		Method method = getMethod(task);
		int boundMethodId = method.getGlobalId(this);
		if (task.doAllTransformations()) {
			createSuperCallInCallOrig(boundMethodId);
		}		
	}

	/**
	 * Do all transformations for a method, that is implemented
	 * in this class
	 * @param task
	 */
	private void weaveBindingInImplementedMethod(WeavingTask task) {
		prepareForFirstTransformation();
		Method method = getMethod(task);
		int joinpointId = TeamManager
				.getJoinpointId(getMethodIdentifier(method));
		int boundMethodId = method.getGlobalId(this);
		if (task.doAllTransformations()) {
			moveCodeToCallOrig(method, boundMethodId);
			createDispatchCodeInCallAllBindings(joinpointId, boundMethodId);
			createCallAllBindingsCallInOrgMethod(method, boundMethodId, false);
		} else {
			createDispatchCodeInCallAllBindings(joinpointId, joinpointId);
		}
	}

	/**
	 * Do all transformations for a method, that is bound 
	 * but not implemented in a subclass
	 * @param task
	 */
	private void weaveBindingOfSubclass(WeavingTask task) {
		prepareForFirstTransformation();
		Method method = getMethod(task);
		int boundMethodId = method.getGlobalId(this);
		moveCodeToCallOrig(method, boundMethodId);
		createCallAllBindingsCallInOrgMethod(method, boundMethodId, false);

	}

	/** replace all "wicked super calls" targeting the given targetMethod. */
	protected abstract void replaceWickedSuperCalls(AbstractBoundClass superclass, Method targetMethod);

	@Override
	public String toString() {
		return this.name+"["+this.id+"]";
	}

	// See AsmBoundClass or AsmWritableBoundClass for documentation
	
	protected abstract void startTransformation();

	protected abstract void endTransformation();

	protected abstract void prepareAsPossibleBaseClass();

	protected abstract void prepareTeamActivation();

	protected abstract void prepareLiftingParticipant();

	protected abstract void createSuperCallInCallOrig(int boundMethodId);

	protected abstract void createCallAllBindingsCallInOrgMethod(
			Method boundMethod, int joinpointId, boolean needToAddMethod);

	protected abstract void createDispatchCodeInCallAllBindings(
			int joinpointId, int boundMethodId);

	protected abstract void moveCodeToCallOrig(Method boundMethod, int boundMethodId);

	protected abstract void prepareForFirstTransformation();

	protected abstract void prepareForFirstStaticTransformation();

	public abstract boolean isFirstTransformation();
	
	public boolean isLoaded() { return isLoaded; }

	protected abstract void createDispatchCodeInOrgMethod(Method boundMethod,
			int joinpointId, int boundMethodId);

	protected abstract void prepareForFirstMemberAccess();

	protected abstract void weaveFieldAccess(Field field, int accessId);

	protected abstract void weaveMethodAccess(Method method, int accessId);

	public abstract boolean isTransformationActive();
	
	public abstract byte[] getBytecode();

	public void dump(byte[] classfileBuffer, String postfix) {}

	public Collection<String> getBoundBaseClasses() { return null; }

	public abstract int compare(String callinLabel1, String callinLabel2);

	public void addWiringTask(ISubclassWiringTask wiringTask) {
		if (this.wiringTasks == null)
			this.wiringTasks = new ArrayList<ISubclassWiringTask>();
		this.wiringTasks.add(wiringTask);
	}

	public void performWiringTasks(AbstractBoundClass superclass, AbstractBoundClass subclass) {
		if (this.wiringTasks == null)
			return;
		synchronized (this.wiringTasks) {			
			for (ISubclassWiringTask task : this.wiringTasks)
				task.wire(superclass, subclass);
			this.wiringTasks.clear();
		}
	}
}
