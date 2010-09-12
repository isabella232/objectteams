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
 * $Id: TeamSmapGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

import java.util.Iterator;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;

/** Generates smap for a TeamType
 * * @author ike
 */
public class TeamSmapGenerator extends AbstractSmapGenerator
{

    /**
     * @param type
     */
    public TeamSmapGenerator(TypeDeclaration type)
    {
        super(type);
    }

    public char[] generate()
    {
        for (Iterator<SmapStratum> iter = this._strata.iterator(); iter.hasNext();)
        {
            SmapStratum stratum = (SmapStratum) iter.next();

            if(stratum.getStratumName().equals(ISMAPConstants.OTJ_STRATUM_NAME))
            {
                return generateOTJSmap(stratum);
            }
        }

        return null;
    }

    //TODO(ike): add JSR-045 Support here
    private char[] generateOTJSmap(SmapStratum stratum)
    {
//    	LineInfoReminder lineInfoReminder = new LineInfoReminder();
//
//        LineNumberProvider provider = _type.getTeamModel().getLineNumberProvider();
//
//        String sourceName = new String();
//        String absoluteSourceName = new String();
//
//        for (Iterator iter = provider.getLineInfos().keySet().iterator(); iter.hasNext();)
//        {
//            ReferenceBinding copySrc = (ReferenceBinding)iter.next();
//            List <LineInfo> lineInfos = provider.getLineInfosForType(copySrc);
//
//            ReferenceBinding outerTypebinding = copySrc.enclosingType();
//            sourceName = String.valueOf(outerTypebinding.sourceName) + ISMAPConstants.OTJ_JAVA_ENDING;
//            absoluteSourceName = getAbsoluteSourcePath(getPackagePathFromRefBinding(outerTypebinding), sourceName);
//
//            FileInfo fileInfo = stratum.getOrCreateFileInfo(sourceName, absoluteSourceName);
//            fileInfo.addLineInfo(lineInfos);
//            lineInfoReminder.storeLineInfos(lineInfos);
//        }
//
//        return getSMAP().toCharArray();
    	return null;
    }

	protected String getPackagePathFromRefBinding(ReferenceBinding binding) {
	    PackageBinding pkgBinding = binding.getPackage();
	    String pkgName = String.valueOf(pkgBinding.readableName());
	    pkgName = pkgName.replace('.',ISMAPConstants.OTJ_PATH_DELIMITER_CHAR);

	    if (pkgName != null &&  pkgName.length() > 0)
	        return pkgName + ISMAPConstants.OTJ_PATH_DELIMITER;

	    return null;
	}

	protected String getAbsoluteSourcePath(String packagePath, String sourceName) {
	    if (packagePath == null)
	        return null;

	    return packagePath + sourceName;
	}
}

