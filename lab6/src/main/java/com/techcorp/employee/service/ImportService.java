


package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ImportService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int EXPECTED_CSV_FIELDS = 6; // 6 pól: firstName,lastName,email,company,position,salary

    private final EmployeeService employeeService;

    @Autowired
    private FileStorageService fileStorageService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ===== METODY DO OBSŁUGI MULTIPART FILE =====

    public ImportSummary importCsvFile(MultipartFile file) {
        ImportSummary summary = new ImportSummary();

        try {
            // Walidacja pliku
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".csv"});
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024); // 10MB

            // Bezpośrednie przetworzenie z MultipartFile - BEZ ZAPISU NA DYSK
            List<String[]> allRows = readCsvFromMultipartFile(file);
            processCsvRows(allRows, summary);

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("CSV import failed: " + e.getMessage());
        }

        return summary;
    }

    public ImportSummary importXmlFile(MultipartFile file) {
        ImportSummary summary = new ImportSummary();

        try {
            // Walidacja pliku
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".xml"});
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024); // 10MB

            // Bezpośrednie przetworzenie z MultipartFile - BEZ ZAPISU NA DYSK
            Document document = parseXmlFromMultipartFile(file);
            processXmlEmployees(document, summary);

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("XML import failed: " + e.getMessage());
        }

        return summary;
    }

    // ===== PRYWATNE METODY PRZETWARZANIA CSV =====

    private List<String[]> readCsvFromMultipartFile(MultipartFile file) throws IOException, CsvException {
        try (InputStream inputStream = file.getInputStream();
             CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            return reader.readAll();
        }
    }

    private void processCsvRows(List<String[]> allRows, ImportSummary summary) {
        if (allRows.isEmpty()) {
            summary.addError("File is empty");
            return;
        }

        // Sprawdź czy pierwszy wiersz to nagłówek
        boolean hasHeader = hasHeaderRow(allRows.get(0));
        int startIndex = hasHeader ? 1 : 0;

        if (startIndex >= allRows.size()) {
            summary.addError("No data rows found after header");
            return;
        }

        for (int i = startIndex; i < allRows.size(); i++) {
            String[] fields = allRows.get(i);

            if (isEmptyLine(fields)) {
                continue;
            }

            processSingleCsvRecord(fields, i + 1, summary);
        }
    }

    private void processSingleCsvRecord(String[] fields, int lineNumber, ImportSummary summary) {
        try {
            Employee employee = parseEmployeeFromCsv(fields, lineNumber);
            employeeService.addEmployee(employee);
            summary.incrementImported();
        } catch (InvalidDataException e) {
            summary.addError("Line " + lineNumber + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            summary.addError("Line " + lineNumber + ": " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Line " + lineNumber + ": Unexpected error - " + e.getMessage());
        }
    }

    public Employee parseEmployeeFromCsv(String[] fields, int lineNumber) throws InvalidDataException {
        validateFieldCount(fields, lineNumber);

        // Pobieranie i trimowanie pól - 6 pól: firstName,lastName,email,company,position,salary
        String firstName = getSafeField(fields, 0);   // Pole 0: First Name
        String lastName = getSafeField(fields, 1);    // Pole 1: Last Name
        String email = getSafeField(fields, 2);       // Pole 2: Email
        String company = getSafeField(fields, 3);     // Pole 3: Company
        String positionStr = getSafeField(fields, 4); // Pole 4: Position
        String salaryStr = getSafeField(fields, 5);   // Pole 5: Salary

        validateRequiredFields(firstName, lastName, email, company, lineNumber);
        validateEmailFormat(email, lineNumber);

        Position position = parsePosition(positionStr, lineNumber);
        double salary = parseSalary(salaryStr, lineNumber);

        // Tworzymy pełne imię i nazwisko
        String fullName = firstName + " " + lastName;

        return new Employee(fullName, email, company, position, salary);
    }

    // ===== PRYWATNE METODY PRZETWARZANIA XML =====

    private Document parseXmlFromMultipartFile(MultipartFile file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Zabezpieczenie przed atakami XXE
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        try (InputStream inputStream = file.getInputStream()) {
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();
            return document;
        }
    }

    private void processXmlEmployees(Document document, ImportSummary summary) {
        Element root = document.getDocumentElement();
        String rootName = root.getNodeName();

        if (!isValidRootElement(rootName)) {
            summary.addError("Invalid XML structure: expected root element 'employees' or 'employeesList', found: " + rootName);
            return;
        }

        NodeList employeeNodes = root.getElementsByTagName("employee");

        if (employeeNodes.getLength() == 0) {
            summary.addError("No employee records found in XML file");
            return;
        }

        for (int i = 0; i < employeeNodes.getLength(); i++) {
            Node employeeNode = employeeNodes.item(i);

            if (employeeNode.getNodeType() == Node.ELEMENT_NODE) {
                processSingleXmlEmployee((Element) employeeNode, i + 1, summary);
            }
        }
    }

    private void processSingleXmlEmployee(Element employeeElement, int elementNumber, ImportSummary summary) {
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

    private Employee parseEmployeeFromXml(Element employeeElement, int elementNumber) throws InvalidDataException {
        String firstName = getElementTextContent(employeeElement, "firstName", elementNumber);
        String lastName = getElementTextContent(employeeElement, "lastName", elementNumber);
        String email = getElementTextContent(employeeElement, "email", elementNumber);
        String company = getElementTextContent(employeeElement, "company", elementNumber);
        String positionStr = getElementTextContent(employeeElement, "position", elementNumber);
        String salaryStr = getElementTextContent(employeeElement, "salary", elementNumber);

        validateRequiredFields(firstName, lastName, email, company, elementNumber);
        validateEmailFormat(email, elementNumber);

        Position position = parsePosition(positionStr, elementNumber);
        double salary = parseSalary(salaryStr, elementNumber);

        // Tworzymy pełne imię i nazwisko
        String fullName = firstName + " " + lastName;

        return new Employee(fullName, email, company, position, salary);
    }

    // ===== METODY WALIDACYJNE =====

    private void validateFieldCount(String[] fields, int lineNumber) throws InvalidDataException {
        if (fields.length != EXPECTED_CSV_FIELDS) {
            throw new InvalidDataException("Invalid number of fields. Expected " + EXPECTED_CSV_FIELDS +
                    " (firstName,lastName,email,company,position,salary), got " + fields.length);
        }
    }

    private String getSafeField(String[] fields, int index) {
        return (fields.length > index && fields[index] != null) ? fields[index].trim() : "";
    }

    private void validateRequiredFields(String firstName, String lastName, String email, String company, int lineNumber) throws InvalidDataException {
        if (firstName == null || firstName.isEmpty()) {
            throw new InvalidDataException("First name cannot be empty");
        }
        if (lastName == null || lastName.isEmpty()) {
            throw new InvalidDataException("Last name cannot be empty");
        }
        if (email == null || email.isEmpty()) {
            throw new InvalidDataException("Email cannot be empty");
        }
        if (company == null || company.isEmpty()) {
            throw new InvalidDataException("Company cannot be empty");
        }
    }

    private void validateEmailFormat(String email, int lineNumber) throws InvalidDataException {
        if (!isValidEmail(email)) {
            throw new InvalidDataException("Invalid email format: " + email);
        }
    }

    private Position parsePosition(String positionStr, int lineNumber) throws InvalidDataException {
        if (positionStr == null || positionStr.trim().isEmpty()) {
            throw new InvalidDataException("Position cannot be empty");
        }

        try {
            return Position.valueOf(positionStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid position: '" + positionStr + "'. Valid values: " + Arrays.toString(Position.values()));
        }
    }

    private double parseSalary(String salaryStr, int lineNumber) throws InvalidDataException {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            throw new InvalidDataException("Salary cannot be empty");
        }

        try {
            double salary = Double.parseDouble(salaryStr.trim());
            if (salary < 0) {
                throw new InvalidDataException("Salary cannot be negative: " + salary);
            }
            if (salary > 1_000_000) {
                throw new InvalidDataException("Salary seems unrealistic: " + salary);
            }
            return salary;
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid salary format: '" + salaryStr + "'. Must be a number.");
        }
    }

    private String getElementTextContent(Element parentElement, String tagName, int elementNumber) throws InvalidDataException {
        NodeList nodes = parentElement.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            throw new InvalidDataException("Missing required element: " + tagName);
        }

        Element element = (Element) nodes.item(0);
        String textContent = element.getTextContent();

        return textContent != null ? textContent.trim() : "";
    }

    // ===== METODY POMOCNICZE =====

    private boolean hasHeaderRow(String[] firstRow) {
        if (firstRow.length < 3) return false;
        // Sprawdź czy pierwszy wiersz wygląda jak nagłówek
        String firstField = firstRow[0] != null ? firstRow[0].toLowerCase() : "";
        return firstField.contains("first") || firstField.contains("name") || firstField.contains("email");
    }

    private boolean isValidRootElement(String rootName) {
        return "employees".equalsIgnoreCase(rootName) || "employeesList".equalsIgnoreCase(rootName);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
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

    public ImportSummary importFromCsv(String filePath) {
        ImportSummary summary = new ImportSummary();
        summary.addError("This method is deprecated. Use importCsvFile(MultipartFile) instead.");
        return summary;
    }


    public ImportSummary importFromXml(String filePath) {
        ImportSummary summary = new ImportSummary();
        summary.addError("This method is deprecated. Use importXmlFile(MultipartFile) instead.");
        return summary;
    }
}