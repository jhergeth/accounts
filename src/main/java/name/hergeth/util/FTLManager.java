package name.hergeth.util;

import freemarker.template.*;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.apache.commons.collections4.IterableUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

public class FTLManager extends HashMap<String, Object> {
    private static FTLManager singleton = null;
    private static Configuration ftlConfig = null;
    private static DefaultObjectWrapper bw = null;

    public static FTLManager getInstance(String tDir){
        if(singleton==null){
            try {
                singleton = new FTLManager(tDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return singleton;
    }


    private FTLManager(String tempDir) throws IOException{
        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.27) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        ftlConfig = new Configuration(Configuration.VERSION_2_3_31);
        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        ftlConfig.setDirectoryForTemplateLoading(new File(tempDir));

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        ftlConfig.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        ftlConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        ftlConfig.setLogTemplateExceptions(false);

        // Wrap unchecked exceptions thrown during template processing into TemplateException-s.
        ftlConfig.setWrapUncheckedExceptions(true);

        ftlConfig.setObjectWrapper(new Java8ObjectWrapper(ftlConfig.getIncompatibleImprovements()));

        ftlConfig.setTemplateUpdateDelayMilliseconds(500);      // DEBUG!!!!!!!!!!!!!!!!
        // Create the root hash. We use a Map here, but it could be a JavaBean too.

        DefaultObjectWrapperBuilder bwb = new DefaultObjectWrapperBuilder(ftlConfig.getIncompatibleImprovements());
        bw = bwb.build();
    }

    public <T> long stuffList(String key, Iterable<T> vList){
        put(key, vList);
        return IterableUtils.size(vList);
    }

 /*   public <T> long stuffList(String key, Iterable<T> vList){
        int anz = 0;
        SimpleSequence ss = new SimpleSequence(bw);

        for(Iterator<T> it = vList.iterator(); it.hasNext();){
            ss.add(it.next());
            anz++;
        }
        put(key, ss);
        return anz;
    }*/

    public String process(String template) throws IOException, TemplateException {
        Template temp = ftlConfig.getTemplate(template);
        Writer mailSubj = new StringWriter();
        temp.process(this, mailSubj);
        return mailSubj.toString();
    }
}
