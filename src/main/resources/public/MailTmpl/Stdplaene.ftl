<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Stundenpl&auml;ne</title>

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
/*			width: 790px;	*/
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

<table align="left" border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
    <tr>
        <td bgcolor="#ffffff" style="padding: 0px;">
            <p>Sehr geehrte<#if geschlecht=="2">r Herr<#else> Frau</#if> ${nachname},</p>
            <p>anbei die neu geplanten Stundenpl&auml;ne vorab zur Kontrolle.</p>
			<p>&Auml;nderungsvorschl&auml;ge schicken Sie bitte an das Stundenplanteam: stundenplan@berufskolleg-geilenkirchen.de oder an Ihre Bereichsleitung.
        </td>
    </tr>
    <tr>
        <td bgcolor="#ffffff" style="padding: 0px;">
            <p>Vielen Dank</p>
            <p>Das Stundenplanteam</br>
            SCN, BUE, HEG, GEH</p>
        </td>
    </tr>
</table>

</body>
</html>