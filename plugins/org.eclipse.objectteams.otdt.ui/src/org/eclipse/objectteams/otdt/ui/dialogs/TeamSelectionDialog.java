/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamSelectionDialog.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.dialogs;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.core.search.OTSearchEngine;
import org.eclipse.objectteams.otdt.core.search.OTSearchRequestor;
import org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.eclipse.ui.model.WorkbenchLabelProvider;


/**
 * A dialog to select a team type from a list of teams
 * 
 * This class is a copy of org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog.
 * The constructor and methods open, computeResult and TypeFilterMatcher.match
 * are modified. The method TypeFilterMatcher.getContainerName is new.
 * 
 * The reason for copying TypeSelectionDialog instead of subclassing it
 * is the following:
 * The method TypeSelectionDialog.open which must be extended
 * contains a super-call (= TwoPaneElementSelector.open).
 * If one subclasses TypeSelectionDialog and overrides the method open,
 * it is not possible to call the (super-super-)method TwoPaneElementSelector.open
 * without calling the (super-)method TypeSelectionDialog.open.
 * 
 * @author kaschja
 * @version $Id: TeamSelectionDialog.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class TeamSelectionDialog extends TwoPaneElementSelector
{

	private IJavaSearchScope fScope;
    private List<ISearchFilter> _searchFilters = new LinkedList<ISearchFilter>();
	
	/**
	 * Constructs a team selection dialog.
	 * @param parent  the parent shell.
	 * @param context the runnable context.
	 * @param scope   the java search scope.
	 */
	public TeamSelectionDialog(Shell parent, IRunnableContext context, IJavaSearchScope scope) 
	{
        super(parent, new WorkbenchLabelProvider(), new OTTypeSelectionLabelProvider());

		assert context != null;
		assert scope != null;

		fScope= scope;
		
		setUpperListLabel(OTNewWizardMessages.TeamSelectionDialog_upperLabel); 
		setLowerListLabel(OTNewWizardMessages.TeamSelectionDialog_lowerLabel); 
	}

	/**
	 * Adds a filter that is consulted after the search engine performed its search
	 * and before the result is shown to the user.
	 * A filter may remove or change search-results as it sees fit.
	 * @param filter
	 */
	public void addSearchFilter(ISearchFilter filter)
	{
	    // does not check if filter is already in the list
	    _searchFilters.add(filter);
	}
	
	public void removeSearchFilter(ISearchFilter filter)
	{
	    _searchFilters.remove(filter);
	}
	
	/*
	 * @see AbstractElementListSelectionDialog#createFilteredList(Composite)
	 */
	protected FilteredList createFilteredList(Composite parent) 
	{
		FilteredList list = super.createFilteredList(parent);
 		
		fFilteredList.setFilterMatcher(new TypeFilterMatcher());
		fFilteredList.setComparator(new StringComparator());
		
		return list;
	}
	
	/*
	 * @see org.eclipse.jface.window.Window#open()
	 */	 
	public int open() 
	{
		OTSearchRequestor requestor = new OTSearchRequestor();
	    
	    try
	    {
		    SearchPattern teamPattern = OTSearchEngine.createTeamTypePattern(IJavaSearchConstants.CLASS, SearchPattern.R_EXACT_MATCH);
		    new OTSearchEngine().search(teamPattern, fScope, requestor, null);
	    }
	    catch (CoreException ex)
	    {
	        OTDTUIPlugin.getExceptionHandler().logCoreException("Problems searching team types", ex); //$NON-NLS-1$
	    }
	    catch (InternalCompilerError ex) // be defensive against InternalCompilerErrors :-/
	    {
	        OTDTUIPlugin.getExceptionHandler().logException("Problems searching team types", ex); //$NON-NLS-1$
	    }
	    
		IOTType[] teamsInScope = requestor.getOTTypes();
		teamsInScope = filterTypes(teamsInScope);

		if ( (teamsInScope == null) || (teamsInScope.length == 0) )
		{ 
			String title   = OTNewWizardMessages.TeamSelectionDialog_notypes_title; 
			String message = OTNewWizardMessages.TeamSelectionDialog_notypes_message;
			MessageDialog.openInformation(getShell(), title, message);
			return CANCEL;                
		}
		else
		{
			setElements(teamsInScope);
			return super.open();
		}
	}
	
	protected IOTType[] filterTypes(IOTType[] teamsInScope)
    {
	    IOTType[] filteredTypes = teamsInScope;
	    
	    for (Iterator<ISearchFilter> iter = _searchFilters.iterator(); iter.hasNext();)
        {
            ISearchFilter filter = iter.next();
            filteredTypes = filter.filterTypes(filteredTypes);
        }
	    
	    return filteredTypes;
    }

    /*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() 
	{
		Object tmpRef = getLowerSelectedElement();
		
		if (tmpRef instanceof IOTType)
		{
		    IOTType ref = (IOTType) tmpRef;
			IType type = (IType) ref.getCorrespondingJavaElement();

			List<IType> result = new ArrayList<IType>(1);
			result.add(type);
			setResult(result);
		}
	}


//------------------------------------------------------------------------------

    private static class TypeFilterMatcher
        implements FilteredList.FilterMatcher
    {

        private static final char END_SYMBOL = '<';
        private static final char ANY_STRING = '*';

		private StringMatcher fMatcher;
        private StringMatcher fQualifierMatcher;

        /*
         * @see FilteredList.FilterMatcher#setFilter(String, boolean)
         */
        public void setFilter(String pattern,
                              boolean ignoreCase,
                              boolean igoreWildCards)
        {
            int qualifierIndex = pattern.lastIndexOf("."); //$NON-NLS-1$

            // type			
            if (qualifierIndex == -1)
            {
                fQualifierMatcher = null;
                fMatcher =
                    new StringMatcher(
                        adjustPattern(pattern),
                        ignoreCase,
                        igoreWildCards);

            }
            //qualified type
            else
            {
                fQualifierMatcher =
                    new StringMatcher(
                        pattern.substring(0, qualifierIndex),
                        ignoreCase,
                        igoreWildCards);
                fMatcher =
                    new StringMatcher(
                        adjustPattern(pattern.substring(qualifierIndex + 1)),
                        ignoreCase,
                        igoreWildCards);
            }
        }

        /*
         * @see FilteredList.FilterMatcher#match(Object)
         */       
        public boolean match(Object element)
        {
            if (!(element instanceof IOTType))
            {
                return false;
            }

            IOTType type = (IOTType) element;

            if (!fMatcher.match(type.getElementName()))
            {
                return false;
            }

            if (fQualifierMatcher == null)
            {
                return true;
            }

            return fQualifierMatcher.match(getContainerName(type));
        }

        public String getContainerName(IOTType type)
        {
            String qualName = type.getFullyQualifiedName();
            int index = qualName.lastIndexOf('.');
            if (index == -1)
            	return ""; //$NON-NLS-1$
            
            String containerName =
                qualName.substring(0, index);

            return containerName;
        }
      

        private String adjustPattern(String pattern)
        {
            int length = pattern.length();
            if (length > 0)
            {
                switch (pattern.charAt(length - 1))
                {
                    case END_SYMBOL :
                        pattern = pattern.substring(0, length - 1);
                        break;
                    case ANY_STRING :
                        break;
                    default :
                        pattern = pattern + ANY_STRING;
                }
            }
            return pattern;
        }
    }
    
//-----------------------------------------------------------------------------
    
    /*
     * A string comparator which is aware of obfuscated code
     * (type names starting with lower case characters).
     */
    private static class StringComparator implements Comparator
    {
        public int compare(Object left, Object right)
        {
            String leftString = (String) left;
            String rightString = (String) right;

            if (Strings.isLowerCase(leftString.charAt(0))
                && !Strings.isLowerCase(rightString.charAt(0)))
            {
                return +1;
            }

            if (Strings.isLowerCase(rightString.charAt(0))
                && !Strings.isLowerCase(leftString.charAt(0)))
            {
                return -1;
            }

            int result = leftString.compareToIgnoreCase(rightString);

            if (result == 0)
            {
                result = leftString.compareTo(rightString);
            }

            return result;
        }
    }
}

