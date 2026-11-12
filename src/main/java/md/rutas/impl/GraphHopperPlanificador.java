package md.rutas.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PointList;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;

/**
 * Adaptador de GraphHopper Core al contrato {@link PlanificadorRutas}. Es el único lugar del
 * proyecto que conoce com.graphhopper.*: lo que sale de acá son tipos de md.rutas.api.
 *
 * Importa el extracto OSM la primera vez (puede tardar minutos) y reutiliza la caché después.
 * Perfil "car" con preparación CH. Hoy solo está implementado el perfil AUTO.
 * No se modelan costos de giro ni tránsito en vivo.
 */
public final class GraphHopperPlanificador implements PlanificadorRutas {

	private static final String PERFIL_AUTO = "car";
	private static final String ACCESO_AUTO = "car_access";

	private final GraphHopper motor;
	private final BooleanEncodedValue accesoAuto;
	private boolean cerrado;

	public GraphHopperPlanificador(String archivoOsm, String directorioCache) {
		if (archivoOsm == null || archivoOsm.isBlank()) {
			throw new IllegalArgumentException("Falta la ruta del archivo .osm");
		}
		if (directorioCache == null || directorioCache.isBlank()) {
			throw new IllegalArgumentException("Falta el directorio de caché del grafo");
		}
		boolean hayCache = Files.isDirectory(Path.of(directorioCache));
		if (!hayCache && !Files.isRegularFile(Path.of(archivoOsm))) {
			throw new IllegalStateException("No se encontró el archivo " + archivoOsm
					+ " (ni una caché previa en " + directorioCache + "). Ver mapas/README.md. "
					+ "Las rutas son relativas al directorio de ejecución: debe ser la raíz del proyecto.");
		}
		try {
			motor = new GraphHopper();
			motor.setOSMFile(archivoOsm);
			motor.setGraphHopperLocation(directorioCache);
			motor.setProfiles(crearPerfilAuto());
			motor.getCHPreparationHandler().setCHProfiles(new CHProfile(PERFIL_AUTO));
			motor.importOrLoad();
			accesoAuto = motor.getEncodingManager().getBooleanEncodedValue(ACCESO_AUTO);
		} catch (RuntimeException e) {
			throw new IllegalStateException("No se pudo importar o cargar el mapa vial: " + e.getMessage(), e);
		}
	}

	private static Profile crearPerfilAuto() {
		try {
			return new Profile(PERFIL_AUTO).setCustomModel(GHUtility.loadCustomModelFromJar("car.json"));
		} catch (RuntimeException e) {
			return new Profile(PERFIL_AUTO);
		}
	}

	@Override
	public RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo vehiculo) {
		verificarAbierto();
		if (origen == null || destino == null || vehiculo == null) {
			throw new IllegalArgumentException("Origen, destino y tipo de vehículo son obligatorios");
		}
		if (vehiculo != TipoVehiculo.AUTO) {
			throw new UnsupportedOperationException("El perfil " + vehiculo + " todavía no está implementado");
		}

		GHRequest pedido = new GHRequest(origen.getLatitud(), origen.getLongitud(),
				destino.getLatitud(), destino.getLongitud()).setProfile(PERFIL_AUTO);
		GHResponse respuesta = motor.route(pedido);
		if (respuesta.hasErrors()) {
			Throwable causa = respuesta.getErrors().get(0);
			throw new RutaNoDisponibleException("No hay ruta entre los puntos: " + causa.getMessage(), causa);
		}

		ResponsePath mejor = respuesta.getBest();
		PointList puntos = mejor.getPoints();
		List<PuntoRuta> geometria = new ArrayList<>(puntos.size() + 1);
		for (int i = 0; i < puntos.size(); i++) {
			geometria.add(new PuntoRuta(puntos.getLat(i), puntos.getLon(i)));
		}
		if (geometria.size() == 1) {
			// Origen y destino se ajustan al mismo punto de la red: el contrato exige al menos dos puntos
			geometria.add(geometria.get(0));
		}
		return new RutaCalculada(mejor.getDistance(), mejor.getTime(), geometria);
	}

	@Override
	public Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros) {
		verificarAbierto();
		if (candidata == null) {
			throw new IllegalArgumentException("La ubicación candidata es obligatoria");
		}
		if (!(maxDistanciaMetros > 0)) {
			throw new IllegalArgumentException("La distancia máxima debe ser mayor a cero");
		}

		// Solo tramos por los que un auto puede circular (en algún sentido)
		EdgeFilter transitableEnAuto = arista -> arista.get(accesoAuto) || arista.getReverse(accesoAuto);
		Snap cercano = motor.getLocationIndex().findClosest(candidata.getLatitud(), candidata.getLongitud(),
				transitableEnAuto);
		if (!cercano.isValid() || cercano.getQueryDistance() > maxDistanciaMetros) {
			return Optional.empty();
		}
		var punto = cercano.getSnappedPoint();
		return Optional.of(new Ubicacion(punto.getLat(), punto.getLon()));
	}

	@Override
	public synchronized void close() {
		if (!cerrado) {
			cerrado = true;
			motor.close();
		}
	}

	private void verificarAbierto() {
		if (cerrado) {
			throw new IllegalStateException("El planificador ya fue cerrado");
		}
	}
}