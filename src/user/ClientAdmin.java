package user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientAdmin extends JFrame {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private JTextField txtBookName, txtOldBook, txtNewBook;
    private JTextArea txtAreaBooks;
    private JButton btnAdd, btnEdit, btnDelete, btnList, btnApprove;

    public ClientAdmin() {
        setTitle("📚 Library - Client Admin");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel thao tác thêm/sửa/xóa
        JPanel panelTop = new JPanel(new GridLayout(3, 3, 5, 5));
        panelTop.setBorder(BorderFactory.createTitledBorder("Quản lý sách"));

        panelTop.add(new JLabel("Tên sách:"));
        txtBookName = new JTextField();
        panelTop.add(txtBookName);
        btnAdd = new JButton("Thêm sách");
        panelTop.add(btnAdd);

        panelTop.add(new JLabel("Sách cũ:"));
        txtOldBook = new JTextField();
        panelTop.add(txtOldBook);
        panelTop.add(new JLabel("Sách mới:"));
        txtNewBook = new JTextField();
        panelTop.add(txtNewBook);
        btnEdit = new JButton("Sửa sách");
        panelTop.add(btnEdit);

        btnDelete = new JButton("Xóa sách");
        panelTop.add(btnDelete);

        // Danh sách sách
        txtAreaBooks = new JTextArea();
        txtAreaBooks.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtAreaBooks);

        JPanel panelBooks = new JPanel(new BorderLayout());
        panelBooks.setBorder(BorderFactory.createTitledBorder("Danh sách sách"));
        panelBooks.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout());
        btnList = new JButton("Xem danh sách");
        btnApprove = new JButton("Duyệt mượn sách");
        panelBottom.add(btnList);
        panelBottom.add(btnApprove);

        add(panelTop, BorderLayout.NORTH);
        add(panelBooks, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);

        // Event
        btnAdd.addActionListener(e -> addBook());
        btnEdit.addActionListener(e -> editBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnList.addActionListener(e -> listBooks());
        btnApprove.addActionListener(e -> approveBorrow());

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

    private void addBook() {
        try {
            String book = txtBookName.getText().trim();
            if (book.isEmpty()) return;

            dos.writeUTF("ADD_BOOK;" + book);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editBook() {
        try {
            String oldBook = txtOldBook.getText().trim();
            String newBook = txtNewBook.getText().trim();
            if (oldBook.isEmpty() || newBook.isEmpty()) return;

            dos.writeUTF("EDIT_BOOK;" + oldBook + ";" + newBook);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteBook() {
        try {
            String book = txtBookName.getText().trim();
            if (book.isEmpty()) return;

            dos.writeUTF("DELETE_BOOK;" + book);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
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

    private void approveBorrow() {
        try {
            String book = JOptionPane.showInputDialog(this, "Nhập tên sách cần duyệt:");
            if (book == null || book.isEmpty()) return;

            dos.writeUTF("APPROVE_BORROW;" + book);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientAdmin().setVisible(true));
    }
}
