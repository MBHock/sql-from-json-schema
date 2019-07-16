package org.mhb.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getSimpleName());

    private static final StringBuilder duplicatEntry = new StringBuilder();
    private static final PathContentReader content = new PathContentReader();
    private static int duplicateEntryCounter = 0;

    public static void main(String... strings) throws IOException {

        List<Path> paths = findJsonFiles();

        Map<String, List<RecordDetail>> collectRecordDetail = parseFileContentToCollectRecordDetail(paths);

        Set<String> insertStatements = createInsertStatementForeachRecord(collectRecordDetail);

        logger.log(Level.INFO, "*** {0} statements have been created ***", insertStatements.size());

        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        insertStatements.stream().forEach(stmt -> sb.append(stmt).append(System.lineSeparator()));
        logger.info("\n" + sb);

        logger.log(Level.WARNING, "*** {0} duplicate entry found while parsing field metadata ***", duplicateEntryCounter);
        logger.log(Level.WARNING, "Duplicated entries are \n{0}", duplicatEntry);
    }

    private static Set<String> createInsertStatementForeachRecord(Map<String, List<RecordDetail>> fieldMetadata) {
        return fieldMetadata.values().parallelStream().map(Main::createInsertStatement).collect(Collectors.toCollection(TreeSet::new));
    }

    private static String createInsertStatement(List<RecordDetail> metadatas) {
        RecordDetail recordDetail = verifyAndGetRecordDetail(metadatas);

        StringJoiner insertPart = new StringJoiner(",", "(", ")");
        insertPart.add("FIELD_NAME");

        StringJoiner valuePart = new StringJoiner(",", "(", ")");
        valuePart.add(formatToSqlValue(recordDetail.getName()));

        if(recordDetail.getMinimumValue() != null) {
            insertPart.add("MINIMUM");
            valuePart.add(formatToSqlValue(recordDetail.getMinimumValue()));
        }

        if(recordDetail.getMaximumValue() != null) {
            insertPart.add("MAXIMUM");
            valuePart.add(formatToSqlValue(recordDetail.getMaximumValue()));
        }

        if(recordDetail.getType() != null) {
            insertPart.add("TYPE");
            valuePart.add(formatToSqlValue(recordDetail.getType()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO FIELD_TYPE_VALIDATION ");
        sb.append(insertPart);
        sb.append(" VALUES ");
        sb.append(valuePart);
        sb.append(";");

        logger.log(Level.FINEST, "{0}, InsertStatement={1}", new Object[]{recordDetail, sb});
        return sb.toString();
    }

    private static String formatToSqlValue(Object object) {
        if(object instanceof Number) {
            return String.format("'%d'", object);
        }
        return String.format("'%s'", object);

    }

    private static RecordDetail verifyAndGetRecordDetail(List<RecordDetail> metadatas) {

        Map<Long, List<RecordDetail>> grouped = metadatas.stream()
                .filter(m -> Objects.nonNull(m.getMaximumValue()) || Objects.nonNull(m.getMinimumValue()))
                .collect(groupingBy(RecordDetail::getMaximumValue));

        RecordDetail recordDetail;
        if(grouped.size() > 1) {
            recordDetail = grouped.values().stream().flatMap(List::stream).max(RecordDetail::compareTo).get();
            duplicateEntryCounter++;
            duplicatEntry.append(String.format("*** %s ***", recordDetail.getName())).append(System.lineSeparator());
            grouped.forEach((k, v) -> duplicatEntry.append(v.get(0)).append(System.lineSeparator()));
        }
        else {
            recordDetail = grouped.values().stream().flatMap(List::stream).findFirst().get();
        }

        return recordDetail;
    }

    private static Map<String, List<RecordDetail>> parseFileContentToCollectRecordDetail(List<Path> paths) {
        Map<String, List<RecordDetail>> recordDetails = paths.stream()
                .map(Main::parseToGetRecordDetail)
                .flatMap(List::stream)
                .peek(Main::verifyResult)
                .collect(groupingBy(RecordDetail::getName));

        logger.log(Level.INFO, "{0} fields information collected from from Schema {0} files.", new Object[]{recordDetails.size(), paths.size()});
        return recordDetails;
    }

    private static List<RecordDetail> parseToGetRecordDetail(Path path) {
        JsonStringParser parser = new JsonStringParser();
        return parser.parseJsonString(content.readContentAsString(path));
    }

    private static List<Path> findJsonFiles() throws IOException {
        PathCollector collector = new PathCollector();
        return collector.findPathsFromDirectory();
    }

    private static RecordDetail verifyResult(RecordDetail recordDetail) {
        Properties properties = PropertyResolver.INSTANCE.getTestProps();
        String testObjectives = properties.getProperty(recordDetail.getName());

        if(Objects.nonNull(testObjectives)) {
            RecordDetail testObject = new RecordDetail();
            testObject.setName(recordDetail.getName());

            for(String testcase : testObjectives.split(",")) {
                String[] values = testcase.split("=");
                String keyName = values[0];
                String keyValue = values[1];

                if("type".equalsIgnoreCase(keyName)) {
                    testObject.setType(keyValue);
                }
                else if("minimum".equalsIgnoreCase(keyName) || "minLength".equalsIgnoreCase(keyName)) {
                    testObject.setMinimumValue(Objects.isNull(keyValue) || "null".equals(keyValue) ? null : Long.valueOf(keyValue));
                }
                else if("maximum".equalsIgnoreCase(keyName) || "maxLength".equalsIgnoreCase(keyName)) {
                    testObject.setMaximumValue(Objects.isNull(keyValue) || "null".equals(keyValue) ? null : Long.valueOf(keyValue));
                }
            }

            assert Objects.equals(recordDetail, testObject) : String.format("Object must be same saw=%s, required=%s", recordDetail, testObject);
        }

        return recordDetail;
    }

}
