package md.ui.impl.javafx.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import md.logica.Logica;
import md.modelo.RegistroViaje;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;

public class AdminController implements Initializable {

    @FXML private ComboBox<Usuario> cmbUsuarios;
    @FXML private ComboBox<Usuario> cmbConductores;
    @FXML private ComboBox<Vehiculo> cmbVehiculoConductor;
    @FXML private Button btnGuardarUsuario;
    @FXML private Button btnGuardarConductor;
    @FXML private VBox formularioUsuario;
    @FXML private VBox formularioConductor;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtTelefonoUsuario;
    @FXML private TextField txtEmailUsuario;
    @FXML private TextField txtNombreConductor;
    @FXML private TextField txtTelefonoConductor;
    @FXML private TextField txtEmailConductor;
    @FXML private TextField txtLicenciaConductor;
    @FXML private Label lblResultadoUsuario;
    @FXML private Label lblResultadoConductor;
    @FXML private Button btnHistorial;
    @FXML private VBox seccionHistorial;
    @FXML private TableView<Viaje> tblHistorial;
    @FXML private TableColumn<Viaje, String> colFecha;
    @FXML private TableColumn<Viaje, String> colOrigen;
    @FXML private TableColumn<Viaje, String> colDestino;
    @FXML private TableColumn<Viaje, String> colServicio;
    @FXML private TableColumn<Viaje, String> colConductor;
    @FXML private Label lblResultadoHistorial;

    private Usuario usuarioEnEdicion;
    private Usuario conductorEnEdicion;
    private boolean creandoUsuario;
    private boolean creandoConductor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        cmbUsuarios.setConverter(conversorUsuario);
        cmbConductores.setConverter(conversorUsuario);
        cmbVehiculoConductor.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vehiculo vehiculo) {
                return vehiculo == null ? "" : vehiculo.toString();
            }

            @Override
            public Vehiculo fromString(String texto) {
                return null;
            }
        });
        configurarHistorial();
        recargarUsuarios(null);
        recargarConductores(null);
        if (cmbUsuarios.getItems().isEmpty()) {
            lblResultadoUsuario.setText("Todavía no hay usuarios para administrar.");
        }
        if (cmbConductores.getItems().isEmpty()) {
            lblResultadoConductor.setText("Todavía no hay conductores para administrar.");
        }
    }

    private void configurarHistorial() {
        colFecha.setCellValueFactory(datos -> {
            Viaje viaje = datos.getValue();
            if (viaje.getRegistroViaje().isEmpty()) {
                return new ReadOnlyStringWrapper("Sin fecha");
            }
            RegistroViaje registro = viaje.getRegistroViaje().get(0);
            return new ReadOnlyStringWrapper(registro.getFechaHora()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });
        colOrigen.setCellValueFactory(datos -> new ReadOnlyStringWrapper(
                formatearUbicacion(datos.getValue().getOrigen())));
        colDestino.setCellValueFactory(datos -> new ReadOnlyStringWrapper(
                formatearUbicacion(datos.getValue().getDestino())));
        colServicio.setCellValueFactory(datos -> new ReadOnlyStringWrapper(
                datos.getValue().getServicio().getNombre()));
        colConductor.setCellValueFactory(datos -> {
            Usuario conductor = datos.getValue().getConductor();
            return new ReadOnlyStringWrapper(conductor == null ? "Sin asignar" : conductor.getNombre());
        });
    }

    @FXML
    private void alternarHistorial() {
        boolean mostrar = !seccionHistorial.isVisible();
        seccionHistorial.setVisible(mostrar);
        seccionHistorial.setManaged(mostrar);
        btnHistorial.setText(mostrar ? "Ocultar historial de viajes" : "Ver historial de viajes");
        if (mostrar) {
            cargarHistorial();
        }
    }

    private void cargarHistorial() {
        tblHistorial.getItems().clear();
        for (Viaje viaje : Logica.getInstance().getViajes()) {
            tblHistorial.getItems().add(viaje);
        }
        lblResultadoHistorial.setText(tblHistorial.getItems().isEmpty()
                ? "Todavía no hay viajes registrados."
                : tblHistorial.getItems().size() + " viaje(s) registrado(s).");
    }

    private String formatearUbicacion(md.modelo.Ubicacion ubicacion) {
        return String.format(java.util.Locale.ROOT, "%.4f, %.4f",
                ubicacion.getLatitud(), ubicacion.getLongitud());
    }

    @FXML
    private void agregarUsuario() {
        usuarioEnEdicion = null;
        creandoUsuario = true;
        txtNombreUsuario.clear();
        txtTelefonoUsuario.clear();
        txtEmailUsuario.clear();
        btnGuardarUsuario.setText("Crear usuario");
        formularioUsuario.setVisible(true);
        formularioUsuario.setManaged(true);
        lblResultadoUsuario.setText("Completá los datos del usuario.");
    }

    @FXML
    private void editarUsuario() {
        usuarioEnEdicion = cmbUsuarios.getValue();
        if (usuarioEnEdicion == null) {
            lblResultadoUsuario.setText("Elegí un usuario primero.");
            return;
        }
        creandoUsuario = false;
        txtNombreUsuario.setText(usuarioEnEdicion.getNombre());
        txtTelefonoUsuario.setText(usuarioEnEdicion.getTelefono());
        txtEmailUsuario.setText(usuarioEnEdicion.getEmail());
        btnGuardarUsuario.setText("Guardar cambios");
        formularioUsuario.setVisible(true);
        formularioUsuario.setManaged(true);
        lblResultadoUsuario.setText("Editá los datos y guardá los cambios.");
    }

    @FXML
    private void guardarUsuario() {
        try {
            if (creandoUsuario) {
                Usuario creado = Logica.getInstance().registrarUsuarioPersistido(
                        txtNombreUsuario.getText(), txtTelefonoUsuario.getText(), txtEmailUsuario.getText());
                recargarUsuarios(creado);
                lblResultadoUsuario.setText("Usuario agregado: " + creado.getNombre() + ".");
            } else {
                if (usuarioEnEdicion == null) {
                    lblResultadoUsuario.setText("Elegí un usuario antes de guardar.");
                    return;
                }
                Usuario actualizado = Logica.getInstance().actualizarUsuario(usuarioEnEdicion,
                        txtNombreUsuario.getText(), txtTelefonoUsuario.getText(),
                        txtEmailUsuario.getText(), null);
                recargarUsuarios(actualizado);
                lblResultadoUsuario.setText("Cambios guardados para " + actualizado.getNombre() + ".");
            }
            formularioUsuario.setVisible(false);
            formularioUsuario.setManaged(false);
            usuarioEnEdicion = null;
            creandoUsuario = false;
        } catch (Exception e) {
            lblResultadoUsuario.setText("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarUsuario() {
        Usuario seleccionado = cmbUsuarios.getValue();
        if (seleccionado == null) {
            lblResultadoUsuario.setText("Elegí un usuario primero.");
            return;
        }
        if (!confirmarEliminacion("usuario", seleccionado.getNombre())) {
            return;
        }
        try {
            Logica.getInstance().eliminarUsuario(seleccionado);
            recargarUsuarios(null);
            recargarConductores(null);
            lblResultadoUsuario.setText("Usuario eliminado: " + seleccionado.getNombre() + ".");
            lblResultadoConductor.setText("La lista de conductores se actualizó.");
        } catch (Exception e) {
            lblResultadoUsuario.setText("No se pudo eliminar el usuario: " + e.getMessage());
        }
    }

    @FXML
    private void agregarConductor() {
        conductorEnEdicion = null;
        creandoConductor = true;
        txtNombreConductor.clear();
        txtTelefonoConductor.clear();
        txtEmailConductor.clear();
        txtLicenciaConductor.clear();
        cargarVehiculos();
        cmbVehiculoConductor.setDisable(false);
        btnGuardarConductor.setText("Crear conductor");
        formularioConductor.setVisible(true);
        formularioConductor.setManaged(true);
        lblResultadoConductor.setText(cmbVehiculoConductor.getItems().isEmpty()
                ? "No hay vehículos registrados. No se puede crear un conductor."
                : "Completá los datos y elegí uno de los vehículos registrados.");
        btnGuardarConductor.setDisable(cmbVehiculoConductor.getItems().isEmpty());
    }

    @FXML
    private void editarConductor() {
        conductorEnEdicion = cmbConductores.getValue();
        if (conductorEnEdicion == null) {
            lblResultadoConductor.setText("Elegí un conductor primero.");
            return;
        }
        creandoConductor = false;
        txtNombreConductor.setText(conductorEnEdicion.getNombre());
        txtTelefonoConductor.setText(conductorEnEdicion.getTelefono());
        txtEmailConductor.setText(conductorEnEdicion.getEmail());
        txtLicenciaConductor.setText(conductorEnEdicion.getConductor().getLicenciaConducir());
        cargarVehiculos();
        cmbVehiculoConductor.setValue(conductorEnEdicion.getConductor().getVehiculoActivo());
        cmbVehiculoConductor.setDisable(true);
        btnGuardarConductor.setDisable(false);
        btnGuardarConductor.setText("Guardar cambios");
        formularioConductor.setVisible(true);
        formularioConductor.setManaged(true);
        lblResultadoConductor.setText("Editá los datos y guardá los cambios.");
    }

    @FXML
    private void guardarConductor() {
        try {
            if (creandoConductor) {
                Vehiculo vehiculo = cmbVehiculoConductor.getValue();
                if (vehiculo == null) {
                    lblResultadoConductor.setText("Elegí un vehículo registrado.");
                    return;
                }
                Usuario creado = Logica.getInstance().registrarConductorPersistido(
                        txtNombreConductor.getText(), txtTelefonoConductor.getText(),
                        txtEmailConductor.getText(), txtLicenciaConductor.getText(), vehiculo);
                recargarUsuarios(cmbUsuarios.getValue());
                recargarConductores(creado);
                lblResultadoConductor.setText("Conductor agregado: " + creado.getNombre() + ".");
            } else {
                if (conductorEnEdicion == null) {
                    lblResultadoConductor.setText("Elegí un conductor antes de guardar.");
                    return;
                }
                Usuario actualizado = Logica.getInstance().actualizarUsuario(conductorEnEdicion,
                        txtNombreConductor.getText(), txtTelefonoConductor.getText(),
                        txtEmailConductor.getText(), txtLicenciaConductor.getText());
                recargarUsuarios(cmbUsuarios.getValue());
                recargarConductores(actualizado);
                lblResultadoConductor.setText("Cambios guardados para " + actualizado.getNombre() + ".");
            }
            formularioConductor.setVisible(false);
            formularioConductor.setManaged(false);
            conductorEnEdicion = null;
            creandoConductor = false;
        } catch (Exception e) {
            lblResultadoConductor.setText("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarConductor() {
        Usuario seleccionado = cmbConductores.getValue();
        if (seleccionado == null) {
            lblResultadoConductor.setText("Elegí un conductor primero.");
            return;
        }
        if (!confirmarEliminacion("conductor", seleccionado.getNombre())) {
            return;
        }
        try {
            Logica.getInstance().eliminarUsuario(seleccionado);
            recargarConductores(null);
            recargarUsuarios(null);
            lblResultadoConductor.setText("Conductor eliminado: " + seleccionado.getNombre() + ".");
            lblResultadoUsuario.setText("La lista de usuarios se actualizó.");
        } catch (Exception e) {
            lblResultadoConductor.setText("No se pudo eliminar el conductor: " + e.getMessage());
        }
    }

    private void cargarVehiculos() {
        cmbVehiculoConductor.getItems().clear();
        Set<String> patentesAgregadas = new HashSet<>();
        for (Vehiculo vehiculo : Logica.getInstance().getVehiculosRegistrados()) {
            if (patentesAgregadas.add(vehiculo.getPatente())) {
                cmbVehiculoConductor.getItems().add(vehiculo);
            }
        }
    }

    private void recargarUsuarios(Usuario seleccion) {
        cmbUsuarios.getItems().clear();
        for (Usuario usuario : Logica.getInstance().getUsuarios()) {
            cmbUsuarios.getItems().add(usuario);
        }
        if (seleccion != null && cmbUsuarios.getItems().contains(seleccion)) {
            cmbUsuarios.setValue(seleccion);
        } else {
            cmbUsuarios.setValue(null);
        }
    }

    private void recargarConductores(Usuario seleccion) {
        cmbConductores.getItems().clear();
        for (Usuario conductor : Logica.getInstance().getConductores()) {
            cmbConductores.getItems().add(conductor);
        }
        if (seleccion != null && cmbConductores.getItems().contains(seleccion)) {
            cmbConductores.setValue(seleccion);
        } else {
            cmbConductores.setValue(null);
        }
    }

    private boolean confirmarEliminacion(String tipo, String nombre) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Querés eliminar el " + tipo + " " + nombre + "?", ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setHeaderText("Confirmar eliminación");
        return confirmacion.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
}
