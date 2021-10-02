<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Vertretungen für {zeitraum}</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>

    <!-- use the font -->
    <style>
        .color-primary-0 { color: #B8CBED }	/* Main Primary color */
        .color-primary-1 { color: #E5EDFB }
        .color-primary-2 { color: #CFDDF5 }
        .color-primary-3 { color: #A0B8E2 }
        .color-primary-4 { color: #88A4D4 }

        body {
            font-family: 'Roboto', sans-serif;
            font-size: 48px;
        }

        .evenRow {
            background-color: #E5EDFB;
        }
        .oddRow{
            background-color: white ;
        }
    </style>
</head>
<body style="margin: 0; padding: 0;">

<table align="center" border="0" cellpadding="0" cellspacing="0" width="800" style="border-collapse: collapse;">
    <tr>
        <td bgcolor="#ffffff" style="padding: 40px 30px 40px 30px;">
            <p>Werte ${vorname} ${nachname}</p>
            <p>wir möchten Sie über die für Sie geplanten Vertretungen un d Präsenzen informieren.</p>
            <#if verts?size gt 0>
                <p>Folgende Vertretungen sind für ${zeitraum} für Sie geplant:</p>
                <table  border="1" cellpadding="0" cellspacing="0" align="center" width="790">
                    <tr bgcolor="#eaeaea">
                        <th>Tag</th>
                        <th>Stunde</th>
                        <th>Klasse</th>
                        <th>Raum (war)</th>
                        <th>Fach (war)</th>
                        <th>Bemerkungen</th>
                    </tr>
                    <#list verts as v>
                        <tr class="${v?item_parity}Row" align="center">
                            <td>${v.datum}</td>
                            <td>${v.stunde}</td>
                            <td>${v.vertKlassen}</td>
                            <#if v.vertRaum == v.absRaum>
                            <td>${v.vertRaum}</td>
                            <#else>
                            <td>${v.vertRaum}(${v.absRaum})</td>
                            </#if>
                            <#if v.vertFach == v.absFach>
                            <td>${v.vertFach}</td>
                            <#else>
                            <td>${v.vertFach}(${v.absFach})</td>
                            </#if>
                            <td align="left">${v.vertText}</td>
                        </tr>
                    </#list>
                </table>
            </#if>
            <#if praes?size gt 0>
                <p>Folgende Präsenzen sind für ${zeitraum} für Sie geplant:</p>
                <table  border="1" cellpadding="0" cellspacing="0" align="center" width="790">
                    <tr bgcolor="#eaeaea">
                        <th>Tag</th>
                        <th>Stunde</th>
                        <th>Bemerkungen</th>
                    </tr>
                    <#list praes as v>
                        <tr class="${v?item_parity}Row" align="center">
                            <td>${v.datum}</td>
                            <td>${v.stunde}</td>
                            <td align="left">${v.vertText}</td>
                        </tr>
                    </#list>
                </table>
            <#else>
                <p>Für ${zeitraum} sind keine Präsenzen für Sie geplant.</p>
            </#if>
            <#if zeitraum == "kommende Woche" && vertanz gt 0>
                <p>Für kommende Woche können bis zu ${vertanz} Vertretungen für Sie geplant werden.</p>
            </#if>
            <p>Vielen Dank</p>
        </td>
    </tr>
    <tr>
        <td bgcolor="#ffffff" style="padding: 30px 30px 30px 30px;">
            <p>Das Vertretungsteam</p>
            <p>GIL, KOE, TRA, GEH</p>
        </td>
    </tr>
</table>

</body>
</html>