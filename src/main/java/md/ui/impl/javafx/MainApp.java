package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import md.ui.i18n.Messages;

public class MainApp extends Application{
	@Override
	public void start(Stage stage) throws Exception {
		System.out.println(">>> start() ejecutado");
		Parent root = FXMLLoader.load(getClass().getResource("/ui/javafx/view/MainWindowMd.fxml"));
		stage.setTitle("Movilidad Digital");
		stage.setScene(new Scene(root));
		stage.show();
		stage.toFront();
		System.out.println(">>> ventana mostrada");
	}

}
