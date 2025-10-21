package service;

import src.service.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import src.exception.ApiException;
import src.model.Employee;
import src.model.Position;

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

    private MockedStatic<HttpClient> httpClientMock;
    private HttpClient mockClient;
    private HttpResponse<String> mockResponse;
    private ApiService apiService;

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
        mockClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);
        httpClientMock = mockStatic(HttpClient.class);
        apiService = new ApiService();

        httpClientMock.when(HttpClient::newHttpClient).thenReturn(mockClient);
    }

    @AfterEach
    void tearDown() {
        if (httpClientMock != null) {
            httpClientMock.close();
        }
    }

    @Test
    @DisplayName("SCENARIUSZ 1: Should return list of employees when API returns valid JSON response - simulation without real API")
    void shouldReturnListOfEmployees_whenApiReturnsValidJsonResponse() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(validJsonResponse);

        // When
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        // Then - jedna asercja z assertAll
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
    @DisplayName("SCENARIUSZ 2: Should throw ApiException when API returns 404 status code - HTTP error verification")
    void shouldThrowApiException_whenApiReturns404StatusCode() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);

        // When & Then - jedna asercja
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertTrue(exception.getMessage().contains("HTTP error: 404"), "Exception message should contain HTTP error code");
    }

    @Test
    @DisplayName("SCENARIUSZ 2: Should throw ApiException when API returns 500 status code - HTTP error verification")
    void shouldThrowApiException_whenApiReturns500StatusCode() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(500);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertTrue(exception.getMessage().contains("HTTP error: 500"), "Exception message should contain HTTP error code");
    }

    @Test
    @DisplayName("SCENARIUSZ 3: Should correctly map JSON to Employee object - data parsing verification")
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
                () -> assertTrue(exception.getMessage().contains("Network/IO error while fetching from API"), "Message should indicate network error"),
                () -> assertTrue(exception.getMessage().contains("Connection refused"), "Message should contain specific error details"),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause"),
                () -> assertInstanceOf(IOException.class, exception.getCause(), "Cause should be IOException")
        );
    }

    @Test
    @DisplayName("Should throw ApiException with data validation error when IllegalArgumentException occurs")
    void shouldThrowApiExceptionWithDataValidationError_whenIllegalArgumentExceptionOccurs() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IllegalArgumentException("Invalid URL format"));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException with proper data validation error details",
                () -> assertTrue(exception.getMessage().contains("Data validation error in API response"), "Message should indicate data validation error"),
                () -> assertTrue(exception.getMessage().contains("Invalid URL format"), "Message should contain specific error details"),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause"),
                () -> assertInstanceOf(IllegalArgumentException.class, exception.getCause(), "Cause should be IllegalArgumentException")
        );
    }

    @Test
    @DisplayName("Should throw ApiException when JSON response is invalid - parsing error verification")
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
                () -> assertTrue(exception.getMessage().contains("Failed to parse API response"), "Message should indicate parsing failure"),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause")
        );
    }

    @Test
    @DisplayName("Should throw ApiException when JSON has missing required fields - data validation")
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
                () -> assertTrue(exception.getMessage().contains("Failed to parse API response"), "Message should indicate parsing failure"),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause")
        );
    }

    @Test
    @DisplayName("Should return empty list when API returns empty JSON array - edge case verification")
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
    @DisplayName("Should throw ApiException when unexpected exception occurs - error handling verification")
    void shouldThrowApiException_whenUnexpectedExceptionOccurs() throws Exception {
        // Given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Unexpected system error"));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException for unexpected errors",
                () -> assertTrue(exception.getMessage().contains("Unexpected error while fetching from API"), "Message should indicate unexpected error"),
                () -> assertTrue(exception.getMessage().contains("Unexpected system error"), "Message should contain specific error details"),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause"),
                () -> assertInstanceOf(RuntimeException.class, exception.getCause(), "Cause should be RuntimeException")
        );
    }

    @Test
    @DisplayName("Should call HttpClient send method exactly once - method invocation verification")
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

        // When & Then - jedna asercja z assertAll
        ApiException exception = assertThrows(ApiException.class, () ->
                apiService.fetchEmployeesFromApi()
        );

        assertAll("Should throw ApiException for timeout",
                () -> assertTrue(exception.getMessage().contains("Network/IO error while fetching from API"), "Message should indicate network error"),
                () -> assertNotNull(exception.getCause(), "Exception should have a cause")
        );
    }

    @Test
    @DisplayName("Should parse single employee correctly when JSON contains one user - specific parsing scenario")
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