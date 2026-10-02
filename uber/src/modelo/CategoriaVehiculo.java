package modelo;

public enum CategoriaVehiculo {
	
	ESTANDAR (0),
	CONFORT (1),
	PREMIUM (2);
	
	private final int valor;
	
	CategoriaVehiculo(int valor) {
		this.valor = valor;
	}
	
	public int getValor() {
		return valor;
	}
	
}
