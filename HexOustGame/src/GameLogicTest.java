import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Comparator; // For sorting lists in tests if needed
import java.util.ArrayList;
import java.util.Set; // For checking group contents efficiently
import java.util.HashSet;
import java.util.Collections;

/**
 * Robust unit tests for the {@link GameLogic} class of the HexOust game.
 * Covers core game rules including neighbor finding, connected group calculations,
 * move legality checks, move processing (placement and capture), and identification
 * of capturable cells.
 */
public class GameLogicTest {

    private Board board;
    private final int DEFAULT_RADIUS = 3; // Use radius 3 for more complex scenarios

    /**
     * Sets up the test fixture.
     * Called before each test method execution.
     * Initializes a new Board with the default radius.
     */
    @BeforeEach
    void setUp() {
        board = new Board(DEFAULT_RADIUS);
    }

    // --- Helper Method ---

    /**
     * Helper method to find a cell by coordinates directly using the board's method.
     * Asserts that the cell is not null, simplifying tests.
     * @param b The board to search on.
     * @param q The q coordinate.
     * @param r The r coordinate.
     * @param s The s coordinate.
     * @return The found Cell.
     * @throws AssertionError if the cell is not found.
     */
    private Cell findCell(Board b, int q, int r, int s) {
        Cell cell = b.getCell(q, r, s);
        assertNotNull(cell, String.format("Cell (%d,%d,%d) should exist on board radius %d.", q, r, s, DEFAULT_RADIUS));
        return cell;
    }

    /**
     * Helper method to assert that two lists of Cells contain the same elements,
     * regardless of order.
     * @param expected The expected list of Cells.
     * @param actual The actual list of Cells.
     * @param message The assertion message if they don't match.
     */
    private void assertCellListsEqualUnordered(List<Cell> expected, List<Cell> actual, String message) {
        assertEquals(expected.size(), actual.size(), message + " (Size mismatch)");
        assertTrue(new HashSet<>(actual).containsAll(expected), message + " (Content mismatch - actual missing expected)");
        assertTrue(new HashSet<>(expected).containsAll(actual), message + " (Content mismatch - expected missing actual)");
    }


    // --- getNeighbors Tests ---
    @Test
    @DisplayName("Get Neighbors - Center Cell")
    void testGetNeighborsCenter() {
        Cell center = findCell(board, 0, 0, 0);
        List<Cell> neighbors = GameLogic.getNeighbors(center, board);
        assertEquals(6, neighbors.size(), "Center cell should have 6 neighbors.");
        // Optionally check coordinates of neighbors
        Set<String> expectedCoords = Set.of("(1,0,-1)", "(1,-1,0)", "(0,-1,1)", "(-1,0,1)", "(-1,1,0)", "(0,1,-1)");
        Set<String> actualCoords = new HashSet<>();
        for(Cell n : neighbors) {
            actualCoords.add(String.format("(%d,%d,%d)", n.q, n.r, n.s));
        }
        assertEquals(expectedCoords, actualCoords, "Neighbor coordinates mismatch for center cell.");
    }

    @Test
    @DisplayName("Get Neighbors - Edge Cell")
    void testGetNeighborsEdge() {
        Cell edge = findCell(board, 3, -3, 0);
        List<Cell> neighbors = GameLogic.getNeighbors(edge, board);
        assertEquals(3, neighbors.size(), "Edge cell (3,-3,0) should have 3 neighbors.");
        Set<String> expectedCoords = Set.of("(2,-2,0)", "(3,-2,-1)", "(2,-3,1)"); // Calculate expected neighbours
        Set<String> actualCoords = new HashSet<>();
        for(Cell n : neighbors) {
            actualCoords.add(String.format("(%d,%d,%d)", n.q, n.r, n.s));
        }
        assertEquals(expectedCoords, actualCoords, "Neighbor coordinates mismatch for edge cell (3,-3,0).");
    }

    @Test
    @DisplayName("Get Neighbors - Corner Cell")
    void testGetNeighborsCorner() {
        Cell corner = findCell(board, 3, 0, -3); // A corner cell for radius 3
        List<Cell> neighbors = GameLogic.getNeighbors(corner, board);
        assertEquals(2, neighbors.size(), "Corner cell (3,0,-3) should have 2 neighbors for radius 3.");
        Set<String> expectedCoords = Set.of("(3,-1,-2)", "(2,0,-2)"); // Calculate expected neighbours
        Set<String> actualCoords = new HashSet<>();
        for(Cell n : neighbors) {
            actualCoords.add(String.format("(%d,%d,%d)", n.q, n.r, n.s));
        }
        assertEquals(expectedCoords, actualCoords, "Neighbor coordinates mismatch for corner cell (3,0,-3).");
    }

    @Test
    @DisplayName("Get Neighbors - Null Cell Input")
    void testGetNeighborsNullCell() {
        assertThrows(NullPointerException.class, () -> {
            GameLogic.getNeighbors(null, board);
        }, "getNeighbors should throw NullPointerException for null cell input.");
    }

    @Test
    @DisplayName("Get Neighbors - Null Board Input")
    void testGetNeighborsNullBoard() {
        Cell center = findCell(board, 0, 0, 0);
        assertThrows(NullPointerException.class, () -> {
            GameLogic.getNeighbors(center, null);
        }, "getNeighbors should throw NullPointerException for null board input.");
    }


    // --- getConnectedGroup Tests ---
    @Test
    @DisplayName("Get Connected Group - Single Stone")
    void testGetConnectedGroupSingle() {
        Cell cell = findCell(board, 0, 0, 0); cell.stone = Stone.RED;
        List<Cell> group = GameLogic.getConnectedGroup(cell, board, Stone.RED);
        assertEquals(1, group.size());
        assertTrue(group.contains(cell));
    }

    @Test
    @DisplayName("Get Connected Group - Linear Chain")
    void testGetConnectedGroupLinear() {
        Cell c1 = findCell(board, 0, 0, 0); c1.stone = Stone.RED;
        Cell c2 = findCell(board, 1, -1, 0); c2.stone = Stone.RED;
        Cell c3 = findCell(board, 2, -2, 0); c3.stone = Stone.RED;
        Cell c4 = findCell(board, 3, -3, 0); c4.stone = Stone.RED; // Edge cell
        Cell other = findCell(board, 0, 1, -1); other.stone = Stone.RED; // Disconnected

        List<Cell> expectedGroup = List.of(c1, c2, c3, c4);
        List<Cell> group = GameLogic.getConnectedGroup(c1, board, Stone.RED);

        assertCellListsEqualUnordered(expectedGroup, group, "Linear group incorrect");
        assertFalse(group.contains(other), "Disconnected stone should not be in the group.");
    }

    @Test
    @DisplayName("Get Connected Group - Cluster")
    void testGetConnectedGroupCluster() {
        Cell c0 = findCell(board, 0, 0, 0); c0.stone = Stone.BLUE;
        Cell c1 = findCell(board, 1, -1, 0); c1.stone = Stone.BLUE;
        Cell c2 = findCell(board, 0, -1, 1); c2.stone = Stone.BLUE;
        Cell c3 = findCell(board, 1, -2, 1); c3.stone = Stone.BLUE; // Connects c1 and c2
        Cell other = findCell(board, -1, 0, 1); other.stone = Stone.BLUE; // Disconnected

        List<Cell> expectedGroup = List.of(c0, c1, c2, c3);
        List<Cell> group = GameLogic.getConnectedGroup(c0, board, Stone.BLUE);

        assertCellListsEqualUnordered(expectedGroup, group, "Cluster group incorrect");
        assertFalse(group.contains(other), "Disconnected stone should not be in the group.");
    }

    @Test
    @DisplayName("Get Connected Group - Start Cell Wrong Color")
    void testGetConnectedGroupWrongColor() {
        Cell cell = findCell(board, 0, 0, 0); cell.stone = Stone.RED;
        List<Cell> group = GameLogic.getConnectedGroup(cell, board, Stone.BLUE); // Looking for BLUE
        assertTrue(group.isEmpty(), "Should return empty list if start cell has wrong color.");
    }

    @Test
    @DisplayName("Get Connected Group - Start Cell Empty")
    void testGetConnectedGroupEmptyStart() {
        Cell cell = findCell(board, 0, 0, 0); // cell.stone is null
        List<Cell> group = GameLogic.getConnectedGroup(cell, board, Stone.RED);
        assertTrue(group.isEmpty(), "Should return empty list if start cell is empty.");
    }

    @Test
    @DisplayName("Get Connected Group - Null Inputs")
    void testGetConnectedGroupNulls() {
        Cell cell = findCell(board, 0, 0, 0); cell.stone = Stone.RED;
        assertThrows(NullPointerException.class, () -> GameLogic.getConnectedGroup(null, board, Stone.RED));
        assertThrows(NullPointerException.class, () -> GameLogic.getConnectedGroup(cell, null, Stone.RED));
        assertThrows(NullPointerException.class, () -> GameLogic.getConnectedGroup(cell, board, null));
    }

    // --- getOpponentGroups Tests ---
    @Test
    @DisplayName("Get Opponent Groups - Single Adjacent Group")
    void testGetOpponentGroupsSingle() {
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell b1 = findCell(board, 1, -1, 0); b1.stone = Stone.BLUE; // Adjacent to r1
        Cell b2 = findCell(board, 1, 0, -1); b2.stone = Stone.BLUE; // Adjacent to r1 and b1

        List<Cell> redGroup = List.of(r1); // Player group
        List<Cell> expectedOpponentGroup = List.of(b1, b2); // The connected blue group

        List<List<Cell>> opponentGroups = GameLogic.getOpponentGroups(redGroup, board, Stone.RED);

        assertEquals(1, opponentGroups.size(), "Should find exactly one opponent group.");
        assertCellListsEqualUnordered(expectedOpponentGroup, opponentGroups.get(0), "Opponent group members mismatch.");
    }


    @Test
    @DisplayName("Get Opponent Groups - Multiple Distinct Adjacent Groups")
    void testGetOpponentGroupsMultipleDistinct() {
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell b1 = findCell(board, 1, -1, 0); b1.stone = Stone.BLUE; // Adjacent to r1 (Group 1)
        Cell b2 = findCell(board, -1, 1, 0); b2.stone = Stone.BLUE; // Adjacent to r1 (Group 2, Cell 1)
        Cell b3 = findCell(board, -1, 0, 1); b3.stone = Stone.BLUE; // Adjacent to r1 and b2 (Group 2, Cell 2)

        List<Cell> redGroup = List.of(r1);
        List<Cell> expectedOpponentGroup1 = List.of(b1);
        List<Cell> expectedOpponentGroup2 = List.of(b2, b3);

        List<List<Cell>> opponentGroups = GameLogic.getOpponentGroups(redGroup, board, Stone.RED);

        assertEquals(2, opponentGroups.size(), "Should find two distinct opponent groups.");

        // Check if both expected groups are present (order doesn't matter)
        boolean foundGroup1 = false;
        boolean foundGroup2 = false;
        for (List<Cell> group : opponentGroups) {
            if (group.size() == 1 && group.contains(b1)) {
                foundGroup1 = true;
            } else if (group.size() == 2 && new HashSet<>(group).containsAll(expectedOpponentGroup2)) {
                foundGroup2 = true;
            }
        }
        assertTrue(foundGroup1, "Expected opponent group 1 (single cell) not found.");
        assertTrue(foundGroup2, "Expected opponent group 2 (two cells) not found.");
    }

    @Test
    @DisplayName("Get Opponent Groups - Player Group Touches Same Opponent Group Multiple Times")
    void testGetOpponentGroupsMultipleTouchesSameGroup() {
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell r2 = findCell(board, 1, 0, -1); r2.stone = Stone.RED; // Part of player group
        Cell b1 = findCell(board, 1, -1, 0); b1.stone = Stone.BLUE; // Adjacent to r1
        Cell b2 = findCell(board, 2, -1, -1); b2.stone = Stone.BLUE; // Adjacent to r2 and b1

        List<Cell> redGroup = GameLogic.getConnectedGroup(r1, board, Stone.RED); // Contains r1, r2
        List<Cell> expectedOpponentGroup = List.of(b1, b2);

        List<List<Cell>> opponentGroups = GameLogic.getOpponentGroups(redGroup, board, Stone.RED);

        assertEquals(1, opponentGroups.size(), "Should find only one distinct opponent group, even if touched multiple times.");
        assertCellListsEqualUnordered(expectedOpponentGroup, opponentGroups.get(0), "Opponent group members mismatch when touched multiple times.");
    }

    @Test
    @DisplayName("Get Opponent Groups - No Opponents")
    void testGetOpponentGroupsNone() {
        Cell red1 = findCell(board, 0, 0, 0); red1.stone = Stone.RED;
        List<Cell> redGroup = List.of(red1);
        List<List<Cell>> opponentGroups = GameLogic.getOpponentGroups(redGroup, board, Stone.RED);
        assertTrue(opponentGroups.isEmpty(), "Should find no opponent groups if none are adjacent.");
    }

    @Test
    @DisplayName("Get Opponent Groups - Only Non-Adjacent Opponents")
    void testGetOpponentGroupsNonAdjacent() {
        Cell red1 = findCell(board, 0, 0, 0); red1.stone = Stone.RED;
        Cell blue1 = findCell(board, 2, 0, -2); blue1.stone = Stone.BLUE; // Not adjacent
        List<Cell> redGroup = List.of(red1);
        List<List<Cell>> opponentGroups = GameLogic.getOpponentGroups(redGroup, board, Stone.RED);
        assertTrue(opponentGroups.isEmpty(), "Should find no opponent groups if opponents are not adjacent.");
    }

    @Test
    @DisplayName("Get Opponent Groups - Null Inputs")
    void testGetOpponentGroupsNulls() {
        Cell red1 = findCell(board, 0, 0, 0); red1.stone = Stone.RED;
        List<Cell> redGroup = List.of(red1);
        assertThrows(NullPointerException.class, () -> GameLogic.getOpponentGroups(null, board, Stone.RED));
        assertThrows(NullPointerException.class, () -> GameLogic.getOpponentGroups(redGroup, null, Stone.RED));
        assertThrows(NullPointerException.class, () -> GameLogic.getOpponentGroups(redGroup, board, null));
        assertTrue(GameLogic.getOpponentGroups(new ArrayList<>(), board, Stone.RED).isEmpty(), "Should return empty for empty input group.");
    }


    // --- isMoveLegal Tests ---
    @Test
    @DisplayName("Is Move Legal - First Move")
    void testIsMoveLegalFirstMove() {
        Cell target = findCell(board, 0, 0, 0);
        assertTrue(GameLogic.isMoveLegal(target, board, Stone.RED), "First move for RED on empty center should be legal.");
        target.stone = Stone.RED; // Place first stone
        Cell targetBlue = findCell(board, 1, 0, -1);
        assertTrue(GameLogic.isMoveLegal(targetBlue, board, Stone.BLUE), "First move for BLUE on empty cell should be legal.");
    }

    @Test
    @DisplayName("Is Move Legal - Occupied Cell")
    void testIsMoveLegalOccupied() {
        Cell target = findCell(board, 0, 0, 0); target.stone = Stone.RED;
        assertFalse(GameLogic.isMoveLegal(target, board, Stone.BLUE), "Move on occupied cell should be illegal.");
        assertFalse(GameLogic.isMoveLegal(target, board, Stone.RED), "Move on occupied cell should be illegal.");
    }

    @Test
    @DisplayName("Is Move Legal - Isolated Placement (Legal)")
    void testIsMoveLegalIsolated() {
        findCell(board, 0, 0, 0).stone = Stone.RED; // Existing red stone
        findCell(board, 0, 2, -2).stone = Stone.BLUE; // Existing blue stone far away
        Cell target = findCell(board, 2, 0, -2); // Target cell not adjacent to red
        assertTrue(GameLogic.isMoveLegal(target, board, Stone.RED), "Placing RED isolated from other RED stones should be legal.");
    }

    @Test
    @DisplayName("Is Move Legal - Connecting Placement (Illegal without Capture)")
    void testIsMoveLegalConnectingNoCapture() {
        findCell(board, 0, 0, 0).stone = Stone.RED; // Existing red stone
        Cell target = findCell(board, 1, -1, 0); // Adjacent empty cell
        assertFalse(GameLogic.isMoveLegal(target, board, Stone.RED), "Placing RED adjacent to existing RED without capture should be illegal.");
    }

    @Test
    @DisplayName("Is Move Legal - Placement Causes Capture (Legal)")
    void testIsMoveLegalCausesCapture() {
        // Setup: R B R surrounding a single B
        //   R(0,0) B(1,-1) R(2,-2)
        //      B(1,0) -> target for RED capture
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell b1 = findCell(board, 1, -1, 0); b1.stone = Stone.BLUE;
        Cell r2 = findCell(board, 2, -2, 0); r2.stone = Stone.RED;
        Cell target = findCell(board, 1, 0, -1); // Empty cell adjacent to b1

        // Placing RED at target should make the red group (r1, target, r2) size 3
        // which is larger than the blue group (b1) size 1.
        assertTrue(GameLogic.isMoveLegal(target, board, Stone.RED), "Placing RED that causes capture should be legal.");
    }

    @Test
    @DisplayName("Is Move Legal - Capture Condition Not Met (Group Size)")
    void testIsMoveLegalCaptureSizeFail() {
        // Setup: R B B R surrounding empty cell (target)
        // R(0,0) B(1,-1) B(0,-1) R(1,-2) target(0,-2)
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell b1 = findCell(board, 1, -1, 0); b1.stone = Stone.BLUE;
        Cell b2 = findCell(board, 0, -1, 1); b2.stone = Stone.BLUE; // b1, b2 form group size 2
        Cell r2 = findCell(board, 1,-2, 1); r2.stone = Stone.RED; // Adjacent to b2
        Cell target = findCell(board, 0,-2, 2); // Target adjacent to b2 and r2

        // Placing RED at target connects to r2 (group size 2)
        // Opponent group (b1, b2) has size 2.
        // Since 2 is not > 2, capture fails.
        // Since placement connects to r2, it's illegal.
        assertFalse(GameLogic.isMoveLegal(target, board, Stone.RED), "Placement connecting to friendly, where potential capture fails size check, should be illegal.");
    }

    @Test
    @DisplayName("Is Move Legal - Connecting Placement Causes Capture (Legal) - FIXED TEST")
    void testIsMoveLegalConnectingCausesCapture() { // Renamed original, fixed setup
        // Setup: R R adjacent to B. Target connects R R and is adjacent to B.
        // R(0,0) R(1,0) B(0,-1) Target(1,-1)
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell r2 = findCell(board, 1, 0, -1); r2.stone = Stone.RED; // Forms group size 2 with r1
        Cell b1 = findCell(board, 0, -1, 1); b1.stone = Stone.BLUE; // Group size 1
        Cell target = findCell(board, 1, -1, 0); // Adjacent to r1, r2, b1

        // Placing RED at target connects to r1 and r2 (potential group size 3)
        // Adjacent opponent group b1 has size 1.
        // 3 > 1, so capture should occur. Move must be legal.
        assertTrue(GameLogic.isMoveLegal(target, board, Stone.RED), "Connecting placement that causes capture should be legal.");
    }


    // --- processMove Tests ---
    @Test
    @DisplayName("Process Move - Simple Placement")
    void testProcessMoveSimple() {
        Cell target = findCell(board, 0, 0, 0);
        int result = GameLogic.processMove(target, board, Stone.RED);
        assertEquals(1, result, "Simple placement should return 1.");
        assertEquals(Stone.RED, target.stone, "Cell should contain RED stone after placement.");
    }

    @Test
    @DisplayName("Process Move - Placement Causes Capture")
    void testProcessMoveCausesCapture() {
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell b1 = findCell(board, 1, -1, 0); b1.stone = Stone.BLUE;
        Cell r2 = findCell(board, 2, -2, 0); r2.stone = Stone.RED;
        Cell target = findCell(board, 1, 0, -1); // Target for RED capture

        int result = GameLogic.processMove(target, board, Stone.RED);
        assertEquals(2, result, "Placement causing capture should return 2.");
        assertEquals(Stone.RED, target.stone, "Target cell should contain RED stone.");
        assertNull(b1.stone, "Captured BLUE stone should be removed.");
        assertEquals(Stone.RED, r1.stone); // Ensure other stones remain
        assertEquals(Stone.RED, r2.stone);
    }

    @Test
    @DisplayName("Process Move - Occupied Cell")
    void testProcessMoveOccupied() {
        Cell target = findCell(board, 0, 0, 0); target.stone = Stone.RED;
        int result = GameLogic.processMove(target, board, Stone.BLUE);
        assertEquals(0, result, "Processing move on occupied cell should return 0.");
        assertEquals(Stone.RED, target.stone, "Occupied cell stone should not change.");
    }


    // --- getCapturableCells Tests ---
    @Test
    @DisplayName("Get Capturable Cells - Simple Case")
    void testGetCapturableCellsSimple() {
        // R R adjacent to B -> B is capturable by RED
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell r3 = findCell(board, -1, 1, 0); r3.stone = Stone.RED; // r1, r3 group size 2
        Cell b1 = findCell(board, 1, -1, 0); b1.stone = Stone.BLUE; // Group size 1, adjacent to r1

        List<Cell> capturable = GameLogic.getCapturableCells(board, Stone.RED); // Check captures for RED
        assertEquals(1, capturable.size(), "Should find 1 capturable BLUE cell.");
        assertTrue(capturable.contains(b1), "The single BLUE stone should be capturable.");
    }

    @Test
    @DisplayName("Get Capturable Cells - No Stones")
    void testGetCapturableCellsEmpty() {
        List<Cell> capturable = GameLogic.getCapturableCells(board, Stone.RED);
        assertTrue(capturable.isEmpty());
        capturable = GameLogic.getCapturableCells(board, Stone.BLUE);
        assertTrue(capturable.isEmpty());
    }

    @Test
    @DisplayName("Get Capturable Cells - Multiple Groups")
    void testGetCapturableCellsMultiple() {
        // R R adjacent to B B (Red=2, Blue=2 -> not capturable)
        Cell r1 = findCell(board, 0, 0, 0); r1.stone = Stone.RED;
        Cell r2 = findCell(board, 1, -1, 0); r2.stone = Stone.RED; // Group size 2
        Cell b1 = findCell(board, 0, 1, -1); b1.stone = Stone.BLUE; // Adjacent to r1
        Cell b2 = findCell(board, 1, 0, -1); b2.stone = Stone.BLUE; // Adjacent to r1, r2, b1. Group size 2 with b1.

        // R3 adjacent to B3 (Red=1, Blue=1 -> not capturable)
        Cell r3 = findCell(board, -2, 0, 2); r3.stone = Stone.RED; // Group size 1
        Cell b3 = findCell(board, -2, 1, 1); b3.stone = Stone.BLUE; // Adjacent to r3. Group size 1.

        // R4 R5 adjacent to B4 (Red=2, Blue=1 -> capturable)
        Cell r4 = findCell(board, 0, -2, 2); r4.stone = Stone.RED;
        Cell r5 = findCell(board, 1, -2, 1); r5.stone = Stone.RED; // Group size 2 with r4
        Cell b4 = findCell(board, 0, -1, 1); b4.stone = Stone.BLUE; // Adjacent to r4. Group size 1.

        List<Cell> capturable = GameLogic.getCapturableCells(board, Stone.RED);
        assertEquals(1, capturable.size(), "Should find exactly 1 capturable BLUE cell (b4).");
        assertTrue(capturable.contains(b4), "Only b4 should be capturable by RED.");

        // Check for BLUE captures
        capturable = GameLogic.getCapturableCells(board, Stone.BLUE);
        assertTrue(capturable.isEmpty(), "BLUE should not be able to capture any RED stones.");
    }

}
