import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class HexOustGameTest {

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
}
