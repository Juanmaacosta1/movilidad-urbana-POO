package md.negocio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import md.modelo.Ubicacion;
import net.datastructures.AdjacencyMapGraph;
import net.datastructures.Edge;
import net.datastructures.Graph;
import net.datastructures.GraphAlgorithms;
import net.datastructures.Position;
import net.datastructures.PositionalList;
import net.datastructures.ProbeHashMap;
import net.datastructures.TreeMap;
import net.datastructures.Vertex;

/**
 * Calcula recorridos mínimos sobre un grafo de ubicaciones y resume tiempos.
 * Adaptación del patrón usado en subte, pero con el dominio de movilidad digital.
 */
public class Calculo {

    private final Graph<Ubicacion, Tramo> grafo;
    private final TreeMap<String, Vertex<Ubicacion>> vertices;
    private final java.util.Map<Ubicacion, Vertex<Ubicacion>> verticesPorUbicacion;

    public Calculo(Map<String, Ubicacion> ubicaciones, List<Tramo> tramos) {
        this.grafo = new AdjacencyMapGraph<>(false);
        this.vertices = new TreeMap<>();
        this.verticesPorUbicacion = new java.util.HashMap<>();

        for (Map.Entry<String, Ubicacion> entrada : ubicaciones.entrySet()) {
            Vertex<Ubicacion> v = grafo.insertVertex(entrada.getValue());
            vertices.put(entrada.getKey(), v);
            verticesPorUbicacion.put(entrada.getValue(), v);
        }

        for (Tramo tramo : tramos) {
            if (tramo == null) {
                continue;
            }
            Vertex<Ubicacion> v1 = verticesPorUbicacion.get(tramo.getEstacion1());
            Vertex<Ubicacion> v2 = verticesPorUbicacion.get(tramo.getEstacion2());
            if (v1 != null && v2 != null) {
                grafo.insertEdge(v1, v2, tramo);
            }
        }
    }

    /**
     * Calcula la ruta más corta entre origen y destino, usando distancia como peso.
     */
    public List<Tramo> rapido(Ubicacion origen, Ubicacion destino) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Las ubicaciones de origen y destino no pueden ser nulas.");
        }

        Graph<Ubicacion, Integer> grafoDistancia = new AdjacencyMapGraph<>(false);
        net.datastructures.Map<Ubicacion, Vertex<Ubicacion>> mapVertices = new ProbeHashMap<>();

        for (Vertex<Ubicacion> v : grafo.vertices()) {
            mapVertices.put(v.getElement(), grafoDistancia.insertVertex(v.getElement()));
        }

        for (Edge<Tramo> edge : grafo.edges()) {
            Vertex<Ubicacion>[] extremos = grafo.endVertices(edge);
            int peso = (int) Math.round(edge.getElement().getDistanciaKm() * 1000.0);
            grafoDistancia.insertEdge(
                mapVertices.get(extremos[0].getElement()),
                mapVertices.get(extremos[1].getElement()),
                peso
            );
        }

        Vertex<Ubicacion> vOrigen = mapVertices.get(origen);
        Vertex<Ubicacion> vDestino = mapVertices.get(destino);

        if (vOrigen == null || vDestino == null) {
            throw new IllegalArgumentException("No existe una ruta válida entre las ubicaciones indicadas.");
        }

        PositionalList<Vertex<Ubicacion>> listaCamino = GraphAlgorithms.shortestPathList(grafoDistancia, vOrigen, vDestino);

        if (listaCamino == null || listaCamino.isEmpty()) {
            throw new IllegalArgumentException("No se encontró una ruta entre las ubicaciones indicadas.");
        }

        List<Tramo> recorrido = new ArrayList<>();
        Position<Vertex<Ubicacion>> actual = listaCamino.first();

        while (actual != null && listaCamino.after(actual) != null) {
            Vertex<Ubicacion> v1 = actual.getElement();
            actual = listaCamino.after(actual);
            Vertex<Ubicacion> v2 = actual.getElement();

            Vertex<Ubicacion> gV1 = buscarVertice(v1.getElement());
            Vertex<Ubicacion> gV2 = buscarVertice(v2.getElement());

            Edge<Tramo> edge = null;
            if (gV1 != null && gV2 != null) {
                edge = grafo.getEdge(gV1, gV2);
                if (edge == null) {
                    edge = grafo.getEdge(gV2, gV1);
                }
            }

            if (edge != null) {
                recorrido.add(edge.getElement());
            }
        }

        return recorrido;
    }

    private Vertex<Ubicacion> buscarVertice(Ubicacion ubicacion) {
        for (Vertex<Ubicacion> v : grafo.vertices()) {
            if (v.getElement().equals(ubicacion)) {
                return v;
            }
        }
        return null;
    }

    /** Tiempo total del recorrido, según una velocidad promedio dada. */
    public double calcularTiempoTotal(List<Tramo> recorrido, double velocidadPromedioKmH) {
        if (recorrido == null || recorrido.isEmpty()) {
            return 0.0;
        }
        if (velocidadPromedioKmH <= 0) {
            throw new IllegalArgumentException("La velocidad promedio debe ser mayor que cero.");
        }
        return recorrido.stream().mapToDouble(t -> t.getTiempoMinutos(velocidadPromedioKmH)).sum();
    }

    /** Criterio histórico: mismo tipo de segmento implica viaje en vehículo. */
    public double calcularTiempoEnVehiculo(List<Tramo> recorrido, double velocidadPromedioKmH) {
        if (recorrido == null || recorrido.isEmpty()) {
            return 0.0;
        }
        return recorrido.stream()
            .filter(Tramo::isEnVehiculo)
            .mapToDouble(t -> t.getTiempoMinutos(velocidadPromedioKmH))
            .sum();
    }

    /** Criterio histórico: segmentos a pie se consideran caminata. */
    public double calcularTiempoCaminando(List<Tramo> recorrido, double velocidadPromedioKmH) {
        if (recorrido == null || recorrido.isEmpty()) {
            return 0.0;
        }
        return recorrido.stream()
            .filter(t -> !t.isEnVehiculo())
            .mapToDouble(t -> t.getTiempoMinutos(velocidadPromedioKmH))
            .sum();
    }

    /**
     * Segmento del recorrido entre dos ubicaciones.
     * Sirve como equivalente a Tramo del proyecto de subte, pero usando Ubicacion.
     */
    public static class Tramo {
        private final Ubicacion estacion1;
        private final Ubicacion estacion2;
        private final double distanciaKm;
        private final boolean enVehiculo;

        public Tramo(Ubicacion estacion1, Ubicacion estacion2, boolean enVehiculo) {
            this(estacion1, estacion2, estacion1.calcularDistancia(estacion2), enVehiculo);
        }

        public Tramo(Ubicacion estacion1, Ubicacion estacion2, double distanciaKm, boolean enVehiculo) {
            if (estacion1 == null || estacion2 == null) {
                throw new IllegalArgumentException("Las ubicaciones del tramo no pueden ser nulas.");
            }
            if (distanciaKm < 0) {
                throw new IllegalArgumentException("La distancia del tramo no puede ser negativa.");
            }
            this.estacion1 = estacion1;
            this.estacion2 = estacion2;
            this.distanciaKm = distanciaKm;
            this.enVehiculo = enVehiculo;
        }

        public Ubicacion getEstacion1() {
            return estacion1;
        }

        public Ubicacion getEstacion2() {
            return estacion2;
        }

        public double getDistanciaKm() {
            return distanciaKm;
        }

        public boolean isEnVehiculo() {
            return enVehiculo;
        }

        public double getTiempoMinutos(double velocidadPromedioKmH) {
            if (velocidadPromedioKmH <= 0) {
                throw new IllegalArgumentException("La velocidad promedio debe ser mayor que cero.");
            }
            return (distanciaKm / velocidadPromedioKmH) * 60.0;
        }
    }
}