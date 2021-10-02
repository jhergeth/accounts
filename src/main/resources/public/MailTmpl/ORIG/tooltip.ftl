<#if vp.statt??>
	${vp.frei.absLehrer} freigesetzt, weil ${vp.frei.absKlassen}/${vp.frei.absFach} abwesend.
	Statt dessen: ${vp.statt.datumS}, ${vp.statt.stunde}te Stunde für ${vp.statt.absLehrer} in ${vp.statt.vertKlassen}.
<#else>
	${vp.frei.absLehrer} freigesetzt, weil ${vp.frei.absKlassen}/${vp.frei.absFach} abwesend.
</#if>
<br/>
