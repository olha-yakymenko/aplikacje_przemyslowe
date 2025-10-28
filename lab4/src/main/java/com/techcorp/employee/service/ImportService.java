package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ImportService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final EmployeeService employeeService;

    @Value("${app.import.csv-file}")
    private String csvFile;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportSummary importFromCsv() throws IOException, CsvException {
        return importFromCsv(csvFile);
    }

    public ImportSummary importFromCsv(String filePath) {
        ImportSummary summary = new ImportSummary();

        // Walidacja ścieżki pliku
        if (filePath == null || filePath.trim().isEmpty()) {
            summary.addError("File path cannot be null or empty");
            return summary;
        }

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> allRows = reader.readAll();

            if (allRows.size() <= 1) {
                return summary; // Pusty plik lub tylko nagłówki
            }

            // Pomijamy pierwszy wiersz (nagłówki)
            for (int i = 1; i < allRows.size(); i++) {
                String[] fields = allRows.get(i);

                // Pomijanie pustych linii
                if (isEmptyLine(fields)) {
                    continue;
                }

                try {
                    Employee employee = parseEmployeeFromCsv(fields, i + 1);
                    employeeService.addEmployee(employee);
                    summary.incrementImported();
                } catch (InvalidDataException e) {
                    summary.addError("Line " + (i + 1) + ": " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    summary.addError("Line " + (i + 1) + ": " + e.getMessage());
                } catch (Exception e) {
                    summary.addError("Line " + (i + 1) + ": Unexpected error - " + e.getMessage());
                }
            }

        } catch (IOException e) {
            summary.addError("File read error: " + e.getMessage());
        } catch (CsvException e) {
            summary.addError("CSV parsing error: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Unexpected error: " + e.getMessage());
        }

        return summary;
    }

    public Employee parseEmployeeFromCsv(String[] fields, int lineNumber) throws InvalidDataException {
        if (fields.length != 5) {
            throw new InvalidDataException("Invalid number of fields. Expected 5, got " + fields.length);
        }

        // Bezpieczne pobieranie i trimowanie pól (ochrona przed null)
        String name = fields[0] != null ? fields[0].trim() : "";
        String email = fields[1] != null ? fields[1].trim() : "";
        String company = fields[2] != null ? fields[2].trim() : "";
        String positionStr = fields[3] != null ? fields[3].trim() : "";
        String salaryStr = fields[4] != null ? fields[4].trim() : "";

        // Walidacja wymaganych pól
        if (name.isEmpty()) {
            throw new InvalidDataException("Name cannot be empty");
        }
        if (email.isEmpty()) {
            throw new InvalidDataException("Email cannot be empty");
        }
        if (company.isEmpty()) {
            throw new InvalidDataException("Company cannot be empty");
        }

        // Walidacja formatu emaila
        if (!isValidEmail(email)) {
            throw new InvalidDataException("Invalid email format: " + email);
        }

        // Parsowanie stanowiska
        Position position;
        try {
            position = Position.valueOf(positionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid position: " + positionStr);
        }

        // Parsowanie wynagrodzenia
        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid salary format: " + salaryStr);
        }

        if (salary < 0) {
            throw new InvalidDataException("Salary cannot be negative: " + salary);
        }

        // Tworzenie pracownika
        return new Employee(name, email, company, position, salary);
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isEmptyLine(String[] fields) {
        if (fields == null || fields.length == 0) {
            return true;
        }
        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}