import java.net.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class Server {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private GameBoard gameBoard;
    private List<ClientHandler> clients;

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
            while (clients.size() < 2) {
                System.out.println("クライアントの接続を待っています...");
                Socket clientSocket = serverSocket.accept();
                int playerId = clients.size() + 1;
                System.out.println("クライアントが接続しました: " + clientSocket.getInetAddress());

                // TODO: クライアントとの通信処理を実装
                // 1. クライアントからのコマンド受信
                // 2. ゲーム状態の更新
                // 3. 更新された状態をクライアントに送信

                ClientHandler handler = new ClientHandler(clientSocket, playerId, gameBoard, this);
                clients.add(handler);
                handler.start();
            }

            System.out.println("ゲームを開始します");

        } catch (IOException e) {
            System.out.println("クライアントとの通信でエラーが発生しました: " + e.getMessage());
        }
    }

    public synchronized void broadcast(String message){
        for(ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
  
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
} 

//各プレイヤーとの通信処理を行うスレッド
class ClientHandler extends Thread {
    private Socket socket;
    private int playerId;
    private Server server;
    private GameBoard gameBoard;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, int playerId, GameBoard gameBoard, Server server) {
        this.socket = socket;
        this.playerId = playerId;
        this.gameBoard = gameBoard;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("プレイヤー" + playerId + " として接続しました。");
            
            String input;
            while ((input = in.readLine()) != null) {
                String[] parts = input.trim().split(" ");
                if (parts.length == 0) {
                    out.println("無効なコマンドです。");
                    continue;
                }

                String command = parts[0].toUpperCase();

                switch (command) {
                    case "PLACE":
                        handlePlace(parts);
                        break;
                    case "MOVE":
                        handleMove(parts);
                        break;
                    case "BOARD":
                        out.println(gameBoard.getBoardState());
                        break;
                    default:
                        out.println("不明なコマンドです。PLACE x y size / MOVE fromX fromY toX toY");
                }
            }
        } catch (IOException e) {
            System.out.println("プレイヤー " + playerId + " との通信でエラー: " + e.getMessage());
        }
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
                    server.broadcast("BOARD\n" + gameBoard.getBoardState());

                    int winner = gameBoard.checkWinner();
                    if (winner != 0) {
                        server.broadcast("勝者: プレイヤー " + winner);
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
                    server.broadcast("BOARD\n" + gameBoard.getBoardState());

                    int winner = gameBoard.checkWinner();
                    if (winner != 0) {
                        server.broadcast("勝者: プレイヤー " + winner);
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