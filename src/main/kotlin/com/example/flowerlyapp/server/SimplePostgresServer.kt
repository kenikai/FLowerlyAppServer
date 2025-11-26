package com.example.flowerlyapp.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import java.util.*
import java.sql.*
import org.mindrot.jbcrypt.BCrypt
import com.example.flowerlyapp.server.models.CartItemResponse

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    // Создаем таблицы при запуске
    try {
        val connection = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/postgres",
            "postgres",
            "password"
        )
        
        try {
            // Создаем таблицу cart_items
            val createCartItemsTable = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS cart_items (
                    id VARCHAR(50) PRIMARY KEY,
                    user_id VARCHAR(50),
                    flower_id VARCHAR(50),
                    quantity INTEGER,
                    price DECIMAL(10,2),
                    created_at BIGINT,
                    updated_at BIGINT
                )
            """)
            createCartItemsTable.executeUpdate()
            
            // Создаем таблицу flowers (если не существует)
            val createFlowersTable = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS flowers (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(200),
                    description TEXT,
                    price DECIMAL(10,2),
                    category_id VARCHAR(50),
                    image_url VARCHAR(500),
                    image_resource_id INTEGER,
                    is_popular BOOLEAN DEFAULT FALSE,
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at BIGINT,
                    updated_at BIGINT
                )
            """)
            createFlowersTable.executeUpdate()
            
            // Добавляем тестовые цветы
            val insertFlower = connection.prepareStatement("""
                INSERT INTO flowers (id, name, description, price, category_id, image_resource_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    description = EXCLUDED.description,
                    price = EXCLUDED.price,
                    image_resource_id = EXCLUDED.image_resource_id,
                    updated_at = EXCLUDED.updated_at
            """)
            
            val currentTime = System.currentTimeMillis()
            insertFlower.setString(1, "flower-1")
            insertFlower.setString(2, "Роза красная")
            insertFlower.setString(3, "Красивая красная роза")
            insertFlower.setDouble(4, 15.99)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 1) // image_resource_id
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower-2")
            insertFlower.setString(2, "Тюльпан желтый")
            insertFlower.setString(3, "Яркий желтый тюльпан")
            insertFlower.setDouble(4, 12.50)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 2) // image_resource_id
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            // Добавляем больше цветов
            insertFlower.setString(1, "flower_3")
            insertFlower.setString(2, "Орхидея белая")
            insertFlower.setString(3, "Элегантная белая орхидея")
            insertFlower.setDouble(4, 6000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 3) // image_resource_id
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_4")
            insertFlower.setString(2, "Лилии розовые")
            insertFlower.setString(3, "Нежные розовые лилии")
            insertFlower.setDouble(4, 3500.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 4) // image_resource_id
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_5")
            insertFlower.setString(2, "Хризантемы")
            insertFlower.setString(3, "Яркие хризантемы")
            insertFlower.setDouble(4, 5000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 5) // image_resource_id
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_6")
            insertFlower.setString(2, "Пионы")
            insertFlower.setString(3, "Пышные пионы")
            insertFlower.setDouble(4, 6000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 6) // image_resource_id
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_7")
            insertFlower.setString(2, "Герберы")
            insertFlower.setString(3, "Яркие герберы")
            insertFlower.setDouble(4, 8000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 7) // image_resource_id
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            // Очищаем таблицу цветов перед добавлением новых
            val clearFlowers = connection.prepareStatement("DELETE FROM flowers")
            clearFlowers.executeUpdate()
            clearFlowers.close()
            
            // Добавляем цветы с уникальными ID изображений
            insertFlower.setString(1, "flower_1")
            insertFlower.setString(2, "101 Роза")
            insertFlower.setString(3, "Классический букет из свежих роз. Подойдет для любого случая.")
            insertFlower.setDouble(4, 8000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 1) // rose101
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_2")
            insertFlower.setString(2, "Букет 'Монстр'")
            insertFlower.setString(3, "Эффектный букет с экзотическими цветами и насыщенными оттенками.")
            insertFlower.setDouble(4, 5000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 2) // monster_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_3")
            insertFlower.setString(2, "Классический 'Пастель'")
            insertFlower.setString(3, "Нежная композиция в пастельных тонах. Идеально для романтического свидания.")
            insertFlower.setDouble(4, 6000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 3) // classic_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_4")
            insertFlower.setString(2, "Букет 'Осень'")
            insertFlower.setString(3, "Теплый осенний букет с желтыми и оранжевыми оттенками.")
            insertFlower.setDouble(4, 8000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 4) // autumn_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_5")
            insertFlower.setString(2, "Коробка 'trick or treaten'")
            insertFlower.setString(3, "Эффектная цветочная композиция в стиле Хэллоуина, сочетающая элегантность и атмосферу праздника.")
            insertFlower.setDouble(4, 5000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 5) // box_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_6")
            insertFlower.setString(2, "Букет 'Валентина'")
            insertFlower.setString(3, "Специальный букет ко Дню Святого Валентина с красными розами.")
            insertFlower.setDouble(4, 6000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 6) // valentine_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_7")
            insertFlower.setString(2, "Букет 'Тюльпанов'")
            insertFlower.setString(3, "Весенний букет из свежих тюльпанов в разных цветах.")
            insertFlower.setDouble(4, 8000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 7) // tulip_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_8")
            insertFlower.setString(2, "Букет 'День рождения'")
            insertFlower.setString(3, "Яркий и веселый букет, специально для Дня Рождения.")
            insertFlower.setDouble(4, 5000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 8) // birthday_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_9")
            insertFlower.setString(2, "Букет 'Лиллия'")
            insertFlower.setString(3, "Изысканный букет с ароматными лилиями.")
            insertFlower.setDouble(4, 6000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 9) // lillies_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            insertFlower.setString(1, "flower_10")
            insertFlower.setString(2, "Букет 'Гербер'")
            insertFlower.setString(3, "Яркий букет из гербер, который поднимет настроение.")
            insertFlower.setDouble(4, 8000.0)
            insertFlower.setString(5, "category-1")
            insertFlower.setInt(6, 10) // gerbera_flower
            insertFlower.setLong(7, currentTime)
            insertFlower.setLong(8, currentTime)
            insertFlower.executeUpdate()
            
            
            println("Таблицы созданы успешно!")
            
        } finally {
            connection.close()
        }
    } catch (e: Exception) {
        println("Ошибка создания таблиц: ${e.message}")
    }
    
    routing {
        get("/") {
            call.respondText("Hello from FlowerlyApp Server!")
        }
        
        get("/health") {
            call.respondText("Server is healthy!")
        }
        
        route("/api/cart") {
            get {
                val userId = call.request.headers["X-User-ID"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, 
                    mapOf("error" to "User ID required")
                )
                
                try {
                    // Подключение к PostgreSQL
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT ci.id, ci.user_id, ci.flower_id, ci.quantity, ci.price, 
                                   ci.created_at, ci.updated_at, f.name as flower_name, f.image_resource_id
                            FROM cart_items ci
                            LEFT JOIN flowers f ON ci.flower_id = f.id
                            WHERE ci.user_id = ?
                        """)
                        stmt.setString(1, userId)
                        val resultSet = stmt.executeQuery()
                        
                        val cartItems = mutableListOf<CartItemResponse>()
                        while (resultSet.next()) {
                            cartItems.add(CartItemResponse(
                                id = resultSet.getString("id"),
                                flowerId = resultSet.getString("flower_id"),
                                flowerName = resultSet.getString("flower_name") ?: "Unknown",
                                quantity = resultSet.getInt("quantity"),
                                price = resultSet.getDouble("price"),
                                totalPrice = resultSet.getDouble("price") * resultSet.getInt("quantity"),
                                imageResourceId = resultSet.getInt("image_resource_id")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, cartItems)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, 
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            post {
                val userId = call.request.headers["X-User-ID"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, 
                    mapOf("error" to "User ID required")
                )
                
                try {
                    val requestBody = call.receiveText()
                    println("Received request body: $requestBody")
                    
                    // Простой парсинг JSON
                    val json = requestBody.replace("{", "").replace("}", "").replace("\"", "")
                    val parts = json.split(",")
                    
                    var flowerId = ""
                    var quantity = 1
                    var price = 0.0
                    
                    for (part in parts) {
                        val keyValue = part.split(":")
                        if (keyValue.size == 2) {
                            val key = keyValue[0].trim()
                            val value = keyValue[1].trim()
                            when (key) {
                                "flowerId" -> flowerId = value
                                "quantity" -> quantity = value.toIntOrNull() ?: 1
                                "price" -> price = value.toDoubleOrNull() ?: 0.0
                            }
                        }
                    }
                    
                    if (flowerId.isEmpty()) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest, 
                            mapOf("error" to "Flower ID required")
                        )
                    }
                    
                    // Подключение к PostgreSQL
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Проверяем, есть ли уже такой товар в корзине
                        val checkStmt = connection.prepareStatement("""
                            SELECT id, quantity FROM cart_items 
                            WHERE user_id = ? AND flower_id = ?
                        """)
                        checkStmt.setString(1, userId)
                        checkStmt.setString(2, flowerId)
                        val resultSet = checkStmt.executeQuery()
                        
                        if (resultSet.next()) {
                            // Обновляем количество
                            val newQuantity = resultSet.getInt("quantity") + quantity
                            val updateStmt = connection.prepareStatement("""
                                UPDATE cart_items 
                                SET quantity = ?, price = ?, updated_at = ?
                                WHERE user_id = ? AND flower_id = ?
                            """)
                            updateStmt.setInt(1, newQuantity)
                            updateStmt.setDouble(2, price)
                            updateStmt.setLong(3, System.currentTimeMillis())
                            updateStmt.setString(4, userId)
                            updateStmt.setString(5, flowerId)
                            updateStmt.executeUpdate()
                        } else {
                            // Добавляем новый товар
                            val insertStmt = connection.prepareStatement("""
                                INSERT INTO cart_items (id, user_id, flower_id, quantity, price, created_at, updated_at)
                                VALUES (?, ?, ?, ?, ?, ?, ?)
                            """)
                            insertStmt.setString(1, UUID.randomUUID().toString())
                            insertStmt.setString(2, userId)
                            insertStmt.setString(3, flowerId)
                            insertStmt.setInt(4, quantity)
                            insertStmt.setDouble(5, price)
                            insertStmt.setLong(6, System.currentTimeMillis())
                            insertStmt.setLong(7, System.currentTimeMillis())
                            insertStmt.executeUpdate()
                        }
                        
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, 
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            put("/{flowerId}") {
                val userId = call.request.headers["X-User-ID"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                val flowerId = call.parameters["flowerId"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Flower ID required")
                )
                
                try {
                    val requestBody = call.receiveText()
                    println("Received update request body: $requestBody")
                    
                    // Простой парсинг JSON
                    val json = requestBody.replace("{", "").replace("}", "").replace("\"", "")
                    val parts = json.split(",")
                    
                    var quantity = 1
                    
                    for (part in parts) {
                        val keyValue = part.split(":")
                        if (keyValue.size == 2) {
                            val key = keyValue[0].trim()
                            val value = keyValue[1].trim()
                            when (key) {
                                "quantity" -> quantity = value.toIntOrNull() ?: 1
                            }
                        }
                    }
                    
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Проверяем, существует ли товар в корзине
                        val checkStmt = connection.prepareStatement("""
                            SELECT id, price FROM cart_items 
                            WHERE user_id = ? AND flower_id = ?
                        """)
                        checkStmt.setString(1, userId)
                        checkStmt.setString(2, flowerId)
                        val resultSet = checkStmt.executeQuery()
                        
                        if (resultSet.next()) {
                            // Обновляем количество
                            val updateStmt = connection.prepareStatement("""
                                UPDATE cart_items 
                                SET quantity = ?, updated_at = ?
                                WHERE user_id = ? AND flower_id = ?
                            """)
                            updateStmt.setInt(1, quantity)
                            updateStmt.setLong(2, System.currentTimeMillis())
                            updateStmt.setString(3, userId)
                            updateStmt.setString(4, flowerId)
                            updateStmt.executeUpdate()
                            
                            call.respond(HttpStatusCode.OK, mapOf("success" to true))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
                        }
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, 
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            delete("/{flowerId}") {
                val userId = call.request.headers["X-User-ID"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                val flowerId = call.parameters["flowerId"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Flower ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val deleteStmt = connection.prepareStatement("""
                            DELETE FROM cart_items 
                            WHERE user_id = ? AND flower_id = ?
                        """)
                        deleteStmt.setString(1, userId)
                        deleteStmt.setString(2, flowerId)
                        val rowsAffected = deleteStmt.executeUpdate()
                        
                        if (rowsAffected > 0) {
                            call.respond(HttpStatusCode.OK, mapOf("success" to true))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
                        }
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, 
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            delete {
                val userId = call.request.headers["X-User-ID"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val deleteStmt = connection.prepareStatement("""
                            DELETE FROM cart_items 
                            WHERE user_id = ?
                        """)
                        deleteStmt.setString(1, userId)
                        deleteStmt.executeUpdate()
                        
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, 
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
        }
        
        route("/api/auth") {
            post("/register") {
                try {
                    val request = call.receive<RegisterRequest>()
                    
                    // Валидация
                    if (request.firstName.isBlank() || request.lastName.isBlank() || 
                        request.email.isBlank() || request.password.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest,
                            AuthResponse(false, "Все поля обязательны для заполнения"))
                        return@post
                    }
                    
                    if (request.password.length < 6) {
                        call.respond(HttpStatusCode.BadRequest,
                            AuthResponse(false, "Пароль должен содержать минимум 6 символов"))
                        return@post
                    }
                    
                    // Подключение к PostgreSQL
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Проверяем, существует ли пользователь
                        val checkStmt = connection.prepareStatement("SELECT id FROM users WHERE email = ?")
                        checkStmt.setString(1, request.email)
                        val resultSet = checkStmt.executeQuery()
                        
                        if (resultSet.next()) {
                            call.respond(HttpStatusCode.BadRequest,
                                AuthResponse(false, "Пользователь с таким email уже существует"))
                            return@post
                        }
                        
                        // Сохраняем нового пользователя
                        val userId = UUID.randomUUID().toString()
                        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
                        val currentTime = System.currentTimeMillis()
                        
                        val insertStmt = connection.prepareStatement("""
                            INSERT INTO users (id, first_name, last_name, email, password_hash, created_at, updated_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                        """)
                        insertStmt.setString(1, userId)
                        insertStmt.setString(2, request.firstName)
                        insertStmt.setString(3, request.lastName)
                        insertStmt.setString(4, request.email)
                        insertStmt.setString(5, passwordHash)
                        insertStmt.setLong(6, currentTime)
                        insertStmt.setLong(7, currentTime)
                        insertStmt.executeUpdate()
                        
                        val token = "jwt_token_${System.currentTimeMillis()}"
                        
                        val user = UserResponse(
                            id = userId,
                            firstName = request.firstName,
                            lastName = request.lastName,
                            email = request.email,
                            createdAt = currentTime
                        )
                        
                        val response = AuthResponse(
                            success = true,
                            message = "Регистрация успешна",
                            token = token,
                            user = user
                        )
                        
                        call.respond(HttpStatusCode.Created, response)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        AuthResponse(false, "Ошибка сервера: ${e.message}"))
                }
            }
            
            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()
                    
                    if (request.email.isBlank() || request.password.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest,
                            AuthResponse(false, "Email и пароль не могут быть пустыми"))
                        return@post
                    }
                    
                    // Подключение к PostgreSQL
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Ищем пользователя
                        val stmt = connection.prepareStatement("""
                            SELECT id, first_name, last_name, email, password_hash, created_at
                            FROM users WHERE email = ?
                        """)
                        stmt.setString(1, request.email)
                        val resultSet = stmt.executeQuery()
                        
                        if (!resultSet.next()) {
                            call.respond(HttpStatusCode.Unauthorized,
                                AuthResponse(false, "Пользователь не найден"))
                            return@post
                        }
                        
                        val passwordHash = resultSet.getString("password_hash")
                        if (!BCrypt.checkpw(request.password, passwordHash)) {
                            call.respond(HttpStatusCode.Unauthorized,
                                AuthResponse(false, "Неверный пароль"))
                            return@post
                        }
                        
                        val userId = resultSet.getString("id")
                        val token = "jwt_token_${System.currentTimeMillis()}"
                        
                        val user = UserResponse(
                            id = userId,
                            firstName = resultSet.getString("first_name"),
                            lastName = resultSet.getString("last_name"),
                            email = resultSet.getString("email"),
                            createdAt = resultSet.getLong("created_at")
                        )
                        
                        val response = AuthResponse(
                            success = true,
                            message = "Вход выполнен успешно",
                            token = token,
                            user = user
                        )
                        
                        call.respond(HttpStatusCode.OK, response)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        AuthResponse(false, "Ошибка сервера: ${e.message}"))
                }
            }
        }
        
        route("/api/flowers") {
            // GET /api/flowers - Получение списка всех цветов
            get {
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT id, name, description, price, category_id, 
                                   image_resource_id, is_popular, is_active, 
                                   created_at, updated_at
                            FROM flowers
                            WHERE is_active = true
                            ORDER BY created_at DESC
                        """)
                        val resultSet = stmt.executeQuery()
                        
                        val flowers = mutableListOf<FlowerResponse>()
                        while (resultSet.next()) {
                            flowers.add(FlowerResponse(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                price = resultSet.getDouble("price"),
                                categoryId = resultSet.getString("category_id"),
                                imageResourceId = resultSet.getInt("image_resource_id"),
                                isPopular = resultSet.getBoolean("is_popular"),
                                isActive = resultSet.getBoolean("is_active"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, flowers)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // GET /api/flowers/{id} - Получение информации о конкретном цветке
            get("/{id}") {
                try {
                    val flowerId = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Flower ID required")
                    )
                    
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT id, name, description, price, category_id, 
                                   image_resource_id, is_popular, is_active, 
                                   created_at, updated_at
                            FROM flowers
                            WHERE id = ? AND is_active = true
                        """)
                        stmt.setString(1, flowerId)
                        val resultSet = stmt.executeQuery()
                        
                        if (resultSet.next()) {
                            val flower = FlowerResponse(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                price = resultSet.getDouble("price"),
                                categoryId = resultSet.getString("category_id"),
                                imageResourceId = resultSet.getInt("image_resource_id"),
                                isPopular = resultSet.getBoolean("is_popular"),
                                isActive = resultSet.getBoolean("is_active"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at")
                            )
                            call.respond(HttpStatusCode.OK, flower)
                        } else {
                            call.respond(HttpStatusCode.NotFound,
                                mapOf("error" to "Flower not found"))
                        }
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // GET /api/flowers/popular - Получение популярных цветов
            get("/popular") {
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT id, name, description, price, category_id, 
                                   image_resource_id, is_popular, is_active, 
                                   created_at, updated_at
                            FROM flowers
                            WHERE is_popular = true AND is_active = true
                            ORDER BY created_at DESC
                        """)
                        val resultSet = stmt.executeQuery()
                        
                        val flowers = mutableListOf<FlowerResponse>()
                        while (resultSet.next()) {
                            flowers.add(FlowerResponse(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                price = resultSet.getDouble("price"),
                                categoryId = resultSet.getString("category_id"),
                                imageResourceId = resultSet.getInt("image_resource_id"),
                                isPopular = resultSet.getBoolean("is_popular"),
                                isActive = resultSet.getBoolean("is_active"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, flowers)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // GET /api/flowers/search?q=query - Поиск цветов
            get("/search") {
                try {
                    val query = call.request.queryParameters["q"] ?: ""
                    
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT id, name, description, price, category_id, 
                                   image_resource_id, is_popular, is_active, 
                                   created_at, updated_at
                            FROM flowers
                            WHERE (LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?))
                              AND is_active = true
                            ORDER BY created_at DESC
                        """)
                        val searchPattern = "%$query%"
                        stmt.setString(1, searchPattern)
                        stmt.setString(2, searchPattern)
                        val resultSet = stmt.executeQuery()
                        
                        val flowers = mutableListOf<FlowerResponse>()
                        while (resultSet.next()) {
                            flowers.add(FlowerResponse(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                price = resultSet.getDouble("price"),
                                categoryId = resultSet.getString("category_id"),
                                imageResourceId = resultSet.getInt("image_resource_id"),
                                isPopular = resultSet.getBoolean("is_popular"),
                                isActive = resultSet.getBoolean("is_active"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, flowers)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // GET /api/flowers/category/{categoryId} - Получение цветов по категории
            get("/category/{categoryId}") {
                try {
                    val categoryId = call.parameters["categoryId"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Category ID required")
                    )
                    
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT id, name, description, price, category_id, 
                                   image_resource_id, is_popular, is_active, 
                                   created_at, updated_at
                            FROM flowers
                            WHERE category_id = ? AND is_active = true
                            ORDER BY created_at DESC
                        """)
                        stmt.setString(1, categoryId)
                        val resultSet = stmt.executeQuery()
                        
                        val flowers = mutableListOf<FlowerResponse>()
                        while (resultSet.next()) {
                            flowers.add(FlowerResponse(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                price = resultSet.getDouble("price"),
                                categoryId = resultSet.getString("category_id"),
                                imageResourceId = resultSet.getInt("image_resource_id"),
                                isPopular = resultSet.getBoolean("is_popular"),
                                isActive = resultSet.getBoolean("is_active"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, flowers)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
        }
        
        route("/api/categories") {
            // GET /api/categories - Получение списка всех категорий
            get {
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT DISTINCT category_id
                            FROM flowers
                            WHERE is_active = true
                            ORDER BY category_id
                        """)
                        val resultSet = stmt.executeQuery()
                        
                        val categories = mutableListOf<CategoryResponse>()
                        while (resultSet.next()) {
                            categories.add(CategoryResponse(
                                id = resultSet.getString("category_id"),
                                name = resultSet.getString("category_id")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, categories)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
        }
        
        route("/api/favorites") {
            // GET /api/favorites - Получение списка избранных цветов
            get {
                val userId = call.request.headers["X-User-ID"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Создаем таблицу favorites, если не существует
                        val createFavoritesTable = connection.prepareStatement("""
                            CREATE TABLE IF NOT EXISTS favorites (
                                id VARCHAR(50) PRIMARY KEY,
                                user_id VARCHAR(50) NOT NULL,
                                flower_id VARCHAR(50) NOT NULL,
                                created_at BIGINT,
                                UNIQUE(user_id, flower_id)
                            )
                        """)
                        createFavoritesTable.executeUpdate()
                        
                        val stmt = connection.prepareStatement("""
                            SELECT f.id, f.name, f.description, f.price, f.category_id, 
                                   f.image_resource_id, f.is_popular, f.is_active, 
                                   f.created_at, f.updated_at, fav.created_at as favorited_at
                            FROM favorites fav
                            JOIN flowers f ON fav.flower_id = f.id
                            WHERE fav.user_id = ? AND f.is_active = true
                            ORDER BY fav.created_at DESC
                        """)
                        stmt.setString(1, userId)
                        val resultSet = stmt.executeQuery()
                        
                        val favorites = mutableListOf<FavoriteResponse>()
                        while (resultSet.next()) {
                            favorites.add(FavoriteResponse(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                price = resultSet.getDouble("price"),
                                categoryId = resultSet.getString("category_id"),
                                imageResourceId = resultSet.getInt("image_resource_id"),
                                isPopular = resultSet.getBoolean("is_popular"),
                                isActive = resultSet.getBoolean("is_active"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at"),
                                favoritedAt = resultSet.getLong("favorited_at")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, favorites)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // POST /api/favorites - Добавление цветка в избранное
            post {
                val userId = call.request.headers["X-User-ID"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                
                try {
                    val requestBody = call.receiveText()
                    val json = requestBody.replace("{", "").replace("}", "").replace("\"", "")
                    val parts = json.split(",")
                    
                    var flowerId = ""
                    for (part in parts) {
                        val keyValue = part.split(":")
                        if (keyValue.size == 2) {
                            val key = keyValue[0].trim()
                            val value = keyValue[1].trim()
                            if (key == "flowerId") {
                                flowerId = value
                            }
                        }
                    }
                    
                    if (flowerId.isEmpty()) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Flower ID required")
                        )
                    }
                    
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Создаем таблицу favorites, если не существует
                        val createFavoritesTable = connection.prepareStatement("""
                            CREATE TABLE IF NOT EXISTS favorites (
                                id VARCHAR(50) PRIMARY KEY,
                                user_id VARCHAR(50) NOT NULL,
                                flower_id VARCHAR(50) NOT NULL,
                                created_at BIGINT,
                                UNIQUE(user_id, flower_id)
                            )
                        """)
                        createFavoritesTable.executeUpdate()
                        
                        val insertStmt = connection.prepareStatement("""
                            INSERT INTO favorites (id, user_id, flower_id, created_at)
                            VALUES (?, ?, ?, ?)
                            ON CONFLICT (user_id, flower_id) DO NOTHING
                        """)
                        insertStmt.setString(1, UUID.randomUUID().toString())
                        insertStmt.setString(2, userId)
                        insertStmt.setString(3, flowerId)
                        insertStmt.setLong(4, System.currentTimeMillis())
                        insertStmt.executeUpdate()
                        
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // DELETE /api/favorites/{flowerId} - Удаление цветка из избранного
            delete("/{flowerId}") {
                val userId = call.request.headers["X-User-ID"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                val flowerId = call.parameters["flowerId"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Flower ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val deleteStmt = connection.prepareStatement("""
                            DELETE FROM favorites 
                            WHERE user_id = ? AND flower_id = ?
                        """)
                        deleteStmt.setString(1, userId)
                        deleteStmt.setString(2, flowerId)
                        val rowsAffected = deleteStmt.executeUpdate()
                        
                        if (rowsAffected > 0) {
                            call.respond(HttpStatusCode.OK, mapOf("success" to true))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Favorite not found"))
                        }
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
        }
        
        route("/api/profile") {
            // GET /api/profile - Получение профиля пользователя
            get {
                val userId = call.request.headers["X-User-ID"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val stmt = connection.prepareStatement("""
                            SELECT id, first_name, last_name, email, created_at, updated_at
                            FROM users
                            WHERE id = ?
                        """)
                        stmt.setString(1, userId)
                        val resultSet = stmt.executeQuery()
                        
                        if (resultSet.next()) {
                            val profile = ProfileResponse(
                                id = resultSet.getString("id"),
                                firstName = resultSet.getString("first_name"),
                                lastName = resultSet.getString("last_name"),
                                email = resultSet.getString("email"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at")
                            )
                            call.respond(HttpStatusCode.OK, profile)
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                        }
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
        }
        
        route("/api/orders") {
            // GET /api/orders - Получение истории заказов пользователя
            get {
                val userId = call.request.headers["X-User-ID"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Создаем таблицы orders и order_items, если не существуют
                        val createOrdersTable = connection.prepareStatement("""
                            CREATE TABLE IF NOT EXISTS orders (
                                id VARCHAR(50) PRIMARY KEY,
                                user_id VARCHAR(50) NOT NULL,
                                total_amount DECIMAL(10,2),
                                status VARCHAR(50),
                                created_at BIGINT,
                                updated_at BIGINT
                            )
                        """)
                        createOrdersTable.executeUpdate()
                        
                        val createOrderItemsTable = connection.prepareStatement("""
                            CREATE TABLE IF NOT EXISTS order_items (
                                id VARCHAR(50) PRIMARY KEY,
                                order_id VARCHAR(50) NOT NULL,
                                flower_id VARCHAR(50) NOT NULL,
                                quantity INTEGER,
                                price DECIMAL(10,2),
                                created_at BIGINT
                            )
                        """)
                        createOrderItemsTable.executeUpdate()
                        
                        val stmt = connection.prepareStatement("""
                            SELECT id, user_id, total_amount, status, created_at, updated_at
                            FROM orders
                            WHERE user_id = ?
                            ORDER BY created_at DESC
                        """)
                        stmt.setString(1, userId)
                        val resultSet = stmt.executeQuery()
                        
                        val orders = mutableListOf<OrderResponse>()
                        while (resultSet.next()) {
                            orders.add(OrderResponse(
                                id = resultSet.getString("id"),
                                userId = resultSet.getString("user_id"),
                                totalAmount = resultSet.getDouble("total_amount"),
                                status = resultSet.getString("status"),
                                createdAt = resultSet.getLong("created_at"),
                                updatedAt = resultSet.getLong("updated_at")
                            ))
                        }
                        
                        call.respond(HttpStatusCode.OK, orders)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // POST /api/orders - Создание нового заказа из корзины
            post {
                val userId = call.request.headers["X-User-ID"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        // Создаем таблицы orders и order_items, если не существуют
                        val createOrdersTable = connection.prepareStatement("""
                            CREATE TABLE IF NOT EXISTS orders (
                                id VARCHAR(50) PRIMARY KEY,
                                user_id VARCHAR(50) NOT NULL,
                                total_amount DECIMAL(10,2),
                                status VARCHAR(50),
                                created_at BIGINT,
                                updated_at BIGINT
                            )
                        """)
                        createOrdersTable.executeUpdate()
                        
                        val createOrderItemsTable = connection.prepareStatement("""
                            CREATE TABLE IF NOT EXISTS order_items (
                                id VARCHAR(50) PRIMARY KEY,
                                order_id VARCHAR(50) NOT NULL,
                                flower_id VARCHAR(50) NOT NULL,
                                quantity INTEGER,
                                price DECIMAL(10,2),
                                created_at BIGINT
                            )
                        """)
                        createOrderItemsTable.executeUpdate()
                        
                        // Получаем товары из корзины
                        val cartStmt = connection.prepareStatement("""
                            SELECT flower_id, quantity, price
                            FROM cart_items
                            WHERE user_id = ?
                        """)
                        cartStmt.setString(1, userId)
                        val cartResult = cartStmt.executeQuery()
                        
                        val cartItems = mutableListOf<Triple<String, Int, Double>>()
                        var totalAmount = 0.0
                        while (cartResult.next()) {
                            val flowerId = cartResult.getString("flower_id")
                            val quantity = cartResult.getInt("quantity")
                            val price = cartResult.getDouble("price")
                            cartItems.add(Triple(flowerId, quantity, price))
                            totalAmount += price * quantity
                        }
                        
                        if (cartItems.isEmpty()) {
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Cart is empty")
                            )
                        }
                        
                        // Создаем заказ
                        val orderId = UUID.randomUUID().toString()
                        val currentTime = System.currentTimeMillis()
                        
                        val insertOrderStmt = connection.prepareStatement("""
                            INSERT INTO orders (id, user_id, total_amount, status, created_at, updated_at)
                            VALUES (?, ?, ?, ?, ?, ?)
                        """)
                        insertOrderStmt.setString(1, orderId)
                        insertOrderStmt.setString(2, userId)
                        insertOrderStmt.setDouble(3, totalAmount)
                        insertOrderStmt.setString(4, "pending")
                        insertOrderStmt.setLong(5, currentTime)
                        insertOrderStmt.setLong(6, currentTime)
                        insertOrderStmt.executeUpdate()
                        
                        // Добавляем товары в заказ
                        for ((flowerId, quantity, price) in cartItems) {
                            val insertItemStmt = connection.prepareStatement("""
                                INSERT INTO order_items (id, order_id, flower_id, quantity, price, created_at)
                                VALUES (?, ?, ?, ?, ?, ?)
                            """)
                            insertItemStmt.setString(1, UUID.randomUUID().toString())
                            insertItemStmt.setString(2, orderId)
                            insertItemStmt.setString(3, flowerId)
                            insertItemStmt.setInt(4, quantity)
                            insertItemStmt.setDouble(5, price)
                            insertItemStmt.setLong(6, currentTime)
                            insertItemStmt.executeUpdate()
                        }
                        
                        // Очищаем корзину
                        val clearCartStmt = connection.prepareStatement("""
                            DELETE FROM cart_items WHERE user_id = ?
                        """)
                        clearCartStmt.setString(1, userId)
                        clearCartStmt.executeUpdate()
                        
                        val order = OrderResponse(
                            id = orderId,
                            userId = userId,
                            totalAmount = totalAmount,
                            status = "pending",
                            createdAt = currentTime,
                            updatedAt = currentTime,
                            itemsCount = cartItems.size
                        )
                        
                        call.respond(HttpStatusCode.Created, order)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
            
            // GET /api/orders/{orderId} - Получение деталей заказа
            get("/{orderId}") {
                val userId = call.request.headers["X-User-ID"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID required")
                )
                val orderId = call.parameters["orderId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Order ID required")
                )
                
                try {
                    val connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "password"
                    )
                    
                    try {
                        val orderStmt = connection.prepareStatement("""
                            SELECT id, user_id, total_amount, status, created_at, updated_at
                            FROM orders
                            WHERE id = ? AND user_id = ?
                        """)
                        orderStmt.setString(1, orderId)
                        orderStmt.setString(2, userId)
                        val orderResult = orderStmt.executeQuery()
                        
                        if (!orderResult.next()) {
                            return@get call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "Order not found")
                            )
                        }
                        
                        // Получаем товары заказа
                        val itemsStmt = connection.prepareStatement("""
                            SELECT oi.flower_id, oi.quantity, oi.price, f.name as flower_name
                            FROM order_items oi
                            LEFT JOIN flowers f ON oi.flower_id = f.id
                            WHERE oi.order_id = ?
                        """)
                        itemsStmt.setString(1, orderId)
                        val itemsResult = itemsStmt.executeQuery()
                        
                        val items = mutableListOf<OrderItemResponse>()
                        while (itemsResult.next()) {
                            items.add(OrderItemResponse(
                                flowerId = itemsResult.getString("flower_id"),
                                flowerName = itemsResult.getString("flower_name") ?: "Unknown",
                                quantity = itemsResult.getInt("quantity"),
                                price = itemsResult.getDouble("price")
                            ))
                        }
                        
                        val orderDetails = OrderDetailResponse(
                            id = orderResult.getString("id"),
                            userId = orderResult.getString("user_id"),
                            totalAmount = orderResult.getDouble("total_amount"),
                            status = orderResult.getString("status"),
                            createdAt = orderResult.getLong("created_at"),
                            updatedAt = orderResult.getLong("updated_at"),
                            items = items
                        )
                        call.respond(HttpStatusCode.OK, orderDetails)
                        
                    } finally {
                        connection.close()
                    }
                    
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        mapOf("error" to "Database error: ${e.message}"))
                }
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

@kotlinx.serialization.Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@kotlinx.serialization.Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: UserResponse? = null
)

@kotlinx.serialization.Serializable
data class UserResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: Long
)

@kotlinx.serialization.Serializable
data class FlowerResponse(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: String,
    val imageResourceId: Int,
    val isPopular: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@kotlinx.serialization.Serializable
data class CategoryResponse(
    val id: String,
    val name: String
)

@kotlinx.serialization.Serializable
data class FavoriteResponse(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: String,
    val imageResourceId: Int,
    val isPopular: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val favoritedAt: Long
)

@kotlinx.serialization.Serializable
data class OrderItemResponse(
    val flowerId: String,
    val flowerName: String,
    val quantity: Int,
    val price: Double
)

@kotlinx.serialization.Serializable
data class OrderResponse(
    val id: String,
    val userId: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val itemsCount: Int? = null
)

@kotlinx.serialization.Serializable
data class OrderDetailResponse(
    val id: String,
    val userId: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val items: List<OrderItemResponse>
)

@kotlinx.serialization.Serializable
data class ProfileResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: Long,
    val updatedAt: Long
)
