package user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientAdmin extends JFrame {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private JTable tableBooks, tableBorrows;
    private DefaultTableModel modelBooks, modelBorrows;
    private JTextField txtName, txtQuantity;
    private JTextArea txtDescription;

    private JButton btnAdd, btnEdit, btnDelete, btnList, btnApprove, btnLoadBorrows;

    public ClientAdmin() {
        setTitle("📚 Library - Client Admin");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ================= Bảng danh sách sách =================
        modelBooks = new DefaultTableModel(new String[]{"Tên sách", "Số lượng", "Mô tả"}, 0);
        tableBooks = new JTable(modelBooks);
        tableBooks.setRowHeight(25);
        tableBooks.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollBooks = new JScrollPane(tableBooks);
        scrollBooks.setBorder(BorderFactory.createTitledBorder("📖 Danh sách sách"));

        // ================= Bảng yêu cầu mượn =================
        modelBorrows = new DefaultTableModel(new String[]{"Người mượn", "Tên sách", "Số lượng"}, 0);
        tableBorrows = new JTable(modelBorrows);
        tableBorrows.setRowHeight(25);
        tableBorrows.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollBorrows = new JScrollPane(tableBorrows);
        scrollBorrows.setBorder(BorderFactory.createTitledBorder("📋 Yêu cầu mượn"));

        // ================= Form nhập liệu =================
        JPanel panelForm = new JPanel(new GridLayout(3, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createTitledBorder("✏️ Thông tin sách"));

        panelForm.add(new JLabel("Tên sách:"));
        txtName = new JTextField();
        panelForm.add(txtName);

        panelForm.add(new JLabel("Số lượng:"));
        txtQuantity = new JTextField();
        panelForm.add(txtQuantity);

        panelForm.add(new JLabel("Mô tả:"));
        txtDescription = new JTextArea(2, 15);
        panelForm.add(new JScrollPane(txtDescription));

        // ================= Các nút chức năng =================
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnAdd = new JButton("➕ Thêm");
        btnEdit = new JButton("✏️ Sửa");
        btnDelete = new JButton("🗑️ Xóa");
        btnList = new JButton("📖 Làm mới");
        btnLoadBorrows = new JButton("📋 Tải yêu cầu mượn");
        btnApprove = new JButton("✔️ Duyệt mượn");

        panelButtons.add(btnAdd);
        panelButtons.add(btnEdit);
        panelButtons.add(btnDelete);
        panelButtons.add(btnList);
        panelButtons.add(btnLoadBorrows);
        panelButtons.add(btnApprove);

        // ================= Bố cục chính =================
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBooks, scrollBorrows);
        splitPane.setDividerLocation(250);

        JPanel panelRight = new JPanel(new BorderLayout(10, 10));
        panelRight.add(panelForm, BorderLayout.CENTER);
        panelRight.add(panelButtons, BorderLayout.SOUTH);

        setLayout(new BorderLayout(10, 10));
        add(splitPane, BorderLayout.CENTER);
        add(panelRight, BorderLayout.EAST);

        // ================= Sự kiện =================
        btnAdd.addActionListener(e -> addBook());
        btnEdit.addActionListener(e -> editBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnList.addActionListener(e -> listBooks());
        btnLoadBorrows.addActionListener(e -> listBorrows());
        btnApprove.addActionListener(e -> approveBorrow());

        // ================= Kết nối server =================
        connectServer();
        listBooks();
        listBorrows();
    }

    // ====== KẾT NỐI SERVER ======
    private void connectServer() {
        try {
            socket = new Socket("localhost", 5555);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            JOptionPane.showMessageDialog(this, "✅ Kết nối server thành công!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "❌ Không thể kết nối server!");
            System.exit(0);
        }
    }

    // ====== THÊM SÁCH ======
    private void addBook() {
        try {
            String name = txtName.getText().trim();
            String qty = txtQuantity.getText().trim();
            String desc = txtDescription.getText().trim();

            if (name.isEmpty() || qty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng nhập đủ Tên và Số lượng!");
                return;
            }

            dos.writeUTF("ADD_BOOK;" + name + ";" + qty + ";" + desc);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
            listBooks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== SỬA SÁCH ======
    private void editBook() {
        try {
            int selectedRow = tableBooks.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn một sách để sửa!");
                return;
            }

            String oldName = modelBooks.getValueAt(selectedRow, 0).toString();
            String newName = txtName.getText().trim();
            String qty = txtQuantity.getText().trim();
            String desc = txtDescription.getText().trim();

            if (newName.isEmpty() || qty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng nhập đủ thông tin mới!");
                return;
            }

            dos.writeUTF("EDIT_BOOK;" + oldName + ";" + newName + ";" + qty + ";" + desc);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
            listBooks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== XÓA SÁCH ======
    private void deleteBook() {
        try {
            int selectedRow = tableBooks.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn một sách để xóa!");
                return;
            }

            String name = modelBooks.getValueAt(selectedRow, 0).toString();

            dos.writeUTF("DELETE_BOOK;" + name);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
            listBooks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== DANH SÁCH SÁCH ======
    private void listBooks() {
        try {
            dos.writeUTF("LIST_BOOKS");
            dos.flush();

            String response = dis.readUTF();
            if (response.startsWith("OK")) {
                String[] parts = response.split(";", 2);
                if (parts.length > 1) {
                    String[] books = parts[1].split("\\|");
                    modelBooks.setRowCount(0);
                    for (String b : books) {
                        if (!b.isBlank()) {
                            String[] info = b.split(",");
                            if (info.length >= 3) {
                                modelBooks.addRow(new Object[]{info[0], info[1], info[2]});
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== DANH SÁCH YÊU CẦU MƯỢN ======
    private void listBorrows() {
        try {
            dos.writeUTF("LIST_BORROWS");
            dos.flush();

            String response = dis.readUTF();
            if (response.startsWith("OK")) {
                String[] parts = response.split(";", 2);
                modelBorrows.setRowCount(0);
                if (parts.length > 1) {
                    String[] borrows = parts[1].split("\\|");
                    for (String b : borrows) {
                        if (!b.isBlank()) {
                            String[] info = b.split(",");
                            if (info.length >= 3) {
                                modelBorrows.addRow(new Object[]{info[0], info[1], info[2]});
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== DUYỆT MƯỢN ======
    private void approveBorrow() {
        try {
            int selectedRow = tableBorrows.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn một yêu cầu để duyệt!");
                return;
            }

            String book = modelBorrows.getValueAt(selectedRow, 1).toString();
            String qty = modelBorrows.getValueAt(selectedRow, 2).toString();

            dos.writeUTF("APPROVE_BORROW;" + book + ";" + qty);
            dos.flush();

            JOptionPane.showMessageDialog(this, dis.readUTF());
            listBorrows();
            listBooks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientAdmin().setVisible(true));
    }
}
