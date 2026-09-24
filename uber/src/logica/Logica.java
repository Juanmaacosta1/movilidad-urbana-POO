package logica;

import java.util.ArrayList;
import java.util.List;

import modelo.*;
import net.datastructures.ProbeHashMap;

public class Logica {

	private ProbeHashMap<String, Usuario> usuarios = new ProbeHashMap<>();
	private ProbeHashMap<String, Vehiculo> vehiculos = new ProbeHashMap<>();
	private ProbeHashMap<String, Viaje> viajes = new ProbeHashMap<>();
	private List<Calificacion> calificaciones = new ArrayList<>();

	public Logica(ProbeHashMap<String, Usuario> usuarios, ProbeHashMap<String, Vehiculo> vehiculos,
			ProbeHashMap<String, Viaje> viajes, List<Calificacion> calificaciones) {
		super();
		this.usuarios = usuarios;
		this.vehiculos = vehiculos;
		this.viajes = viajes;
		this.calificaciones = calificaciones;
	}

}
