package com.bank.service;

import com.bank.database.DatabaseConnection;
import com.bank.model.Account;
import com.bank.model.AccountRequest;
import com.bank.model.Customer;
import com.bank.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BankService {
    
    // ==================== КЛИЕНТЫ ====================
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (full_name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Создание клиента с логином и паролем
    public boolean addCustomerWithLogin(Customer customer, String username, String password) {
        String sql = "INSERT INTO customers (full_name, phone, email, address, username, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, username);
            pstmt.setString(6, hashPassword(password));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Вход клиента
    public Customer loginCustomer(String username, String password) {
        String sql = "SELECT * FROM customers WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                );
                customer.setUsername(rs.getString("username"));
                return customer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(new Customer(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    // ==================== СЧЕТА ====================
    public boolean createAccount(Account account) {
        String sql = "INSERT INTO accounts (account_number, customer_id, balance, account_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getAccountNumber());
            pstmt.setInt(2, account.getCustomerId());
            pstmt.setDouble(3, account.getBalance());
            pstmt.setString(4, account.getAccountType());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Account> getAccountsByCustomer(int customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                accounts.add(new Account(
                    rs.getInt("id"),
                    rs.getString("account_number"),
                    rs.getInt("customer_id"),
                    rs.getDouble("balance"),
                    rs.getString("account_type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }
    
    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Account(
                    rs.getInt("id"),
                    rs.getString("account_number"),
                    rs.getInt("customer_id"),
                    rs.getDouble("balance"),
                    rs.getString("account_type")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // ==================== ТРАНЗАКЦИИ ====================
    public boolean deposit(int accountId, double amount, String description) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Обновить баланс
            String updateSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, accountId);
                pstmt.executeUpdate();
            }
            
            // Записать транзакцию
            String insertSql = "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (?, 'Deposit', ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, accountId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, description);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    
    public boolean withdraw(int accountId, double amount, String description) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Проверить баланс
            Account account = getAccountById(accountId);
            if (account == null || account.getBalance() < amount) {
                return false;
            }
            
            // Обновить баланс
            String updateSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, accountId);
                pstmt.executeUpdate();
            }
            
            // Записать транзакцию
            String insertSql = "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (?, 'Withdraw', ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, accountId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, description);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Перевод между счетами
    public boolean transfer(int fromAccountId, int toAccountId, double amount, String description) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Проверить баланс счета отправителя
            Account fromAccount = getAccountById(fromAccountId);
            if (fromAccount == null || fromAccount.getBalance() < amount) {
                return false;
            }
            
            // Проверить существование счета получателя
            Account toAccount = getAccountById(toAccountId);
            if (toAccount == null) {
                return false;
            }
            
            // Снять со счета отправителя
            String withdrawSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(withdrawSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, fromAccountId);
                pstmt.executeUpdate();
            }
            
            // Добавить на счет получателя
            String depositSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(depositSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, toAccountId);
                pstmt.executeUpdate();
            }
            
            // Записать транзакцию для отправителя
            String insertWithdrawSql = "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (?, 'Transfer Out', ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertWithdrawSql)) {
                pstmt.setInt(1, fromAccountId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, description + " (на счет " + toAccount.getAccountNumber() + ")");
                pstmt.executeUpdate();
            }
            
            // Записать транзакцию для получателя
            String insertDepositSql = "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (?, 'Transfer In', ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertDepositSql)) {
                pstmt.setInt(1, toAccountId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, description + " (со счета " + fromAccount.getAccountNumber() + ")");
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    
    // Получить транзакции по счету
    public List<Transaction> getTransactionsByAccount(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC LIMIT 50";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getInt("id"));
                transaction.setAccountId(rs.getInt("account_id"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setAmount(rs.getDouble("amount"));
                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    public String generateAccountNumber() {
        Random random = new Random();
        return String.format("%010d", random.nextInt(1000000000));
    }
    
    // Хэширование пароля
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    // ==================== ЗАЯВКИ НА ОТКРЫТИЕ СЧЕТА ====================

    // Создать заявку на открытие счета
    public boolean createAccountRequest(int customerId, String accountType) {
        String sql = "INSERT INTO account_requests (customer_id, account_type) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setString(2, accountType);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить заявки клиента
    public List<AccountRequest> getAccountRequestsByCustomer(int customerId) {
        List<AccountRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM account_requests WHERE customer_id = ? ORDER BY request_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AccountRequest request = new AccountRequest();
                request.setId(rs.getInt("id"));
                request.setCustomerId(rs.getInt("customer_id"));
                request.setAccountType(rs.getString("account_type"));
                request.setStatus(rs.getString("status"));
                request.setRequestDate(rs.getTimestamp("request_date"));
                Integer approvedBy = (Integer) rs.getObject("approved_by");
                request.setApprovedBy(approvedBy);
                request.setApprovedDate(rs.getTimestamp("approved_date"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    // Получить все заявки (для сотрудников)
    public List<AccountRequest> getAllAccountRequests() {
        List<AccountRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM account_requests ORDER BY request_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AccountRequest request = new AccountRequest();
                request.setId(rs.getInt("id"));
                request.setCustomerId(rs.getInt("customer_id"));
                request.setAccountType(rs.getString("account_type"));
                request.setStatus(rs.getString("status"));
                request.setRequestDate(rs.getTimestamp("request_date"));
                Integer approvedBy = (Integer) rs.getObject("approved_by");
                request.setApprovedBy(approvedBy);
                request.setApprovedDate(rs.getTimestamp("approved_date"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    // Получить заявки со статусом PENDING (для сотрудников)
    public List<AccountRequest> getPendingAccountRequests() {
        List<AccountRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM account_requests WHERE status = 'PENDING' ORDER BY request_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AccountRequest request = new AccountRequest();
                request.setId(rs.getInt("id"));
                request.setCustomerId(rs.getInt("customer_id"));
                request.setAccountType(rs.getString("account_type"));
                request.setStatus(rs.getString("status"));
                request.setRequestDate(rs.getTimestamp("request_date"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    // Одобрить заявку (создает счет)
    public boolean approveAccountRequest(int requestId, int approvedByUserId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Получить данные заявки
            String selectSql = "SELECT * FROM account_requests WHERE id = ? AND status = 'PENDING'";
            AccountRequest request = null;
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, requestId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    request = new AccountRequest();
                    request.setId(rs.getInt("id"));
                    request.setCustomerId(rs.getInt("customer_id"));
                    request.setAccountType(rs.getString("account_type"));
                }
            }
            
            if (request == null) {
                return false;
            }
            
            // Создать счет
            Account account = new Account();
            account.setAccountNumber(generateAccountNumber());
            account.setCustomerId(request.getCustomerId());
            account.setBalance(0.0);
            account.setAccountType(request.getAccountType());
            
            String insertAccountSql = "INSERT INTO accounts (account_number, customer_id, balance, account_type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertAccountSql)) {
                pstmt.setString(1, account.getAccountNumber());
                pstmt.setInt(2, account.getCustomerId());
                pstmt.setDouble(3, account.getBalance());
                pstmt.setString(4, account.getAccountType());
                pstmt.executeUpdate();
            }
            
            // Обновить статус заявки
            String updateSql = "UPDATE account_requests SET status = 'APPROVED', approved_by = ?, approved_date = NOW() WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, approvedByUserId);
                pstmt.setInt(2, requestId);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Отклонить заявку
    public boolean rejectAccountRequest(int requestId, int rejectedByUserId) {
        String sql = "UPDATE account_requests SET status = 'REJECTED', approved_by = ?, approved_date = NOW() WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rejectedByUserId);
            pstmt.setInt(2, requestId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}