package md.rutas.api;

import java.util.Optional;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;

/**
 * Contrato del módulo de rutas: qué se puede pedir, sin decidir cómo se resuelve.
 * Quien lo consume no necesita importar clases de ninguna biblioteca concreta.
 */
public interface PlanificadorRutas extends AutoCloseable {

	/**
	 * @throws RutaNoDisponibleException si no existe un camino entre los puntos
	 */
	RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo vehiculo);

	/** Busca la posición transitable más próxima a la candidata, dentro de la distancia máxima. */
	Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros);

	/** Libera los recursos del motor (grafo, índices, archivos). */
	@Override
	void close();
}
