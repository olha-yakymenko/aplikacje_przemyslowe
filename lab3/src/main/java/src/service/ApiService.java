package src.service;

import src.exception.ApiException;
import src.model.Employee;
import src.model.Position;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ApiService {
    private static final String API_URL = "https://jsonplaceholder.typicode.com/users";

    /**
     * Pobiera pracowników z REST API
     */
    public List<Employee> fetchEmployeesFromApi() throws ApiException {
        List<Employee> employees = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiException("HTTP error: " + response.statusCode(), null);
            }

            employees = parseApiResponse(response.body());

        } catch (IOException e) {
            throw new ApiException("Network/IO error while fetching from API: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Data validation error in API response: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ApiException("Unexpected error while fetching from API: " + e.getMessage(), e);
        }

        return employees;
    }

    private List<Employee> parseApiResponse(String jsonResponse) throws ApiException {
        List<Employee> employees = new ArrayList<>();

        try {
            JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject user = element.getAsJsonObject();

                String fullName = user.get("name").getAsString();
                String email = user.get("email").getAsString();

                JsonObject company = user.getAsJsonObject("company");
                String companyName = company.get("name").getAsString();

                // Tworzenie pracownika z domyślnym stanowiskiem PROGRAMMER
                Employee employee = new Employee(
                        fullName,
                        email,
                        companyName,
                        Position.PROGRAMMER,
                        Position.PROGRAMMER.getBaseSalary()
                );

                employees.add(employee);
            }

        } catch (Exception e) {
            throw new ApiException("Failed to parse API response", e);
        }


        return employees;
    }
}