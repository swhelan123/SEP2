import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List; // Assuming you might need List

/**
 * Unit tests for the {@link Board} class.
 * Focuses on board creation, cell retrieval, stone counting, coordinate conversions, and reset functionality.
 */
public class BoardTest {

    private Board board;
    private Layout layout;
    private final int DEFAULT_RADIUS = 3; // Example radius
    private final double HEX_SIZE = 30;   // Example size
    private final double ORIGIN_X = 100;  // Example origin
    private final double ORIGIN_Y = 100;  // Example origin

    /**
     * Sets up the test fixture.
     * Called before each test method execution.
     * Initializes a default board and layout.
     */
    @BeforeEach
    void setUp() {
        // Initialize board and layout for each test
        board = new Board(DEFAULT_RADIUS);
        layout = new Layout(Layout.FLAT, HEX_SIZE, ORIGIN_X, ORIGIN_Y);
    }

    @Test
    @DisplayName("Board Creation - Correct Number of Cells")
    void testBoardCreationCellCount() {
        int expectedCells = 3 * DEFAULT_RADIUS * (DEFAULT_RADIUS + 1) + 1;
        assertEquals(expectedCells, board.getCells().size(), "Board should have the correct number of cells for the given radius.");
    }

    @Test
    @DisplayName("Board Creation - Center Cell Exists")
    void testBoardCreationCenterCell() {
        Cell center = board.getCell(0, 0, 0);
        assertNotNull(center, "Center cell (0,0,0) should exist.");
        assertEquals(0, center.q);
        assertEquals(0, center.r);
        assertEquals(0, center.s);
    }

    @Test
    @DisplayName("Board Creation - Edge Cell Exists")
    void testBoardCreationEdgeCell() {
        Cell edge = board.getCell(DEFAULT_RADIUS, 0, -DEFAULT_RADIUS); // Example edge cell
        assertNotNull(edge, "Edge cell (" + DEFAULT_RADIUS + ",0," + (-DEFAULT_RADIUS) + ") should exist.");
    }

    @Test
    @DisplayName("Board Creation - Invalid Radius")
    void testBoardCreationInvalidRadius() {
        assertThrows(IllegalArgumentException.class, () -> new Board(-1), "Board constructor should throw IllegalArgumentException for negative radius.");
    }

    @Test
    @DisplayName("Get Cell - Valid Coordinates")
    void testGetCellValid() {
        Cell cell = board.getCell(1, -1, 0);
        assertNotNull(cell, "Should retrieve cell for valid coordinates (1, -1, 0).");
        assertEquals(1, cell.q);
        assertEquals(-1, cell.r);
        assertEquals(0, cell.s);
    }

    @Test
    @DisplayName("Get Cell - Invalid Coordinates (Sum != 0)")
    void testGetCellInvalidSum() {
        Cell cell = board.getCell(1, 1, 1); // Invalid sum
        assertNull(cell, "Should return null for coordinates where q+r+s != 0.");
    }

    @Test
    @DisplayName("Get Cell - Out of Bounds Coordinates")
    void testGetCellOutOfBounds() {
        Cell cell = board.getCell(DEFAULT_RADIUS + 1, 0, -(DEFAULT_RADIUS + 1)); // Outside radius
        assertNull(cell, "Should return null for coordinates outside the board radius.");
    }

    @Test
    @DisplayName("Count Stones - Initially Zero")
    void testCountStonesInitial() {
        assertEquals(0, board.countStones(Stone.RED), "Initial count for RED stones should be 0.");
        assertEquals(0, board.countStones(Stone.BLUE), "Initial count for BLUE stones should be 0.");
    }

    @Test
    @DisplayName("Count Stones - After Placement")
    void testCountStonesAfterPlacement() {
        Cell c1 = board.getCell(0, 0, 0);
        Cell c2 = board.getCell(1, 0, -1);
        Cell c3 = board.getCell(0, 1, -1);
        if (c1 != null) c1.stone = Stone.RED;
        if (c2 != null) c2.stone = Stone.RED;
        if (c3 != null) c3.stone = Stone.BLUE;

        assertEquals(2, board.countStones(Stone.RED), "Count for RED stones should be 2.");
        assertEquals(1, board.countStones(Stone.BLUE), "Count for BLUE stones should be 1.");
    }

    @Test
    @DisplayName("Find Cell Closest - Center")
    void testFindCellClosestCenter() {
        // Get the expected pixel coordinates for the center cell
        Cell centerCell = board.getCell(0, 0, 0);
        assertNotNull(centerCell, "Center cell must exist for this test.");
        Point2D centerPx = layout.hexToPixel(centerCell); // *** FIXED ***

        // Find the cell closest to the calculated center pixel coordinates
        Cell found = board.findCellClosest(centerPx.x, centerPx.y, layout);
        assertEquals(centerCell, found, "Should find the center cell when clicking its center coordinates.");
    }

    @Test
    @DisplayName("Find Cell Closest - Offset")
    void testFindCellClosestOffset() {
        // Get the expected pixel coordinates for a known cell (e.g., 1, -1, 0)
        Cell targetCell = board.getCell(1, -1, 0);
        assertNotNull(targetCell, "Target cell (1, -1, 0) must exist for this test.");
        Point2D targetPx = layout.hexToPixel(targetCell); // *** FIXED ***

        // Click slightly offset from the center, but still closer to this cell than any other
        double offsetX = HEX_SIZE * 0.1; // Small offset
        double offsetY = HEX_SIZE * 0.1;
        Cell found = board.findCellClosest(targetPx.x + offsetX, targetPx.y + offsetY, layout);
        assertEquals(targetCell, found, "Should find the correct cell when clicking slightly offset from its center.");
    }

    @Test
    @DisplayName("Find Cell Closest - Null Layout")
    void testFindCellClosestNullLayout() {
        assertThrows(NullPointerException.class, () -> board.findCellClosest(100, 100, null),
                "findCellClosest should throw NullPointerException for null layout.");
    }

    @Test
    @DisplayName("Board Reset")
    void testBoardReset() {
        Cell c1 = board.getCell(0, 0, 0);
        Cell c2 = board.getCell(1, 0, -1);
        if (c1 != null) c1.stone = Stone.RED;
        if (c2 != null) c2.stone = Stone.BLUE;

        assertEquals(1, board.countStones(Stone.RED));
        assertEquals(1, board.countStones(Stone.BLUE));

        board.reset();

        assertEquals(0, board.countStones(Stone.RED), "Stone count should be 0 after reset.");
        assertEquals(0, board.countStones(Stone.BLUE), "Stone count should be 0 after reset.");
        assertNull(c1.stone, "Cell 1 should be empty after reset.");
        assertNull(c2.stone, "Cell 2 should be empty after reset.");
    }
}
