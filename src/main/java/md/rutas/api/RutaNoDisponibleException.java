package md.rutas.api;

public class RutaNoDisponibleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RutaNoDisponibleException() {
		super("La ruta solicitada no está disponible");
	}

	public RutaNoDisponibleException(String message) {
		super(message);
	}

	public RutaNoDisponibleException(String message, Throwable cause) {
		super(message, cause);
	}
}