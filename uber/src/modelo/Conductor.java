package modelo;

public class Conductor extends Usuario {
	private boolean disponible;
	private static int limiteCalificaciones = 200;

	public Conductor(String nombre, String documento, String telefono, String email, int totalCalificaciones,
			boolean disponible) {
		super(nombre, documento, telefono, email);
		this.disponible = disponible;
		
		
	}

	@Override
	protected int getLimiteCalificaciones() {
		// TODO Auto-generated method stub
		return limiteCalificaciones;
	}

}
