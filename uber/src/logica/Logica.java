package logica;

import java.time.LocalDateTime;
import java.util.Objects;

import datos.CargarDatos;
import datos.CargarParametros;
import modelo.*;
import net.datastructures.ArrayList;
import net.datastructures.ChainHashMap;
import net.datastructures.List;
import net.datastructures.Map;

public class Logica {

	private static final double VELOCIDAD_PROMEDIO = 30.0;

	private static Logica instancia;

	private Map<String, Usuario> usuarios = new ChainHashMap<>();
	private List<Servicio> servicios = new ArrayList<>();
	private List<Viaje> viajes = new ArrayList<>();

	private Logica() {
		cargarDatosIniciales();
	}

	public static Logica getInstance() {
		if (instancia == null) {
			instancia = new Logica();
		}

		return instancia;
	}

	private void cargarDatosIniciales() {
		try {
			CargarParametros.parametros();

			java.util.ArrayList<Servicio> serviciosCargados = CargarDatos
					.cargarServicios(CargarParametros.getArchivoServicios());

			servicios = new ArrayList<>();

			for (Servicio servicio : serviciosCargados) {
				servicios.add(servicios.size(), servicio);
			}

			java.util.Set<Vehiculo> vehiculos = CargarDatos.cargarVehiculos(CargarParametros.getArchivoVehiculos());

			usuarios = CargarDatos.cargarUsuarios(CargarParametros.getArchivoUsuarios(), vehiculos);

		} catch (Exception e) {
			throw new IllegalStateException("No se pudieron cargar los datos iniciales", e);
		}
	}

	public void reiniciar() {
		usuarios = new ChainHashMap<>();
		servicios = new ArrayList<>();
		viajes = new ArrayList<>();
	}

	public Usuario registrarUsuario(String nombre, String telefono, String email) {

		validarTexto(nombre, "El nombre es obligatorio");

		validarTexto(telefono, "El teléfono es obligatorio");

		validarTexto(email, "El email es obligatorio");

		String clave = email.trim().toLowerCase();

		if (usuarios.get(clave) != null) {
			throw new IllegalArgumentException("Ya existe un usuario con el email " + email);
		}

		Usuario usuario = new Usuario(nombre.trim(), telefono.trim(), email.trim());

		usuarios.put(clave, usuario);

		return usuario;
	}

	public Usuario buscarUsuario(String email) {
		if (email == null) {
			return null;
		}

		return usuarios.get(email.trim().toLowerCase());
	}

	public void ponerConductorEnServicio(Usuario usuario, Vehiculo vehiculo, CategoriaVehiculo categoriaMinima) {

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}

		if (!usuario.esConductor()) {
			throw new IllegalStateException("El usuario no está registrado como conductor");
		}

		if (vehiculo == null) {
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
		}

		if (categoriaMinima == null) {
			throw new IllegalArgumentException("La categoría no puede ser nula");
		}

		Conductor conductor = usuario.getConductor();

		if (conductor.getEstadoConductor() != EstadoConductor.FUERA_DE_SERVICIO) {

			throw new IllegalStateException("El conductor debe estar Fuera de Servicio para ponerse en servicio");
		}

		if (!contiene(conductor.getVehiculos(), vehiculo)) {
			throw new IllegalArgumentException("El vehículo no pertenece al conductor");
		}

		if (categoriaMinima.getValor() > vehiculo.getCategoriaVehiculo().getValor()) {

			throw new IllegalArgumentException("La categoría mínima no puede ser mayor a la categoría del vehículo");
		}

		conductor.cambiarVehiculoActivo(vehiculo);

		// ESTA LÍNEA ES LA QUE FALTA
		conductor.establecerCategoriaMinima(categoriaMinima);

		conductor.cambiarEstado(EstadoConductor.DISPONIBLE);
	}

	public void retirarConductorDeServicio(Usuario usuario) {

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}

		if (!usuario.esConductor()) {
			throw new IllegalStateException("El usuario no está registrado como conductor");
		}

		Conductor conductor = usuario.getConductor();

		if (conductor.getEstadoConductor() != EstadoConductor.DISPONIBLE) {

			throw new IllegalStateException("El conductor no está disponible");
		}

		conductor.cambiarEstado(EstadoConductor.FUERA_DE_SERVICIO);
	}

	public List<Usuario> buscarConductoresDisponibles(Viaje viaje) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		List<Usuario> conductoresDisponibles = new ArrayList<>();
		Servicio servicio = viaje.getServicio();

		for (Usuario usuario : usuarios.values()) {

			if (!usuario.esConductor()) {
				continue;
			}

			Conductor conductor = usuario.getConductor();

			if (conductor.getEstadoConductor() != EstadoConductor.DISPONIBLE) {
				continue;
			}

			Vehiculo vehiculo = conductor.getVehiculoActivo();

			if (!vehiculo.presta(servicio.getTipoServicio())) {
				continue;
			}

			if (vehiculo.getTipoVehiculo() != servicio.getTipoVehiculo()) {
				continue;
			}

			int categoriaPedida = servicio.getCategoriaVehiculo().getValor();

			int categoriaMinima = conductor.getCategoriaMinimaAceptada().getValor();

			int categoriaVehiculo = vehiculo.getCategoriaVehiculo().getValor();

			if (categoriaPedida < categoriaMinima || categoriaPedida > categoriaVehiculo) {
				continue;
			}

			conductoresDisponibles.add(conductoresDisponibles.size(), usuario);
		}

		return conductoresDisponibles;
	}

	public Viaje solicitarViaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {

		if (cliente == null) {
			throw new IllegalArgumentException("El cliente no puede ser nulo");
		}

		if (cliente.getCliente().enViaje()) {
			throw new IllegalStateException("El cliente ya tiene un viaje en curso");
		}

		Viaje viaje = new Viaje(cliente, origen, destino, servicio);

		viaje.solicitar(LocalDateTime.now());

		viajes.add(viajes.size(), viaje);

		return viaje;
	}

	public void rechazarViaje(Viaje viaje, String motivo) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
		}

		viaje.rechazar(LocalDateTime.now(), motivo);
	}

	public void aceptarViaje(Viaje viaje, Usuario conductor) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		if (conductor == null) {
			throw new IllegalArgumentException("El conductor no puede ser nulo");
		}

		List<Usuario> conductoresDisponibles = buscarConductoresDisponibles(viaje);

		if (!contiene(conductoresDisponibles, conductor)) {

			throw new IllegalStateException("El conductor no está disponible para este viaje");
		}

		viaje.aceptar(LocalDateTime.now(), conductor);
	}

	public void iniciarViaje(Viaje viaje) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		if (viaje.estadoActual() != EstadoViaje.ACEPTADO) {

			throw new IllegalStateException("Solo se puede iniciar un viaje ACEPTADO");
		}

		viaje.iniciar(LocalDateTime.now());
	}

	public void finalizarViaje(Viaje viaje, CalificacionViaje calificacionConductor,
			CalificacionViaje calificacionCliente) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		if (viaje.estadoActual() != EstadoViaje.INICIADO) {

			throw new IllegalStateException("Solo se puede finalizar un viaje INICIADO");
		}

		viaje.finalizar(LocalDateTime.now(), calificacionConductor, calificacionCliente);
	}

	public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}

		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
		}

		viaje.cancelar(LocalDateTime.now(), usuario, motivo);
	}

	public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo, double kmRecorridos) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}

		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
		}

		if (kmRecorridos < 0) {
			throw new IllegalArgumentException("Los kilómetros recorridos no pueden ser negativos");
		}

		viaje.cancelar(LocalDateTime.now(), usuario, motivo, kmRecorridos);
	}

	public List<Viaje> getViajes() {
		return viajes;
	}

	public List<Usuario> getUsuarios() {

		List<Usuario> lista = new ArrayList<>();

		for (Usuario usuario : usuarios.values()) {
			lista.add(lista.size(), usuario);
		}

		return lista;
	}

	public List<Vehiculo> getVehiculos() {

		List<Vehiculo> lista = new ArrayList<>();

		for (Usuario usuario : usuarios.values()) {

			if (usuario.esConductor()) {

				for (Vehiculo vehiculo : usuario.getConductor().getVehiculos()) {

					lista.add(lista.size(), vehiculo);
				}
			}
		}

		return lista;
	}

	public List<Servicio> getServicios() {
		return servicios;
	}

	private <T> boolean contiene(Iterable<T> coleccion, T buscado) {

		for (T elemento : coleccion) {

			if (Objects.equals(elemento, buscado)) {

				return true;
			}
		}

		return false;
	}

	private void validarUsuario(Usuario usuario) {

		Objects.requireNonNull(usuario, "El usuario es obligatorio");

		if (usuarios.get(usuario.getEmail().trim().toLowerCase()) != usuario) {

			throw new IllegalArgumentException("El usuario no está registrado");
		}
	}

	private Conductor obtenerConductor(Usuario usuario) {

		validarUsuario(usuario);

		if (usuario.getConductor() == null) {
			throw new IllegalStateException("El usuario no está habilitado como conductor");
		}

		return usuario.getConductor();
	}

	private void validarTexto(String texto, String mensaje) {

		if (texto == null || texto.trim().isEmpty()) {

			throw new IllegalArgumentException(mensaje);
		}
	}
}