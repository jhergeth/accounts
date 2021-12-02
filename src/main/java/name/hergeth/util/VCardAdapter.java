package name.hergeth.util;

import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Telephone;
import ezvcard.property.VCardProperty;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class VCardAdapter {
    VCard vc = null;

    public VCardAdapter(VCard vc){ this.vc = vc;}

    public String getVorName(){ return vc.getStructuredName().getGiven();}
    public String getNachName(){ return vc.getStructuredName().getFamily();}
    public String getAnrede(){ return vc.getStructuredName().getPrefixes().get(0);}
    public String getDisplayName(){ return vc.getFormattedName().toString();}

    // Implementierung ignoriert den Adresstyp (HOME, OFFICE, ...) und bearbeitet nur die erste Adresse der Liste
    public String getStrasse(){ return vc.getAddresses().get(0).getStreetAddress();}
    public String getOrt(){ return vc.getAddresses().get(0).getLocality();}
    public String getPLZ(){ return vc.getAddresses().get(0).getPostalCode();}

    public String getHomeStrasse(){ return getAdress("HOME", Address::getStreetAddress);}
    public String getHomeOrt(){ return getAdress("HOME", Address::getLocality);}
    public String getHomePLZ(){ return getAdress("HOME", Address::getPostalCode);}

    public Date getGeburtstag(){ return vc.getBirthday().getDate();}

    public String getHomeEMail(){ return getEmail("HOME");}
    public String getWorkEMail(){ return getEmail("WORK");}

    public String getCellPhone(){ return getTel("CELL");}
    public String getHomePhone(){ return getTel("HOME");}

    public static String getWorkEMail(VCard vc){ VCardAdapter a = new VCardAdapter(vc); return a.getWorkEMail();}

    private String getAdress(String type, Function<Address,String> f){
        Address a = getPropWithType(vc.getAddresses(), type);
        return a != null ? f.apply(a) : "";
    }

    private String getEmail(String type){ Email e = getPropWithType(vc.getEmails(), type);
        return e != null ? e.getValue() : "";
    }

    private String getTel(String type){ Telephone t = getPropWithType(vc.getTelephoneNumbers(), type);
        return t != null ? t.getText() : "";
    }

    private <T extends VCardProperty> T getPropWithType(List<T> vpl, String type){
        return vpl.stream().filter(r -> {
            return r.getParameters("TYPE").stream().filter(p -> p.equalsIgnoreCase(type)).findFirst().isPresent();
        }).findFirst().get();
    }
}
