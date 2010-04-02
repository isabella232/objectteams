/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Germany and Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IAspectRequestNegotiator.java 23468 2010-02-04 22:34:27Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox;

import org.eclipse.objectteams.otequinox.hook.AspectPermission;

/**
 * Interface for extenders wishing to participate in negotiation about aspect binding requests
 * including forced exports.
 * 
 * @author stephan
 * @since 1.2.6
 */
public interface IAspectRequestNegotiator {
	
	/** 
	 * Check whether a request for forced exports should be granted.
	 * @param aspectBundleSymbolicName the aspect issuing the request
	 * @param baseBundleSymbolicName   the affected base bundle
	 * @param basePackage              the affected base package
	 * @param previousNegotiation      the result of negotations up-to this point
	 * @return a structure holding the answer, must not be null.
	 */
	AspectBindingRequestAnswer checkForcedExport(String aspectBundleSymbolicName, String baseBundleSymbolicName, String basePackage, AspectPermission previousNegotiation);

	/** 
	 * Check whether a request for an aspect binding should be granted.
	 * @param aspectBundleSymbolicName the aspect issuing the request
	 * @param baseBundleSymbolicName   the affected base bundle
	 * @param teamClass 	           an affecting team class involved in this aspect binding
	 * @param previousNegotiation      the result of negotations up-to this point
	 * @return a structure holding the answer, must not be null.
	 */
	AspectBindingRequestAnswer checkAspectBinding(String aspectBundleSymbolicName, String baseBundleSymbolicName, String teamClass, AspectPermission previousNegotiation);
}
