<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Fehlerhafte Weiterleitungsaufgabe</title>

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
            <p>FEHLER!</p>
			<p>Dieser Betreff: '${realsubject}' definiert keine Empf&auml;ngerliste.</p>
            <p>Eine Empf&auml;ngerliste wird definiert mit einem Betreff, der beginnt mit entweder 'ALL' oder der Name der Klasse.</p>
            <p>Falls als erstes Zeichen des Betreffs ein '!' steht, wird eine Mails an alle geplanten Empf&auml;nger verschickt, sonst wird nur eine Mail an den Absender und eine Best&auml;tigungsmail erzeugt.</p>
            <p>Hinter der Bezeichung der Empf&auml;nger muss ein':' stehen, danach folgt der eigentliche Betreff.</p>
			
        </td>
    </tr>
    <tr>
        <td bgcolor="#ffffff" style="padding: 0px 20px 20px 20px;">
            <p>Einen sch&ouml;nen Tag,</p>
            <p>der Mailer</p>
        </td>
    </tr>
</table>

</body>
</html>