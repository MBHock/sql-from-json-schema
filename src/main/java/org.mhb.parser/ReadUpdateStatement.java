package org.mhb.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class ReadUpdateStatement {
    static String filePath = "src/main/resources/updateStatements.sql";

    public static void main(String... strings) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(filePath));

        Set<String> statements = new TreeSet<>();

        lines.forEach(statements::add);

        statements.stream().forEach(System.out::println);

        for (String statement : statements) {
            int index = statement.indexOf("WHERE GPTYPFLD_FELDNAME");
            String condition = statement.substring(index, statement.length() - 1);
            System.out.println(condition);
        }
    }
}
