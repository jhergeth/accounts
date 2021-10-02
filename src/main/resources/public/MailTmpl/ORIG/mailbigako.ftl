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
            font-size: 12px;
			margin: 0;
			padding: 0;
        }

		th,
		td {
			padding: 0 1em 0 1em;
		}


        .evenRow {
            background-color: #80d7ff;
        }
        .oddRow{
            background-color: #b3e7ff ;
        }
    </style>
	
	
</head>
<body style="margin: 0; padding: 0;">

<#setting date_format="dd.MM.yyyy">
<#setting datetime_format="dd.MM.yyyy">

<table bgcolor="#b3e7ff" align="center" border="0" cellpadding="0" cellspacing="0" width="800" style="border-collapse: collapse;">
    <tr>
        <td style="padding: 20px 20px 20px 20px;">
            <p>Sehr geehrte<#if geschlecht=="2">r</#if> ${vorname} ${nachname},</p>
            <#if verts?size gt 0>
				<p>in den von Ihnen koordinierten Bildungs&auml;ngen sind folgende Vertretungen f&uuml;r ${zeitraum} den ${ab} geplant.</p>
                <table  border="1" cellpadding="0 0 0 10" cellspacing="0" align="center" width="790">
                    <tr bgcolor="#4dc6ff">
                        <th>Tag</th>
                        <th>Stunde</th>
                        <th>Klasse</th>
                        <th>Raum (war)</th>
                        <th>Fach (war)</th>
                        <th>KuK (war)</th>
                        <th>Bemerkungen</th>
                    </tr>
                    <#list verts as v>
                        <tr class="${v?item_parity}Row" align="center">
                            <td>${v.datumS}</td>
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
                            <#if v.vertLehrer == v.absLehrer>
								<td>${v.vertLehrer}</td>
                            <#else>
								<td>${v.vertLehrer}(${v.absLehrer})</td>
                            </#if>
                            <td align="left">
								<#if v.vertArt == "B">
									Pausenaufsicht: Pause zwischen ${v.stunde-1}. Std und ${v.stunde}. Std
								<#else>
									${v.vertText}
								</#if>
							</td>
                        </tr>
                    </#list>
                </table>
            </#if>
            <#if vertsneu?size gt 0>
                <p>Folgende &Auml;nderungen in Vertretungen haben sich ergeben f&uuml;r ${zeitraum} den ${ab} :</p>
                <table  border="1" cellpadding="0" cellspacing="0" align="center" width="790">
                    <tr bgcolor="#4dc6ff">
                        <th>Tag</th>
                        <th>Stunde</th>
                        <th>Klasse</th>
                        <th>Raum (war)</th>
                        <th>Fach (war)</th>
                        <th>KuK (war)</th>
                        <th>Bemerkungen</th>
                    </tr>
                    <#list vertsneu as v>
                        <tr class="${v?item_parity}Row" align="center">
                            <td>${v.datumS}</td>
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
                            <td>${v.absLehrer}</td>
                            <td align="left">
								<#if v.vertArt == "B">
									Pausenaufsicht: Pause zwischen ${v.stunde-1}. Std und ${v.stunde}. Std
								<#else>
									${v.vertText}
								</#if>
							</td>
                        </tr>
                    </#list>
                </table>
            </#if>
        </td>
    </tr>
    <tr>
        <td style="padding: 0px 20px 20px 20px;">
            <p>Vielen Dank</p>
            <p>Das Vertretungsteam</br>
            GIL, ROB, TRA, GEH</p>
        </td>
    </tr>
</table>

</body>
</html>