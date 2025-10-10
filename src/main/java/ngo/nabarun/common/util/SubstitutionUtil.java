package ngo.nabarun.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for performing placeholder substitutions in JSON objects or plain text templates.
 * <p>
 * Features:
 * <ul>
 *     <li>Supports scalar placeholder substitution, e.g., {{name}}</li>
 *     <li>Supports nested properties, e.g., {{user.name}}</li>
 *     <li>Supports list iteration with {{#each items}}...{{/each}}</li>
 *     <li>Works with JSON structures or plain text/HTML templates</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * Map<String, Object> data = Map.of(
 *     "user", Map.of("name", "Souvik", "email", "souvik@nabarun.org"),
 *     "org", "Nabarun NGO"
 * );
 *
 * String template = "Hello {{user.name}}, welcome to {{org}}!";
 * String result = JsonTemplateSubstitutor.replaceText(template, data);
 * System.out.println(result); // Output: Hello Souvik, welcome to Nabarun NGO!
 * }</pre>
 */
public class SubstitutionUtil {

	  private static final ObjectMapper mapper = new ObjectMapper();
	    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)}}");
	    private static final Pattern EACH_BLOCK_PATTERN = Pattern.compile("\\{\\{#each (.*?)}}([\\s\\S]*?)\\{\\{/each}}");

	    /**
	     * Substitute placeholders in a JSON node with values from a context POJO or Map.
	     *
	     * @param node  JSON node to process
	     * @param context POJO or Map containing values
	     * @return substituted JSON node
	     */
	    public static JsonNode substitute(JsonNode node, Object context) {
	        Map<String, Object> values = toMap(context);
	        if (node.isObject()) {
	            ObjectNode objectNode = (ObjectNode) node;
	            objectNode.properties().forEach(entry ->
	                objectNode.set(entry.getKey(), substitute(entry.getValue(), values))
	            );
	            return objectNode;
	        } else if (node.isArray()) {
	            ArrayNode arrayNode = (ArrayNode) node;
	            for (int i = 0; i < arrayNode.size(); i++) {
	                arrayNode.set(i, substitute(arrayNode.get(i), values));
	            }
	            return arrayNode;
	        } else if (node.isTextual()) {
	            return TextNode.valueOf(replaceText(node.asText(), values));
	        } else {
	            return node;
	        }
	    }

	    /**
	     * Substitute placeholders in a JSON string using a context POJO or Map and deserialize into a specific type.
	     *
	     * @param input   JSON string or plain text
	     * @param context POJO or Map containing values
	     * @param type    Type to deserialize output into
	     * @param <T>     Generic type
	     * @return substituted object of type T
	     */
	    public static <T> T substitute(String input, Object context, Class<T> type) {
	        try {
	            JsonNode node = mapper.readTree(input);
	            JsonNode substitutedNode = substitute(node, context);
	            return mapper.treeToValue(substitutedNode, type);
	        } catch (Exception e) {
	            // fallback for plain text
	            String replaced = replaceText(input, toMap(context));
	            if (type.equals(String.class)) {
	                return type.cast(replaced);
	            }
	            throw new RuntimeException("Failed to substitute template", e);
	        }
	    }

	    /**
	     * Replace placeholders in a text template.
	     *
	     * @param template text template
	     * @param data     Map containing values
	     * @return substituted string
	     */
	    public static String replaceText(String template, Map<String, Object> data) {
	        String result = template;

	        // handle each blocks
	        Matcher eachMatcher = EACH_BLOCK_PATTERN.matcher(result);
	        StringBuffer sb = new StringBuffer();
	        while (eachMatcher.find()) {
	            String key = eachMatcher.group(1).trim();
	            String block = eachMatcher.group(2);
	            Object value = resolvePath(key, data);

	            StringBuilder blockResult = new StringBuilder();
	            if (value instanceof List<?> list) {
	                for (Object item : list) {
	                    Map<String, Object> scoped = mergeContext(data, item);
	                    blockResult.append(replaceText(block, scoped));
	                }
	            }
	            eachMatcher.appendReplacement(sb, Matcher.quoteReplacement(blockResult.toString()));
	        }
	        eachMatcher.appendTail(sb);
	        result = sb.toString();

	        // handle scalar/nested placeholders
	        Matcher matcher = PLACEHOLDER_PATTERN.matcher(result);
	        sb = new StringBuffer();
	        while (matcher.find()) {
	            String key = matcher.group(1).trim();
	            Object value = resolvePath(key, data);
	            matcher.appendReplacement(sb, Matcher.quoteReplacement(value == null ? "" : value.toString()));
	        }
	        matcher.appendTail(sb);

	        return sb.toString();
	    }

	    private static Object resolvePath(String path, Map<String, Object> data) {
	        String[] parts = path.split("\\.");
	        Object current = data;
	        for (String part : parts) {
	            if (current instanceof Map<?, ?> map) {
	                current = map.get(part);
	            } else if (current != null) {
	                try {
	                    var field = current.getClass().getDeclaredField(part);
	                    field.setAccessible(true);
	                    current = field.get(current);
	                } catch (Exception e) {
	                    return null;
	                }
	            } else {
	                return null;
	            }
	        }
	        return current;
	    }

	    private static Map<String, Object> mergeContext(Map<String, Object> base, Object obj) {
	        Map<String, Object> copy = new HashMap<>(base);
	        if (obj == null) return copy;
	        if (obj instanceof Map<?, ?> mapObj) {
	            mapObj.forEach((k, v) -> copy.put(k.toString(), v));
	        } else {
	            Arrays.stream(obj.getClass().getDeclaredFields()).forEach(f -> {
	                try {
	                    f.setAccessible(true);
	                    copy.put(f.getName(), f.get(obj));
	                } catch (Exception ignored) {}
	            });
	        }
	        copy.put("this", obj);
	        return copy;
	    }

	    /**
	     * Converts a POJO to a Map using Jackson.
	     *
	     * @param obj POJO or Map
	     * @return Map representation
	     */
	    @SuppressWarnings(value = {"unchecked", "rawtypes"})
		private static Map<String, Object> toMap(Object obj) {
	        if (obj == null) return Collections.emptyMap();
	        if (obj instanceof Map<?, ?> map) {
				HashMap map1 =new HashMap<String, Object>();
	        	map1.putAll(map);
	        	return map1;
	        }
	        return mapper.convertValue(obj, Map.class);
	    }
}
