#!/bin/bash

echo "🚀 Запуск FlowerlyApp Server..."
echo

echo "📋 Проверка PostgreSQL..."
if ! pg_isready -h localhost -p 5432; then
    echo "❌ PostgreSQL не запущен!"
    echo "💡 Запустите PostgreSQL и попробуйте снова"
    exit 1
fi

echo "✅ PostgreSQL запущен"
echo

echo "🔧 Сборка проекта..."
./gradlew build
if [ $? -ne 0 ]; then
    echo "❌ Ошибка сборки!"
    exit 1
fi

echo "✅ Проект собран"
echo

echo "🌐 Запуск сервера на http://localhost:8080"
echo "📧 Тестовый пользователь: test@example.com"
echo "🔑 Пароль: password123"
echo

./gradlew run
