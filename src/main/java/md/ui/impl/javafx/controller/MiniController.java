package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import javafx.concurrent.Worker;
import java.net.URL;
import java.util.ResourceBundle;

import md.logica.Logica;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;

public class MiniController implements Initializable {

    @FXML private ComboBox<Usuario> cmbPasajero;
    @FXML private ComboBox<Usuario> cmbChofer;
    @FXML private Button btnElegirOrigen;
    @FXML private Button btnElegirDestino;
    @FXML private Button btnAdministracion;
    @FXML private VBox seccionAdministracion;
    @FXML private WebView mapa;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;
    @FXML private Label lblEstadoMapa;
    @FXML private Label lblResultado;
    @FXML private Label lblResultadoChofer;
    @FXML private ComboBox<Usuario> cmbUsuarios;
    @FXML private Label lblResultadoUsuario;
    @FXML private ComboBox<Usuario> cmbConductoresAdmin;
    @FXML private Label lblResultadoConductorAdmin;
    @FXML private Label lblResultadoServicio;
    @FXML private ComboBox<Servicio> cmbTipoServicio;

    private Ubicacion origen;
    private Ubicacion destino;
    private String tipoDePuntoPendiente;
    private boolean mapaCargado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarMapa();
        StringConverter<Usuario> conversorUsuario = new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario == null ? "" : usuario.getNombre() + " · " + usuario.getEmail();
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        };
        cmbPasajero.setConverter(conversorUsuario);
        cmbChofer.setConverter(conversorUsuario);
        cmbUsuarios.setConverter(conversorUsuario);
        cmbConductoresAdmin.setConverter(conversorUsuario);
        cmbTipoServicio.setConverter(new StringConverter<>() {
            @Override
            public String toString(Servicio servicio) {
                return servicio == null ? "" : servicio.getNombre() + " · " + servicio.getTipoServicio();
            }

            @Override
            public Servicio fromString(String texto) {
                return null;
            }
        });

        try {
            Logica logica = Logica.getInstance();
            for (Servicio servicio : logica.getServicios()) {
                cmbTipoServicio.getItems().add(servicio);
            }
            for (Usuario usuario : logica.getUsuarios()) {
                cmbPasajero.getItems().add(usuario);
            }
            for (Usuario conductor : logica.getConductores()) {
                cmbChofer.getItems().add(conductor);
                cmbConductoresAdmin.getItems().add(conductor);
            }
            for (Usuario usuario : logica.getUsuarios()) {
                cmbUsuarios.getItems().add(usuario);
            }
            if (cmbPasajero.getItems().isEmpty()) {
                lblResultado.setText("Todavía no hay usuarios registrados.");
            }
            if (cmbChofer.getItems().isEmpty()) {
                lblResultadoChofer.setText("Todavía no hay conductores registrados.");
            }
            if (cmbUsuarios.getItems().isEmpty()) {
                lblResultadoUsuario.setText("Todavía no hay usuarios para administrar.");
            }
            if (cmbConductoresAdmin.getItems().isEmpty()) {
                lblResultadoConductorAdmin.setText("Todavía no hay conductores para administrar.");
            }
            if (cmbTipoServicio.getItems().isEmpty()) {
                lblResultadoServicio.setText("Todavía no hay servicios disponibles.");
            }
        } catch (IllegalStateException e) {
            String mensaje = "No se pudieron cargar los datos: " + e.getMessage();
            lblResultado.setText(mensaje);
            lblResultadoChofer.setText("No se pudieron cargar los conductores.");
            lblResultadoUsuario.setText(mensaje);
            lblResultadoConductorAdmin.setText(mensaje);
            lblResultadoServicio.setText(mensaje);
            e.printStackTrace();
        }
    }

    private void inicializarMapa() {
        URL recursoMapa = getClass().getResource("/ui/javafx/map/MapaPuertoMadryn.html");
        if (recursoMapa == null) {
            throw new IllegalStateException("No se encontró el recurso del mapa de Puerto Madryn.");
        }

        WebEngine motor = mapa.getEngine();
        motor.setOnAlert(evento -> procesarSeleccionMapa(evento.getData()));
        motor.getLoadWorker().stateProperty().addListener((observable, estadoAnterior, estado) -> {
            if (estado == Worker.State.SUCCEEDED) {
                mapaCargado = true;
                btnElegirOrigen.setDisable(false);
                btnElegirDestino.setDisable(false);
                lblEstadoMapa.setText("Mapa listo. Elegí origen y destino.");
                actualizarMapa();
            } else if (estado == Worker.State.FAILED) {
                lblEstadoMapa.setText("No se pudo cargar el mapa.");
            }
        });
        motor.load(recursoMapa.toExternalForm());
    }

    @FXML
    private void seleccionarOrigen() {
        iniciarSeleccion("origen");
    }

    @FXML
    private void seleccionarDestino() {
        iniciarSeleccion("destino");
    }

    private void iniciarSeleccion(String tipoDePunto) {
        if (!mapaCargado) {
            lblEstadoMapa.setText("El mapa todavía no está disponible.");
            return;
        }
        tipoDePuntoPendiente = tipoDePunto;
        mapa.getEngine().executeScript("window.setModoSeleccion('" + tipoDePunto + "')");
        lblEstadoMapa.setText("Hacé clic en el mapa para elegir el " + tipoDePunto + ".");
    }

    private void procesarSeleccionMapa(String mensaje) {
        if (mensaje == null || !mensaje.startsWith("PUNTO_MAPA:") || tipoDePuntoPendiente == null) {
            return;
        }

        try {
            String[] coordenadas = mensaje.substring("PUNTO_MAPA:".length()).split(",", -1);
            if (coordenadas.length != 2) {
                throw new IllegalArgumentException("El mapa devolvió coordenadas incompletas.");
            }
            Ubicacion seleccion = new Ubicacion(
                    Double.parseDouble(coordenadas[0]),
                    Double.parseDouble(coordenadas[1]));
            if ("origen".equals(tipoDePuntoPendiente)) {
                origen = seleccion;
                lblOrigen.setText(formatearUbicacion("Origen", origen));
            } else {
                destino = seleccion;
                lblDestino.setText(formatearUbicacion("Destino", destino));
            }

            tipoDePuntoPendiente = null;
            mapa.getEngine().executeScript("window.setModoSeleccion(null)");
            lblEstadoMapa.setText("Ubicación seleccionada.");
            actualizarMapa();
        } catch (IllegalArgumentException e) {
            tipoDePuntoPendiente = null;
            mapa.getEngine().executeScript("window.setModoSeleccion(null)");
            lblEstadoMapa.setText("No se pudo seleccionar la ubicación: " + e.getMessage());
        }
    }

    private String formatearUbicacion(String nombre, Ubicacion ubicacion) {
        return String.format(java.util.Locale.ROOT, "%s: %.5f, %.5f",
                nombre, ubicacion.getLatitud(), ubicacion.getLongitud());
    }

    private void actualizarMapa() {
        if (!mapaCargado) {
            return;
        }
        mapa.getEngine().executeScript(String.format(java.util.Locale.ROOT,
                "window.actualizarRuta(%s, %s, %s, %s)",
                coordenada(origen, true), coordenada(origen, false),
                coordenada(destino, true), coordenada(destino, false)));
    }

    private String coordenada(Ubicacion ubicacion, boolean latitud) {
        if (ubicacion == null) {
            return "null";
        }
        return Double.toString(latitud ? ubicacion.getLatitud() : ubicacion.getLongitud());
    }

    @FXML
    private void alternarAdministracion() {
        boolean mostrar = !seccionAdministracion.isVisible();
        seccionAdministracion.setVisible(mostrar);
        seccionAdministracion.setManaged(mostrar);
        btnAdministracion.setText(mostrar
                ? "Cerrar administración"
                : "Administrar usuarios y conductores");
    }

    @FXML
    private void solicitarViaje() {
        Usuario elegido = cmbPasajero.getValue();
        if (elegido == null) {
            lblResultado.setText("Elegí un pasajero primero");
            return;
        }
        if (origen == null || destino == null) {
            lblResultado.setText("Seleccioná el origen y el destino en el mapa.");
            return;
        }
        Servicio servicioElegido = cmbTipoServicio.getValue();
        if (servicioElegido == null) {
            lblResultado.setText("Elegí un tipo de servicio primero.");
            return;
        }

        try {
            Logica.getInstance().solicitarViaje(elegido, origen, destino, servicioElegido);
            lblResultado.setText("Viaje solicitado para " + elegido.getNombre()
                    + " con el servicio " + servicioElegido.getNombre() + ".");
            lblResultadoServicio.setText("Servicio seleccionado: " + servicioElegido.getNombre());
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo solicitar el viaje: " + e.getMessage());
        }
    }
    @FXML
    private void solicitarChofer() {
        Usuario elegido = cmbChofer.getValue();
        if (elegido == null) {
            lblResultadoChofer.setText("Elegí un conductor primero.");
            return;
        }
        lblResultadoChofer.setText("Conductor seleccionado: " + elegido.getNombre());
    }

    @FXML
    private void solicitarUsuario() {
        Usuario elegido = cmbUsuarios.getValue();

        if (elegido == null) {
            lblResultadoUsuario.setText("Elegí un usuario primero");
            return;
        }
        lblResultadoUsuario.setText(
                "Usuario seleccionado: " + elegido.getNombre() + " - " + elegido.getEmail());
    }

    @FXML
    private void solicitarConductorAdmin() {
        Usuario elegido = cmbConductoresAdmin.getValue();
        if (elegido == null) {
            lblResultadoConductorAdmin.setText("Elegí un conductor primero.");
            return;
        }
        lblResultadoConductorAdmin.setText(
                "Conductor seleccionado: " + elegido.getNombre() + " - " + elegido.getEmail());
    }

    @FXML
    private void seleccionarServicio() {
        Servicio elegido = cmbTipoServicio.getValue();
        if (elegido == null) {
            lblResultadoServicio.setText("Elegí un servicio primero.");
            return;
        }
        lblResultadoServicio.setText("Servicio seleccionado: " + elegido.getNombre());
    }
}