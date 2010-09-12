/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AbstractSmapGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;

/**
 * @author ike
 */
public abstract class AbstractSmapGenerator
{

    protected TypeDeclaration _type;
    protected List <SmapStratum>_strata;
	private String _defaultStratum;

    public AbstractSmapGenerator(TypeDeclaration type)
    {
        this._type = type;
        this._strata =  new ArrayList<SmapStratum>();
    }

    public void addStratum(String stratum)
    {
        this._strata.add(new SmapStratum(stratum));
    }

    public abstract char[] generate();

    @SuppressWarnings("nls")
	public String getSMAP()
    {
        String generatedFileName = getClassFileNameForType(this._type);
        StringBuffer out = new StringBuffer();

        // print Header
        out.append("SMAP\n");
        out.append(generatedFileName + "\n");

        // print defaultstratum
        if (this._defaultStratum != null)
        	out.append(this._defaultStratum + "\n");
        else
        	out.append(ISMAPConstants.OTJ_STRATUM_NAME + "\n");

        // print strata
        for (int idx = 0; idx < this._strata.size(); idx++)
        {
            SmapStratum stratum = this._strata.get(idx);
            if (stratum.hasFileInfos())
            {
                stratum.optimize();
                out.append(stratum.getSmapAsString());
            }
        }

        // print EndSection
        out.append("*E");

        return out.toString();
    }

    private String getClassFileNameForType(TypeDeclaration type)
    {
        String generatedFileName = String.valueOf(type.binding.getRealClass().constantPoolName())
        						   + ISMAPConstants.OTJ_CLASS_ENDING;

        String [] tmp = generatedFileName.split("/"); //$NON-NLS-1$

        if (tmp.length > 0)
            return tmp[tmp.length-1];

        return generatedFileName;
    }

    public List<SmapStratum> getStrata()
    {
        return this._strata;
    }

	public void setDefaultStratum(String defaultStratum)
	{
		this._defaultStratum = defaultStratum;
	}
}
