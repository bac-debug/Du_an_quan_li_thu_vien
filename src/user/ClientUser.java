package user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientUser extends JFrame {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private JTextField txtUsername, txtPassword, txtSearchBook;
    private JTextArea txtAreaBooks;
    private JButton btnLogin, btnRegister, btnListBooks, btnBorrow;

    private String currentUser = null;

    public ClientUser() {
        setTitle("📚 Library - Client User");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ================= UI =================
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Đăng nhập / Đăng ký"));

        panel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panel.add(txtUsername);

        panel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");
        panel.add(btnLogin);
        panel.add(btnRegister);

        txtAreaBooks = new JTextArea();
        txtAreaBooks.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtAreaBooks);

        JPanel panelBooks = new JPanel(new BorderLayout());
        panelBooks.setBorder(BorderFactory.createTitledBorder("Danh sách sách"));
        panelBooks.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout());
        btnListBooks = new JButton("Xem danh sách");
        txtSearchBook = new JTextField(15);
        btnBorrow = new JButton("Mượn sách");
        panelBottom.add(btnListBooks);
        panelBottom.add(new JLabel("Tên sách:"));
        panelBottom.add(txtSearchBook);
        panelBottom.add(btnBorrow);

        add(panel, BorderLayout.NORTH);
        add(panelBooks, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);

        // ================= Event =================
        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> register());
        btnListBooks.addActionListener(e -> listBooks());
        btnBorrow.addActionListener(e -> borrowBook());

        // ================= Connect Server =================
        connectServer();
    }

    private void connectServer() {
        try {
            socket = new Socket("localhost", 12345);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            JOptionPane.showMessageDialog(this, "✅ Kết nối server thành công!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "❌ Không thể kết nối server!");
            System.exit(0);
        }
    }

    private void register() {
        try {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
                return;
            }

            dos.writeUTF("REGISTER;" + username + ";" + password);
            dos.flush();

            String response = dis.readUTF();
            JOptionPane.showMessageDialog(this, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        try {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();

            dos.writeUTF("LOGIN;" + username + ";" + password);
            dos.flush();

            String response = dis.readUTF();
            if (response.startsWith("OK")) {
                currentUser = username;
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listBooks() {
        try {
            dos.writeUTF("LIST_BOOKS");
            dos.flush();

            String response = dis.readUTF();
            if (response.startsWith("OK")) {
                String[] parts = response.split(";", 2);
                String[] books = parts[1].split("\\|");

                txtAreaBooks.setText("");
                for (String b : books) {
                    if (!b.isBlank()) txtAreaBooks.append(b + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void borrowBook() {
        try {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Bạn cần đăng nhập trước!");
                return;
            }

            String bookName = txtSearchBook.getText().trim();
            if (bookName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sách muốn mượn!");
                return;
            }

            dos.writeUTF("BORROW;" + currentUser + ";" + bookName);
            dos.flush();

            String response = dis.readUTF();
            JOptionPane.showMessageDialog(this, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUser().setVisible(true));
    }
}
