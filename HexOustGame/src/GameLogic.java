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
 * @author [Group 34 WheMurPap]
 */
public final class GameLogic {

    /**
     * Defines the six possible axial direction vectors for moving between adjacent hexagons.
     * Each inner array represents {dq, dr, ds}.
     */
    private static final int[][] AXIAL_DIRECTIONS = {
            {+1,  0, -1}, {+1, -1,  0}, { 0, -1, +1},
            {-1,  0, +1}, {-1, +1,  0}, { 0, +1, -1}
    };

    /** Private constructor to prevent instantiation of this utility class. */
    private GameLogic() {
        throw new UnsupportedOperationException("GameLogic is a utility class and cannot be instantiated.");
    }

    /**
     * Finds all valid neighboring cells for a given cell on the board.
     * A neighbor is a cell directly adjacent in one of the six hexagonal directions
     * and exists on the board.
     * @param cell The central cell. Must not be null.
     * @param board The game board. Must not be null.
     * @return A List of neighboring Cell objects. Returns an empty list if no neighbors are found
     * or if the input cell is invalid.
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
     * A group consists of the start cell and all cells of the same color
     * reachable from the start cell by stepping between adjacent cells of that color.
     *
     * @param startCell The starting cell of the group. Must not be null and must contain a stone of the specified color.
     * @param board The game board. Must not be null.
     * @param color The color of the group to find. Must not be null.
     * @return A List of cells belonging to the connected group, including the startCell.
     * Returns an empty list if the startCell does not contain a stone of the specified color.
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
     * Uses {@link #getConnectedGroup(Cell, Board, Stone)} to identify the opponent groups.
     *
     * @param sameColorGroup A list of cells forming a connected group of the player's color. Must not be null.
     * @param board The game board. Must not be null.
     * @param playerColor The color of the player's group. Must not be null.
     * @return A List of Lists, where each inner list represents a distinct connected group of opponent stones
     * that is adjacent to the player's group. Returns an empty list if no such opponent groups exist.
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
        Set<Cell> includedOpponentCells = new HashSet<>(); // Track opponent cells already part of a found group

        for (Cell playerCell : sameColorGroup) {
            for (Cell neighbor : getNeighbors(playerCell, board)) {
                // Check if the neighbor is an opponent stone and hasn't been included in a group yet
                if (neighbor.stone == opponentColor && !includedOpponentCells.contains(neighbor)) {
                    // Find the complete connected group this opponent neighbor belongs to
                    List<Cell> newOpponentGroup = getConnectedGroup(neighbor, board, opponentColor);
                    if (!newOpponentGroup.isEmpty()) {
                        opponentGroups.add(newOpponentGroup);
                        // Add all cells from this newly found group to the set to avoid processing them again
                        includedOpponentCells.addAll(newOpponentGroup);
                    }
                }
            }
        }
        return opponentGroups;
    }

    /**
     * Checks if placing a stone of the given color on the specified target cell is a legal move
     * according to HexOust rules:
     * 1. The target cell must be empty.
     * 2. If it's the player's first move, it's always legal on an empty cell.
     * 3. If placing the stone results in a capture (player's new group size > adjacent opponent group size), it's legal.
     * 4. If placing the stone does NOT result in a capture, it is only legal if the target cell is NOT adjacent
     * to any existing friendly stones.
     * This method simulates the move to check conditions 3 and 4.
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

        // Rule 1: Must be empty
        if (targetCell.stone != null) {
            return false;
        }

        // Rule 2a: First move is always legal (on an empty cell)
        if (board.countStones(playerColor) == 0) {
            return true;
        }

        // --- Simulate the move ---
        targetCell.stone = playerColor; // Temporarily place the stone
        List<Cell> potentialOwnGroup = getConnectedGroup(targetCell, board, playerColor);
        boolean resultsInCapture = false;

        // Check for captures only if a valid group is formed (should always be size >= 1 here)
        if (!potentialOwnGroup.isEmpty()) {
            List<List<Cell>> adjacentOpponentGroups = getOpponentGroups(potentialOwnGroup, board, playerColor);
            for (List<Cell> opponentGroup : adjacentOpponentGroups) {
                // Capture occurs if player group is strictly larger than the opponent group
                if (!opponentGroup.isEmpty() && potentialOwnGroup.size() > opponentGroup.size()) {
                    resultsInCapture = true;
                    break; // Found a capture, no need to check further opponent groups
                }
            }
        } else {
            // This case should technically not happen if getConnectedGroup works correctly,
            // as the targetCell itself should form a group of at least 1.
            System.err.println("Warning: potentialOwnGroup was empty during isMoveLegal simulation for " + targetCell);
        }
        targetCell.stone = null; // --- Revert the simulation ---

        // Rule 2b: If capture results, the move is legal
        if (resultsInCapture) {
            return true;
        }

        // Rule 2c: If no capture, check for connection to friendly stones
        boolean isConnectedToFriendly = false;
        for (Cell neighbor : getNeighbors(targetCell, board)) {
            if (neighbor.stone == playerColor) {
                isConnectedToFriendly = true;
                break; // Found a friendly neighbor
            }
        }

        // If no capture occurred, the move is legal ONLY if it's NOT connected to friendly stones
        return !isConnectedToFriendly;
    }

    /**
     * Processes a move by placing a stone and handling any resulting captures.
     * Assumes the move has already been validated as legal by {@link #isMoveLegal(Cell, Board, Stone)},
     * but includes a basic check for occupied cell as a safeguard.
     *
     * @param targetCell The cell where the stone is placed. Must not be null and should ideally be empty.
     * @param board The game board. Must not be null.
     * @param playerColor The color of the stone being placed. Must not be null.
     * @return An integer indicating the result:
     * <ul>
     * <li>{@code 2}: Stone placed and one or more opponent groups were captured.</li>
     * <li>{@code 1}: Stone placed successfully without any captures.</li>
     * <li>{@code 0}: Move was invalid (e.g., target cell occupied - defensive check).</li>
     * </ul>
     * @throws NullPointerException if any input parameter is null.
     */
    public static int processMove(Cell targetCell, Board board, Stone playerColor) {
        if (targetCell == null) throw new NullPointerException("Target cell cannot be null.");
        if (board == null) throw new NullPointerException("Board cannot be null.");
        if (playerColor == null) throw new NullPointerException("Player color cannot be null.");

        // Defensive check: Although isMoveLegal should be called first, double-check occupation.
        if (targetCell.stone != null) {
            System.err.println("Warning: processMove called on occupied cell: " + targetCell);
            return 0; // Indicate invalid move
        }

        // --- Place the stone ---
        targetCell.stone = playerColor;
        List<Cell> ownGroup = getConnectedGroup(targetCell, board, playerColor);

        // Sanity check: Own group should not be empty after placing a stone.
        if (ownGroup.isEmpty()) {
            System.err.println("Critical Warning: ownGroup is empty after placement in processMove for cell: " + targetCell + ". Reverting placement.");
            targetCell.stone = null; // Revert placement if something went wrong
            return 0; // Indicate error/invalid state
        }

        // --- Check for and execute captures ---
        List<List<Cell>> opponentGroups = getOpponentGroups(ownGroup, board, playerColor);
        boolean captureOccurred = false;
        for (List<Cell> opponentGroup : opponentGroups) {
            // Capture condition: player's group size > opponent's group size
            if (!opponentGroup.isEmpty() && ownGroup.size() > opponentGroup.size()) {
                // Remove stones from the captured opponent group
                for (Cell capturedCell : opponentGroup) {
                    capturedCell.stone = null;
                }
                captureOccurred = true; // Mark that at least one capture happened
            }
        }

        // Return result code
        return captureOccurred ? 2 : 1;
    }

    /**
     * Finds all opponent cells that are part of a group currently capturable by the player.
     * A group is capturable if it's adjacent to a friendly group that is strictly larger.
     * This method identifies the individual cells belonging to such capturable groups.
     * Note: This identifies potentially capturable groups based on the *current* board state,
     * primarily used for UI highlighting. A capture action itself might involve placing a stone first
     * (handled by processMove) or directly removing a stone from a capturable group (handled in UI logic).
     *
     * @param board The game board. Must not be null.
     * @param playerColor The color of the player looking to capture. Must not be null.
     * @return A List of opponent {@link Cell} objects that belong to a capturable group.
     * Returns an empty list if no opponent cells are currently capturable.
     * @throws NullPointerException if board or playerColor is null.
     */
    public static List<Cell> getCapturableCells(Board board, Stone playerColor) {
        if (board == null) throw new NullPointerException("Board cannot be null.");
        if (playerColor == null) throw new NullPointerException("Player color cannot be null.");

        List<Cell> capturableOpponentCells = new ArrayList<>();
        // Keep track of opponent cells already identified as capturable to avoid duplicates
        Set<Cell> addedOpponentCells = new HashSet<>();
        // Keep track of player cells already processed as part of a group to avoid redundant checks
        Set<Cell> processedPlayerCells = new HashSet<>();
        Stone opponentColor = (playerColor == Stone.RED) ? Stone.BLUE : Stone.RED;

        // Iterate through all cells on the board
        for (Cell cell : board.getCells()) {
            // If it's a player stone and hasn't been processed as part of a group yet
            if (cell.stone == playerColor && !processedPlayerCells.contains(cell)) {
                // Find the full group this player stone belongs to
                List<Cell> playerGroup = getConnectedGroup(cell, board, playerColor);
                if (playerGroup.isEmpty()) {
                    // Should not happen if cell.stone == playerColor, but check defensively
                    continue;
                }
                // Mark all cells in this player group as processed
                processedPlayerCells.addAll(playerGroup);

                // Find all distinct opponent groups adjacent to this player group
                List<List<Cell>> adjacentOpponentGroups = getOpponentGroups(playerGroup, board, playerColor);

                // Check each adjacent opponent group for capturability
                for (List<Cell> opponentGroup : adjacentOpponentGroups) {
                    // Capture condition: player group size > opponent group size
                    if (!opponentGroup.isEmpty() && playerGroup.size() > opponentGroup.size()) {
                        // Add all cells from the capturable opponent group to the result list
                        // (if not already added)
                        for (Cell opponentCell : opponentGroup) {
                            if (addedOpponentCells.add(opponentCell)) { // .add() returns true if the element was not already present
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
