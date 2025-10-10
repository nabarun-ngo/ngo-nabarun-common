package ngo.nabarun.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * General-purpose common utilities used across the application.
 *
 * <p>This class contains helpers for JSON conversion, null/default handling, simple collection
 * predicates, environment property resolution, and small I/O helpers.
 */
public class CommonUtil {
	private final static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Extract the file name portion from a URL string.
	 *
	 * @param url the URL string (e.g. "https://example.com/path/file.txt")
	 * @return the file name (e.g. "file.txt") or null if the url cannot be parsed
	 */
	public static String getURLToFileName(String url) {
		try {
			return Paths.get(new URI(url).getPath()).getFileName().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Convert a JSON string to a POJO of the provided class.
	 *
	 * @param json JSON string
	 * @param classz target class
	 * @param <T> target type
	 * @return deserialized object
	 * @throws Exception any parsing or mapping exception is propagated to the caller
	 */
	public static <T> T jsonToPojo(String json, Class<T> classz) throws Exception {
		return objectMapper.readValue(json, classz);
	}
	
	/**
	 * Convert a JSON string to a generic type using a TypeReference.
	 *
	 * @param json JSON string
	 * @param type TypeReference describing the target type
	 * @param <T> target type
	 * @return deserialized object
	 * @throws Exception any parsing or mapping exception is propagated to the caller
	 */
	public static <T> T jsonToPojo(String json, TypeReference<T> type) throws Exception {
		return objectMapper.readValue(json, type);
	}

	/**
	 * Return newValue if it is not null, otherwise return currentValue. Useful for applying
	 * defaults in a fluent manner.
	 *
	 * @param newValue candidate value
	 * @param currentValue fallback value
	 * @param <T> type
	 * @return newValue if non-null else currentValue
	 */
	public static <T> T defaultIfNull(T newValue, T currentValue) {
		return newValue != null ? newValue : currentValue;
	}

	/**
	 * Create a predicate that filters a stream to distinct elements by a key extractor.
	 *
	 * <p>The returned predicate is stateful and uses a concurrent Set to track seen keys.
	 * It is suitable for use with streams where elements are processed in a single JVM.
	 *
	 * @param keyExtractor function to extract the comparison key
	 * @param <T> element type
	 * @return predicate that returns true the first time a key is seen
	 */
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	/**
	 * Check that an object is not null. For Strings this also checks for non-empty trimmed value.
	 *
	 * @param obj object to check
	 * @return true if object is non-null and (for String) not blank
	 */
	public static boolean isNotNull(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof String) {
			String str = (String) obj;
			return str.trim().length() > 0;
		}
		return true;
	}

	/**
	 * Check that all provided objects are not null (and non-blank for Strings).
	 *
	 * @param objs objects to check
	 * @return true only if every object passes {@link #isNotNull(Object)}
	 */
	public static boolean isNotNull(Object... objs) {
		for (Object obj : objs) {
			if (!isNotNull(obj)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Convenience inverse of {@link #isNotNull(Object)} for strings.
	 *
	 * @param str input string
	 * @return true if the string is null or blank
	 */
	public static boolean isNullOrEmpty(String str) {
		return !isNotNull(str);
	}

	/**
	 * Read the contents of a URL into a byte array. Any IO errors during reading are logged to
	 * stderr and the method will attempt to close the stream.
	 *
	 * @param url the URL to read
	 * @return byte array of contents
	 * @throws IOException if an I/O error occurs while closing or writing the output stream
	 */
	public static byte[] toByteArray(URL url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = url.openStream();
			byte[] byteChunk = new byte[4096];
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				baos.write(byteChunk, 0, n);
			}
		} catch (IOException e) {
			System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
			e.printStackTrace();
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return baos.toByteArray();
	}

	/**
	 * Convert an object to a Map<String, Object> using Jackson's ObjectMapper conversion rules.
	 *
	 * @param object input object
	 * @return map representation of the object
	 */
	public static Map<String, Object> toMap(Object object) {
		return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {
		});
	}

	/**
	 * Convert an object to a generic type using a TypeReference. This method enables
	 * configuration to accept empty strings as null.
	 *
	 * @param object input object
	 * @param type target type reference
	 * @param <T> target type
	 * @return converted object
	 */
	public static <T> T convertToType(Object object, TypeReference<T> type) {
		private static final ObjectMapper EMPTY_STRING_MAPPER = objectMapper.copy().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

		// In the convertToType method:
		return EMPTY_STRING_MAPPER.convertValue(object, type);
	}

	/**
	 * Convert an object to the provided class type. Accepts empty strings as null during
	 * conversion.
	 *
	 * @param object input object
	 * @param type target class
		private static final ObjectMapper NULL_AS_EMPTY_MAPPER =
		        objectMapper.copy().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

		public static <T> T convertToType(Object object, Class<T> type) {
		    return NULL_AS_EMPTY_MAPPER.convertValue(object, type);
		}
		objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		return objectMapper.convertValue(object, type);
	}

	/**
	 * Convert an object to its JSON string representation.
	 *
	 * @param obj object to serialize
	 * @param pretty if true, produce pretty-printed JSON
	 * @return JSON string or the original object cast to String on serialization failure
	 */
	public static String toJSONString(Object obj, boolean pretty) {

		try {
			if (pretty) {
				return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
			}
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return (String) obj;
		}
	}

	/**
	 * Null-safe equality check between two objects.
	 *
	 * @param oldValue first value
	 * @param newValue second value
	 * @return true if values are equal (including both null)
	 */
	public static boolean areEqual(Object oldValue, Object newValue) {
		if (oldValue == null) {
			return newValue == null;
		} else {
			return oldValue.equals(newValue);
		}
	}

	/**
	 * Lookup an environment variable or system property. If neither is set return the default.
	 *
	 * @param key environment variable or system property name
	 * @param defaultValue fallback value
	 * @return resolved value or default
	 */
	public static String getEnvProperty(String key, String defaultValue) {
		String value = System.getenv(key) == null ? System.getProperty(key) : System.getenv(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Lookup an environment variable or system property returning null if not found.
	 *
	 * @param key property name
	 * @return resolved value or null
	 */
	public static String getEnvProperty(String key) {
		return getEnvProperty(key, null);
	}

	/**
	 * Return the shared ObjectMapper instance used by these utilities.
	 *
	 * @return shared ObjectMapper
	 */
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

}