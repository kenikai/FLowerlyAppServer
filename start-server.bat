@echo off
echo 🚀 Запуск FlowerlyApp Server...
echo.

REM Устанавливаем Java 17 из Android Studio
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo 📋 Java версия:
java -version

echo.
echo 🔨 Сборка проекта...
call gradlew.bat build

if %ERRORLEVEL% neq 0 (
    echo ❌ Ошибка сборки!
    pause
    exit /b 1
)

echo.
echo 🌺 Запуск сервера на порту 8080...
echo 📡 Сервер будет доступен по адресу: http://localhost:8080
echo 🔗 API endpoints:
echo    - POST /api/auth/register
echo    - POST /api/auth/login
echo    - GET  /health
echo.
echo ⏹️  Для остановки нажмите Ctrl+C
echo.

call gradlew.bat run