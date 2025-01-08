import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class enter {

    @FXML
    private TextArea ta_l;  // Текстовое поле для логина

    @FXML
    private TextArea ta_p;  // Текстовое поле для пароля

    @FXML
    private Button btnLogin;  // Кнопка "Войти"

    @FXML
    private Button btnRegister;  // Кнопка "Зарегистрироваться"

    public void entering(){

        try {
            Socket socket = Client.Get_socket();

            if(ta_l.getText().equals("") || ta_p.getText().equals("") || ta_p.getText().length() < 4){
                Alert alert = new Alert(Alert.AlertType.INFORMATION); // Тип сообщения: информационное
                alert.setContentText("Введите данные"); // Основной текст сообщения
                alert.showAndWait(); // Показать и ждать закрытия пользователем
                return;
            }

            PrintWriter write= new PrintWriter(socket.getOutputStream(), true);
            write.println("check_login_and_password:" + ta_l.getText()+","+ta_p.getText());

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response;
            if((response = reader.readLine()) != null){
                if(response.charAt(0) == '0'){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION); // Тип сообщения: информационное
                    alert.setContentText("Неверные логин или пароль"); // Основной текст сообщения
                    alert.showAndWait(); // Показать и ждать закрытия пользователем
                } else{
                    Stage currentStage = (Stage) ta_l.getScene().getWindow();
                    currentStage.close();

                    // Запустить основное приложение
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Course.fxml")); // Укажите ваш файл FXML
                     Client.setRoot(loader.load());

                    Stage mainStage = new Stage();
                    mainStage.setTitle("Морской бой");
                    mainStage.setScene(new Scene(Client.getRoot()));
                    mainStage.setFullScreen(false);
                    mainStage.show();

                    if(response.equals("1:")){
                        Buttons_controller.rand();
                        write.println("turn:");
                        if ((response = reader.readLine()) != null) {
                            if (response.charAt(0) == '1') {
                                Buttons_controller.setDisable(false);
                            }
                            if (response.charAt(0) == '0') {
                                Buttons_controller.setDisable(true);
                                Thread t = new Thread(() -> {
                                    Client.Response();
                                });
                                t.start();
                            }
                        }
                    }else{
                        GridPane gridPane = (GridPane) Client.getRoot().lookup("#player_battlefield");
                        GridPane gridPane_e = (GridPane) Client.getRoot().lookup("#enemy_battlefield");
                        Ship[] ships = new Ship[10];

                        Client.set_y_battlefield(new battlefield());
                        for(int i =0; i<10; i++){
                            for(int j=0; j<10; j++){
                                Node cell = Client.getNodeByRowColumnIndex(i, j, gridPane);
                                cell.setStyle("-fx-background-color: white;");
                                cell = Client.getNodeByRowColumnIndex(i, j, gridPane_e);
                                cell.setStyle("-fx-background-color: white;");
                            }
                        }

                        int i = 5;

                        int sc = 0;
                        while(sc<4){
                            int y = response.charAt(i) - 48;
                            i++;
                            int x = response.charAt(i)- 48;
                            i++;
                            if(response.charAt(i) == 'b'){
                                int[][] tmp = {{y, x}};
                                ships[sc] = new Ship(tmp,1);
                                i+=5;
                                sc++;
                                Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                cell.setStyle("-fx-background-color: blue;");
                            }
                            if(response.charAt(i) == 'r'){
                                int[][] tmp = {{y, x}};
                                ships[sc] = new Ship(tmp,0);
                                i+=4;
                                sc++;
                                Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                cell.setStyle("-fx-background-color: red;");
                            }
                        }

                        while(sc<7){
                            int life = 2;
                            int[][] tmp = {{-1,-1},{-1,-1}};
                            for(int sc_c =0; sc_c<2; sc_c++){
                                int y = response.charAt(i) - 48;
                                i++;
                                int x = response.charAt(i) - 48;
                                i++;
                                if(response.charAt(i) == 'b'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=5;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: blue;");
                                }
                                if(response.charAt(i) == 'r'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=4;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: red;");
                                    life = 0;
                                }
                                if(response.charAt(i) == 'g'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=6;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: green;");
                                    life -= 1;
                                }
                            }
                            ships[sc] = new Ship(tmp,life);
                            sc++;
                        }

                        while(sc<9){
                            int life = 3;
                            int[][] tmp = {{-1,-1},{-1,-1},{-1,-1}};
                            for(int sc_c =0; sc_c<3; sc_c++){
                                int y = response.charAt(i) - 48;
                                i++;
                                int x = response.charAt(i) - 48;
                                i++;
                                if(response.charAt(i) == 'b'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=5;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: blue;");
                                }
                                if(response.charAt(i) == 'r'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=4;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: red;");
                                    life = 0;
                                }
                                if(response.charAt(i) == 'g'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=6;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: green;");
                                    life -= 1;
                                }
                            }
                            ships[sc] = new Ship(tmp,life);
                            sc++;
                        }

                        while(sc<10){
                            int life = 4;
                            int[][] tmp = {{-1,-1},{-1,-1},{-1,-1}, {-1,-1}};
                            for(int sc_c =0; sc_c<4; sc_c++){
                                int y = response.charAt(i) - 48;
                                i++;
                                int x = response.charAt(i) - 48;
                                i++;
                                if(response.charAt(i) == 'b'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=5;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: blue;");
                                }
                                if(response.charAt(i) == 'r'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=4;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: red;");
                                    life = 0;
                                }
                                if(response.charAt(i) == 'g'){
                                    tmp[sc_c][0] = y;
                                    tmp[sc_c][1] = x;
                                    i+=6;
                                    Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                                    cell.setStyle("-fx-background-color: green;");
                                    life -= 1;
                                }

                            }
                            ships[sc] = new Ship(tmp,life);
                            sc++;
                        }

                        while(response.charAt(i) != ';'){
                            int y = response.charAt(i) - 48;
                            i++;
                            int x = response.charAt(i) - 48;

                            Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane);
                            cell.setStyle("-fx-background-color: gray;");
                            i+=6;
                        }

                        i+=7;

                        while(response.charAt(i) != ';'){
                            int y = response.charAt(i) - 48;
                            i++;
                            int x = response.charAt(i) - 48;
                            i++;

                            if(response.charAt(i) == 'b'){
                                i+=5;
                                Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane_e);
                                cell.setStyle("-fx-background-color: blue;");
                            }
                            if(response.charAt(i) == 'r'){
                                i+=4;
                                Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane_e);
                                cell.setStyle("-fx-background-color: red;");
                            }
                            if(response.charAt(i) == 'g' && response.charAt(i+2) == 'e'){
                                i+=6;
                                Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane_e);
                                cell.setStyle("-fx-background-color: green;");
                            }
                            if(response.charAt(i) == 'g' && response.charAt(i+2) == 'a'){
                                i+=5;
                                Node cell = Client.getNodeByRowColumnIndex(y, x, gridPane_e);
                                cell.setStyle("-fx-background-color: gray;");
                            }
                        }

                        i++;
                        String life;
                        if(i == response.length() - 2){
                            life = Character.toString(response.charAt(i));
                            i+=1;
                        }else{
                            life = Character.toString(response.charAt(i)) + Character.toString(response.charAt(i+1));
                            i+=2;
                        }

                        if(response.charAt(i) == '1'){
                            Buttons_controller.setDisable(false);
                        }else{
                            Buttons_controller.setDisable(true);
                            Thread t = new Thread(() -> {
                                Client.Response();
                            });
                            t.start();
                        }
                        Client.Set_life(Integer.parseInt(life));
                        Client.Set_yShips(ships);
                    }
                }
            }

        } catch (Exception e) {

        }
    }

    public void login(){
        try {
            Socket socket = Client.Get_socket();  // Подключаемся к серверу

            if (ta_l.getText().equals("") || ta_p.getText().equals("") || ta_p.getText().length() < 4) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION); // Тип сообщения: информационное
                alert.setContentText("Введите данные"); // Основной текст сообщения
                alert.showAndWait(); // Показать и ждать закрытия пользователем
                return;
            }

            PrintWriter write = new PrintWriter(socket.getOutputStream(), true);
            write.println("registration:" + ta_l.getText() + "," + ta_p.getText());
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response;

            if ((response = reader.readLine()) != null) {
                if (response.charAt(0) == '1') {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION); // Тип сообщения: информационное
                    alert.setContentText("Вы зарегистрированы"); // Основной текст сообщения
                    alert.showAndWait(); // Показать и ждать закрытия пользователем

                    Stage currentStage = (Stage) ta_l.getScene().getWindow();
                    currentStage.close();

                    // Запустить основное приложение
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Course.fxml")); // Укажите ваш файл FXML
                    Client.setRoot(loader.load());

                    Stage mainStage = new Stage();
                    mainStage.setTitle("Основное приложение");
                    mainStage.setScene(new Scene(Client.getRoot()));
                    mainStage.show();
                    Buttons_controller.rand();

                    write.println("turn:");
                    if ((response = reader.readLine()) != null) {
                        if (response.charAt(0) == '1') {
                            Buttons_controller.setDisable(false);
                        }
                        if (response.charAt(0) == '0') {
                            Buttons_controller.setDisable(true);
                            Thread t = new Thread(() -> {
                                Client.Response();
                            });
                            t.start();
                        }
                    }

                }else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION); // Тип сообщения: информационное
                    alert.setContentText("Такой пользователь уже существует"); // Основной текст сообщения
                    alert.showAndWait(); // Показать и ждать закрытия пользователем
                }
            }
        } catch (IOException e) {

        }
    }
}
