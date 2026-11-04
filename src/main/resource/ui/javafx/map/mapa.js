const centroPuertoMadryn = [-42.7692, -65.0385];
const zoomInicial = 13;
const colorRecorrido = "#2563eb";

const mapa = L.map("map").setView(centroPuertoMadryn, zoomInicial);
const capasDelRecorrido = [];
let tipoDePuntoPendiente = null;
let numeroDeSolicitudDeRuta = 0;

L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(mapa);

const marcadorDePuertoMadryn = L.marker(centroPuertoMadryn)
    .addTo(mapa)
    .bindPopup("Puerto Madryn");

window.setModoSeleccion = function(tipoDePunto) {
    tipoDePuntoPendiente = tipoDePunto || null;
    mapa.getContainer().style.cursor = tipoDePuntoPendiente ? "crosshair" : "";
};

window.ajustarTamanoMapa = function() {
    window.requestAnimationFrame(() => mapa.invalidateSize());
};

window.actualizarRuta = function(latitudOrigen, longitudOrigen, latitudDestino, longitudDestino) {
    limpiarCapasDelRecorrido();
    numeroDeSolicitudDeRuta++;

    if (latitudOrigen === null && latitudDestino === null) {
        mostrarPuertoMadryn();
        return;
    }

    mapa.removeLayer(marcadorDePuertoMadryn);

    const origen = crearCoordenada(latitudOrigen, longitudOrigen);
    const destino = crearCoordenada(latitudDestino, longitudDestino);

    mostrarMarcador(origen, "Punto de partida");
    mostrarMarcador(destino, "Destino");

    if (origen && destino) {
        solicitarRutaPorCalles(origen, destino, numeroDeSolicitudDeRuta);
    } else {
        mapa.setView(origen || destino, 15);
    }
};

mapa.on("click", evento => {
    if (!tipoDePuntoPendiente) {
        return;
    }

    // JavaFX recibe las coordenadas del clic mediante el evento de alerta del WebView.
    window.alert(`PUNTO_MAPA:${evento.latlng.lat},${evento.latlng.lng}`);
});

function crearCoordenada(latitud, longitud) {
    return latitud === null ? null : [latitud, longitud];
}

function mostrarPuertoMadryn() {
    marcadorDePuertoMadryn.addTo(mapa);
    mapa.setView(centroPuertoMadryn, zoomInicial);
}

function mostrarMarcador(coordenada, descripcion) {
    if (!coordenada) {
        return;
    }

    const marcador = L.marker(coordenada)
        .addTo(mapa)
        .bindPopup(descripcion);

    capasDelRecorrido.push(marcador);
}

function solicitarRutaPorCalles(origen, destino, numeroDeSolicitud) {
    const url = crearUrlDeRuta(origen, destino);

    fetch(url)
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error(`El servicio de rutas respondió ${respuesta.status}`);
            }
            return respuesta.json();
        })
        .then(datos => {
            if (numeroDeSolicitud !== numeroDeSolicitudDeRuta) {
                return;
            }
            if (!datos.routes || datos.routes.length === 0) {
                throw new Error("El servicio no encontró una ruta por calles");
            }

            mostrarRutaPorCalles(datos.routes[0].geometry);
        })
        .catch(error => {
            if (numeroDeSolicitud !== numeroDeSolicitudDeRuta) {
                return;
            }

            console.error("No se pudo cargar la ruta por calles:", error);
            mostrarRutaAproximada(origen, destino);
        });
}

function crearUrlDeRuta(origen, destino) {
    // OSRM recibe longitud,latitud; Leaflet guarda las coordenadas como latitud,longitud.
    const coordenadas = `${origen[1]},${origen[0]};${destino[1]},${destino[0]}`;
    return `https://router.project-osrm.org/route/v1/driving/${coordenadas}?overview=full&geometries=geojson`;
}

function mostrarRutaPorCalles(geometria) {
    const ruta = L.geoJSON(geometria, {
        style: {
            color: colorRecorrido,
            weight: 5,
            opacity: 0.85
        }
    }).addTo(mapa);

    capasDelRecorrido.push(ruta);
    mapa.fitBounds(ruta.getBounds().pad(0.2), { maxZoom: 15 });
}

function mostrarRutaAproximada(origen, destino) {
    const lineaAproximada = L.polyline([origen, destino], {
        color: colorRecorrido,
        weight: 5,
        opacity: 0.85,
        dashArray: "8 8"
    })
        .addTo(mapa)
        .bindPopup("No se pudo calcular la ruta por calles. Esta línea es solo una referencia.");

    capasDelRecorrido.push(lineaAproximada);
    mapa.fitBounds(lineaAproximada.getBounds().pad(0.2), { maxZoom: 15 });
}

function limpiarCapasDelRecorrido() {
    capasDelRecorrido.forEach(capa => mapa.removeLayer(capa));
    capasDelRecorrido.length = 0;
}
