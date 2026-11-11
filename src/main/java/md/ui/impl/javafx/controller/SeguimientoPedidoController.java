package md.ui.impl.javafx.controller;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import md.logica.Logica;
import md.modelo.CalificacionViaje;
import md.modelo.CategoriaVehiculo;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
import md.modelo.RegistroViaje;
import md.modelo.RolUsuario;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.RutaCalculada;
import md.rutas.impl.MotorRutas;
import md.simulacion.ResultadoSimulacion;
import md.simulacion.SimuladorViaje;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;

/**
 * Ventana de seguimiento de un pedido: estado, progreso, detalles e historial.
 * Se refresca sola cada segundo. Según el rol con el que se abre ofrece cancelar (cliente)
 * o iniciar, finalizar y cancelar (conductor).
 */
public class SeguimientoPedidoController implements Initializable {

    private static final double VELOCIDAD_PROMEDIO_KMH = 30.0;
    private static final double FACTOR_APROXIMACION_CALLES = 1.3;
    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final EstadoViaje[] PASOS = { EstadoViaje.SOLICITADO, EstadoViaje.ACEPTADO,
            EstadoViaje.INICIADO, EstadoViaje.FINALIZADO };

    /** Una sola ventana por pedido y rol: si ya está abierta, se la trae al frente. */
    private static final Map<String, Stage> VENTANAS_ABIERTAS = new HashMap<>();

    @FXML private Label lblEstadoActual;
    @FXML private Label lblCodigo;
    @FXML private Label lblMensajeEstado;
    @FXML private Label lblPaso1;
    @FXML private Label lblPaso2;
    @FXML private Label lblPaso3;
    @FXML private Label lblPaso4;
    @FXML private Label lblTerminal;
    @FXML private Label lblTiempo;
    @FXML private Label lblDistancia;
    @FXML private Label lblCosto;
    @FXML private Label lblPasajero;
    @FXML private Label lblConductor;
    @FXML private Label lblVehiculo;
    @FXML private Label lblServicio;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;
    @FXML private Label lblCalificaciones;
    @FXML private Label lblCancelacion;
    @FXML private Label lblSinAcciones;
    @FXML private Label lblResultado;
    @FXML private ListView<String> lstHistorial;
    @FXML private Button btnIniciar;
    @FXML private Button btnFinalizar;
    @FXML private Button btnCancelar;
    @FXML private VBox cardConductor;
    @FXML private Label lblConductoresInfo;
    @FXML private ComboBox<Usuario> cmbConductor;
    @FXML private Button btnAsignarConductor;
    @FXML private Label lblFuenteRutas;
    @FXML private VBox cardSimulacion;
    @FXML private Button btnSimularCompleto;
    @FXML private Button btnAvanzarPaso;
    @FXML private Button btnNadieAcepta;
    @FXML private Label lblSimulacion;

    private Label[] pasos;
    private Viaje viaje;
    private RolUsuario rol;
    private Timeline refresco;
    private String firmaConductores = "";
    private Consumer<Viaje> alCambiarEstado;
    private EstadoViaje ultimoEstado;
    private RutaCalculada trasladoPorCalles;
    private boolean trasladoFallido;
    private boolean calificacionAbierta;

    /** Abre (o trae al frente) la ventana de seguimiento del viaje para el rol indicado. */
    public static void abrir(Window propietario, Viaje viaje, RolUsuario rol) throws IOException {
        abrir(propietario, viaje, rol, null);
    }

    /**
     * @param alCambiarEstado se avisa cada vez que el viaje cambia de estado (por ejemplo, para
     *                        redibujar el mapa de la ventana principal); puede ser null
     */
    public static void abrir(Window propietario, Viaje viaje, RolUsuario rol, Consumer<Viaje> alCambiarEstado)
            throws IOException {
        if (viaje == null || rol == null) {
            throw new IllegalArgumentException("Se necesita un pedido y un rol para abrir el seguimiento");
        }
        String clave = viaje.getId() + ":" + rol;
        Stage existente = VENTANAS_ABIERTAS.get(clave);
        if (existente != null && existente.isShowing()) {
            existente.toFront();
            existente.requestFocus();
            return;
        }

        URL recurso = SeguimientoPedidoController.class.getResource("/ui/javafx/view/SeguimientoPedido.fxml");
        if (recurso == null) {
            throw new IOException("No se encontró la pantalla de seguimiento del pedido");
        }
        FXMLLoader cargador = new FXMLLoader(recurso);
        Parent raiz = cargador.load();
        SeguimientoPedidoController controlador = cargador.getController();
        controlador.mostrar(viaje, rol, alCambiarEstado);

        Stage ventana = new Stage();
        ventana.setTitle(rol == RolUsuario.CLIENTE ? "Seguimiento de mi pedido" : "Seguimiento del viaje");
        if (propietario != null) {
            ventana.initOwner(propietario);
        }
        ventana.setScene(new Scene(raiz));
        ventana.setOnHidden(cierre -> {
            controlador.detener();
            VENTANAS_ABIERTAS.remove(clave);
        });
        VENTANAS_ABIERTAS.put(clave, ventana);
        ventana.show();
        ventana.toFront();
        ventana.setOnShown(e -> Platform.runLater(() -> {
            if (!controlador.calificacionAbierta && viaje.estadoActual() == EstadoViaje.FINALIZADO) {
                controlador.abrirCalificacionSiHaceFalta(ventana);
            }
        }));
        Platform.runLater(() -> {
            if (!controlador.calificacionAbierta && viaje.estadoActual() == EstadoViaje.FINALIZADO) {
                controlador.abrirCalificacionSiHaceFalta(ventana);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pasos = new Label[] { lblPaso1, lblPaso2, lblPaso3, lblPaso4 };
        lblResultado.setText("");
        cmbConductor.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario == null ? "" : describirConductor(usuario);
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        });
        cmbConductor.valueProperty().addListener(
                (observable, anterior, actual) -> btnAsignarConductor.setDisable(actual == null));
    }

    private void mostrar(Viaje viaje, RolUsuario rol, Consumer<Viaje> alCambiarEstado) {
        this.viaje = viaje;
        this.rol = rol;
        this.alCambiarEstado = alCambiarEstado;
        this.calificacionAbierta = false;
        actualizar();
        if (viaje.estadoActual() == EstadoViaje.FINALIZADO && !calificacionAbierta) {
            Platform.runLater(() -> abrirCalificacionSiHaceFalta(getSceneWindow()));
        }
        refresco = new Timeline(new KeyFrame(Duration.seconds(1), evento -> actualizar()));
        refresco.setCycleCount(Animation.INDEFINITE);
        refresco.play();
    }

    private void abrirCalificacionSiHaceFalta(Window propietario) {
        if (viaje == null || viaje.estadoActual() != EstadoViaje.FINALIZADO || calificacionAbierta) {
            return;
        }

        try {
            Window owner = propietario != null ? propietario : getSceneWindow();

            if (rol == RolUsuario.CLIENTE) {
                calificacionAbierta = true;
                CalificacionViaje calificacion = CalificarViajeController.abrir(
                        owner,
                        "Calificar al conductor",
                        "conductor");
                if (calificacion != null) {
                    viaje.setCalificacionConductor(calificacion);
                    lblResultado.setText("Calificación del conductor guardada: " + estrellas(calificacion));
                } else {
                    lblResultado.setText("Se canceló la calificación del conductor.");
                }
                calificacionAbierta = false;
                actualizar();
                return;
            }

            if (rol == RolUsuario.CONDUCTOR) {
                calificacionAbierta = true;
                CalificacionViaje calificacion = CalificarViajeController.abrir(
                        owner,
                        "Calificar al cliente",
                        "cliente");
                if (calificacion != null) {
                    viaje.setCalificacionCliente(calificacion);
                    lblResultado.setText("Calificación del cliente guardada: " + estrellas(calificacion));
                } else {
                    lblResultado.setText("Se canceló la calificación del cliente.");
                }
                calificacionAbierta = false;
                actualizar();
                return;
            }

            lblResultado.setText("Calificación completa.");
        } catch (IOException e) {
            lblResultado.setText("No se pudo abrir la calificación: " + e.getMessage());
        } finally {
            actualizar();
        }
    }

    private CalificacionViaje pedirCalificacion(String titulo, String tipo) throws IOException {
        Window owner = getSceneWindow();
        return CalificarViajeController.abrir(owner, titulo, tipo);
    }

    private void detener() {
        if (refresco != null) {
            refresco.stop();
        }
    }

    // ------------------------------------------------------------------ actualización de pantalla

    private void actualizar() {
        EstadoViaje estado = viaje.estadoActual();
        lblCodigo.setText("Pedido #" + viaje.getId().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        lblEstadoActual.setText(nombreEstado(estado));
        lblEstadoActual.setStyle(estiloEstado(estado));
        lblMensajeEstado.setText(mensajeEstado(estado));
        actualizarProgreso(estado);
        actualizarEstimaciones();
        actualizarDetalles(estado);
        actualizarHistorial();
        actualizarConductoresElegibles(estado);
        actualizarAcciones(estado);
        avisarCambioDeEstado(estado);
    }

    private void avisarCambioDeEstado(EstadoViaje estado) {
        if (estado == ultimoEstado) {
            return;
        }
        ultimoEstado = estado;
        if (alCambiarEstado != null) {
            try {
                alCambiarEstado.accept(viaje);
            } catch (RuntimeException e) {
                System.err.println("No se pudo actualizar el mapa: " + e.getMessage());
            }
        }
    }

    private void actualizarProgreso(EstadoViaje estado) {
        boolean terminalSinExito = estado == EstadoViaje.CANCELADO || estado == EstadoViaje.RECHAZADO;
        int alcanzado = pasoMasAvanzado();
        for (int i = 0; i < pasos.length; i++) {
            pasos[i].getStyleClass().removeAll("step-actual", "step-done");
            if (estado == EstadoViaje.FINALIZADO || i < alcanzado) {
                pasos[i].getStyleClass().add("step-done");
            } else if (i == alcanzado && !terminalSinExito) {
                pasos[i].getStyleClass().add("step-actual");
            } else if (i == alcanzado) {
                pasos[i].getStyleClass().add("step-done");
            }
        }
        lblTerminal.setVisible(terminalSinExito);
        lblTerminal.setManaged(terminalSinExito);
        if (terminalSinExito) {
            lblTerminal.setText(estado == EstadoViaje.CANCELADO ? "✖ El pedido fue cancelado"
                    : "✖ El pedido fue rechazado");
        }
    }

    /** Índice (en PASOS) del estado más avanzado que alcanzó el viaje según su historial. */
    private int pasoMasAvanzado() {
        int mayor = 0;
        for (RegistroViaje registro : viaje.getRegistroViaje()) {
            for (int i = 0; i < PASOS.length; i++) {
                if (PASOS[i] == registro.getEstadoViaje() && i > mayor) {
                    mayor = i;
                }
            }
        }
        return mayor;
    }

    private void actualizarEstimaciones() {
        RutaCalculada ruta = rutaDelTraslado();
        double kilometros;
        double minutos;
        if (ruta != null) {
            kilometros = ruta.distanciaKm();
            minutos = ruta.duracionMinutos();
            lblFuenteRutas.setText("Valores calculados por calles con el mapa vial. El costo final puede variar.");
        } else {
            kilometros = viaje.getOrigen().calcularDistancia(viaje.getDestino()) * FACTOR_APROXIMACION_CALLES;
            minutos = kilometros / VELOCIDAD_PROMEDIO_KMH * 60;
            lblFuenteRutas.setText("Valores aproximados en línea recta con un ajuste por calles. "
                    + MotorRutas.getInstance().getDetalle());
        }
        double costo = viaje.getServicio().calcularCosto(kilometros, minutos);

        lblDistancia.setText(String.format(ARGENTINA, "%.1f km", kilometros));
        lblTiempo.setText(formatearDuracion(minutos));
        lblCosto.setText(NumberFormat.getCurrencyInstance(ARGENTINA).format(costo));
    }

    /** Ruta por calles origen -> destino, calculada una sola vez cuando el motor está listo (null si no). */
    private RutaCalculada rutaDelTraslado() {
        if (trasladoPorCalles != null || trasladoFallido) {
            return trasladoPorCalles;
        }
        Optional<PlanificadorRutas> planificador = MotorRutas.getInstance().planificador();
        if (planificador.isEmpty()) {
            return null; // todavía no está cargado: se vuelve a intentar en el próximo refresco
        }
        try {
            trasladoPorCalles = planificador.get().calcular(viaje.getOrigen(), viaje.getDestino(),
                    viaje.getServicio().getTipoVehiculo());
        } catch (RuntimeException e) {
            trasladoFallido = true; // sin ruta por calles (fuera de cobertura, etc.): se queda con la estimación
        }
        return trasladoPorCalles;
    }

    private void actualizarDetalles(EstadoViaje estado) {
        Usuario cliente = viaje.getCliente();
        Usuario conductor = viaje.getConductor();
        Vehiculo vehiculo = viaje.getVehiculo();

        lblPasajero.setText(cliente.getNombre() + " · " + cliente.getTelefono());
        lblConductor.setText(conductor == null ? "Buscando conductor..."
                : conductor.getNombre() + " · " + conductor.getTelefono());
        lblVehiculo.setText(vehiculo == null ? "Se asigna cuando un conductor acepta el pedido"
                : vehiculo.getModelo() + " · " + vehiculo.getPatente() + " · "
                        + nombreCategoria(vehiculo.getCategoriaVehiculo()));
        lblServicio.setText(viaje.getServicio().getNombre() + " ("
                + nombreTipoServicio(viaje.getServicio().getTipoServicio()) + ")");
        lblOrigen.setText(formatearUbicacion(viaje.getOrigen().getLatitud(), viaje.getOrigen().getLongitud()));
        lblDestino.setText(formatearUbicacion(viaje.getDestino().getLatitud(), viaje.getDestino().getLongitud()));

        if (estado == EstadoViaje.FINALIZADO) {
            String estrellasCliente = formatearEstrellas(viaje.getCalificacionCliente());
            String estrellasConductor = formatearEstrellas(viaje.getCalificacionConductor());
            lblCalificaciones.setText("Cliente: " + estrellasCliente + "  ·  Conductor: " + estrellasConductor);
        } else {
            lblCalificaciones.setText("Se registran al finalizar el viaje");
        }

        boolean cancelado = estado == EstadoViaje.CANCELADO;
        lblCancelacion.setVisible(cancelado);
        lblCancelacion.setManaged(cancelado);
        if (cancelado) {
            String quien = viaje.getRolCancela() == RolUsuario.CONDUCTOR ? "el conductor" : "el pasajero";
            String texto = "Cancelado por " + quien + ". Motivo: " + viaje.getMotivoCancelacion() + ".";
            if (viaje.getCostoCancelacion() > 0) {
                texto += " Costo de cancelación: "
                        + NumberFormat.getCurrencyInstance(ARGENTINA).format(viaje.getCostoCancelacion()) + ".";
            }
            lblCancelacion.setText(texto);
        }
    }

    private void actualizarHistorial() {
        List<RegistroViaje> registros = viaje.getRegistroViaje();
        if (lstHistorial.getItems().size() == registros.size()) {
            return;
        }
        lstHistorial.getItems().clear();
        for (RegistroViaje registro : registros) {
            lstHistorial.getItems().add(registro.getFechaHora().format(FORMATO_FECHA)
                    + "   " + nombreEstado(registro.getEstadoViaje()));
        }
        lstHistorial.scrollTo(lstHistorial.getItems().size() - 1);
    }

    /**
     * Mientras el pedido espera conductor, el pasajero puede elegir uno entre los compatibles con el
     * servicio que no estén en un viaje, sin tener que ingresar como conductor.
     */
    private void actualizarConductoresElegibles(EstadoViaje estado) {
        boolean mostrar = rol == RolUsuario.CLIENTE && estado == EstadoViaje.SOLICITADO;
        cardConductor.setVisible(mostrar);
        cardConductor.setManaged(mostrar);
        if (!mostrar) {
            firmaConductores = "";
            return;
        }

        List<Usuario> elegibles = new ArrayList<>();
        StringBuilder firma = new StringBuilder();
        for (Usuario candidato : Logica.getInstance().buscarConductoresCompatibles(viaje)) {
            EstadoConductor situacion = candidato.getConductor().getEstadoConductor();
            boolean libre = situacion == EstadoConductor.DISPONIBLE
                    || situacion == EstadoConductor.FUERA_DE_SERVICIO;
            if (candidato != viaje.getCliente() && libre) {
                elegibles.add(candidato);
                firma.append(candidato.getEmail()).append(':').append(situacion).append('|');
            }
        }
        // Solo se vuelve a cargar la lista si cambió: así no se pisa la elección mientras se refresca
        if (firma.toString().equals(firmaConductores)) {
            return;
        }
        firmaConductores = firma.toString();

        Usuario seleccionado = cmbConductor.getValue();
        cmbConductor.setValue(null);
        cmbConductor.getItems().setAll(elegibles);
        if (seleccionado != null && elegibles.contains(seleccionado)) {
            cmbConductor.setValue(seleccionado);
        }
        lblConductoresInfo.setText(elegibles.isEmpty()
                ? "Por ahora no hay conductores compatibles con este servicio."
                : (elegibles.size() == 1 ? "Hay 1 conductor que puede tomar este pedido."
                        : "Hay " + elegibles.size() + " conductores que pueden tomar este pedido.")
                        + " Elegí uno y tocá «Asignar conductor».");
    }

    private String describirConductor(Usuario usuario) {
        Conductor conductor = usuario.getConductor();
        Vehiculo vehiculo = conductor.getVehiculoActivo();
        String situacion = conductor.getEstadoConductor() == EstadoConductor.DISPONIBLE
                ? "Disponible" : "Fuera de servicio (se activa al elegirlo)";
        return usuario.getNombre() + " · " + nombreTipoVehiculo(vehiculo.getTipoVehiculo()) + " "
                + vehiculo.getModelo() + " (" + vehiculo.getPatente() + ") · " + situacion;
    }

    private void actualizarAcciones(EstadoViaje estado) {
        boolean esCliente = rol == RolUsuario.CLIENTE;
        boolean puedeCancelar = estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO
                || (esCliente && estado == EstadoViaje.SOLICITADO);
        boolean puedeIniciar = !esCliente && estado == EstadoViaje.ACEPTADO;
        boolean puedeFinalizar = !esCliente && estado == EstadoViaje.INICIADO;

        mostrarBoton(btnCancelar, puedeCancelar);
        mostrarBoton(btnIniciar, puedeIniciar);
        mostrarBoton(btnFinalizar, puedeFinalizar);
        boolean activo = estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO
                || estado == EstadoViaje.INICIADO;
        cardSimulacion.setVisible(activo);
        cardSimulacion.setManaged(activo);
        mostrarBoton(btnNadieAcepta, estado == EstadoViaje.SOLICITADO);
        boolean sinAcciones = !puedeCancelar && !puedeIniciar && !puedeFinalizar;
        lblSinAcciones.setVisible(sinAcciones);
        lblSinAcciones.setManaged(sinAcciones);
    }

    private void mostrarBoton(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }

    // ------------------------------------------------------------------ acciones

    @FXML
    private void asignarConductor() {
        Usuario elegido = cmbConductor.getValue();
        if (elegido == null) {
            lblResultado.setText("Elegí un conductor primero.");
            return;
        }
        try {
            Logica logica = Logica.getInstance();
            Conductor datos = elegido.getConductor();
            if (datos.getEstadoConductor() == EstadoConductor.FUERA_DE_SERVICIO) {
                // Se lo pone en servicio con el vehículo y la categoría que ya tiene configurados
                CategoriaVehiculo minima = datos.getCategoriaVehiculoActivo() != null
                        ? datos.getCategoriaVehiculoActivo()
                        : datos.getVehiculoActivo().getCategoriaVehiculo();
                logica.ponerConductorEnServicio(elegido, datos.getVehiculoActivo(), minima);
            }
            nuevoSimulador().asegurarPosicion(elegido, viaje.getOrigen());
            logica.aceptarViaje(viaje, elegido);
            lblResultado.setText(elegido.getNombre() + " tomó tu pedido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo asignar el conductor: " + e.getMessage());
        }
        firmaConductores = "";
        actualizar();
    }

    @FXML
    private void iniciarViaje() {
        try {
            Logica.getInstance().iniciarViaje(viaje);
            lblResultado.setText("El viaje comenzó.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo iniciar el viaje: " + e.getMessage());
        }
        actualizar();
    }

    @FXML
    private void finalizarViaje() {
        try {
            CalificacionViaje calificacionConductor = viaje.getCalificacionConductor();
            if (calificacionConductor == null || calificacionConductor == CalificacionViaje.NO_CALIFICADO) {
                calificacionConductor = pedirCalificacion("Calificar al conductor", "conductor");
                if (calificacionConductor == null) {
                    lblResultado.setText("Se canceló la calificación del conductor.");
                    return;
                }
                viaje.setCalificacionConductor(calificacionConductor);
            }

            CalificacionViaje calificacionCliente = viaje.getCalificacionCliente();
            if (calificacionCliente == null || calificacionCliente == CalificacionViaje.NO_CALIFICADO) {
                calificacionCliente = pedirCalificacion("Calificar al cliente", "cliente");
                if (calificacionCliente == null) {
                    lblResultado.setText("Se canceló la calificación del cliente.");
                    return;
                }
                viaje.setCalificacionCliente(calificacionCliente);
            }

            Logica.getInstance().finalizarViaje(viaje, calificacionConductor, calificacionCliente);
            lblResultado.setText("El viaje finalizó y quedó calificado para conductor y pasajero.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo finalizar el viaje: " + e.getMessage());
        } catch (IOException e) {
            lblResultado.setText("No se pudo abrir la pantalla de calificación: " + e.getMessage());
        }
        actualizar();
    }

    private void calificarViaje(String titulo, String tipo) {
        try {
            CalificacionViaje calificacion = pedirCalificacion(titulo, tipo);
            if (calificacion == null) {
                lblResultado.setText("Se canceló la calificación del viaje.");
                return;
            }
            if ("conductor".equals(tipo)) {
                viaje.setCalificacionConductor(calificacion);
            } else {
                viaje.setCalificacionCliente(calificacion);
            }
            lblResultado.setText("Se guardó la calificación de " + tipo + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo guardar la calificación: " + e.getMessage());
        } catch (IOException e) {
            lblResultado.setText("No se pudo abrir la pantalla de calificación: " + e.getMessage());
        }
    }

    @FXML
    private void cancelarPedido() {
        boolean esCliente = rol == RolUsuario.CLIENTE;
        Usuario quienCancela = esCliente ? viaje.getCliente() : viaje.getConductor();
        String motivoInicial = esCliente ? "Cancelado por el pasajero" : "Cancelado por el conductor";
        LocalDateTime fecha = fechaDeCancelacion();

        try {
            if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {
                // Nadie lo aceptó todavía: cancelar no genera ningún costo
                TextInputDialog dialogo = new TextInputDialog(motivoInicial);
                dialogo.setTitle("Cancelar pedido");
                dialogo.setHeaderText("¿Querés cancelar este pedido? No genera ningún costo.");
                dialogo.setContentText("Motivo:");
                dialogo.initOwner(btnCancelar.getScene().getWindow());
                Optional<String> motivo = dialogo.showAndWait();
                if (motivo.isEmpty()) {
                    return;
                }
                Logica.getInstance().cancelarViaje(viaje, quienCancela, motivo.get(), fecha);
                lblResultado.setText("Se canceló el pedido.");
            } else {
                cancelarConCosto(quienCancela, motivoInicial, fecha);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo cancelar el pedido: " + e.getMessage());
        }
        actualizar();
    }

    /** Ya aceptado: el costo es proporcional a lo que recorrió el conductor, que se estima y se puede corregir. */
    private void cancelarConCosto(Usuario quienCancela, String motivoInicial, LocalDateTime fecha) {
        double kmEstimados = nuevoSimulador().kmRecorridosEstimados(viaje);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Cancelar pedido");
        dialogo.setHeaderText("El conductor ya aceptó el pedido: se cobra un costo proporcional a lo recorrido.");
        dialogo.initOwner(btnCancelar.getScene().getWindow());
        TextField txtMotivo = new TextField(motivoInicial);
        TextField txtKm = new TextField(String.format(Locale.ROOT, "%.1f", kmEstimados));
        GridPane grilla = new GridPane();
        grilla.setHgap(12);
        grilla.setVgap(10);
        grilla.add(new Label("Motivo"), 0, 0);
        grilla.add(txtMotivo, 1, 0);
        grilla.add(new Label("Km recorridos por el conductor"), 0, 1);
        grilla.add(txtKm, 1, 1);
        dialogo.getDialogPane().setContent(grilla);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }
        double km;
        try {
            km = Double.parseDouble(txtKm.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            lblResultado.setText("Los kilómetros tienen que ser un número (por ejemplo 2.5).");
            return;
        }
        Logica.getInstance().cancelarViaje(viaje, quienCancela, txtMotivo.getText(), km, fecha);
        lblResultado.setText("Se canceló el pedido.");
    }

    /** La hora real, salvo que la simulación ya haya avanzado más: el historial no puede retroceder. */
    private LocalDateTime fechaDeCancelacion() {
        List<RegistroViaje> registros = viaje.getRegistroViaje();
        LocalDateTime ultimo = registros.get(registros.size() - 1).getFechaHora();
        LocalDateTime ahora = LocalDateTime.now();
        return ultimo.isAfter(ahora) ? ultimo.plusSeconds(1) : ahora;
    }

    // ------------------------------------------------------------------ simulación (sin esperar)

    private SimuladorViaje nuevoSimulador() {
        return new SimuladorViaje(Logica.getInstance(), MotorRutas.getInstance().planificador().orElse(null));
    }

    @FXML
    private void simularViajeCompleto() {
        ejecutarSimulacion(() -> nuevoSimulador().simularCompleto(viaje));
    }

    @FXML
    private void avanzarUnPaso() {
        ejecutarSimulacion(() -> nuevoSimulador().avanzarUnPaso(viaje));
    }

    @FXML
    private void simularNadieAcepta() {
        ejecutarSimulacion(() -> nuevoSimulador().simularRechazoPorFaltaDeConductor(viaje));
    }

    private void ejecutarSimulacion(java.util.function.Supplier<ResultadoSimulacion> simulacion) {
        try {
            ResultadoSimulacion resultado = simulacion.get();
            lblSimulacion.setText(String.join("\n", resultado.pasos()));
            lblResultado.setText("");
            if (viaje.estadoActual() == EstadoViaje.FINALIZADO) {
                Platform.runLater(() -> abrirCalificacionSiHaceFalta(getSceneWindow()));
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblSimulacion.setText("No se pudo simular: " + e.getMessage());
        }
        firmaConductores = "";
        actualizar();
    }

    private Window getSceneWindow() {
        return btnFinalizar != null && btnFinalizar.getScene() != null ? btnFinalizar.getScene().getWindow() : null;
    }

    private ComboBox<CalificacionViaje> crearComboCalificacion() {
        ComboBox<CalificacionViaje> combo = new ComboBox<>();
        for (CalificacionViaje calificacion : CalificacionViaje.values()) {
            if (calificacion != CalificacionViaje.NO_CALIFICADO) {
                combo.getItems().add(calificacion);
            }
        }
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CalificacionViaje calificacion) {
                if (calificacion == null) {
                    return "";
                }
                int valor = calificacion.getValor();
                return "★".repeat(valor) + "☆".repeat(5 - valor) + " (" + valor + ")";
            }

            @Override
            public CalificacionViaje fromString(String texto) {
                return null;
            }
        });
        combo.setValue(CalificacionViaje.EXCELENTE);
        return combo;
    }

    private String estrellas(CalificacionViaje calificacion) {
        if (calificacion == null || calificacion == CalificacionViaje.NO_CALIFICADO) {
            return "Sin calificar";
        }
        return "★".repeat(calificacion.getValor()) + "☆".repeat(5 - calificacion.getValor());
    }

    private String formatearEstrellas(CalificacionViaje calificacion) {
        return estrellas(calificacion);
    }

    // ------------------------------------------------------------------ textos

    private String mensajeEstado(EstadoViaje estado) {
        boolean esCliente = rol == RolUsuario.CLIENTE;
        Usuario conductor = viaje.getConductor();
        String nombreConductor = conductor == null ? "El conductor" : conductor.getNombre();
        return switch (estado) {
            case SOLICITADO -> esCliente
                    ? "Elegí un conductor de la lista o esperá a que alguno tome el pedido. Esta pantalla se actualiza sola."
                    : "El pedido todavía no tiene conductor asignado.";
            case ACEPTADO -> esCliente
                    ? nombreConductor + " aceptó tu pedido y va hacia el punto de origen."
                    : "Andá hasta el origen y tocá «Iniciar viaje» cuando el pasajero suba.";
            case INICIADO -> esCliente
                    ? "Tu viaje está en curso con " + nombreConductor + "."
                    : "Viaje en curso. Tocá «Finalizar viaje» al llegar al destino.";
            case FINALIZADO -> "El viaje finalizó. ¡Gracias por usar Movilidad Digital!";
            case CANCELADO -> "Este pedido fue cancelado.";
            case RECHAZADO -> "Este pedido fue rechazado.";
        };
    }

    private String nombreEstado(EstadoViaje estado) {
        return switch (estado) {
            case SOLICITADO -> "Solicitado";
            case ACEPTADO -> "Aceptado";
            case INICIADO -> "En viaje";
            case FINALIZADO -> "Finalizado";
            case CANCELADO -> "Cancelado";
            case RECHAZADO -> "Rechazado";
        };
    }

    private String estiloEstado(EstadoViaje estado) {
        String colores = switch (estado) {
            case SOLICITADO -> "#fef3c7; -fx-text-fill: #92400e";
            case ACEPTADO -> "#dbeafe; -fx-text-fill: #1e40af";
            case INICIADO -> "#e0e7ff; -fx-text-fill: #3730a3";
            case FINALIZADO -> "#dcfce7; -fx-text-fill: #166534";
            case CANCELADO, RECHAZADO -> "#fee2e2; -fx-text-fill: #991b1b";
        };
        return "-fx-background-color: " + colores + ";";
    }

    private String nombreCategoria(CategoriaVehiculo categoria) {
        return switch (categoria) {
            case ESTANDAR -> "Estándar";
            case CONFORT -> "Confort";
            case PREMIUM -> "Premium";
        };
    }

    private String nombreTipoVehiculo(TipoVehiculo tipoVehiculo) {
        return switch (tipoVehiculo) {
            case AUTO -> "Auto";
            case MOTO -> "Moto";
        };
    }

    private String nombreTipoServicio(TipoServicio tipoServicio) {
        return switch (tipoServicio) {
            case PASAJEROS -> "Pasajeros";
            case ENVIOS -> "Envíos";
        };
    }

    private String formatearUbicacion(double latitud, double longitud) {
        return String.format(Locale.ROOT, "%.5f, %.5f", latitud, longitud);
    }

    private String formatearDuracion(double minutos) {
        long redondeados = Math.max(1, Math.round(minutos));
        if (redondeados < 60) {
            return redondeados + " min";
        }
        long horas = redondeados / 60;
        long resto = redondeados % 60;
        return resto == 0 ? horas + " h" : horas + " h " + resto + " min";
    }
}

















