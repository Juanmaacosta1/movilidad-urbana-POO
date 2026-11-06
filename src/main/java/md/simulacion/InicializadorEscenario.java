package md.simulacion;

import md.config.CargaParametros;
import md.modelo.Ubicacion;

public class InicializadorEscenario {

	private final double latMin;
	private final double latMax;
	private final double lonMin;
	private final double lonMax;
	private final GeneradorUbicacionesViales generador;

	public InicializadorEscenario() {
		this.latMin = Math.min(CargaParametros.getLatitud1(), CargaParametros.getLatitud2());
		this.latMax = Math.max(CargaParametros.getLatitud1(), CargaParametros.getLatitud2());
		this.lonMin = Math.min(CargaParametros.getLongitud1(), CargaParametros.getLongitud2());
		this.lonMax = Math.max(CargaParametros.getLongitud1(), CargaParametros.getLongitud2());
		this.generador = new GeneradorUbicacionesViales();
	}

	public Ubicacion generarUbicacionAleatoria() {
		return generador.generarEnArea(latMin, latMax, lonMin, lonMax);
	}

	public double getLatMin() {
		return latMin;
	}

	public double getLatMax() {
		return latMax;
	}

	public double getLonMin() {
		return lonMin;
	}

	public double getLonMax() {
		return lonMax;
	}

	public GeneradorUbicacionesViales getGenerador() {
		return generador;
	}
}