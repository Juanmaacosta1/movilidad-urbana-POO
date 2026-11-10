package md.ui.impl.javafx.controller;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import md.logica.Logica;
import md.modelo.CalificacionViaje;
import md.modelo.EstadoViaje;
import md.modelo.Viaje;

public class PedidosController implements Initializable {

    private static final double VELOCIDAD_PROMEDIO_KMH = 30.0;
    private static final double FACTOR_APROXIMACION_CALLES = 1.3;

    @FXML private Label lblPasajero;
    @FXML private Label lblConductor;
    @FXML private Label lblServicio;
    @FXML private Label lblTiempoEspera;
    @FXML private Label lblDistancia;
    @FXML private Label lblCosto;
    @FXML private Label lblEstado;
    @FXML private Label lblResultado;
    @FXML private Label lblCalificacion;
    @FXML private Label lblCalificacionConductor;
    @FXML private Label lblCalificacionPasajero;
    @FXML private Button btnCancelarPedido;

    private Viaje viajeActual;
    private Runnable accionVolver;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblPasajero.setText("");
        lblConductor.setText("");
        lblServicio.setText("");
        lblResultado.setText("");
        lblCalificacion.setText("Las calificaciones aparecen cuando finaliza el viaje.");
        lblCalificacionConductor.setText("Conductor: sin calificar");
        lblCalificacionPasajero.setText("Pasajero: sin calificar");
    }

    public void mostrarPedido(Viaje viaje) {
        if (viaje == null) {
            throw new IllegalArgumentException("Se necesita un pedido para mostrar su estado");
        }
        viajeActual = viaje;
        lblPasajero.setText("Pasajero: " + viaje.getCliente().getNombre());
        lblServicio.setText("Servicio: " + viaje.getServicio().getNombre());
        actualizarEstimaciones(viaje);
        actualizarEstado();
        actualizarCalificaciones();
    }

    public void setAccionVolver(Runnable accionVolver) {
        this.accionVolver = accionVolver;
    }

    @FXML
    private void volverAlPanel() {
        if (accionVolver != null) {
            accionVolver.run();
        }
    }

    private void actualizarEstimaciones(Viaje viaje) {
        double distanciaEnLineaRecta = viaje.getOrigen().calcularDistancia(viaje.getDestino());
        double distanciaEstimadaKm = distanciaEnLineaRecta * FACTOR_APROXIMACION_CALLES;
        double minutosEstimados = distanciaEstimadaKm / VELOCIDAD_PROMEDIO_KMH * 60;
        double costoEstimado = viaje.getServicio().calcularCosto(distanciaEstimadaKm, minutosEstimados);

        lblDistancia.setText(String.format(Locale.forLanguageTag("es-AR"),
                "%.1f km aprox.", distanciaEstimadaKm));
        lblTiempoEspera.setText(formatearDuracion(minutosEstimados));
        lblCosto.setText(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                .format(costoEstimado));
    }

    private String formatearDuracion(double minutos) {
        long minutosRedondeados = Math.max(1, Math.round(minutos));
        if (minutosRedondeados < 60) {
            return minutosRedondeados + " min aprox.";
        }
        long horas = minutosRedondeados / 60;
        long minutosRestantes = minutosRedondeados % 60;
        return minutosRestantes == 0
                ? horas + " h aprox."
                : horas + " h " + minutosRestantes + " min aprox.";
    }

    public void actualizarEstado() {
        if (viajeActual == null) {
            return;
        }
        if (viajeActual.getConductor() == null) {
            lblConductor.setText("Conductor: buscando...");
            lblEstado.setText("Esperando que un conductor tome el pedido.");
        } else {
            lblConductor.setText("Conductor: " + viajeActual.getConductor().getNombre());
            lblEstado.setText("El conductor aceptó el pedido.");
        }
    }

    @FXML
    private void cancelarPedido() {
        if (viajeActual == null) {
            lblResultado.setText("No hay un pedido para cancelar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Querés cancelar este pedido?", ButtonType.CANCEL, ButtonType.OK);
        confirmacion.setHeaderText("Cancelar pedido");
        if (confirmacion.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }

        try {
            Logica.getInstance().cancelarViaje(viajeActual, viajeActual.getCliente(), "Cancelado por el pasajero");
            actualizarEstado();
            lblEstado.setText("Pedido cancelado.");
            lblResultado.setText("Se canceló el pedido.");
            btnCancelarPedido.setDisable(true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("No se pudo cancelar el pedido: " + e.getMessage());
        }
    }


    private void actualizarCalificaciones() {
        if (viajeActual == null) {
            return;
        }
        if (viajeActual.estadoActual() != EstadoViaje.FINALIZADO) {
            lblCalificacion.setText("Las calificaciones aparecen cuando finaliza el viaje.");
            lblCalificacionConductor.setText("Conductor: sin calificar");
            lblCalificacionPasajero.setText("Pasajero: sin calificar");
            return;
        }

        lblCalificacion.setText("Calificaciones del viaje");
        lblCalificacionConductor.setText("Conductor: "
                + formatearCalificacion(viajeActual.getCalificacionConductor()));
        lblCalificacionPasajero.setText("Pasajero: "
                + formatearCalificacion(viajeActual.getCalificacionCliente()));
    }

    private String formatearCalificacion(CalificacionViaje calificacion) {
        if (calificacion == null || calificacion == CalificacionViaje.NO_CALIFICADO) {
            return "sin calificar";
        }
        return calificacion.getValor() + "/5";
    }
}
