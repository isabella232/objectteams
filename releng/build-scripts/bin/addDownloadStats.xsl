<!--
Copyright (c) 2010 Mia-Software
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors:
Gregoire Dupe - initial implementation
Stephan Herrmann - adaptation for Object Teams
-->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
        version="1.0">
        <xsl:output encoding="UTF-8" method="xml" indent="yes" />
        <xsl:strip-space elements="*" />
        <xsl:param name="repo" />
        <xsl:param name="version" />

        <xsl:template match="/">
                <xsl:processing-instruction name="artifactRepository">version='1.1.0'</xsl:processing-instruction>

                <xsl:apply-templates />
        </xsl:template>

        <xsl:template match="repository/properties">
                <properties size='{@size+1}'>
                        <xsl:copy-of select="property" />
                        <property name='p2.statsURI'>
                                <xsl:attribute name="value"><xsl:value-of select="$repo" /></xsl:attribute>
                        </property>
                </properties>
        </xsl:template>

        <xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.objectteams.runtime']/properties">
                <xsl:call-template name="artifact_properties"/>
        </xsl:template>

        <xsl:template match="artifact[@classifier='org.eclipse.update.feature' and @id='org.eclipse.objectteams.otdt']/properties">
                <xsl:call-template name="artifact_properties"/>
        </xsl:template>

        <xsl:template match="artifact[@classifier='org.eclipse.update.feature' and @id='org.eclipse.objectteams.otequinox']/properties">
                <xsl:call-template name="artifact_properties"/>
        </xsl:template>

        <xsl:template match="artifact[@classifier='org.eclipse.update.feature' and @id='org.eclipse.objectteams.otequinox.otre']/properties">
                <xsl:call-template name="artifact_properties"/>
        </xsl:template>

        <xsl:template match="artifact[@classifier='org.eclipse.update.feature' and @id='org.eclipse.objectteams.otequinox.turbo']/properties">
                <xsl:call-template name="artifact_properties"/>
        </xsl:template>

        <xsl:template name="artifact_properties">
                <properties size='{@size+1}'>
                        <xsl:copy-of select="property" />
                        <property name='download.stats'>
                                <xsl:attribute name="value">
                                        <xsl:copy-of select='string(../@id)'/>.<xsl:value-of select='$version' />
                                </xsl:attribute>
                        </property>
                </properties>
        </xsl:template>

        <xsl:template match="*">
                <xsl:copy>
                        <xsl:for-each select="@*">
                                <xsl:copy-of select="." />
                        </xsl:for-each>
                        <xsl:apply-templates />
                </xsl:copy>
        </xsl:template>

</xsl:stylesheet>
