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
# OT_RECIPIENT          mail address for failure messages
# SDK_QUALIFIER			build qualifier of the base eclipse SDK
# ECLIPSE_SDK_TGZ       archive file of the base eclipse SDK build (full path)
# ECLIPSE_TESTLIB_ZIP   archive file of the eclipse test framework (full path)
# PUBLISHED_UPDATES 	directory of previously published plugins&features
# ANT_PROFILE           configure the ant process
# X11                   XVFB, XVNC or X11
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
# =============================================================================

usage()
{
	echo "Usage:"
	echo "$0 [-b|-nobuild]"
        echo "  -b:         build OTDT only, no testing."
        echo "  -nobuild:   don't build OTDT, directly invoke testing."
}

notifyTestRunFailure()
{
	echo "Running the test-cases failed!"; 
	local subject="OT Testsuite: Failure!"
	local message="See the attached log to fix the problems."
	local cmdLogfiles="-a ${OT_SUITE_LOG}-tail.gz"
	
	grep -q "\[java\] BUILD FAILED" "$OT_SUITE_LOG" && { subject="OT Testsuite: Compile/Build Failure!"; }
	tail -1000 "$OT_SUITE_LOG" | gzip -f - > "${OT_SUITE_LOG}-tail.gz"
	echo -e "$message" | mutt -s "$subject" $cmdLogfiles $OT_RECIPIENT
	cleanup
	exit 1;
}

cleanup()
{
	if test $X11 = "XVNC"; then
		vncserver -kill $VNC_DISPLAY
	fi
}

_prefix=`dirname $0`
_prefix=`readlink -f $_prefix`
. "${_prefix}/otdt_prerequisites.sh"

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

#LOCAL: Display to be used by VNC:
VNC_DISPLAY=:23


while test $# -gt 0; do
	case "$1" in 
    -b)
        MAIN_TARGET="ot-junit-build"
        DO_RUN="false"
        shift
        ;;
	-nobuild)
		DO_BUILD="false"
		shift
		;;
	-x11)
	    X11=X11
		shift
		;;
	-tmp)
        shift
        TEST_TMPDIR="$1"
        shift
        ;;
	*)
		echo "Unknown argument: $1"
		usage
		exit 1
	esac
	
done

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
    -DfetchCacheLocation=${FETCH_CACHE_LOCATION}
    -Dmap.file.path=${MAP_FILE_PATH}"

ANT_OPTS="-Xmx1024m"
export ANT_OPTS

CMD="nice -n ${NICE} ant -f ${BUILDFILE} ${ANT_OPTIONS} ${MAIN_TARGET}"

if test "$X11" = "XVFB" && test `which xvfb-run` > /dev/null; then
        echo "Running xvfb-run $CMD"

        # make sure that xauth can be found
        export PATH="$PATH:/usr/bin/X11"
        # try to start with DISPLAY=10 instead of default 99 -- seems to not work everywhere
        exec xvfb-run -n 10 -e xvfb.log --auto-servernum $CMD < /dev/null > ${OT_SUITE_LOG} 2>&1 || { notifyTestRunFailure; }
elif test "$X11" = "XVNC"; then
		echo "starting vncserver $VNC_DISPLAY"
		vncserver $VNC_DISPLAY
		DISPLAY=$VNC_DISPLAY
		export DISPLAY
		exec $CMD < /dev/null > ${OT_SUITE_LOG} 2>&1  && { cleanup; } || { notifyTestRunFailure; }
		echo "Cleaning up after successful run..."
		/bin/rm -rf ${OT_TESTSUITE_DIR}/build-root/eclipse
else
        echo "##### You don't have xvfb nor vnc, the GUI tests will appear on your display... ####"
        echo "Running $CMD"
        eval "$CMD" < /dev/null
fi

trap - INT


