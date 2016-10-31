###############################################################################
# Copyright (c) 2010 Stephan Herrmann and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Stephan Herrmann - initial API and implementation
###############################################################################

# Environment variables for the script otdt_runtests.sh

# EXPORT: Root location on build.eclipse.org:
BASEDIR=`pwd`

# EXPORT: root directory for building and testing:
OT_TESTSUITE_DIR=${BASEDIR}/testrun

# EXPORT: tmp directory for logging:
TMPDIR="${BASEDIR}/tmp"

# EXPORT: tmp directory for test files:
#TEST_TMPDIR="${HOME}/tmp"
TEST_TMPDIR=${JAVA_TMPDIR}

# EXPORT: directory for metadata from previous builds:
METADATA="${BASEDIR}/metadata"

# Base dir for finding previous platform build:
DROPS4=/home/data/httpd/download.eclipse.org/eclipse/downloads/drops4

# EXPORT: mail address to receive notification about build result (currently only build failures):
OT_RECIPIENT="nobody@nowhere.net"

# Configure ANT:
ANT_HOME=/shared/common/apache-ant-1.7.1/
PATH=${ANT_HOME}/bin:${PATH}

# EXPORT: additional arguments to pass to Ant:
ANT_PROFILE="-verbose"
#ANT_PROFILE=""

# EXPORT: Nice-level for the Ant process:
NICE="10"

# EXPORT: virtual or real X server (XVFB, XVNC or X11):
if test `which Xvfb` && test `which xvfb-run` &> /dev/null; then
	X11=XVFB
elif test `which Xvnc` &> /dev/null; then
	X11=XVNC
else
	X11=X11
fi

# VERSIONS:
# Eclipse SDK build identifier (used for substitution in otdt.map.in etc.):
SDK_QUALIFIER=I20161027-0700

# Architecture (as used by OSGi):
ARCH=`arch`

# used only locally (components of the ECLIPSE_SDK_TGZ path):
EVERSION=I20161027-0700
DROP=${DROPS4}/I20161027-0700

# EXPORT: archive file of the base eclipse SDK build:
ECLIPSE_SDK_TGZ=${DROP}/eclipse-SDK-${EVERSION}-linux-gtk-${ARCH}.tar.gz

# EXPORT: archive file of the eclipse test framework:
ECLIPSE_TESTLIB_ZIP=${DROP}/eclipse-test-framework-${EVERSION}.zip

# EXPORT: where to find previously published plugins&features:
PUBLISHED_UPDATES=${HOME}/downloads/objectteams/updates/ot2.5

