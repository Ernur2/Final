package com.bank.service;

import com.bank.database.DatabaseConnection;
import com.bank.model.User;
import com.bank.model.AuditLog;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    
    private User currentUser = null;
    
    // Хэширование пароля
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback
        }
    }
    
    // Вход в систему
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentUser = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("role")
                );
                logAction("LOGIN", "Пользователь вошел в систему");
                return currentUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Выход из системы
    public void logout() {
        if (currentUser != null) {
            logAction("LOGOUT", "Пользователь вышел из системы");
            currentUser = null;
        }
    }
    
    // Получить текущего пользователя
    public User getCurrentUser() {
        return currentUser;
    }
    
    // Проверка, авторизован ли пользователь
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    // Создание нового пользователя (только для админа)
    public boolean createUser(User user) {
        if (currentUser != null && !currentUser.isAdmin()) {
            System.err.println("Только администратор может создавать пользователей");
            return false;
        }
        
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashPassword(user.getPassword()));
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            pstmt.executeUpdate();
            
            logAction("CREATE_USER", "Создан новый пользователь: " + user.getUsername());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Получить всех пользователей
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, full_name, role FROM users ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    "",
                    rs.getString("full_name"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    // Логирование действий
    public void logAction(String action, String details) {
        if (currentUser == null) return;
        
        String sql = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Получить историю действий
    public List<AuditLog> getAuditLogs(Integer userId, int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = userId != null 
            ? "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?"
            : "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, limit);
            } else {
                pstmt.setInt(1, limit);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(new AuditLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getTimestamp("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    // Удалить пользователя (только для админа)
    public boolean deleteUser(int userId) {
        if (currentUser != null && !currentUser.isAdmin()) {
            return false;
        }
        
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
            logAction("DELETE_USER", "Удален пользователь ID: " + userId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}