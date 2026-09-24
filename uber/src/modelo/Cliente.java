package modelo;

public class Cliente extends Usuario {
	private static int limiteCalificaciones=100;

	public Cliente(String nombre, String documento, String telefono, String email) {
		super(nombre, documento, telefono, email);
	}

	@Override
	protected int getLimiteCalificaciones() {
		// TODO Auto-generated method stub
		return limiteCalificaciones;
	}

	
	


	
	

}
