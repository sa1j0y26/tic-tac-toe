import java.net.Socket;
import java.io.*;
import java.util.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5050;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Client() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String str=in.readLine();
            System.out.println(str);

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
            boolean my_turn = false;
            Scanner sc = new Scanner(System.in);

            while (true) {
                String str_server = in.readLine();
                System.out.println(str_server);

                if (str_server.equals("あなたのターンです！")) {
                    my_turn = true;
                }

                if (my_turn) {
                    //System.out.println("行動を選択してください: ボードの状態を確認する:BOARD コマを置く:PLACE x y size コマを動かす:MOVE fromX fromY toX toY");
                    String str_client = sc.nextLine();
                    out.println(str_client);
                    my_turn = false;
                }

                if (str_server.startsWith("勝者:")) break;
            }

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