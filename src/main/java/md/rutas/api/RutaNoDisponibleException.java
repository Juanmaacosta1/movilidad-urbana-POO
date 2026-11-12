package md.rutas.api;

/** El motor no pudo encontrar una ruta. No traslada excepciones internas del proveedor. */
public class RutaNoDisponibleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RutaNoDisponibleException(String mensaje) {
		super(mensaje);
	}

	public RutaNoDisponibleException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
