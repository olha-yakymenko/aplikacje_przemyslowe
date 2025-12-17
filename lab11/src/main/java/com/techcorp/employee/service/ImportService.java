


package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Validated
public class ImportService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int EXPECTED_CSV_FIELDS = 6; // 6 pól: firstName,lastName,email,company,position,salary

    private final EmployeeService employeeService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private Validator validator;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ===== METODY DO OBSŁUGI MULTIPART FILE =====

    public ImportSummary importCsvFile(
            @NotNull MultipartFile file) {        ImportSummary summary = new ImportSummary();
        String savedFileName = null;

        try {
            // Walidacja pliku
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".csv"});
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024); // 10MB

            List<String[]> allRows = readCsvFromMultipartFile(file);
            processCsvRows(allRows, summary);

            if (summary.getImportedCount() > 0) {
                savedFileName = fileStorageService.storeFile(file, "uploads");
            }

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
            if (savedFileName != null) {
                fileStorageService.deleteFile(savedFileName, "uploads");
            }
        } catch (Exception e) {
            summary.addError("CSV import failed: " + e.getMessage());
            if (savedFileName != null) {
                fileStorageService.deleteFile(savedFileName, "uploads");
            }
        }

        return summary;
    }
    public ImportSummary importXmlFile(
            @NotNull MultipartFile file) {        ImportSummary summary = new ImportSummary();
        String savedFileName = null;

        try {
            // Walidacja pliku
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".xml"});
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024); // 10MB

            Document document = parseXmlFromMultipartFile(file);
            processXmlEmployees(document, summary);

            if (summary.getImportedCount() > 0) {
                savedFileName = fileStorageService.storeFile(file, "uploads");
            }

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
            // Usuń plik jeśli został zapisany (choć nie powinien być)
            if (savedFileName != null) {
                fileStorageService.deleteFile(savedFileName, "uploads");
            }
        } catch (Exception e) {
            summary.addError("XML import failed: " + e.getMessage());
            // Usuń plik jeśli został zapisany (choć nie powinien być)
            if (savedFileName != null) {
                fileStorageService.deleteFile(savedFileName, "uploads");
            }
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

//    private void processSingleCsvRecord(String[] fields, int lineNumber, ImportSummary summary) {
//        try {
//            Employee employee = parseEmployeeFromCsv(fields, lineNumber);
//            employeeService.addEmployee(employee);
//            summary.incrementImported();
//        } catch (InvalidDataException e) {
//            summary.addError("Line " + lineNumber + ": " + e.getMessage());
//        } catch (IllegalArgumentException e) {
//            summary.addError("Line " + lineNumber + ": " + e.getMessage());
//        } catch (Exception e) {
//            summary.addError("Line " + lineNumber + ": Unexpected error - " + e.getMessage());
//        }
//    }

    private void processSingleCsvRecord(String[] fields, int lineNumber, ImportSummary summary) {
        try {
            Employee employee = parseEmployeeFromCsv(fields, lineNumber);

            // Walidacja Bean Validation
            validateEmployee(employee);

            // Zapisz przez serwis (który również ma walidację)
            employeeService.addEmployee(employee);
            summary.incrementImported();

        } catch (InvalidDataException e) {
            summary.addError("Line " + lineNumber + ": " + e.getMessage());
        } catch (ConstraintViolationException e) {
            // Błędy walidacji Bean Validation
            String validationErrors = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            summary.addError("Line " + lineNumber + ": Validation error - " + validationErrors);
        } catch (IllegalArgumentException e) {
            summary.addError("Line " + lineNumber + ": " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Line " + lineNumber + ": Unexpected error - " + e.getMessage());
        }
    }

//    public Employee parseEmployeeFromCsv(String[] fields, int lineNumber) throws InvalidDataException {
Employee parseEmployeeFromCsv(
        @NotNull String[] fields,
        @Min(1) int lineNumber) throws InvalidDataException {
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
        BigDecimal salary = parseSalary(salaryStr, lineNumber);

        // Tworzymy pełne imię i nazwisko
        String fullName = firstName + " " + lastName;

        return new Employee(fullName, email, company, position, salary);
    }


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

//    private void processSingleXmlEmployee(Element employeeElement, int elementNumber, ImportSummary summary) {
//        try {
//            Employee employee = parseEmployeeFromXml(employeeElement, elementNumber);
//            employeeService.addEmployee(employee);
//            summary.incrementImported();
//        } catch (InvalidDataException e) {
//            summary.addError("Employee element " + elementNumber + ": " + e.getMessage());
//        } catch (IllegalArgumentException e) {
//            summary.addError("Employee element " + elementNumber + ": " + e.getMessage());
//        } catch (Exception e) {
//            summary.addError("Employee element " + elementNumber + ": Unexpected error - " + e.getMessage());
//        }
//    }

    private void processSingleXmlEmployee(Element employeeElement, int elementNumber, ImportSummary summary) {
        try {
            Employee employee = parseEmployeeFromXml(employeeElement, elementNumber);

            // Dodaj jawną walidację przed zapisem
            validateEmployee(employee);

            employeeService.addEmployee(employee);
            summary.incrementImported();
        } catch (InvalidDataException e) {
            summary.addError("Employee element " + elementNumber + ": " + e.getMessage());
        } catch (ConstraintViolationException e) {
            // Przechwyć błędy walidacji Bean Validation
            String validationErrors = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            summary.addError("Employee element " + elementNumber + ": Validation error - " + validationErrors);
        } catch (IllegalArgumentException e) {
            summary.addError("Employee element " + elementNumber + ": " + e.getMessage());
        } catch (Exception e) {
            summary.addError("Employee element " + elementNumber + ": Unexpected error - " + e.getMessage());
        }
    }


    private void validateEmployee(Employee employee) {
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
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
        BigDecimal salary = parseSalary(salaryStr, elementNumber);

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

    private BigDecimal parseSalary(String salaryStr, int lineNumber) throws InvalidDataException {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            throw new InvalidDataException("Wynagrodzenie nie może być puste (linia " + lineNumber + ")");
        }

        String trimmedSalary = salaryStr.trim();

        try {
            BigDecimal salary = new BigDecimal(trimmedSalary);

            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidDataException(
                        "Wynagrodzenie nie może być ujemne: " + salary.toPlainString() + " (linia " + lineNumber + ")"
                );
            }

            BigDecimal MAX_SALARY = new BigDecimal("1000000.00");
            if (salary.compareTo(MAX_SALARY) > 0) {
                throw new InvalidDataException(
                        "Wynagrodzenie wydaje się nierealistyczne: " + salary.toPlainString() +
                                ". Maksymalna wartość: " + MAX_SALARY.toPlainString() + " (linia " + lineNumber + ")"
                );
            }

            return salary.setScale(2, RoundingMode.HALF_UP);

        } catch (NumberFormatException e) {
            throw new InvalidDataException(
                    "Nieprawidłowy format wynagrodzenia: '" + trimmedSalary +
                            "'. Musi być liczbą (linia " + lineNumber + ")"
            );
        } catch (ArithmeticException e) {
            throw new InvalidDataException(
                    "Nieprawidłowa wartość wynagrodzenia: '" + trimmedSalary +
                            "' (linia " + lineNumber + ")"
            );
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