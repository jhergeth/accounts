package name.hergeth.accounts.domain;

import io.micronaut.context.annotation.Bean;
import name.hergeth.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Bean
public class ScannerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ScannerBuilder.class);

    int[] match = null;

    private class Columns {
        String name;
        String[] head = null;
        Consumer<String[]> func = null;
        Predicate<int[]> test = null;
        public Columns(String name, String[] head, Predicate<int[]> test, Consumer<String[]> func){
            this.name = name;
            this.head = head;
            this.func = func;
            this.test = test;
        }
    };
    List<Columns> colDefs = null;

    public ScannerBuilder(){
        colDefs = new LinkedList<>();
    }

    public void addColDef(String name, String[] head, Predicate<int[]> test, Consumer<String[]> func){
        colDefs.add(new Columns(name, head, test, func));
    }
    public Consumer<String[]> buildScanner(File file) {
        String[] head = null;
        final AtomicBoolean skipNext = new AtomicBoolean(false);

        String[] elms = Utils.readFirstLine(file, LOG);
        LOG.info("First line of file is: {}", (Object[]) elms);
        LOG.info("Umlaute sind: ae oe ue ss -> ä ö ü ß");

        for (Columns col : colDefs) {
            match = new int[col.head.length];
            buildMatcher(col.head, elms, match);
            if (col.test.test(match)) {
                LOG.info("Using scanner {} for input.", col.name);
                return (String[] line) -> {
                    String[] sort = new String[line.length];
                    for(int i = 0; i < line.length; i++){
                        if(i >= match.length || match[i] < 0){
                            sort[i] = "";
                        }
                        else{
                            sort[i] = line[match[i]];
                        }
                    }
                    col.func.accept(sort);
                };
            }
        }
        LOG.info("No matching scanner found!");
        return null;
    }

    private void buildMatcher(String[] head, String[] elms, int[] match) {
        for (int i = 0; i < match.length; i++) {
            match[i] = Utils.inArray(elms, head[i]);
        }
    }
}
