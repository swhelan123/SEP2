import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class HexOustGameTest {

    // Helper method to find a cell by its cubic coordinates
    private Cell findCell(Board board, int q, int r, int s) {
        for (Cell cell : board.getCells()) {
            if (cell.q == q && cell.r == r && cell.s == s) {
                return cell;
            }
        }
        return null;
    }

    @Test
    public void testValidNonCapturingMove() {
        Board board = new Board(2);
        Cell cell = findCell(board, 0, 0, 0);
        assertNotNull(cell);
        int result = GameLogic.processMove(cell, board, Stone.RED);
        assertEquals(1, result);
        assertEquals(Stone.RED, cell.stone);
    }

    @Test
    public void testIllegalMoveRestricted() {
        Board board = new Board(2);
        Cell cellA = findCell(board, 0, 0, 0);
        assertNotNull(cellA);
        int resA = GameLogic.processMove(cellA, board, Stone.RED);
        assertEquals(1, resA);
        Cell cellB = findCell(board, 0, -1, 1);
        assertNotNull(cellB);
        int resB = GameLogic.processMove(cellB, board, Stone.RED);
        assertEquals(0, resB);
        assertNull(cellB.stone);
    }

    @Test
    public void testCaptureMove() {
        Board board = new Board(3);
        Cell redCell = findCell(board, 0, 0, 0);
        Cell blueCell = findCell(board, 1, -1, 0);
        assertNotNull(redCell);
        assertNotNull(blueCell);
        redCell.stone = Stone.RED;
        blueCell.stone = Stone.BLUE;
        Cell moveCell = findCell(board, 0, -1, 1);
        assertNotNull(moveCell);
        int result = GameLogic.processMove(moveCell, board, Stone.RED);
        assertEquals(2, result);
        assertNull(blueCell.stone);
    }

    @Test
    public void testAlternateTurnLogic() {
        Board board = new Board(2);
        Cell cellA = findCell(board, 0, 0, 0);
        assertNotNull(cellA);
        int resA = GameLogic.processMove(cellA, board, Stone.RED);
        assertEquals(1, resA);
        assertEquals(Stone.RED, cellA.stone);
        Cell cellB = findCell(board, 1, -1, 0);
        assertNotNull(cellB);
        int resB = GameLogic.processMove(cellB, board, Stone.BLUE);
        assertEquals(1, resB);
        assertEquals(Stone.BLUE, cellB.stone);
    }

    @Test
    public void testFirstStoneFreedom() {
        Board board = new Board(2);
        // first move anywhere should be legal, even adjacent
        Cell cell = findCell(board, 1, -1, 0);
        assertNotNull(cell);
        int result = GameLogic.processMove(cell, board, Stone.RED);
        assertEquals(1, result, "First stone must be legal regardless of adjacency.");
    }

    @Test
    public void testSecondStoneRestriction() {
        Board board = new Board(2);
        Cell first = findCell(board, 0, 0, 0);
        assertNotNull(first);
        assertEquals(1, GameLogic.processMove(first, board, Stone.RED));
        // second stone adjacent should be illegal
        Cell adj = findCell(board, 1, -1, 0);
        assertNotNull(adj);
        int result = GameLogic.processMove(adj, board, Stone.RED);
        assertEquals(0, result, "Second stone adjacent to first must be illegal.");
    }

    @Test
    public void testIllegalRePlacement() {
        Board board = new Board(2);
        Cell cell = findCell(board, 0, 0, 0);
        assertNotNull(cell);
        assertEquals(1, GameLogic.processMove(cell, board, Stone.RED));
        // attempt to place again on the same cell
        int second = GameLogic.processMove(cell, board, Stone.BLUE);
        assertEquals(0, second, "Cannot place a stone on an occupied cell.");
    }

    @Test
    public void testBoundaryHexes() {
        Board board = new Board(2);
        // extreme coordinate for radius=2: (2,0,-2)
        Cell edge = findCell(board, 2, 0, -2);
        assertNotNull(edge);
        int result = GameLogic.processMove(edge, board, Stone.RED);
        assertEquals(1, result, "Should be able to place on a corner hex.");
    }
}
