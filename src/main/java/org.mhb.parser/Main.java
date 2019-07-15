package org.mhb.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.groupingBy;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getSimpleName());

    private static final Set<String> updateStatements = new TreeSet<>();
    private static final List<String> missingColumns = new ArrayList<>();
    private static final StringBuilder duplicatEntry = new StringBuilder();
    private static int duplicateEntryCounter = 0;

    public static void main(String... strings) throws IOException {

        List<Path> paths = getAllJsonFileNames();

        createUpdateStatementForeachField(getAllFieldMetadata(paths));

        logger.log(Level.INFO, "*** {0} update statements have been created ***", updateStatements.size());

        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        updateStatements.stream().forEach(stmt -> sb.append(stmt).append(System.lineSeparator()));
        logger.info("\n" + sb);

        logger.log(Level.WARNING, "*** {0} column(s) properties are missing in collected JSON Schema ***", missingColumns.size());
        logger.log(Level.WARNING, "*** Missing columns are [{0}] ***", missingColumns);
        logger.log(Level.WARNING, "*** {0} duplicate entry found while parsing field metadata ***", duplicateEntryCounter);
        logger.log(Level.WARNING, "Duplicated entries are \n{0}", duplicatEntry);
    }

    private static void createUpdateStatementForeachField(Map<String, List<RecordDetail>> fieldMetadata) {

        fieldMetadata.forEach((k, v) -> {
            createUpdateStatement(k, v);
        });
    }

    private static void createUpdateStatement(String fieldName, List<RecordDetail> metadatas) {

        RecordDetail dataTypeProp = verifyAndGetDataTypeProperties(fieldName, metadatas);

        String minimumPart = null;
        if(dataTypeProp.getMinimumValue() != null) {
            minimumPart = String.format("MINIMUM = '%d'", dataTypeProp.getMinimumValue());
        }

        String maximumPart = null;
        if(dataTypeProp.getMaximumValue() != null) {
            maximumPart = String.format("MAXIMUM = '%d'", dataTypeProp.getMaximumValue());
        }

        String wherePart = String.format("WHERE GPTYPFLD_FELDNAME = '%s';", fieldName);

        if(minimumPart != null || maximumPart != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE DBZILI02.TBZI0072GPTYPFLD SET");
            sb.append(minimumPart == null ? String.format(" %s %s", maximumPart, wherePart) : String.format(" %s, %s %s", minimumPart, maximumPart, wherePart));
            updateStatements.add(sb.toString());
            logger.log(Level.FINEST, "{0}, UpdateStatement={1}", new Object[]{dataTypeProp, sb});
        }
    }

    private static RecordDetail verifyAndGetDataTypeProperties(String fieldName, List<RecordDetail> metadatas) {

        Map<Long, List<RecordDetail>> grouped = metadatas.stream()
                .filter(m -> Objects.nonNull(m.getMaximumValue()) || Objects.nonNull(m.getMinimumValue()))
                .collect(groupingBy(RecordDetail::getMaximumValue));

        RecordDetail recordDetail;
        if(grouped.size() > 1) {
            duplicateEntryCounter++;
            duplicatEntry.append(String.format("*** %s ***", fieldName)).append(System.lineSeparator());
            grouped.forEach((k, v) -> duplicatEntry.append(v.get(0)).append(System.lineSeparator()));
            recordDetail = grouped.values().stream().flatMap(List::stream).max(RecordDetail::compareTo).get();
        }
        else {
            recordDetail = grouped.values().stream().flatMap(List::stream).findFirst().get();
        }

        return recordDetail;
    }

    private static Map<String, List<RecordDetail>> getAllFieldMetadata(List<Path> paths) {
        Map<String, List<RecordDetail>> metadatas = paths.stream()
                .map(Main::readFieldPropertiesFromJson)
                .flatMap(List::stream)
                .peek(Main::verifyResult)
                .collect(groupingBy(RecordDetail::getName));

        logger.log(Level.INFO, "{0} fields information collected from from Schema {0} files.", new Object[]{metadatas.size(), paths.size()});
        return metadatas;
    }

    private static List<RecordDetail> readFieldPropertiesFromJson(Path path) {
        ReadPathContent content = new ReadPathContent();
        ParseJsonString parser = new ParseJsonString();
        return parser.parseJsonString(content.readPathContentAsString(path));
    }

    private static List<Path> getAllJsonFileNames() throws IOException {
        FileNameCollector collector = new FileNameCollector();
        return collector.getAllJsonSchemaFile();
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
