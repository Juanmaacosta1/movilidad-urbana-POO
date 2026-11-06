package md.ui.impl.javafx;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

	private static final String VISTA = "/ui/javafx/view/MainWindowMd.fxml";

	@Override
	public void start(Stage stage) throws IOException {
		URL recurso = getClass().getResource(VISTA);
		if (recurso == null) {
			throw new IOException("No se encontró el recurso " + VISTA + " (revisar src/main/resources)");
		}
		Parent root = FXMLLoader.load(recurso);
		stage.setTitle("Movilidad Digital");
		stage.setScene(new Scene(root));
		stage.show();
	}
}