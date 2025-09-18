<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Khoa Công nghệ thông tin (Đại học Đại Nam)
    </a>
</h2>
<h2 align="center">
    QUẢN LÍ SÁCH-THƯ VIỆN QUA MẠNG
</h2>
<div align="center">
    <p align="center">
        <img src="aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="fitdnu_logo (3).png" alt="AIoTLab Logo" width="180"/>
        <img src="dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 📖 1. Giới thiệu hệ thống
Hệ thống Quản Lý Thư Viện được xây dựng nhằm hỗ trợ quản lý sách, người dùng và hoạt động mượn/trả sách.  
Chức năng chính:
- Đăng ký và đăng nhập người dùng.
- Quản lý danh mục sách.
- Quản lý thông tin mượn và trả sách.
- Giao tiếp client - server thông qua TCP Socket.

Mục tiêu: Giúp số hóa quy trình quản lý thư viện, giảm thiểu thao tác thủ công, tăng tính chính xác và hiệu quả.

---


## 🔧 2. Các công nghệ được sử dụng
- **Ngôn ngữ lập trình:** Java
- **Mô hình client-server:** Socket TCP (`ServerSocket` & `Socket`)
- **Quản lý mã nguồn:** Git & GitHub

## 🚀 3. Một số hình ảnh hệ thống
<img src="aiotlab_logo.png" alt="User" width="170"/>

## ⚙️ 4. Các bước cài đặtđặt

### 4.1. Cài đặt công cụ, môi trường và các thư viện cần thiết

#### 4.1.1. Tải project.
```
git clone https://gitlab.com/anhlta/odoo-fitdnu.git
```
#### 4.1.2. Cài đặt các thư viện cần thiết
Người sử dụng thực thi các lệnh sau đề cài đặt các thư viện cần thiết

```
sudo apt-get install libxml2-dev libxslt-dev libldap2-dev libsasl2-dev libssl-dev python3.10-distutils python3.10-dev build-essential libssl-dev libffi-dev zlib1g-dev python3.10-venv libpq-dev
```
#### 4.1.3. Khởi tạo môi trường ảo.
- Khởi tạo môi trường ảo
```
python3.10 -m venv ./venv
```
- Thay đổi trình thông dịch sang môi trường ảo
```
source venv/bin/activate
```
- Chạy requirements.txt để cài đặt tiếp các thư viện được yêu cầu
```
pip3 install -r requirements.txt
```
### 4.2. Setup database

Khởi tạo database trên docker bằng việc thực thi file dockercompose.yml.
```
sudo docker-compose up -d
```
### 4.3. Setup tham số chạy cho hệ thống

### 4.4. Chạy hệ thống và cài đặt các ứng dụng cần thiết


## 📝 5. Liên hệ

© 2024 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---

    
