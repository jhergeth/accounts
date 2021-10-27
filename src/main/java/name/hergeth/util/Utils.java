package name.hergeth.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Enumeration;
import java.util.function.Consumer;

public class Utils {
    public static void unZipToFolder(String zipfilename, String outputdir) throws IOException {
        File zipfile = new File(zipfilename);
        if (zipfile.exists()) {
            outputdir = outputdir + File.separator;
            FileUtils.forceMkdir(new File(outputdir));

            ZipFile zf = new ZipFile(zipfile, "UTF-8");
            Enumeration zipArchiveEntrys = zf.getEntries();
            while (zipArchiveEntrys.hasMoreElements()) {
                ZipArchiveEntry zipArchiveEntry = (ZipArchiveEntry) zipArchiveEntrys.nextElement();
                if (zipArchiveEntry.isDirectory()) {
                    FileUtils.forceMkdir(new File(outputdir + zipArchiveEntry.getName() + File.separator));
                } else {
                    FileOutputStream os = FileUtils.openOutputStream(new File(outputdir + zipArchiveEntry.getName()));
                    IOUtils.copy(zf.getInputStream(zipArchiveEntry), os);
                    os.close();
                }
            }
        } else {
            throw new IOException("Error unzipping " + zipfilename);
        }
    }

    public static int readLines(File file, Consumer<String[]> func, Logger LOG) {
        int lines = 0;

        try {
            String line ="";

            String pattern = "";
            BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            while ((line = br.readLine()) != null) {
                if(lines == 0){
                    char[] cs = line.toCharArray();
                    int cc = 0;
                    int sc = 0;
                    for(char c : cs){
                        if(c == ',') cc++;
                        if(c == ';') sc++;
                    }
                    pattern = cc > sc ? "," : ";" + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
                }

                String[] elm = line.split(pattern, -1);
                for(int i = 0; i < elm.length; i++){
                    elm[i] = elm[i].replace("\"", "");
                }
                int absn = 0;

                func.accept(elm);

                lines++;
                if(lines%100 == 0){
                    LOG.info("Read {} lines from {}.", lines, file.getAbsolutePath());
                }
            }
            LOG.info("Read {} lines from {}.", lines, file.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Exception during {} reading: {} after {} lines.",
                    file.getAbsolutePath(), e.getMessage(), lines);
        }

        return lines;
    }

    public static String[] readFirstLine(File file, Logger LOG) {
        try {
            String line ="";

            String pattern = "";
            BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            line = br.readLine();
            char[] cs = line.toCharArray();
            int cc = 0;
            int sc = 0;
            for(char c : cs){
                if(c == ',') cc++;
                if(c == ';') sc++;
            }
            pattern = cc > sc ? "," : ";" + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

            String[] elm = line.split(pattern, -1);
            for(int i = 0; i < elm.length; i++){
                elm[i] = elm[i].replace("\"", "");
            }
            return elm;
        } catch (Exception e) {
            LOG.error("Exception during {} reading: {} in first line.",
                    file.getAbsolutePath(), e.getMessage());
        }
        return null;
    }

    public static int countLines(File file, Logger LOG){
        try{
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));

            int data = lineNumberReader.read();
            while(data != -1){
                data = lineNumberReader.read();
            }
            int lineNumber = lineNumberReader.getLineNumber();
            lineNumberReader.close();
            return lineNumber;
        }
        catch(Exception e){
            LOG.error("Exception {}during counting lines of {}.",
                    e.getMessage(), file.getAbsolutePath());
        }
        return 0;
    }

    public static int inArray(String[] elm, String s){
        for(int i = 0; i < elm.length; i++){
            if(elm[i].contains(s)){
                return i;
            }
        }
        return -1;
    }

    private static final int SALT_LENGTH = 4;

    public static String generateSSHA(byte[] password)
            throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(password);
        crypt.update(salt);
        byte[] hash = crypt.digest();

        byte[] hashPlusSalt = new byte[hash.length + salt.length];
        System.arraycopy(hash, 0, hashPlusSalt, 0, hash.length);
        System.arraycopy(salt, 0, hashPlusSalt, hash.length, salt.length);

        return new StringBuilder().append("{SSHA}")
                .append(Base64.getEncoder().encodeToString(hashPlusSalt))
                .toString();
    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    public static boolean isValidFileName(String fileName){
        final char[] ILLEGAL_CHARACTERS = {' ', '\t', '\n', '\r', '\b', '\f', '\\', '/', '"' };
        if (fileName == null || fileName.length() == 0) {
            return false;
        }
//        if(!CharMatcher.javaLetterOrDigit().matchesAllOf(fileName)){ // non ASCII in filename
//            return false;
//        }
        for (char c : ILLEGAL_CHARACTERS) {
            if (fileName.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * from: http://gordon.koefner.at/blog/coding/replacing-german-umlauts/
     * Replaces all german umlaute in the input string with the usual replacement
     * scheme, also taking into account capitilization.
     * A test String such as
     * "Käse Köln Füße Öl Übel Äü Üß ÄÖÜ Ä Ö Ü ÜBUNG" will yield the result
     * "Kaese Koeln Fuesse Oel Uebel Aeue Uess AEOEUe Ae Oe Ue UEBUNG"
     * @param input
     * @return the input string with replaces umlaute
     */
    public static String replaceUmlaut(String input) {

        //replace all lower Umlauts
        String o_strResult =
                input
                        .replaceAll("ü", "ue")
                        .replaceAll("ö", "oe")
                        .replaceAll("ä", "ae")
                        .replaceAll("ß", "ss");

        //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        o_strResult =
                o_strResult
                        .replaceAll("Ü(?=[a-zäöüß ])", "Ue")
                        .replaceAll("Ö(?=[a-zäöüß ])", "Oe")
                        .replaceAll("Ä(?=[a-zäöüß ])", "Ae");

        //now replace all the other capital umlaute
        o_strResult =
                o_strResult
                        .replaceAll("Ü", "UE")
                        .replaceAll("Ö", "OE")
                        .replaceAll("Ä", "AE");

        return o_strResult;
    }

    /**
     * aus: https://www.it-swarm.com.de/de/java/gibt-es-eine-moeglichkeit-akzente-zu-entfernen-und-eine-ganze-zeichenfolge-regulaere-buchstaben-umzuwandeln/969548740/
     * @param string
     * @return
     */
    public static String flattenToAscii(String string) {
        char[] out = new char[string.length()];
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        int j = 0;
        for (int i = 0, n = string.length(); i < n; ++i) {
            char c = string.charAt(i);
            if (c <= '\u007F') out[j++] = c;
        }
        return new String(out);
    }
}
