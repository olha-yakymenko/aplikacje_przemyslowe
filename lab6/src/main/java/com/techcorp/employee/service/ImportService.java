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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
@Service
public class ImportService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int EXPECTED_CSV_FIELDS = 5; // 5 pól: name,email,company,position,salary

    private final EmployeeService employeeService;

    @Autowired
    private FileStorageService fileStorageService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportSummary importFromCsv(String filePath) {
        ImportSummary summary = new ImportSummary();

        if (!isValidFilePath(filePath)) {
            summary.addError("File path cannot be null or empty");
            return summary;
        }

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> allRows = reader.readAll();
            processCsvRows(allRows, summary);
        } catch (IOException e) {
            summary.addError("File read error: " + e.getMessage());
        } catch (CsvException e) {
            summary.addError("CSV parsing error: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Unexpected error: " + e.getMessage());
        }

        return summary;
    }

    public ImportSummary importFromXml(String filePath) {
        ImportSummary summary = new ImportSummary();

        if (!isValidFilePath(filePath)) {
            summary.addError("XML file path cannot be null or empty");
            return summary;
        }

        File xmlFile = new File(filePath);
        if (!xmlFile.exists()) {
            summary.addError("XML file not found: " + filePath);
            return summary;
        }

        try {
            Document document = parseXmlDocument(xmlFile);
            processXmlEmployees(document, summary);
        } catch (IOException e) {
            summary.addError("File read error: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Unexpected error during XML import: " + e.getMessage());
        }

        return summary;
    }

    public Employee parseEmployeeFromCsv(String[] fields, int lineNumber) throws InvalidDataException {
        validateFieldCount(fields, lineNumber);

        // Pobieranie i trimowanie pól - 5 pól bez dodatkowego nagłówka
        String name = getSafeField(fields, 0);    // Pole 0: Name
        String email = getSafeField(fields, 1);   // Pole 1: Email
        String company = getSafeField(fields, 2); // Pole 2: Company
        String positionStr = getSafeField(fields, 3); // Pole 3: Position
        String salaryStr = getSafeField(fields, 4);   // Pole 4: Salary

        validateRequiredFields(name, email, company, lineNumber);
        validateEmailFormat(email, lineNumber);

        Position position = parsePosition(positionStr, lineNumber);
        double salary = parseSalary(salaryStr, lineNumber);

        return new Employee(name, email, company, position, salary);
    }

    // ===== PRYWATNE METODY POMOCNICZE =====

    private boolean isValidFilePath(String filePath) {
        return filePath != null && !filePath.trim().isEmpty();
    }

    private void processCsvRows(List<String[]> allRows, ImportSummary summary) {
        if (allRows.isEmpty()) {
            return; // Pusty plik
        }

        // Sprawdź czy pierwszy wiersz to nagłówek
        boolean hasHeader = hasHeaderRow(allRows.get(0));
        int startIndex = hasHeader ? 1 : 0;

        for (int i = startIndex; i < allRows.size(); i++) {
            String[] fields = allRows.get(i);

            if (isEmptyLine(fields)) {
                continue;
            }

            processSingleCsvRecord(fields, i + 1, summary);
        }
    }

    private boolean hasHeaderRow(String[] firstRow) {
        if (firstRow.length < 3) return false;
        // Sprawdź czy pierwszy wiersz wygląda jak nagłówek
        String firstField = firstRow[0] != null ? firstRow[0].toLowerCase() : "";
        return firstField.contains("name") || firstField.contains("email") || firstField.contains("company");
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

    private Document parseXmlDocument(File xmlFile) throws Exception {
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

        return document;
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
        String name = getElementTextContent(employeeElement, "name", elementNumber);
        String email = getElementTextContent(employeeElement, "email", elementNumber);
        String company = getElementTextContent(employeeElement, "company", elementNumber);
        String positionStr = getElementTextContent(employeeElement, "position", elementNumber);
        String salaryStr = getElementTextContent(employeeElement, "salary", elementNumber);

        validateRequiredFields(name, email, company, elementNumber);
        validateEmailFormat(email, elementNumber);

        Position position = parsePosition(positionStr, elementNumber);
        double salary = parseSalary(salaryStr, elementNumber);

        return new Employee(name, email, company, position, salary);
    }

    private void validateFieldCount(String[] fields, int lineNumber) throws InvalidDataException {
        if (fields.length != EXPECTED_CSV_FIELDS) {
            throw new InvalidDataException("Invalid number of fields. Expected " + EXPECTED_CSV_FIELDS + ", got " + fields.length);
        }
    }

    private String getSafeField(String[] fields, int index) {
        return (fields.length > index && fields[index] != null) ? fields[index].trim() : "";
    }

    private void validateRequiredFields(String name, String email, String company, int lineNumber) throws InvalidDataException {
        if (name.isEmpty()) {
            throw new InvalidDataException("Name cannot be empty");
        }
        if (email.isEmpty()) {
            throw new InvalidDataException("Email cannot be empty");
        }
        if (company.isEmpty()) {
            throw new InvalidDataException("Company cannot be empty");
        }
    }

    private void validateEmailFormat(String email, int lineNumber) throws InvalidDataException {
        if (!isValidEmail(email)) {
            throw new InvalidDataException("Invalid email format: " + email);
        }
    }

    private Position parsePosition(String positionStr, int lineNumber) throws InvalidDataException {
        try {
            return Position.valueOf(positionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid position: " + positionStr + ". Valid values: " + Arrays.toString(Position.values()));
        }
    }

    private double parseSalary(String salaryStr, int lineNumber) throws InvalidDataException {
        try {
            double salary = Double.parseDouble(salaryStr);
            if (salary < 0) {
                throw new InvalidDataException("Salary cannot be negative: " + salary);
            }
            return salary;
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid salary format: " + salaryStr);
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

    private boolean isValidRootElement(String rootName) {
        return "employees".equalsIgnoreCase(rootName) || "employeesList".equalsIgnoreCase(rootName);
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


    // Dodaj te metody do istniejącego ImportService.java

    public ImportSummary importCsvFile(MultipartFile file) {
        ImportSummary summary = new ImportSummary();

        try {
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".csv"});
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024);

            String fileName = fileStorageService.storeFile(file, "uploads");
            Path uploadsDir = fileStorageService.getFileStorageLocation().resolve("uploads");
            String filePath = uploadsDir.resolve(fileName).toString();

            summary = importFromCsv(filePath);

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Import failed: " + e.getMessage());
        }

        return summary;
    }

    public ImportSummary importXmlFile(MultipartFile file) {
        ImportSummary summary = new ImportSummary();

        try {
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".xml"});
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024);

            String fileName = fileStorageService.storeFile(file, "uploads");
            Path uploadsDir = fileStorageService.getFileStorageLocation().resolve("uploads");
            String filePath = uploadsDir.resolve(fileName).toString();

            summary = importFromXml(filePath);

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
        } catch (Exception e) {
            summary.addError("XML import failed: " + e.getMessage());
        }

        return summary;
    }
}