import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
	Access values in the "config.properties" file.
*/
public class Config {

	private final static Properties props;
	static {
		try {
			// load 'dem prop's $%#&* !
			props = new Properties();
			props.load(Config.class.getResourceAsStream("config.properties"));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** No Object for you! */
	private Config() {}

	/** Gets the Object with the given key. */
	public static Object get(String key) {
		return props.get(key);
	}

	/** Gets the string with the given key. */
	public static String getString(String key) {
		return (String) get(key);
	}

	/** Gets the int with the given key. */
	public static int getInt(String key) {
		return (Integer) props.compute(key, (k,v) ->
			v instanceof Integer ? v : Integer.valueOf((String) v));
	}

	/** Gets the float with the given key. */
	public static float getFloat(String key) {
		return (Float) props.compute(key, (k,v) ->
			v instanceof Float ? v : Float.valueOf((String) v));
	}

	/** Gets the Color with the given key.
		Value must be a declared color constant on java.awt.Color
		@see java.awt.Color
	*/
	public static Color getColor(String key) {
		return (Color) props.compute(key, (k,v) ->
			v instanceof Color ? v : stringToColor((String) v));
	}

	/** Coverts a string description to a color.
		Currently only supports declared color constants. */
	private static Color stringToColor(String s) {
		try {
			Field f = Color.class.getDeclaredField(s.toUpperCase());
			return (Color) f.get(null);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
