import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;

import static java.lang.StrictMath.abs;

public class Buttons_controller {
    @FXML
    private Button step;

    @FXML
    private Button exit;

    private static Buttons_controller instance; // Храним ссылку на контроллер

    private int[] y_x = {-1,-1};

    @FXML
    public void initialize() {
        instance = this; // Сохраняем экземпляр контроллера
        System.out.println("Controller initialized. Step: " + step);
    }

    @FXML
    public void end_move(){
        try{

            if(y_x[0] == -1){
                Alert alert = new Alert(Alert.AlertType.INFORMATION); // Тип сообщения: информационное
                alert.setContentText("клетка не выбрана"); // Основной текст сообщения
                alert.showAndWait(); // Показать и ждать закрытия пользователем
                return;
            }

            setDisable(true);

            PrintWriter write = new PrintWriter(Client.Get_socket().getOutputStream(), true);
            write.println("step:" + String.valueOf(y_x[0]) + String.valueOf(y_x[1]));// отправляем координату.Еще нужно отправлять попал противник или нет

            BufferedReader reader = new BufferedReader(new InputStreamReader(Client.Get_socket().getInputStream()));
            String response; //блокирующее чтение(код дальше не выполняется)

            if((response = reader.readLine()) == null) {
                return;
            }

            char miss_or_not = response.charAt(0);

            if(miss_or_not == '0'){
                int colonIndex = response.indexOf('+');
                String remaining = colonIndex != -1 ? response.substring(colonIndex + 1) : "";

                int[][] tmp = {{0,0}, {0,0}, {0,0}, {0,0}};
                int sc_i=0;
                int sc_j=0;

                for(int i =0; i<remaining.length(); i++){
                    if(remaining.charAt(i) == ','){
                        if(sc_j == 0){
                            sc_j += 1;
                        }else{
                            sc_j = 0;
                            sc_i += 1;
                        }
                    }else{
                        tmp[sc_i][sc_j] = remaining.charAt(i)-48;
                    }
                }

                GridPane gridPane = (GridPane) Client.getRoot().lookup("#enemy_battlefield");
                for(int i =0; i<= sc_i; i++){
                    Node cell = Client.getNodeByRowColumnIndex(tmp[i][0], tmp[i][1], gridPane);
                    cell.setStyle("-fx-background-color: red;");
                    cell.setDisable(true);
                }

                for(int i =0; i<10; i++){
                    for(int j =0; j<10; j++){
                        Node cell = Client.getNodeByRowColumnIndex(i,j, gridPane);
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("white")){
                            cell.setDisable(false);
                        }
                    }
                }
                y_x[0] = -1;
                y_x[1] = -1;
                setDisable(true);
                Client.Set_check_end_game();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Вы выиграли");
                    alert.showAndWait();
                });

                return;
            }
            if(miss_or_not == '1'){
                GridPane gridPane = (GridPane) Client.getRoot().lookup("#enemy_battlefield");
                Node cell = Client.getNodeByRowColumnIndex(y_x[0], y_x[1], gridPane);
                cell.setStyle("-fx-background-color: green;");
                cell.setDisable(true);
                for(int i =0; i<10; i++){
                    for(int j =0; j<10; j++){
                        cell = Client.getNodeByRowColumnIndex(i,j, gridPane);
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("white")){
                            cell.setDisable(false);
                        }
                    }
                }
                y_x[0] = -1;
                y_x[1] = -1;
                setDisable(false);
                return;
            }
            if(miss_or_not == '2'){
                GridPane gridPane = (GridPane) Client.getRoot().lookup("#enemy_battlefield");
                Node cell = Client.getNodeByRowColumnIndex(y_x[0], y_x[1], gridPane);
                cell.setStyle("-fx-background-color: gray;");
                cell.setDisable(true);
                for(int i =0; i<10; i++){
                    for(int j =0; j<10; j++){
                        cell = Client.getNodeByRowColumnIndex(i,j, gridPane);
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("white")){
                            cell.setDisable(false);
                        }
                    }
                }
                y_x[0] = -1;
                y_x[1] = -1;
                Thread t = new Thread(() -> {
                    Client.Response();
                });
                t.start();
                return;
            }
            if(miss_or_not == '3'){
                int colonIndex = response.indexOf(':');
                String remaining = colonIndex != -1 ? response.substring(colonIndex + 1) : "";

                int[][] tmp = {{0,0}, {0,0}, {0,0}, {0,0}};
                int sc_i=0;
                int sc_j=0;

                for(int i =0; i<remaining.length(); i++){
                    if(remaining.charAt(i) == ','){
                        if(sc_j == 0){
                            sc_j += 1;
                        }else{
                            sc_j = 0;
                            sc_i += 1;
                        }
                    }else{
                        tmp[sc_i][sc_j] = remaining.charAt(i)-48;
                    }
                }

                GridPane gridPane = (GridPane) Client.getRoot().lookup("#enemy_battlefield");
                for(int i =0; i<= sc_i; i++){
                    Node cell = Client.getNodeByRowColumnIndex(tmp[i][0], tmp[i][1], gridPane);
                    cell.setStyle("-fx-background-color: red;");
                    cell.setDisable(true);
                }

                for(int i =0; i<10; i++){
                    for(int j =0; j<10; j++){
                        Node cell = Client.getNodeByRowColumnIndex(i,j, gridPane);
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("white")){
                            cell.setDisable(false);
                        }
                    }
                }
                y_x[0] = -1;
                y_x[1] = -1;
                setDisable(false);
                return;
            }
            if(miss_or_not == '-'){

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Противник вышел");
                    alert.showAndWait();
                });
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void setDisable(boolean m) {
        if (instance != null && instance.step != null) {
            instance.step.setDisable(m); // Управляем кнопкой через экземпляр
        }
    }

    @FXML
    public void HandleClick(ActionEvent event){
        Button clicked_button = (Button) event.getSource();

        GridPane gridPane = (GridPane) Client.getRoot().lookup("#enemy_battlefield");
        if(Client.extractBackgroundColor(clicked_button.getStyle()).equals("blue")){
            y_x[0] = -1;
            y_x[1] = -1;
            for(int i =0; i<10; i++){
                for(int j =0; j<10; j++){
                    Node cell = Client.getNodeByRowColumnIndex(i,j, gridPane);
                    if(Client.extractBackgroundColor(cell.getStyle()).equals("white")){
                        cell.setDisable(false);
                    }
                }
            }
            clicked_button.setStyle("-fx-background-color: white;");
        }else{
            clicked_button.setStyle("-fx-background-color: blue;");
            for(int i =0; i<10; i++){
                for(int j =0; j<10; j++){
                    Node cell = Client.getNodeByRowColumnIndex(i,j, gridPane);
                    if(i != gridPane.getRowIndex(clicked_button) || j != gridPane.getColumnIndex(clicked_button)){
                        cell.setDisable(true);
                    }
                }
            }
            y_x[0] = gridPane.getRowIndex(clicked_button);
            y_x[1] = gridPane.getColumnIndex(clicked_button);
        }
    }

    @FXML
    public void Exit(){
        Stage stage = (Stage) exit.getScene().getWindow();
        try {
            PrintWriter write = new PrintWriter(Client.Get_socket().getOutputStream(), true);
            if(Client.Get_life() != 0 && Client.Get_check_end_game()==false){
                String request = "End_game:-;me:";
                GridPane gridPane = (GridPane) Client.getRoot().lookup("#player_battlefield");

                Ship[] ships = Client.Get_yShip();
                for(int i =0 ; i<10 ; i++){
                    int[][] cells = ships[i].GetCells();
                    for(int j =0; j<cells.length; j++){
                        if(Client.extractBackgroundColor(Client.getNodeByRowColumnIndex(cells[j][0], cells[j][1], gridPane).getStyle()).equals("blue")){
                            request += String.valueOf(cells[j][0])+ String.valueOf(cells[j][1])+"blue,";
                        }
                        if(Client.extractBackgroundColor(Client.getNodeByRowColumnIndex(cells[j][0], cells[j][1], gridPane).getStyle()).equals("red")){
                            request += String.valueOf(cells[j][0])+ String.valueOf(cells[j][1])+"red,";
                        }
                        if(Client.extractBackgroundColor(Client.getNodeByRowColumnIndex(cells[j][0], cells[j][1], gridPane).getStyle()).equals("green")){
                            request += String.valueOf(cells[j][0])+ String.valueOf(cells[j][1])+"green,";
                        }
                    }
                }

                for(int i =0; i<10; i++){
                    for(int j=0; j<10; j++){
                        Node cell = Client.getNodeByRowColumnIndex(i, j, gridPane);
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("gray")){
                            request += String.valueOf(i)+ String.valueOf(j)+"gray,";
                        }
                    }
                    if(i+1 == 10){
                        request += ";";
                    }
                }

                gridPane = (GridPane) Client.getRoot().lookup("#enemy_battlefield");
                request += "enemy:";
                for(int i =0; i<10; i++){
                    for(int j=0; j<10; j++){
                        Node cell = Client.getNodeByRowColumnIndex(i, j, gridPane);
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("green")){
                            request += String.valueOf(i)+ String.valueOf(j)+"green,";
                        }
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("red")){
                            request += String.valueOf(i)+ String.valueOf(j)+"red,";
                        }
                        if(Client.extractBackgroundColor(cell.getStyle()).equals("gray")){
                            request += String.valueOf(i)+ String.valueOf(j)+"gray,";
                        }
                    }
                    if(i+1 == 10){
                        request += ";";
                    }
                }

                request += String.valueOf(Client.Get_life());
                if(step.isDisable() == true){
                    request += "0";
                }else{
                    request += "1";
                }
                write.println(request);
                Client.Get_socket().close();
                stage.close();
            }else{
                write = new PrintWriter(Client.Get_socket().getOutputStream(), true);
                write.println("End_game:-;");
                Client.Get_socket().close();
                stage.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean Get_step_desable(){
        return instance.step.isDisable();
    }

    @FXML
    public static void rand() throws IOException {
        GridPane gridPane = (GridPane) Client.getRoot().lookup("#player_battlefield");
        GridPane gridPane_e = (GridPane) Client.getRoot().lookup("#enemy_battlefield");

        //задаем расстановку
        Random random = new Random();
        String cell_color;
        Ship[] ships = new Ship[10];
        int sc =0;

        Client.set_y_battlefield(new battlefield());
        for(int i =0; i<10; i++){
            for(int j=0; j<10; j++){
                Node cell = Client.getNodeByRowColumnIndex(i, j, gridPane);
                cell.setStyle("-fx-background-color: white;");
                cell = Client.getNodeByRowColumnIndex(i, j, gridPane_e);
                cell.setStyle("-fx-background-color: white;");
            }
        }

        // однопалубные корабли
        int[] coordinates = new int[2];
        for(int i =0; i<4; i++){
            coordinates[0] = random.nextInt(10);
            coordinates[1] = random.nextInt(10);
            Node cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);

            cell_color = Client.extractBackgroundColor(cell.getStyle());

            if(cell_color.equals("blue")){
                i--;
            }else{
                if(Client.check_cell(coordinates) == true){
                    cell.setStyle("-fx-background-color: blue;");
                    Client.Get_y_battlefield().add_in_used(coordinates);
                    int[][] tmp = {{coordinates[0], coordinates[1]}};
                    ships[sc] = new Ship(tmp,1);
                    sc++;
                }else{
                    i--;

                }
            }
        }

        //двухпалубные корабли
        for(int i =0; i<3; i++){
            coordinates[0] = random.nextInt(10);
            coordinates[1] = random.nextInt(10);
            Node cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);

            cell_color = Client.extractBackgroundColor(cell.getStyle());

            if(cell_color.equals("blue")){
                i--;
            }else{
                if(Client.check_cell(coordinates) == true){

                    int rand_direction = random.nextInt(2); // 0 - по вертикали 1 - по горизонтали
                    for(int j =0; j<2; j++){
                        if(rand_direction == 0){
                            if(coordinates[0] != 9){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0]+1;
                                buf_c[1] = coordinates[1];
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true){
                                    Client.Get_y_battlefield().add_in_used(coordinates);
                                    Client.Get_y_battlefield().add_in_used(buf_c);

                                    cell.setStyle("-fx-background-color: blue;");
                                    cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                    cell.setStyle("-fx-background-color: blue;");
                                    int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}};
                                    ships[sc] = new Ship(tmp,2);
                                    sc++;
                                    break;
                                }

                            }
                            if(coordinates[0] != 0){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0]-1;
                                buf_c[1] = coordinates[1];
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true){
                                    Client.Get_y_battlefield().add_in_used(coordinates);
                                    Client.Get_y_battlefield().add_in_used(buf_c);

                                    cell.setStyle("-fx-background-color: blue;");
                                    cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                    cell.setStyle("-fx-background-color: blue;");
                                    int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}};
                                    ships[sc] = new Ship(tmp,2);
                                    sc++;
                                    break;
                                }
                            }
                        }else{
                            if(coordinates[1] != 9){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0];
                                buf_c[1] = coordinates[1]+1;
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true){
                                    Client.Get_y_battlefield().add_in_used(coordinates);
                                    Client.Get_y_battlefield().add_in_used(buf_c);

                                    cell.setStyle("-fx-background-color: blue;");
                                    cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                    cell.setStyle("-fx-background-color: blue;");
                                    int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}};
                                    ships[sc] = new Ship(tmp,2);
                                    sc++;
                                    break;
                                }
                            }
                            if(coordinates[1] != 0){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0];
                                buf_c[1] = coordinates[1]-1;
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true){
                                    Client.Get_y_battlefield().add_in_used(coordinates);
                                    Client.Get_y_battlefield().add_in_used(buf_c);

                                    cell.setStyle("-fx-background-color: blue;");
                                    cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                    cell.setStyle("-fx-background-color: blue;");
                                    int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}};
                                    ships[sc] = new Ship(tmp,2);
                                    sc++;
                                    break;
                                }
                            }
                        }
                        if(j +1 == 2){
                            i--;
                        }
                        rand_direction = abs(rand_direction - 1);
                    }


                }else{
                    i--;

                }
            }
        }

        //трехпалубные корабли
        for(int i =0; i<2; i++){
            coordinates[0] = random.nextInt(10);
            coordinates[1] = random.nextInt(10);
            Node cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);

            cell_color = Client.extractBackgroundColor(cell.getStyle());

            if(cell_color.equals("blue")){
                i--;
            }else{
                if(Client.check_cell(coordinates) == true){

                    int rand_direction = random.nextInt(2); // 0 - по вертикали 1 - по горизонтали
                    for(int j =0; j<2; j++){
                        if(rand_direction == 0){
                            if(coordinates[0] != 9){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0]+1;
                                buf_c[1] = coordinates[1];
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[0] != 9){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0]+1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }else if(coordinates[0] != 0)  {
                                        buf_buf_c[0] = coordinates[0]-1;
                                        buf_buf_c[1] = buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,3);
                                            sc++;
                                            break;
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[0] != 0)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = coordinates[0]-1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }
                                }
                            }
                            if(coordinates[0] != 0){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0]-1;
                                buf_c[1] = coordinates[1];
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[0] != 0){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0]-1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());

                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }else if(coordinates[0] != 9)  {
                                        buf_buf_c[0] = coordinates[0]+1;
                                        buf_buf_c[1] = buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,3);
                                            sc++;
                                            break;
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[0] != 9)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = coordinates[0]+1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }
                                }
                            }
                        }else{
                            if(coordinates[1] != 9){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0];
                                buf_c[1] = coordinates[1]+1;
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[1] != 9){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = buf_c[1]+1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[1] != 0)  {
                                        buf_buf_c[0] = buf_c[0];
                                        buf_buf_c[1] = coordinates[1]-1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,3);
                                            sc++;
                                            break;
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[1] != 0)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = coordinates[1]-1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }
                                }
                            }
                            if(coordinates[1] != 0){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0];
                                buf_c[1] = coordinates[1]-1;
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[1] != 0){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = buf_c[1]-1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }else if(coordinates[1] != 9)  {
                                        buf_buf_c[0] = buf_c[0];
                                        buf_buf_c[1] = coordinates[1]+1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,3);
                                            sc++;
                                            break;
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[1] != 9)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = coordinates[1]+1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true){
                                        Client.Get_y_battlefield().add_in_used(coordinates);
                                        Client.Get_y_battlefield().add_in_used(buf_c);
                                        Client.Get_y_battlefield().add_in_used(buf_buf_c);

                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                        cell.setStyle("-fx-background-color: blue;");
                                        int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]}};
                                        ships[sc] = new Ship(tmp,3);
                                        sc++;
                                        break;
                                    }
                                }
                            }
                        }
                        if(j +1 == 2){
                            i--;
                        }
                        rand_direction = abs(rand_direction - 1);
                    }


                }else{
                    i--;

                }
            }
        }

        //четырехпалубный корабль
        for(int i =0; i<1; i++){
            coordinates[0] = random.nextInt(10);
            coordinates[1] = random.nextInt(10);
            Node cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);

            cell_color = Client.extractBackgroundColor(cell.getStyle());

            if(cell_color.equals("blue")){
                i--;
            }else{
                if(Client.check_cell(coordinates) == true){

                    int rand_direction = random.nextInt(2); // 0 - по вертикали 1 - по горизонтали
                    for(int j =0; j<2; j++){
                        if(rand_direction == 0){
                            if(coordinates[0] != 9){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0]+1;
                                buf_c[1] = coordinates[1];
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[0] != 9){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0]+1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[0] != 9){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0]+1;
                                        buf_buf_buf_c[1] = buf_buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true && coordinates[0] != 0)  {
                                            buf_buf_buf_c[0] = coordinates[0]-1;
                                            buf_buf_buf_c[1] = buf_buf_c[1];
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && coordinates[0] != 0)  {
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = coordinates[0]-1;
                                        buf_buf_buf_c[1] = buf_buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }else if(coordinates[0]!=0){
                                        buf_buf_c[0] = coordinates[0]-1;
                                        buf_buf_c[1] = coordinates[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[0] != 0){
                                            int[] buf_buf_buf_c = new int[2];
                                            buf_buf_buf_c[0] = buf_buf_c[0]-1;
                                            buf_buf_buf_c[1] = buf_buf_c[1];
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[0] != 0)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = coordinates[0]-1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[0] != 0){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0]-1;
                                        buf_buf_buf_c[1] = buf_buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }
                                }
                            }
                            if(coordinates[0] != 0){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0]-1;
                                buf_c[1] = coordinates[1];
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[0] != 0){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0]-1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[0] != 0){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0]-1;
                                        buf_buf_buf_c[1] = buf_buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true && coordinates[0] != 9)  {
                                            buf_buf_buf_c[0] = coordinates[0]+1;
                                            buf_buf_buf_c[1] = buf_buf_c[1];
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && coordinates[0] != 9)  {
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = coordinates[0]+1;
                                        buf_buf_buf_c[1] = buf_buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }else if(coordinates[0]!=9){
                                        buf_buf_c[0] = coordinates[0]+1;
                                        buf_buf_c[1] = coordinates[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[0] != 9){
                                            int[] buf_buf_buf_c = new int[2];
                                            buf_buf_buf_c[0] = buf_buf_c[0]+1;
                                            buf_buf_buf_c[1] = buf_buf_c[1];
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[0] != 9)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = coordinates[0]+1;
                                    buf_buf_c[1] = buf_c[1];
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[0] != 9){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0]+1;
                                        buf_buf_buf_c[1] = buf_buf_c[1];
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }
                                }
                            }
                        }else{
                            if(coordinates[1] != 9){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0];
                                buf_c[1] = coordinates[1]+1;
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[1] != 9){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = buf_c[1]+1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[1] != 9){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0];
                                        buf_buf_buf_c[1] = buf_buf_c[1]+1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true && coordinates[1] != 0)  {
                                            buf_buf_buf_c[0] = buf_buf_c[0];
                                            buf_buf_buf_c[1] = coordinates[1]-1;
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && coordinates[1] != 0)  {
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0];
                                        buf_buf_buf_c[1] = coordinates[1]-1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }else if(coordinates[1]!=0){
                                        buf_buf_c[0] = coordinates[0];
                                        buf_buf_c[1] = coordinates[1]-1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[1] != 0){
                                            int[] buf_buf_buf_c = new int[2];
                                            buf_buf_buf_c[0] = buf_buf_c[0];
                                            buf_buf_buf_c[1] = buf_buf_c[1]-1;
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[1] != 0)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = coordinates[1]-1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[1] != 0){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0];
                                        buf_buf_buf_c[1] = buf_buf_c[1]-1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }
                                }
                            }
                            if(coordinates[1] != 0){
                                int[] buf_c = new int[2];
                                buf_c[0] = coordinates[0];
                                buf_c[1] = coordinates[1]-1;
                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                cell_color = Client.extractBackgroundColor(cell.getStyle());

                                if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && buf_c[1] != 0){
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = buf_c[1]-1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[1] != 0){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0];
                                        buf_buf_buf_c[1] = buf_buf_c[1]-1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true && coordinates[1] != 9)  {
                                            buf_buf_buf_c[0] = buf_buf_c[0];
                                            buf_buf_buf_c[1] = coordinates[1]+1;
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }else if(cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && coordinates[1] != 9)  {
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0];
                                        buf_buf_buf_c[1] = coordinates[1]+1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true){
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }else if(coordinates[1]!=9){
                                        buf_buf_c[0] = coordinates[0];
                                        buf_buf_c[1] = coordinates[1]+1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[1] != 9){
                                            int[] buf_buf_buf_c = new int[2];
                                            buf_buf_buf_c[0] = buf_buf_c[0];
                                            buf_buf_buf_c[1] = buf_buf_c[1]+1;
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                            cell_color = Client.extractBackgroundColor(cell.getStyle());
                                            if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                                Client.Get_y_battlefield().add_in_used(coordinates);
                                                Client.Get_y_battlefield().add_in_used(buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                                Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                                cell.setStyle("-fx-background-color: blue;");
                                                int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                                ships[sc] = new Ship(tmp,4);
                                                break;
                                            }
                                        }
                                    }
                                } else if(cell_color.equals("blue") == false && Client.check_cell(buf_c) == true && coordinates[1] != 9)  {
                                    int[] buf_buf_c = new int[2];
                                    buf_buf_c[0] = buf_c[0];
                                    buf_buf_c[1] = coordinates[1]+1;
                                    cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                    cell_color = Client.extractBackgroundColor(cell.getStyle());
                                    if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_c) == true && buf_buf_c[1] != 9){
                                        int[] buf_buf_buf_c = new int[2];
                                        buf_buf_buf_c[0] = buf_buf_c[0];
                                        buf_buf_buf_c[1] = buf_buf_c[1]+1;
                                        cell = Client.getNodeByRowColumnIndex(buf_buf_buf_c[0], buf_buf_buf_c[1], gridPane);
                                        cell_color = Client.extractBackgroundColor(cell.getStyle());
                                        if (cell_color.equals("blue") == false && Client.check_cell(buf_buf_buf_c) == true) {
                                            Client.Get_y_battlefield().add_in_used(coordinates);
                                            Client.Get_y_battlefield().add_in_used(buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_c);
                                            Client.Get_y_battlefield().add_in_used(buf_buf_buf_c);

                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(coordinates[0], coordinates[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_c[0], buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            cell = Client.getNodeByRowColumnIndex(buf_buf_c[0], buf_buf_c[1], gridPane);
                                            cell.setStyle("-fx-background-color: blue;");
                                            int[][] tmp = {{coordinates[0], coordinates[1]}, {buf_c[0], buf_c[1]}, {buf_buf_c[0], buf_buf_c[1]},{buf_buf_buf_c[0], buf_buf_buf_c[1]}};
                                            ships[sc] = new Ship(tmp,4);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if(j +1 == 2){
                            i--;
                        }
                        rand_direction = abs(rand_direction - 1);
                    }


                }else{
                    i--;

                }
            }
        }

        Client.Set_yShips(ships);
    }
}

