package org.mhb.parser;


import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileNameCollector {

    private static final Logger logger = Logger.getLogger(FileNameCollector.class.getSimpleName());

    private Configuration configuration = new Configuration();


    public List<Path> getAllJsonSchemaFile() throws IOException {
        logger.log(Level.FINE, "Search file in {0}.", configuration.getSearchDirectory());

        Stream<Path> paths = Files.find(Paths.get(configuration.getSearchDirectory()), Integer.MAX_VALUE, (path, basicFileAttributes) -> !matchExludePath(path) && matchIncludePath(path), new FileVisitOption[0]);
        List<Path> collect = paths.collect(Collectors.toList());

        logger.log(Level.INFO, "{0} number of files found in the {1}.", new Object[]{collect.size(), configuration.getSearchDirectory()});
        return collect;
    }


    private boolean matchIncludePath(Path path) {
        boolean shouldInclude = true;

        for (String pattern : configuration.getIncludePattern()) {
            shouldInclude &= path.toString().matches(pattern);

            if (!shouldInclude) {
                break;
            }
        }

        logger.log(Level.FINER, "File={0}, IncludePattern={1}, matched={2}.", new Object[]{path, Arrays.deepToString(configuration.getIncludePattern()), shouldInclude});
        return shouldInclude;
    }

    private boolean matchExludePath(Path path) {
        return path.toString().matches(configuration.getExcludePattern());
    }
}
