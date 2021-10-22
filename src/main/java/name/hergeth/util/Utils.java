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
}
