package service;

import com.techcorp.employee.service.ApiService;
import com.techcorp.employee.exception.ApiException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiService Unit Tests with HTTP Client Mocking")
class ApiServiceTest {

    @Mock
    private HttpClient mockClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private ApiService apiService;
    private final Gson gson = new Gson();

    private final String validJsonResponse = """
        [
            {
                "id": 1,
                "name": "Leanne Graham",
                "email": "sincere@april.biz",
                "company": {
                    "name": "Romaguera-Crona"
                }
            },
            {
                "id": 2,
                "name": "Ervin Howell",
                "email": "shanna@melissa.tv",
                "company": {
                    "name": "Deckow-Crist"
                }
            }
        ]
        """;

    @BeforeEach
    void setUp() {
        apiService = new ApiService(mockClient, gson);
        // Ustaw apiUrl przez refleksję - TO JEST KLUCZOWE!
        ReflectionTestUtils.setField(apiService, "apiUrl", "http://test-api.com/users");
    }

    @Test
    @DisplayName("SCENARIUSZ 1: Should return list of employees when API returns valid JSON response")
    void shouldReturnListOfEmployees_whenApiReturnsValidJsonResponse() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(validJsonResponse);

        // When
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        // Then
        assertAll("Should return correct list of employees with proper mapping",
                () -> assertNotNull(employees, "Employees list should not be null"),
                () -> assertEquals(2, employees.size(), "Should return exactly 2 employees"),
                () -> assertEquals("Leanne Graham", employees.get(0).getName(), "First employee name should match"),
                () -> assertEquals("sincere@april.biz", employees.get(0).getEmail(), "First employee email should match"),
                () -> assertEquals("Romaguera-Crona", employees.get(0).getCompany(), "First employee company should match"),
                () -> assertEquals(Position.PROGRAMMER, employees.get(0).getPosition(), "First employee position should be PROGRAMMER"),
                () -> assertEquals(Position.PROGRAMMER.getBaseSalary(), employees.get(0).getSalary(), "First employee salary should match base salary"),
                () -> assertEquals("Ervin Howell", employees.get(1).getName(), "Second employee name should match"),
                () -> assertEquals("shanna@melissa.tv", employees.get(1).getEmail(), "Second employee email should match"),
                () -> assertEquals("Deckow-Crist", employees.get(1).getCompany(), "Second employee company should match"),
                () -> assertEquals(Position.PROGRAMMER, employees.get(1).getPosition(), "Second employee position should be PROGRAMMER")
        );
    }

    @Test
    @DisplayName("SCENARIUSZ 2: Should throw ApiException when API returns 404 status code")
    void shouldThrowApiException_whenApiReturns404StatusCode() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);
        // USUŃ LINIĘ PONIŻEJ - body nie jest używane gdy status code jest błędem
        // when(mockResponse.body()).thenReturn("Not Found");

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertTrue(exception.getMessage().contains("HTTP error: 404"),
                "Exception message should contain HTTP error code. Actual: " + exception.getMessage());
    }

    @Test
    @DisplayName("SCENARIUSZ 2: Should throw ApiException when API returns 500 status code")
    void shouldThrowApiException_whenApiReturns500StatusCode() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(500);
        // USUŃ LINIĘ PONIŻEJ - body nie jest używane gdy status code jest błędem
        // when(mockResponse.body()).thenReturn("Server Error");

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertTrue(exception.getMessage().contains("HTTP error: 500"),
                "Exception message should contain HTTP error code. Actual: " + exception.getMessage());
    }

    @Test
    @DisplayName("SCENARIUSZ 3: Should correctly map JSON to Employee object")
    void shouldCorrectlyMapJsonToEmployeeObject() throws Exception {
        // Given
        String singleUserJson = """
            [
                {
                    "id": 1,
                    "name": "John Doe",
                    "email": "john.doe@company.com",
                    "company": {
                        "name": "Tech Corp"
                    }
                }
            ]
            """;

        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(singleUserJson);

        // When
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        // Then
        assertAll("Should correctly map all JSON fields to Employee object",
                () -> assertEquals(1, employees.size(), "Should return exactly one employee"),
                () -> assertEquals("John Doe", employees.get(0).getName(), "Name should be correctly mapped"),
                () -> assertEquals("john.doe@company.com", employees.get(0).getEmail(), "Email should be correctly mapped"),
                () -> assertEquals("Tech Corp", employees.get(0).getCompany(), "Company name should be correctly mapped"),
                () -> assertEquals(Position.PROGRAMMER, employees.get(0).getPosition(), "Position should be default PROGRAMMER"),
                () -> assertEquals(Position.PROGRAMMER.getBaseSalary(), employees.get(0).getSalary(), "Salary should match base salary")
        );
    }

    @Test
    @DisplayName("Should throw ApiException with network error details when IOException occurs")
    void shouldThrowApiExceptionWithNetworkError_whenIOExceptionOccurs() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection refused"));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException with proper network error details",
                () -> assertTrue(exception.getMessage().contains("Error while fetching from API"),
                        "Message should indicate network error. Actual: " + exception.getMessage()),
                () -> assertTrue(exception.getMessage().contains("Connection refused"),
                        "Message should contain specific error details. Actual: " + exception.getMessage()),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause"),
                () -> assertInstanceOf(IOException.class, exception.getCause(),
                        "Cause should be IOException. Actual: " + (exception.getCause() != null ? exception.getCause().getClass().getName() : "null"))
        );
    }

    @Test
    @DisplayName("Should throw ApiException when JSON response is invalid")
    void shouldThrowApiException_whenJsonResponseIsInvalid() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("invalid json format");

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException for invalid JSON",
                () -> assertTrue(exception.getMessage().contains("Failed to parse API response"),
                        "Message should indicate parsing failure. Actual: " + exception.getMessage()),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause")
        );
    }

    @Test
    @DisplayName("Should throw ApiException when JSON has missing required fields")
    void shouldThrowApiException_whenJsonHasMissingRequiredFields() throws Exception {
        // Given
        String jsonMissingFields = """
            [
                {
                    "id": 1,
                    "email": "test@test.com"
                }
            ]
            """;

        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonMissingFields);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException for missing required fields",
                () -> assertTrue(exception.getMessage().contains("Failed to parse API response"),
                        "Message should indicate parsing failure. Actual: " + exception.getMessage()),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause")
        );
    }

    @Test
    @DisplayName("Should return empty list when API returns empty JSON array")
    void shouldReturnEmptyList_whenApiReturnsEmptyJsonArray() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");

        // When
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        // Then
        assertTrue(employees.isEmpty(), "Should return empty list for empty JSON array");
    }

    @Test
    @DisplayName("Should throw ApiException when unexpected exception occurs")
    void shouldThrowApiException_whenUnexpectedExceptionOccurs() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Unexpected system error"));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException for unexpected errors",
                () -> assertTrue(exception.getMessage().contains("Error while fetching from API"),
                        "Message should indicate unexpected error. Actual: " + exception.getMessage()),
                () -> assertTrue(exception.getMessage().contains("Unexpected system error"),
                        "Message should contain specific error details. Actual: " + exception.getMessage()),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause"),
                () -> assertInstanceOf(RuntimeException.class, exception.getCause(),
                        "Cause should be RuntimeException. Actual: " + (exception.getCause() != null ? exception.getCause().getClass().getName() : "null"))
        );
    }

    @Test
    @DisplayName("Should call HttpClient send method exactly once")
    void shouldCallHttpClientSendMethod_once() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");

        // When
        apiService.fetchEmployeesFromApi();

        // Then
        verify(mockClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @DisplayName("Should handle HttpTimeoutException - network timeout scenario")
    void shouldHandleHttpTimeoutException() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.net.http.HttpTimeoutException("Request timed out"));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException for timeout",
                () -> assertTrue(exception.getMessage().contains("Error while fetching from API"),
                        "Message should indicate network error. Actual: " + exception.getMessage()),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause")
        );
    }

    @Test
    @DisplayName("Should parse single employee correctly when JSON contains one user")
    void shouldParseSingleEmployeeCorrectly_whenJsonContainsOneUser() throws Exception {
        // Given
        String singleUserJson = """
            [
                {
                    "id": 1,
                    "name": "Test User",
                    "email": "test@example.com",
                    "company": {
                        "name": "Test Company"
                    }
                }
            ]
            """;

        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(singleUserJson);

        // When
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        // Then
        assertAll("Should correctly parse single employee from JSON",
                () -> assertEquals(1, employees.size(), "Should return exactly one employee"),
                () -> assertEquals("Test User", employees.get(0).getName(), "Name should match"),
                () -> assertEquals("test@example.com", employees.get(0).getEmail(), "Email should match"),
                () -> assertEquals("Test Company", employees.get(0).getCompany(), "Company name should match"),
                () -> assertEquals(Position.PROGRAMMER, employees.get(0).getPosition(), "Position should be PROGRAMMER")
        );
    }
}