package org.mhb.parser;


import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class PathCollector {

    private static final Logger logger = Logger.getLogger(PathCollector.class.getSimpleName());

    private Configuration configuration = new Configuration();


    public List<Path> findPathsFromDirectory() throws IOException {
        logger.log(Level.FINE, "Search file in {0}.", configuration.getSearchDirectory());

        Stream<Path> paths = Files.find(Paths.get(configuration.getSearchDirectory()),
                Integer.MAX_VALUE,
                (path, basicFileAttributes) -> !matchExludePath(path) && matchIncludePath(path),
                new FileVisitOption[0]);
        List<Path> collect = paths.collect(collectingAndThen(toList(), Collections::unmodifiableList));

        logger.log(Level.INFO, "{0} number of files found in the {1}.", new Object[]{collect.size(), configuration.getSearchDirectory()});
        return collect;
    }


    private boolean matchIncludePath(Path path) {
        boolean shouldInclude = true;

        for(String pattern : configuration.getIncludePattern()) {
            shouldInclude = shouldInclude && path.toString().matches(pattern);
            if(!shouldInclude) {
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
