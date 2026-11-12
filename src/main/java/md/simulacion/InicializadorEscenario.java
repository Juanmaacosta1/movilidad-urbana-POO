package md.simulacion;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import md.modelo.Usuario;

/**
 * Prepara la posición vial inicial de los conductores ya cargados por los DAO. No simula viajes
 * ni decide qué conductor atiende un pedido (eso es responsabilidad de Logica).
 *
 * Por ahora {@link md.modelo.Conductor} no guarda su ubicación: el resultado se devuelve en un
 * mapa, y cuando el modelo incorpore la posición alcanzará con asignarla en este método.
 */
public class InicializadorEscenario {

	private final GeneradorUbicacionesViales generador;

	public InicializadorEscenario(GeneradorUbicacionesViales generador) {
		if (generador == null) {
			throw new IllegalArgumentException("El generador de ubicaciones es obligatorio");
		}
		this.generador = generador;
	}

	public Map<Usuario, md.modelo.Ubicacion> ubicarConductores(Collection<Usuario> conductores) {
		Map<Usuario, md.modelo.Ubicacion> posiciones = new LinkedHashMap<>();
		for (Usuario usuario : conductores) {
			if (usuario.esConductor()) {
				posiciones.put(usuario, generador.generar());
			}
		}
		return posiciones;
	}
}
