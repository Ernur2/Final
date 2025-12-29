package com.bank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bank_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "pass1234"; // ИЗМЕНИТЕ НА ВАШ ПАРОЛЬ!!!
    
    private static Connection connection = null;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Подключение к базе данных успешно!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver не найден!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Ошибка подключения к базе данных!");
            System.err.println("Проверьте:");
            System.err.println("1. MySQL Server запущен");
            System.err.println("2. Правильность логина: " + USER);
            System.err.println("3. Правильность пароля");
            System.err.println("4. База данных существует или может быть создана");
            e.printStackTrace();
        }
        return connection;
    }
    
    public static void initializeDatabase() {
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("❌ Не удалось подключиться к базе данных. Приложение будет работать без БД.");
            return;
        }
        
        try (Statement stmt = conn.createStatement()) {
            
            // ==================== ТАБЛИЦА ПОЛЬЗОВАТЕЛЕЙ ====================
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_username (username)
                ) ENGINE=InnoDB
            """;
            
            // ==================== ТАБЛИЦА ИСТОРИИ ДЕЙСТВИЙ ====================
            String createAuditLogsTable = """
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    action VARCHAR(50) NOT NULL,
                    details TEXT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id),
                    INDEX idx_timestamp (timestamp)
                ) ENGINE=InnoDB
            """;
            
            // ==================== ТАБЛИЦА КЛИЕНТОВ (С ЛОГИНОМ И ПАРОЛЕМ) ====================
            String createCustomersTable = """
                CREATE TABLE IF NOT EXISTS customers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    phone VARCHAR(20),
                    email VARCHAR(100),
                    address TEXT,
                    username VARCHAR(50) UNIQUE,
                    password VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_full_name (full_name),
                    INDEX idx_username (username)
                ) ENGINE=InnoDB
            """;
            
            // ==================== ТАБЛИЦА СЧЕТОВ ====================
            String createAccountsTable = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    account_number VARCHAR(20) UNIQUE NOT NULL,
                    customer_id INT NOT NULL,
                    balance DECIMAL(15, 2) DEFAULT 0.00,
                    account_type VARCHAR(20) DEFAULT 'Savings',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
                    INDEX idx_customer_id (customer_id),
                    INDEX idx_account_number (account_number)
                ) ENGINE=InnoDB
            """;

            // ==================== ТАБЛИЦА ЗАЯВОК НА ОТКРЫТИЕ СЧЕТА ====================
            String createAccountRequestsTable = """
                CREATE TABLE IF NOT EXISTS account_requests (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    customer_id INT NOT NULL,
                    account_type VARCHAR(20) NOT NULL,
                    status VARCHAR(20) DEFAULT 'PENDING',
                    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    approved_by INT,
                    approved_date TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
                    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
                    INDEX idx_customer_id (customer_id),
                    INDEX idx_status (status)
                ) ENGINE=InnoDB
            """;
            
            // ==================== ТАБЛИЦА ТРАНЗАКЦИЙ ====================
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    account_id INT NOT NULL,
                    transaction_type VARCHAR(20) NOT NULL,
                    amount DECIMAL(15, 2) NOT NULL,
                    description TEXT,
                    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
                    INDEX idx_account_id (account_id),
                    INDEX idx_transaction_date (transaction_date)
                ) ENGINE=InnoDB
            """;
            
            // Создание всех таблиц
            stmt.execute(createUsersTable);
            System.out.println("✅ Таблица 'users' создана/проверена");
            
            stmt.execute(createAuditLogsTable);
            System.out.println("✅ Таблица 'audit_logs' создана/проверена");
            
            stmt.execute(createCustomersTable);
            System.out.println("✅ Таблица 'customers' создана/проверена");
            
            stmt.execute(createAccountsTable);
            System.out.println("✅ Таблица 'accounts' создана/проверена");

            stmt.execute(createAccountRequestsTable);
            System.out.println("✅ Таблица 'account_requests' создана/проверена");
            
            stmt.execute(createTransactionsTable);
            System.out.println("✅ Таблица 'transactions' создана/проверена");
            
            // Создать пользователей и тестовые данные
            createDefaultAdmin(conn);
            createTestCustomer(conn);
            
            System.out.println("✅ Все таблицы базы данных созданы успешно!");
            
        } catch (SQLException e) {
            System.err.println("❌ Ошибка инициализации базы данных!");
            e.printStackTrace();
        }
    }
    
    /**
     * Создает администратора и кассира по умолчанию
     */
    private static void createDefaultAdmin(Connection conn) {
        try {
            // Проверить, существует ли пользователь admin
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(checkSql);
            rs.next();
            
            if (rs.getInt(1) == 0) {
                // Создать админа с логином: admin, пароль: admin123
                String insertSql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, "admin");
                // SHA-256 хэш для пароля "admin123"
                pstmt.setString(2, "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9");
                pstmt.setString(3, "Администратор");
                pstmt.setString(4, "ADMIN");
                pstmt.executeUpdate();
                
                System.out.println("✅ Создан пользователь по умолчанию:");
                System.out.println("   👤 Логин: admin");
                System.out.println("   🔑 Пароль: admin123");
                System.out.println("   📋 Роль: ADMIN");
                
                // Создать тестового кассира
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, "cashier");
                // SHA-256 хэш для пароля "cashier123"
                pstmt.setString(2, "8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72");
                pstmt.setString(3, "Кассир");
                pstmt.setString(4, "CASHIER");
                pstmt.executeUpdate();
                
                System.out.println("✅ Создан тестовый кассир:");
                System.out.println("   👤 Логин: cashier");
                System.out.println("   🔑 Пароль: cashier123");
                System.out.println("   📋 Роль: CASHIER");
            } else {
                System.out.println("ℹ️  Пользователи уже существуют в системе");
            }
        } catch (SQLException e) {
            System.err.println("❌ Ошибка при создании пользователя по умолчанию");
            e.printStackTrace();
        }
    }
    
    /**
     * Создает тестового клиента с логином для демонстрации
     */
    private static void createTestCustomer(Connection conn) {
        try {
            // Проверить, существует ли клиент с логином testclient
            String checkSql = "SELECT COUNT(*) FROM customers WHERE username = 'testclient'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(checkSql);
            rs.next();
            
            if (rs.getInt(1) == 0) {
                // Создать тестового клиента
                String insertSql = "INSERT INTO customers (full_name, phone, email, address, username, password) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, "Тестовый Клиент");
                pstmt.setString(2, "+77771234567");
                pstmt.setString(3, "test@example.com");
                pstmt.setString(4, "Астана, ул. Тестовая 1");
                pstmt.setString(5, "testclient");
                // SHA-256 хэш для пароля "test123"
                pstmt.setString(6, "ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae");
                pstmt.executeUpdate();
                
                // Получить ID созданного клиента
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int customerId = generatedKeys.getInt(1);
                    
                    // Создать тестовый счет для клиента
                    String accountSql = "INSERT INTO accounts (account_number, customer_id, balance, account_type) VALUES (?, ?, ?, ?)";
                    PreparedStatement accountPstmt = conn.prepareStatement(accountSql);
                    accountPstmt.setString(1, "1234567890");
                    accountPstmt.setInt(2, customerId);
                    accountPstmt.setDouble(3, 5000.00);
                    accountPstmt.setString(4, "Savings");
                    accountPstmt.executeUpdate();
                    
                    System.out.println("✅ Создан тестовый клиент:");
                    System.out.println("   👤 Логин: testclient");
                    System.out.println("   🔑 Пароль: test123");
                    System.out.println("   💳 Счет: 1234567890");
                    System.out.println("   💰 Баланс: $5000.00");
                }
            } else {
                System.out.println("ℹ️  Тестовый клиент уже существует");
            }
        } catch (SQLException e) {
            System.err.println("❌ Ошибка при создании тестового клиента");
            e.printStackTrace();
        }
    }
    
    /**
     * Закрыть соединение с базой данных
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Соединение с базой данных закрыто.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}