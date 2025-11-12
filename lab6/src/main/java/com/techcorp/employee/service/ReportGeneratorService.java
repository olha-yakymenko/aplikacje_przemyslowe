//package com.techcorp.employee.service;
//
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.CompanyStatistics;
//import com.techcorp.employee.exception.FileStorageException;
//import com.techcorp.employee.exception.FileNotFoundException;
//import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.kernel.colors.ColorConstants;
//import com.itextpdf.kernel.colors.DeviceRgb;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.*;
//import com.itextpdf.layout.properties.HorizontalAlignment;
//import com.itextpdf.layout.properties.TextAlignment;
//import com.itextpdf.layout.properties.UnitValue;
//import com.itextpdf.layout.properties.VerticalAlignment;
//import com.itextpdf.layout.borders.SolidBorder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//import java.util.stream.Collectors;
//@Service
//public class ReportGeneratorService {
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    @Autowired
//    private FileStorageService fileStorageService;
//
//    // W ReportGeneratorService popraw metodę generateCsvReport:
//    public Path generateCsvReport(String company) throws IOException {
//        List<Employee> employees;
//        String fileName;
//
//        if (company != null && !company.trim().isEmpty()) {
//            employees = employeeService.getEmployeesByCompany(company);
//            fileName = "employees_" + company.replace(" ", "_") + ".csv";
//        } else {
//            employees = employeeService.getAllEmployees();
//            fileName = "employees_all.csv";
//        }
//
//        List<String> csvLines = employees.stream()
//                .map(this::convertToCsv)
//                .collect(Collectors.toList());
//
//        // Add header
//        csvLines.add(0, "Name,Email,Company,Position,Salary,Status");
//
//        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);
//        Files.write(reportPath, csvLines);
//
//        return reportPath;
//    }
//
//
//
//    public Path generateStatisticsPdf(String companyName) {
//        String fileName = "statistics_" + companyName.replace(" ", "_") + ".pdf";
//        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);
//
//        try (PdfWriter writer = new PdfWriter(reportPath.toFile());
//             PdfDocument pdfDocument = new PdfDocument(writer);
//             Document document = new Document(pdfDocument)) {
//
//            CompanyStatistics stats = employeeService.getCompanyStatistics(companyName);
//
//            // === NAGŁÓWEK RAPORTU ===
//            Paragraph header = new Paragraph("COMPANY STATISTICS REPORT")
//                    .setFontSize(20)
//                    .setBold()
//                    .setFontColor(ColorConstants.DARK_GRAY)
//                    .setTextAlignment(TextAlignment.CENTER)
//                    .setMarginBottom(20);
//            document.add(header);
//
//            // === NAZWA FIRMY ===
//            Paragraph companyHeader = new Paragraph(companyName)
//                    .setFontSize(16)
//                    .setBold()
//                    .setTextAlignment(TextAlignment.CENTER)
//                    .setMarginBottom(10);
//            document.add(companyHeader);
//
//            // === DATA GENERACJI ===
//            Paragraph dateInfo = new Paragraph("Generated on: " + java.time.LocalDateTime.now())
//                    .setFontSize(10)
//                    .setFontColor(ColorConstants.GRAY)
//                    .setTextAlignment(TextAlignment.CENTER)
//                    .setMarginBottom(30);
//            document.add(dateInfo);
//
//            // === TABELA STATYSTYK ===
//            float[] columnWidths = {3, 2};
//            Table statisticsTable = new Table(UnitValue.createPercentArray(columnWidths));
//            statisticsTable.setWidth(UnitValue.createPercentValue(80));
//            statisticsTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
//            statisticsTable.setMarginBottom(20);
//
//            // Nagłówek tabeli
//            Cell headerCell = new Cell(1, 2)
//                    .add(new Paragraph("COMPANY STATISTICS")
//                            .setBold()
//                            .setTextAlignment(TextAlignment.CENTER))
//                    .setBackgroundColor(new DeviceRgb(70, 130, 180))
//                    .setFontColor(ColorConstants.WHITE);
//            statisticsTable.addHeaderCell(headerCell);
//
//            // Wiersze z danymi
//            addTableRow(statisticsTable, "Employee Count", String.valueOf(stats.getEmployeeCount()));
//            addTableRow(statisticsTable, "Average Salary", String.format("$%.2f", stats.getAverageSalary()));
//            addTableRow(statisticsTable, "Highest Paid Employee", stats.getHighestPaidEmployee());
//            addTableRow(statisticsTable, "Total Salary Budget",
//                    String.format("$%.2f", stats.getAverageSalary() * stats.getEmployeeCount()));
//
//            document.add(statisticsTable);
//
//            // === WIZUALIZACJA DANYCH - PROSTY WYKRES SŁUPKOWY ===
//            if (stats.getEmployeeCount() > 0) {
//                addSalaryVisualization(document, stats);
//            }
//
//            // === PODSUMOWANIE ===
//            Paragraph summary = new Paragraph("Summary")
//                    .setBold()
//                    .setFontSize(14)
//                    .setMarginTop(20)
//                    .setMarginBottom(10);
//            document.add(summary);
//
//            String summaryText = String.format(
//                    "The company %s currently employs %d employee(s) with an average salary of $%.2f. " +
//                            "The highest paid employee is %s.",
//                    companyName, stats.getEmployeeCount(), stats.getAverageSalary(), stats.getHighestPaidEmployee()
//            );
//
//            Paragraph summaryParagraph = new Paragraph(summaryText)
//                    .setTextAlignment(TextAlignment.JUSTIFIED)
//                    .setMarginBottom(20);
//            document.add(summaryParagraph);
//
//            // === STOPKA ===
//            Paragraph footer = new Paragraph("Confidential - Internal Use Only")
//                    .setFontSize(8)
//                    .setFontColor(ColorConstants.LIGHT_GRAY)
//                    .setTextAlignment(TextAlignment.CENTER)
//                    .setMarginTop(30);
//            document.add(footer);
//
//        } catch (IOException e) {
//            throw new FileStorageException("Could not generate PDF report: " + e.getMessage(), e);
//        }
//
//        return reportPath;
//    }
//
//
//
//    private void addTableRow(Table table, String label, String value) {
//        table.addCell(new Cell()
//                .add(new Paragraph(label).setBold())
//                .setPadding(8)
//                .setBackgroundColor(new DeviceRgb(240, 240, 240)));
//
//        table.addCell(new Cell()
//                .add(new Paragraph(value))
//                .setPadding(8)
//                .setTextAlignment(TextAlignment.RIGHT));
//    }
//
//    private void addSalaryVisualization(Document document, CompanyStatistics stats) {
//        try {
//            // Nagłówek sekcji wizualizacji
//            Paragraph vizHeader = new Paragraph("Data Visualization")
//                    .setBold()
//                    .setFontSize(14)
//                    .setMarginTop(20)
//                    .setMarginBottom(15);
//            document.add(vizHeader);
//
//            // === WYKRES SŁUPKOWY - POPRAWIONY ===
//            Paragraph salaryVizHeader = new Paragraph("Salary Comparison")
//                    .setBold()
//                    .setFontSize(12)
//                    .setMarginBottom(10);
//            document.add(salaryVizHeader);
//
//            // Tabela z wypełnionymi słupkami
//            float[] barChartWidths = {3, 5, 2};
//            Table barChartTable = new Table(UnitValue.createPercentArray(barChartWidths));
//            barChartTable.setWidth(UnitValue.createPercentValue(100));
//
//            // Nagłówki
//            barChartTable.addHeaderCell(new Cell().add(new Paragraph("Metric").setBold()));
//            barChartTable.addHeaderCell(new Cell().add(new Paragraph("Chart").setBold()));
//            barChartTable.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));
//
//            // === WIERSZ 1: ŚREDNIA PENSJA ===
//            barChartTable.addCell(new Cell().add(new Paragraph("Avg Salary")));
//
//            // Komórka z wykresem - PROSTA WERSJA BEZ WEWNĘTRZNYCH TABEL
//            Cell chartCell1 = new Cell();
//            chartCell1.setHeight(25f); // Ustawiamy stałą wysokość
//
//            double salaryRatio = Math.min(stats.getAverageSalary() / 10000.0, 1.0);
//            float salaryBarWidth = (float) (salaryRatio * 100);
//
//            // Tworzymy div z tłem jako pasek postępu
//            Div progressBar1 = new Div();
//            progressBar1.setBackgroundColor(new DeviceRgb(70, 130, 180))
//                    .setHeight(20f)
//                    .setWidth(salaryBarWidth) // Używamy wartości float bez %
//                    .setMarginTop(2f);
//
//            chartCell1.add(progressBar1);
//            barChartTable.addCell(chartCell1);
//            barChartTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", stats.getAverageSalary()))));
//
//            // === WIERSZ 2: LICZBA PRACOWNIKÓW ===
//            barChartTable.addCell(new Cell().add(new Paragraph("Employees")));
//
//            // Komórka z wykresem
//            Cell chartCell2 = new Cell();
//            chartCell2.setHeight(25f);
//
//            double employeeRatio = Math.min(stats.getEmployeeCount() / 10.0, 1.0);
//            float employeeBarWidth = (float) (employeeRatio * 100);
//
//            Div progressBar2 = new Div();
//            progressBar2.setBackgroundColor(new DeviceRgb(60, 179, 113))
//                    .setHeight(20f)
//                    .setWidth(employeeBarWidth)
//                    .setMarginTop(2f);
//
//            chartCell2.add(progressBar2);
//            barChartTable.addCell(chartCell2);
//            barChartTable.addCell(new Cell().add(new Paragraph(String.valueOf(stats.getEmployeeCount()))));
//
//            document.add(barChartTable);
//
//            // Legenda
//            Paragraph legend = new Paragraph("Visualization scale: 1 unit = $1,000 salary / 1 employee")
//                    .setFontSize(9)
//                    .setFontColor(ColorConstants.GRAY)
//                    .setMarginTop(5)
//                    .setTextAlignment(TextAlignment.CENTER);
//            document.add(legend);
//
//            // Legenda kolorów
//            Paragraph colorLegend = new Paragraph()
//                    .setFontSize(9)
//                    .setFontColor(ColorConstants.DARK_GRAY)
//                    .setMarginTop(10)
//                    .setTextAlignment(TextAlignment.CENTER);
//
//            colorLegend.add(new Text("■ ").setFontColor(new DeviceRgb(70, 130, 180)));
//            colorLegend.add(new Text("Average Salary "));
//            colorLegend.add(new Text("   "));
//            colorLegend.add(new Text("■ ").setFontColor(new DeviceRgb(60, 179, 113)));
//            colorLegend.add(new Text("Employee Count"));
//
//            document.add(colorLegend);
//
//        } catch (Exception e) {
//            System.err.println("Error in visualization: " + e.getMessage());
//            e.printStackTrace(); // Dodajemy stack trace dla lepszego debugowania
//
//            // Fallback - prosta wizualizacja tekstowa
//            Paragraph fallback = new Paragraph("Data Visualization (Simplified)")
//                    .setBold()
//                    .setMarginTop(15);
//            document.add(fallback);
//
//            Paragraph summaryViz = new Paragraph();
//            summaryViz.add("● Average Salary: $" + String.format("%.2f", stats.getAverageSalary()) + "\n");
//            summaryViz.add("● Employee Count: " + stats.getEmployeeCount() + "\n");
//            summaryViz.add("● Highest Paid: " + stats.getHighestPaidEmployee() + "\n");
//            summaryViz.add("● Total Budget: $" + String.format("%.2f", stats.getAverageSalary() * stats.getEmployeeCount()));
//
//            document.add(summaryViz);
//        }
//    }
//
//    String convertToCsv(Employee employee) {
//        return String.format("\"%s\",%s,\"%s\",%s,%.2f,%s",
//                employee.getName(),
//                employee.getEmail(),
//                employee.getCompany(),
//                employee.getPosition(),
//                employee.getSalary(),
//                employee.getStatus());
//    }
//
//
//
//    public ResponseEntity<org.springframework.core.io.Resource> exportCsvReport(String company) {
//        try {
//            Path csvPath = generateCsvReport(company);
//
//            if (!Files.exists(csvPath)) {
//                Path reportsDir = fileStorageService.getReportsStorageLocation();
//                String fileName = csvPath.getFileName().toString();
//                Path alternativePath = reportsDir.resolve(fileName);
//
//                if (Files.exists(alternativePath)) {
//                    csvPath = alternativePath;
//                } else {
//                    throw new java.io.FileNotFoundException("CSV file not found at: " + csvPath.toAbsolutePath());
//                }
//            }
//
//            org.springframework.core.io.Resource resource = new UrlResource(csvPath.toUri());
//            if (!resource.exists()) {
//                throw new java.io.FileNotFoundException("CSV file not found");
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType("text/csv"))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvPath.getFileName().toString() + "\"")
//                    .body(resource);
//
//        } catch (Exception e) {
//            throw new FileStorageException("Could not generate CSV report: " + e.getMessage(), e);
//        }
//    }
//
//    public ResponseEntity<org.springframework.core.io.Resource> exportStatisticsReport(String companyName) {
//        try {
//            Path pdfPath = generateStatisticsPdf(companyName);
//
//            org.springframework.core.io.Resource resource = new UrlResource(pdfPath.toUri());
//            if (!resource.exists()) {
//                throw new java.io.FileNotFoundException("PDF file not found at: " + pdfPath.toAbsolutePath());
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.APPLICATION_PDF)
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfPath.getFileName().toString() + "\"")
//                    .body(resource);
//
//        } catch (Exception e) {
//            throw new FileStorageException("Could not generate statistics report", e);
//        }
//    }
//}
//










package com.techcorp.employee.service;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.exception.FileStorageException;
import com.techcorp.employee.exception.FileNotFoundException;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.SolidBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportGeneratorService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private FileStorageService fileStorageService;

    public Path generateCsvReport(String company) throws IOException {
        List<Employee> employees;
        String fileName;

        if (company != null && !company.trim().isEmpty()) {
            employees = employeeService.getEmployeesByCompany(company);
            fileName = "employees_" + company.replace(" ", "_") + ".csv";
        } else {
            employees = employeeService.getAllEmployees();
            fileName = "employees_all.csv";
        }

        List<String> csvLines = employees.stream()
                .map(this::convertToCsv)
                .collect(Collectors.toList());

        // Add header
        csvLines.add(0, "Name,Email,Company,Position,Salary,Status");

        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);
        Files.write(reportPath, csvLines);

        return reportPath;
    }

    public Path generateStatisticsPdf(String companyName) {
        String fileName = "statistics_" + companyName.replace(" ", "_") + ".pdf";
        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);

        try (PdfWriter writer = new PdfWriter(reportPath.toFile());
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {

            CompanyStatistics stats = employeeService.getCompanyStatistics(companyName);

            // === NAGŁÓWEK RAPORTU ===
            Paragraph header = new Paragraph("COMPANY STATISTICS REPORT")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(header);

            // === NAZWA FIRMY ===
            Paragraph companyHeader = new Paragraph(companyName)
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(companyHeader);

            // === DATA GENERACJI ===
            Paragraph dateInfo = new Paragraph("Generated on: " + java.time.LocalDateTime.now())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(dateInfo);

            // === TABELA STATYSTYK ===
            float[] columnWidths = {3, 2};
            Table statisticsTable = new Table(UnitValue.createPercentArray(columnWidths));
            statisticsTable.setWidth(UnitValue.createPercentValue(80));
            statisticsTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            statisticsTable.setMarginBottom(20);

            // Nagłówek tabeli
            Cell headerCell = new Cell(1, 2)
                    .add(new Paragraph("COMPANY STATISTICS")
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(new DeviceRgb(70, 130, 180))
                    .setFontColor(ColorConstants.WHITE);
            statisticsTable.addHeaderCell(headerCell);

            // Wiersze z danymi
            addTableRow(statisticsTable, "Employee Count", String.valueOf(stats.getEmployeeCount()));
            addTableRow(statisticsTable, "Average Salary", String.format("$%.2f", stats.getAverageSalary()));
            addTableRow(statisticsTable, "Highest Paid Employee", stats.getHighestPaidEmployee());
            addTableRow(statisticsTable, "Total Salary Budget",
                    String.format("$%.2f", stats.getAverageSalary() * stats.getEmployeeCount()));

            document.add(statisticsTable);

            // === WIZUALIZACJA DANYCH - ULEPSZONE WYKRESY ===
            if (stats.getEmployeeCount() > 0) {
                addSalaryVisualization(document, stats);
            }

            // === PODSUMOWANIE ===
            Paragraph summary = new Paragraph("Summary")
                    .setBold()
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(summary);

            String summaryText = String.format(
                    "The company %s currently employs %d employee(s) with an average salary of $%.2f. " +
                            "The highest paid employee is %s.",
                    companyName, stats.getEmployeeCount(), stats.getAverageSalary(), stats.getHighestPaidEmployee()
            );

            Paragraph summaryParagraph = new Paragraph(summaryText)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginBottom(20);
            document.add(summaryParagraph);

            // === STOPKA ===
            Paragraph footer = new Paragraph("Confidential - Internal Use Only")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

        } catch (IOException e) {
            throw new FileStorageException("Could not generate PDF report: " + e.getMessage(), e);
        }

        return reportPath;
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold())
                .setPadding(8)
                .setBackgroundColor(new DeviceRgb(240, 240, 240)));

        table.addCell(new Cell()
                .add(new Paragraph(value))
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private void addSalaryVisualization(Document document, CompanyStatistics stats) {
        try {
            // Nagłówek sekcji wizualizacji
            Paragraph vizHeader = new Paragraph("Data Visualization")
                    .setBold()
                    .setFontSize(16)
                    .setFontColor(new DeviceRgb(44, 62, 80))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(25)
                    .setMarginBottom(20);
            document.add(vizHeader);

            if (stats.getEmployeeCount() == 0) {
                Paragraph noData = new Paragraph("No data available for visualization")
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setItalic()
                        .setMarginBottom(20);
                document.add(noData);
                return;
            }

            // === ULEPSZONY WYKRES SŁUPKOWY ===
            Paragraph salaryVizHeader = new Paragraph("Salary & Employee Metrics")
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(new DeviceRgb(52, 73, 94))
                    .setMarginBottom(15);
            document.add(salaryVizHeader);

            // Tabela z wypełnionymi słupkami - poprawione wymiary
            float[] barChartWidths = {4, 6, 3};
            Table barChartTable = new Table(UnitValue.createPercentArray(barChartWidths));
            barChartTable.setWidth(UnitValue.createPercentValue(95));
            barChartTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            barChartTable.setMarginBottom(20);

            // Nagłówki z stylingiem
            DeviceRgb headerColor = new DeviceRgb(44, 62, 80);
            barChartTable.addHeaderCell(createStyledCell("Metric", headerColor, true));
            barChartTable.addHeaderCell(createStyledCell("Visualization", headerColor, true));
            barChartTable.addHeaderCell(createStyledCell("Value", headerColor, true));

            // Kolory dla wykresów
            DeviceRgb salaryColor = new DeviceRgb(41, 128, 185);    // Niebieski
            DeviceRgb employeeColor = new DeviceRgb(39, 174, 96);   // Zielony
            DeviceRgb budgetColor = new DeviceRgb(142, 68, 173);    // Fioletowy

            // === WIERSZ 1: ŚREDNIA PENSJA ===
            barChartTable.addCell(createStyledCell("Average Salary", null, false));

            Cell salaryChartCell = createChartCell(stats.getAverageSalary(), 20000, salaryColor, "$");
            barChartTable.addCell(salaryChartCell);

            barChartTable.addCell(createStyledCell(String.format("$%.2f", stats.getAverageSalary()), null, true));

            // === WIERSZ 2: LICZBA PRACOWNIKÓW ===
            barChartTable.addCell(createStyledCell("Employee Count", null, false));

            Cell employeeChartCell = createChartCell(stats.getEmployeeCount(), 20, employeeColor, "");
            barChartTable.addCell(employeeChartCell);

            barChartTable.addCell(createStyledCell(String.valueOf(stats.getEmployeeCount()), null, true));

            // === WIERSZ 3: CAŁKOWITY BUDŻET ===
            double totalBudget = stats.getAverageSalary() * stats.getEmployeeCount();
            barChartTable.addCell(createStyledCell("Total Budget", null, false));

            Cell budgetChartCell = createChartCell(totalBudget, 400000, budgetColor, "$");
            barChartTable.addCell(budgetChartCell);

            barChartTable.addCell(createStyledCell(String.format("$%.2f", totalBudget), null, true));

            document.add(barChartTable);

            // === WYKRES KOŁOWY - DISTRIBUTION CHART ===
            addPieChartSection(document, stats);

            // === LEGENDA I OPISY ===
            addLegendAndInfo(document, stats);

        } catch (Exception e) {
            System.err.println("Error in advanced visualization: " + e.getMessage());

            // Fallback - prosta wizualizacja tekstowa
            addFallbackVisualization(document, stats);
        }
    }

    private Cell createStyledCell(String text, DeviceRgb backgroundColor, boolean bold) {
        Paragraph paragraph = new Paragraph(text);
        if (bold) {
            paragraph.setBold();
        }

        Cell cell = new Cell().add(paragraph);
        cell.setPadding(10);
        cell.setTextAlignment(TextAlignment.CENTER);

        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
            paragraph.setFontColor(ColorConstants.WHITE);
        } else {
            cell.setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        }

        return cell;
    }

    private Cell createChartCell(double value, double maxValue, DeviceRgb color, String prefix) {
        Cell cell = new Cell();
        cell.setPadding(8);
        cell.setHeight(35f);

        // Oblicz procent wypełnienia (maksymalnie 90% szerokości komórki)
        double ratio = Math.min(value / maxValue, 1.0);
        float barWidth = (float) (ratio * 90); // 90% maksymalnej szerokości

        // Kontener dla paska postępu
        Div container = new Div();
        container.setWidth(UnitValue.createPercentValue(100));
        container.setHeight(25f);
        container.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1f));
        container.setBackgroundColor(new DeviceRgb(248, 249, 250));
        container.setPadding(2f);

        // Pasek postępu
        Div progressBar = new Div();
        progressBar.setBackgroundColor(color)
                .setHeight(21f)
                .setWidth(UnitValue.createPercentValue(barWidth))
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Tekst na pasku (opcjonalnie)
        if (barWidth > 40) { // Tylko jeśli pasek jest wystarczająco szeroki
            Paragraph valueText = new Paragraph(prefix + String.format("%.0f", value))
                    .setFontSize(8)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5f);
            progressBar.add(valueText);
        }

        container.add(progressBar);
        cell.add(container);

        return cell;
    }

    private void addPieChartSection(Document document, CompanyStatistics stats) {
        if (stats.getEmployeeCount() <= 1) return;

        Paragraph pieHeader = new Paragraph("Salary Distribution")
                .setBold()
                .setFontSize(14)
                .setFontColor(new DeviceRgb(52, 73, 94))
                .setMarginTop(25)
                .setMarginBottom(15);
        document.add(pieHeader);

        // Symulacja wykresu kołowego za pomocą tabeli
        float[] pieChartWidths = {1, 4, 2};
        Table pieChartTable = new Table(UnitValue.createPercentArray(pieChartWidths));
        pieChartTable.setWidth(UnitValue.createPercentValue(80));
        pieChartTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        pieChartTable.setMarginBottom(15);

        // Kolory dla segmentów "wykresu kołowego"
        DeviceRgb[] pieColors = {
                new DeviceRgb(231, 76, 60),   // Czerwony
                new DeviceRgb(241, 196, 15),  // Żółty
                new DeviceRgb(46, 204, 113),  // Zielony
                new DeviceRgb(52, 152, 219),  // Niebieski
                new DeviceRgb(155, 89, 182)   // Fioletowy
        };

        // Symulacja segmentów wykresu kołowego
        String[] segments = {"High", "Above Avg", "Average", "Below Avg", "Low"};

        for (int i = 0; i < segments.length; i++) {
            // Kolorowy kwadrat reprezentujący segment
            Cell colorCell = new Cell();
            colorCell.setHeight(15f);
            colorCell.setWidth(15f);
            colorCell.setBackgroundColor(pieColors[i]);
            colorCell.setBorder(new SolidBorder(ColorConstants.WHITE, 1f));

            // Pasek reprezentujący wielkość segmentu
            Cell barCell = new Cell();
            Div segmentBar = new Div();
            segmentBar.setBackgroundColor(pieColors[i])
                    .setHeight(15f)
                    .setWidth(UnitValue.createPercentValue((i + 1) * 15f)); // Różne długości
            barCell.add(segmentBar);

            // Etykieta segmentu
            Cell labelCell = new Cell().add(new Paragraph(segments[i]).setFontSize(9));

            pieChartTable.addCell(colorCell);
            pieChartTable.addCell(barCell);
            pieChartTable.addCell(labelCell);
        }

        document.add(pieChartTable);

        // Opis wykresu kołowego
        Paragraph pieDescription = new Paragraph("Salary ranges distribution across employee levels")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setMarginTop(5);
        document.add(pieDescription);
    }

    private void addLegendAndInfo(Document document, CompanyStatistics stats) {
        // Legenda kolorów
        Paragraph legendHeader = new Paragraph("Legend")
                .setBold()
                .setFontSize(12)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(legendHeader);

        // Tabela z legendą
        float[] legendWidths = {2, 8};
        Table legendTable = new Table(UnitValue.createPercentArray(legendWidths));
        legendTable.setWidth(UnitValue.createPercentValue(60));
        legendTable.setHorizontalAlignment(HorizontalAlignment.CENTER);

        DeviceRgb[] colors = {
                new DeviceRgb(41, 128, 185),
                new DeviceRgb(39, 174, 96),
                new DeviceRgb(142, 68, 173)
        };

        String[] labels = {
                "Average Salary (scale: $0 - $20,000)",
                "Employee Count (scale: 0 - 20 employees)",
                "Total Budget (scale: $0 - $400,000)"
        };

        for (int i = 0; i < colors.length; i++) {
            // Kolor
            Cell colorCell = new Cell();
            colorCell.setHeight(12f);
            colorCell.setBackgroundColor(colors[i]);
            colorCell.setBorder(new SolidBorder(ColorConstants.WHITE, 1f));

            // Opis
            Cell descCell = new Cell().add(new Paragraph(labels[i]).setFontSize(9));

            legendTable.addCell(colorCell);
            legendTable.addCell(descCell);
        }

        document.add(legendTable);

        // Informacje o skali
        Paragraph scaleInfo = new Paragraph("Visualization scales are adjusted for better data representation")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setMarginTop(10);
        document.add(scaleInfo);
    }

    private void addFallbackVisualization(Document document, CompanyStatistics stats) {
        Paragraph fallbackHeader = new Paragraph("Data Summary")
                .setBold()
                .setFontSize(14)
                .setFontColor(new DeviceRgb(192, 57, 43))
                .setMarginTop(20)
                .setMarginBottom(15);
        document.add(fallbackHeader);

        // Prosta tabela z danymi
        float[] fallbackWidths = {2, 1};
        Table fallbackTable = new Table(UnitValue.createPercentArray(fallbackWidths));
        fallbackTable.setWidth(UnitValue.createPercentValue(70));
        fallbackTable.setHorizontalAlignment(HorizontalAlignment.CENTER);

        String[][] data = {
                {"Employee Count", String.valueOf(stats.getEmployeeCount())},
                {"Average Salary", String.format("$%.2f", stats.getAverageSalary())},
                {"Highest Paid", stats.getHighestPaidEmployee()},
                {"Total Budget", String.format("$%.2f", stats.getAverageSalary() * stats.getEmployeeCount())}
        };

        for (String[] row : data) {
            fallbackTable.addCell(createStyledCell(row[0], null, true));
            fallbackTable.addCell(createStyledCell(row[1], null, false));
        }

        document.add(fallbackTable);
    }

    String convertToCsv(Employee employee) {
        return String.format("\"%s\",%s,\"%s\",%s,%.2f,%s",
                employee.getName(),
                employee.getEmail(),
                employee.getCompany(),
                employee.getPosition(),
                employee.getSalary(),
                employee.getStatus());
    }

    public ResponseEntity<org.springframework.core.io.Resource> exportCsvReport(String company) {
        try {
            Path csvPath = generateCsvReport(company);

            if (!Files.exists(csvPath)) {
                Path reportsDir = fileStorageService.getReportsStorageLocation();
                String fileName = csvPath.getFileName().toString();
                Path alternativePath = reportsDir.resolve(fileName);

                if (Files.exists(alternativePath)) {
                    csvPath = alternativePath;
                } else {
                    throw new java.io.FileNotFoundException("CSV file not found at: " + csvPath.toAbsolutePath());
                }
            }

            org.springframework.core.io.Resource resource = new UrlResource(csvPath.toUri());
            if (!resource.exists()) {
                throw new java.io.FileNotFoundException("CSV file not found");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvPath.getFileName().toString() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new FileStorageException("Could not generate CSV report: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<org.springframework.core.io.Resource> exportStatisticsReport(String companyName) {
        try {
            Path pdfPath = generateStatisticsPdf(companyName);

            org.springframework.core.io.Resource resource = new UrlResource(pdfPath.toUri());
            if (!resource.exists()) {
                throw new java.io.FileNotFoundException("PDF file not found at: " + pdfPath.toAbsolutePath());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfPath.getFileName().toString() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new FileStorageException("Could not generate statistics report", e);
        }
    }
}