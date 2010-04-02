#!/bin/sh
#
# This script is inteded to be invoked from within eclipse 
# using the launch configuration genIOOTBreakPoints.launch
# in the same directory.
# Prerequisite: 
# You must have the project OTRE within the same workspace.
#
# author: stephan@cs.tu-berlin.de


# Let's identify ourselves:
CWD=`pwd`
PROG=`echo $0 | sed "s|${CWD}/||"`

HEADER="/**********************************************************************
 * This file is part of \"Object Teams Development Tooling\"-Software
 * 
 * Copyright 2006, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * \$Id\$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.core.breakpoints;

/** 
 *  Do not edit: This interface is auto-generated from org/objectteams/Team.java 
 *  using ${PROG}
 *
 *  Purpose: make specific line numbers of Team.java available for the debugger.
 */
public interface IOOTBreakPoints 
{"


# INPUT:
OTRE=$1
if [ ! -d ${OTRE} ]; then
	echo "Project root of OTRE not found at ${OTRE}".
	echo "Perhaps you don't have project OTRE in your workspace?"
	exit
fi
TEAM=${OTRE}/src/org/objectteams/Team.java
if [ ! -f ${TEAM} ]; then
	echo "Team.java not found at ${TEAM}."
	exit
fi

# OUTPUT:
OUT=src/org/objectteams/otdt/debug/core/breakpoints/IOOTBreakPoints.java

if [ "$2" != "RUN" ]
then
    # fetch stdin from Team.java, write to IOOTBreakPoints.java and restart:
    $0 $1 RUN 0<"$TEAM" | tee $OUT
    echo ">>>> Please refresh the source folder. <<<<"
    exit
else

	echo "${HEADER}"
    i=0
    while read 
    do
        i=`expr $i + 1`
        l=`echo "$REPLY" | sed -e "s/^.*[$]Debug(\(.*\))/\1/"`
        if [ "$REPLY" != "$l" ]
        then
            echo "  int LINE_$l = $i;"
        fi
    done
	echo "}"
fi


