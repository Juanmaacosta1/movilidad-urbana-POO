package modelo;

public class Servicio {

	private String nombre;
	private double tarifaBase;
	private double precioKm;
	private double precioMinuto;
	private CategoriaVehiculo categoriaVehiculo;
	private TipoVehiculo tipovehiculo;
	private TipoServicio tipoServicio;

	public Servicio(String nombre, double tarifaBase, double precioKm, double precioMinuto,
			CategoriaVehiculo categoriaVehiculo, TipoVehiculo tipovehiculo, TipoServicio tipoServicio) {
		super();
		this.nombre = nombre;
		this.tarifaBase = tarifaBase;
		this.precioKm = precioKm;
		this.precioMinuto = precioMinuto;
		this.categoriaVehiculo = categoriaVehiculo;
		this.tipovehiculo = tipovehiculo;
		this.tipoServicio = tipoServicio;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getTarifaBase() {
		return tarifaBase;
	}

	public void setTarifaBase(double tarifaBase) {
		this.tarifaBase = tarifaBase;
	}

	public double getPrecioKm() {
		return precioKm;
	}

	public void setPrecioKm(double precioKm) {
		this.precioKm = precioKm;
	}

	public double getPrecioMinuto() {
		return precioMinuto;
	}

	public void setPrecioMinuto(double precioMinuto) {
		this.precioMinuto = precioMinuto;
	}

	public double calcularCosto(double km, double minutos) {
		if (km < 0 || minutos < 0)
			throw new IllegalArgumentException("Kilometros y minutos no pueden ser negativos");
		return tarifaBase + precioKm * km + precioMinuto * minutos;
	}

	@Override
	public String toString() {
		return "Servicio [nombre=" + nombre + ", tarifaBase=" + tarifaBase + ", precioKm=" + precioKm
				+ ", precioMinuto=" + precioMinuto + "]";
	}

}
