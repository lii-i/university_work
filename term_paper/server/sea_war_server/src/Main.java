import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static java.lang.StrictMath.abs;

public class Main {
    static ServerSocket socket;
    static Connection connection;
    static Statement statement;
    public static CompletableFuture<Void> await(Socket[] clientSockets) {
        return CompletableFuture.runAsync(() -> {
            try {
                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSockets[0].getInputStream()));

                while (reader.readLine() != null && clientSockets[1] == null) {
                    writer.println(".");
                }
                writer.println("+");

                writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                writer.println("+");


            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        try {
            socket = new ServerSocket(8080);

            String url_db = "jdbc:postgresql://localhost:5432/Course";
            String user = "postgres";
            String password_db = "c456tx93";

            try {
                connection = DriverManager.getConnection(url_db, user, password_db);
                statement = connection.createStatement();
                if (connection != null) {
                    System.out.println("Успешное подключение к базе данных!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Ошибка подключения к базе данных.");
            }

            Socket[] clientSockets = new Socket[2];
            for (int i = 0; i<2; i++) {
                clientSockets[i] = socket.accept();
                if(i+1 != 2){
                    await(clientSockets);
                }else{
                }
            }

            Random random = new Random();
            int turn = random.nextInt(2);

            Thread client1 = new Thread(() -> {
                try {
                    System.out.println("Поток клиента с адресом: " + clientSockets[0].getInetAddress() + " начался");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSockets[0].getInputStream()));

                    String request;
                    String Login = "";
                    while ((request = reader.readLine()) != null) {
                        System.out.println("Получен запрос: " + request);

                        // Ищем индекс символа ':'
                        int colonIndex = request.indexOf(':');

                        // Если символ ':' найден, разделяем строку на URL и оставшуюся часть
                        String url = colonIndex != -1 ? request.substring(0, colonIndex) : "";
                        String remaining = colonIndex != -1 ? request.substring(colonIndex + 1) : "";

                        if (url.equals("check_login_and_password")) {
                            colonIndex = remaining.indexOf(',');

                            Login = colonIndex != -1 ? remaining.substring(0, colonIndex) : "";
                            String password = colonIndex != -1 ? remaining.substring(colonIndex + 1) : "";
                            try{
                                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM player WHERE login = ? AND password_p = ?");
                                preparedStatement.setString(1, Login);
                                preparedStatement.setString(2, password);

                                // Выполняем запрос
                                ResultSet resultSet = preparedStatement.executeQuery();

                                // Проверяем, существует ли запись
                                if (resultSet.next()) {
                                    String tmp="1:";

                                    PreparedStatement find = connection.prepareStatement("SELECT player_b FROM battlefield WHERE login_player = ?");
                                    find.setString(1, Login);

                                    // Выполняем запрос
                                    ResultSet resultfind = find.executeQuery();

                                    if (resultfind.next()) {
                                        tmp+=resultfind.getString("player_b");
                                    }

                                    PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                    writer.println(tmp); // Отправляем клиенту ответ
                                } else {
                                    PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                    writer.println("0"); // Отправляем клиенту ответ
                                }

                            }catch (SQLException e){

                            }
                        }
                        if (url.equals("registration")) {
                            colonIndex = remaining.indexOf(',');

                            Login = colonIndex != -1 ? remaining.substring(0, colonIndex) : "";
                            String password = colonIndex != -1 ? remaining.substring(colonIndex + 1) : "";

                            try {
                                // Проверяем, существует ли уже логин в базе данных
                                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM player WHERE login = ?");
                                preparedStatement.setString(1, Login);

                                // Выполняем запрос
                                ResultSet resultSet = preparedStatement.executeQuery();

                                // Если логин уже существует
                                if (resultSet.next()) {
                                    PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                    writer.println("0"); // Логин уже существует
                                } else {
                                    // Если логин не существует, отправляем ответ о возможности регистрации
                                    PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                    writer.println("1");

                                    // Подготавливаем запрос на добавление нового пользователя
                                    String insertSQL = "INSERT INTO player (login, password_p, life) VALUES (?, ?, ?)";
                                    try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
                                        insertStatement.setString(1, Login);
                                        insertStatement.setString(2, password); // Примечание: желательно хешировать пароль перед сохранением
                                        insertStatement.setInt(3, 10); // Начальное количество жизней

                                        // Выполнение запроса на добавление данных
                                        int rowsAffected = insertStatement.executeUpdate();  // Выполнение INSERT-запроса

                                        // Проверяем успешность добавления
                                        if (rowsAffected > 0) {
                                            System.out.println("Пользователь зарегистрирован!");
                                        } else {
                                            System.out.println("Ошибка при добавлении пользователя.");
                                        }
                                    } catch (SQLException e) {
                                        System.err.println("Ошибка при выполнении INSERT: " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            } catch (SQLException e) {
                                System.err.println("Ошибка при выполнении SELECT: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        if(url.equals("turn")){
                            //проверяем, есть ли сохраненная игра
                            System.out.println(turn);
                            PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                            writer.println(String.valueOf(turn)); // Отправляем клиенту ответ
                        }
                        if(url.equals("step")){
                            System.out.println(remaining);
                            if(clientSockets[1].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                writer.println("-");
                            }else{
                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                writer.println(remaining);
                            }
                        }
                        if(url.equals("2")){
                            System.out.println(remaining);
                            if(!clientSockets[1].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                writer.println("2");
                            }
                        }
                        if(url.equals("1")){
                            System.out.println(remaining);
                            if(clientSockets[1].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                writer.println("-");
                            }else{
                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                writer.println("1");
                            }
                        }
                        if(url.equals("3")){
                            System.out.println(remaining);
                            if(clientSockets[1].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                writer.println("-");
                            }else{
                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                writer.println("3:"+remaining);
                            }
                        }
                        if(url.equals("End_game")){
                            colonIndex = remaining.indexOf(';');

                            // Если символ ':' найден, разделяем строку на URL и оставшуюся часть
                            String url_symbol = colonIndex != -1 ? remaining.substring(0, colonIndex) : "";
                            String message_to_base = colonIndex != -1 ? remaining.substring(colonIndex + 1) : "";

                            if(message_to_base.equals("")){
                                if(!clientSockets[1].isClosed()){
                                    PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                    writer.println(url_symbol);
                                }else{
                                    PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                    writer.println(url_symbol);
                                }
                            }else{
                                try{
                                    // Поиск записи по логину
                                    String selectQuery = "SELECT * FROM " + "battlefield" + " WHERE login_player = ?";
                                    PreparedStatement find = connection.prepareStatement(selectQuery);
                                    find.setString(1, Login);
                                    ResultSet resultSet = find.executeQuery();

                                    if (resultSet.next()) {
                                        // Если запись найдена, удалить её
                                        String deleteQuery = "DELETE FROM " + "battlefield" + " WHERE login_player = ?";
                                        PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                                        deleteStatement.setString(1, Login);
                                        int rowsAffected = deleteStatement.executeUpdate();

                                        if (rowsAffected > 0) {
                                            System.out.println("Запись с логином '" + Login + "' успешно удалена.");

                                            String insertQuery = "INSERT INTO " + "battlefield" + " (login_player, player_b) VALUES (?, ?)";
                                            PreparedStatement insert = connection.prepareStatement(insertQuery);
                                            insert.setString(1, Login);
                                            insert.setString(2,message_to_base);
                                            int resultins = insert.executeUpdate();

                                            if (resultins > 0) {
                                                System.out.println("Запись с логином '" + Login + "' успешно добавлена.");
                                                if(!clientSockets[1].isClosed()){
                                                    PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                                    writer.println(url_symbol);
                                                }

                                            }else{
                                                System.out.println("Ошибка добавления");
                                            }
                                        } else {
                                            System.out.println("Не удалось удалить запись с логином '" + Login + "'.");
                                        }
                                    } else {
                                        System.out.println("Запись с логином '" + Login + "' не найдена.");

                                        String insertQuery = "INSERT INTO " + "battlefield" + " (login_player, player_b) VALUES (?, ?)";
                                        PreparedStatement insert = connection.prepareStatement(insertQuery);
                                        insert.setString(1, Login);
                                        insert.setString(2,message_to_base);
                                        int resultins = insert.executeUpdate();

                                        if (resultins > 0) {
                                            System.out.println("Запись с логином '" + Login + "' успешно добавлена.");
                                            if(!clientSockets[1].isClosed()){
                                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                                writer.println(url_symbol);
                                            }

                                        }else{
                                            System.out.println("Ошибка добавления");
                                        }
                                    }


                                }catch (SQLException e){

                                }
                            }

                        }
                    }
                    clientSockets[0].close();
                } catch (IOException e) {
                    System.err.println("Ошибка в потоке клиента: " + e.getMessage());
                }

                System.out.println("Поток клиента с адресом: " + clientSockets[0].getInetAddress() + " закончился");
            });
            client1.start();

            Thread client2 = new Thread(() -> {
                try {
                    System.out.println("Поток клиента с адресом: " + clientSockets[1].getInetAddress() + " начался");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSockets[1].getInputStream()));

                    String request;
                    String Login = "";
                    while ((request = reader.readLine()) != null) {
                        System.out.println("Получен запрос: " + request);

                        // Ищем индекс символа ':'
                        int colonIndex = request.indexOf(':');

                        // Если символ ':' найден, разделяем строку на URL и оставшуюся часть
                        String url = colonIndex != -1 ? request.substring(0, colonIndex) : "";
                        String remaining = colonIndex != -1 ? request.substring(colonIndex + 1) : "";

                        if (url.equals("check_login_and_password")) {
                            colonIndex = remaining.indexOf(',');

                            Login = colonIndex != -1 ? remaining.substring(0, colonIndex) : "";
                            String password = colonIndex != -1 ? remaining.substring(colonIndex + 1) : "";
                            try{
                                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM player WHERE login = ? AND password_p = ?");
                                preparedStatement.setString(1, Login);
                                preparedStatement.setString(2, password);

                                // Выполняем запрос
                                ResultSet resultSet = preparedStatement.executeQuery();

                                // Проверяем, существует ли запись
                                if (resultSet.next()) {
                                    String tmp="1:";

                                    PreparedStatement find = connection.prepareStatement("SELECT player_b FROM battlefield WHERE login_player = ?");
                                    find.setString(1, Login);

                                    // Выполняем запрос
                                    ResultSet resultfind = find.executeQuery();

                                    if (resultfind.next()) {
                                        tmp+=resultfind.getString("player_b");
                                    }

                                    PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                    writer.println(tmp); // Отправляем клиенту ответ
                                } else {
                                    PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                    writer.println("0"); // Отправляем клиенту ответ
                                }

                            }catch (SQLException e){

                            }
                        }
                        if (url.equals("registration")) {
                            colonIndex = remaining.indexOf(',');

                            Login = colonIndex != -1 ? remaining.substring(0, colonIndex) : "";
                            String password = colonIndex != -1 ? remaining.substring(colonIndex + 1) : "";

                            try {
                                // Проверяем, существует ли уже логин в базе данных
                                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM player WHERE login = ?");
                                preparedStatement.setString(1, Login);

                                // Выполняем запрос
                                ResultSet resultSet = preparedStatement.executeQuery();

                                if (resultSet.next()) {
                                    // Если логин уже существует
                                    PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                    writer.println("0"); // Логин уже существует

                                } else {
                                    // Если логин не существует, отправляем ответ о возможности регистрации
                                    PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                    writer.println("1"); // Регистрация возможна


                                    // Подготавливаем запрос на добавление нового пользователя
                                    String insertSQL = "INSERT INTO player (login, password_p, life) VALUES (?, ?, ?)";
                                    try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
                                        insertStatement.setString(1, Login);
                                        insertStatement.setString(2, password); // Примечание: желательно хешировать пароль перед сохранением
                                        insertStatement.setInt(3, 10); // Начальное количество жизней

                                        // Выполнение запроса на добавление данных
                                        int rowsAffected = insertStatement.executeUpdate();  // Выполнение INSERT-запроса

                                        // Проверяем успешность добавления
                                        if (rowsAffected > 0) {
                                            System.out.println("Пользователь зарегистрирован!");
                                        } else {
                                            System.out.println("Ошибка при добавлении пользователя.");
                                        }
                                    } catch (SQLException e) {
                                        System.err.println("Ошибка при выполнении INSERT: " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                }

                            } catch (SQLException e) {
                                System.err.println("Ошибка при выполнении SELECT: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        if(url.equals("turn")){
                            //проверяем, есть ли сохраненная игра
                            System.out.println(turn);
                            PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                            writer.println(String.valueOf(abs(turn-1))); // Отправляем клиенту ответ
                        }
                        if(url.equals("step")){
                            System.out.println(remaining);
                            if(clientSockets[0].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                writer.println("-");
                            }else{
                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                writer.println(remaining);
                            }
                        }
                        if(url.equals("2")){
                            System.out.println(remaining);
                            if(!clientSockets[0].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                writer.println("2");
                            }
                        }
                        if(url.equals("1")){
                            System.out.println(remaining);
                            if(clientSockets[0].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                writer.println("-");
                            }else{
                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                writer.println("1");
                            }
                        }
                        if(url.equals("3")){
                            System.out.println(remaining);
                            if(clientSockets[0].isClosed()){
                                PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                writer.println("-");
                            }else{
                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                writer.println("3:"+remaining);
                            }
                        }
                        if(url.equals("End_game")){
                            colonIndex = remaining.indexOf(';');

                            // Если символ ':' найден, разделяем строку на URL и оставшуюся часть
                            String url_symbol = colonIndex != -1 ? remaining.substring(0, colonIndex) : "";
                            String message_to_base = colonIndex != -1 ? remaining.substring(colonIndex + 1) : "";

                            if(message_to_base.equals("")){
                                if(!clientSockets[0].isClosed()){
                                    PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                    writer.println(url_symbol);
                                }else{
                                    PrintWriter writer = new PrintWriter(clientSockets[1].getOutputStream(), true);
                                    writer.println(url_symbol);
                                }
                            }else{
                                try{
                                    // Поиск записи по логину
                                    String selectQuery = "SELECT * FROM " + "battlefield" + " WHERE login_player = ?";
                                    PreparedStatement find = connection.prepareStatement(selectQuery);
                                    find.setString(1, Login);
                                    ResultSet resultSet = find.executeQuery();

                                    if (resultSet.next()) {
                                        // Если запись найдена, удалить её
                                        String deleteQuery = "DELETE FROM " + "battlefield" + " WHERE login_player = ?";
                                        PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                                        deleteStatement.setString(1, Login);
                                        int rowsAffected = deleteStatement.executeUpdate();

                                        if (rowsAffected > 0) {
                                            System.out.println("Запись с логином '" + Login + "' успешно удалена.");

                                            String insertQuery = "INSERT INTO " + "battlefield" + " (login_player, player_b) VALUES (?, ?)";
                                            PreparedStatement insert = connection.prepareStatement(insertQuery);
                                            insert.setString(1, Login);
                                            insert.setString(2,message_to_base);
                                            int resultins = insert.executeUpdate();

                                            if (resultins > 0) {
                                                System.out.println("Запись с логином '" + Login + "' успешно добавлена.");
                                                if(!clientSockets[0].isClosed()){
                                                    PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                                    writer.println(url_symbol);
                                                }

                                            }else{
                                                System.out.println("Ошибка добавления");
                                            }
                                        } else {
                                            System.out.println("Не удалось удалить запись с логином '" + Login + "'.");
                                        }
                                    } else {
                                        System.out.println("Запись с логином '" + Login + "' не найдена.");

                                        String insertQuery = "INSERT INTO " + "battlefield" + " (login_player, player_b) VALUES (?, ?)";
                                        PreparedStatement insert = connection.prepareStatement(insertQuery);
                                        insert.setString(1, Login);
                                        insert.setString(2,message_to_base);
                                        int resultins = insert.executeUpdate();

                                        if (resultins > 0) {
                                            System.out.println("Запись с логином '" + Login + "' успешно добавлена.");
                                            if(!clientSockets[0].isClosed()){
                                                PrintWriter writer = new PrintWriter(clientSockets[0].getOutputStream(), true);
                                                writer.println(url_symbol);
                                            }

                                        }else{
                                            System.out.println("Ошибка добавления");
                                        }
                                    }

                                }catch (SQLException e){

                                }
                            }

                        }
                    }
                    clientSockets[1].close();
                } catch (IOException e) {
                    System.err.println("Ошибка в потоке клиента: " + e.getMessage());
                }

                System.out.println("Поток клиента с адресом: " + clientSockets[1].getInetAddress() + " закончился");
            });
            client2.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

