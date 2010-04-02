package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.inlinecallin;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.objectteams.otdt.core.ICallinMapping;

/**
 * @author Johannes Gebauer
 * 
 */
public class CallinBaseMethodInfo {

	private IMethod _method;
	private ICallinMapping _callinMapping;
	private String _newMethodName;

	public CallinBaseMethodInfo(IMethod _method, ICallinMapping mapping) {
		super();
		this._method = _method;
		this._callinMapping = mapping;
	}

	public IMethod getMethod() {
		return _method;
	}

	public ICallinMapping getCallinMapping() {
		return _callinMapping;
	}

	public void setNewMethodName(String _newMethodName) {
		this._newMethodName = _newMethodName;
	}

	public String getNewMethodName() {
		return _newMethodName;
	}

}
