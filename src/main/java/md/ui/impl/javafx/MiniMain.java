package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MiniMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/view/MainWindowMd.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Movilidad Digital");
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}   