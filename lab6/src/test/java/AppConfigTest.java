package com.techcorp.employee.config;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void testHttpClientBean() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // When
        HttpClient httpClient = context.getBean(HttpClient.class);

        // Then - POPRAWIONE: Sprawdzamy czy obiekt jest przypisywalny do HttpClient
        assertNotNull(httpClient);
        assertTrue(httpClient instanceof HttpClient, "Bean should be instance of HttpClient");

        context.close();
    }

    @Test
    void testGsonBean() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // When
        Gson gson = context.getBean(Gson.class);

        // Then - POPRAWIONE: Sprawdzamy czy obiekt jest przypisywalny do Gson
        assertNotNull(gson);
        assertTrue(gson instanceof Gson, "Bean should be instance of Gson");

        context.close();
    }

    @Test
    void testBeansAreSingletons() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // When
        HttpClient httpClient1 = context.getBean(HttpClient.class);
        HttpClient httpClient2 = context.getBean(HttpClient.class);
        Gson gson1 = context.getBean(Gson.class);
        Gson gson2 = context.getBean(Gson.class);

        // Then - powinny być te same instancje (singleton)
        assertSame(httpClient1, httpClient2);
        assertSame(gson1, gson2);

        context.close();
    }

    @Test
    void testBeanNames() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // When & Then - sprawdzamy czy beany są zarejestrowane pod domyślnymi nazwami
        assertTrue(context.containsBean("httpClient"));
        assertTrue(context.containsBean("gson"));

        context.close();
    }
}