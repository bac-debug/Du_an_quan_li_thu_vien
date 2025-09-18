package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final String DATA_FILE = "data.txt";

    private static List<String> users = new ArrayList<>();
    private static List<String> books = new ArrayList<>();
    private static List<String> borrows = new ArrayList<>();

    public static void main(String[] args) {
        loadData();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("📚 Library Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("👉 Client connected: " + socket);

                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Đọc dữ liệu từ file
    private static void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            String section = "";

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    section = line;
                } else if (!line.trim().isEmpty()) {
                    switch (section) {
                        case "#USERS":
                            users.add(line);
                            break;
                        case "#BOOKS":
                            books.add(line);
                            break;
                        case "#BORROWS":
                            borrows.add(line);
                            break;
                    }
                }
            }
            System.out.println("✅ Dữ liệu đã load từ file.");
        } catch (IOException e) {
            System.out.println("⚠️ Không tìm thấy file dữ liệu, sẽ tạo mới.");
        }
    }

    // Ghi dữ liệu xuống file
    public static synchronized void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            pw.println("#USERS");
            for (String u : users) pw.println(u);

            pw.println("#BOOKS");
            for (String b : books) pw.println(b);

            pw.println("#BORROWS");
            for (String br : borrows) pw.println(br);

            System.out.println("💾 Dữ liệu đã lưu xuống file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lớp xử lý client
    static class ClientHandler implements Runnable {
        private Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
            ) {
                while (true) {
                    String request = dis.readUTF();
                    String[] parts = request.split(";", 3);
                    String command = parts[0];

                    switch (command) {
                        // ------------------- USER -------------------
                        case "REGISTER": {
                            String username = parts[1];
                            String password = parts[2];
                            if (users.stream().anyMatch(u -> u.split(";")[0].equals(username))) {
                                dos.writeUTF("ERROR;Tài khoản đã tồn tại!");
                            } else {
                                users.add(username + ";" + password);
                                saveData();
                                dos.writeUTF("OK;Đăng ký thành công!");
                            }
                            break;
                        }
                        case "LOGIN": {
                            String username = parts[1];
                            String password = parts[2];
                            if (users.contains(username + ";" + password)) {
                                dos.writeUTF("OK;Đăng nhập thành công!");
                            } else {
                                dos.writeUTF("ERROR;Sai tài khoản hoặc mật khẩu!");
                            }
                            break;
                        }
                        case "LIST_BOOKS": {
                            StringBuilder sb = new StringBuilder();
                            for (String b : books) sb.append(b).append("|");
                            dos.writeUTF("OK;" + sb.toString());
                            break;
                        }
                        case "BORROW": {
                            String username = parts[1];
                            String book = parts[2];

                            if (!books.contains(book)) {
                                dos.writeUTF("ERROR;Không tìm thấy sách!");
                            } else if (borrows.contains(book)) {
                                dos.writeUTF("ERROR;Sách đã có người mượn!");
                            } else {
                                borrows.add(book);
                                saveData();
                                dos.writeUTF("OK;Mượn sách thành công! Đang chờ duyệt.");
                            }
                            break;
                        }

                        // ------------------- ADMIN -------------------
                        case "ADD_BOOK": { // 🔹 Thêm mới
                            String bookName = parts[1];
                            if (books.contains(bookName)) {
                                dos.writeUTF("ERROR;Sách đã tồn tại!");
                            } else {
                                books.add(bookName);
                                saveData();
                                dos.writeUTF("OK;Thêm sách thành công!");
                            }
                            break;
                        }
                        case "EDIT_BOOK": { // 🔹 Thêm mới
                            String oldBook = parts[1];
                            String newBook = parts[2];
                            if (!books.contains(oldBook)) {
                                dos.writeUTF("ERROR;Không tìm thấy sách để sửa!");
                            } else {
                                books.remove(oldBook);
                                books.add(newBook);
                                saveData();
                                dos.writeUTF("OK;Sửa sách thành công!");
                            }
                            break;
                        }
                        case "DELETE_BOOK": { // 🔹 Thêm mới
                            String bookName = parts[1];
                            if (!books.contains(bookName)) {
                                dos.writeUTF("ERROR;Không tìm thấy sách để xóa!");
                            } else {
                                books.remove(bookName);
                                borrows.remove(bookName); // Xóa luôn nếu sách đang mượn
                                saveData();
                                dos.writeUTF("OK;Xóa sách thành công!");
                            }
                            break;
                        }
                        case "APPROVE_BORROW": { // 🔹 Thêm mới
                            String bookName = parts[1];
                            if (!borrows.contains(bookName)) {
                                dos.writeUTF("ERROR;Không có yêu cầu mượn cho sách này!");
                            } else {
                                // Có thể thêm trạng thái "đã duyệt"
                                dos.writeUTF("OK;Đã duyệt yêu cầu mượn sách: " + bookName);
                            }
                            break;
                        }

                        default:
                            dos.writeUTF("ERROR;Lệnh không hợp lệ!");
                    }
                }
            } catch (IOException e) {
                System.out.println("❌ Client ngắt kết nối: " + socket);
            }
        }
    }
}
