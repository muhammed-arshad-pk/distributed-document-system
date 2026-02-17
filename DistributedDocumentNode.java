import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DistributedDocumentNode extends JFrame {

    // ===== CONFIG (CHANGE PER DEVICE) =====
    int MY_PORT = 5000;   // change per node

    String[][] PEERS = {
            {"10.10.129.36", "5001"},
            {"10.10.129.37", "5002"}
    };

    // ===== GUI =====
    private JTextArea editor;
    private JLabel status;

    private JButton editBtn;
    private JButton updateBtn;
    private JButton lockBtn;

    private boolean editable = false;

    // ===== DATA =====
    private String document = "";
    private Path FILE_PATH = Paths.get("document_" + MY_PORT + ".txt");

    private List<PrintWriter> peerWriters = new ArrayList<>();

    public DistributedDocumentNode() {
        setTitle("Distributed Doc Node : " + MY_PORT);
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Toolbar =====
        JToolBar bar = new JToolBar();
        editBtn = new JButton("Edit");
        updateBtn = new JButton("Update");
        lockBtn = new JButton("Lock");

        bar.add(editBtn);
        bar.add(updateBtn);
        bar.add(lockBtn);
        add(bar, BorderLayout.NORTH);

        // ===== Editor =====
        editor = new JTextArea();
        editor.setFont(new Font("Consolas", Font.PLAIN, 16));
        editor.setEditable(false);
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        add(new JScrollPane(editor), BorderLayout.CENTER);

        // ===== Status =====
        status = new JLabel("Locked");
        status.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(status, BorderLayout.SOUTH);

        // ===== Button Logic =====
        editBtn.addActionListener(e -> unlock());
        lockBtn.addActionListener(e -> lock());
        updateBtn.addActionListener(e -> update());

        loadDocument();

        // Networking
        new Thread(this::startServer).start();
        new Thread(this::connectToPeers).start();

        setVisible(true);
    }

    // ===== GUI ACTIONS =====
    private void unlock() {
        editable = true;
        editor.setEditable(true);
        status.setText("Editing");
    }

    private void lock() {
        editable = false;
        editor.setEditable(false);
        status.setText("Locked");
    }

    private void update() {
        if (!editable) return;

        document = editor.getText();
        saveDocument();
        broadcast(document);
        status.setText("Updated & Synced");
    }

    // ===== SERVER (RECEIVE FROM PEERS) =====
    private void startServer() {
        try (ServerSocket server = new ServerSocket(MY_PORT)) {
            while (true) {
                Socket socket = server.accept();
                new Thread(() -> receive(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receive(Socket socket) {
        try {
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = in.readLine()) != null) {
                document = msg;
                SwingUtilities.invokeLater(() -> editor.setText(document));
                saveDocument();
                status.setText("Updated from peer");
            }
        } catch (Exception ignored) {}
    }

    // ===== CLIENT (SEND TO PEERS) =====
    private void connectToPeers() {
        for (String[] peer : PEERS) {
            try {
                Socket s = new Socket(peer[0], Integer.parseInt(peer[1]));
                peerWriters.add(new PrintWriter(s.getOutputStream(), true));
            } catch (Exception ignored) {}
        }
    }

    private void broadcast(String msg) {
        for (PrintWriter pw : peerWriters) {
            pw.println(msg);
        }
    }

    // ===== STORAGE =====
    private void saveDocument() {
        try {
            Files.writeString(FILE_PATH, document);
        } catch (Exception ignored) {}
    }

    private void loadDocument() {
        try {
            if (Files.exists(FILE_PATH)) {
                document = Files.readString(FILE_PATH);
                editor.setText(document);
            }
        } catch (Exception ignored) {}
    }

    // ===== MAIN =====
    public static void main(String[] args) {
        new DistributedDocumentNode();
    }
}
