package library;

import org.apache.poi.util.IOUtils;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xddf.usermodel.*;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ExcelExporter — generates styled .xlsx reports using Apache POI.
 * Each report has:
 *   Sheet 1: logo placeholder, header, data table, signature box
 *   Sheet 2: bar/pie chart of the data
 */
public class ExcelExporter {

    // ── Amber theme colors ───────────────────────────────────────────
    private static final XSSFColor AMBER_800 = new XSSFColor(new byte[]{(byte)0x92,(byte)0x40,(byte)0x0E}, null);
    private static final XSSFColor AMBER_100 = new XSSFColor(new byte[]{(byte)0xFE,(byte)0xF3,(byte)0xC7}, null);
    private static final XSSFColor AMBER_50  = new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFB,(byte)0xEB}, null);
    private static final XSSFColor WHITE     = new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF}, null);
    private static final XSSFColor DARK      = new XSSFColor(new byte[]{(byte)0x1F,(byte)0x1F,(byte)0x1F}, null);
    private static final XSSFColor GREEN_BG  = new XSSFColor(new byte[]{(byte)0xDC,(byte)0xFC,(byte)0xE7}, null);
    private static final XSSFColor RED_BG    = new XSSFColor(new byte[]{(byte)0xFC,(byte)0xEB,(byte)0xEB}, null);
    private static final XSSFColor GRAY_BG   = new XSSFColor(new byte[]{(byte)0xF3,(byte)0xF4,(byte)0xF6}, null);

    // ══════════════════════════════════════════════════════════════════
    // REPORT 1 — Borrow Transactions
    // ══════════════════════════════════════════════════════════════════
    public static void exportBorrowReport(List<Object[]> data, String filePath, String generatedBy) throws IOException {
        String[] headers = {"ID","Member Name","Book Title","Borrow Date","Due Date","Return Date","Status","Processed By"};
        String title    = "Book Borrow Transactions Report";
        String subtitle = "Library Management System";

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet1 = wb.createSheet("Borrow Report");
            XSSFSheet sheet2 = wb.createSheet("Chart");

            buildReportSheet(wb, sheet1, title, subtitle, headers, data, generatedBy, 6);

            // Chart data — monthly summary (Status column = col 6)
            String[] chartLabels = {"Borrowed","Returned","Overdue"};
            long[] chartValues = {
                data.stream().filter(r -> "Borrowed".equals(r[6])).count(),
                data.stream().filter(r -> "Returned".equals(r[6])).count(),
                data.stream().filter(r -> "Overdue".equals(r[6])).count()
            };
            buildBarChart(wb, sheet2, "Borrow Status Summary", chartLabels, chartValues);

            writeFile(wb, filePath);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // REPORT 2 — Return Transactions
    // ══════════════════════════════════════════════════════════════════
    public static void exportReturnReport(List<Object[]> data, String filePath, String generatedBy) throws IOException {
        String[] headers = {"Return ID","Borrow ID","Member Name","Book Title","Return Date","Condition","Processed By"};
        String title    = "Book Return Transactions Report";
        String subtitle = "Library Management System";

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet1 = wb.createSheet("Return Report");
            XSSFSheet sheet2 = wb.createSheet("Chart");

            buildReportSheet(wb, sheet1, title, subtitle, headers, data, generatedBy, -1);

            // Chart: count by condition
            String[] labels = {"Good condition","Slight damage","Damaged","No note"};
            long[] vals = new long[4];
            for (Object[] r : data) {
                String cond = r[5] == null ? "" : r[5].toString().toLowerCase();
                if (cond.contains("good"))    vals[0]++;
                else if (cond.contains("slight")) vals[1]++;
                else if (cond.contains("damage")) vals[2]++;
                else vals[3]++;
            }
            buildBarChart(wb, sheet2, "Returns by Book Condition", labels, vals);

            writeFile(wb, filePath);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // REPORT 3 — Fine Payments
    // ══════════════════════════════════════════════════════════════════
    public static void exportFineReport(List<Object[]> data, String filePath, String generatedBy) throws IOException {
        String[] headers = {"Fine ID","Member Name","Book Title","Days Overdue","Fine Amount","Paid Amount","Status","Processed By"};
        String title    = "Fine Payments Report";
        String subtitle = "Library Management System";

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet1 = wb.createSheet("Fine Report");
            XSSFSheet sheet2 = wb.createSheet("Chart");

            buildReportSheet(wb, sheet1, title, subtitle, headers, data, generatedBy, 6);

            // Pie chart data
            String[] labels = {"Paid","Unpaid","Waived"};
            long[] vals = {
                data.stream().filter(r -> "Paid".equals(r[6])).count(),
                data.stream().filter(r -> "Unpaid".equals(r[6])).count(),
                data.stream().filter(r -> "Waived".equals(r[6])).count()
            };
            buildBarChart(wb, sheet2, "Fine Payment Status", labels, vals);

            writeFile(wb, filePath);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // SHARED: Build Sheet 1
    // ══════════════════════════════════════════════════════════════════
    private static void buildReportSheet(XSSFWorkbook wb, XSSFSheet sheet,
                                          String title, String subtitle,
                                          String[] headers, List<Object[]> data,
                                          String generatedBy, int statusCol) {

        int totalCols = headers.length;

        // ── Set column widths ────────────────────────────────────────
        sheet.setColumnWidth(0, 3000);
        for (int i = 1; i < totalCols; i++) sheet.setColumnWidth(i, 5500);
    
       
// ── ROW 0: LOGO IMAGE FIX ───────────────────────────────────
Row logoRow = sheet.createRow(0);
logoRow.setHeightInPoints(120);

// Merge full header width
sheet.addMergedRegion(new CellRangeAddress(0,0,0,totalCols-1));

try {

    File file = new File("C:\\Users\\Lethay\\Downloads\\bu logo.png");

    if (!file.exists()) {
        System.out.println("LOGO NOT FOUND: " + file.getAbsolutePath());
        return;
    }

    FileInputStream fis = new FileInputStream(file);
    byte[] bytes = IOUtils.toByteArray(fis);
    fis.close();

    int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

    CreationHelper helper = wb.getCreationHelper();
    Drawing<?> drawing = sheet.createDrawingPatriarch();

    ClientAnchor anchor = helper.createClientAnchor();

    // 🔥 IMPORTANT: use larger placement area
    anchor.setCol1(0);
    anchor.setRow1(0);
    anchor.setCol2(5);
    anchor.setRow2(4);

    Picture pict = drawing.createPicture(anchor, pictureIdx);

    // ❌ REMOVE resize() completely (this is the common bug)
    // pict.resize();

    System.out.println("✔ LOGO INSERTED");

} catch (Exception e) {
    e.printStackTrace();
    System.out.println("❌ LOGO ERROR: " + e.getMessage());
}


        // ── ROW 1: System name ───────────────────────────────────────
        Row sysRow = sheet.createRow(1);
        sysRow.setHeightInPoints(30);
        Cell sysCell = sysRow.createCell(0);
        sysCell.setCellValue("LIBRARY MANAGEMENT SYSTEM");
        XSSFCellStyle sysStyle = wb.createCellStyle();
        sysStyle.setAlignment(HorizontalAlignment.CENTER);
        sysStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        sysStyle.setFillForegroundColor(AMBER_800);
        sysStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont sysFont = wb.createFont();
        sysFont.setBold(true);
        sysFont.setColor(WHITE);
        sysFont.setFontHeightInPoints((short)14);
        sysStyle.setFont(sysFont);
        sysCell.setCellStyle(sysStyle);
        sheet.addMergedRegion(new CellRangeAddress(1,1,0,totalCols-1));

        // ── ROW 2: Report title ──────────────────────────────────────
        Row titleRow = sheet.createRow(2);
        titleRow.setHeightInPoints(24);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        XSSFCellStyle titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFillForegroundColor(AMBER_50);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setColor(AMBER_800);
        titleFont.setFontHeightInPoints((short)12);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(2,2,0,totalCols-1));

        // ── ROW 3: Date generated ────────────────────────────────────
        Row dateRow = sheet.createRow(3);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Date Generated: " + LocalDate.now().toString() + "   |   Generated by: " + generatedBy);
        XSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setAlignment(HorizontalAlignment.CENTER);
        XSSFFont dateFont = wb.createFont();
        dateFont.setItalic(true);
        dateFont.setFontHeightInPoints((short)10);
        dateStyle.setFont(dateFont);
        dateCell.setCellStyle(dateStyle);
        sheet.addMergedRegion(new CellRangeAddress(3,3,0,totalCols-1));

        // ── ROW 4: Spacer ────────────────────────────────────────────
        sheet.createRow(4);

        // ── ROW 5: Column headers ────────────────────────────────────
        Row headerRow = sheet.createRow(5);
        headerRow.setHeightInPoints(20);
        XSSFCellStyle hdrStyle = wb.createCellStyle();
        hdrStyle.setFillForegroundColor(AMBER_800);
        hdrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        hdrStyle.setAlignment(HorizontalAlignment.CENTER);
        hdrStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont hdrFont = wb.createFont();
        hdrFont.setBold(true);
        hdrFont.setColor(WHITE);
        hdrFont.setFontHeightInPoints((short)11);
        hdrStyle.setFont(hdrFont);
        hdrStyle.setBorderBottom(BorderStyle.THIN);

        for (int c = 0; c < headers.length; c++) {
            Cell hCell = headerRow.createCell(c);
            hCell.setCellValue(headers[c]);
            hCell.setCellStyle(hdrStyle);
        }

        // ── ROWS 6+: Data ────────────────────────────────────────────
        XSSFCellStyle evenStyle = wb.createCellStyle();
        evenStyle.setFillForegroundColor(AMBER_50);
        evenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle oddStyle = wb.createCellStyle();
        // white background (default)

        XSSFCellStyle statusGreen = wb.createCellStyle();
        statusGreen.setFillForegroundColor(GREEN_BG);
        statusGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        statusGreen.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle statusRed = wb.createCellStyle();
        statusRed.setFillForegroundColor(RED_BG);
        statusRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        statusRed.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle statusGray = wb.createCellStyle();
        statusGray.setFillForegroundColor(GRAY_BG);
        statusGray.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        statusGray.setAlignment(HorizontalAlignment.CENTER);

        int rowNum = 6;
        for (Object[] record : data) {
            Row row = sheet.createRow(rowNum);
            row.setHeightInPoints(18);
            for (int c = 0; c < record.length && c < headers.length; c++) {
                Cell cell = row.createCell(c);
                String val = record[c] == null ? "" : record[c].toString();
                cell.setCellValue(val);
                // Status coloring
                if (c == statusCol) {
                    switch (val) {
                        case "Borrowed": case "Returned": case "Paid":
                            cell.setCellStyle(statusGreen); break;
                        case "Overdue":  case "Unpaid":
                            cell.setCellStyle(statusRed);   break;
                        case "Waived":
                            cell.setCellStyle(statusGray);  break;
                        default:
                            cell.setCellStyle(rowNum % 2 == 0 ? evenStyle : oddStyle);
                    }
                } else {
                    cell.setCellStyle(rowNum % 2 == 0 ? evenStyle : oddStyle);
                }
            }
            rowNum++;
        }

        // ── Total row ────────────────────────────────────────────────
        Row totalRow = sheet.createRow(rowNum + 1);
        XSSFCellStyle totalStyle = wb.createCellStyle();
        totalStyle.setFillForegroundColor(AMBER_100);
        totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont totalFont = wb.createFont();
        totalFont.setBold(true);
        totalStyle.setFont(totalFont);
        Cell totalLbl = totalRow.createCell(0);
        totalLbl.setCellValue("Total Records: " + data.size());
        totalLbl.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum+1, rowNum+1, 0, totalCols-1));

        // ── Signature block ──────────────────────────────────────────
        int sigStart = rowNum + 4;
        XSSFCellStyle sigStyle = wb.createCellStyle();
        XSSFFont sigFont = wb.createFont();
        sigFont.setBold(true);
        sigStyle.setFont(sigFont);

        Row r1 = sheet.createRow(sigStart);
        r1.createCell(0).setCellValue("Prepared by:");
        r1.createCell(1).setCellValue("________________________________");

        Row r2 = sheet.createRow(sigStart + 1);
        r2.createCell(0).setCellValue("Name:");
        r2.createCell(1).setCellValue("________________________________");

        Row r3 = sheet.createRow(sigStart + 2);
        r3.createCell(0).setCellValue("Position:");
        r3.createCell(1).setCellValue("________________________________");

        Row r4 = sheet.createRow(sigStart + 3);
        r4.createCell(0).setCellValue("Date Signed:");
        r4.createCell(1).setCellValue("________________________________");

        for (Row sr : new Row[]{r1,r2,r3,r4}) {
            sr.getCell(0).setCellStyle(sigStyle);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // SHARED: Build Sheet 2 Bar Chart
    // ══════════════════════════════════════════════════════════════════
    private static void buildBarChart(XSSFWorkbook wb, XSSFSheet sheet,
                                       String chartTitle, String[] labels, long[] values) {
        // Write data to sheet first (hidden table for chart source)
        Row hdr = sheet.createRow(0);
        hdr.createCell(0).setCellValue("Category");
        hdr.createCell(1).setCellValue("Count");

        for (int i = 0; i < labels.length; i++) {
            Row r = sheet.createRow(i + 1);
            r.createCell(0).setCellValue(labels[i]);
            r.createCell(1).setCellValue(values[i]);
        }

        // Title label on sheet
        Row titleRow = sheet.createRow(labels.length + 3);
        XSSFCell titleCell = (XSSFCell) titleRow.createCell(0);
        titleCell.setCellValue(chartTitle);
        XSSFCellStyle ts = wb.createCellStyle();
        XSSFFont tf = wb.createFont();
        tf.setBold(true); tf.setFontHeightInPoints((short)14);
        tf.setColor(AMBER_800);
        ts.setFont(tf);
        titleCell.setCellStyle(ts);

        // Embed chart using Apache POI XDDF
        try {
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0,0,0,0,0,labels.length+5,10,labels.length+25);
            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText(chartTitle);
            chart.setTitleOverlay(false);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);

            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            bottomAxis.setTitle("Category");
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Count");
            leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            XDDFDataSource<String> catSource = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new org.apache.poi.ss.util.CellRangeAddress(1,labels.length,0,0));
            XDDFNumericalDataSource<Double> valSource = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new org.apache.poi.ss.util.CellRangeAddress(1,labels.length,1,1));

            XDDFBarChartData barData = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
            barData.setBarDirection(BarDirection.COL);
            XDDFBarChartData.Series series = (XDDFBarChartData.Series) barData.addSeries(catSource, valSource);
            series.setTitle(chartTitle, null);
            chart.plot(barData);

        } catch (Exception e) {
            // Chart creation may fail on some POI versions — data still on sheet
            System.out.println("[ExcelExporter] Chart note: " + e.getMessage());
        }
    }

    private static void writeFile(XSSFWorkbook wb, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        }
    }
}
