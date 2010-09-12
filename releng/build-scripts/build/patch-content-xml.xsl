<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2004/07/xpath-functions" xmlns:xdt="http://www.w3.org/2004/07/xpath-datatypes" exclude-result-prefixes="xs fn xdt">
   <xsl:param name="version"/>
   <xsl:param name="versionnext" />
   <xsl:template match="@range[../@name='org.eclipse.jdt.feature.group']">
    <xsl:choose>
     <!-- cannot use "concat('[',$version)" inside match predicate, so choose now: -->
     <xsl:when test="starts-with(.,concat('[',$version))">
        <xsl:attribute name="range"><xsl:value-of select="concat('[',$version,',',$versionnext,')')" /></xsl:attribute>
     </xsl:when>
     <xsl:otherwise>
        <xsl:attribute name="range"><xsl:value-of select="."/></xsl:attribute>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:template>
   <!-- Whenever you match any node or any attribute -->
   <xsl:template match="node()|@*">
     <!-- Copy the current node -->
     <xsl:copy>
       <!-- Including any attributes it has and any child nodes -->
       <xsl:apply-templates select="@*|node()"/>
     </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
