import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Contains the static methods implementing the core game logic for HexOust.
 * This includes finding neighbors, connected groups, checking move legality,
 * processing moves (placement and capture), and identifying capturable cells.
 * This is a utility class and cannot be instantiated.
 *
 * @author [Group 34 WheMurPap] // <-- Add your group name/members
 * @version 1.5 // Version incremented
 * @since 2025-05-05 // <-- Adjust date if needed
 */
public final class GameLogic {

    private static final int[][] AXIAL_DIRECTIONS = {
            {+1,  0, -1}, {+1, -1,  0}, { 0, -1, +1},
            {-1,  0, +1}, {-1, +1,  0}, { 0, +1, -1}
    };

    /** Private constructor to prevent instantiation. */
    private GameLogic() {
        throw new UnsupportedOperationException("GameLogic is a utility class and cannot be instantiated.");
    }

    /**
     * Finds all valid neighboring cells for a given cell on the board.
     * @param cell The central cell. Must not be null.
     * @param board The game board. Must not be null.
     * @return A List of neighboring Cell objects.
     * @throws NullPointerException if cell or board is null.
     */
    public static List<Cell> getNeighbors(Cell cell, Board board) {
        if (cell == null) throw new NullPointerException("Cell cannot be null for getNeighbors.");
        if (board == null) throw new NullPointerException("Board cannot be null for getNeighbors.");

        List<Cell> neighbors = new ArrayList<>();
        for (int[] dir : AXIAL_DIRECTIONS) {
            Cell neighborCell = board.getCell(cell.q + dir[0], cell.r + dir[1], cell.s + dir[2]);
            if (neighborCell != null) {
                neighbors.add(neighborCell);
            }
        }
        return neighbors;
    }

    /**
     * Finds all connected cells of the same color starting from a given cell,
     * using a Breadth-First Search (BFS) approach.
     * **FIXED v3:** Simplified BFS loop logic.
     *
     * @param startCell The starting cell of the group. Must not be null.
     * @param board The game board. Must not be null.
     * @param color The color of the group to find. Must not be null.
     * @return A List of cells belonging to the connected group.
     * @throws NullPointerException if startCell, board, or color is null.
     */
    public static List<Cell> getConnectedGroup(Cell startCell, Board board, Stone color) {
        if (startCell == null) throw new NullPointerException("Start cell cannot be null.");
        if (board == null) throw new NullPointerException("Board cannot be null.");
        if (color == null) throw new NullPointerException("Color cannot be null.");

        List<Cell> group = new ArrayList<>();
        if (startCell.stone != color) {
            return group; // Start cell must match the target color
        }

        Set<Cell> visited = new HashSet<>();
        Queue<Cell> frontier = new LinkedList<>();

        // Initialize queue and visited set with the start cell
        frontier.add(startCell);
        visited.add(startCell);

        while (!frontier.isEmpty()) {
            Cell current = frontier.poll(); // Dequeue
            group.add(current); // Add the dequeued cell to the result group

            for (Cell neighbor : getNeighbors(current, board)) {
                // If neighbor has the same color AND has not been visited yet
                if (neighbor.stone == color && !visited.contains(neighbor)) {
                    visited.add(neighbor); // Mark as visited
                    frontier.add(neighbor); // Enqueue for processing
                }
            }
        }
        return group;
    }


    /**
     * Finds all distinct groups of opponent stones that are adjacent to any cell
     * within a given group of same-colored stones.
     * (Logic relies on getConnectedGroup)
     *
     * @param sameColorGroup A list of cells forming a connected group of the player's color. Must not be null.
     * @param board The game board. Must not be null.
     * @param playerColor The color of the player's group. Must not be null.
     * @return A List of Lists, where each inner list represents a distinct connected group of opponent stones.
     * @throws NullPointerException if any input parameter is null.
     */
    public static List<List<Cell>> getOpponentGroups(List<Cell> sameColorGroup, Board board, Stone playerColor) {
        if (sameColorGroup == null) throw new NullPointerException("Player group cannot be null.");
        if (board == null) throw new NullPointerException("Board cannot be null.");
        if (playerColor == null) throw new NullPointerException("Player color cannot be null.");

        List<List<Cell>> opponentGroups = new ArrayList<>();
        if (sameColorGroup.isEmpty()) {
            return opponentGroups;
        }

        Stone opponentColor = (playerColor == Stone.RED) ? Stone.BLUE : Stone.RED;
        Set<Cell> includedOpponentCells = new HashSet<>();

        for (Cell playerCell : sameColorGroup) {
            for (Cell neighbor : getNeighbors(playerCell, board)) {
                if (neighbor.stone == opponentColor && !includedOpponentCells.contains(neighbor)) {
                    List<Cell> newOpponentGroup = getConnectedGroup(neighbor, board, opponentColor);
                    if (!newOpponentGroup.isEmpty()) {
                        opponentGroups.add(newOpponentGroup);
                        includedOpponentCells.addAll(newOpponentGroup);
                    }
                }
            }
        }
        return opponentGroups;
    }

    /**
     * Checks if placing a stone of the given color on the specified cell is a legal move
     * according to HexOust rules.
     * (Logic relies on getConnectedGroup and getOpponentGroups)
     *
     * @param targetCell The cell where the move is proposed. Must not be null.
     * @param board The game board. Must not be null.
     * @param playerColor The color of the stone to be placed. Must not be null.
     * @return {@code true} if the move is legal, {@code false} otherwise.
     * @throws NullPointerException if any input parameter is null.
     */
    public static boolean isMoveLegal(Cell targetCell, Board board, Stone playerColor) {
        if (targetCell == null) throw new NullPointerException("Target cell cannot be null.");
        if (board == null) throw new NullPointerException("Board cannot be null.");
        if (playerColor == null) throw new NullPointerException("Player color cannot be null.");

        if (targetCell.stone != null) {
            return false; // Rule 1: Must be empty
        }

        if (board.countStones(playerColor) == 0) {
            return true; // Rule 2a: First move is always legal
        }

        // Simulate move
        targetCell.stone = playerColor;
        List<Cell> potentialOwnGroup = getConnectedGroup(targetCell, board, playerColor);
        boolean resultsInCapture = false;
        // Important: Check if potentialOwnGroup is not empty before proceeding
        if (!potentialOwnGroup.isEmpty()) {
            List<List<Cell>> adjacentOpponentGroups = getOpponentGroups(potentialOwnGroup, board, playerColor);
            for (List<Cell> opponentGroup : adjacentOpponentGroups) {
                if (!opponentGroup.isEmpty() && potentialOwnGroup.size() > opponentGroup.size()) {
                    resultsInCapture = true;
                    break;
                }
            }
        } else {
            // If placing the stone didn't even form a group of size 1, something is wrong,
            // but it definitely didn't capture anything.
            // This case shouldn't happen if startCell.stone == color check works in getConnectedGroup.
            System.err.println("Warning: potentialOwnGroup was empty during isMoveLegal simulation for " + targetCell);
        }
        targetCell.stone = null; // Revert simulation

        // Rule 2b: If capture results, it's legal
        if (resultsInCapture) {
            return true;
        }

        // Rule 2c: If no capture, check for connection to friendly stones
        boolean isConnectedToFriendly = false;
        for (Cell neighbor : getNeighbors(targetCell, board)) {
            if (neighbor.stone == playerColor) {
                isConnectedToFriendly = true;
                break;
            }
        }

        // Legal only if NOT connected
        return !isConnectedToFriendly;
    }

    /**
     * Processes a move by placing a stone and handling any resulting captures.
     * @param targetCell The cell where the stone is placed. Must not be null.
     * @param board The game board. Must not be null.
     * @param playerColor The color of the stone being placed. Must not be null.
     * @return <ul>
     * <li>2 if placing the stone resulted in capturing one or more opponent groups.</li>
     * <li>1 if the stone was placed successfully without any captures.</li>
     * <li>0 if the move was invalid (e.g., target cell occupied - defensive check).</li>
     * </ul>
     * @throws NullPointerException if any input parameter is null.
     */
    public static int processMove(Cell targetCell, Board board, Stone playerColor) {
        // (No changes here)
        if (targetCell == null) throw new NullPointerException("Target cell cannot be null.");
        if (board == null) throw new NullPointerException("Board cannot be null.");
        if (playerColor == null) throw new NullPointerException("Player color cannot be null.");

        if (targetCell.stone != null) {
            System.err.println("Warning: processMove called on occupied cell: " + targetCell);
            return 0;
        }

        targetCell.stone = playerColor;
        List<Cell> ownGroup = getConnectedGroup(targetCell, board, playerColor);

        if (ownGroup.isEmpty()) {
            System.err.println("Warning: ownGroup is empty after placement in processMove for cell: " + targetCell);
            targetCell.stone = null; // Revert placement
            return 0;
        }

        List<List<Cell>> opponentGroups = getOpponentGroups(ownGroup, board, playerColor);
        boolean captureOccurred = false;
        for (List<Cell> opponentGroup : opponentGroups) {
            if (!opponentGroup.isEmpty() && ownGroup.size() > opponentGroup.size()) {
                for (Cell capturedCell : opponentGroup) {
                    capturedCell.stone = null;
                }
                captureOccurred = true;
            }
        }
        return captureOccurred ? 2 : 1;
    }

    /**
     * Finds all opponent cells that are currently capturable by the player clicking on them directly.
     * (Logic relies on getConnectedGroup and getOpponentGroups)
     *
     * @param board The game board. Must not be null.
     * @param playerColor The color of the player looking to capture. Must not be null.
     * @return A List of opponent cells that belong to a capturable group.
     * @throws NullPointerException if board or playerColor is null.
     */
    public static List<Cell> getCapturableCells(Board board, Stone playerColor) {
        // (No changes here)
        if (board == null) throw new NullPointerException("Board cannot be null.");
        if (playerColor == null) throw new NullPointerException("Player color cannot be null.");

        List<Cell> capturableOpponentCells = new ArrayList<>();
        Set<Cell> addedOpponentCells = new HashSet<>();
        Set<Cell> processedPlayerCells = new HashSet<>();

        for (Cell cell : board.getCells()) {
            if (cell.stone == playerColor && !processedPlayerCells.contains(cell)) {
                List<Cell> playerGroup = getConnectedGroup(cell, board, playerColor);
                if (playerGroup.isEmpty()) continue;
                processedPlayerCells.addAll(playerGroup);

                List<List<Cell>> adjacentOpponentGroups = getOpponentGroups(playerGroup, board, playerColor);

                for (List<Cell> opponentGroup : adjacentOpponentGroups) {
                    if (!opponentGroup.isEmpty() && playerGroup.size() > opponentGroup.size()) {
                        for (Cell opponentCell : opponentGroup) {
                            if (addedOpponentCells.add(opponentCell)) {
                                capturableOpponentCells.add(opponentCell);
                            }
                        }
                    }
                }
            }
        }
        return capturableOpponentCells;
    }
}
