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
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
public class ReportGeneratorService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private FileStorageService fileStorageService;

    public Path generateCsvReport(@NotBlank(message = "Company name cannot be blank") String company) throws IOException {
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

    public Path generateStatisticsPdf(
            @NotBlank(message = "Company name cannot be blank") String companyName) {
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
                    .setFontColor(new DeviceRgb(44, 62, 80))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(header);

            // === NAZWA FIRMY ===
            Paragraph companyHeader = new Paragraph(companyName)
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(new DeviceRgb(52, 73, 94))
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

            // Użyj BigDecimal
            BigDecimal avgSalary = stats.getAverageSalary();
            addTableRow(statisticsTable, "Average Salary",
                    String.format("$%s", avgSalary.setScale(2, RoundingMode.HALF_UP).toPlainString()));

            addTableRow(statisticsTable, "Highest Paid Employee",
                    stats.getHighestPaidEmployee());

            // Oblicz całkowity budżet z BigDecimal
            BigDecimal totalBudget = avgSalary.multiply(BigDecimal.valueOf(stats.getEmployeeCount()));
            addTableRow(statisticsTable, "Total Salary Budget",
                    String.format("$%s", totalBudget.setScale(2, RoundingMode.HALF_UP).toPlainString()));

            document.add(statisticsTable);

            // === WIZUALIZACJA DANYCH - PROSTE I CZYTELNE WYKRESY ===
            if (stats.getEmployeeCount() > 0) {
                addSimpleVisualization(document, stats);
            }

            // === PODSUMOWANIE ===
            Paragraph summary = new Paragraph("Summary")
                    .setBold()
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(summary);

            String summaryText = String.format(
                    "The company %s currently employs %d employee(s) with an average salary of $%s. " +
                            "The highest paid employee is %s.",
                    companyName,
                    stats.getEmployeeCount(),
                    avgSalary.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    stats.getHighestPaidEmployee()
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

    private void addSimpleVisualization(Document document, CompanyStatistics stats) {
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

            // === PROSTE WYKRESY SŁUPKOWE ===
            Paragraph chartsHeader = new Paragraph("Key Metrics Comparison")
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(new DeviceRgb(52, 73, 94))
                    .setMarginBottom(15);
            document.add(chartsHeader);

            // Tabela z wykresami
            float[] chartWidths = {3, 5, 2};
            Table chartTable = new Table(UnitValue.createPercentArray(chartWidths));
            chartTable.setWidth(UnitValue.createPercentValue(90));
            chartTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            chartTable.setMarginBottom(25);

            // Nagłówki
            chartTable.addHeaderCell(createHeaderCell("Metric"));
            chartTable.addHeaderCell(createHeaderCell("Chart"));
            chartTable.addHeaderCell(createHeaderCell("Value"));

            // Kolory dla wykresów
            DeviceRgb[] colors = {
                    new DeviceRgb(65, 105, 225),  // Royal Blue
                    new DeviceRgb(34, 139, 34),   // Forest Green
                    new DeviceRgb(178, 34, 34)    // Firebrick Red
            };

            // Wiersz 1: Średnia pensja (BigDecimal → double dla wykresu)
            BigDecimal avgSalary = stats.getAverageSalary();
            addChartRow(chartTable, "Average Salary",
                    avgSalary.doubleValue(), 20000, colors[0], "$");

            // Wiersz 2: Liczba pracowników
            addChartRow(chartTable, "Employee Count",
                    stats.getEmployeeCount(), 20, colors[1], "");

            // Wiersz 3: Całkowity budżet (BigDecimal obliczenia)
            BigDecimal totalBudget = avgSalary.multiply(
                    BigDecimal.valueOf(stats.getEmployeeCount()));
            addChartRow(chartTable, "Total Budget",
                    totalBudget.doubleValue(), 400000, colors[2], "$");

            document.add(chartTable);

            // === PROSTA LEGENDA ===
            addSimpleLegend(document, colors);

            // === DODATKOWE INFORMACJE ===
            addAdditionalInfo(document, stats);

        } catch (Exception e) {
            System.err.println("Error in visualization: " + e.getMessage());
            addFallbackVisualization(document, stats);
        }
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(44, 62, 80))
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void addChartRow(Table table, String label, double value, double maxValue, DeviceRgb color, String prefix) {
        // Komórka z etykietą
        table.addCell(new Cell()
                .add(new Paragraph(label))
                .setPadding(8)
                .setTextAlignment(TextAlignment.LEFT));

        // Komórka z wykresem
        Cell chartCell = new Cell();
        chartCell.setPadding(5);
        chartCell.setHeight(30f);

        // Oblicz procent wypełnienia
        double ratio = Math.min(value / maxValue, 1.0);
        float barWidth = (float) (ratio * 100);

        // Kontener dla całego wykresu
        Div chartContainer = new Div();
        chartContainer.setWidth(UnitValue.createPercentValue(100));
        chartContainer.setHeight(25f);

        // Tło wykresu
        Div background = new Div();
        background.setWidth(UnitValue.createPercentValue(100));
        background.setHeight(20f);
        background.setBackgroundColor(new DeviceRgb(240, 240, 240));
        background.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1f));
        background.setMarginTop(2f);

        // Pasek postępu
        Div progressBar = new Div();
        progressBar.setBackgroundColor(color)
                .setHeight(18f)
                .setWidth(UnitValue.createPercentValue(barWidth))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setMarginTop(1f)
                .setMarginLeft(1f);

        // Tekst wartości na pasku (jeśli miejsce)
        if (barWidth > 25) {
            String displayValue;
            if (prefix.equals("$")) {
                displayValue = String.format("$%.2f", value);
            } else {
                displayValue = String.format("%.0f", value);
            }
            Paragraph valueText = new Paragraph(displayValue)
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(4f);
            progressBar.add(valueText);
        }

        background.add(progressBar);
        chartContainer.add(background);
        chartCell.add(chartContainer);
        table.addCell(chartCell);

        // Komórka z wartością
        String valueText;
        if (prefix.equals("$")) {
            valueText = String.format("$%.2f", value);
        } else {
            valueText = String.format("%.0f", value);
        }
        table.addCell(new Cell()
                .add(new Paragraph(valueText).setBold())
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private String formatValue(double value) {
        if (value >= 1000) {
            return String.format("%.1fk", value / 1000);
        }
        return String.format("%.0f", value);
    }

    private void addSimpleLegend(Document document, DeviceRgb[] colors) {
        Paragraph legendHeader = new Paragraph("Legend")
                .setBold()
                .setFontSize(12)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(legendHeader);

        // Prosta legenda w jednym wierszu
        Paragraph legend = new Paragraph();
        legend.setTextAlignment(TextAlignment.CENTER);
        legend.setMarginBottom(15);

        String[] labels = {"Average Salary", "Employee Count", "Total Budget"};

        for (int i = 0; i < colors.length; i++) {
            if (i > 0) {
                legend.add(new Text("   ")); // Odstęp między elementami
            }
            legend.add(new Text("■ ").setFontColor(colors[i]).setFontSize(10));
            legend.add(new Text(labels[i]).setFontSize(9));
        }

        document.add(legend);

        // Informacja o skali
        Paragraph scaleInfo = new Paragraph("Scales: Salary $0-20k | Employees 0-20 | Budget $0-400k")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setMarginTop(5);
        document.add(scaleInfo);
    }

    private void addAdditionalInfo(Document document, CompanyStatistics stats) {
        if (stats.getEmployeeCount() > 1) {
            Paragraph additionalInfo = new Paragraph("Additional Insights")
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(additionalInfo);

            // Proste insights z użyciem BigDecimal
            BigDecimal avgSalary = stats.getAverageSalary();
            BigDecimal totalMonthly = avgSalary.multiply(
                    BigDecimal.valueOf(stats.getEmployeeCount()));
            BigDecimal annualEstimate = totalMonthly.multiply(BigDecimal.valueOf(12));

            List<String> insights = List.of(
                    String.format("• Average salary per employee: $%s",
                            avgSalary.setScale(2, RoundingMode.HALF_UP).toPlainString()),
                    String.format("• Total monthly payroll: $%s",
                            totalMonthly.setScale(2, RoundingMode.HALF_UP).toPlainString()),
                    String.format("• Annual payroll estimate: $%s",
                            annualEstimate.setScale(2, RoundingMode.HALF_UP).toPlainString()),
                    String.format("• Highest paid: %s", stats.getHighestPaidEmployee())
            );

            for (String insight : insights) {
                Paragraph insightPara = new Paragraph(insight)
                        .setFontSize(9)
                        .setMarginBottom(3);
                document.add(insightPara);
            }
        }
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

        BigDecimal avgSalary = stats.getAverageSalary();
        BigDecimal totalBudget = avgSalary.multiply(
                BigDecimal.valueOf(stats.getEmployeeCount()));

        String[][] data = {
                {"Employee Count", String.valueOf(stats.getEmployeeCount())},
                {"Average Salary", String.format("$%s",
                        avgSalary.setScale(2, RoundingMode.HALF_UP).toPlainString())},
                {"Highest Paid", stats.getHighestPaidEmployee()},
                {"Total Budget", String.format("$%s",
                        totalBudget.setScale(2, RoundingMode.HALF_UP).toPlainString())}
        };

        for (String[] row : data) {
            fallbackTable.addCell(new Cell().add(new Paragraph(row[0]).setBold()).setPadding(5));
            fallbackTable.addCell(new Cell().add(new Paragraph(row[1])).setPadding(5));
        }

        document.add(fallbackTable);
    }

    String convertToCsv(Employee employee) {
        // Użyj BigDecimal do formatowania wynagrodzenia
        BigDecimal salary = employee.getSalary();
        return String.format("\"%s\",%s,\"%s\",%s,%s,%s",
                employee.getName(),
                employee.getEmail(),
                employee.getCompany(),
                employee.getPosition(),
                salary.setScale(2, RoundingMode.HALF_UP).toPlainString(), // Format BigDecimal
                employee.getStatus());
    }

    public Map<String, Object> getCsvReportData(String company) {
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

            // Zwróć Mapę z danymi potrzebnymi do utworzenia ResponseEntity
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("resource", resource);
            reportData.put("fileName", csvPath.getFileName().toString());
            reportData.put("contentType", "text/csv");
            reportData.put("contentDisposition", "attachment");

            return reportData;

        } catch (Exception e) {
            throw new FileStorageException("Could not generate CSV report: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getStatisticsReportData(
            @NotBlank(message = "Company name cannot be blank") String companyName) {
        try {
            Path pdfPath = generateStatisticsPdf(companyName);

            org.springframework.core.io.Resource resource = new UrlResource(pdfPath.toUri());
            if (!resource.exists()) {
                throw new java.io.FileNotFoundException("PDF file not found at: " + pdfPath.toAbsolutePath());
            }

            // Zwróć Mapę z danymi potrzebnymi do utworzenia ResponseEntity
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("resource", resource);
            reportData.put("fileName", pdfPath.getFileName().toString());
            reportData.put("contentType", MediaType.APPLICATION_PDF_VALUE);
            reportData.put("contentDisposition", "inline");

            return reportData;

        } catch (Exception e) {
            throw new FileStorageException("Could not generate statistics report", e);
        }
    }
}