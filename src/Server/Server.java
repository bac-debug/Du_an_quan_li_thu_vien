package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

// ===================== CLASS BOOK =====================
class Book {
    String title;
    int quantity;
    String description;

    Book(String title, int quantity, String description) {
        this.title = title;
        this.quantity = quantity;
        this.description = description;
    }

    @Override
    public String toString() {
        return title + ";" + quantity + ";" + description;
    }

    public static Book fromString(String line) {
        String[] parts = line.split(";", 3);
        if (parts.length < 3) return null;
        try {
            return new Book(parts[0], Integer.parseInt(parts[1]), parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

// ===================== CLASS BORROW =====================
class Borrow {
    String username;
    String bookTitle;
    int quantity;

    Borrow(String username, String bookTitle, int quantity) {
        this.username = username;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return username + ";" + bookTitle + ";" + quantity;
    }

    public static Borrow fromString(String line) {
        String[] parts = line.split(";", 3);
        if (parts.length < 3) return null;
        try {
            return new Borrow(parts[0], parts[1], Integer.parseInt(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

// ===================== SERVER =====================
public class Server {
    private static final int PORT = 5555;
    private static final String DATA_FILE = "data.txt";

    // thread-safe lists
    private static final List<String> users = new CopyOnWriteArrayList<>();
    private static final List<Book> books = new CopyOnWriteArrayList<>();
    private static final List<Borrow> borrows = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        loadData();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("📚 Library Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("👉 Client connected: " + socket.getRemoteSocketAddress());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== LOAD DATA =====================
    private static void loadData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) {
            System.out.println("⚠️ Không tìm thấy file dữ liệu, sẽ tạo mới khi lưu.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            String section = "";

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    section = line.trim();
                } else if (!line.trim().isEmpty()) {
                    switch (section) {
                        case "#USERS" -> users.add(line.trim());
                        case "#BOOKS" -> {
                            Book b = Book.fromString(line.trim());
                            if (b != null) books.add(b);
                        }
                        case "#BORROWS" -> {
                            Borrow brw = Borrow.fromString(line.trim());
                            if (brw != null) borrows.add(brw);
                        }
                        default -> {
                            // ignore unknown section lines
                        }
                    }
                }
            }
            System.out.println("✅ Dữ liệu đã load từ file.");
        } catch (IOException e) {
            System.out.println("⚠️ Lỗi khi đọc file dữ liệu: " + e.getMessage());
        }
    }

    // ===================== SAVE DATA =====================
    public static synchronized void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            pw.println("#USERS");
            for (String u : users) pw.println(u);

            pw.println("#BOOKS");
            for (Book b : books) pw.println(b);

            pw.println("#BORROWS");
            for (Borrow br : borrows) pw.println(br);

            System.out.println("💾 Dữ liệu đã lưu xuống file.");
        } catch (IOException e) {
            System.out.println("❌ Lỗi khi lưu dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===================== CLIENT HANDLER =====================
    static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

                while (true) {
                    String request;
                    try {
                        request = dis.readUTF();
                    } catch (EOFException | SocketException se) {
                        System.out.println("❌ Client ngắt kết nối: " + socket.getRemoteSocketAddress());
                        break;
                    }

                    if (request == null || request.trim().isEmpty()) {
                        dos.writeUTF("ERROR;Yêu cầu trống!");
                        continue;
                    }

                    String[] parts = request.split(";", 5);
                    String command = parts[0].trim().toUpperCase();

                    switch (command) {
                        // -------------------- REGISTER --------------------
                        case "REGISTER" -> {
                            if (parts.length < 3) {
                                dos.writeUTF("ERROR;Thiếu tham số REGISTER!");
                                break;
                            }
                            String username = parts[1].trim();
                            String password = parts[2].trim();

                            boolean exists = users.stream()
                                    .anyMatch(u -> u.split(";", 2)[0].equalsIgnoreCase(username));

                            if (exists) {
                                dos.writeUTF("ERROR;Tài khoản đã tồn tại!");
                            } else {
                                users.add(username + ";" + password);
                                saveData();
                                dos.writeUTF("OK;Đăng ký thành công!");
                            }
                        }

                        // -------------------- LOGIN --------------------
                        case "LOGIN" -> {
                            if (parts.length < 3) {
                                dos.writeUTF("ERROR;Thiếu tham số LOGIN!");
                                break;
                            }
                            String username = parts[1].trim();
                            String password = parts[2].trim();
                            String credential = username + ";" + password;

                            if (users.contains(credential)) {
                                // Gửi OK + danh sách sách ngay khi login
                                StringBuilder sb = new StringBuilder("OK;");
                                for (Book book : books) {
                                    sb.append(book.title).append(",")
                                      .append(book.quantity).append(",")
                                      .append(book.description).append("|");
                                }
                                dos.writeUTF(sb.toString());
                            } else {
                                dos.writeUTF("ERROR;Sai tài khoản hoặc mật khẩu!");
                            }
                        }

                        // -------------------- LIST BOOKS --------------------
                        case "LIST_BOOKS" -> {
                            StringBuilder sb = new StringBuilder("OK;");
                            for (Book book : books) {
                                sb.append(book.title).append(",")
                                  .append(book.quantity).append(",")
                                  .append(book.description).append("|");
                            }
                            dos.writeUTF(sb.toString());
                        }

                        // -------------------- BORROW BOOK --------------------
                        case "BORROW" -> {
                            if (parts.length < 4) {
                                dos.writeUTF("ERROR;Thiếu tham số BORROW!");
                                break;
                            }
                            String username = parts[1].trim();
                            String bookName = parts[2].trim();
                            int qty;
                            try {
                                qty = Integer.parseInt(parts[3].trim());
                                if (qty <= 0) {
                                    dos.writeUTF("ERROR;Số lượng phải lớn hơn 0!");
                                    break;
                                }
                            } catch (NumberFormatException nfe) {
                                dos.writeUTF("ERROR;Tham số số lượng không hợp lệ!");
                                break;
                            }

                            Optional<Book> bookOpt = books.stream()
                                    .filter(b -> b.title.equalsIgnoreCase(bookName))
                                    .findFirst();

                            if (bookOpt.isEmpty()) {
                                dos.writeUTF("ERROR;Không tìm thấy sách!");
                            } else {
                                Book book = bookOpt.get();
                                if (book.quantity < qty) {
                                    dos.writeUTF("ERROR;Không đủ số lượng sách!");
                                } else {
                                    borrows.add(new Borrow(username, bookName, qty));
                                    saveData();
                                    dos.writeUTF("OK;Yêu cầu mượn đã gửi, chờ admin duyệt.");
                                }
                            }
                        }

                        // -------------------- MY BORROWS (user) --------------------
                        case "MY_BORROWS" -> {
                            if (parts.length < 2) {
                                dos.writeUTF("ERROR;Thiếu tham số MY_BORROWS!");
                                break;
                            }
                            String username = parts[1].trim();
                            StringBuilder sb = new StringBuilder("OK;");
                            for (Borrow br : borrows) {
                                if (br.username.equalsIgnoreCase(username)) {
                                    // Trả về: bookTitle,Trạng thái|  (client đang parse theo format này)
                                    sb.append(br.bookTitle).append(",").append("Chờ duyệt").append("|");
                                }
                            }
                            dos.writeUTF(sb.toString());
                        }

                        // -------------------- ADMIN: ADD_BOOK --------------------
                        case "ADD_BOOK" -> {
                            if (parts.length < 4) {
                                dos.writeUTF("ERROR;Thiếu tham số ADD_BOOK!");
                                break;
                            }
                            String name = parts[1].trim();
                            int qty;
                            try {
                                qty = Integer.parseInt(parts[2].trim());
                                if (qty < 0) {
                                    dos.writeUTF("ERROR;Số lượng không hợp lệ!");
                                    break;
                                }
                            } catch (NumberFormatException nfe) {
                                dos.writeUTF("ERROR;Tham số số lượng không hợp lệ!");
                                break;
                            }
                            String desc = parts[3].trim();

                            boolean exists = books.stream()
                                    .anyMatch(b -> b.title.equalsIgnoreCase(name));
                            if (exists) {
                                dos.writeUTF("ERROR;Sách đã tồn tại!");
                            } else {
                                books.add(new Book(name, qty, desc));
                                saveData();
                                dos.writeUTF("OK;Thêm sách thành công!");
                            }
                        }

                        // -------------------- LIST_BORROWS (admin) --------------------
                        case "LIST_BORROWS" -> {
                            StringBuilder sb = new StringBuilder("OK;");
                            for (Borrow br : borrows) {
                                sb.append(br.username).append(",")
                                  .append(br.bookTitle).append(",")
                                  .append(br.quantity).append("|");
                            }
                            dos.writeUTF(sb.toString());
                        }

                        // -------------------- APPROVE_BORROW (admin) --------------------
                        case "APPROVE_BORROW" -> {
                            if (parts.length < 3) {
                                dos.writeUTF("ERROR;Thiếu tham số APPROVE_BORROW!");
                                break;
                            }
                            String bookName = parts[1].trim();
                            int qty;
                            try {
                                qty = Integer.parseInt(parts[2].trim());
                            } catch (NumberFormatException nfe) {
                                dos.writeUTF("ERROR;Tham số số lượng không hợp lệ!");
                                break;
                            }

                            Optional<Borrow> borrowOpt = borrows.stream()
                                    .filter(br -> br.bookTitle.equalsIgnoreCase(bookName) && br.quantity == qty)
                                    .findFirst();

                            if (borrowOpt.isEmpty()) {
                                dos.writeUTF("ERROR;Không tìm thấy yêu cầu mượn!");
                            } else {
                                Borrow borrow = borrowOpt.get();
                                String username = borrow.username;

                                Optional<Book> bookOpt = books.stream()
                                        .filter(b -> b.title.equalsIgnoreCase(bookName))
                                        .findFirst();

                                if (bookOpt.isPresent()) {
                                    Book book = bookOpt.get();
                                    if (book.quantity >= qty) {
                                        // đồng bộ khi chỉnh sửa số lượng và danh sách borrows
                                        synchronized (Server.class) {
                                            book.quantity -= qty;
                                            borrows.remove(borrow);
                                            saveData();
                                        }
                                        dos.writeUTF("OK;Đã duyệt cho " + username + " mượn " + qty + " cuốn " + bookName);
                                    } else {
                                        dos.writeUTF("ERROR;Không đủ sách để duyệt!");
                                    }
                                } else {
                                    dos.writeUTF("ERROR;Sách không tồn tại!");
                                }
                            }
                        }

                        // -------------------- DELETE_BOOK (admin) --------------------
                        case "DELETE_BOOK" -> {
                            if (parts.length < 2) {
                                dos.writeUTF("ERROR;Thiếu tham số DELETE_BOOK!");
                                break;
                            }
                            String title = parts[1].trim();
                            boolean removed;
                            synchronized (Server.class) {
                                removed = books.removeIf(b -> b.title.equalsIgnoreCase(title));
                                if (removed) {
                                    borrows.removeIf(br -> br.bookTitle.equalsIgnoreCase(title));
                                    saveData();
                                }
                            }
                            if (removed) {
                                dos.writeUTF("OK;Xóa sách thành công!");
                            } else {
                                dos.writeUTF("ERROR;Không tìm thấy sách để xóa!");
                            }
                        }

                        // -------------------- UNKNOWN COMMAND --------------------
                        default -> dos.writeUTF("ERROR;Lệnh không hợp lệ!");
                    }
                }

            } catch (IOException e) {
                System.out.println("❌ Client handler lỗi: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignore) {}
            }
        }
    }
}
