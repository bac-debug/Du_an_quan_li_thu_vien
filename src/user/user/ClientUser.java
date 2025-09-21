package user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;

// ===================== LOGIN FRAME =====================
class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public LoginFrame() {
        setTitle("📚 Library - Login");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Username:"));
        txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");
        add(btnLogin);
        add(btnRegister);

        connectServer();

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> register());
    }

    private void connectServer() {
        try {
            socket = new Socket("localhost", 5555);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "❌ Không thể kết nối server!");
            System.exit(0);
        }
    }

    private void register() {
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng nhập đủ thông tin!");
                return;
            }
            dos.writeUTF("REGISTER;" + username + ";" + password);
            dos.flush();
            JOptionPane.showMessageDialog(this, dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            dos.writeUTF("LOGIN;" + username + ";" + password);
            dos.flush();
            String response = dis.readUTF();
            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "✅ Đăng nhập thành công!");
                this.dispose();
                new UserFrame(socket, username).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// ===================== USER FRAME =====================
class UserFrame extends JFrame {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String currentUser;

    private JTable tableBooks, tableBorrows;
    private DefaultTableModel modelBooks, modelBorrows;
    private JTextField txtSearchBook;
    private JButton btnBorrow, btnMyBorrows;

    public UserFrame(Socket socket, String username) {
        this.socket = socket;
        this.currentUser = username;

        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi khi kết nối dữ liệu!");
            System.exit(0);
        }

        setTitle("📚 Library - User: " + username);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ================= Bảng sách =================
        modelBooks = new DefaultTableModel(new String[]{"Tên sách", "Số lượng", "Mô tả"}, 0);
        tableBooks = new JTable(modelBooks);
        tableBooks.setRowHeight(25);
        tableBooks.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollBooks = new JScrollPane(tableBooks);
        scrollBooks.setBorder(BorderFactory.createTitledBorder("📖 Danh sách sách"));

        // ================= Bảng mượn =================
        modelBorrows = new DefaultTableModel(new String[]{"Tên sách", "Trạng thái"}, 0);
        tableBorrows = new JTable(modelBorrows);
        tableBorrows.setRowHeight(25);
        tableBorrows.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollBorrows = new JScrollPane(tableBorrows);
        scrollBorrows.setBorder(BorderFactory.createTitledBorder("📕 Sách bạn đã mượn"));

        // ================= Panel dưới =================
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        txtSearchBook = new JTextField(20);
        btnBorrow = new JButton("📕 Mượn sách");
        btnMyBorrows = new JButton("📝 Sách đã mượn");
        panelBottom.add(new JLabel("Tên sách:"));
        panelBottom.add(txtSearchBook);
        panelBottom.add(btnBorrow);
        panelBottom.add(btnMyBorrows);

        // ================= Layout chính =================
        JPanel panelCenter = new JPanel(new GridLayout(2, 1, 10, 10));
        panelCenter.add(scrollBooks);
        panelCenter.add(scrollBorrows);
        add(panelCenter, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);

        // ================= Event =================
        btnBorrow.addActionListener(e -> borrowBook());
        btnMyBorrows.addActionListener(e -> listMyBorrows());

        // ================= Tự động load danh sách sách =================
        listBooks();
        listMyBorrows(); // tùy chọn: hiển thị luôn sách đã mượn
    }

    private void listBooks() {
        try {
            dos.writeUTF("LIST_BOOKS");
            dos.flush();
            String response = dis.readUTF();
            if (response.startsWith("OK")) {
                modelBooks.setRowCount(0);
                String[] parts = response.split(";", 2);
                if (parts.length > 1) {
                    String[] books = parts[1].split("\\|");
                    for (String b : books) {
                        if (!b.isBlank()) {
                            String[] info = b.split(",");
                            if (info.length >= 3) {
                                modelBooks.addRow(new Object[]{info[0], info[1], info[2]});
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void borrowBook() {
        try {
            String bookName = txtSearchBook.getText().trim();
            if (bookName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng nhập tên sách muốn mượn!");
                return;
            }
            dos.writeUTF("BORROW;" + currentUser + ";" + bookName + ";1");
            dos.flush();
            JOptionPane.showMessageDialog(this, dis.readUTF());

            // Cập nhật lại danh sách sách và mượn
            listBooks();
            listMyBorrows();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listMyBorrows() {
        try {
            dos.writeUTF("MY_BORROWS;" + currentUser);
            dos.flush();
            String response = dis.readUTF();
            if (response.startsWith("OK")) {
                modelBorrows.setRowCount(0);
                String[] parts = response.split(";", 2);
                if (parts.length > 1) {
                    String[] borrows = parts[1].split("\\|");
                    for (String br : borrows) {
                        if (!br.isBlank()) {
                            String[] info = br.split(",");
                            if (info.length >= 2) {
                                modelBorrows.addRow(new Object[]{info[0], info[1]});
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// ===================== MAIN =====================
public class ClientUser {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
