#!/usr/bin/env bash
set -e

# ==============================================================
# HỆ THỐNG QUẢN LÝ VÉ XEM PHIM (JAVA OOP & MYSQL)
# Script biên dịch và chạy trên Linux / Ubuntu / macOS
# ==============================================================

echo "========================================================"
echo "  BUILD & RUN: HỆ THỐNG QUẢN LÝ VÉ XEM PHIM"
echo "  Java thuần (JDK) + MySQL Connector/J"
echo "========================================================"

# Tạo thư mục output nếu chưa có
mkdir -p out

# Kiểm tra thư viện MySQL connector
if [ ! -d "lib" ] || [ -z "$(ls -A lib/*.jar 2>/dev/null)" ]; then
    echo "[CẢNH BÁO] Không tìm thấy mysql-connector-j.jar trong thư mục lib/!"
    echo "Đang thử tìm trong cache Maven..."
    M2_JAR=$(find ~/.m2/repository/com/mysql/mysql-connector-j -name "mysql-connector-j-*.jar" 2>/dev/null | head -n 1)
    if [ -n "$M2_JAR" ]; then
        mkdir -p lib
        cp "$M2_JAR" lib/
        echo "[OK] Đã sao chép $M2_JAR vào lib/"
    else
        echo "[LỖI] Vui lòng tải thư viện mysql-connector-j.jar và đặt vào thư mục lib/."
        exit 1
    fi
fi

echo ""
echo "[1/2] Đang biên dịch mã nguồn Java (src/ -> out/)..."
javac -encoding UTF-8 -cp "lib/*" -d out \
    src/model/*.java \
    src/utils/*.java \
    src/repository/*.java \
    src/service/*.java \
    src/main/Main.java \
    src/main/WebServer.java

echo "[OK] Biên dịch THÀNH CÔNG 100%! Không có bất kỳ lỗi cú pháp nào."
echo ""

# Lựa chọn chạy: Web Server (mặc định) hoặc Console (nếu truyền tham số console)
if [ "$1" == "console" ]; then
    echo "[2/2] Đang khởi động Giao diện Console (main.Main)..."
    java -cp "out:lib/*" -Dfile.encoding=UTF-8 main.Main
else
    echo "[2/2] Đang khởi động Giao diện Web (main.WebServer)..."
    echo "Truy cập trình duyệt tại: http://localhost:8080"
    java -cp "out:lib/*" -Dfile.encoding=UTF-8 main.WebServer
fi
