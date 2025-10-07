
import org.example.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class PositionTest {

    @Test
    void testBaseSalaries() {
        assertEquals(25000, Position.PRESIDENT.getBaseSalary(), 0.001);
        assertEquals(18000, Position.VICE_PRESIDENT.getBaseSalary(), 0.001);
        assertEquals(12000, Position.MANAGER.getBaseSalary(), 0.001);
        assertEquals(8000, Position.PROGRAMMER.getBaseSalary(), 0.001);
        assertEquals(3000, Position.INTERN.getBaseSalary(), 0.001);
    }

    @Test
    void testHierarchyLevels() {
        assertEquals(1, Position.PRESIDENT.getHierarchyLevel());
        assertEquals(2, Position.VICE_PRESIDENT.getHierarchyLevel());
        assertEquals(3, Position.MANAGER.getHierarchyLevel());
        assertEquals(4, Position.PROGRAMMER.getHierarchyLevel());
        assertEquals(5, Position.INTERN.getHierarchyLevel());
    }

    @Test
    void testHierarchyComparator() {
        Comparator<Position> comparator = Position.getHierarchyComparator();

        assertTrue(comparator.compare(Position.PRESIDENT, Position.VICE_PRESIDENT) < 0);
        assertTrue(comparator.compare(Position.VICE_PRESIDENT, Position.MANAGER) < 0);
        assertTrue(comparator.compare(Position.MANAGER, Position.PROGRAMMER) < 0);
        assertTrue(comparator.compare(Position.PROGRAMMER, Position.INTERN) < 0);
        assertEquals(0, comparator.compare(Position.MANAGER, Position.MANAGER));
    }

    @Test
    void testPositionsInHierarchyOrder() {
        List<Position> sortedPositions = List.of(Position.values()).stream()
                .sorted(Position.getHierarchyComparator())
                .collect(Collectors.toList());

        assertEquals(Position.PRESIDENT, sortedPositions.get(0));
        assertEquals(Position.VICE_PRESIDENT, sortedPositions.get(1));
        assertEquals(Position.MANAGER, sortedPositions.get(2));
        assertEquals(Position.PROGRAMMER, sortedPositions.get(3));
        assertEquals(Position.INTERN, sortedPositions.get(4));
    }
}