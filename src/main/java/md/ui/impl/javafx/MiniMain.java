package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.stage.Stage;
import md.rutas.impl.MotorRutas;

public class MiniMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // El mapa vial se importa en segundo plano: la ventana abre enseguida y, mientras no esté
        // listo (o si falta el .osm), la aplicación usa estimaciones en línea recta.
        MotorRutas.getInstance().iniciarCargaEnSegundoPlano();

        // La primera pantalla es la selección de usuario; desde ahí se entra a la aplicación
        Navegador.mostrarSeleccionUsuario(stage);
        stage.show();
    }

    @Override
    public void stop() {
        MotorRutas.getInstance().cerrar();
    }

    public static void main(String[] args) { launch(args); }
}
