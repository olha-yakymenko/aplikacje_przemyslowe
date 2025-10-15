package src.service;

import src.exception.InvalidDataException;
import src.model.Employee;
import src.model.ImportSummary;
import src.model.Position;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ImportService {
    private final EmployeeService employeeService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportService() {
        this.employeeService = new EmployeeService();
    }

    /**
     * Importuje pracowników z pliku CSV używając OpenCSV
     */
    public ImportSummary importFromCsv(String filePath) {
        ImportSummary summary = new ImportSummary();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> allRows = reader.readAll();

            // Pomijamy pierwszy wiersz (nagłówki)
            for (int i = 1; i < allRows.size(); i++) {
                String[] fields = allRows.get(i);

                if (fields.length == 0 || (fields.length == 1 && fields[0].trim().isEmpty())) {
                    continue; // Pomijanie pustych linii
                }

                try {
                    Employee employee = parseEmployeeFromCsv(fields, i + 1);
                    employeeService.addEmployee(employee);
                    summary.incrementImported();
                } catch (InvalidDataException e) {
                    // Specyficzna obsługa błędów danych
                    summary.addError("Line " + (i + 1) + ": " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    // Specyficzna obsługa błędów walidacji (np. duplikat email)
                    summary.addError("Line " + (i + 1) + ": " + e.getMessage());
                } catch (Exception e) {
                    // Ogólna obsługa innych nieoczekiwanych błędów
                    summary.addError("Line " + (i + 1) + ": Unexpected error - " + e.getMessage());
                }
            }

        } catch (FileNotFoundException e) {
            // Plik nie istnieje
            summary.addError("File not found: " + filePath);
        } catch (IOException e) {
            // Błędy odczytu pliku
            summary.addError("File read error: " + e.getMessage());
        } catch (CsvException e) {
            // Błędy parsowania CSV
            summary.addError("CSV parsing error: " + e.getMessage());
        }

        return summary;
    }

    private Employee parseEmployeeFromCsv(String[] fields, int lineNumber) throws InvalidDataException {
        if (fields.length != 6) {
            throw new InvalidDataException("Invalid number of fields. Expected 6, got " + fields.length);
        }

        String firstName = fields[0].trim();
        String lastName = fields[1].trim();
        String email = fields[2].trim();
        String company = fields[3].trim();
        String positionStr = fields[4].trim();
        String salaryStr = fields[5].trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || company.isEmpty()) {
            throw new InvalidDataException("Required fields cannot be empty");
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

        // Tworzenie pełnego imienia i nazwiska
        String fullName = firstName + " " + lastName;

        return new Employee(fullName, email, company, position, salary);
    }

    public EmployeeService getEmployeeService() {
        return employeeService;
    }
}