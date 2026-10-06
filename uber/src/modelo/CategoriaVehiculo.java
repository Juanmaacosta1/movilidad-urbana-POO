package modelo;

public enum CategoriaVehiculo {
	
	ESTANDAR (1),
	CONFORT (2),
	PREMIUM (3);
	
	private final int valor;
	
	CategoriaVehiculo(int valor) {
		this.valor = valor;
	}
	
	public int getValor() {
		return valor;
	}
	
}