package com.bank;

import com.bank.database.DatabaseConnection;
import com.bank.model.Account;
import com.bank.model.AccountRequest;
import com.bank.model.Customer;
import com.bank.model.User;
import com.bank.model.Transaction;
import com.bank.model.AuditLog;
import com.bank.service.AuthService;
import com.bank.service.BankService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {
    
    private BankService bankService;
    private AuthService authService;
    private BorderPane mainLayout;
    private Stage primaryStage;
    private Customer selectedCustomer;
    private Customer loggedInCustomer;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        bankService = new BankService();
        authService = new AuthService();
        DatabaseConnection.initializeDatabase();
        
        showLoginScreen();
    }
    
    // ==================== ЭКРАН ВЫБОРА ТИПА ВХОДА ====================
    private void showLoginScreen() {
        VBox loginBox = new VBox(20);
        loginBox.setPadding(new Insets(40));
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setStyle("-fx-background-color: #f5f5f5;");
        
        Label title = new Label("🏦 Банковская Система");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        Label subtitle = new Label("Выберите тип входа");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
        
        HBox buttonsBox = new HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);
        
        VBox employeeBox = createLoginTypeBox("👔", "Сотрудник", "Вход для сотрудников банка", 
            () -> showEmployeeLogin());
        VBox clientBox = createLoginTypeBox("👤", "Клиент", "Вход для клиентов банка", 
            () -> showClientLogin());
        
        buttonsBox.getChildren().addAll(employeeBox, clientBox);
        
        loginBox.getChildren().addAll(title, subtitle, buttonsBox);
        
        Scene scene = new Scene(loginBox, 900, 600);
        primaryStage.setTitle("Банковская Система - Вход");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createLoginTypeBox(String icon, String title, String description, Runnable action) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                     "-fx-cursor: hand;");
        box.setPrefSize(250, 200);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 64px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);
        
        box.setOnMouseClicked(e -> action.run());
        box.setOnMouseEntered(e -> box.setStyle(
            "-fx-background-color: white; -fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.4), 15, 0, 0, 3); " +
            "-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        box.setOnMouseExited(e -> box.setStyle(
            "-fx-background-color: white; -fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
            "-fx-cursor: hand;"));
        
        box.getChildren().addAll(iconLabel, titleLabel, descLabel);
        return box;
    }
    
    // ==================== ВХОД ДЛЯ СОТРУДНИКОВ ====================
    private void showEmployeeLogin() {
        VBox loginBox = new VBox(15);
        loginBox.setPadding(new Insets(40));
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setStyle("-fx-background-color: #f5f5f5;");
        
        Button btnBack = new Button("← Назад");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showLoginScreen());
        
        HBox topBox = new HBox();
        topBox.getChildren().add(btnBack);
        
        Label title = new Label("👔 Вход для Сотрудников");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        VBox formBox = new VBox(10);
        formBox.setMaxWidth(350);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        TextField tfUsername = new TextField();
        tfUsername.setPromptText("Логин");
        tfUsername.setPrefHeight(40);
        
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Пароль");
        pfPassword.setPrefHeight(40);
        
        Button btnLogin = new Button("Войти");
        btnLogin.setPrefHeight(40);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label lblHint = new Label("По умолчанию:\nЛогин: admin | Пароль: admin123\nЛогин: cashier | Пароль: cashier123");
        lblHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-text-alignment: center;");
        lblHint.setAlignment(Pos.CENTER);
        
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        btnLogin.setOnAction(e -> {
            if (tfUsername.getText().isEmpty() || pfPassword.getText().isEmpty()) {
                lblError.setText("Заполните все поля");
                return;
            }
            
            User user = authService.login(tfUsername.getText(), pfPassword.getText());
            if (user != null) {
                showMainApplication();
            } else {
                lblError.setText("Неверный логин или пароль");
            }
        });
        
        pfPassword.setOnAction(e -> btnLogin.fire());
        
        formBox.getChildren().addAll(
            new Label("Логин:"), tfUsername,
            new Label("Пароль:"), pfPassword,
            lblError, btnLogin, lblHint
        );
        
        loginBox.getChildren().addAll(topBox, title, formBox);
        
        Scene scene = new Scene(loginBox, 900, 600);
        primaryStage.setScene(scene);
    }
    
    // ==================== ВХОД ДЛЯ КЛИЕНТОВ ====================
    private void showClientLogin() {
        VBox loginBox = new VBox(15);
        loginBox.setPadding(new Insets(40));
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setStyle("-fx-background-color: #f5f5f5;");
        
        Button btnBack = new Button("← Назад");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showLoginScreen());
        
        HBox topBox = new HBox();
        topBox.getChildren().add(btnBack);
        
        Label title = new Label("👤 Вход для Клиентов");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        VBox formBox = new VBox(10);
        formBox.setMaxWidth(350);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        TextField tfUsername = new TextField();
        tfUsername.setPromptText("Логин");
        tfUsername.setPrefHeight(40);
        
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Пароль");
        pfPassword.setPrefHeight(40);
        
        Button btnLogin = new Button("Войти");
        btnLogin.setPrefHeight(40);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Hyperlink linkRegister = new Hyperlink("Нет аккаунта? Зарегистрироваться");
        linkRegister.setStyle("-fx-text-fill: #4CAF50;");
        linkRegister.setOnAction(e -> showClientRegistration());
        
        btnLogin.setOnAction(e -> {
            if (tfUsername.getText().isEmpty() || pfPassword.getText().isEmpty()) {
                lblError.setText("Заполните все поля");
                return;
            }
            
            Customer customer = bankService.loginCustomer(tfUsername.getText(), pfPassword.getText());
            if (customer != null) {
                loggedInCustomer = customer;
                showClientDashboard(customer);
            } else {
                lblError.setText("Неверный логин или пароль");
            }
        });
        
        pfPassword.setOnAction(e -> btnLogin.fire());
        
        formBox.getChildren().addAll(
            new Label("Логин:"), tfUsername,
            new Label("Пароль:"), pfPassword,
            lblError, btnLogin, linkRegister
        );
        
        loginBox.getChildren().addAll(topBox, title, formBox);
        
        Scene scene = new Scene(loginBox, 900, 600);
        primaryStage.setScene(scene);
    }
    
    // ==================== РЕГИСТРАЦИЯ КЛИЕНТА ====================
    private void showClientRegistration() {
        VBox regBox = new VBox(15);
        regBox.setPadding(new Insets(40));
        regBox.setAlignment(Pos.CENTER);
        regBox.setStyle("-fx-background-color: #f5f5f5;");
        
        Button btnBack = new Button("← Назад");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showClientLogin());
        
        HBox topBox = new HBox();
        topBox.getChildren().add(btnBack);
        
        Label title = new Label("📝 Регистрация Клиента");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        VBox formBox = new VBox(10);
        formBox.setMaxWidth(400);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        TextField tfFullName = new TextField();
        tfFullName.setPromptText("ФИО");
        tfFullName.setPrefHeight(40);
        
        TextField tfPhone = new TextField();
        tfPhone.setPromptText("Телефон");
        tfPhone.setPrefHeight(40);
        
        TextField tfEmail = new TextField();
        tfEmail.setPromptText("Email");
        tfEmail.setPrefHeight(40);
        
        TextField tfAddress = new TextField();
        tfAddress.setPromptText("Адрес");
        tfAddress.setPrefHeight(40);
        
        TextField tfUsername = new TextField();
        tfUsername.setPromptText("Логин");
        tfUsername.setPrefHeight(40);
        
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Пароль");
        pfPassword.setPrefHeight(40);
        
        PasswordField pfConfirmPassword = new PasswordField();
        pfConfirmPassword.setPromptText("Подтверждение пароля");
        pfConfirmPassword.setPrefHeight(40);
        
        Button btnRegister = new Button("Зарегистрироваться");
        btnRegister.setPrefHeight(40);
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(350);
        
        btnRegister.setOnAction(e -> {
            if (tfFullName.getText().isEmpty() || tfUsername.getText().isEmpty() || 
                pfPassword.getText().isEmpty() || pfConfirmPassword.getText().isEmpty()) {
                lblError.setText("Заполните обязательные поля (ФИО, Логин, Пароль)");
                return;
            }
            
            if (!pfPassword.getText().equals(pfConfirmPassword.getText())) {
                lblError.setText("Пароли не совпадают");
                return;
            }
            
            if (pfPassword.getText().length() < 6) {
                lblError.setText("Пароль должен содержать минимум 6 символов");
                return;
            }
            
            Customer customer = new Customer();
            customer.setFullName(tfFullName.getText());
            customer.setPhone(tfPhone.getText());
            customer.setEmail(tfEmail.getText());
            customer.setAddress(tfAddress.getText());
            
            if (bankService.addCustomerWithLogin(customer, tfUsername.getText(), pfPassword.getText())) {
                showAlert("Успех", "Регистрация прошла успешно!\nТеперь вы можете войти в систему.");
                showClientLogin();
            } else {
                lblError.setText("Ошибка регистрации. Возможно, логин уже занят.");
            }
        });
        
        formBox.getChildren().addAll(
            new Label("ФИО *:"), tfFullName,
            new Label("Телефон:"), tfPhone,
            new Label("Email:"), tfEmail,
            new Label("Адрес:"), tfAddress,
            new Separator(),
            new Label("Логин *:"), tfUsername,
            new Label("Пароль *:"), pfPassword,
            new Label("Подтверждение пароля *:"), pfConfirmPassword,
            lblError, btnRegister
        );
        
        ScrollPane scrollPane = new ScrollPane(formBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        regBox.getChildren().addAll(topBox, title, scrollPane);
        
        Scene scene = new Scene(regBox, 900, 600);
        primaryStage.setScene(scene);
    }
    
   // ==================== ЛИЧНЫЙ КАБИНЕТ КЛИЕНТА ====================
    private void showClientDashboard(Customer customer) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));
        
        // Верхнее меню клиента
        HBox topMenu = createClientTopMenu(customer);
        layout.setTop(topMenu);
        
        // Контент
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label welcome = new Label("Добро пожаловать, " + customer.getFullName() + "!");
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label subtitle = new Label("Ваши банковские счета");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnOpenAccount = new Button("➕ Открыть новый счет");
        btnOpenAccount.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnOpenAccount.setOnAction(e -> showRequestNewAccount(customer));
        
        headerBox.getChildren().addAll(subtitle, spacer, btnOpenAccount);
        
        // Счета клиента
        VBox accountsBox = new VBox(15);
        java.util.List<Account> accounts = bankService.getAccountsByCustomer(customer.getId());
        
        if (accounts.isEmpty()) {
            Label noAccounts = new Label("У вас пока нет банковских счетов.\nОткройте свой первый счет!");
            noAccounts.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-text-alignment: center;");
            noAccounts.setAlignment(Pos.CENTER);
            accountsBox.getChildren().add(noAccounts);
        } else {
            for (Account account : accounts) {
                VBox accountCard = createAccountCard(account);
                accountsBox.getChildren().add(accountCard);
            }
        }
        
        // Заявки на открытие счета
        java.util.List<AccountRequest> requests = bankService.getAccountRequestsByCustomer(customer.getId());
        if (!requests.isEmpty()) {
            Label requestsTitle = new Label("📋 Мои заявки на открытие счета");
            requestsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #666;");
            
            VBox requestsBox = new VBox(10);
            for (AccountRequest request : requests) {
                HBox requestCard = createRequestCard(request);
                requestsBox.getChildren().add(requestCard);
            }
            
            accountsBox.getChildren().addAll(new Separator(), requestsTitle, requestsBox);
        }
        
        ScrollPane scrollPane = new ScrollPane(accountsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        content.getChildren().addAll(welcome, headerBox, scrollPane);
        layout.setCenter(content);
        
        Scene scene = new Scene(layout, 1000, 700);
        primaryStage.setTitle("Банковская Система - Личный Кабинет");
        primaryStage.setScene(scene);
    }

    private HBox createRequestCard(AccountRequest request) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        
        String bgColor = switch(request.getStatus()) {
            case "PENDING" -> "#FFF3CD";
            case "APPROVED" -> "#D4EDDA";
            case "REJECTED" -> "#F8D7DA";
            default -> "#E9ECEF";
        };
        
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8;");
        
        String icon = switch(request.getStatus()) {
            case "PENDING" -> "⏳";
            case "APPROVED" -> "✅";
            case "REJECTED" -> "❌";
            default -> "❓";
        };
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        VBox infoBox = new VBox(5);
        Label typeLabel = new Label("Тип счета: " + request.getAccountType());
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        String statusText = switch(request.getStatus()) {
            case "PENDING" -> "Ожидает рассмотрения";
            case "APPROVED" -> "Одобрено";
            case "REJECTED" -> "Отклонено";
            default -> request.getStatus();
        };
        
        Label statusLabel = new Label("Статус: " + statusText);
        statusLabel.setStyle("-fx-font-size: 12px;");
        
        Label dateLabel = new Label("Дата заявки: " + request.getRequestDate());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        infoBox.getChildren().addAll(typeLabel, statusLabel, dateLabel);
        
        card.getChildren().addAll(iconLabel, infoBox);
        return card;
    }
    
    private HBox createClientTopMenu(Customer customer) {
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(10));
        menu.setAlignment(Pos.CENTER_LEFT);
        menu.setStyle("-fx-background-color: #4CAF50;");
        
        Label lblBank = new Label("🏦 Банковская Система");
        lblBank.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label lblUser = new Label("👤 " + customer.getFullName());
        lblUser.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8;");
        
        Button btnLogout = new Button("🚪 Выход");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> {
            loggedInCustomer = null;
            showLoginScreen();
        });
        
        menu.getChildren().addAll(lblBank, spacer, lblUser, btnLogout);
        return menu;
    }
    
    private VBox createAccountCard(Account account) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label typeIcon = new Label("💳");
        typeIcon.setStyle("-fx-font-size: 32px;");
        
        VBox infoBox = new VBox(5);
        Label accountNumber = new Label("Счет: " + account.getAccountNumber());
        accountNumber.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label accountType = new Label("Тип: " + account.getAccountType());
        accountType.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        infoBox.getChildren().addAll(accountNumber, accountType);
        headerBox.getChildren().addAll(typeIcon, infoBox);
        
        Label balance = new Label(String.format("$%.2f", account.getBalance()));
        balance.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        HBox buttonsBox = new HBox(10);
        
        Button btnViewTransactions = new Button("📋 История");
        btnViewTransactions.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnViewTransactions.setOnAction(e -> showClientTransactions(account));
        
        Button btnTransfer = new Button("💸 Перевод");
        btnTransfer.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        btnTransfer.setOnAction(e -> showClientTransfer(account));
        
        buttonsBox.getChildren().addAll(btnViewTransactions, btnTransfer);
        
        card.getChildren().addAll(headerBox, balance, buttonsBox);
        return card;
    }
    // ==================== ИСТОРИЯ ТРАНЗАКЦИЙ КЛИЕНТА ====================
    private void showClientTransactions(Account account) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));
        
        HBox topMenu = createClientTopMenu(loggedInCustomer);
        layout.setTop(topMenu);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Button btnBack = new Button("← Назад к счетам");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showClientDashboard(loggedInCustomer));
        
        Label title = new Label("История транзакций");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label accountInfo = new Label("Счет: " + account.getAccountNumber());
        accountInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        Label balanceLabel = new Label(String.format("Баланс: $%.2f", account.getBalance()));
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        ListView<String> transactionList = new ListView<>();
        transactionList.setPrefHeight(400);
        
        java.util.List<Transaction> transactions = bankService.getTransactionsByAccount(account.getId());
        
        if (transactions.isEmpty()) {
            transactionList.getItems().add("Нет транзакций");
        } else {
            for (Transaction t : transactions) {
                String type = t.getTransactionType().equals("Deposit") ? "➕ Пополнение" : "➖ Снятие";
                String desc = t.getDescription() != null && !t.getDescription().isEmpty() 
                    ? " - " + t.getDescription() : "";
                transactionList.getItems().add(
                    t.getTransactionDate() + "\n" +
                    type + ": $" + String.format("%.2f", t.getAmount()) + desc + "\n"
                );
            }
        }
        
        content.getChildren().addAll(btnBack, title, accountInfo, balanceLabel, 
                                     new Label("Последние 50 транзакций:"), transactionList);
        layout.setCenter(content);
        
        Scene scene = new Scene(layout, 1000, 700);
        primaryStage.setScene(scene);
    }

    // ==================== ПЕРЕВОД МЕЖДУ СЧЕТАМИ ====================
    private void showClientTransfer(Account fromAccount) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));
        
        HBox topMenu = createClientTopMenu(loggedInCustomer);
        layout.setTop(topMenu);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setMaxWidth(600);
        content.setAlignment(Pos.TOP_CENTER);
        
        Button btnBack = new Button("← Назад к счетам");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showClientDashboard(loggedInCustomer));
        
        Label title = new Label("💸 Перевод между счетами");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Информация о счете отправителя
        VBox fromBox = new VBox(10);
        fromBox.setPadding(new Insets(20));
        fromBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        
        Label fromLabel = new Label("Со счета:");
        fromLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        Label fromAccountInfo = new Label(fromAccount.getAccountNumber());
        fromAccountInfo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label fromBalance = new Label(String.format("Доступно: $%.2f", fromAccount.getBalance()));
        fromBalance.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50;");
        
        fromBox.getChildren().addAll(fromLabel, fromAccountInfo, fromBalance);
        
        // Выбор счета получателя
        Label toLabel = new Label("На счет:");
        toLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        ComboBox<Account> cbToAccount = new ComboBox<>();
        cbToAccount.setPrefWidth(400);
        cbToAccount.setPromptText("Выберите счет получателя");
        
        // Загрузить все счета клиента кроме текущего
        java.util.List<Account> accounts = bankService.getAccountsByCustomer(loggedInCustomer.getId());
        for (Account acc : accounts) {
            if (acc.getId() != fromAccount.getId()) {
                cbToAccount.getItems().add(acc);
            }
        }
        
        // Сумма перевода
        Label amountLabel = new Label("Сумма:");
        amountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        TextField tfAmount = new TextField();
        tfAmount.setPromptText("Введите сумму");
        tfAmount.setPrefHeight(40);
        tfAmount.setStyle("-fx-font-size: 16px;");
        
        // Описание
        Label descLabel = new Label("Описание (необязательно):");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        TextField tfDescription = new TextField();
        tfDescription.setPromptText("Например: оплата за квартиру");
        tfDescription.setPrefHeight(40);
        
        // Кнопка перевода
        Button btnTransfer = new Button("💸 Выполнить перевод");
        btnTransfer.setPrefHeight(50);
        btnTransfer.setPrefWidth(300);
        btnTransfer.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(400);
        
        btnTransfer.setOnAction(e -> {
            lblError.setText("");
            
            if (cbToAccount.getValue() == null) {
                lblError.setText("Выберите счет получателя");
                return;
            }
            
            if (tfAmount.getText().isEmpty()) {
                lblError.setText("Введите сумму перевода");
                return;
            }
            
            try {
                double amount = Double.parseDouble(tfAmount.getText());
                
                if (amount <= 0) {
                    lblError.setText("Сумма должна быть больше нуля");
                    return;
                }
                
                if (amount > fromAccount.getBalance()) {
                    lblError.setText("Недостаточно средств на счете");
                    return;
                }
                
                // Подтверждение
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Подтверждение перевода");
                confirm.setHeaderText("Выполнить перевод?");
                confirm.setContentText(
                    String.format("Сумма: $%.2f\n", amount) +
                    "Со счета: " + fromAccount.getAccountNumber() + "\n" +
                    "На счет: " + cbToAccount.getValue().getAccountNumber()
                );
                
                if (confirm.showAndWait().get() == ButtonType.OK) {
                    String description = tfDescription.getText().isEmpty() ? "Перевод" : tfDescription.getText();
                    
                    if (bankService.transfer(fromAccount.getId(), cbToAccount.getValue().getId(), amount, description)) {
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Успех");
                        success.setHeaderText("Перевод выполнен!");
                        success.setContentText(String.format("Переведено $%.2f со счета %s на счет %s", 
                            amount, fromAccount.getAccountNumber(), cbToAccount.getValue().getAccountNumber()));
                        success.showAndWait();
                        
                        // Вернуться к списку счетов
                        showClientDashboard(loggedInCustomer);
                    } else {
                        lblError.setText("Ошибка при выполнении перевода. Попробуйте снова.");
                    }
                }
            } catch (NumberFormatException ex) {
                lblError.setText("Неверный формат суммы. Используйте цифры.");
            }
        });
        
        // Проверка: есть ли другие счета для перевода
        if (cbToAccount.getItems().isEmpty()) {
            Label noAccounts = new Label("У вас нет других счетов для перевода.\nОткройте дополнительный счет в отделении банка.");
            noAccounts.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-text-alignment: center;");
            noAccounts.setAlignment(Pos.CENTER);
            noAccounts.setWrapText(true);
            
            content.getChildren().addAll(btnBack, title, fromBox, noAccounts);
        } else {
            content.getChildren().addAll(
                btnBack, title, fromBox,
                toLabel, cbToAccount,
                amountLabel, tfAmount,
                descLabel, tfDescription,
                lblError, btnTransfer
            );
        }
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        layout.setCenter(scrollPane);
        
        Scene scene = new Scene(layout, 1000, 700);
        primaryStage.setScene(scene);
    }

        // ==================== ЗАЯВКА НА ОТКРЫТИЕ НОВОГО СЧЕТА ====================
    private void showRequestNewAccount(Customer customer) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));
        
        HBox topMenu = createClientTopMenu(customer);
        layout.setTop(topMenu);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setMaxWidth(500);
        content.setAlignment(Pos.TOP_CENTER);
        
        Button btnBack = new Button("← Назад");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showClientDashboard(customer));
        
        Label title = new Label("➕ Открыть новый счет");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("Выберите тип счета для открытия");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        // Типы счетов
        VBox accountTypes = new VBox(15);
        
        VBox savingsBox = createAccountTypeBox(
            "💰", "Сберегательный счет (Savings)", 
            "Для накоплений и хранения средств",
            "Savings"
        );
        
        VBox checkingBox = createAccountTypeBox(
            "💳", "Текущий счет (Checking)", 
            "Для повседневных операций",
            "Checking"
        );
        
        VBox businessBox = createAccountTypeBox(
            "🏢", "Бизнес счет (Business)", 
            "Для коммерческой деятельности",
            "Business"
        );
        
        accountTypes.getChildren().addAll(savingsBox, checkingBox, businessBox);
        
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(450);
        
        content.getChildren().addAll(btnBack, title, subtitle, accountTypes, lblError);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        layout.setCenter(scrollPane);
        
        Scene scene = new Scene(layout, 1000, 700);
        primaryStage.setScene(scene);
    }

    private VBox createAccountTypeBox(String icon, String title, String description, String accountType) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                    "-fx-cursor: hand;");
        
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 36px;");
        
        VBox textBox = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        descLabel.setWrapText(true);
        
        textBox.getChildren().addAll(titleLabel, descLabel);
        headerBox.getChildren().addAll(iconLabel, textBox);
        
        box.getChildren().add(headerBox);
        
        box.setOnMouseEntered(e -> box.setStyle(
            "-fx-background-color: white; -fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(76, 175, 80, 0.4), 15, 0, 0, 3); " +
            "-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        
        box.setOnMouseExited(e -> box.setStyle(
            "-fx-background-color: white; -fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
            "-fx-cursor: hand;"));
        
        box.setOnMouseClicked(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение");
            confirm.setHeaderText("Открыть " + title + "?");
            confirm.setContentText("Ваша заявка будет отправлена на рассмотрение сотруднику банка.");
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                if (bankService.createAccountRequest(loggedInCustomer.getId(), accountType)) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Успех");
                    success.setHeaderText("Заявка отправлена!");
                    success.setContentText("Ваша заявка на открытие счета отправлена на рассмотрение.\nОжидайте одобрения сотрудником банка.");
                    success.showAndWait();
                    
                    showClientDashboard(loggedInCustomer);
                } else {
                    showAlert("Ошибка", "Не удалось отправить заявку. Попробуйте позже.");
                }
            }
        });
        
        return box;
    }
    
    // ==================== ГЛАВНОЕ ПРИЛОЖЕНИЕ ДЛЯ СОТРУДНИКОВ ====================
    private void showMainApplication() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));
        
        HBox topMenu = createTopMenu();
        mainLayout.setTop(topMenu);
        
        showMainScreen();
        
        Scene scene = new Scene(mainLayout, 1100, 700);
        primaryStage.setTitle("Банковская Система - " + authService.getCurrentUser().getFullName());
        primaryStage.setScene(scene);
    }
    
    private HBox createTopMenu() {
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(10));
        menu.setAlignment(Pos.CENTER_LEFT);
        menu.setStyle("-fx-background-color: #2196F3;");
        
        Button btnMain = new Button("🏠 Главная");
        Button btnCustomers = new Button("👥 Клиенты");
        Button btnAccounts = new Button("💳 Счета");
        Button btnTransactions = new Button("💸 Транзакции");
        Button btnRequests = new Button("📋 Заявки");  // НОВАЯ КНОПКА
        Button btnAudit = new Button("📜 История");
        Button btnUsers = new Button("👤 Пользователи");
        
        String buttonStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 15 8 15; -fx-cursor: hand;";
        btnMain.setStyle(buttonStyle);
        btnCustomers.setStyle(buttonStyle);
        btnAccounts.setStyle(buttonStyle);
        btnTransactions.setStyle(buttonStyle);
        btnRequests.setStyle(buttonStyle);  // НОВАЯ КНОПКА
        btnAudit.setStyle(buttonStyle);
        btnUsers.setStyle(buttonStyle);
        
        btnMain.setOnAction(e -> showMainScreen());
        btnCustomers.setOnAction(e -> showCustomersScreen());
        btnAccounts.setOnAction(e -> showAccountsScreen());
        btnTransactions.setOnAction(e -> showTransactionsScreen());
        btnRequests.setOnAction(e -> showAccountRequestsScreen());  // НОВАЯ КНОПКА
        btnAudit.setOnAction(e -> showAuditScreen());
        btnUsers.setOnAction(e -> showUsersScreen());
        
        HBox leftButtons = new HBox(5);
        leftButtons.getChildren().addAll(btnMain, btnCustomers, btnAccounts, btnTransactions, btnRequests, btnAudit);  // ДОБАВИЛИ btnRequests
        if (authService.getCurrentUser().isAdmin()) {
            leftButtons.getChildren().add(btnUsers);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label lblUser = new Label("👤 " + authService.getCurrentUser().getFullName() + 
                                " (" + authService.getCurrentUser().getRole() + ")");
        lblUser.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8;");
        
        Button btnLogout = new Button("🚪 Выход");
        btnLogout.setStyle(buttonStyle);
        btnLogout.setOnAction(e -> {
            authService.logout();
            showLoginScreen();
        });
        
        menu.getChildren().addAll(leftButtons, spacer, lblUser, btnLogout);
        return menu;
    }
    
    // ==================== ГЛАВНЫЙ ЭКРАН ====================
    private void showMainScreen() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        
        Label title = new Label("Добро пожаловать, " + authService.getCurrentUser().getFullName() + "!");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        Label info = new Label("Используйте меню выше для навигации по системе");
        info.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setAlignment(Pos.CENTER);

        VBox stat1 = createStatBox("👥", "Клиентов", String.valueOf(bankService.getAllCustomers().size()));
        VBox stat2 = createStatBox("💳", "Счетов", String.valueOf(getTotalAccounts()));
        VBox stat3 = createStatBox("💰", "Всего средств", String.format("$%.2f", getTotalBalance()));
        VBox stat4 = createStatBox("⏳", "Заявок ожидают", String.valueOf(bankService.getPendingAccountRequests().size()));  // НОВАЯ СТАТИСТИКА

        statsGrid.add(stat1, 0, 0);
        statsGrid.add(stat2, 1, 0);
        statsGrid.add(stat3, 0, 1);
        statsGrid.add(stat4, 1, 1);  // НОВАЯ СТАТИСТИКА
        
        content.getChildren().addAll(title, info, statsGrid);
        mainLayout.setCenter(content);
        
        authService.logAction("VIEW_DASHBOARD", "Просмотр главного экрана");
    }
    
    private VBox createStatBox(String icon, String label, String value) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        box.setPrefWidth(200);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2196F3;"); 
         Label descLabel = new Label(label);
    descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
    
    box.getChildren().addAll(iconLabel, valueLabel, descLabel);
    return box;
}

private int getTotalAccounts() {
    int total = 0;
    for (Customer customer : bankService.getAllCustomers()) {
        total += bankService.getAccountsByCustomer(customer.getId()).size();
    }
    return total;
}

private double getTotalBalance() {
    double total = 0;
    for (Customer customer : bankService.getAllCustomers()) {
        for (Account account : bankService.getAccountsByCustomer(customer.getId())) {
            total += account.getBalance();
        }
    }
    return total;
}

// ==================== ЭКРАН КЛИЕНТОВ ====================
private void showCustomersScreen() {
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    
    Label title = new Label("👥 Управление Клиентами");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    
    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    
    TextField tfName = new TextField();
    TextField tfPhone = new TextField();
    TextField tfEmail = new TextField();
    TextArea taAddress = new TextArea();
    taAddress.setPrefRowCount(3);
    
    form.add(new Label("ФИО:"), 0, 0);
    form.add(tfName, 1, 0);
    form.add(new Label("Телефон:"), 0, 1);
    form.add(tfPhone, 1, 1);
    form.add(new Label("Email:"), 0, 2);
    form.add(tfEmail, 1, 2);
    form.add(new Label("Адрес:"), 0, 3);
    form.add(taAddress, 1, 3);
    
    Button btnAdd = new Button("➕ Добавить Клиента");
    btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    
    ListView<Customer> customerList = new ListView<>();
    customerList.setPrefHeight(300);
    refreshCustomerList(customerList);
    
    btnAdd.setOnAction(e -> {
        if (tfName.getText().isEmpty()) {
            showAlert("Ошибка", "Пожалуйста, заполните ФИО");
            return;
        }
        
        Customer customer = new Customer();
        customer.setFullName(tfName.getText());
        customer.setPhone(tfPhone.getText());
        customer.setEmail(tfEmail.getText());
        customer.setAddress(taAddress.getText());
        
        if (bankService.addCustomer(customer)) {
            showAlert("Успех", "Клиент добавлен успешно!");
            authService.logAction("ADD_CUSTOMER", "Добавлен клиент: " + customer.getFullName());
            tfName.clear();
            tfPhone.clear();
            tfEmail.clear();
            taAddress.clear();
            refreshCustomerList(customerList);
        } else {
            showAlert("Ошибка", "Не удалось добавить клиента");
        }
    });
    
    content.getChildren().addAll(title, form, btnAdd, new Label("Список клиентов:"), customerList);
    mainLayout.setCenter(content);
    
    authService.logAction("VIEW_CUSTOMERS", "Просмотр списка клиентов");
}

// ==================== ЭКРАН СЧЕТОВ ====================
private void showAccountsScreen() {
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    
    Label title = new Label("💳 Управление Счетами");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    
    ComboBox<Customer> cbCustomer = new ComboBox<>();
    cbCustomer.getItems().addAll(bankService.getAllCustomers());
    cbCustomer.setPromptText("Выберите клиента");
    cbCustomer.setPrefWidth(300);
    
    ComboBox<String> cbAccountType = new ComboBox<>();
    cbAccountType.getItems().addAll("Savings", "Checking", "Business");
    cbAccountType.setValue("Savings");
    
    TextField tfInitialBalance = new TextField("0");
    
    Button btnCreate = new Button("➕ Создать Счет");
    btnCreate.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    
    ListView<Account> accountList = new ListView<>();
    accountList.setPrefHeight(300);
    
    cbCustomer.setOnAction(e -> {
        Customer selected = cbCustomer.getValue();
        if (selected != null) {
            selectedCustomer = selected;
            accountList.getItems().clear();
            accountList.getItems().addAll(bankService.getAccountsByCustomer(selected.getId()));
        }
    });
    
    btnCreate.setOnAction(e -> {
        if (cbCustomer.getValue() == null) {
            showAlert("Ошибка", "Пожалуйста, выберите клиента");
            return;
        }
        
        try {
            double initialBalance = Double.parseDouble(tfInitialBalance.getText());
            if (initialBalance < 0) {
                showAlert("Ошибка", "Баланс не может быть отрицательным");
                return;
            }
            
            Account account = new Account();
            account.setAccountNumber(bankService.generateAccountNumber());
            account.setCustomerId(cbCustomer.getValue().getId());
            account.setBalance(initialBalance);
            account.setAccountType(cbAccountType.getValue());
            
            if (bankService.createAccount(account)) {
                showAlert("Успех", "Счет создан успешно!\nНомер счета: " + account.getAccountNumber());
                authService.logAction("CREATE_ACCOUNT", "Создан счет: " + account.getAccountNumber() + 
                                     " для клиента ID: " + account.getCustomerId());
                tfInitialBalance.setText("0");
                accountList.getItems().clear();
                accountList.getItems().addAll(bankService.getAccountsByCustomer(cbCustomer.getValue().getId()));
            } else {
                showAlert("Ошибка", "Не удалось создать счет");
            }
        } catch (NumberFormatException ex) {
            showAlert("Ошибка", "Неверный формат суммы");
        }
    });
    
    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.add(new Label("Клиент:"), 0, 0);
    form.add(cbCustomer, 1, 0);
    form.add(new Label("Тип счета:"), 0, 1);
    form.add(cbAccountType, 1, 1);
    form.add(new Label("Начальный баланс:"), 0, 2);
    form.add(tfInitialBalance, 1, 2);
    
    content.getChildren().addAll(title, form, btnCreate, new Label("Счета клиента:"), accountList);
    mainLayout.setCenter(content);
    
    authService.logAction("VIEW_ACCOUNTS", "Просмотр счетов");
}

// ==================== ЭКРАН ТРАНЗАКЦИЙ ====================
private void showTransactionsScreen() {
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    
    Label title = new Label("💸 Транзакции");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    
    ComboBox<Customer> cbCustomer = new ComboBox<>();
    cbCustomer.getItems().addAll(bankService.getAllCustomers());
    cbCustomer.setPromptText("Выберите клиента");
    cbCustomer.setPrefWidth(300);
    
    ComboBox<Account> cbAccount = new ComboBox<>();
    cbAccount.setPromptText("Выберите счет");
    cbAccount.setPrefWidth(300);
    
    cbCustomer.setOnAction(e -> {
        Customer selected = cbCustomer.getValue();
        if (selected != null) {
            cbAccount.getItems().clear();
            cbAccount.getItems().addAll(bankService.getAccountsByCustomer(selected.getId()));
        }
    });
    
    TextField tfAmount = new TextField();
    tfAmount.setPromptText("Сумма");
    
    TextField tfDescription = new TextField();
    tfDescription.setPromptText("Описание");
    
    Button btnDeposit = new Button("⬇ Внести");
    btnDeposit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    
    Button btnWithdraw = new Button("⬆ Снять");
    btnWithdraw.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
    
    Label lblBalance = new Label("Текущий баланс: -");
    lblBalance.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    
    cbAccount.setOnAction(e -> {
        Account selected = cbAccount.getValue();
        if (selected != null) {
            lblBalance.setText(String.format("Текущий баланс: $%.2f", selected.getBalance()));
        }
    });
    
    btnDeposit.setOnAction(e -> {
        if (cbAccount.getValue() == null) {
            showAlert("Ошибка", "Выберите счет");
            return;
        }
        
        try {
            double amount = Double.parseDouble(tfAmount.getText());
            if (amount <= 0) {
                showAlert("Ошибка", "Сумма должна быть больше нуля");
                return;
            }
            
            if (bankService.deposit(cbAccount.getValue().getId(), amount, tfDescription.getText())) {
                showAlert("Успех", "Средства внесены успешно!");
                authService.logAction("DEPOSIT", "Внесено $" + amount + " на счет " + cbAccount.getValue().getAccountNumber());
                tfAmount.clear();
                tfDescription.clear();
                
                Account updated = bankService.getAccountById(cbAccount.getValue().getId());
                if (updated != null) {
                    lblBalance.setText(String.format("Текущий баланс: $%.2f", updated.getBalance()));
                    cbAccount.setValue(updated);
                }
            } else {
                showAlert("Ошибка", "Не удалось выполнить операцию");
            }
        } catch (NumberFormatException ex) {
            showAlert("Ошибка", "Неверный формат суммы");
        }
    });
    
    btnWithdraw.setOnAction(e -> {
        if (cbAccount.getValue() == null) {
            showAlert("Ошибка", "Выберите счет");
            return;
        }
        
        try {
            double amount = Double.parseDouble(tfAmount.getText());
            if (amount <= 0) {
                showAlert("Ошибка", "Сумма должна быть больше нуля");
                return;
            }
            
            if (bankService.withdraw(cbAccount.getValue().getId(), amount, tfDescription.getText())) {
                showAlert("Успех", "Средства сняты успешно!");
                authService.logAction("WITHDRAW", "Снято $" + amount + " со счета " + cbAccount.getValue().getAccountNumber());
                tfAmount.clear();
                tfDescription.clear();
                
                Account updated = bankService.getAccountById(cbAccount.getValue().getId());
                if (updated != null) {
                    lblBalance.setText(String.format("Текущий баланс: $%.2f", updated.getBalance()));
                    cbAccount.setValue(updated);
                }
            } else {
                showAlert("Ошибка", "Недостаточно средств или ошибка операции");
            }
        } catch (NumberFormatException ex) {
            showAlert("Ошибка", "Неверный формат суммы");
        }
    });
    
    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.add(new Label("Клиент:"), 0, 0);
    form.add(cbCustomer, 1, 0);
    form.add(new Label("Счет:"), 0, 1);
    form.add(cbAccount, 1, 1);
    form.add(new Label("Сумма:"), 0, 2);
    form.add(tfAmount, 1, 2);
    form.add(new Label("Описание:"), 0, 3);
    form.add(tfDescription, 1, 3);
    
    HBox buttons = new HBox(10);
    buttons.getChildren().addAll(btnDeposit, btnWithdraw);
    
    content.getChildren().addAll(title, form, buttons, lblBalance);
    mainLayout.setCenter(content);
    
    authService.logAction("VIEW_TRANSACTIONS", "Просмотр транзакций");
}

// ==================== ЭКРАН ИСТОРИИ ДЕЙСТВИЙ ====================
private void showAuditScreen() {
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    
    Label title = new Label("📋 История Действий Пользователей");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    
    HBox filterBox = new HBox(10);
    filterBox.setAlignment(Pos.CENTER_LEFT);
    
    ComboBox<String> cbFilter = new ComboBox<>();
    cbFilter.getItems().addAll("Все действия", "Только мои действия");
    cbFilter.setValue("Все действия");
    
    ComboBox<Integer> cbLimit = new ComboBox<>();
    cbLimit.getItems().addAll(50, 100, 200, 500);
    cbLimit.setValue(100);
    
    Button btnRefresh = new Button("🔄 Обновить");
    
    filterBox.getChildren().addAll(new Label("Фильтр:"), cbFilter, new Label("Показать:"), cbLimit, btnRefresh);
    
    ListView<String> auditList = new ListView<>();
    auditList.setPrefHeight(400);
    
    Runnable refreshAudit = () -> {
        auditList.getItems().clear();
        Integer userId = cbFilter.getValue().equals("Только мои действия") 
            ? authService.getCurrentUser().getId() 
            : null;
        
        for (AuditLog log : authService.getAuditLogs(userId, cbLimit.getValue())) {
            String userInfo = userId == null ? " (ID пользователя: " + log.getUserId() + ")" : "";
            auditList.getItems().add(
                log.getTimestamp() + userInfo + "\n" +
                "  📌 " + log.getAction() + ": " + log.getDetails() + "\n"
            );
        }
    };
    
    btnRefresh.setOnAction(e -> refreshAudit.run());
    cbFilter.setOnAction(e -> refreshAudit.run());
    cbLimit.setOnAction(e -> refreshAudit.run());
    
    refreshAudit.run();
    
    content.getChildren().addAll(title, filterBox, auditList);
    mainLayout.setCenter(content);
    
    authService.logAction("VIEW_AUDIT", "Просмотр истории действий");
}

// ==================== ЭКРАН УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ ====================
private void showUsersScreen() {
    if (!authService.getCurrentUser().isAdmin()) {
        showAlert("Доступ запрещен", "Только администратор может управлять пользователями");
        return;
    }
    
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    
    Label title = new Label("👤 Управление Пользователями");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    
    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    
    TextField tfUsername = new TextField();
    PasswordField pfPassword = new PasswordField();
    TextField tfFullName = new TextField();
    ComboBox<String> cbRole = new ComboBox<>();
    cbRole.getItems().addAll("ADMIN", "CASHIER");
    cbRole.setValue("CASHIER");
    
    form.add(new Label("Логин:"), 0, 0);
    form.add(tfUsername, 1, 0);
    form.add(new Label("Пароль:"), 0, 1);
    form.add(pfPassword, 1, 1);
    form.add(new Label("ФИО:"), 0, 2);
    form.add(tfFullName, 1, 2);
    form.add(new Label("Роль:"), 0, 3);
    form.add(cbRole, 1, 3);
    
    Button btnCreateUser = new Button("➕ Создать Пользователя");
    btnCreateUser.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    
    ListView<User> userList = new ListView<>();
    userList.setPrefHeight(300);
    
    Runnable refreshUsers = () -> {
        userList.getItems().clear();
        userList.getItems().addAll(authService.getAllUsers());
    };
    
    refreshUsers.run();
    
    btnCreateUser.setOnAction(e -> {
        if (tfUsername.getText().isEmpty() || pfPassword.getText().isEmpty() || tfFullName.getText().isEmpty()) {
            showAlert("Ошибка", "Заполните все поля");
            return;
        }
        
        User newUser = new User();
        newUser.setUsername(tfUsername.getText());
        newUser.setPassword(pfPassword.getText());
        newUser.setFullName(tfFullName.getText());
        newUser.setRole(cbRole.getValue());
        
        if (authService.createUser(newUser)) {
            showAlert("Успех", "Пользователь создан успешно!");
            tfUsername.clear();
            pfPassword.clear();
            tfFullName.clear();
            refreshUsers.run();
        } else {
            showAlert("Ошибка", "Не удалось создать пользователя. Возможно, логин уже существует.");
        }
    });
    
    Button btnDelete = new Button("🗑 Удалить выбранного");
    btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
    btnDelete.setOnAction(e -> {
        User selected = userList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите пользователя");
            return;
        }
        
        if (selected.getId() == authService.getCurrentUser().getId()) {
            showAlert("Ошибка", "Нельзя удалить себя");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить пользователя?");
        confirm.setContentText("Вы уверены, что хотите удалить пользователя " + selected.getFullName() + "?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (authService.deleteUser(selected.getId())) {
                showAlert("Успех", "Пользователь удален");
                refreshUsers.run();
            } else {
                showAlert("Ошибка", "Не удалось удалить пользователя");
            }
        }
    });
    
    content.getChildren().addAll(title, form, btnCreateUser, 
                                 new Label("Список пользователей:"), userList, btnDelete);
    mainLayout.setCenter(content);
    
    authService.logAction("VIEW_USERS", "Просмотр списка пользователей");
}

// ==================== ЭКРАН ЗАЯВОК НА ОТКРЫТИЕ СЧЕТА (ДЛЯ СОТРУДНИКОВ) ====================
private void showAccountRequestsScreen() {
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    
    Label title = new Label("📋 Заявки на открытие счета");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    
    // Фильтр
    HBox filterBox = new HBox(10);
    filterBox.setAlignment(Pos.CENTER_LEFT);
    
    ComboBox<String> cbFilter = new ComboBox<>();
    cbFilter.getItems().addAll("Только ожидающие", "Все заявки");
    cbFilter.setValue("Только ожидающие");
    
    Button btnRefresh = new Button("🔄 Обновить");
    
    filterBox.getChildren().addAll(new Label("Фильтр:"), cbFilter, btnRefresh);
    
    // Список заявок
    VBox requestsBox = new VBox(10);
    ScrollPane scrollPane = new ScrollPane(requestsBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(500);
    scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    
    Runnable refreshRequests = () -> {
        requestsBox.getChildren().clear();
        
        java.util.List<AccountRequest> requests;
        if (cbFilter.getValue().equals("Только ожидающие")) {
            requests = bankService.getPendingAccountRequests();
        } else {
            requests = bankService.getAllAccountRequests();
        }
        
        if (requests.isEmpty()) {
            Label noRequests = new Label("Нет заявок");
            noRequests.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-padding: 20;");
            requestsBox.getChildren().add(noRequests);
        } else {
            for (AccountRequest request : requests) {
                VBox requestCard = createEmployeeRequestCard(request);
                requestsBox.getChildren().add(requestCard);
            }
        }
    };
    
    btnRefresh.setOnAction(e -> refreshRequests.run());
    cbFilter.setOnAction(e -> refreshRequests.run());
    
    refreshRequests.run();
    
    content.getChildren().addAll(title, filterBox, scrollPane);
    mainLayout.setCenter(content);
    
    authService.logAction("VIEW_REQUESTS", "Просмотр заявок на открытие счета");
}

private VBox createEmployeeRequestCard(AccountRequest request) {
    VBox card = new VBox(15);
    card.setPadding(new Insets(20));
    
    String bgColor = switch(request.getStatus()) {
        case "PENDING" -> "white";
        case "APPROVED" -> "#D4EDDA";
        case "REJECTED" -> "#F8D7DA";
        default -> "white";
    };
    
    card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; " +
                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
    
    // Заголовок
    HBox headerBox = new HBox(15);
    headerBox.setAlignment(Pos.CENTER_LEFT);
    
    String icon = switch(request.getStatus()) {
        case "PENDING" -> "⏳";
        case "APPROVED" -> "✅";
        case "REJECTED" -> "❌";
        default -> "❓";
    };
    
    Label iconLabel = new Label(icon);
    iconLabel.setStyle("-fx-font-size: 32px;");
    
    VBox infoBox = new VBox(5);
    
    // Получить информацию о клиенте
    Customer customer = null;
    for (Customer c : bankService.getAllCustomers()) {
        if (c.getId() == request.getCustomerId()) {
            customer = c;
            break;
        }
    }
    
    String customerName = customer != null ? customer.getFullName() : "ID: " + request.getCustomerId();
    
    Label customerLabel = new Label("Клиент: " + customerName);
    customerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    
    Label typeLabel = new Label("Тип счета: " + request.getAccountType());
    typeLabel.setStyle("-fx-font-size: 14px;");
    
    Label dateLabel = new Label("Дата заявки: " + request.getRequestDate());
    dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
    
    String statusText = switch(request.getStatus()) {
        case "PENDING" -> "Ожидает рассмотрения";
        case "APPROVED" -> "Одобрено";
        case "REJECTED" -> "Отклонено";
        default -> request.getStatus();
    };
    
    Label statusLabel = new Label("Статус: " + statusText);
    statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
    
    infoBox.getChildren().addAll(customerLabel, typeLabel, dateLabel, statusLabel);
    
    headerBox.getChildren().addAll(iconLabel, infoBox);
    
    card.getChildren().add(headerBox);
    
    // Кнопки действий (только для PENDING заявок)
    if (request.getStatus().equals("PENDING")) {
        HBox buttonsBox = new HBox(10);
        
        Button btnApprove = new Button("✅ Одобрить");
        btnApprove.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button btnReject = new Button("❌ Отклонить");
        btnReject.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        
        btnApprove.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение");
            confirm.setHeaderText("Одобрить заявку?");
            confirm.setContentText(
                "Клиент: " + customerName + "\n" +
                "Тип счета: " + request.getAccountType() + "\n\n" +
                "Будет создан новый счет с нулевым балансом."
            );
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                if (bankService.approveAccountRequest(request.getId(), authService.getCurrentUser().getId())) {
                    authService.logAction("APPROVE_REQUEST", 
                        "Одобрена заявка #" + request.getId() + " для клиента ID: " + request.getCustomerId());
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Успех");
                    success.setHeaderText("Заявка одобрена!");
                    success.setContentText("Счет успешно создан для клиента.");
                    success.showAndWait();
                    
                    showAccountRequestsScreen();
                } else {
                    showAlert("Ошибка", "Не удалось одобрить заявку");
                }
            }
        });
        
        btnReject.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение");
            confirm.setHeaderText("Отклонить заявку?");
            confirm.setContentText(
                "Клиент: " + customerName + "\n" +
                "Тип счета: " + request.getAccountType() + "\n\n" +
                "Вы уверены?"
            );
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                if (bankService.rejectAccountRequest(request.getId(), authService.getCurrentUser().getId())) {
                    authService.logAction("REJECT_REQUEST", 
                        "Отклонена заявка #" + request.getId() + " для клиента ID: " + request.getCustomerId());
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Выполнено");
                    success.setHeaderText("Заявка отклонена");
                    success.showAndWait();
                    
                    showAccountRequestsScreen();
                } else {
                    showAlert("Ошибка", "Не удалось отклонить заявку");
                }
            }
        });
        
        buttonsBox.getChildren().addAll(btnApprove, btnReject);
        card.getChildren().add(buttonsBox);
    }
    
    return card;
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
private void refreshCustomerList(ListView<Customer> list) {
    list.getItems().clear();
    list.getItems().addAll(bankService.getAllCustomers());
}

private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

public static void main(String[] args) {
    launch(args);
}
}

//mvn clean compile exec:java
//testcllient test123
//admin admin123