package sitandwalk;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SitAndWalk extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        
        primaryStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        
        Platform.setImplicitExit(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sit And Walk");
        primaryStage.getIcons().addAll(
            new Image(getClass().getResourceAsStream("sitandwalk-64.png")),
            new Image(getClass().getResourceAsStream("sitandwalk-48.png")),
            new Image(getClass().getResourceAsStream("sitandwalk-32.png")),
            new Image(getClass().getResourceAsStream("sitandwalk-16.png")),
            new Image(getClass().getResourceAsStream("sitandwalk.ico"))
        );
        
        primaryStage.show();
        
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
