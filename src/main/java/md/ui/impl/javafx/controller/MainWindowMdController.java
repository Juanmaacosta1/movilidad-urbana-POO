package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import md.logica.Logica;
import md.modelo.Usuario;

public class MainWindowMdController {

	@FXML
	private Label lblTitulo;

	@FXML
	private ListView<String> listaUsuarios;

	@FXML
	private void verUsuarios() {
		listaUsuarios.getItems().clear();
		for (Usuario u : Logica.getInstance().getUsuarios()) {
			listaUsuarios.getItems().add(u.getNombre() + " - " + u.getEmail());
		}
	}
}