package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
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
        // POPRAWKA: Zmień z 5 na 6 pól (bo jest nagłówek)
        if (fields.length != 6) {
            throw new InvalidDataException("Invalid number of fields. Expected 6, got " + fields.length);
        }

        // Bezpieczne pobieranie i trimowanie pól (pomiń pierwsze pole - to jest nagłówek)
        String name = fields[1] != null ? fields[1].trim() : "";        // Pole 1: Name
        String email = fields[2] != null ? fields[2].trim() : "";       // Pole 2: Email
        String company = fields[3] != null ? fields[3].trim() : "";     // Pole 3: Company
        String positionStr = fields[4] != null ? fields[4].trim() : ""; // Pole 4: Position
        String salaryStr = fields[5] != null ? fields[5].trim() : "";   // Pole 5: Salary

        // Reszta kodu bez zmian...
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




    // DODAJ TE METODY DO ISTNIEJĄCEJ KLASY ImportService:

    public ImportSummary importFromXml(String filePath) {
        ImportSummary summary = new ImportSummary();

        // Walidacja ścieżki pliku
        if (filePath == null || filePath.trim().isEmpty()) {
            summary.addError("XML file path cannot be null or empty");
            return summary;
        }

        File xmlFile = new File(filePath);
        if (!xmlFile.exists()) {
            summary.addError("XML file not found: " + filePath);
            return summary;
        }

        try {
            // Konfiguracja parsera XML z zabezpieczeniami
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Zabezpieczenie przed atakami XXE
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            // Sprawdź główny element
            Element root = document.getDocumentElement();
            String rootName = root.getNodeName();

            if (!"employees".equalsIgnoreCase(rootName) && !"employeesList".equalsIgnoreCase(rootName)) {
                summary.addError("Invalid XML structure: expected root element 'employees' or 'employeesList', found: " + rootName);
                return summary;
            }

            // Pobierz wszystkie elementy employee
            NodeList employeeNodes = root.getElementsByTagName("employee");

            if (employeeNodes.getLength() == 0) {
                summary.addError("No employee records found in XML file");
                return summary;
            }

            // Przetwarzaj każdy element employee z numerami
            for (int i = 0; i < employeeNodes.getLength(); i++) {
                Node employeeNode = employeeNodes.item(i);
                int elementNumber = i + 1;

                if (employeeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element employeeElement = (Element) employeeNode;

                    try {
                        Employee employee = parseEmployeeFromXml(employeeElement, elementNumber);
                        employeeService.addEmployee(employee);
                        summary.incrementImported();
                    } catch (InvalidDataException e) {
                        summary.addError("Employee element " + elementNumber + ": " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        summary.addError("Employee element " + elementNumber + ": " + e.getMessage());
                    } catch (Exception e) {
                        summary.addError("Employee element " + elementNumber + ": Unexpected error - " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            summary.addError("File read error: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Unexpected error during XML import: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Parsuje pojedynczy element employee z XML
     */
    private Employee parseEmployeeFromXml(Element employeeElement, int elementNumber) throws InvalidDataException {
        // Pobierz wartości z elementów XML - z obsługą brakujących elementów
        String name = getElementTextContent(employeeElement, "name", elementNumber);
        String email = getElementTextContent(employeeElement, "email", elementNumber);
        String company = getElementTextContent(employeeElement, "company", elementNumber);
        String positionStr = getElementTextContent(employeeElement, "position", elementNumber);
        String salaryStr = getElementTextContent(employeeElement, "salary", elementNumber);

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
            throw new InvalidDataException("Invalid position: " + positionStr + ". Valid values: " +
                    Arrays.toString(Position.values()));
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

    /**
     * Bezpiecznie pobiera tekst z elementu XML
     */
    private String getElementTextContent(Element parentElement, String tagName, int elementNumber) throws InvalidDataException {
        NodeList nodes = parentElement.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            throw new InvalidDataException("Missing required element: " + tagName);
        }

        Element element = (Element) nodes.item(0);
        String textContent = element.getTextContent();

        return textContent != null ? textContent.trim() : "";
    }
}