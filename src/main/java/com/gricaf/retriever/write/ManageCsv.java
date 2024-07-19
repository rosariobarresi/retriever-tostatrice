package com.gricaf.retriever.write;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class ManageCsv implements ManageWrite {
    public File addDataToFile(List<String> headers, List<Object> row, String nameSheet, String fileLocation) throws IOException {
        File csvFile = new File(fileLocation + " " + nameSheet + ".csv");
        if (csvFile.exists() && !csvFile.isDirectory()) {
            modifyFile(headers, row, nameSheet, csvFile);
        } else {
            writeFile(headers, row, nameSheet, csvFile);
        }
        return csvFile;
    }

    private void modifyFile(List<String> headers, List<Object> row, String nameSheet, File csvFile) throws IOException {
        FileWriter fileWriter = new FileWriter(csvFile, true);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Object elem : row) {
            sb.append(elem).append(";");
        }
        sb.deleteCharAt(sb.lastIndexOf(";"));
        fileWriter.append(sb.toString());
        fileWriter.close();
    }

    private void writeFile(List<String> headers, List<Object> row, String nameSheet, File csvFile) throws IOException {

        FileWriter fileWriter = new FileWriter(csvFile);
        StringBuilder sb = new StringBuilder();
        for (String header : headers) {
            sb.append(header).append(";");
        }
        sb.deleteCharAt(sb.lastIndexOf(";"));
        sb.append("\n");
        fileWriter.append(sb.toString());

        sb = new StringBuilder();
        for (Object elem : row) {
            sb.append(elem).append(";");
        }
        sb.deleteCharAt(sb.lastIndexOf(";"));
        fileWriter.append(sb.toString());
        fileWriter.close();
    }
}
