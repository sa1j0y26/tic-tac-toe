import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class Server {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private GameBoard gameBoard;
    private int currentPlayer;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            gameBoard = new GameBoard();
            currentPlayer = 1;
            System.out.println("サーバーが起動しました。ポート: " + PORT);
        } catch (IOException e) {
            System.out.println("サーバーの起動に失敗しました: " + e.getMessage());
        }
    }

    public void start() {
        try {
            while (true) {
                System.out.println("クライアントの接続を待っています...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("クライアントが接続しました: " + clientSocket.getInetAddress());

                // TODO: クライアントとの通信処理を実装
                // 1. クライアントからのコマンド受信
                // 2. ゲーム状態の更新
                // 3. 更新された状態をクライアントに送信
            }
        } catch (IOException e) {
            System.out.println("クライアントとの通信でエラーが発生しました: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
} 