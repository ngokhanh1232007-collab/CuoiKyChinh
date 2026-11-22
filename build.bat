@echo off
chcp 65001 >nul
echo ========================================================
echo   BUILD ^& RUN: HE THONG QUAN LY VE XEM PHIM
echo   Java thuan (JDK) + MySQL Connector/J
echo ========================================================

REM Tao thu muc output neu chua co
if not exist out mkdir out
if not exist lib mkdir lib

echo.
echo [1/2] Dang bien dich ma nguon Java (src\ -^> out\)...
javac -encoding UTF-8 -cp "lib/*;lib\*" -d out src\model\*.java src\utils\*.java src\repository\*.java src\service\*.java src\main\Main.java src\main\WebServer.java

if %ERRORLEVEL% EQU 0 (
    echo [OK] Bien dich THANH CONG 100%%! Khong co loi cu phap.
    echo.
    if "%1"=="console" (
        echo [2/2] Dang khoi chay Giao dien Console (main.Main)...
        java -cp "out;lib/*;lib\*" -Dfile.encoding=UTF-8 main.Main
        goto end
    )
    if "%1"=="--no-pause" (
        echo [2/2] Dang khoi chay Giao dien Web (main.WebServer)...
        echo Truy cap trinh duyet tai: http://localhost:8080
        java -cp "out;lib/*;lib\*" -Dfile.encoding=UTF-8 main.WebServer
        goto end
    )
    echo [2/2] Chay Web Server tai http://localhost:8080
    echo (Nhan Enter de chay, hoac truyen tham so "console" de chay Console)
    echo.
    pause
    java -cp "out;lib/*;lib\*" -Dfile.encoding=UTF-8 main.WebServer
) else (
    echo [LOI] Bien dich THAT BAI! Vui long kiem tra loi o tren.
    if not "%1"=="--no-pause" pause
)

:end
