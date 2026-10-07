package modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Vehículo registrado por un conductor. Presta entre 1 y 2 tipos de servicio (sin repetir). */
public class Vehiculo {
    private final String patente;
    private final String modelo;
    private final int capacidadPasajeros;
    private final CategoriaVehiculo categoriaVehiculo;
    private final TipoVehiculo tipoVehiculo;
    private final Set<TipoServicio> tipoServicios = EnumSet.noneOf(TipoServicio.class);

    /** Crea un vehículo con su primer tipo de servicio (multiplicidad mínima 1). */
    public Vehiculo(String patente, String modelo, int capacidadPasajeros,
                    CategoriaVehiculo categoriaVehiculo, TipoVehiculo tipoVehiculo,
                    TipoServicio tipoServicio) {
        if (patente == null || patente.isBlank()) {
            throw new IllegalArgumentException("La patente es obligatoria");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("El modelo es obligatorio");
        }
        if (capacidadPasajeros <= 0) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe ser mayor a cero");
        }
        if (categoriaVehiculo == null || tipoVehiculo == null) {
            throw new IllegalArgumentException("Categoría y tipo de vehículo son obligatorios");
        }
        this.patente = patente.trim().toUpperCase();
        this.modelo = modelo;
        this.capacidadPasajeros = capacidadPasajeros;
        this.categoriaVehiculo = categoriaVehiculo;
        this.tipoVehiculo = tipoVehiculo;
        agregarTipoServicio(tipoServicio);
    }

    /** Agrega un tipo de servicio. Un tipo ya presente se ignora (unique). */
    public void agregarTipoServicio(TipoServicio tipoServicio) {
        if (tipoServicio == null) {
            throw new IllegalArgumentException("El tipo de servicio no puede ser nulo");
        }
        tipoServicios.add(tipoServicio);
    }

    public boolean presta(TipoServicio tipoServicio) {
        return tipoServicios.contains(tipoServicio);
    }

    public String getPatente() { return patente; }
    public String getModelo() { return modelo; } 
    public int getCapacidadPasajeros() { return capacidadPasajeros; }
    public CategoriaVehiculo getCategoriaVehiculo() { return categoriaVehiculo; }
    public TipoVehiculo getTipoVehiculo() { return tipoVehiculo; }

    public Set<TipoServicio> getTipoServicios() {
        return Collections.unmodifiableSet(tipoServicios);
    }

    
    
    @Override
    public String toString() {
        return patente + " (" + modelo + ", " + categoriaVehiculo + ")";
    }
}
