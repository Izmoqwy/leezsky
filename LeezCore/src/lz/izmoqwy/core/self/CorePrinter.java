package lz.izmoqwy.core.self;

import org.bukkit.Bukkit;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CorePrinter {

	private static Logger logger = Bukkit.getLogger();

	public static void print(String message, Object... arguments) {
		logger.info("> " + MessageFormat.format(message, arguments));
	}

	public static void warn(String message, Object... arguments) {
		logger.warning("> " + MessageFormat.format(message, arguments));
	}

	public static void err(String message, Object... arguments) {
		logger.severe(">"  + MessageFormat.format(message, arguments));
	}

	public static void write(String message, Level level, Object... arguments) {
		logger.log(level, "> " + MessageFormat.format(message, arguments));
	}

}
