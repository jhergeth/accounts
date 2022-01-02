package name.hergeth.eplan.util;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Func {
    private static final Logger LOG = LoggerFactory.getLogger(Func.class);
    private static final NumberFormat numberFormatDefault = NumberFormat.getInstance(Locale.getDefault());
    private static final NumberFormat numberFormatEN = NumberFormat.getInstance(Locale.ENGLISH);
    private static final NumberFormat numberFormatDE = NumberFormat.getInstance(Locale.GERMANY);
    private static DecimalFormatSymbols localSymbols = DecimalFormatSymbols.getInstance();
    private static Pattern pattern = Pattern.compile("[\\d\\s.,]*");


    public static Set<String> getSet(String str, String split){
        return addToSet(new HashSet<>(), str, split);
    }

    public static Set<String> addToSet(Set<String> s, String str, String split){
        s.addAll(Arrays.asList(str.split(split, -1)).stream().map(String::trim).sorted().collect(Collectors.toList()));
        return s;
    }

    public static String setToString(Set<String> set){
        return set.stream().sorted().collect(Collectors.joining(","));
    }

    public static String strNormalize(String s, String split){
        Set<String> set = new HashSet<>();
        return setToString(addToSet(set, s, split));
    }

    public static double parseDouble(String s){
        double res = 0.0d;
        if(s != null && s.length() > 0 && isNumeric(s)){
            Number number = 0.0d;

            NumberFormat nf = numberFormatDefault;
            if(s.contains(".")){
                nf = numberFormatEN;
            }
            else if(s.contains(",")){
                nf = numberFormatDE;
            }
            try{
                number = nf.parse(s);
            }
            catch(Exception e){
                LOG.debug("Cannot parse number: {}", s);
            }
            res = number.doubleValue();
        }
        return res;
    }

    public static boolean isNumeric(String value) {
        if (value == null)
            return false;

        Matcher m = pattern.matcher(value);

        return m.matches();
    }

    public static void readZipStream(InputStream in, BiConsumer<String, String> fUser) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry;
        while ((entry = zipIn.getNextEntry()) != null) {
            String zName = entry.getName();
            File file = File.createTempFile(zName, "tmp");
            readContents(new FilterInputStream(zipIn) {
                @Override
                public void close() throws IOException {
                    zipIn.closeEntry();
                }
            }, file);
            zName = zName.substring(zName.indexOf("/")+1);
            fUser.accept(zName, file.getAbsolutePath());
        }
    }

    private static void readContents(InputStream contentsIn, File destFile) throws IOException {
        byte contents[] = new byte[4096];
        try(FileOutputStream outputStream = new FileOutputStream(destFile);
        ){
            int data = contentsIn.read(contents, 0, contents.length);
            while(data != -1){
                outputStream.write(contents, 0, data);
                data = contentsIn.read(contents, 0, contents.length);
            }
        }
    }


    public static String guessEncoding(InputStream input) throws IOException {
        // Load input data
        long count = 0;
        int n = 0, EOF = -1;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ((EOF != (n = input.read(buffer))) && (count <= Integer.MAX_VALUE)) {
            output.write(buffer, 0, n);
            count += n;
        }

        if (count > Integer.MAX_VALUE) {
            throw new RuntimeException("Inputstream too large.");
        }

        byte[] data = output.toByteArray();

        // * ICU4j
        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(data);
        charsetDetector.enableInputFilter(true);
        CharsetMatch cm = charsetDetector.detect();
        if (cm != null) {
            return cm.getName();
        }
        return null;
    }

    public static Double[] getZeroDouble(int anz){
        Double[] res = new Double[anz];
        Arrays.setAll(res, i -> 0d);
        return res;
    }
}
