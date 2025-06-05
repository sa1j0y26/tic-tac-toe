import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GameBoard {
    private static final int BOARD_SIZE = 3;
    private List<List<List<Piece>>> board;
    private Map<Integer, List<List<Piece>>> playerPieces;

    public GameBoard() {
        initializeBoard();
        initializePieces();
    }

    private void initializeBoard() {
        board = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            List<List<Piece>> row = new ArrayList<>();
            for (int j = 0; j < BOARD_SIZE; j++) {
                row.add(new ArrayList<>());
            }
            board.add(row);
        }
    }

    private void initializePieces() {
        playerPieces = new HashMap<>();
        for (int player = 1; player <= 2; player++) {
            List<List<Piece>> piecesList = new ArrayList<>();
            for (int size = 1; size <= 3; size++) {
                List<Piece> pieces = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    pieces.add(new Piece(size, player));
                }
                piecesList.add(pieces);
            }
            playerPieces.put(player, piecesList);
        }
    }

    public boolean canPlacePiece(int x, int y, Piece piece) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) return false;
        if (!hasPiece(piece.getOwner(), piece.getSize())) return false;
        List<Piece> stack = board.get(y).get(x);
        if (stack.size() >= 3) return false;
        if (stack.size() > 0 && stack.get(stack.size() - 1).getSize() >= piece.getSize()) return false;
        return true;
    }

    public boolean canMovePiece(int fromX, int fromY, int toX, int toY, int player) {
        if (fromX < 0 || fromX >= BOARD_SIZE || fromY < 0 || fromY >= BOARD_SIZE || toX < 0 || toX >= BOARD_SIZE || toY < 0 || toY >= BOARD_SIZE) return false;
        List<Piece> fromStack = board.get(fromY).get(fromX);
        if (fromStack.isEmpty()) return false;
        Piece top = fromStack.get(fromStack.size() - 1);
        if (top.getOwner() != player) return false;
        List<Piece> toStack = board.get(toY).get(toX);
        if (toStack.size() >= 3) return false;
        if (toStack.size() > 0 && toStack.get(toStack.size() - 1).getSize() >= top.getSize()) return false;
        return true;
    }

    public void placePiece(int x, int y, Piece piece) {
        board.get(y).get(x).add(piece);
        usePiece(piece.getOwner(), piece.getSize());
    }

    public void movePiece(int fromX, int fromY, int toX, int toY) {
        board.get(toY).get(toX).add(board.get(fromY).get(fromX).remove(board.get(fromY).get(fromX).size() - 1));
    }

    public String getBoardState() {
        StringBuilder boardState = new StringBuilder();
        boardState.append("BOARD\n");
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                List<Piece> stack = board.get(y).get(x);
                if (stack.isEmpty()) {
                    boardState.append("0 ");
                } else {
                    Piece top = stack.get(stack.size() - 1);
                    boardState.append(top.getOwner()).append(top.getSize()).append(" ");
                }
            }
            boardState.append("\n");
        }
        return boardState.toString();
    }

    private int getTopOwner(int x, int y) {
        List<Piece> stack = board.get(y).get(x);
        if (stack.isEmpty()) return 0;
        return stack.get(stack.size() - 1).getOwner();
    }

    public int checkWinner() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            int rowOwner = getTopOwner(0, i);
            boolean rowWin = rowOwner > 0;
            for (int j = 1; j < BOARD_SIZE; j++) {
                if (getTopOwner(j, i) != rowOwner) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin) return rowOwner;
            int colOwner = getTopOwner(i, 0);
            boolean colWin = colOwner > 0;
            for (int j = 1; j < BOARD_SIZE; j++) {
                if (getTopOwner(i, j) != colOwner) {
                    colWin = false;
                    break;
                }
            }
            if (colWin) return colOwner;
        }
        int diagOwner1 = getTopOwner(0, 0);
        boolean diagWin1 = diagOwner1 > 0;
        for (int i = 1; i < BOARD_SIZE; i++) {
            if (getTopOwner(i, i) != diagOwner1) {
                diagWin1 = false;
                break;
            }
        }
        if (diagWin1) return diagOwner1;
        int diagOwner2 = getTopOwner(BOARD_SIZE - 1, 0);
        boolean diagWin2 = diagOwner2 > 0;
        for (int i = 1; i < BOARD_SIZE; i++) {
            if (getTopOwner(BOARD_SIZE - 1 - i, i) != diagOwner2) {
                diagWin2 = false;
                break;
            }
        }
        if (diagWin2) return diagOwner2;
        return -1;
    }

    public boolean hasPiece(int player, int size) {
        List<List<Piece>> pieces = playerPieces.get(player);
        int idx = size - 1;
        if (idx < 0 || idx >= pieces.size()) return false;
        return !pieces.get(idx).isEmpty();
    }

    public void usePiece(int player, int size) {
        List<List<Piece>> pieces = playerPieces.get(player);
        int idx = size - 1;
        if (idx < 0 || idx >= pieces.size()) return;
        if (!pieces.get(idx).isEmpty()) {
            pieces.get(idx).remove(0);
        }
    }

    public Map<Integer, Integer> getPlayerPiecesInfo(int player) {
        List<List<Piece>> pieces = playerPieces.get(player);
        Map<Integer, Integer> info = new HashMap<>();
        for (int size = 1; size <= 3; size++) {
            int idx = size - 1;
            if (idx >= 0 && idx < pieces.size()) {
                info.put(size, pieces.get(idx).size());
            } else {
                info.put(size, 0);
            }
        }
        return info;
    }
} 
