package logica;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import datos.CargarDatos;
import datos.CargarParametros;
import modelo.CalificacionViaje;
import modelo.CategoriaVehiculo;
import modelo.Conductor;
import modelo.EstadoConductor;
import modelo.EstadoViaje;
import modelo.Servicio;
import modelo.Ubicacion;
import modelo.Usuario;
import modelo.Vehiculo;
import modelo.Viaje;

public class Logica {

	private Set<Usuario> usuarios;
	private Set<Vehiculo> vehiculos;
	private ArrayList<Servicio> servicios;
	private Set<Viaje> viajes;

	public Logica() throws IOException {

		vehiculos = CargarDatos.cargarVehiculos(CargarParametros.getArchivoVehiculos());
		usuarios = CargarDatos.cargarUsuarios(CargarParametros.getArchivoUsuarios(), vehiculos);
		servicios = CargarDatos.cargarServicios(CargarParametros.getArchivoServicios());
		viajes = new HashSet<>();

	}
	
	
	public Set<Usuario> buscarConductoresDisponibles(Viaje viaje) {

	    if (viaje == null) {
	        throw new IllegalArgumentException("El viaje no puede ser nulo");
	    }

	    Set<Usuario> conductoresDisponibles = new HashSet<>();
	    Servicio servicio = viaje.getServicio();

	    for (Usuario usuario : usuarios) {
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
	        if (servicio.getCategoriaVehiculo().getValor()
	                > conductor.getCategoriaMaximaAceptada().getValor()) {
	            continue;
	        }
	        conductoresDisponibles.add(usuario);
	    }

	    return conductoresDisponibles;
	}

	public Viaje solicitarViaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {

		Viaje viaje = new Viaje(cliente, origen, destino, servicio);
		viaje.solicitar(LocalDateTime.now());
		viajes.add(viaje);
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

		Set<Usuario> conductoresDisponibles = buscarConductoresDisponibles(viaje);

		if (!conductoresDisponibles.contains(conductor)) {
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

	public void ponerConductorEnServicio(Usuario usuario, Vehiculo vehiculo, CategoriaVehiculo categoriaMaxima) {

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}
		if (!usuario.esConductor()) {
			throw new IllegalStateException("El usuario no está registrado como conductor");
		}
		if (vehiculo == null) {
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
		}
		if (categoriaMaxima == null) {
			throw new IllegalArgumentException("La categoría máxima no puede ser nula");
		}

		Conductor conductor = usuario.getConductor();

		if (!conductor.getVehiculos().contains(vehiculo)) {
			throw new IllegalArgumentException("El vehículo no pertenece al conductor");
		}

		conductor.cambiarVehiculoActivo(vehiculo);
		conductor.establecerCategoriaMaxima(categoriaMaxima);
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

	public Set<Viaje> getViajes() {
		return viajes;
	}

	public Set<Usuario> getUsuarios() {
		return usuarios;
	}

	public Set<Vehiculo> getVehiculos() {
		return vehiculos;
	}

	public ArrayList<Servicio> getServicios() {
		return servicios;
	}

}