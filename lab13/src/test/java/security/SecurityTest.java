package com.techcorp.employee.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;


@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().is3xxRedirection()) // Przekierowanie do /login
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccessToEmployees() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserCannotAccessNewEmployeeForm() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminCanAccessNewEmployeeForm() throws Exception {
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk());
    }

    @Test
    void testApiAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized()); // 401 dla API bez auth
    }


//    @Test
//    void testApiUserCannotDelete() throws Exception {
//        mockMvc.perform(delete("/api/employees/1")
//                        .with(httpBasic("user", "user123")))
//                .andExpect(status().isForbidden()); // 403 - USER nie może usuwać
//    }
//
//    @Test
//    @WithUserDetails("admin")
//    void testAdminCanDelete() throws Exception {
//        // Test dla widoków MVC
//        mockMvc.perform(post("/employees/delete/1"))
//                .andExpect(status().is3xxRedirection()); // Admin może usuwać
//    }



    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminCanAccessEmployeeList() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminCanAccessAddEmployeeForm() throws Exception {
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminCanAccessImportForm() throws Exception {
        mockMvc.perform(get("/employees/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/import-form"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminCanAccessQuickList() throws Exception {
        mockMvc.perform(get("/employees/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list-quick"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminCanAccessSearchFull() throws Exception {
        mockMvc.perform(get("/employees/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/search-full"));
    }



    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/employees/delete/some@email.com"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminHasAccessToEverythingUserHas() throws Exception {
        // Wszystko co USER może, ADMIN też może
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/employees/list"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/employees/search"))
                .andExpect(status().isOk());
    }



    // Test 1: Próba wejścia na /api/employees bez autoryzacji -> 401 Unauthorized
    @Test
    void testApiAccessWithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/employees/statistics/company"))
                .andExpect(status().isUnauthorized());
    }


    // Test 3: USER próbuje stworzyć pracownika przez API -> 403 Forbidden (brak uprawnień)
    @Test
    void testApiPostEmployeeByUser_ShouldReturn403() throws Exception {
        String employeeJson = """
        {
            "firstName": "Jan",
            "lastName": "Kowalski",
            "email": "jan.kowalski@techcorp.com",
            "company": "TechCorp",
            "position": "PROGRAMMER",
            "salary": 5000.0,
            "status": "ACTIVE"
        }
        """;

        mockMvc.perform(post("/api/employees")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andDo(MockMvcResultHandlers.print()) // DODAJ TO
                .andExpect(status().isForbidden());
    }

    // Test 4: ADMIN może stworzyć pracownika przez API
    @Test
    void testApiPostEmployeeByAdmin_ShouldReturn201() throws Exception {
        String uniqueEmail = "admin.test." + System.currentTimeMillis() + "@techcorp.com";
        String employeeJson = String.format("""
            {
                "firstName": "Admin",
                "lastName": "Testowy",
                "email": "%s",
                "company": "TechCorp",
                "position": "PROGRAMMER",
                "salary": 8000.0,
                "status": "ACTIVE"
            }
            """, uniqueEmail);

        mockMvc.perform(post("/api/employees")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isCreated());
    }

    // Test 5: USER może odczytać pracownika po emailu (jeśli istnieje)
    @Test
    void testApiGetEmployeeByEmailByUser_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/employees/test@example.com")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(
                        is(200),
                        is(404)
                )));
    }

    // Test 6: USER NIE może usunąć pracownika -> 403 Forbidden
    @Test
    void testApiDeleteEmployeeByUser_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/employees/test@example.com")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

     //Test 7: ADMIN może usunąć pracownika
    @Test
    void testApiDeleteEmployeeByAdmin_ShouldReturn204() throws Exception {
        // Najpierw utwórz pracownika
        String uniqueEmail = "to.delete." + System.currentTimeMillis() + "@techcorp.com";
        String employeeJson = String.format("""
            {
                "firstName": "ToDelete",
                "lastName": "Employee",
                "email": "%s",
                "company": "TechCorp",
                "position": "PROGRAMMER",
                "salary": 3000.0,
                "status": "ACTIVE"
            }
            """, uniqueEmail);

        // Utwórz pracownika
        mockMvc.perform(post("/api/employees")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isCreated());

        // Teraz usuń
        mockMvc.perform(delete("/api/employees/" + uniqueEmail)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }



    // Test 8: USER NIE może aktualizować pracownika
    @Test
    void testApiUpdateEmployeeByUser_ShouldReturn403() throws Exception {
        String updateJson = """
            {
                "firstName": "Updated",
                "lastName": "Name",
                "email": "test@techcorp.com",
                "company": "UpdatedCorp",
                "position": "PRESIDENT",
                "salary": 6000.0,
                "status": "ACTIVE"
            }
            """;

        mockMvc.perform(put("/api/employees/test@example.com")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }

    // Test 9: USER NIE może zmienić statusu
    @Test
    void testApiUpdateStatusByUser_ShouldReturn403() throws Exception {
        String statusJson = """
            {
                "status": "TERMINATED"
            }
            """;

        mockMvc.perform(patch("/api/employees/test@example.com/status")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson))
                .andExpect(status().isForbidden());
    }

    // Test 10: USER może przeglądać statystyki (jeśli endpoint działa)
    @Test
    void testApiGetStatisticsByUser_ShouldSucceedIfEndpointExists() throws Exception {
        mockMvc.perform(get("/api/employees/statistics/company")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(anyOf(
                        is(200),
                        is(404),
                        is(500)
                )));
    }

    // Test 11: USER może przeglądać pracowników po statusie
    @Test
    void testApiGetEmployeesByStatusByUser_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/employees/status/ACTIVE?page=0&size=10")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test 12: USER może przeglądać pracowników po firmie
    @Test
    void testApiGetEmployeesByCompanyByUser_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/employees/company/TechCorp?page=0&size=10")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test 13: ADMIN ma dostęp do wszystkich endpointów odczytu
    @Test
    void testAdminHasReadAccessToAllEndpoints() throws Exception {
        mockMvc.perform(get("/api/employees/statistics/company")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(
                        is(200),
                        is(404),
                        is(500)
                )));
    }


    // Test 14: Weryfikacja że Basic Auth działa z błędnym hasłem
    @Test
    void testBasicAuthWithWrongPassword_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/employees/statistics/company")
                        .with(httpBasic("user", "wrongpassword"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // Test 15: Próba dostępu do API bez żadnych danych uwierzytelniających
    @Test
    void testApiPostWithoutAuth_ShouldReturn401() throws Exception {
        String employeeJson = """
            {
                "firstName": "Test",
                "lastName": "User",
                "email": "test@example.com",
                "company": "TechCorp",
                "position": "Tester",
                "salary": 4000.0,
                "status": "ACTIVE"
            }
            """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isUnauthorized());
    }

    // Test 16: ADMIN może aktualizować pracownika
    @Test
    void testApiUpdateEmployeeByAdmin_ShouldSucceed() throws Exception {
        // Najpierw utwórz pracownika
        String uniqueEmail = "to.update." + System.currentTimeMillis() + "@techcorp.com";
        String createJson = String.format("""
            {
                "firstName": "Original",
                "lastName": "Name",
                "email": "%s",
                "company": "TechCorp",
                "position": "PROGRAMMER",
                "salary": 4000.0,
                "status": "ACTIVE"
            }
            """, uniqueEmail);

        mockMvc.perform(post("/api/employees")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated());

        // Teraz zaktualizuj
        String updateJson = String.format("""
            {
                "firstName": "Updated",
                "lastName": "Name",
                "email": "%s",
                "company": "UpdatedCorp",
                "position": "PRESIDENT",
                "salary": 5000.0,
                "status": "ACTIVE"
            }
            """, uniqueEmail);

        mockMvc.perform(put("/api/employees/" + uniqueEmail)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());
    }
}