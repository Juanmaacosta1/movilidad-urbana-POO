package md.ui.impl.javafx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import md.logica.Logica;
import md.modelo.Conductor;
import md.modelo.Servicio;
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
}