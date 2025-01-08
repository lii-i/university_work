import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.Random;

public class random_generation {
    @FXML
    Button button;

    public void rand() throws IOException {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Course.fxml"));

        GridPane gridPane = (GridPane) root.lookup("#player_battlefield");
        //задаем расстановку
        Random random = new Random();
        String cell_color;

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
                    Client.Get_y_battlefield().add_in_used(coordinates);
                    cell.setStyle("-fx-background-color: blue;");
                }
            }
        }
    }

}
