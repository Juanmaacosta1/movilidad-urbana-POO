package md.logica;

import java.util.ArrayList;
import java.util.List;

import md.modelo.CalificacionViaje;
import md.modelo.CategoriaVehiculo;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;

public class Movilidad {

	private static Movilidad instancia;
	private final Logica logica;

	private Movilidad() {
		this.logica = Logica.getInstance();
	}

	public static Movilidad getInstance() {
		if (instancia == null) {
			instancia = new Movilidad();
		}
		return instancia;
	}

	public Usuario registrarUsuario(String nombre, String telefono, String email) {
		return logica.registrarUsuario(nombre, telefono, email);
	}

	public Usuario buscarUsuario(String email) {
		return logica.buscarUsuario(email);
	}

	public void altaConductor(Usuario usuario, String licencia, Vehiculo vehiculo) {
		logica.altaConductor(usuario, licencia, vehiculo);
	}

	public void ponerConductorEnServicio(Usuario usuario, Vehiculo vehiculo, CategoriaVehiculo categoriaMinima) {
		logica.ponerConductorEnServicio(usuario, vehiculo, categoriaMinima);
	}

	public void retirarConductorDeServicio(Usuario usuario) {
		logica.retirarConductorDeServicio(usuario);
	}

	public List<Usuario> buscarConductoresDisponibles(Viaje viaje) {
		return convertir(logica.buscarConductoresDisponibles(viaje));
	}

	public Viaje solicitarViaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {
		return logica.solicitarViaje(cliente, origen, destino, servicio);
	}

	public void aceptarViaje(Viaje viaje, Usuario conductor) {
		logica.aceptarViaje(viaje, conductor);
	}

	public void iniciarViaje(Viaje viaje) {
		logica.iniciarViaje(viaje);
	}

	public void finalizarViaje(Viaje viaje, CalificacionViaje calificacionConductor,
			CalificacionViaje calificacionCliente) {
		logica.finalizarViaje(viaje, calificacionConductor, calificacionCliente);
	}

	public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo) {
		logica.cancelarViaje(viaje, usuario, motivo);
	}

	public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo, double kmRecorridos) {
		logica.cancelarViaje(viaje, usuario, motivo, kmRecorridos);
	}

	public List<Viaje> getViajes() {
		return convertir(logica.getViajes());
	}

	public List<Usuario> getUsuarios() {
		return convertir(logica.getUsuarios());
	}

	public List<Vehiculo> getVehiculos() {
		return convertir(logica.getVehiculos());
	}

	public List<Servicio> getServicios() {
		return convertir(logica.getServicios());
	}

	public double calcularPromedioEstrellas(Usuario usuario) {
		return logica.calcularPromedioEstrellas(usuario);
	}

	public double calcularPromedioEstrellasCliente(Usuario usuario) {
		return logica.calcularPromedioEstrellasCliente(usuario);
	}

	public String estrellas(double promedio) {
		return logica.estrellas(promedio);
	}

	public boolean usuarioEnViaje(Usuario usuario) {
		return logica.UsuarioEnViaje(usuario);
	}

	public List<Usuario> getConductoresDisponibles() {
		return convertir(logica.getConductoresEnServicio());
	}

	public void cambiarRol(Usuario usuario, md.modelo.RolUsuario rol) {
		logica.CambiarRol(usuario, rol);
	}

	public void agregarVehiculoAConductor(Usuario usuario, Vehiculo vehiculo) {
		logica.agregarVehiculoAConductor(usuario, vehiculo);
	}

	public boolean prestaServicio(Vehiculo vehiculo, TipoServicio tipoServicio) {
		return vehiculo != null && vehiculo.presta(tipoServicio);
	}

	private static <T> List<T> convertir(Iterable<T> elementos) {
		List<T> resultado = new ArrayList<>();
		for (T elemento : elementos) {
			resultado.add(elemento);
		}
		return resultado;
	}
}