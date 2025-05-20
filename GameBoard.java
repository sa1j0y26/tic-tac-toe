import java.util.ArrayList;
import java.util.List;

public class GameBoard {
    private static final int BOARD_SIZE = 3;
    private List<List<List<Piece>>> board; // 3次元リスト：board[y][x][stack]で各マスのコマのスタックを管理
    private List<List<Piece>> player1Pieces; // プレイヤー1の持ち駒
    private List<List<Piece>> player2Pieces; // プレイヤー2の持ち駒

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
        // プレイヤー1の持ち駒初期化
        player1Pieces = new ArrayList<>();
        for (int size = 1; size <= 3; size++) {
            List<Piece> pieces = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                pieces.add(new Piece(size, 1));
            }
            player1Pieces.add(pieces);
        }

        // プレイヤー2の持ち駒初期化
        player2Pieces = new ArrayList<>();
        for (int size = 1; size <= 3; size++) {
            List<Piece> pieces = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                pieces.add(new Piece(size, 2));
            }
            player2Pieces.add(pieces);
        }
    }

    public boolean canPlacePiece(int x, int y, Piece piece) {
        // TODO: コマを置けるかどうかの判定ロジックを実装
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return false;
        }
        List<Piece> stack = board.get(y).get(x);
        if (stack.size() >= 3) {
            return false;
        }
        if (stack.size() > 0 && stack.get(stack.size() - 1).getSize() >= piece.getSize()) {
            return false;
        }else{
            return true;
        }
    }

    public boolean canMovePiece(int fromX, int fromY, int toX, int toY, int player) {
        // TODO: コマを移動できるかどうかの判定ロジックを実装
        if (fromX < 0 || fromX >= BOARD_SIZE || fromY < 0 || fromY >= BOARD_SIZE || toX < 0 || toX >= BOARD_SIZE || toY < 0 || toY >= BOARD_SIZE) {
            return false;
        }
        List<Piece> fromStack = board.get(fromY).get(fromX);
        if (fromStack.size() == 0) {
            return false;
        }
        Piece top = fromStack.get(fromStack.size() - 1);
        if (top.getOwner() != player) {
            return false;
        }
        List<Piece> toStack = board.get(toY).get(toX);
        if (toStack.size() >= 3) {
            return false;
        }
        if (toStack.size() > 0 && toStack.get(toStack.size() - 1).getSize() >= top.getSize()) {
            return false;
        }else{
            return true;
        }
    }

    public void placePiece(int x, int y, Piece piece) {
        // TODO: コマを置く処理を実装
        board.get(y).get(x).add(piece);
    }

    public void movePiece(int fromX, int fromY, int toX, int toY) {
        // TODO: コマを移動する処理を実装
        board.get(toY).get(toX).add(board.get(fromY).get(fromX).remove(board.get(fromY).get(fromX).size() - 1));
    }

    public String getBoardState() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                List<Piece> stack = board.get(y).get(x);
                if (stack.isEmpty()) {
                    sb.append("0");
                } else {
                    Piece top = stack.get(stack.size() - 1);
                    String mark = (top.getOwner() == 1) ? "o" : "x";
                    sb.append(top.getSize()).append(mark);
                }
                if (x < BOARD_SIZE - 1) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    

    private int getTopOwner(int x, int y) {
        List<Piece> stack = board.get(y).get(x);
        if (stack.size() == 0) return 0;
        return stack.get(stack.size() - 1).getOwner();
    }

    public int checkWinner() {
        // 横・縦
        for (int i = 0; i < BOARD_SIZE; i++) {
            // 横
            int rowOwner = getTopOwner(0, i);
            boolean rowWin = rowOwner > 0;
            for (int j = 1; j < BOARD_SIZE; j++) {
                if (getTopOwner(j, i) != rowOwner) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin) return rowOwner;
            // 縦
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
        // 斜め（左上→右下）
        int diagOwner1 = getTopOwner(0, 0);
        boolean diagWin1 = diagOwner1 > 0;
        for (int i = 1; i < BOARD_SIZE; i++) {
            if (getTopOwner(i, i) != diagOwner1) {
                diagWin1 = false;
                break;
            }
        }
        if (diagWin1) return diagOwner1;
        // 斜め（右上→左下）
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
} 
