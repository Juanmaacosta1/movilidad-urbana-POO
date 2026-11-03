package md.ui.impl.javafx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import md.logica.Logica;
import md.modelo.CalificacionViaje;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;

public class MainWindowMdController {

	@FXML
	private Label lblTitulo;
	@FXML
	private Label lblEstado;

	@FXML
	private TableView<String[]> tablaUsuarios;
	@FXML
	private TableView<String[]> tablaServicios;
	@FXML
	private TableView<String[]> tablaVehiculos;
	@FXML
	private TableView<String[]> tablaViajes;
	@FXML
	private ListView<String> listaConductores;

	@FXML
	private TextField txtEmail;
	@FXML
	private TextField txtPatente;
	@FXML
	private TextArea txtResultado;

	/** Se ejecuta solo al cargar el FXML. */
	@FXML
	private void initialize() {
		configurar(tablaUsuarios, "Nombre", "Teléfono", "Email", "Rol");
		configurar(tablaServicios, "Nombre", "Tarifa base", "Precio/km", "Precio/min", "Vehículo", "Categoría",
				"Tipo");
		configurar(tablaVehiculos, "Patente", "Modelo", "Capacidad", "Tipo", "Categoría", "Servicios");
		configurar(tablaViajes, "ID", "Cliente", "Servicio", "Estado");
		configurarViaje();
		refrescar();
	}

	private void configurar(TableView<String[]> tabla, String... titulos) {
		tabla.getColumns().clear();
		for (int i = 0; i < titulos.length; i++) {
			final int col = i;
			TableColumn<String[], String> c = new TableColumn<>(titulos[i]);
			c.setCellValueFactory(f -> new SimpleStringProperty(f.getValue()[col]));
			tabla.getColumns().add(c);
		}
		tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
	}

	@FXML
	private void refrescar() {
		try {
			Logica logica = Logica.getInstance();

			tablaUsuarios.getItems().clear();
			for (Usuario u : logica.getUsuarios()) {
				tablaUsuarios.getItems().add(new String[] { u.getNombre(), u.getTelefono(), u.getEmail(),
						u.esConductor() ? "Cliente y conductor" : "Cliente" });
			}

			tablaServicios.getItems().clear();
			for (Servicio s : logica.getServicios()) {
				tablaServicios.getItems()
						.add(new String[] { s.getNombre(), "$" + s.getTarifaBase(), "$" + s.getPrecioKm(),
								"$" + s.getPrecioMinuto(), String.valueOf(s.getTipoVehiculo()),
								String.valueOf(s.getCategoriaVehiculo()), String.valueOf(s.getTipoServicio()) });
			}

			tablaVehiculos.getItems().clear();
			for (Vehiculo v : logica.getVehiculos()) {
				tablaVehiculos.getItems()
						.add(new String[] { v.getPatente(), v.getModelo(), String.valueOf(v.getCapacidadPasajeros()),
								String.valueOf(v.getTipoVehiculo()), String.valueOf(v.getCategoriaVehiculo()),
								String.valueOf(v.getTipoServicios()) });
			}

			tablaViajes.getItems().clear();
			for (Viaje v : logica.getViajes()) {
				tablaViajes.getItems().add(new String[] { String.valueOf(v.getId()), v.getCliente().getNombre(),
						v.getServicio().getNombre(), String.valueOf(v.estadoActual()) });
			}

			listaConductores.setItems(FXCollections.observableArrayList());
			for (Usuario u : logica.getConductoresEnServicio()) {
				listaConductores.getItems().add(u.getNombre() + " | " + u.getEmail() + " | Estado: "
						+ u.getConductor().getEstadoConductor());
			}

			refrescarViaje(logica);

			lblEstado.setText("Datos cargados correctamente.");
		} catch (Exception e) {
			Throwable causa = e.getCause() != null ? e.getCause() : e;
			lblEstado.setText("Error al cargar los datos: " + causa.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void buscarUsuario() {
		String email = txtEmail.getText().trim();
		if (email.isEmpty()) {
			txtResultado.setText("Ingrese un email.");
			return;
		}

		Logica logica = Logica.getInstance();
		Usuario usuario = logica.buscarUsuario(email);

		if (usuario == null) {
			txtResultado.setText("No existe un usuario con ese email.");
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("===== DATOS DEL USUARIO =====\n");
		sb.append("Nombre: ").append(usuario.getNombre()).append('\n');
		sb.append("Teléfono: ").append(usuario.getTelefono()).append('\n');
		sb.append("Email: ").append(usuario.getEmail()).append('\n');
		sb.append("Rol activo: ").append(usuario.getRolActivo()).append('\n');

		if (usuario.esConductor()) {
			Conductor conductor = usuario.getConductor();
			sb.append("\n----- Conductor -----\n");
			sb.append("Licencia: ").append(conductor.getLicenciaConducir()).append('\n');
			sb.append("Estado: ").append(conductor.getEstadoConductor()).append('\n');
			sb.append("Viajes finalizados: ").append(logica.cantidadDeViajesFinalizados(usuario)).append('\n');
			sb.append(String.format("Promedio de calificaciones: %.2f / 5%n",
					logica.calcularPromedioEstrellas(usuario)));
			sb.append("Vehículos:\n");
			for (Vehiculo v : conductor.getVehiculos()) {
				sb.append("  ").append(v).append('\n');
			}
		}

		txtResultado.setText(sb.toString());
	}

	@FXML
	private void buscarVehiculo() {
		String patente = txtPatente.getText().trim();
		if (patente.isEmpty()) {
			txtResultado.setText("Ingrese una patente.");
			return;
		}

		Logica logica = Logica.getInstance();
		Vehiculo encontrado = null;
		for (Vehiculo v : logica.getVehiculos()) {
			if (v.getPatente().equalsIgnoreCase(patente)) {
				encontrado = v;
				break;
			}
		}

		if (encontrado == null) {
			txtResultado.setText("No se encontró un vehículo con esa patente.");
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("===== VEHÍCULO ENCONTRADO =====\n");
		sb.append("Patente: ").append(encontrado.getPatente()).append('\n');
		sb.append("Modelo: ").append(encontrado.getModelo()).append('\n');
		sb.append("Capacidad: ").append(encontrado.getCapacidadPasajeros()).append('\n');
		sb.append("Tipo: ").append(encontrado.getTipoVehiculo()).append('\n');
		sb.append("Categoría: ").append(encontrado.getCategoriaVehiculo()).append('\n');
		sb.append("Servicios: ").append(encontrado.getTipoServicios()).append('\n');

		String conductorTexto = "No se encontró un conductor asociado a este vehículo.";
		for (Usuario u : logica.getConductores()) {
			for (Vehiculo v : u.getConductor().getVehiculos()) {
				if (v.getPatente().equalsIgnoreCase(patente)) {
					conductorTexto = "Conductor registrado: " + u.getNombre() + " (" + u.getEmail() + ")";
				}
			}
		}
		sb.append('\n').append(conductorTexto).append('\n');

		txtResultado.setText(sb.toString());
	}

	// ============================================================================
//  AGREGAR a MainWindowMdController.java  (no borres lo que ya tenés)
//  Imports nuevos:
//    import javafx.scene.control.Button;
//    import javafx.scene.control.ComboBox;
//    import javafx.util.StringConverter;
//    import md.modelo.CalificacionViaje;
//    import md.modelo.EstadoConductor;
//    import md.modelo.EstadoViaje;
//    import md.modelo.Ubicacion;
// ============================================================================

	// ---- Pasajero ----
	@FXML private ComboBox<Usuario> cmbPasajero;
	@FXML private ComboBox<Servicio> cmbServicio;
	@FXML private TextField txtOrigenLat, txtOrigenLng, txtDestinoLat, txtDestinoLng;
	@FXML private Button btnSolicitar;
	@FXML private Label lblViajePasajero;
	@FXML private WebView mapView;
	@FXML private Label lblMapaEstado, lblMapaConductor;

	// ---- Conductor ----
	@FXML private ComboBox<Usuario> cmbConductor;
	@FXML private Label lblEstadoConductor, lblCalifConductor, lblViajesConductor,
			lblVehiculoConductor, lblViajeConductor;
	@FXML private Button btnServicio, btnAceptar, btnIniciar, btnFinalizar;

	// ---- Línea de vida ----
	@FXML private Label stepSolicitado, stepAceptado, stepIniciado, stepFinalizado;

	/** Viaje que se está mostrando en las dos tarjetas. */
	private Viaje viajeActual;
	private boolean mapaListo;

	/** Llamalo al final de initialize(), antes de refrescar(). */
	private void configurarViaje() {
		cmbPasajero.setConverter(new StringConverter<>() {
			@Override public String toString(Usuario u) { return u == null ? "" : u.getNombre() + " · " + u.getEmail(); }
			@Override public Usuario fromString(String s) { return null; }
		});
		cmbConductor.setConverter(cmbPasajero.getConverter());
		cmbServicio.setConverter(new StringConverter<>() {
			@Override public String toString(Servicio s) { return s == null ? "" : s.getNombre(); }
			@Override public Servicio fromString(String s) { return null; }
		});
		// Al cambiar de conductor se repinta su tarjeta
		cmbConductor.valueProperty().addListener((o, a, b) -> {
			actualizarTarjetaConductor();
			actualizarMapa();
		});
		mapView.getEngine().getLoadWorker().stateProperty().addListener((observable, anterior, estado) -> {
			if (estado == Worker.State.SUCCEEDED) {
				if ("function".equals(mapView.getEngine().executeScript("typeof window.actualizarRuta"))) {
					mapaListo = true;
					actualizarMapa();
				} else {
					lblMapaEstado.setText("No se pudo cargar el mapa. Revisá la conexión a internet.");
				}
			} else if (estado == Worker.State.FAILED) {
				mapaListo = false;
				lblMapaEstado.setText("No se pudo cargar el mapa. Revisá la conexión a internet.");
			}
		});
		var recursoMapa = getClass().getResource("/ui/javafx/map/MapaPuertoMadryn.html");
		if (recursoMapa == null) {
			lblMapaEstado.setText("No se encontró el recurso del mapa de Puerto Madryn.");
		} else {
			mapView.getEngine().load(recursoMapa.toExternalForm());
		}
		txtOrigenLat.textProperty().addListener((o, a, b) -> actualizarMapa());
		txtOrigenLng.textProperty().addListener((o, a, b) -> actualizarMapa());
		txtDestinoLat.textProperty().addListener((o, a, b) -> actualizarMapa());
		txtDestinoLng.textProperty().addListener((o, a, b) -> actualizarMapa());
	}

	private void actualizarMapa() {
		if (lblMapaConductor == null || lblMapaEstado == null) {
			return;
		}

		Usuario conductor = viajeActual != null && viajeActual.getConductor() != null
				? viajeActual.getConductor() : cmbConductor.getValue();
		if (conductor == null) {
			lblMapaConductor.setText("Conductor: sin seleccionar");
		} else {
			boolean asignado = viajeActual != null && viajeActual.getConductor() == conductor;
			Vehiculo vehiculo = conductor.getConductor().getVehiculoActivo();
			lblMapaConductor.setText((asignado ? "Conductor asignado: " : "Conductor seleccionado: ")
					+ conductor.getNombre() + " · " + conductor.getEmail()
					+ (vehiculo == null ? "" : "\nVehículo: " + vehiculo)
					+ "\nLa ubicación GPS del conductor no está disponible.");
		}

		Ubicacion origen = viajeActual == null ? leerUbicacion(txtOrigenLat, txtOrigenLng)
				: viajeActual.getOrigen();
		Ubicacion destino = viajeActual == null ? leerUbicacion(txtDestinoLat, txtDestinoLng)
				: viajeActual.getDestino();
		if (origen == null || destino == null) {
			lblMapaEstado.setText("Ingresá coordenadas válidas de origen y destino para ver la ruta.");
		} else {
			lblMapaEstado.setText(String.format("Origen: %.5f, %.5f    ·    Destino: %.5f, %.5f",
					origen.getLatitud(), origen.getLongitud(), destino.getLatitud(), destino.getLongitud()));
		}
		if (mapaListo) {
			String origenLat = origen == null ? "null" : Double.toString(origen.getLatitud());
			String origenLng = origen == null ? "null" : Double.toString(origen.getLongitud());
			String destinoLat = destino == null ? "null" : Double.toString(destino.getLatitud());
			String destinoLng = destino == null ? "null" : Double.toString(destino.getLongitud());
			mapView.getEngine().executeScript(String.format(java.util.Locale.ROOT,
					"window.actualizarRuta(%s, %s, %s, %s)",
					origenLat, origenLng, destinoLat, destinoLng));
		}
	}

	private Ubicacion leerUbicacion(TextField latitud, TextField longitud) {
		try {
			String lat = latitud.getText().trim();
			String lng = longitud.getText().trim();
			if (lat.isEmpty() || lng.isEmpty()) {
				return null;
			}
			double latitudValor = Double.parseDouble(lat);
			double longitudValor = Double.parseDouble(lng);
			if (!Double.isFinite(latitudValor) || !Double.isFinite(longitudValor)) {
				return null;
			}
			return new Ubicacion(latitudValor, longitudValor);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/** Llamalo al final de refrescar(), dentro del try. */
	private void refrescarViaje(Logica logica) {
		Usuario pas = cmbPasajero.getValue();
		Usuario con = cmbConductor.getValue();
		Servicio ser = cmbServicio.getValue();
		cmbPasajero.getItems().clear();
		for (Usuario u : logica.getUsuarios()) {
			cmbPasajero.getItems().add(u);
		}
		cmbConductor.getItems().clear();
		for (Usuario u : logica.getConductores()) {
			cmbConductor.getItems().add(u);
		}
		cmbServicio.getItems().clear();
		for (Servicio s : logica.getServicios()) {
			cmbServicio.getItems().add(s);
		}
		cmbPasajero.setValue(pas);
		cmbConductor.setValue(con);
		cmbServicio.setValue(ser);
		actualizarTarjetaConductor();
		actualizarEstadoViaje();
	}

	@FXML
	private void solicitarViaje() {
		try {
			Usuario pasajero = cmbPasajero.getValue();
			Servicio servicio = cmbServicio.getValue();
			if (pasajero == null || servicio == null) {
				lblEstado.setText("Elegí un pasajero y un servicio.");
				return;
			}
			Ubicacion origen = new Ubicacion(Double.parseDouble(txtOrigenLat.getText().trim()),
					Double.parseDouble(txtOrigenLng.getText().trim()));
			Ubicacion destino = new Ubicacion(Double.parseDouble(txtDestinoLat.getText().trim()),
					Double.parseDouble(txtDestinoLng.getText().trim()));

			viajeActual = Logica.getInstance().solicitarViaje(pasajero, origen, destino, servicio);
			actualizarEstadoViaje();
		} catch (NumberFormatException e) {
			lblEstado.setText("Las coordenadas deben ser números (ej: -42.65).");
		} catch (Exception e) {
			lblEstado.setText("No se pudo solicitar: " + e.getMessage());
		}
	}

	@FXML
	private void aceptarViaje() {
		try {
			Logica.getInstance().aceptarViaje(viajeActual, cmbConductor.getValue());
			actualizarEstadoViaje();
		} catch (Exception e) {
			lblEstado.setText("No se pudo aceptar: " + e.getMessage());
		}
	}

	@FXML
	private void iniciarViaje() {
		try {
			Logica.getInstance().iniciarViaje(viajeActual);
			actualizarEstadoViaje();
		} catch (Exception e) {
			lblEstado.setText("No se pudo iniciar: " + e.getMessage());
		}
	}

	@FXML
	private void finalizarViaje() {
		try {
			// Calificaciones fijas para el ejemplo: cámbialas por un diálogo si querés
			Logica.getInstance().finalizarViaje(viajeActual, CalificacionViaje.EXCELENTE, CalificacionViaje.EXCELENTE);
			actualizarEstadoViaje();
		} catch (Exception e) {
			lblEstado.setText("No se pudo finalizar: " + e.getMessage());
		}
	}

	@FXML
	private void alternarServicio() {
		Usuario u = cmbConductor.getValue();
		if (u == null) return;
		try {
			Logica logica = Logica.getInstance();
			if (u.getConductor().getEstadoConductor() == EstadoConductor.FUERA_DE_SERVICIO) {
				Vehiculo v = u.getConductor().getVehiculos().get(0);
				logica.ponerConductorEnServicio(u, v, v.getCategoriaVehiculo());
			} else {
				logica.retirarConductorDeServicio(u);
			}
			actualizarTarjetaConductor();
		} catch (Exception e) {
			lblEstado.setText("No se pudo cambiar el estado: " + e.getMessage());
		}
	}

	private void actualizarTarjetaConductor() {
		Usuario u = cmbConductor.getValue();
		boolean hay = u != null;
		if (!hay) {
			lblEstadoConductor.setText("—"); lblCalifConductor.setText("—");
			lblViajesConductor.setText("—"); lblVehiculoConductor.setText("—");
			btnServicio.setDisable(true);
			return;
		}
		Logica logica = Logica.getInstance();
		Conductor c = u.getConductor();
		lblEstadoConductor.setText(String.valueOf(c.getEstadoConductor()));
		lblCalifConductor.setText(String.format("%.1f ★", logica.calcularPromedioEstrellas(u)));
		lblViajesConductor.setText(String.valueOf(logica.cantidadDeViajesFinalizados(u)));
		lblVehiculoConductor.setText(c.getVehiculoActivo() != null ? c.getVehiculoActivo().toString()
				: "Sin vehículo activo (fuera de servicio)");
		btnServicio.setDisable(false);
		btnServicio.setText(c.getEstadoConductor() == EstadoConductor.FUERA_DE_SERVICIO
				? "Ponerse en servicio" : "Salir de servicio");
	}

	/** Pinta las dos tarjetas + la línea de vida según el estado del viaje. */
	private void actualizarEstadoViaje() {
		Label[] pasos = { stepSolicitado, stepAceptado, stepIniciado, stepFinalizado };
		for (Label p : pasos) p.getStyleClass().removeAll("step-activo", "step-hecho");

		if (viajeActual == null) {
			lblViajePasajero.setText("Sin viaje en curso");
			lblViajeConductor.setText("Sin viaje asignado");
			btnAceptar.setDisable(true); btnIniciar.setDisable(true); btnFinalizar.setDisable(true);
			actualizarMapa();
			return;
		}

		EstadoViaje e = viajeActual.estadoActual();
		String detalle = viajeActual.getServicio().getNombre() + " · " + e;
		lblViajePasajero.setText("Estado: " + e + "\nServicio: " + viajeActual.getServicio().getNombre()
				+ (viajeActual.getConductor() != null
						? "\nConductor: " + viajeActual.getConductor().getNombre() : "\nBuscando conductor…"));
		lblViajeConductor.setText("Pasajero: " + viajeActual.getCliente().getNombre() + "\n" + detalle);

		// Botones: cada uno sólo se habilita en el paso que corresponde
		btnAceptar.setDisable(e != EstadoViaje.SOLICITADO || cmbConductor.getValue() == null);
		btnIniciar.setDisable(e != EstadoViaje.ACEPTADO);
		btnFinalizar.setDisable(e != EstadoViaje.INICIADO);

		// Línea de vida: pasos hechos en verde, el actual en azul
		int actual = switch (e) { case SOLICITADO -> 0; case ACEPTADO -> 1; case INICIADO -> 2; case FINALIZADO -> 3; default -> -1; };
		for (int i = 0; i < pasos.length; i++) {
			if (i < actual) pasos[i].getStyleClass().add("step-hecho");
			else if (i == actual) pasos[i].getStyleClass().add("step-activo");
		}
		actualizarTarjetaConductor();
		actualizarMapa();
	}
}