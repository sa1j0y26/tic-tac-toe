import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextArea chatHistoryArea;
    private JTextField chatInput;
    private JButton sendButton;
    private JPanel boardPanel;
    private JButton[][] boardButtons = new JButton[3][3];
    private JPanel piecePanel;
    private JScrollPane pieceScrollPane;
    private java.util.List<JButton> pieceButtons = new java.util.ArrayList<>();
    private JPanel commandPanel;
    private JButton placeButton;
    private JButton moveButton;
    private JButton helpButton;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private Point selectedCell = null;
    private JButton selectedPieceButton = null;
    private int selectedPieceSize = -1;
    private boolean moveMode = false;
    private Point moveFrom = null;
    private Point moveTo = null;
    private Map<Integer, Integer> latestPieces = new HashMap<>();
    private JTabbedPane tabbedPane;
    private int myPlayerId = -1;

    public ClientGUI() {
        setTitle("スタック式陣取りゲーム");
        setSize(500, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));

        chatArea = new JTextArea(8, 30);
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        gamePanel.add(chatScroll);

        JPanel chatInputPanel = new JPanel();
        chatInputPanel.setLayout(new BoxLayout(chatInputPanel, BoxLayout.X_AXIS));
        chatInput = new JTextField();
        sendButton = new JButton("送信");
        chatInputPanel.add(chatInput);
        chatInputPanel.add(sendButton);
        gamePanel.add(chatInputPanel);
        gamePanel.add(new JSeparator());

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
                btn.addActionListener(_ -> selectCell(fx, fy));
                boardButtons[y][x] = btn;
                boardPanel.add(btn);
            }
        }
        gamePanel.add(boardPanel);

        gamePanel.add(new JSeparator());

        piecePanel = new JPanel();
        piecePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
        JLabel pieceLabel = new JLabel("手持ちコマ:");
        piecePanel.add(pieceLabel);
        for (int size = 1; size <= 3; size++) {
            for (int i = 0; i < 2; i++) {
                JButton pieceBtn = new JButton(sizeToStr(size));
                pieceBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                int pieceSize = size;
                pieceBtn.addActionListener(_ -> selectPiece(pieceBtn, pieceSize));
                pieceButtons.add(pieceBtn);
                piecePanel.add(pieceBtn);
            }
        }
        pieceScrollPane = new JScrollPane(piecePanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        pieceScrollPane.setPreferredSize(new Dimension(400, 60));
        gamePanel.add(pieceScrollPane);

        gamePanel.add(new JSeparator());

        commandPanel = new JPanel();
        commandPanel.setLayout(new FlowLayout());
        placeButton = new JButton("PLACE");
        moveButton = new JButton("MOVE");
        helpButton = new JButton("HELP");
        commandPanel.add(placeButton);
        commandPanel.add(moveButton);
        commandPanel.add(helpButton);
        gamePanel.add(commandPanel);

        tabbedPane.addTab("ゲーム", gamePanel);
        chatHistoryArea = new JTextArea();
        chatInput.setPreferredSize(new Dimension(500, 35));
        chatInput.setMaximumSize(new Dimension(500, 35));
        chatHistoryArea.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(chatHistoryArea);
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        tabbedPane.addTab("履歴", historyPanel);
        tabbedPane.setSelectedIndex(0);

        setContentPane(tabbedPane);

        placeButton.addActionListener(_ -> {
            moveMode = false;
            clearMoveSelection();
            sendPlaceCommand();
        });
        moveButton.addActionListener(_ -> {
            moveMode = true;
            clearPlaceSelection();
            JOptionPane.showMessageDialog(this, "移動元マスを選択してください");
        });

        helpButton.addActionListener(_ ->{
            JOptionPane.showMessageDialog(this,
                "<操作方法>\n" +
                "コマを置く: 場所と大きさを指定してPLACEボタンを押す\n" +
                "コマを動かす: MOVEボタンを押してから移動前のマス、移動先のマスを指定する\n" +
                "チャット送信: 下のテキストボックスにメッセージを入力後、送信ボタンを押す\n" +
                "チャット履歴参照: 上の履歴タブ\n",
                "ヘルプ",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        sendButton.addActionListener(_ -> sendChat());
        chatInput.addActionListener(_ -> sendChat());

        connectToServer();
    }

    private void selectCell(int x, int y) {
        if (moveMode) {
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

            new Thread(() -> {
                try {
                    String line;
                    StringBuilder boardStr = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("CHAT:")) {
                            String chatMsg = line.substring(5);
                            chatArea.append(chatMsg + "\n");
                            chatHistoryArea.append(chatMsg + "\n");
                        } else if (line.startsWith("BOARD")) {
                            boardStr.setLength(0);
                            boardStr.append(line).append("\n");
                            for (int i = 0; i < 3; i++) {
                                String l = in.readLine();
                                if (l != null) boardStr.append(l).append("\n");
                            }
                            updateBoard(boardStr.toString());
                        } else if (line.startsWith("PIECES:")) {
                            updatePiecesPanel(line);
                        } else if (line.contains("勝者: プレイヤー")) {
                            JOptionPane.showMessageDialog(this, line + "\nおめでとうございます！", "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
                            chatArea.append("\n=== " + line + " ===\n");
                        } else if (line.startsWith("プレイヤー") && line.contains("として接続しました。")) {
                            String[] parts = line.split(" ");
                            try {
                                myPlayerId = Integer.parseInt(parts[0].replace("プレイヤー", ""));
                            } catch (NumberFormatException e) {
                                myPlayerId = -1;
                            }
                            chatArea.append(line + "\n");
                        } else if (line.startsWith("WINNER ")) {
                            int winnerId = Integer.parseInt(line.substring(7).trim());
                            String result = (winnerId == myPlayerId) ? "あなたの勝ちです！おめでとう！" : "あなたの負けです…";
                            JOptionPane.showMessageDialog(this, result, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
                            chatArea.append("\n=== " + result + " ===\n");
                        } else {
                            chatArea.append(line + "\n");
                        }
                    }
                } catch (IOException e) {
                    chatArea.append("サーバとの通信が切断されました\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "サーバーに接続できませんでした");
            System.exit(0);
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
                    int player = Integer.parseInt(cell.substring(0, 1));
                    int size = Integer.parseInt(cell.substring(1));

                    String circle = player == 1 ? "🔴" : "🔵";
                    btn.setText(circle); 
                    int fontSize;
                    switch (size) {
                        case 1: fontSize = 20; break;
                        case 2: fontSize = 35; break;
                        case 3: fontSize = 50; break;
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
            piecePanel.removeAll();
            piecePanel.add(new JLabel("手持ちコマ:"));
            pieceButtons.clear();
            for (int size = 1; size <= 3; size++) {
                int count = latestPieces.getOrDefault(size, 0);
                for (int i = 0; i < count; i++) {
                    JButton pieceBtn = new JButton(sizeToStr(size));
                    pieceBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                    int pieceSize = size;
                    pieceBtn.addActionListener(_ -> selectPiece(pieceBtn, pieceSize));
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