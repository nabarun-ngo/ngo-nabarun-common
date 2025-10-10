package ngo.nabarun.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.util.Map;
import java.util.regex.*;

/**
 * Utility class for performing placeholder substitution inside JSON templates and plain text.
 *
 * <p>This class supports substituting placeholders of the form "{{key}}" with values provided
 * in a Map. It can operate on a raw JSON string and will recursively walk the parsed JSON tree
 * to replace placeholders inside string values while preserving the JSON structure for non-text
 * nodes (objects, arrays, numbers, booleans, etc.). After substitution the JSON tree can be
 * converted into a target POJO type using Jackson's ObjectMapper.
 *
 * <p>Notes:
 * - The placeholder pattern is defined by {@link #PLACEHOLDER_PATTERN} and matches double
 *   curly-brace placeholders such as "{{name}}". Whitespace inside the braces is trimmed.
 * - If a placeholder key is not found in the provided variables map, it will be replaced with an
 *   empty string.
 * - The shared {@link #MAPPER} ObjectMapper instance is reused. Jackson's ObjectMapper is
 *   thread-safe for typical read-only operations performed here.
 *
 * Usage example:
 * <pre>
 * Map<String,String> vars = Map.of("name", "Alice");
 * String json = "{\"greeting\": \"Hello {{name}}\"}";
 * MyDto dto = SubstitutionUtil.substituteJson(json, vars, MyDto.class);
 * </pre>
 *
 * @see #substituteJson(String, Map, Class)
 * @see #substitute(String, Map)
 */
public final class SubstitutionUtil {

    /**
     * Shared ObjectMapper instance used for parsing and writing JSON.
     * Reusing a single ObjectMapper is recommended for performance.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Pattern that matches placeholders in the form {{key}}. The captured group contains the
     * placeholder key (leading/trailing whitespace is trimmed before lookup).
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

    private SubstitutionUtil() {}

    /**
     * Parse the provided JSON template, substitute placeholders using the provided variables map,
     * and map the resulting JSON tree to an instance of the requested class type.
     *
     * <p>All string values inside the JSON are scanned for placeholders and replaced. Non-string
     * nodes (objects, arrays, numbers, booleans, null) are preserved and only their string
     * children may be altered.
     *
     * @param jsonTemplate the JSON string containing placeholders (must be valid JSON)
     * @param vars the map of placeholder keys to replacement values; missing keys are treated as
     *             empty string
     * @param clazz the target class to map the substituted JSON to
     * @param <T> the target type
     * @return an instance of the target type populated from the substituted JSON
     * @throws RuntimeException if parsing, substitution or mapping fails
     */
    public static <T> T substituteJson(String jsonTemplate, Map<String, String> vars, Class<T> clazz) {
        try {
            JsonNode root = MAPPER.readTree(jsonTemplate);
            JsonNode replaced = replaceNode(root, vars);
            return MAPPER.treeToValue(replaced, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to substitute JSON template", e);
        }
    }

    /**
     * Recursively traverses a Jackson JsonNode tree and replaces placeholders in textual nodes.
     *
     * <p>Behavior:
     * - Text nodes: placeholders inside the text are substituted and a new TextNode is returned.
     * - Array nodes: each element is processed and added to a new ArrayNode.
     * - Object nodes: each field value is processed and put into a new ObjectNode with the same
     *   field names.
     * - Other node types (numbers, booleans, null): returned unchanged.
     *
     * @param node the node to process
     * @param vars the map of placeholder keys to replacement values
     * @return a JsonNode with placeholders replaced in textual content
     */
    private static JsonNode replaceNode(JsonNode node, Map<String, String> vars) {
        if (node.isTextual()) {
            return new TextNode(substitute(node.asText(), vars));
        } else if (node.isArray()) {
            ArrayNode array = MAPPER.createArrayNode();
            for (JsonNode element : node) {
                array.add(replaceNode(element, vars));
            }
            return array;
        } else if (node.isObject()) {
            ObjectNode obj = MAPPER.createObjectNode();
            node.properties().forEach(entry->{
                obj.set(entry.getKey(), replaceNode(entry.getValue(), vars));
            });
            return obj;
        } else {
            return node;
        }
    }

    /**
     * Substitute placeholders in the given input string using values from the vars map.
     *
     * <p>Placeholders are identified by the {@code {{key}}} syntax. The key is trimmed before
     * lookup. If a key is not present in the map, it is replaced with an empty string. Values are
     * safely quoted when performing regex-based replacements to avoid accidental interpretation of
     * replacement text as regex constructs.
     *
     * @param input the input string possibly containing placeholders
     * @param vars the map of placeholder keys to replacement values
     * @return the input string with all placeholders replaced
     */
    public static String substitute(String input, Map<String, String> vars) {
        if (input == null) {
            return null;
        }
        if (vars == null) {
            return input;
        }
    
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = vars.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}