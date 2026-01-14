// EmployeeViewRepositoryIntegrationTest.java
package com.techcorp.employee.repository;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class EmployeeViewRepositoryIntegrationTest {

    @Autowired
    private EmployeeViewRepository employeeViewRepository;

    @Test
    void shouldFailWhenTryingToUseSaveMethod() {
        // 1. Sprawdź czy repozytorium działa (metody do odczytu)
        assertThat(employeeViewRepository.findAll()).isNotNull();

        // 2. Próba wywołania save() przez refleksję - powinna się nie udać
        try {
            // Szukaj metody save w interfejsie
            Method saveMethod = EmployeeViewRepository.class.getMethod("save", Employee.class);

            // Jeśli znajdziemy metodę save (co nie powinno mieć miejsca) - to test fail
            assertThat(saveMethod)
                    .as("EmployeeViewRepository NIE powinien mieć metody save()")
                    .isNull(); // To się wykona tylko jeśli getMethod nie rzuci wyjątku

        } catch (NoSuchMethodException e) {
            // TO JEST OCZEKIWANE! - metoda save() nie istnieje
            assertThat(e)
                    .as("Oczekiwany wyjątek - metoda save() nie istnieje")
                    .isInstanceOf(NoSuchMethodException.class);
        }

        // 3. Próba stworzenia Employee i "zapisania" go inaczej
        Employee testEmployee = new Employee(
                "Test Name",
                "test@example.com",
                "Test Company",
                Position.PROGRAMMER,
                BigDecimal.valueOf(5000)
        );

        // 4. Potwierdź że NIE MOŻEMY zapisać (brak metody save)
        //    Próba przez refleksję:
        assertThatThrownBy(() -> {
            // Próba znalezienia metody save (rzuci NoSuchMethodException)
            EmployeeViewRepository.class.getMethod("save", Object.class);
        })
                .isInstanceOf(NoSuchMethodException.class)
                .hasMessageContaining("save");
    }

    @Test
    void shouldOnlyHaveReadMethodsAvailable() {
        // Jedna asercja z assertAll wewnątrz
        assertThat(employeeViewRepository).satisfies(repo -> {
            // Sprawdź metody przez refleksję
            Method[] methods = repo.getClass().getInterfaces()[0].getMethods();

            long readMethodCount = java.util.Arrays.stream(methods)
                    .filter(m -> m.getName().startsWith("find") ||
                            m.getName().startsWith("count") ||
                            m.getName().startsWith("exists"))
                    .count();

            long modifyMethodCount = java.util.Arrays.stream(methods)
                    .filter(m -> m.getName().startsWith("save") ||
                            m.getName().startsWith("delete"))
                    .count();

            assertThat(modifyMethodCount)
                    .as("Liczba metod modyfikujących powinna być 0")
                    .isZero();

            assertThat(readMethodCount)
                    .as("Powinny być dostępne metody do odczytu")
                    .isGreaterThan(0);
        });
    }

//nie ma save wiec nie moge sprawdzic czy blad bedzie rzucony
//    @Test
//    void shouldThrowExceptionWhenTryingToCallSaveMethod() {
//        Employee testEmployee = new Employee(
//                "Test Name",
//                "test@example.com",
//                "Test Company",
//                Position.PROGRAMMER,
//                BigDecimal.valueOf(5000)
//        );
//
//        assertThatThrownBy(() -> {
//            Method saveMethod = employeeViewRepository.getClass()
//                    .getMethod("save", Object.class);
//
//            saveMethod.invoke(employeeViewRepository, testEmployee);
//        })
//                .as("Wywołanie save() powinno rzucić wyjątek")
//                .isInstanceOf(InvocationTargetException.class)
//                .hasCauseInstanceOf(InvalidDataAccessApiUsageException.class)
//                .cause()
//                .hasMessageContaining("No accessor to set property");
//    }


    @Test
    void shouldNotHaveSaveMethodAvailable() {
        assertAll(
                "Weryfikacja braku metody save() w repozytorium",
                () -> {
                    assertThatThrownBy(() -> {
                        employeeViewRepository.getClass()
                                .getMethod("save", Object.class);
                    })
                            .as("Metoda save() nie powinna istnieć w proxy repozytorium")
                            .isInstanceOf(NoSuchMethodException.class);
                },
                () -> {
                    assertThatThrownBy(() -> {
                        employeeViewRepository.getClass()
                                .getMethod("delete", Object.class);
                    })
                            .as("Metoda delete() nie powinna istnieć w proxy repozytorium")
                            .isInstanceOf(NoSuchMethodException.class);
                },
                () -> {
                    assertThat(employeeViewRepository.findAll()).isNotNull();
                },
                () -> {
                    assertThat(employeeViewRepository.findById(1L)).isNotNull();
                }
        );
    }
}