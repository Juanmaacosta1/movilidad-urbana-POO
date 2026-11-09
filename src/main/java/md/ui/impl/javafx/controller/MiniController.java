package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javafx.concurrent.Worker;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import md.logica.Logica;
import md.logica.PlanificacionViajeService;
import md.modelo.CategoriaVehiculo;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutasViaje;
import md.rutas.impl.MotorRutas;
import md.ui.impl.javafx.Navegador;

public class MiniController implements Initializable {

    @FXML private ComboBox<Usuario> cmbPasajero;
    @FXML private ComboBox<Usuario> cmbChofer;
    @FXML private VBox seccionPasajero;
    @FXML private VBox seccionConductor;
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblRolActual;
    @FXML private Button btnCambiarRol;
    @FXML private Button btnConfirmarConductor;
    @FXML private Button btnCambiarDisponibilidad;
    @FXML private Button btnElegirOrigen;
    @FXML private Button btnElegirDestino;
    @FXML private WebView mapa;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;
    @FXML private Label lblEstadoMapa;
    @FXML private Label lblResultado;
    @FXML private Label lblResultadoChofer;
    @FXML private Label lblResultadoServicio;
    @FXML private Label lblEstadoConductor;
    @FXML private ComboBox<Servicio> cmbTipoServicio;
    @FXML private ComboBox<Viaje> cmbPedidosPendientes;
    @FXML private ComboBox<TipoServicio> cmbFiltroTipoServicio;
    @FXML private ComboBox<Vehiculo> cmbVehiculoServicio;
    @FXML private ComboBox<CategoriaVehiculo> cmbCategoriaMinima;

    private Ubicacion origen;
    private Ubicacion destino;
    private String tipoDePuntoPendiente;
    private boolean mapaCargado;
    private Stage ventanaAdministracion;
    private Usuario usuarioActual;
    private RolUsuario rolActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarMapa();
        StringConverter<Usuario> conversorUsuario = new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) {
                    return "";
                }
                return usuario.getNombre() + " · " + usuario.getEmail();
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        };
        cmbPasajero.setConverter(conversorUsuario);
        cmbChofer.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) {
                    return "";
                }
                Conductor conductor = usuario.getConductor();
                Vehiculo vehiculo = conductor.getVehiculoActivo();
                return usuario.getNombre() + " · " + nombreEstadoConductor(conductor.getEstadoConductor())
                        + " · " + nombreTipoVehiculo(vehiculo.getTipoVehiculo())
                        + " · " + formatearTiposServicio(vehiculo)
                        + " · acepta " + nombreCategoria(conductor.getCategoriaVehiculoActivo())
                        + " a " + nombreCategoria(vehiculo.getCategoriaVehiculo());
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        });
        cmbPedidosPendientes.setConverter(new StringConverter<>() {
            @Override
            public String toString(Viaje viaje) {
                if (viaje == null) {
                    return "";
                }
                String compatibilidad = esCompatible(viaje, cmbChofer.getValue())
                        ? "Compatible con tu vehículo" : "No compatible";
                return viaje.getCliente().getNombre() + " · " + viaje.getServicio().getNombre()
                        + " · " + nombreTipoServicio(viaje.getServicio().getTipoServicio())
                        + " · " + compatibilidad
                        + " · " + formatearUbicacion("Origen", viaje.getOrigen())
                        + " · " + formatearUbicacion("Destino", viaje.getDestino());
            }

            @Override
            public Viaje fromString(String texto) {
                return null;
            }
        });
        cmbVehiculoServicio.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vehiculo vehiculo) {
                return vehiculo == null ? "" : vehiculo.toString();
            }

            @Override
            public Vehiculo fromString(String texto) {
                return null;
            }
        });
        cmbCategoriaMinima.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoriaVehiculo categoria) {
                return categoria == null ? "" : nombreCategoria(categoria);
            }

            @Override
            public CategoriaVehiculo fromString(String texto) {
                return null;
            }
        });
        cmbFiltroTipoServicio.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoServicio tipoServicio) {
                return tipoServicio == null ? "Todos los servicios" : nombreTipoServicio(tipoServicio);
            }

            @Override
            public TipoServicio fromString(String texto) {
                return null;
            }
        });
        cmbFiltroTipoServicio.getItems().addAll(TipoServicio.values());
        cmbFiltroTipoServicio.setPromptText("Todos los servicios");
        cmbFiltroTipoServicio.setOnAction(evento -> actualizarPedidosPendientes());
        cmbChofer.setOnAction(evento -> {
            actualizarDisponibilidadConductor();
            actualizarPedidosPendientes();
        });
        cmbVehiculoServicio.setOnAction(evento -> {
            actualizarCategoriasMinimas();
            actualizarPedidosPendientes();
        });
        cmbCategoriaMinima.setOnAction(evento -> actualizarPedidosPendientes());
        cmbPedidosPendientes.setOnAction(evento -> actualizarBotonTomarPedido());
        btnCambiarDisponibilidad.setDisable(true);
        btnConfirmarConductor.setDisable(true);
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
            }
            if (cmbPasajero.getItems().isEmpty()) {
                lblResultado.setText("Todavía no hay usuarios registrados.");
            }
            if (cmbChofer.getItems().isEmpty()) {
                lblResultadoChofer.setText("Todavía no hay conductores registrados. Agregalos desde Administración.");
                lblEstadoConductor.setText("No hay conductores registrados.");
            }
            if (cmbTipoServicio.getItems().isEmpty()) {
                lblResultadoServicio.setText("Todavía no hay servicios disponibles.");
            }
        } catch (IllegalStateException e) {
            String mensaje = "No se pudieron cargar los datos: " + e.getMessage();
            lblResultado.setText(mensaje);
            lblResultadoChofer.setText("No se pudieron cargar los conductores.");
            lblResultadoServicio.setText(mensaje);
            e.printStackTrace();
        }
        actualizarBotonTomarPedido();
    }

    private void inicializarMapa() {
        URL recursoMapa = getClass().getResource("/ui/javafx/map/MapaPuertoMadryn.html");
        if (recursoMapa == null) {
            throw new IllegalStateException("No se encontró el recurso del mapa de Puerto Madryn.");
        }

        WebEngine motor = mapa.getEngine();
        motor.setOnAlert(evento -> procesarSeleccionMapa(evento.getData()));
        mapa.widthProperty().addListener((observable, valorAnterior, anchoActual) -> {
            if (mapaCargado && anchoActual.doubleValue() > 0) {
                ajustarMapa();
            }
        });
        mapa.heightProperty().addListener((observable, valorAnterior, altoActual) -> {
            if (mapaCargado && altoActual.doubleValue() > 0) {
                ajustarMapa();
            }
        });
        motor.getLoadWorker().stateProperty().addListener((observable, estadoAnterior, estado) -> {
            if (estado == Worker.State.SUCCEEDED) {
                mapaCargado = true;
                btnElegirOrigen.setDisable(false);
                btnElegirDestino.setDisable(false);
                lblEstadoMapa.setText("Mapa listo. Elegí origen y destino.");
                ajustarMapa();
                PauseTransition espera = new PauseTransition(Duration.millis(500));
                espera.setOnFinished(e -> {
                    ajustarMapa();
                    mostrarDiagnosticoMapa();
                });
                espera.play();
                actualizarMapa();
            } else if (estado == Worker.State.FAILED) {
                lblEstadoMapa.setText("No se pudo cargar el mapa.");
            }
        });
        motor.load(recursoMapa.toExternalForm());
    }

    /** Muestra el tamaño que ve Leaflet frente al del WebView (ayuda a detectar desajustes). */
    private void mostrarDiagnosticoMapa() {
        Platform.runLater(() -> {
            try {
                Object datos = mapa.getEngine().executeScript("window.diagnosticoMapa()");
                lblEstadoMapa.setText("Mapa listo. Elegí origen y destino. [WebView "
                        + (int) mapa.getWidth() + "x" + (int) mapa.getHeight() + " | " + datos + "]");
            } catch (RuntimeException e) {
                lblEstadoMapa.setText("Mapa listo, pero sin diagnóstico: " + e.getMessage());
            }
        });
    }

    /** Pide a Leaflet que recalcule el tamaño del contenedor (en el hilo de JavaFX). */
    private void ajustarMapa() {
        if (!mapaCargado) {
            return;
        }
        Platform.runLater(() -> {
            try {
                mapa.getEngine().executeScript(String.format(java.util.Locale.ROOT,
                        "window.ajustarTamanoMapa(%d, %d)", (int) mapa.getWidth(), (int) mapa.getHeight()));
            } catch (RuntimeException e) {
                lblEstadoMapa.setText("No se pudo ajustar el tamaño del mapa.");
            }
        });
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
    private void abrirAdministracion(ActionEvent evento) {
        if (ventanaAdministracion != null && ventanaAdministracion.isShowing()) {
            ventanaAdministracion.toFront();
            ventanaAdministracion.requestFocus();
            return;
        }

        URL recurso = getClass().getResource("/ui/javafx/view/AdminWindow.fxml");
        if (recurso == null) {
            lblResultado.setText("No se encontró la ventana de administración.");
            return;
        }

        try {
            FXMLLoader cargador = new FXMLLoader(recurso);
            Scene escena = new Scene(cargador.load());
            Stage ventana = new Stage();
            ventana.setTitle("Administración de usuarios y conductores");
            ventana.initOwner(((Node) evento.getSource()).getScene().getWindow());
            ventana.setScene(escena);
            ventana.setOnHidden(cierre -> {
                ventanaAdministracion = null;
                recargarUsuariosYConductores();
            });
            ventanaAdministracion = ventana;
            ventana.show();
        } catch (IOException e) {
            lblResultado.setText("No se pudo abrir la administración: " + e.getMessage());
        }
    }

    @FXML
    private void solicitarViaje(ActionEvent evento) {
        Usuario elegido = cmbPasajero.getValue();
        if (elegido == null) {
            lblResultado.setText("Elegí un pasajero primero.");
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
            Logica logica = Logica.getInstance();
            Viaje viaje = logica.solicitarViaje(elegido, origen, destino, servicioElegido);
            actualizarEstadoPasajero();
            actualizarPedidosPendientes();
            actualizarEstadoPedidoParaConductor(viaje);
            abrirSeguimiento(viaje, RolUsuario.CLIENTE);
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo solicitar el viaje: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarDisponibilidad() {
        Usuario conductor = cmbChofer.getValue();
        if (conductor == null) {
            lblEstadoConductor.setText("Elegí un conductor primero.");
            return;
        }
        try {
            Logica logica = Logica.getInstance();
            if (conductor.getConductor().getEstadoConductor() == EstadoConductor.DISPONIBLE) {
                logica.retirarConductorDeServicio(conductor);
            } else {
                Vehiculo vehiculo = cmbVehiculoServicio.getValue();
                CategoriaVehiculo categoria = cmbCategoriaMinima.getValue();
                if (vehiculo == null || categoria == null) {
                    lblEstadoConductor.setText("Elegí un vehículo y la categoría mínima que aceptás.");
                    return;
                }
                logica.ponerConductorEnServicio(conductor, vehiculo, categoria);
            }
            actualizarDisponibilidadConductor();
            actualizarPedidosPendientes();
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblEstadoConductor.setText("No se pudo cambiar la disponibilidad: " + e.getMessage());
        }
    }

    private void actualizarDisponibilidadConductor() {
        Usuario conductor = cmbChofer.getValue();
        cmbVehiculoServicio.getItems().clear();
        cmbCategoriaMinima.getItems().clear();
        cmbCategoriaMinima.setValue(null);
        if (conductor == null) {
            lblEstadoConductor.setText("Seleccioná el conductor que está usando la aplicación.");
            cmbVehiculoServicio.setDisable(true);
            cmbCategoriaMinima.setDisable(true);
            btnCambiarDisponibilidad.setDisable(true);
            actualizarBotonTomarPedido();
            return;
        }

        for (Vehiculo vehiculo : conductor.getConductor().getVehiculos()) {
            cmbVehiculoServicio.getItems().add(vehiculo);
        }
        cmbVehiculoServicio.setValue(conductor.getConductor().getVehiculoActivo());
        actualizarCategoriasMinimas();

        EstadoConductor estado = conductor.getConductor().getEstadoConductor();
        CategoriaVehiculo categoriaActual = conductor.getConductor().getCategoriaVehiculoActivo();
        if (estado != EstadoConductor.FUERA_DE_SERVICIO
                && cmbCategoriaMinima.getItems().contains(categoriaActual)) {
            cmbCategoriaMinima.setValue(categoriaActual);
        }
        if (estado == EstadoConductor.DISPONIBLE) {
            lblEstadoConductor.setText("Estás disponible para recibir pedidos compatibles.");
            btnCambiarDisponibilidad.setText("Dejar de recibir pedidos");
            btnCambiarDisponibilidad.setDisable(false);
            cmbVehiculoServicio.setDisable(true);
            cmbCategoriaMinima.setDisable(true);
        } else if (estado == EstadoConductor.FUERA_DE_SERVICIO) {
            lblEstadoConductor.setText("Para recibir pedidos, elegí tu vehículo y la categoría mínima que aceptás.");
            btnCambiarDisponibilidad.setText("Empezar a recibir pedidos");
            btnCambiarDisponibilidad.setDisable(false);
            cmbVehiculoServicio.setDisable(false);
            cmbCategoriaMinima.setDisable(false);
        } else {
            lblEstadoConductor.setText("No podés recibir otro pedido mientras estás en un viaje.");
            btnCambiarDisponibilidad.setText("Ocupado con un viaje");
            btnCambiarDisponibilidad.setDisable(true);
            cmbVehiculoServicio.setDisable(true);
            cmbCategoriaMinima.setDisable(true);
        }
        actualizarBotonTomarPedido();
    }

    private void actualizarCategoriasMinimas() {
        Vehiculo vehiculo = cmbVehiculoServicio.getValue();
        CategoriaVehiculo seleccionAnterior = cmbCategoriaMinima.getValue();
        cmbCategoriaMinima.getItems().clear();
        if (vehiculo == null) {
            return;
        }
        for (CategoriaVehiculo categoria : CategoriaVehiculo.values()) {
            if (categoria.getValor() <= vehiculo.getCategoriaVehiculo().getValor()) {
                cmbCategoriaMinima.getItems().add(categoria);
            }
        }
        if (seleccionAnterior != null && cmbCategoriaMinima.getItems().contains(seleccionAnterior)) {
            cmbCategoriaMinima.setValue(seleccionAnterior);
        } else if (!cmbCategoriaMinima.getItems().isEmpty()) {
            cmbCategoriaMinima.setValue(cmbCategoriaMinima.getItems().get(0));
        }
    }

    @FXML
    private void actualizarPedidos() {
        actualizarPedidosPendientes();
    }

    private void actualizarPedidosPendientes() {
        Usuario conductor = cmbChofer.getValue();
        Viaje pedidoSeleccionado = cmbPedidosPendientes.getValue();
        cmbPedidosPendientes.getItems().clear();
        TipoServicio filtro = cmbFiltroTipoServicio.getValue();
        int cantidadDelTipo = 0;
        int cantidadCompatible = 0;
        if (conductor == null) {
            lblResultadoChofer.setText("Elegí tu perfil de chofer para ver los pedidos que podés tomar.");
            actualizarBotonTomarPedido();
            return;
        }
        for (Viaje viaje : Logica.getInstance().getViajes()) {
            if (viaje.estadoActual() != EstadoViaje.SOLICITADO
                    || (filtro != null && viaje.getServicio().getTipoServicio() != filtro)) {
                continue;
            }
            cantidadDelTipo++;
            if (esCompatible(viaje, conductor)) {
                cmbPedidosPendientes.getItems().add(viaje);
                cantidadCompatible++;
            }
        }
        if (cantidadDelTipo == 0) {
            lblResultadoChofer.setText(filtro == null
                    ? "Por ahora no hay pedidos pendientes. Cuando llegue uno, lo vas a ver acá."
                    : "Por ahora no hay pedidos de " + nombreTipoServicio(filtro) + ".");
        } else if (cantidadCompatible == 0) {
            lblResultadoChofer.setText("Hay " + cantidadDelTipo
                    + " pedido(s) " + (filtro == null ? "pendientes" : "de " + nombreTipoServicio(filtro))
                    + ", pero ninguno coincide con tu vehículo, los servicios que ofrecés y la categoría que aceptás.");
        } else if (conductor.getConductor().getEstadoConductor() != EstadoConductor.DISPONIBLE) {
            lblResultadoChofer.setText(cantidadCompatible
                    + (cantidadCompatible == 1 ? " pedido compatible" : " pedidos compatibles")
                    + ". Activá «Empezar a recibir pedidos» para poder tomar uno.");
        } else {
            lblResultadoChofer.setText(cantidadCompatible == 1
                    ? "Hay un pedido que podés tomar. Elegilo y tocá «Tomar pedido»."
                    : "Hay " + cantidadCompatible + " pedidos que podés tomar. Elegí uno y tocá «Tomar pedido».");
        }
        if (pedidoSeleccionado != null && cmbPedidosPendientes.getItems().contains(pedidoSeleccionado)) {
            cmbPedidosPendientes.setValue(pedidoSeleccionado);
        }
        actualizarBotonTomarPedido();
    }

    private void actualizarBotonTomarPedido() {
        Usuario conductor = cmbChofer.getValue();
        Viaje pedido = cmbPedidosPendientes.getValue();
        boolean puedeTomarPedido = conductor != null
                && conductor.getConductor().getEstadoConductor() == EstadoConductor.DISPONIBLE
                && pedido != null
                && esCompatible(pedido, conductor);
        btnConfirmarConductor.setDisable(!puedeTomarPedido);
    }

    private void actualizarEstadoPedidoParaConductor(Viaje viaje) {
        Usuario conductor = cmbChofer.getValue();
        if (conductor == null) {
            lblResultadoChofer.setText("Se agregó un pedido. Los conductores pueden buscarlo en esta sección.");
        } else if (esCompatible(viaje, conductor)) {
            lblResultadoChofer.setText("Hay un nuevo pedido compatible para " + conductor.getNombre() + ".");
        }
    }

    private boolean esCompatible(Viaje viaje, Usuario conductor) {
        for (Usuario compatible : Logica.getInstance().buscarConductoresCompatibles(viaje)) {
            if (compatible == conductor) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void tomarPedido(ActionEvent evento) {
        Usuario conductor = cmbChofer.getValue();
        Viaje pedido = cmbPedidosPendientes.getValue();
        if (conductor == null || pedido == null) {
            lblResultadoChofer.setText("Elegí un conductor y un pedido pendiente.");
            return;
        }
        try {
            Logica.getInstance().aceptarViaje(pedido, conductor);
            abrirSeguimiento(pedido, RolUsuario.CONDUCTOR);
            lblResultadoChofer.setText(conductor.getNombre() + " tomó el pedido de "
                    + pedido.getCliente().getNombre() + ".");
            actualizarEstadoPasajero();
            actualizarDisponibilidadConductor();
            actualizarPedidosPendientes();
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultadoChofer.setText("No se pudo tomar el pedido: " + e.getMessage());
            actualizarPedidosPendientes();
        }
    }

    @FXML
    private void verSeguimientoPasajero() {
        Viaje viaje = viajeMasReciente(RolUsuario.CLIENTE);
        if (viaje == null) {
            lblResultado.setText("Todavía no pediste ningún viaje.");
            return;
        }
        abrirSeguimiento(viaje, RolUsuario.CLIENTE);
    }

    @FXML
    private void verSeguimientoConductor() {
        Viaje viaje = viajeMasReciente(RolUsuario.CONDUCTOR);
        if (viaje == null) {
            lblResultadoChofer.setText("Todavía no tomaste ningún pedido.");
            return;
        }
        abrirSeguimiento(viaje, RolUsuario.CONDUCTOR);
    }

    /** El viaje en curso del usuario actual en ese rol o, si no hay, el último que tuvo. */
    private Viaje viajeMasReciente(RolUsuario rol) {
        Viaje ultimo = null;
        Viaje enCurso = null;
        for (Viaje viaje : Logica.getInstance().getViajes()) {
            Usuario participante = rol == RolUsuario.CLIENTE ? viaje.getCliente() : viaje.getConductor();
            if (participante == null || participante != usuarioActual) {
                continue;
            }
            ultimo = viaje;
            EstadoViaje estado = viaje.estadoActual();
            if (estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO
                    || estado == EstadoViaje.INICIADO) {
                enCurso = viaje;
            }
        }
        return enCurso != null ? enCurso : ultimo;
    }

    private void abrirSeguimiento(Viaje viaje, RolUsuario rol) {
        try {
            SeguimientoPedidoController.abrir(ventanaActual(), viaje, rol, this::dibujarRutasDelViaje);
        } catch (IOException | RuntimeException e) {
            Label destino = rol == RolUsuario.CLIENTE ? lblResultado : lblResultadoChofer;
            destino.setText("No se pudo abrir el seguimiento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Dibuja en el mapa los dos tramos del viaje (conductor -> origen y origen -> destino) calculados
     * por calles. Si el mapa vial todavía no está cargado, queda la ruta de OSRM que ya se muestra.
     */
    private void dibujarRutasDelViaje(Viaje viaje) {
        EstadoViaje estado = viaje.estadoActual();
        if (!mapaCargado || viaje.getConductor() == null
                || estado == EstadoViaje.CANCELADO || estado == EstadoViaje.RECHAZADO) {
            return;
        }
        Optional<PlanificacionViajeService> servicio = MotorRutas.getInstance().servicioPlanificacion();
        if (servicio.isEmpty()) {
            return;
        }
        Conductor datos = viaje.getConductor().getConductor();
        Ubicacion posicion = datos.getUbicacion() != null ? datos.getUbicacion() : viaje.getOrigen();
        try {
            RutasViaje rutas = servicio.get().planificar(posicion, viaje.getOrigen(), viaje.getDestino(),
                    viaje.getServicio().getTipoVehiculo());
            // Terminado el viaje el acercamiento ya no tiene sentido: solo se muestra el traslado
            String acercamiento = estado == EstadoViaje.FINALIZADO ? "[]" : puntosEnJson(rutas.acercamiento());
            String json = "{\"acercamiento\":" + acercamiento
                    + ",\"traslado\":" + puntosEnJson(rutas.traslado())
                    + ",\"conductor\":" + puntoEnJson(posicion.getLatitud(), posicion.getLongitud())
                    + ",\"origen\":" + puntoEnJson(viaje.getOrigen().getLatitud(), viaje.getOrigen().getLongitud())
                    + ",\"destino\":" + puntoEnJson(viaje.getDestino().getLatitud(), viaje.getDestino().getLongitud())
                    + "}";
            mapa.getEngine().executeScript("window.mostrarRutasViaje(" + json + ")");
        } catch (RuntimeException e) {
            // Sin ruta por calles (fuera de cobertura, etc.): queda la que ya estaba dibujada
            System.err.println("No se pudieron dibujar las rutas del viaje: " + e.getMessage());
        }
    }

    private String puntosEnJson(RutaCalculada ruta) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < ruta.geometria().size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(puntoEnJson(ruta.geometria().get(i).latitud(), ruta.geometria().get(i).longitud()));
        }
        return json.append(']').toString();
    }

    private String puntoEnJson(double latitud, double longitud) {
        return String.format(Locale.ROOT, "[%.6f,%.6f]", latitud, longitud);
    }

    private Window ventanaActual() {
        return cmbPasajero.getScene() == null ? null : cmbPasajero.getScene().getWindow();
    }

    private void recargarUsuariosYConductores() {
        if (usuarioActual != null && !contieneUsuario(Logica.getInstance().getUsuarios(), usuarioActual)) {
            cambiarUsuario();
            return;
        }
        Usuario pasajeroSeleccionado = cmbPasajero.getValue();
        Usuario conductorSeleccionado = cmbChofer.getValue();
        cmbPasajero.getItems().clear();
        for (Usuario usuario : Logica.getInstance().getUsuarios()) {
            cmbPasajero.getItems().add(usuario);
        }
        cmbChofer.getItems().clear();
        for (Usuario conductor : Logica.getInstance().getConductores()) {
            cmbChofer.getItems().add(conductor);
        }
        if (contieneUsuario(cmbPasajero.getItems(), pasajeroSeleccionado)) {
            cmbPasajero.setValue(pasajeroSeleccionado);
        }
        if (contieneUsuario(cmbChofer.getItems(), conductorSeleccionado)) {
            cmbChofer.setValue(conductorSeleccionado);
        } else {
            cmbChofer.setValue(null);
        }
        actualizarDisponibilidadConductor();
        actualizarPedidosPendientes();
    }

    @FXML
    private void actualizarEstadoPasajero() {
        Usuario pasajero = cmbPasajero.getValue();
        if (pasajero == null) {
            return;
        }
        for (Viaje viaje : pasajero.getCliente().getViajes()) {
            if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {
                lblResultado.setText("Tu pedido está pendiente; esperando que un conductor compatible lo tome.");
                return;
            }
            if (viaje.estadoActual() == EstadoViaje.ACEPTADO) {
                lblResultado.setText("Tu pedido fue tomado por " + viaje.getConductor().getNombre() + ".");
                return;
            }
            if (viaje.estadoActual() == EstadoViaje.INICIADO) {
                lblResultado.setText("Tu viaje está en curso con " + viaje.getConductor().getNombre() + ".");
                return;
            }
        }
        lblResultado.setText("Elegí un origen, destino y servicio para pedir un viaje.");
    }

    private boolean contieneUsuario(Iterable<Usuario> usuarios, Usuario buscado) {
        if (buscado == null) {
            return false;
        }
        for (Usuario usuario : usuarios) {
            if (usuario == buscado) {
                return true;
            }
        }
        return false;
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

    private String formatearTiposServicio(Vehiculo vehiculo) {
        return vehiculo.getTipoServicios().stream()
                .map(this::nombreTipoServicio)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String nombreTipoServicio(TipoServicio tipoServicio) {
        return switch (tipoServicio) {
            case PASAJEROS -> "Pasajeros";
            case ENVIOS -> "Envíos";
        };
    }

    private String nombreTipoVehiculo(TipoVehiculo tipoVehiculo) {
        return switch (tipoVehiculo) {
            case AUTO -> "Auto";
            case MOTO -> "Moto";
        };
    }

    private String nombreCategoria(CategoriaVehiculo categoria) {
        return switch (categoria) {
            case ESTANDAR -> "Estándar";
            case CONFORT -> "Confort";
            case PREMIUM -> "Premium";
        };
    }

    private String nombreEstadoConductor(EstadoConductor estado) {
        return switch (estado) {
            case FUERA_DE_SERVICIO -> "Fuera de servicio";
            case DISPONIBLE -> "Disponible";
            case VIAJE_A_ORIGEN -> "Yendo al origen";
            case VIAJE_A_DESTINO -> "En viaje";
        };
    }

    /** Lo llama el Navegador al entrar desde la pantalla de selección de usuario. */
    public void iniciarSesion(Usuario usuario, RolUsuario rol) {
        if (usuario == null) {
            throw new IllegalArgumentException("Se necesita un usuario para iniciar la sesión");
        }
        usuarioActual = usuario;
        lblUsuarioActual.setText(usuario.getNombre() + " · " + usuario.getEmail());
        btnCambiarRol.setVisible(usuario.esConductor());
        btnCambiarRol.setManaged(usuario.esConductor());
        if (rol == RolUsuario.CONDUCTOR && usuario.esConductor()) {
            aplicarRolConductor();
        } else {
            aplicarRolCliente();
        }
    }

    private void aplicarRolCliente() {
        rolActual = RolUsuario.CLIENTE;
        lblRolActual.setText("Ingresaste como cliente");
        cmbPasajero.setValue(usuarioActual);
        cmbPasajero.setDisable(true);
        seccionConductor.setVisible(false);
        seccionConductor.setManaged(false);
        seccionPasajero.setVisible(true);
        seccionPasajero.setManaged(true);
        actualizarEstadoPasajero();
    }

    private void aplicarRolConductor() {
        rolActual = RolUsuario.CONDUCTOR;
        lblRolActual.setText("Ingresaste como conductor");
        cmbChofer.setValue(usuarioActual);
        cmbChofer.setDisable(true);
        seccionPasajero.setVisible(false);
        seccionPasajero.setManaged(false);
        seccionConductor.setVisible(true);
        seccionConductor.setManaged(true);
        actualizarDisponibilidadConductor();
        actualizarPedidosPendientes();
    }

    @FXML
    private void cambiarRol() {
        if (usuarioActual == null || !usuarioActual.esConductor()) {
            lblResultado.setText("Este usuario no está registrado como conductor.");
            return;
        }
        if (rolActual == RolUsuario.CLIENTE) {
            aplicarRolConductor();
        } else {
            aplicarRolCliente();
        }
    }

    @FXML
    private void cambiarUsuario() {
        Window ventana = ventanaActual();
        if (!(ventana instanceof Stage)) {
            return;
        }
        try {
            Navegador.mostrarSeleccionUsuario((Stage) ventana);
        } catch (IOException | RuntimeException e) {
            lblResultado.setText("No se pudo volver a la selección de usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
