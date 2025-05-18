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
        // TODO: ボードの状態を文字列で返す処理を実装
        String boardState = "";
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                for (Piece piece : board.get(y).get(x)) {
                    boardState += piece.getSize() + " ";
                }
            }
        }
        return boardState;
    }
} 