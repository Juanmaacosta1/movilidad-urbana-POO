package md.ui.impl.javafx.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import md.modelo.CalificacionViaje;

public class CalificarViajeController implements Initializable {

    @FXML private ToggleButton btnEstrella1;
    @FXML private ToggleButton btnEstrella2;
    @FXML private ToggleButton btnEstrella3;
    @FXML private ToggleButton btnEstrella4;
    @FXML private ToggleButton btnEstrella5;
    @FXML private Label lblSeleccion;
    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;
    @FXML private Button btnGuardar;

    private final ToggleButton[] estrellas = new ToggleButton[5];
    private CalificacionViaje seleccion;
    private Stage ventana;
    private String entidadCalificada = "persona";

    public static CalificacionViaje abrir(Window propietario, String titulo, String entidadCalificada) throws IOException {
        URL recurso = CalificarViajeController.class.getResource("/ui/javafx/view/CalificarViaje.fxml");
        if (recurso == null) {
            throw new IOException("No se encontró la pantalla de calificación");
        }

        FXMLLoader cargador = new FXMLLoader(recurso);
        Parent raiz = cargador.load();
        CalificarViajeController controlador = cargador.getController();
        controlador.configurarEntidad(entidadCalificada);

        controlador.ventana = new Stage();
        controlador.ventana.setTitle(titulo == null || titulo.isBlank() ? "Calificar" : titulo);
        controlador.ventana.initModality(Modality.WINDOW_MODAL);
        if (propietario != null) {
            controlador.ventana.initOwner(propietario);
        }
        controlador.ventana.setScene(new Scene(raiz));
        controlador.ventana.setResizable(false);
        System.out.println("[Calificacion] mostrando ventana: " + (titulo == null || titulo.isBlank() ? "Calificar" : titulo));
        controlador.ventana.showAndWait();
        return controlador.seleccion;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        estrellas[0] = btnEstrella1;
        estrellas[1] = btnEstrella2;
        estrellas[2] = btnEstrella3;
        estrellas[3] = btnEstrella4;
        estrellas[4] = btnEstrella5;
        for (int i = 0; i < estrellas.length; i++) {
            final int indice = i;
            estrellas[i].setOnAction(event -> seleccionarEstrellas(indice + 1));
        }
        actualizarTexto();
        btnGuardar.setDisable(true);
    }

    private void configurarEntidad(String entidad) {
        if (entidad == null || entidad.isBlank()) {
            entidad = "persona";
        }
        this.entidadCalificada = entidad;
        if (lblTitulo != null) {
            String nombre = entidad.equalsIgnoreCase("conductor") ? "al conductor" : entidad.equalsIgnoreCase("cliente") ? "al cliente" : "a esta persona";
            lblTitulo.setText("Calificá " + nombre);
        }
        if (lblDescripcion != null) {
            lblDescripcion.setText("Elegí cuántas estrellas te parece " + (entidad.equalsIgnoreCase("conductor") ? "el conductor" : entidad.equalsIgnoreCase("cliente") ? "el cliente" : "esta persona") + ".");
        }
    }

    @FXML
    private void guardarCalificacion() {
        if (seleccion == null) {
            lblSeleccion.setText("Elegí al menos 1 estrella");
            return;
        }
        if (ventana != null) {
            ventana.close();
        }
    }

    @FXML
    private void cancelar() {
        seleccion = null;
        if (ventana != null) {
            ventana.close();
        }
    }

    private void seleccionarEstrellas(int cantidad) {
        if (cantidad < 1 || cantidad > 5) {
            cantidad = 0;
        }
        for (int i = 0; i < estrellas.length; i++) {
            estrellas[i].setSelected(i < cantidad);
        }
        seleccion = switch (cantidad) {
            case 1 -> CalificacionViaje.MALO;
            case 2 -> CalificacionViaje.REGULAR;
            case 3 -> CalificacionViaje.BUENO;
            case 4 -> CalificacionViaje.MUY_BUENO;
            case 5 -> CalificacionViaje.EXCELENTE;
            default -> null;
        };
        btnGuardar.setDisable(seleccion == null);
        actualizarTexto();
    }

    private void actualizarTexto() {
        if (seleccion == null) {
            lblSeleccion.setText("Sin calificación");
            return;
        }
        String textoEstrellas = "★".repeat(seleccion.getValor()) + "☆".repeat(5 - seleccion.getValor());
        lblSeleccion.setText(textoEstrellas + " (" + seleccion.getValor() + "/5)");
    }
}