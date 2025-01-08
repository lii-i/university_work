import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class server {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/space_war";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public static void main(String[] args) throws Exception {
        // Создаем HTTP сервер на порту 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/login", new LoginHandler());
        server.setExecutor(null); // Используем стандартный пул потоков
        server.start();
        System.out.println("Сервер запущен на порту 8080");
    }

    // Обработчик для запросов на /login
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Извлечение строки запроса
            String fullQuery = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).readLine();
            String query = (fullQuery != null && fullQuery.contains(",")) ? fullQuery.split(",")[0] : fullQuery;

            String login = null;
            String password = null;

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue[0].equals("login")) {
                        login = keyValue[1];
                    } else if (keyValue[0].equals("password")) {
                        password = keyValue[1];
                    }
                }
            }

            // Проверка логина и пароля
            String response = (login != null && password != null && isValidUser(login, password))
                    ? "Пользователь найден"
                    : "Неверный логин или пароль";

            // Отправляем ответ клиенту
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean isValidUser(String login, String password) {
            // Проверяем логин и пароль в базе данных
            String query = "SELECT password FROM users WHERE login = ?";
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {

                pstmt.setString(1, login); // Устанавливаем логин в запрос

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String dbPassword = rs.getString("password");
                        // Сравниваем пароли
                        return password.equals(dbPassword);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Логирование ошибки
            }
            return false;
        }
    }
}
