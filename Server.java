import java.net.*;
import java.util.*;
import java.io.*;

public class Server {
    private static final int PORT = 5050;
    private ServerSocket serverSocket;
    GameBoard gameBoard;
    private List<ClientHandler> clients;
    private int currentTurn = 1;
    private boolean gameOver = false;
    protected List<String> chatHistory = Collections.synchronizedList(new ArrayList<>());

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            gameBoard = new GameBoard();
            clients = new ArrayList<>();
            System.out.println("サーバーが起動しました。ポート: " + PORT);
        } catch (IOException e) {
            System.out.println("サーバーの起動に失敗しました: " + e.getMessage());
        }
    }

    public void start() {
        try {
            for (int i = 0; i < 2; i++) {
                System.out.println("クライアントの接続を待っています...");
                Socket clientSocket = serverSocket.accept();
                int playerId = clients.size() + 1;
                System.out.println("クライアントが接続しました: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, playerId, this);
                clients.add(handler);
                handler.start();
            }
            broadcast("ゲーム開始！");
            broadcastBoard();
            broadcastTurn();
        } catch (IOException e) {
            System.out.println("クライアントとの通信でエラーが発生しました: " + e.getMessage());
        }
    }

    public synchronized void broadcast(String message){
        for(ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized void broadcastBoard() {
        String boardState = gameBoard.getBoardState();
        for (ClientHandler client : clients) {
            client.sendMessage(boardState);
        }
    }

    public synchronized void broadcastTurn() {
        broadcast("現在のターン: プレイヤー " + currentTurn);
    }

    public synchronized int getCurrentTurn(){
        return currentTurn;
    }

    public synchronized void switchTurn(){
        currentTurn = (currentTurn == 1) ? 2 : 1;
        broadcastTurn();
    }

    public synchronized boolean isGameOver(){
        return gameOver;
    }

    public synchronized void setGameOver(boolean over){
        gameOver = over;
    }

    public synchronized void addChat(String msg) {
        chatHistory.add(msg);
    }

    public synchronized void endGame(int winner) {
        if (!gameOver) {
            broadcast("WINNER " + winner);
            setGameOver(true);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private int playerId;
    private Server server;
    private GameBoard gameBoard;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, int playerId, Server server) {
        this.socket = socket;
        this.playerId = playerId;
        this.server = server;
        this.gameBoard = server.gameBoard;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("プレイヤー" + playerId + " として接続しました。");
            sendPlayerPiecesInfo();
            while (!server.isGameOver()) {
                String input = in.readLine();
                if (input == null) break;
                input = input.trim();
                if (input.isEmpty()) continue;
                if (input.startsWith("/chat ")) {
                    String chatMsg = "プレイヤー" + playerId + ": " + input.substring(6);
                    server.addChat(chatMsg);
                    server.broadcast("CHAT:" + chatMsg);
                } else if (server.getCurrentTurn() == playerId) {
                    processGameCommand(input);
                } else {
                    out.println("現在はあなたの番ではありません。");
                }
            }
        } catch (IOException e) {
            System.out.println("プレイヤー " + playerId + " との通信でエラー: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) { }
        }
    }

    private void processGameCommand(String input) {
        String[] parts = input.split(" ");
        String command = parts[0].toUpperCase();
        switch (command) {
            case "PLACE":
                handlePlace(parts);
                break;
            case "MOVE":
                handleMove(parts);
                break;
            default:
                out.println("不明なコマンドです。PLACE x y size / MOVE fromX fromY toX toY");
        }
    }

    private void sendPlayerPiecesInfo() {
        Map<Integer, Integer> info = gameBoard.getPlayerPiecesInfo(playerId);
        StringBuilder sb = new StringBuilder();
        sb.append("PIECES:");
        for (int size = 1; size <= 3; size++) {
            if (size > 1) sb.append(",");
            sb.append(size).append(":").append(info.getOrDefault(size, 0));
        }
        sendMessage(sb.toString());
    }

    private void handlePlace(String[] parts) {
        if (parts.length != 4) {
            out.println("PLACEコマンドの形式: PLACE x y size");
            return;
        }
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int size = Integer.parseInt(parts[3]);
            Piece piece = new Piece(size, playerId);
            synchronized (gameBoard) {
                if (gameBoard.canPlacePiece(x, y, piece)) {
                    gameBoard.placePiece(x, y, piece);
                    server.broadcastBoard();
                    sendPlayerPiecesInfo();
                    int winner = gameBoard.checkWinner();
                    if (winner > 0) {
                        server.endGame(winner);
                        return;
                    } else {
                        server.switchTurn();
                    }
                } else {
                    out.println("その位置にはコマを置けません。");
                }
            }
        } catch (NumberFormatException e) {
            out.println("引数が正しくありません。数値で指定してください。");
        }
    }

    private void handleMove(String[] parts) {
        if (parts.length != 5) {
            out.println("MOVEコマンドの形式: MOVE fromX fromY toX toY");
            return;
        }
        try {
            int fromX = Integer.parseInt(parts[1]);
            int fromY = Integer.parseInt(parts[2]);
            int toX = Integer.parseInt(parts[3]);
            int toY = Integer.parseInt(parts[4]);
            synchronized (gameBoard) {
                if (gameBoard.canMovePiece(fromX, fromY, toX, toY, playerId)) {
                    gameBoard.movePiece(fromX, fromY, toX, toY);
                    server.broadcastBoard();
                    sendPlayerPiecesInfo();
                    int winner = gameBoard.checkWinner();
                    if (winner > 0) {
                        server.endGame(winner);
                        return;
                    } else {
                        server.switchTurn();
                    }
                } else {
                    out.println("その移動は無効です。");
                }
            }
        } catch (NumberFormatException e) {
            out.println("引数が正しくありません。数値で指定してください。");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}