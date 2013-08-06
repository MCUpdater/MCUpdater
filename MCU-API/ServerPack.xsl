<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
  <h1>MCUpdater ServerPack version <xsl:value-of select="ServerPack/@version"/></h1>
  <xsl:for-each select="ServerPack/Server">
  <h2><xsl:value-of select="@name"/> - Minecraft <xsl:value-of select="@version"/> (revision <xsl:value-of select="@revision"/>)</h2>
  <h3>Mods</h3>
    <table border="1">
      <tr bgcolor="#cccccc">
        <th>Name</th>
        <th>Version</th>
        <th>URL</th>
        <th>MD5</th>
      </tr>
      <xsl:for-each select="Module">
      <tr>
        <td><xsl:value-of select="@name"/></td>
        <td><xsl:value-of select="Meta/version"/></td>
        <td><a href="{Meta/URL}"><xsl:value-of select="Meta/URL"/></a></td>
        <td><xsl:value-of select="MD5"/></td>
      </tr>
      </xsl:for-each>      
    </table>
    <hr/>
  </xsl:for-each>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
