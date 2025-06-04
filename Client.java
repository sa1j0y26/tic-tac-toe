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
            List<String> chatHistory = Collections.synchronizedList(new ArrayList<>());
            Scanner sc = new Scanner(System.in);

            // サーバからのメッセージ受信スレッド
            Thread receiveThread = new Thread(() -> {
                try {
                    String str_server;
                    while ((str_server = in.readLine()) != null) {
                        if (str_server.startsWith("CHAT:")) {
                            String chatMsg = str_server.substring(5);
                            chatHistory.add(chatMsg);
                            System.out.println("[チャット] " + chatMsg);
                        } else if (str_server.startsWith("HISTORY:")) {
                            // サーバから履歴が送られてきた場合
                            System.out.println("--- チャット履歴 ---");
                            String[] lines = str_server.substring(8).split("\\\n");
                            for (String line : lines) {
                                System.out.println(line);
                            }
                            System.out.println("------------------");
                        } else {
                            System.out.println(str_server);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("サーバとの通信が切断されました: " + e.getMessage());
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // 標準入力受付スレッド
            while (true) {
                String input = sc.nextLine();
                if (input.trim().equalsIgnoreCase("HELP")) {
                    System.out.println("<操作方法>\nボードの状態を確認する: BOARD\nコマを置く: PLACE x y size\nコマを動かす: MOVE fromX fromY toX toY\nチャット送信: /chat メッセージ\nチャット履歴参照: /history");
                    continue;
                } else if (input.startsWith("/chat ")) {
                    out.println(input);
                } else if (input.equals("/history")) {
                    out.println(input);
                } else {
                    out.println(input);
                }
            }
        } catch (Exception e) {
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