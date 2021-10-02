<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Vertretungen f&uuml;r ${zeitraum}</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    
    <!-- use the font 	-->
    <style>
        .color-primary-0 { color: #B8CBED }	/* Main Primary color */
        .color-primary-1 { color: #E5EDFB }
        .color-primary-2 { color: #CFDDF5 }
        .color-primary-3 { color: #A0B8E2 }
        .color-primary-4 { color: #88A4D4 }

        body {
            font-size: 1em;
			margin: 0;
			padding: 0;
			font-family: Arial, sans-serif;
		}

		th,
		td {
			border-collapse: collapse;
			padding: 0 0.5em 0 0.5em;
			position:relative;
		}

		.tabelle {
			border-width: 1px;
			border-style: solid;
			border-collapse: collapse;
			cellpadding: 0;
			cellspacing: 0;
			width: 790px;
			position:relative;
		}
		.tabelle th {
			border-width: 1px;
			border-style: solid;
			border-collapse: collapse;
			padding: 0 0.2em 0 0.2em;
			text-align: center;
            font-size: 0.9em;
			position:relative;
		}
		.tabelle td {
			border-width: 1px;
			border-style: solid;
			border-collapse: collapse;
			padding: 0 0.2em 0 0.2em;
            font-size: 0.9em;
			position:relative;
		}
        .evenRow {
            background-color: white ;
        }
        .oddRow{
            background-color: #E5EDFB;
        }
		.lowright{
			position: absolute;
			bottom: 0;
			right: 0;
			text-align: right;
			font-size:xx-small;
		}
    </style>
	<!--[if mso]>
		<style type="text/css">
			body, table, td {font-family: Arial, sans-serif !important;}
		</style>
	<![endif]-->
	
	
</head>
<body style="margin: 0; padding: 0;">

<#setting date_format="dd.MM.yyyy">
<#setting datetime_format="dd.MM.yyyy">

<table align="center" border="0" cellpadding="0" cellspacing="0" width="800" style="border-collapse: collapse;">
    <tr>
        <td bgcolor="#ffffff" style="padding: 20px 20px 20px 20px;">
            <p>Sehr geehrte<#if geschlecht=="2">r Herr<#else> Frau</#if> ${nachname},</p>
            <p>wir m&ouml;chten Sie &uuml;ber die f&uuml;r Sie geplanten Freisetzungen, Vertretungen und Pr&auml;senzen informieren.</p>
            <#if zeitraum == "kommende Woche" && vertanz gt 0>
                <p>In der kommenden Woche sind sie <#if vertanz==1>eine Stunde<#else>${vertanz} Stunden</#if> freigesetzt und k&ouml;nnen für Vertretungen eingesetzt werden.</p>
            </#if>
            <#if praes?size gt 0>
                <p>In folgenden Stunden haben Sie Pr&auml;senzpflicht:</p>
                <table  class="tabelle">
                    <tr bgcolor="#eaeaea">
                        <th width="15%">Tag</th>
                        <th width="15%">Stunde</th>
                        <th>Bemerkungen</th>
                    </tr>
                    <#list praes as v>
                        <tr class="${v?item_parity}Row" align="center">
                            <td width="15%">${v.datumS}</td>
                            <td width="15%">${v.stunde}</td>
                            <td align="left">
                                <#if v.vertText?length gt 0>
                                    ${v.vertText}
                                <#else>
                                    <#if v.absGrund?contains("KPra") || v.absGrund?contains("VKab") || v.absGrund?contains("KKa") || v.absGrund?contains("VKf")>
                                        Klasse ${v.absKlassen} / Fach ${v.absFach} fehlt.
                                    </#if>
                                </#if>
                                </td>
                        </tr>
                    </#list>
                </table>
            </#if>
            <#if verts?size gt 0>
                <p>Folgende Freisetzungen oder Vertretungen sind f&uuml;r ${zeitraum} f&uuml;r Sie verplant:</p>
                <table  class="tabelle">
                    <tr bgcolor="#eaeaea">
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
                            <#if v.vertKlassen == "">
                                <td>${v.absKlassen}</td>
                            <#else>
                                <td>${v.vertKlassen}</td>
                            </#if>
                            <#if v.vertRaum == v.absRaum>
                                <td>${v.vertRaum}</td>
                            <#elseif v.vertRaum == "">
                                <td>??? (${v.absRaum})</td>
                            <#elseif v.absRaum == "">
                                <td>${v.vertRaum}</td>
							<#else>
                                <td>${v.vertRaum} (${v.absRaum})</td>
                            </#if>
                            <#if v.vertFach == v.absFach>
                                <td>${v.vertFach}</td>
                            <#elseif v.vertFach == "">
                                <td>${v.absFach}</td>
                            <#elseif v.absFach == "">
                                <td>${v.vertFach}</td>
							<#else>
                                <td>${v.vertFach} (${v.absFach})</td>
                            </#if>
                            <#if v.vertLehrer == v.absLehrer>
								<td>${v.vertLehrer}</td>
                            <#else>
								<td>${v.vertLehrer} (${v.absLehrer})</td>
                            </#if>
                            <td align="left">
								<#if v.vertArt == "B">
									Pausenaufsicht: Pause zwischen ${v.stunde-1}. Std und ${v.stunde}. Std
								<#else>
									<#if v.vertArt == "C">
										Entfall
									<#elseif v.vertArt == "T" || v.vertArt == "F">
										Verlegung
									<#else>
										<#if v.vertRaum == "">
											Neuer Raum wird noch zugewiesen.
										</#if>
									</#if>
									<#if v.absLehrer?length gt 0 && !v.absLehrer?contains(v.vertLehrer)>
										(${v.absLehrer} abwesend)
									</#if> ${v.vertText}
								</#if>
							</td>
                        </tr>
                    </#list>
                </table>
            </#if>
            <#if vertsneu?size gt 0>
                <p>Folgende &Auml;nderungen haben sich ergeben:</p>
                <table  class="tabelle">
                    <tr bgcolor="#eaeaea">
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
                            <#if v.vertKlassen == "">
                                <td>${v.absKlassen}</td>
                            <#else>
                                <td>${v.vertKlassen}</td>
                            </#if>
                            <#if v.vertRaum == v.absRaum>
                                <td>${v.vertRaum}</td>
                            <#elseif v.vertRaum == "">
                                <td>??? (${v.absRaum})</td>
                            <#elseif v.absRaum == "">
                                <td>${v.vertRaum}</td>
							<#else>
                                <td>${v.vertRaum} (${v.absRaum})</td>
                            </#if>
                            <#if v.vertFach == v.absFach>
                                <td>${v.vertFach}</td>
                            <#elseif v.vertFach == "">
                                <td>${v.absFach}</td>
                            <#elseif v.absFach == "">
                                <td>${v.vertFach}</td>
							<#else>
                                <td>${v.vertFach} (${v.absFach})</td>
                            </#if>
                            <#if v.vertLehrer == v.absLehrer>
								<td>${v.vertLehrer}</td>
                            <#else>
								<td>${v.vertLehrer} (${v.absLehrer})</td>
                            </#if>
                            <td align="left">
								<#if v.vertArt == "B">
									Pausenaufsicht: Pause zwischen ${v.stunde-1}. Std und ${v.stunde}. Std
								<#else>
									<#if v.vertArt == "C">
										Entfall
									<#elseif v.vertArt == "T" || v.vertArt == "F">
										Verlegung
									<#else>
										<#if v.vertRaum == "">
											Neuer Raum wird noch zugewiesen.
										</#if>
									</#if>
									<#if v.absLehrer?length gt 0 && !v.absLehrer?contains(v.vertLehrer)>
										(${v.absLehrer} abwesend)
									</#if> ${v.vertText}
								</#if>
							</td>
                        </tr>
                    </#list>
                </table>
            </#if>
        </td>
    </tr>
    <tr>
        <td bgcolor="#ffffff" style="padding: 0px 20px 20px 20px;">
            <p>Vielen Dank</p>
            <p>Das Vertretungsteam</br>
            GIL, ROB, TRA, GEH</p>
        </td>
    </tr>
	<tr>
        <td bgcolor="#ffffff" style="padding: 0px 20px 20px 20px;">
            <p>p.s.:</p>
			<p>Sollten die Angaben in dieser Mail nicht stimmen, beantworten Sie diese Mail einfach mit einer kurzen Erl&auml;uterung.</p>
			<p>
				In den Tabellen steht in den Spalten Raum, Fach und KuK jeweils der Vertretungseintrag und in Klammern dahinter der urspr&uuml;ngliche Eintrag,
				falls sich die beiden Eintr&auml;ge unterscheiden. Falls noch kein neuer Raum zugewiesen wurde, wird dies durch '???' angezeigt.
			</p>
        </td>
    </tr>
</table>

</body>
</html>