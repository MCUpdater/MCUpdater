<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.mcupdater.com" exclude-result-prefixes="x">

    <xsl:template match="/">
        <html>
            <body>
                <h1>MCUpdater ServerPack version <xsl:value-of select="x:ServerPack/@version"/></h1>
                <xsl:for-each select="x:ServerPack/x:Server">
                    <h2><xsl:value-of select="@name"/> &lt;<xsl:value-of select="@id"/>&gt;- Minecraft <xsl:value-of select="@version"/> (revision <xsl:value-of select="@revision"/>)</h2>
                    <xsl:if test="count(x:Import)>0">
                    <h3>Imports</h3>
                    <table border="1">
                        <tr bgcolor="#cccccc">
                            <th>Id</th>
                            <th>URL</th>
                        </tr>
                        <xsl:for-each select="x:Import">
                            <tr>
                                <td><xsl:value-of select="."/></td>
                                <td><a href="{@url}"><xsl:value-of select="@url"/></a></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                    </xsl:if>
                    <h3>Mods</h3>
                    <table border="1">
                        <tr bgcolor="#cccccc">
                            <th>Name</th>
                            <th>URL</th>
                            <th>MD5</th>
                        </tr>
                        <xsl:for-each select="x:Module">
                            <tr>
                                <td><xsl:value-of select="@name"/></td>
                                <td><a href="{x:URL}"><xsl:value-of select="x:URL"/></a></td>
                                <td><xsl:value-of select="x:MD5"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                    <hr/>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
