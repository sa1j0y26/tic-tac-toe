import java.net.Socket;
import java.io.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int playerNumber;

    public Client() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("サーバーに接続しました");
        } catch (IOException e) {
            System.out.println("サーバーへの接続に失敗しました: " + e.getMessage());
        }
    }

    public void start() {
        try {
            // TODO: サーバーとの通信処理を実装
            // 1. サーバーからのゲーム状態の受信
            // 2. ユーザーからの入力の受付
            // 3. サーバーへのコマンド送信
        } catch (IOException e) {
            System.out.println("通信でエラーが発生しました: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("ソケットのクローズに失敗しました: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
} 