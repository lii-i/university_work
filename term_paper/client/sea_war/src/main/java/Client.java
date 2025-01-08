import javafx.application.Application;

import java.io.*;
import java.net.Socket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;



// URL ВСЕГДА ПИШЕТСЯ ПЕРВЫМ ПРИ ПЕРЕДАЧЕ ЧЕГО - ЛИБО
public class Client extends Application {
    @FXML
    private static int life;
    private static Socket socket = null;
    private static battlefield e_battlefield;
    private static battlefield y_battlefield;
    private static  Ship[] e_ships;
    private static  Ship[] y_ships;
    private static Parent root;
    private static boolean check_end_game;
    public static void main(String[] args) {
        launch(args); // Запуск JavaFX-приложения
    }

    public  static void Response(){

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response;

            if((response = reader.readLine()) != null){
                if(response.charAt(0) == '-'){
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Противник вышел");
                        alert.showAndWait();
                    });
                    return;
                }
                for(int i =0; i<10; i++){
                    if(y_ships[i].Cells_ship(response.charAt(0)-48, response.charAt(1)-48) && (y_ships[i].GetLife()-1)==0){
                        String tmp = "";
                        int[][]cells = y_ships[i].GetCells();
                        y_ships[i].HpDamage(1);

                        GridPane gridPane = (GridPane) Client.getRoot().lookup("#player_battlefield");

                        for(int j =0; j< cells.length; j++){
                            if(j+1 == cells.length){
                                tmp += String.valueOf(cells[j][0]) + "," + String.valueOf(cells[j][1]);
                            }else{
                                tmp += String.valueOf(cells[j][0]) + "," + String.valueOf(cells[j][1]) + ",";
                            }
                            Node cell = Client.getNodeByRowColumnIndex(cells[j][0], cells[j][1], gridPane);
                            cell.setStyle("-fx-background-color: red;");
                        }

                        life -= 1;
                        if(life == 0){
                            PrintWriter write = new PrintWriter(Client.Get_socket().getOutputStream(), true);
                            write.println("End_game:0"+"+"+tmp+";");
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setContentText("Вы проиграли");
                                alert.showAndWait();
                            });
                            return;
                        }else{
                            PrintWriter write = new PrintWriter(Client.Get_socket().getOutputStream(), true);
                            write.println(String.valueOf(3) + ":" + tmp);
                        }
                        Thread t = new Thread(() -> {
                            Client.Response();
                        });
                        t.start();
                        return;
                    }
                    if(y_ships[i].Cells_ship(response.charAt(0)-48, response.charAt(1)-48) && (y_ships[i].GetLife()-1)!=0){
                        y_ships[i].HpDamage(1);
                        GridPane gridPane = (GridPane) Client.getRoot().lookup("#player_battlefield");
                        Node cell = Client.getNodeByRowColumnIndex(response.charAt(0)-48, response.charAt(1)-48, gridPane);
                        cell.setStyle("-fx-background-color: green;");

                        PrintWriter write = new PrintWriter(Client.Get_socket().getOutputStream(), true);
                        write.println(String.valueOf(1)+":");
                        Thread t = new Thread(() -> {
                            Client.Response();
                        });
                        t.start();
                        return;
                    }
                    if(y_ships[i].Cells_ship(response.charAt(0)-48, response.charAt(1)-48) == false && i+1 == 10){
                        GridPane gridPane = (GridPane) Client.getRoot().lookup("#player_battlefield");
                        Node cell = Client.getNodeByRowColumnIndex(response.charAt(0)-48, response.charAt(1)-48, gridPane);
                        cell.setStyle("-fx-background-color: gray;");

                        PrintWriter write = new PrintWriter(Client.Get_socket().getOutputStream(), true);
                        write.println(String.valueOf(2)+":");
                        Buttons_controller.setDisable(false);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Node getNodeByRowColumnIndex(int row, int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = gridPane.getRowIndex(node);
            Integer columnIndex = gridPane.getColumnIndex(node);

            // Сравниваем координаты
            if ((rowIndex != null && rowIndex == row) && (columnIndex != null && columnIndex == column)) {
                return node;
            }
        }
        return null; // Если узел не найден
    }
    // Метод для извлечения цвета
    public static String extractBackgroundColor(String style) {

        for (String part : style.split(";")) {
            if (part.trim().startsWith("-fx-background-color")) {
                return part.split(":")[1].trim(); // Возвращаем значение цвета
            }
        }
        return "Неизвестно";
    }

    public static boolean check_cell(int[] coordinates){
        int[] check_c = new int[2];
        boolean pr = true;

        if(coordinates[0] != 9){
            check_c[0] = coordinates[0]+1;
            check_c[1] = coordinates[1];
            if(y_battlefield.check_cells(check_c) == true){
                pr = false;
            }
        }
        if(coordinates[0] != 0){
            check_c[0] = coordinates[0]-1;
            check_c[1] = coordinates[1];
            if(y_battlefield.check_cells(check_c) == true){
                pr = false;
            }
        }
        if(coordinates[1] != 9){
            check_c[0] = coordinates[0];
            check_c[1] = coordinates[1] + 1;
            if(y_battlefield.check_cells(check_c) == true){
                pr = false;
            }
        }
        if(coordinates[1] != 0){
            check_c[0] = coordinates[0];
            check_c[1] = coordinates[1] - 1;
            if(y_battlefield.check_cells(check_c) == true){
                pr = false;
            }
        }
        return pr;
    }

    @Override
    public void start(Stage primaryStage) {
        try{
            life = 10;
            e_battlefield = new battlefield();
            y_battlefield = new battlefield();
            e_ships = new Ship[10];
            check_end_game = false;

            socket = new Socket("127.0.0.1", 8080);  // Подключаемся к серверу

            root = FXMLLoader.load(getClass().getClassLoader().getResource("Register.fxml"));
            Scene scene = new Scene(root);  // создаем сцену с root
            primaryStage.setScene(scene);   // устанавливаем сцену
            primaryStage.setTitle("Окно регистрации");
            primaryStage.show();            // показываем окно

            Button b_l = (Button) Client.root.lookup("#btnLogin");
            Button b_r = (Button) Client.root.lookup("#btnRegister");

            b_l.setDisable(true);
            b_r.setDisable(true);

            Thread t = new Thread(() -> {
                try {
                    PrintWriter write= new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response;
                    write.println("?");
                    while((response = reader.readLine()) != null){
                        if(response.charAt(0) == '+'){
                            b_l.setDisable(false);
                            b_r.setDisable(false);
                            break;
                        }else{
                            write.println("?");
                        }
                    }
                }catch(IOException e){

                }
            });
            t.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket Get_socket(){
        return socket;
    }
    public static battlefield Get_e_battlefield(){
        return e_battlefield;
    }
    public static void set_e_battlefield(battlefield b){
        e_battlefield = b;
    }
    public static battlefield Get_y_battlefield(){
        return y_battlefield;
    }
    public static void set_y_battlefield(battlefield b){
        y_battlefield = b;
    }
    public static Parent getRoot(){
        return root;
    }
    public static void setRoot(Parent r){
        root = r;
    }
    public static Ship[] Get_eShip(){
        return e_ships;
    }
    public static Ship[] Get_yShip(){
        return y_ships;
    }
    public static void Set_eShips(Ship[] ships){
        e_ships = ships;
    }
    public static void Set_yShips(Ship[] ships){
        y_ships = ships;
    }
    public static int Get_life(){
        return life;
    }
    public static void Set_life(int l){
        life = l;
    }
    public static boolean Get_check_end_game(){
        return check_end_game;
    }
    public static void Set_check_end_game(){;
        check_end_game = true;
    }

}