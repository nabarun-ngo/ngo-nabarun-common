package ngo.nabarun.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTemplateHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.*?)}}");

    /**
     * Replaces variables in a JSON template string with values from the given context map.
     */
    public static String resolveTemplate(String jsonTemplate, Map<String, Object> context) {
        try {
            JsonNode node = MAPPER.readTree(jsonTemplate);
            JsonNode resolved = resolveNode(node, context);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(resolved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve JSON template", e);
        }
    }

    private static JsonNode resolveNode(JsonNode node, Map<String, Object> context) {
        if (node.isObject()) {
            ObjectNode result = MAPPER.createObjectNode();
            node.properties().forEach(entry -> result.set(entry.getKey(), resolveNode(entry.getValue(), context)));
            return result;
        } else if (node.isArray()) {
            ArrayNode array = MAPPER.createArrayNode();
            for (JsonNode element : node) {
                array.add(resolveNode(element, context));
            }
            return array;
        } else if (node.isTextual()) {
            String text = node.asText();
            return new TextNode(replaceVariablesInText(text, context));
        } else {
            return node;
        }
    }

    private static String replaceVariablesInText(String text, Map<String, Object> context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = getValueFromContext(key, context);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value != null ? value.toString() : ""));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Object getValueFromContext(String key, Map<String, Object> context) {
        String[] parts = key.split("\\.");
        Object current = context;
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
