package com.techcorp.employee.service;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.exception.FileStorageException;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.SolidBorder;
import org.springframework.beans.factory.annotation.Autowired;
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

    // W ReportGeneratorService popraw metodę generateCsvReport:
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

//    public Path generateStatisticsPdf(String companyName) throws IOException {
//        CompanyStatistics stats = employeeService.getCompanyStatistics(companyName);
//
//        // Simple PDF generation - in real application use iText or PDFBox
//        List<String> pdfContent = List.of(
//                "Company Statistics Report",
//                "Company: " + companyName,
//                "Employee Count: " + stats.getEmployeeCount(),
//                "Average Salary: " + String.format("%.2f", stats.getAverageSalary()),
//                "Highest Paid Employee: " + stats.getHighestPaidEmployee(),
//                "Generated on: " + java.time.LocalDateTime.now()
//        );
//
//        String fileName = "statistics_" + companyName.replace(" ", "_") + ".pdf";
//        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);
//        Files.write(reportPath, pdfContent);
//
//        return reportPath;
//    }


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
//                    "The company %s currently employs %d employees with an average salary of $%.2f. " +
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

            // === WIZUALIZACJA DANYCH - PROSTY WYKRES SŁUPKOWY ===
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


    /**
     * Dodaje wiersz do tabeli
     */
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

    /**
     * Dodaje wizualizację danych z wypełnionymi wykresami - POPRAWIONA
     */
    private void addSalaryVisualization(Document document, CompanyStatistics stats) {
        try {
            // Nagłówek sekcji wizualizacji
            Paragraph vizHeader = new Paragraph("Data Visualization")
                    .setBold()
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(15);
            document.add(vizHeader);

            // === WYKRES SŁUPKOWY - POPRAWIONY ===
            Paragraph salaryVizHeader = new Paragraph("Salary Comparison")
                    .setBold()
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(salaryVizHeader);

            // Tabela z wypełnionymi słupkami
            float[] barChartWidths = {3, 5, 2};
            Table barChartTable = new Table(UnitValue.createPercentArray(barChartWidths));
            barChartTable.setWidth(UnitValue.createPercentValue(100));

            // Nagłówki
            barChartTable.addHeaderCell(new Cell().add(new Paragraph("Metric").setBold()));
            barChartTable.addHeaderCell(new Cell().add(new Paragraph("Chart").setBold()));
            barChartTable.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));

            // === WIERSZ 1: ŚREDNIA PENSJA ===
            barChartTable.addCell(new Cell().add(new Paragraph("Avg Salary")));

            // Komórka z wykresem - PROSTA WERSJA BEZ WEWNĘTRZNYCH TABEL
            Cell chartCell1 = new Cell();
            chartCell1.setHeight(25f); // Ustawiamy stałą wysokość

            double salaryRatio = Math.min(stats.getAverageSalary() / 10000.0, 1.0);
            float salaryBarWidth = (float) (salaryRatio * 100);

            // Tworzymy div z tłem jako pasek postępu
            Div progressBar1 = new Div();
            progressBar1.setBackgroundColor(new DeviceRgb(70, 130, 180))
                    .setHeight(20f)
                    .setWidth(salaryBarWidth) // Używamy wartości float bez %
                    .setMarginTop(2f);

            chartCell1.add(progressBar1);
            barChartTable.addCell(chartCell1);
            barChartTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", stats.getAverageSalary()))));

            // === WIERSZ 2: LICZBA PRACOWNIKÓW ===
            barChartTable.addCell(new Cell().add(new Paragraph("Employees")));

            // Komórka z wykresem
            Cell chartCell2 = new Cell();
            chartCell2.setHeight(25f);

            double employeeRatio = Math.min(stats.getEmployeeCount() / 10.0, 1.0);
            float employeeBarWidth = (float) (employeeRatio * 100);

            Div progressBar2 = new Div();
            progressBar2.setBackgroundColor(new DeviceRgb(60, 179, 113))
                    .setHeight(20f)
                    .setWidth(employeeBarWidth)
                    .setMarginTop(2f);

            chartCell2.add(progressBar2);
            barChartTable.addCell(chartCell2);
            barChartTable.addCell(new Cell().add(new Paragraph(String.valueOf(stats.getEmployeeCount()))));

            document.add(barChartTable);

            // Legenda
            Paragraph legend = new Paragraph("Visualization scale: 1 unit = $1,000 salary / 1 employee")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(5)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(legend);

            // Legenda kolorów
            Paragraph colorLegend = new Paragraph()
                    .setFontSize(9)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginTop(10)
                    .setTextAlignment(TextAlignment.CENTER);

            colorLegend.add(new Text("■ ").setFontColor(new DeviceRgb(70, 130, 180)));
            colorLegend.add(new Text("Average Salary "));
            colorLegend.add(new Text("   "));
            colorLegend.add(new Text("■ ").setFontColor(new DeviceRgb(60, 179, 113)));
            colorLegend.add(new Text("Employee Count"));

            document.add(colorLegend);

        } catch (Exception e) {
            System.err.println("Error in visualization: " + e.getMessage());
            e.printStackTrace(); // Dodajemy stack trace dla lepszego debugowania

            // Fallback - prosta wizualizacja tekstowa
            Paragraph fallback = new Paragraph("Data Visualization (Simplified)")
                    .setBold()
                    .setMarginTop(15);
            document.add(fallback);

            Paragraph summaryViz = new Paragraph();
            summaryViz.add("● Average Salary: $" + String.format("%.2f", stats.getAverageSalary()) + "\n");
            summaryViz.add("● Employee Count: " + stats.getEmployeeCount() + "\n");
            summaryViz.add("● Highest Paid: " + stats.getHighestPaidEmployee() + "\n");
            summaryViz.add("● Total Budget: $" + String.format("%.2f", stats.getAverageSalary() * stats.getEmployeeCount()));

            document.add(summaryViz);
        }
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
}

