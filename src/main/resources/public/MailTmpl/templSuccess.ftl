<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Mail weitergeleitet an ${anzrec} Empf&auml;nger</title>

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
            <p>Betreff: ${subject}.</p>
            <p>Mail weitergeleitet an folgende ${anzrec} Empf&auml;nger.</p>
			<table  class="tabelle">
				<tr bgcolor="#eaeaea">
					<th width="40%">email</th>
					<th width="30%">Name</th>
					<th width="30%">Vorname</th>
				</tr>
				<#list receiver as r>
					<tr class="${r?item_parity}Row" align="center">
						<td width="40%">${r.mailadresse}</td>
						<td width="30%">${r.nachname}</td>
						<td width="30%">${r.vorname}</td>
					</tr>
				</#list>
			</table>
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