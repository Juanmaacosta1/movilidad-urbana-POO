package md.ui.impl.javafx.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Locale;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import md.logica.Logica;
import md.modelo.RolUsuario;
import md.modelo.Usuario;
import md.ui.impl.javafx.Navegador;
import md.ui.i18n.Messages;

/** Pantalla inicial: se elige el usuario y el rol con el que se entra a la aplicación. */
public class SeleccionUsuarioController implements Initializable {

    @FXML private ComboBox<Usuario> cmbUsuario;
    @FXML private ComboBox<String> cmbIdioma;
    @FXML private Label lblDetalle;
    @FXML private Label lblMensaje;
    @FXML private Label lblRoles;
    @FXML private Button btnCliente;
    @FXML private Button btnConductor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (cmbIdioma != null) {
            cmbIdioma.getItems().setAll("Español", "English");
            cmbIdioma.setValue(Messages.getLocale().getLanguage().equals("en") ? "English" : "Español");
        }
        cmbUsuario.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario == null ? "" : usuario.getNombre() + " · " + usuario.getEmail();
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        });

        lblMensaje.setText(md.ui.i18n.Messages.get("ui.loading"));
        cmbUsuario.setDisable(true);

        Task<net.datastructures.List<Usuario>> tareaUsuarios = new Task<>() {
            @Override
            protected net.datastructures.List<Usuario> call() {
                return Logica.getInstance().getUsuarios();
            }
        };

        tareaUsuarios.setOnSucceeded(event -> {
            cmbUsuario.getItems().clear();
            net.datastructures.List<Usuario> usuarios = tareaUsuarios.getValue();
            for (Usuario usuario : usuarios) {
                cmbUsuario.getItems().add(usuario);
            }
            cmbUsuario.setDisable(false);
            if (cmbUsuario.getItems().isEmpty()) {
                lblMensaje.setText(md.ui.i18n.Messages.get("ui.no.users"));
            } else {
                lblMensaje.setText("");
            }
            usuarioElegido();
        });

        tareaUsuarios.setOnFailed(event -> {
            cmbUsuario.setDisable(false);
            Throwable error = tareaUsuarios.getException();
            lblMensaje.setText(md.ui.i18n.Messages.get("ui.users.error") + (error != null ? error.getMessage() : md.ui.i18n.Messages.get("ui.unknown.error")));
        });

        Thread hiloUsuarios = new Thread(tareaUsuarios, "carga-usuarios");
        hiloUsuarios.setDaemon(true);
        hiloUsuarios.start();
    }

    @FXML
    private void cambiarIdioma() {
        if (cmbIdioma == null || cmbIdioma.getValue() == null) return;
        Messages.setLocale("English".equals(cmbIdioma.getValue())
                ? Locale.ENGLISH : new Locale("es"));
        try {
            Stage stage = (Stage) cmbIdioma.getScene().getWindow();
            Navegador.mostrarSeleccionUsuario(stage);
        } catch (IOException e) {
            lblMensaje.setText(md.ui.i18n.Messages.get("ui.language.error") + e.getMessage());
        }
    }

    @FXML
    private void usuarioElegido() {
        Usuario usuario = cmbUsuario.getValue();
        lblMensaje.setText("");
        btnCliente.setDisable(usuario == null);
        btnConductor.setDisable(usuario == null || !usuario.esConductor());

        if (usuario == null) {
            lblRoles.setText(md.ui.i18n.Messages.get("ui.roles.client"));
            lblDetalle.setText(md.ui.i18n.Messages.get("ui.select.help"));
            return;
        }

        String roles = "CLIENTE";
        if (usuario.esConductor()) {
            roles += ", CONDUCTOR";
        }
        lblRoles.setText("Roles disponibles: " + roles);

        StringBuilder detalle = new StringBuilder("Teléfono: " + usuario.getTelefono() + ". ");
        detalle.append(usuario.esConductor()
                ? "Está registrado como conductor: puede ingresar como cliente o como conductor."
                : "Solo puede ingresar como cliente (no está registrado como conductor).");
        if (usuario.getCliente().enViaje()) {
            detalle.append(" Tiene un pedido en curso.");
        }
        lblDetalle.setText(detalle.toString());
    }

    @FXML
    private void entrarComoCliente() {
        entrar(RolUsuario.CLIENTE);
    }

    @FXML
    private void entrarComoConductor() {
        entrar(RolUsuario.CONDUCTOR);
    }

    private void entrar(RolUsuario rol) {
        Usuario usuario = cmbUsuario.getValue();
        if (usuario == null) {
            lblMensaje.setText("Elegí un usuario primero.");
            return;
        }
        Window ventana = cmbUsuario.getScene().getWindow();
        try {
            Navegador.mostrarPrincipal((Stage) ventana, usuario, rol);
        } catch (IOException | RuntimeException e) {
            lblMensaje.setText("No se pudo abrir la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}