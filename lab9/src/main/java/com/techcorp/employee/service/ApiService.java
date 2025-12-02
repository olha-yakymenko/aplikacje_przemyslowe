
package com.techcorp.employee.service;

import com.techcorp.employee.exception.ApiException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.transaction.Status;
import jdk.jshell.Snippet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {

    private final HttpClient httpClient;
    private final Gson gson;

    @Value("${app.api.url}")
    private String apiUrl;

    public ApiService(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }


    public List<Employee> fetchEmployeesFromApi() throws ApiException {
        List<Employee> employees = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiException("HTTP error: " + response.statusCode(), null);
            }

            employees = parseApiResponse(response.body());

        } catch (Exception e) {
            throw new ApiException("Error while fetching from API: " + e.getMessage(), e);
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
                        Position.PROGRAMMER.getBaseSalary(),
                        EmploymentStatus.ACTIVE
                );

                employees.add(employee);
            }

        } catch (Exception e) {
            throw new ApiException("Failed to parse API response", e);
        }

        return employees;
    }
}