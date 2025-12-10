import com.techcorp.employee.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    @DisplayName("Should return correct base salary for each position")
    void testGetBaseSalary() {
        assertAll("Base salaries validation",
                () -> assertEquals(25000, Position.PRESIDENT.getBaseSalary(), 0.001),
                () -> assertEquals(18000, Position.VICE_PRESIDENT.getBaseSalary(), 0.001),
                () -> assertEquals(12000, Position.MANAGER.getBaseSalary(), 0.001),
                () -> assertEquals(8000, Position.PROGRAMMER.getBaseSalary(), 0.001),
                () -> assertEquals(3000, Position.INTERN.getBaseSalary(), 0.001)
        );
    }

    @Test
    @DisplayName("Should return correct hierarchy level for each position")
    void testGetHierarchyLevel() {
        assertAll("Hierarchy levels validation",
                () -> assertEquals(1, Position.PRESIDENT.getHierarchyLevel()),
                () -> assertEquals(2, Position.VICE_PRESIDENT.getHierarchyLevel()),
                () -> assertEquals(3, Position.MANAGER.getHierarchyLevel()),
                () -> assertEquals(4, Position.PROGRAMMER.getHierarchyLevel()),
                () -> assertEquals(5, Position.INTERN.getHierarchyLevel())
        );
    }

    @ParameterizedTest
    @EnumSource(Position.class)
    @DisplayName("Should have positive base salary for all positions")
    void testAllPositionsHavePositiveBaseSalary(Position position) {
        assertTrue(position.getBaseSalary() > 0,
                "Position " + position + " should have positive base salary");
    }

    @ParameterizedTest
    @EnumSource(Position.class)
    @DisplayName("Should have positive hierarchy level for all positions")
    void testAllPositionsHavePositiveHierarchyLevel(Position position) {
        assertTrue(position.getHierarchyLevel() > 0,
                "Position " + position + " should have positive hierarchy level");
    }

    @Test
    @DisplayName("Should sort positions by hierarchy level in ascending order")
    void testHierarchyComparator() {
        Comparator<Position> comparator = Position.getHierarchyComparator();

        assertAll("Hierarchy comparator validation",
                () -> assertTrue(comparator.compare(Position.PRESIDENT, Position.VICE_PRESIDENT) < 0,
                        "PRESIDENT should be before VICE_PRESIDENT in hierarchy"),
                () -> assertTrue(comparator.compare(Position.VICE_PRESIDENT, Position.MANAGER) < 0,
                        "VICE_PRESIDENT should be before MANAGER in hierarchy"),
                () -> assertTrue(comparator.compare(Position.MANAGER, Position.PROGRAMMER) < 0,
                        "MANAGER should be before PROGRAMMER in hierarchy"),
                () -> assertTrue(comparator.compare(Position.PROGRAMMER, Position.INTERN) < 0,
                        "PROGRAMMER should be before INTERN in hierarchy"),
                () -> assertEquals(0, comparator.compare(Position.MANAGER, Position.MANAGER),
                        "Same positions should be equal in comparison"),
                () -> assertTrue(comparator.compare(Position.VICE_PRESIDENT, Position.PRESIDENT) > 0,
                        "VICE_PRESIDENT should be after PRESIDENT in hierarchy")
        );
    }

    @Test
    @DisplayName("Should maintain correct hierarchy order when sorted")
    void testPositionsInHierarchyOrder() {
        List<Position> sortedPositions = List.of(Position.values()).stream()
                .sorted(Position.getHierarchyComparator())
                .collect(Collectors.toList());

        assertAll("Sorted hierarchy validation",
                () -> assertEquals(5, sortedPositions.size(), "Should have all 5 positions"),
                () -> assertEquals(Position.PRESIDENT, sortedPositions.get(0), "PRESIDENT should be first"),
                () -> assertEquals(Position.VICE_PRESIDENT, sortedPositions.get(1), "VICE_PRESIDENT should be second"),
                () -> assertEquals(Position.MANAGER, sortedPositions.get(2), "MANAGER should be third"),
                () -> assertEquals(Position.PROGRAMMER, sortedPositions.get(3), "PROGRAMMER should be fourth"),
                () -> assertEquals(Position.INTERN, sortedPositions.get(4), "INTERN should be last")
        );
    }

    @Test
    @DisplayName("Should have consistent hierarchy levels")
    void testHierarchyLevelsAreConsistent() {
        List<Integer> levels = List.of(Position.values()).stream()
                .map(Position::getHierarchyLevel)
                .sorted()
                .collect(Collectors.toList());

        assertAll("Hierarchy consistency validation",
                () -> assertEquals(5, levels.stream().distinct().count(),
                        "All positions should have unique hierarchy levels"),
                () -> assertEquals(List.of(1, 2, 3, 4, 5), levels,
                        "Hierarchy levels should be 1 through 5")
        );
    }

    @Test
    @DisplayName("Should have base salaries in descending order by hierarchy")
    void testBaseSalariesCorrelateWithHierarchy() {
        assertAll("Salary hierarchy correlation",
                () -> assertTrue(Position.PRESIDENT.getBaseSalary() > Position.VICE_PRESIDENT.getBaseSalary(),
                        "PRESIDENT should have higher salary than VICE_PRESIDENT"),
                () -> assertTrue(Position.VICE_PRESIDENT.getBaseSalary() > Position.MANAGER.getBaseSalary(),
                        "VICE_PRESIDENT should have higher salary than MANAGER"),
                () -> assertTrue(Position.MANAGER.getBaseSalary() > Position.PROGRAMMER.getBaseSalary(),
                        "MANAGER should have higher salary than PROGRAMMER"),
                () -> assertTrue(Position.PROGRAMMER.getBaseSalary() > Position.INTERN.getBaseSalary(),
                        "PROGRAMMER should have higher salary than INTERN")
        );
    }

    @Test
    @DisplayName("Should return all enum values")
    void testEnumValues() {
        Position[] positions = Position.values();

        assertAll("Enum values validation",
                () -> assertEquals(5, positions.length, "Should have exactly 5 position types"),
                () -> assertArrayEquals(new Position[]{
                        Position.PRESIDENT,
                        Position.VICE_PRESIDENT,
                        Position.MANAGER,
                        Position.PROGRAMMER,
                        Position.INTERN
                }, positions)
        );
    }

    @Test
    @DisplayName("Should convert string to Position enum")
    void testValueOf() {
        assertAll("ValueOf conversion validation",
                () -> assertEquals(Position.PRESIDENT, Position.valueOf("PRESIDENT")),
                () -> assertEquals(Position.VICE_PRESIDENT, Position.valueOf("VICE_PRESIDENT")),
                () -> assertEquals(Position.MANAGER, Position.valueOf("MANAGER")),
                () -> assertEquals(Position.PROGRAMMER, Position.valueOf("PROGRAMMER")),
                () -> assertEquals(Position.INTERN, Position.valueOf("INTERN"))
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid position name")
    void testValueOfWithInvalidName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Position.valueOf("INVALID_POSITION"));

        assertTrue(exception.getMessage().contains("INVALID_POSITION"));
    }

    @Test
    @DisplayName("Should return correct string representation")
    void testToString() {
        assertAll("ToString validation",
                () -> assertEquals("PRESIDENT", Position.PRESIDENT.toString()),
                () -> assertEquals("VICE_PRESIDENT", Position.VICE_PRESIDENT.toString()),
                () -> assertEquals("MANAGER", Position.MANAGER.toString()),
                () -> assertEquals("PROGRAMMER", Position.PROGRAMMER.toString()),
                () -> assertEquals("INTERN", Position.INTERN.toString())
        );
    }

    @Test
    @DisplayName("Comparator should be consistent with hierarchy levels")
    void testComparatorConsistency() {
        Comparator<Position> comparator = Position.getHierarchyComparator();

        for (Position p1 : Position.values()) {
            for (Position p2 : Position.values()) {
                int expected = Integer.compare(p1.getHierarchyLevel(), p2.getHierarchyLevel());
                int actual = comparator.compare(p1, p2);
                assertEquals(expected, actual,
                        String.format("Comparison mismatch for %s vs %s", p1, p2));
            }
        }
    }

    @Test
    @DisplayName("Should maintain enum order consistency")
    void testEnumOrderConsistency() {
        Position[] positions = Position.values();

        assertAll("Enum order consistency",
                () -> assertEquals(Position.PRESIDENT, positions[0]),
                () -> assertEquals(Position.VICE_PRESIDENT, positions[1]),
                () -> assertEquals(Position.MANAGER, positions[2]),
                () -> assertEquals(Position.PROGRAMMER, positions[3]),
                () -> assertEquals(Position.INTERN, positions[4])
        );
    }
}