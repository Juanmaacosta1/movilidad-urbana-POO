package md.ui;

import md.config.CargaParametros;
import md.ui.api.UIEngine;
import md.ui.impl.cli.CommandLineInterface;
import md.ui.impl.javafx.JavaFXUserInterface;

/** Fábrica de presentaciones: elige la UI según app.ui en config.properties. */
public final class UILoader {

	private UILoader() {
	}

	public static UIEngine createUIEngine() {
		return switch (CargaParametros.ui()) {
		case "javafx" -> new JavaFXUserInterface();
		case "cli" -> new CommandLineInterface();
		default -> throw new IllegalArgumentException("app.ui debe ser javafx o cli");
		};
	}
}
