<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes" />
    <xsl:template match="test">
        <xsl:copy>
             <xsl:text>xslt test</xsl:text>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>