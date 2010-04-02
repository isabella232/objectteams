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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.reorg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IConfirmQuery;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgQueries;

/**
 * @author brcan
 */
public class MockReorgQueries implements IReorgQueries
{
    private final List fQueriesRun= new ArrayList();

    public IConfirmQuery createYesNoQuery(String queryTitle, boolean allowCancel, int queryID) {
        run(queryID);
        return yesQuery;
    }

    public IConfirmQuery createYesYesToAllNoNoToAllQuery(String queryTitle, boolean allowCancel, int queryID) {
        run(queryID);
        return yesQuery;
    }

    private void run(int queryID) {
        fQueriesRun.add(new Integer(queryID));
    }

    //List<Integer>
    public List getRunQueryIDs() {
        return fQueriesRun;
    }

    private final IConfirmQuery yesQuery= new IConfirmQuery() {
        public boolean confirm(String question) throws OperationCanceledException {
            return true;
        }

        public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
            return true;
        }
    };

    public IConfirmQuery createSkipQuery(String queryTitle, int queryID) {
        run(queryID);
        return yesQuery;
    }
}
