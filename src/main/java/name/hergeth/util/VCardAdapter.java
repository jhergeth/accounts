package name.hergeth.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import ezvcard.VCard;
import ezvcard.property.*;
import name.hergeth.accounts.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class VCardAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(VCardAdapter.class);
    private static PhoneNumberUtil pu = PhoneNumberUtil.getInstance();

    private VCard vc = null;
    private String href = "";
    private Phonenumber.PhoneNumber homePhone = null;
    private Phonenumber.PhoneNumber cellPhone = null;
    private String sCellPhone = "";
    private String sHomePhone = "";

    public VCardAdapter(VCard vc, String href){
        this.vc = vc;
        this.href = href;

        updateType(vc.getAddresses(), "HOME", "Privat");
        updateType(vc.getEmails(), "HOME", "Privat");
        updateType(vc.getEmails(), "WORK", "Arbeit");
        updateType(vc.getTelephoneNumbers(), "CELL", "Mobil");
        updateType(vc.getTelephoneNumbers(), "HOME", "Privat");

        if(getOrigHomePhone().length() > 4){
            try {
                homePhone = pu.parse(getOrigHomePhone(), "DE");
                sHomePhone = homePhone==null?"":pu.format(homePhone, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } catch (NumberParseException e) {
                LOG.info("Could not parse home phone number of {}: {}", getDisplayName(), getOrigHomePhone());
                homePhone = null;
            }
        }
        if(getOrigCellPhone().length() > 4){
            try {
                cellPhone = pu.parse(getOrigCellPhone(), "DE");
                sCellPhone = cellPhone==null?"":pu.format(cellPhone, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } catch (NumberParseException e) {
                LOG.info("Could not parse cell phone number of {}: {}", getDisplayName(), getOrigCellPhone());
                cellPhone = null;
            }
        }
    }

    public VCardAdapter(Account a, String path){
        VCard vc = new VCard();
        this.vc = vc;

        String uuid = UUID.randomUUID().toString();
        this.href = path + uuid + ".vcf";
        vc.setUid(new Uid("urn:uuid:" + uuid));

        StructuredName sn = new StructuredName();
        sn.setFamily(a.getNachname());
        sn.setGiven(a.getVorname());
        sn.getPrefixes().add(a.getAnrede());
        vc.setStructuredName(sn);

        vc.setFormattedName(a.getAnzeigeName());

        Address adr = new Address();
        adr.setParameter("TYPE", "Privat");
        adr.setStreetAddress(a.getHomeStrasse());
        adr.setLocality(a.getHomeOrt());
        adr.setPostalCode(a.getHomePLZ());
        vc.getAddresses().add(adr);

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        try {
            Date bDay = formatter.parse(a.getGeburtstag());
            vc.setBirthday(new Birthday(bDay));
        } catch (ParseException e) {
            LOG.warn("Could not parse birthday <{}>", a.getGeburtstag());
        }

        Email em = new Email(a.getEmail());
        em.setParameter("TYPE", "Arbeit");
        vc.getEmails().add(em);
        em = new Email(a.getHomeEMail());
        em.setParameter("TYPE", "Privat");
        vc.getEmails().add(em);

        Telephone tel = new Telephone(a.getCellPhone());
        tel.setParameter("TYPE", "Mobil");
        vc.getTelephoneNumbers().add(tel);
        tel = new Telephone(a.getHomePhone());
        tel.setParameter("TYPE", "Privat");
        vc.getTelephoneNumbers().add(tel);
    }

    public String getHRef(){ return href;}
    public VCard getVCard(){ return vc; }

    public String getVorName(){ return vc.getStructuredName().getGiven();}
    public String getNachName(){ return vc.getStructuredName().getFamily();}
    public String getAnrede(){ return vc.getStructuredName().getPrefixes().get(0);}
    public String getDisplayName(){ return vc.getFormattedName().getValue();}

    // Implementierung ignoriert den Adresstyp (HOME, OFFICE, ...) und bearbeitet nur die erste Adresse der Liste
    public String getStrasse(){ return vc.getAddresses().get(0).getStreetAddress();}
    public String getOrt(){ return vc.getAddresses().get(0).getLocality();}
    public String getPLZ(){ return vc.getAddresses().get(0).getPostalCode();}

    public String getHomeStrasse(){ return getAddress("Privat", Address::getStreetAddress);}
    public String getHomeOrt(){ return getAddress("Privat", Address::getLocality);}
    public String getHomePLZ(){ return getAddress("Privat", Address::getPostalCode);}

    public Date getGeburtstag(){ Birthday b = vc.getBirthday(); return b==null?null:b.getDate();}

    public String getHomeEMail(){ return getEmail("Privat");}
    public String getWorkEMail(){ return getEmail("Arbeit");}

    public String getCellPhone(){ return sCellPhone;}
    public String getHomePhone(){ return sHomePhone;}

    private String getOrigCellPhone(){ return getTel("Mobil");}
    private String getOrigHomePhone(){ return getTel("Privat");}

    public VCard updateFromAccount(Account a){
        vc.getStructuredName().setFamily(a.getNachname());
        vc.getStructuredName().setGiven(a.getVorname());
        vc.getStructuredName().getPrefixes().set(0, a.getAnrede());
        vc.getFormattedName().setValue(a.getAnzeigeName());

        setAddress("Privat", Address::setStreetAddress, a.getHomeStrasse());
        setAddress("Privat", Address::setLocality, a.getHomeOrt());
        setAddress("Privat", Address::setPostalCode, a.getHomePLZ());
        updateType(vc.getAddresses(), "HOME", "Privat");

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        try {
            Date bDay = formatter.parse(a.getGeburtstag());
            List<RawProperty> rem =  vc.removeExtendedProperty("BDAY");
            LOG.warn("Removed {} extended properties!", rem.size());
            vc.setBirthday(new Birthday(bDay));
        } catch (ParseException e) {
            LOG.warn("Could not parse birthday <{}>", a.getGeburtstag());
        }

        setEmail("Privat", a.getHomeEMail());
        updateType(vc.getEmails(), "HOME", "Privat");
        setEmail("Arbeit", a.getEmail());
        updateType(vc.getEmails(), "WORK", "Arbeit");

        setTel("Mobil", a.getCellPhone());
        updateType(vc.getTelephoneNumbers(), "CELL", "Mobil");
        setTel("Privat", a.getHomePhone());
        updateType(vc.getTelephoneNumbers(), "HOME", "Privat");

        return vc;
    }


    public void prettyPrint(){
        LOG.info("pretty Print VCardAdapter:");
        LOG.info("...Anrede: {}", getAnrede());
        LOG.info("...Vorname: {}", getVorName());
        LOG.info("...Nachname: {}", getNachName());
        LOG.info("...Anzeigename: {}", getDisplayName());
        LOG.info("...Strasse: {}", getHomeStrasse());
        LOG.info("...Ort: {} {}", getHomePLZ(), getHomeOrt());
        LOG.info("...Geburtstag: {}", getGeburtstag());
        LOG.info("...Home-Mail: {}", getHomeEMail());
        LOG.info("...Work-Mail: {}", getWorkEMail());
        LOG.info("...Home-Phone: {}", getHomePhone());
        LOG.info("...Cell-Phone: {}", getCellPhone());
    }

//    public static String getWorkEMail(VCard vc){ VCardAdapter a = new VCardAdapter(vc); return a.getWorkEMail();}

    private String getAddress(String type, Function<Address,String> f){
        Address a = getPropWithType(vc.getAddresses(), type);
        return a != null ? f.apply(a) : "";
    }
    private void setAddress(String type, BiConsumer<Address,String> f, String nv){
        Address a = getPropWithType(vc.getAddresses(), type);
        if(a != null){
            f.accept(a, nv);
        }
    }

    private String getEmail(String type){ Email e = getPropWithType(vc.getEmails(), type);
        return e != null ? e.getValue() : "";
    }
    private void setEmail(String type, String nv){
        Email e = getPropWithType(vc.getEmails(), type);
        if(e != null){
            e.setValue(nv);
        }
    }

    private String getTel(String type){ Telephone t = getPropWithType(vc.getTelephoneNumbers(), type);
        return t != null ? t.getText() : "";
    }
    private void setTel(String type, String nv){
        Telephone t = getPropWithType(vc.getTelephoneNumbers(), type);
        if(t != null){
            t.setText(nv);
        }
    }

    private <T extends VCardProperty> void updateType(List<T> p, String oldType, String newType){
        T a = getPropWithType( p, oldType);
        if(a != null ){
            a.setParameter("TYPE", newType);
        }
    }

    private <T extends VCardProperty> T getPropWithType(List<T> vpl, String type){
        Optional<T> op = vpl.stream().filter(r -> {
            return r.getParameters("TYPE").stream().filter(p -> p.equalsIgnoreCase(type)).findFirst().isPresent();
        }).findFirst();
        if(op.isPresent())return op.get();
        return null;
    }
}
