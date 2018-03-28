#! /bin/sh
# Copyright (c) 2010 Stephan Herrmann.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Stephan Herrmann - initial API and implementation
###############################################################################

# =============================================================================
# MAIN BUILD AND TEST SCRIPT FOR THE OBJECT TEAMS DEVELOPMENT TOOLING (OTDT)
# =============================================================================
# INPUT: Variables from otdt_prerequisites:
# -----------------------------------------------------------------------------
# TMPDIR                for log output
# TEST_TMPDIR           for temp test files
# OT_TESTSUITE_DIR      root directory for building and testing
# METADATA				directory for metadata from previous builds
# SDK_QUALIFIER			build qualifier of the base eclipse SDK
# ECLIPSE_SDK_TGZ       archive file of the base eclipse SDK build (full path)
# ECLIPSE_TESTLIB_ZIP   archive file of the eclipse test framework (full path)
# PUBLISHED_UPDATES 	directory of previously published plugins&features
# FETCH_CACHE_LOCATION	git working area holding caches for fetch
# MAP_FILE_PATH			path to the otdt.map file (original location of otdt.map.in)
# ANT_PROFILE           configure the ant process
# NICE                  niceness value for nice -n ${NICE}
# =============================================================================
# OUTPUT: Variables passed to the toplevel ant script
# -----------------------------------------------------------------------------
## As Environment Variables:
##      ANT_OPTS                configure Ant
## As Ant Arguments (from ANT_PROFILE):
##      -verbose                configure Ant
## As Java Properties:
##      -Declipse-app.tgz       path of eclipse SDK
##      -Declipse.tests.zip     path of eclipse test framework
##		-Dpublished.updates		path to previously published things
##      -Ddo.build.all          true|false: should OTDT and tests be built?
##      -Ddo.run.tests          true|false: should test be run?
##    	-DfetchCacheLocation    git working area holding caches for fetch
##		-Dmap.file.path			path to the otdt.map file (original location of otdt.map.in)
##		-D_hasSaxon.jar			to prevent copying of saxon8.jar (was needed on build.eclipse.org)
# =============================================================================

usage()
{
	echo "Usage:"
	echo "$0 [-b|-nobuild]"
        echo "  -b:         build OTDT only, no testing."
        echo "  -nobuild:   don't build OTDT, directly invoke testing."
}

cleanup()
{
	echo "cleanup(): Currently no cleanup is configured"
}

_prefix=`dirname $0`
_prefix=`readlink -f $_prefix`
. "${_prefix}/otdt_prerequisites-hipp.sh"

echo "=== Sourced otdt_prerequisites-hipp.sh ==="
env
echo "====================================="

#LOCAL: log file:
OT_SUITE_LOG=$TMPDIR/ot-testsuite.log

# LOCAL: the initial ant build file:
BUILDFILE="${_prefix}/run.xml"

#LOCAL: main ant target:
MAIN_TARGET=${MAIN_TARGET:="ot-junit-all"}

#LOCAL: should OTDT and tests be built?
DO_BUILD="true"

#LOCAL: should the tests be run?
DO_RUN="true"

case ${MAIN_TARGET} in
	"ot-junit-run")
		DO_BUILD="false"
		;;
	"ot-compiler-build")
		;&
	"ot-junit-build")
		DO_RUN="false"
		;;
esac

# start working:

test -d "$TMPDIR" || mkdir -p "$TMPDIR"
test -d "$OT_TESTSUITE_DIR" || mkdir -p "$OT_TESTSUITE_DIR"
cd "$OT_TESTSUITE_DIR"

# cleanup previous:
if [ "$DO_BUILD" == "true" ]
then
	rm -rf build-root
	rm -rf test-root
	rm -rf updateSite
    rm -rf updateSiteTests
    rm -rf updateSiteCompiler
	rm -rf metadata
	rm -rf ecj
else
	rm -f test-root/eclipse/results/*
fi

# preload metadata for appending:
if [ -f "${METADATA}/content.xml" ]
then
    mkdir -p metadata
    cp ${METADATA}/*.xml metadata
fi

trap "echo Aborting by SIGTERM; cleanup; exit 130" INT

# Assemble the Ant call:
ANT_OPTIONS="${ANT_PROFILE} \
    -Declipse-app.tgz=${ECLIPSE_SDK_TGZ} \
    -Declipse.tests.zip=${ECLIPSE_TESTLIB_ZIP} \
    -Declipse.sdk.qualifier=${SDK_QUALIFIER} \
    -Dpublished.updates=${PUBLISHED_UPDATES} \
    -Ddo.run.tests=${DO_RUN} \
    -Ddo.build.all=${DO_BUILD} \
    -Dtest.tmpDir=${TEST_TMPDIR} \
    -DfetchCacheLocation=${FETCH_CACHE_LOCATION} \
    -Dmap.file.path=${MAP_FILE_PATH} \
    -D_hasSaxon.jar=true"

ANT_OPTS="-Xmx1024m"
export ANT_OPTS

unset JAVA_TOOL_OPTIONS

CMD="nice -n ${NICE} ant -f ${BUILDFILE} ${ANT_OPTIONS} ${MAIN_TARGET}"

echo "Running $CMD"
eval "$CMD" < /dev/null

trap - INT


