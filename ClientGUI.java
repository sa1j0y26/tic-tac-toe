import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClientGUI extends JFrame {
    // チャット
    private JTextArea chatArea;
    private JTextArea chatHistoryArea;
    private JTextField chatInput;
    private JButton sendButton;
    // ボード
    private JPanel boardPanel;
    private JButton[][] boardButtons = new JButton[3][3];
    // 手持ちコマ
    private JPanel piecePanel;
    private JScrollPane pieceScrollPane;
    private java.util.List<JButton> pieceButtons = new java.util.ArrayList<>();
    // コマンド
    private JPanel commandPanel;
    private JButton placeButton;
    private JButton moveButton;
    // 通信
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    // 選択状態
    private Point selectedCell = null;
    private JButton selectedPieceButton = null;
    private int selectedPieceSize = -1;
    // MOVE用
    private boolean moveMode = false;
    private Point moveFrom = null;
    private Point moveTo = null;
    // 手持ちコマの最新状態
    private Map<Integer, Integer> latestPieces = new HashMap<>();
    private JTabbedPane tabbedPane;

    public ClientGUI() {
        setTitle("Tic-Tac-Toe クライアント");
        setSize(500, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        // ゲーム画面パネル
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));

        // チャットエリア
        chatArea = new JTextArea(8, 30);
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        gamePanel.add(chatScroll);

        // チャット入力
        JPanel chatInputPanel = new JPanel();
        chatInputPanel.setLayout(new BoxLayout(chatInputPanel, BoxLayout.X_AXIS));
        chatInput = new JTextField();
        sendButton = new JButton("送信");
        chatInputPanel.add(chatInput);
        chatInputPanel.add(sendButton);
        gamePanel.add(chatInputPanel);

        // 区切り線
        gamePanel.add(new JSeparator());

        // ボード
        boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        boardPanel.setMaximumSize(new Dimension(400, 400));
        boardPanel.setPreferredSize(new Dimension(400, 400));
        Font boardFont = new Font(Font.SANS_SERIF, Font.BOLD, 32);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                JButton btn = new JButton("");
                btn.setFont(boardFont);
                btn.setPreferredSize(new Dimension(100, 100));
                int fx = x, fy = y;
                btn.addActionListener(e -> selectCell(fx, fy));
                boardButtons[y][x] = btn;
                boardPanel.add(btn);
            }
        }
        gamePanel.add(boardPanel);

        // 区切り線
        gamePanel.add(new JSeparator());

        // 手持ちコマリスト
        piecePanel = new JPanel();
        piecePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
        JLabel pieceLabel = new JLabel("手持ちコマ:");
        piecePanel.add(pieceLabel);
        for (int size = 1; size <= 3; size++) {
            for (int i = 0; i < 2; i++) {
                JButton pieceBtn = new JButton(sizeToStr(size));
                pieceBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                int pieceSize = size;
                pieceBtn.addActionListener(e -> selectPiece(pieceBtn, pieceSize));
                pieceButtons.add(pieceBtn);
                piecePanel.add(pieceBtn);
            }
        }
        pieceScrollPane = new JScrollPane(piecePanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        pieceScrollPane.setPreferredSize(new Dimension(400, 60));
        gamePanel.add(pieceScrollPane);

        // 区切り線
        gamePanel.add(new JSeparator());

        // コマンドボタン群
        commandPanel = new JPanel();
        commandPanel.setLayout(new FlowLayout());
        placeButton = new JButton("PLACE");
        moveButton = new JButton("MOVE");
        commandPanel.add(placeButton);
        commandPanel.add(moveButton);
        gamePanel.add(commandPanel);

        // タブ1: ゲーム画面
        tabbedPane.addTab("ゲーム", gamePanel);
        // タブ2: 履歴（チャット履歴だけ大きく表示）
        chatHistoryArea = new JTextArea();
        chatInput.setPreferredSize(new Dimension(500, 35)); //チャット入力欄サイズ初期値
        chatInput.setMaximumSize(new Dimension(500, 35)); //チャット入力欄サイズ最大値
        chatHistoryArea.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(chatHistoryArea);
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        tabbedPane.addTab("履歴", historyPanel);
        tabbedPane.setSelectedIndex(0); // 起動時にゲームタブを選択

        setContentPane(tabbedPane);

        // PLACEボタン
        placeButton.addActionListener(e -> {
            moveMode = false;
            clearMoveSelection();
            sendPlaceCommand();
        });
        // MOVEボタン
        moveButton.addActionListener(e -> {
            moveMode = true;
            clearPlaceSelection();
            JOptionPane.showMessageDialog(this, "移動元マスを選択してください");
        });

        // チャット送信
        sendButton.addActionListener(e -> sendChat());
        chatInput.addActionListener(e -> sendChat());

        // サーバ接続
        connectToServer();
    }

    private void selectCell(int x, int y) {
        if (moveMode) {
            // MOVEモード
            if (moveFrom == null) {
                clearMoveSelection();
                moveFrom = new Point(x, y);
                boardButtons[y][x].setBackground(Color.YELLOW);
                JOptionPane.showMessageDialog(this, "移動先マスを選択してください");
            } else if (moveTo == null && !(moveFrom.x == x && moveFrom.y == y)) {
                moveTo = new Point(x, y);
                boardButtons[y][x].setBackground(Color.ORANGE);
                sendMoveCommand();
            }
        } else {
            // PLACEモード
            if (selectedCell != null) {
                boardButtons[selectedCell.y][selectedCell.x].setBackground(null);
            }
            selectedCell = new Point(x, y);
            boardButtons[y][x].setBackground(Color.YELLOW);
        }
    }

    private void selectPiece(JButton btn, int size) {
        if (selectedPieceButton != null) {
            selectedPieceButton.setBackground(null);
        }
        selectedPieceButton = btn;
        selectedPieceSize = size;
        btn.setBackground(Color.CYAN);
    }

    private void sendPlaceCommand() {
        if (out == null) return;
        if (selectedCell == null || selectedPieceButton == null) {
            JOptionPane.showMessageDialog(this, "マスとコマを選択してください");
            return;
        }
        out.println("PLACE " + selectedCell.x + " " + selectedCell.y + " " + selectedPieceSize);
        selectedPieceButton.setBackground(null);
        selectedPieceButton = null;
        selectedPieceSize = -1;
        boardButtons[selectedCell.y][selectedCell.x].setBackground(null);
        selectedCell = null;
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5050);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // サーバからのメッセージ受信スレッド
            new Thread(() -> {
                try {
                    String line;
                    StringBuilder boardStr = new StringBuilder();
                    boolean boardMode = false;
                    while ((line = in.readLine()) != null) {
                        // ゲームタブには全てappend
                        if (line.startsWith("CHAT:")) {
                            String chatMsg = line.substring(5);
                            chatArea.append(chatMsg + "\n");
                            chatHistoryArea.append(chatMsg + "\n");
                        } else if (line.startsWith("BOARD")) {
                            // BOARDメッセージは複数行
                            boardStr.setLength(0);
                            boardStr.append(line).append("\n");
                            for (int i = 0; i < 3; i++) {
                                String l = in.readLine();
                                if (l != null) boardStr.append(l).append("\n");
                            }
                            updateBoard(boardStr.toString());
                        } else if (line.startsWith("PIECES:")) {
                            updatePiecesPanel(line);
                        } else {
                            // システムメッセージはゲームタブだけにappend
                            chatArea.append(line + "\n");
                        }
                    }
                } catch (IOException e) {
                    chatArea.append("サーバとの通信が切断されました\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "サーバーに接続できませんでした");
        }
    }

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty() && out != null) {
            out.println("/chat " + msg);
            chatInput.setText("");
        }
    }

    private void updateBoard(String boardStr) {
        // "BOARD\n0 0 0\n0 0 0\n0 0 0\n" 形式をパース
        String[] lines = boardStr.split("\n");
        if (lines.length < 4) return;
        for (int y = 0; y < 3; y++) {
            String[] cells = lines[y + 1].split(" ");
            for (int x = 0; x < 3; x++) {
                String cell = cells[x];
                JButton btn = boardButtons[y][x];

                if (cell.equals("0")) {
                    btn.setText("");
                } else {
                    int player = Integer.parseInt(cell.substring(0, 1)); // 1 or 2
                    int size = Integer.parseInt(cell.substring(1));      // 1, 2, 3

                    String circle = player == 1 ? "🔴" : "🔵";
                    btn.setText(circle); 
                    int fontSize;
                    switch (size) {
                        case 1: fontSize = 20; break;  // 小
                        case 2: fontSize = 35; break;  // 中
                        case 3: fontSize = 50; break;  // 大
                        default: fontSize = 20;
                    }
                    btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
                }

            }
        }
    }

    private String sizeToStr(int size) {
        switch (size) {
            case 1: return "小";
            case 2: return "中";
            case 3: return "大";
            default: return "?";
        }
    }

    private void sendMoveCommand() {
        if (out == null) return;
        if (moveFrom == null || moveTo == null) {
            JOptionPane.showMessageDialog(this, "移動元・移動先マスを選択してください");
            return;
        }
        out.println("MOVE " + moveFrom.x + " " + moveFrom.y + " " + moveTo.x + " " + moveTo.y);
        clearMoveSelection();
        moveMode = false;
    }

    private void clearMoveSelection() {
        if (moveFrom != null) boardButtons[moveFrom.y][moveFrom.x].setBackground(null);
        if (moveTo != null) boardButtons[moveTo.y][moveTo.x].setBackground(null);
        moveFrom = null;
        moveTo = null;
    }

    private void clearPlaceSelection() {
        if (selectedCell != null) boardButtons[selectedCell.y][selectedCell.x].setBackground(null);
        selectedCell = null;
        if (selectedPieceButton != null) selectedPieceButton.setBackground(null);
        selectedPieceButton = null;
        selectedPieceSize = -1;
    }

    // PIECES:1:2,2:1,3:0 のような形式をパースし、手持ちコマパネルを更新
    private void updatePiecesPanel(String piecesLine) {
        SwingUtilities.invokeLater(() -> {
            latestPieces.clear();
            String data = piecesLine.substring("PIECES:".length());
            String[] parts = data.split(",");
            for (String part : parts) {
                String[] kv = part.split(":");
                if (kv.length == 2) {
                    int size = Integer.parseInt(kv[0]);
                    int count = Integer.parseInt(kv[1]);
                    latestPieces.put(size, count);
                }
            }
            // パネルを再構築
            piecePanel.removeAll();
            piecePanel.add(new JLabel("手持ちコマ:"));
            pieceButtons.clear();
            for (int size = 1; size <= 3; size++) {
                int count = latestPieces.getOrDefault(size, 0);
                for (int i = 0; i < count; i++) {
                    JButton pieceBtn = new JButton(sizeToStr(size));
                    pieceBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                    int pieceSize = size;
                    pieceBtn.addActionListener(e -> selectPiece(pieceBtn, pieceSize));
                    pieceButtons.add(pieceBtn);
                    piecePanel.add(pieceBtn);
                }
            }
            piecePanel.revalidate();
            piecePanel.repaint();
            pieceScrollPane.revalidate();
            pieceScrollPane.repaint();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
} 