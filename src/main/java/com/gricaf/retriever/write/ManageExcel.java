package com.gricaf.retriever.write;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ManageExcel implements ManageWrite {

    static final Logger LOGGER = Logger.getLogger(ManageExcel.class.getName());

    private void writeFile(List<String> headerValues, List<Object> values, String nameSheet, String fileLocation) {
        LOGGER.info("writeFile");
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
//            createSheet(headerValues, values, nameSheet, workbook);
            FileOutputStream outputStream = new FileOutputStream(fileLocation);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

    private static void createSheet(List<String> headerValues, List<Object> values, String nameSheet, Workbook workbook) throws IOException {
        LOGGER.info("createSheet");
        Sheet sheet = workbook.createSheet(nameSheet);
        Row header = sheet.createRow(0);


        LOGGER.info("creo header");
        Cell headerCell = null;
        try {
            headerCell = header.createCell(0);

            headerCell.setCellValue("Data/ora");
            LOGGER.info("aggiunto titolo 0");
            int i = 1;
            for (String headerValue : headerValues) {
                headerCell = header.createCell(i);
                headerCell.setCellValue(headerValue);
                LOGGER.info("aggiunto titolo " + headerValue);
                i++;
            }

            headerCell = header.createCell(i);
            headerCell.setCellValue("prod giornaliera");
        } catch (Throwable e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }

        LOGGER.info("header creato");
        Row row = sheet.createRow(1);
        writeCells(values, row, workbook, sheet);

    }

    private void modifyFile(List<String> headerValues, List<Object> values, String nameSheet, String fileLocation) throws IOException {
        LOGGER.info("modifyFile");
        FileInputStream inputStream = new FileInputStream(fileLocation);
        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet sheet = workbook.getSheet(nameSheet);
        if (sheet == null) {
            createSheet(headerValues, values, nameSheet, workbook);
        } else {
            updateSheet(headerValues, values, sheet, workbook);
        }
        inputStream.close();
        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    private static void updateSheet(List<String> headerValues, List<Object> values, Sheet sheet, Workbook workbook) {
        LOGGER.info("updateSheet");
        Row rowHeader = sheet.createRow(0);
        Cell headerCell = rowHeader.createCell(0);
        int i = 1;
        headerCell.setCellValue("Data/ora");
        for (String headerValue : headerValues) {
            headerCell = rowHeader.createCell(i);
            headerCell.setCellValue(headerValue);
            i++;
        }
        headerCell = rowHeader.createCell(i);
        headerCell.setCellValue("prod giornaliera");
        int rowCount = sheet.getLastRowNum();
        Row row = sheet.createRow(++rowCount);
        writeCells(values, row, workbook, sheet);
    }

    private static void writeCells(List<Object> values, Row row, Workbook workbook, Sheet sheet) {
        try {
            LOGGER.info("inizio scrittura dati");
            CellStyle cellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
            Cell cell = row.createCell(0);
            cell.setCellValue(new Date());
            cell.setCellStyle(cellStyle);
            LOGGER.info("scrivo dato 0");
            int columnCount = 1;
            for (Object elem : values) {
                cell = row.createCell(columnCount);
                LOGGER.info("scrivo row " + row.getRowNum() + " cell " + columnCount + " value " + elem);
                if (elem == null) {
                    cell.setCellValue("null");
                } else if (elem instanceof Integer) {
                    CellStyle cellStyleInt = workbook.createCellStyle();
                    cellStyleInt.setDataFormat(workbook.createDataFormat().getFormat("#"));
                    cell.setCellValue((Integer) elem);
                    cell.setCellStyle(cellStyleInt);
                } else if (elem instanceof Double) {
                    CellStyle cellStyleDouble = workbook.createCellStyle();
                    cellStyleDouble.setDataFormat(workbook.createDataFormat().getFormat("#"));
                    cell.setCellValue((Double) elem);
                    cell.setCellStyle(cellStyleDouble);
                } else if (elem instanceof String) {
                    cell.setCellValue((String) elem);
                }
                LOGGER.info("scrivo dato " + columnCount);
                columnCount++;
            }
            if (row.getRowNum() >= 2) {
                int j = 1;
                while (sheet.getLastRowNum() - j >= 1) {
                    Row row1 = sheet.getRow(sheet.getLastRowNum() - j);
                    if (row1.getCell(2) != null) {
                        double prodTot1 = row1.getCell(2).getNumericCellValue();
                        double prodTot2 = sheet.getRow(sheet.getLastRowNum()).getCell(2).getNumericCellValue();
                        cell = row.createCell(columnCount);
                        CellStyle cellStyleDouble = workbook.createCellStyle();
                        cellStyleDouble.setDataFormat(workbook.createDataFormat().getFormat("#"));
                        cell.setCellValue((prodTot2 - prodTot1));
                        cell.setCellStyle(cellStyleDouble);
                        break;
                    }
                    j++;
                }
            }
            LOGGER.info("fine scrittura dati");
        } catch (Throwable e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ManageExcel manageExcel = new ManageExcel();
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "modify";
        List<String> headers = Arrays.asList("h1", "h2", "h3");
        List<Object> r1 = Arrays.asList(1, 2.5);
        manageExcel.addDataToFile(headers, r1, "ip", fileLocation);
    }

    public File addDataToFile(List<String> headers, List<Object> row, String nameSheet, String fileLocation) throws IOException {
        File f = new File(fileLocation + ".xlsx");
        if (!f.exists()) {
            writeFile(headers, row, nameSheet, f.getAbsolutePath());
        }
        modifyFile(headers, row, nameSheet, f.getAbsolutePath());
        return f;
    }
}
