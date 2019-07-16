package org.mhb.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PathContentReader {

    private static final Logger logger = Logger.getLogger(PathContentReader.class.getSimpleName());

    public String readContentAsString(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        }
        catch(IOException e) {
            logger.log(Level.SEVERE, String.format("Got exception while reading conttent form %s.", path), e);
        }
        return "";
    }


    public List<String> readAllLinesFromPath(Path path) {
        try {
            List<String> collect = Files.readAllLines(path).stream().map(String::trim).collect(Collectors.toList());
            logger.log(Level.FINER, "{0} numbers of line read from {1}", new Object[]{collect.size(), path});

            return collect;
        }
        catch(IOException e) {
            logger.log(Level.SEVERE, String.format("Got exception while reading lines form %s.", path), e);
        }
        return Collections.emptyList();
    }

}
