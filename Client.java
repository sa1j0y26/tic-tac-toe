import java.net.Socket;
import java.io.*;
import java.util.*;

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

    public void Move(){
        Scanner sc=new Scanner(System.in);
        int bi,bj,ai,aj;//b_:動かす前の場所 a_:動かす先の場所
        System.out.println("動かすコマの場所と動かす先のコマの場所を入力してください");
        bi=sc.nextInt();
        bj=sc.nextInt();
        ai=sc.nextInt();
        aj=sc.nextInt();
        //out.print() サーバーに情報を送信
    }
    public void Place(){
        Scanner sc=new Scanner(System.in);
        int size,i,j;//sizeのコマを(i,j)に置く
        System.out.println("置くコマのサイズと場所を入力してください");
        size=sc.nextInt();
        i=sc.nextInt();
        j=sc.nextInt();
        //サーバーに情報を送信
    }
    public void start() {
        try {
            // TODO: サーバーとの通信処理を実装
            // 1. サーバーからのゲーム状態の受信
            // 2. ユーザーからの入力の受付
            // 3. サーバーへのコマンド送信
            while(true){
                // ゲーム中
                String str=in.readLine();

                //サーバから、ゲームの状態を受け取り、相手のターンなら
                System.out.println("相手のターンです");
                //自分のターンなら
                System.out.println("行動を選択してください: コマを置く:p コマを動かす:m");
                //勝敗が決まった
                //break;
                Scanner sc=new Scanner(System.in);
                while(true){
                String str_in=sc.nextLine();
                    if(str_in=="p") //新たにコマを置く
                    {
                        Move();
                        break;
                    }
                    else if(str_in=="m")//動かす
                    {
                        Place();
                        break;
                    }
                }
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