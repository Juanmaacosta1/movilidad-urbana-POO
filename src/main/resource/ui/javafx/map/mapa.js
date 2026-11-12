const centroPuertoMadryn = [-42.7692, -65.0385];
const zoomInicial = 13;
const colorRecorrido = "#2563eb";

const mapa = L.map("map").setView(centroPuertoMadryn, zoomInicial);
const capasDelRecorrido = [];
let tipoDePuntoPendiente = null;
let numeroDeSolicitudDeRuta = 0;
let capaBase = null;

const URL_TESELAS = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
const MAX_REINTENTOS_FONDO = 3;
let reintentosFondo = 0;
let temporizadorReintento = null;

function cargarCapaBase() {
    capaBase = L.tileLayer(URL_TESELAS, {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        updateWhenIdle: false,
        keepBuffer: 4
    });

    // Un error de tesela NO debe reconstruir la capa: cada reconstrucción vuelve a pedir
    // todas las teselas y deja el fondo a medias. Se agenda un único reintento con redraw().
    capaBase.on("tileerror", programarReintentoDelFondo);
    capaBase.on("load", () => {
        reintentosFondo = 0;
    });

    mapa.addLayer(capaBase);
}

function programarReintentoDelFondo() {
    if (temporizadorReintento !== null || reintentosFondo >= MAX_REINTENTOS_FONDO) {
        return;
    }
    temporizadorReintento = window.setTimeout(() => {
        temporizadorReintento = null;
        reintentosFondo++;
        if (capaBase) {
            capaBase.redraw();
        }
    }, 4000);
}

cargarCapaBase();

const marcadorDePuertoMadryn = L.marker(centroPuertoMadryn)
    .addTo(mapa)
    .bindPopup("Puerto Madryn");

window.setModoSeleccion = function(tipoDePunto) {
    tipoDePuntoPendiente = tipoDePunto || null;
    mapa.getContainer().style.cursor = tipoDePuntoPendiente ? "crosshair" : "";
};

function recalcularTamano() {
    mapa.invalidateSize({ pan: false, animate: false });
}

// JavaFX redimensiona el WebView antes de que WebKit termine su layout interno:
// se recalcula de inmediato y otra vez un instante después, ya con el tamaño final.
window.ajustarTamanoMapa = function(ancho, alto) {
    // Java informa el tamaño real del WebView: no se depende de lo que mida WebKit por su cuenta.
    if (ancho > 0 && alto > 0) {
        const contenedor = mapa.getContainer();
        contenedor.style.width = ancho + "px";
        contenedor.style.height = alto + "px";
    }
    recalcularTamano();
    window.setTimeout(recalcularTamano, 100);
    window.setTimeout(recalcularTamano, 400);
};

window.diagnosticoMapa = function() {
    const contenedor = mapa.getContainer();
    return "Leaflet " + mapa.getSize().x + "x" + mapa.getSize().y
        + " | contenedor " + contenedor.clientWidth + "x" + contenedor.clientHeight
        + " | ventana " + window.innerWidth + "x" + window.innerHeight;
};

if (window.ResizeObserver) {
    new ResizeObserver(recalcularTamano).observe(mapa.getContainer());
}
window.addEventListener("resize", recalcularTamano);
window.addEventListener("load", () => {
    recalcularTamano();
    window.setTimeout(recalcularTamano, 300);
    window.setTimeout(recalcularTamano, 1000);
});

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

// Dibuja los dos tramos calculados por el motor de rutas (GraphHopper): acercamiento en azul
// y traslado en verde, más el conductor, el origen y el destino.
window.mostrarRutasViaje = function(datos) {
    limpiarCapasDelRecorrido();
    numeroDeSolicitudDeRuta++; // descarta una ruta de OSRM que todavía no haya llegado
    mapa.removeLayer(marcadorDePuertoMadryn);

    const limites = [];

    function dibujarTramo(puntos, color, descripcion) {
        if (!puntos || puntos.length < 2) {
            return;
        }
        const linea = L.polyline(puntos, { color: color, weight: 5, opacity: 0.9 })
            .addTo(mapa)
            .bindPopup(descripcion);
        capasDelRecorrido.push(linea);
        puntos.forEach(punto => limites.push(punto));
    }

    dibujarTramo(datos.acercamiento, "#1763c0", "Acercamiento: del conductor al origen");
    dibujarTramo(datos.traslado, "#149566", "Traslado: del origen al destino");

    if (datos.conductor) {
        const conductor = L.circleMarker(datos.conductor, {
            radius: 8, color: "#ffffff", weight: 2, fillColor: "#1763c0", fillOpacity: 1
        }).addTo(mapa).bindPopup("Conductor");
        capasDelRecorrido.push(conductor);
        limites.push(datos.conductor);
    }
    mostrarMarcador(datos.origen, "Punto de partida");
    mostrarMarcador(datos.destino, "Destino");

    if (limites.length > 1) {
        mapa.fitBounds(L.latLngBounds(limites).pad(0.15), { maxZoom: 16 });
    }
};

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