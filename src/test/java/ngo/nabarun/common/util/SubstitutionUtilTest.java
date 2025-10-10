package ngo.nabarun.common.util;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SubstitutionUtilTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void testScalarReplacement() throws Exception {
		String json = "{\"greeting\":\"Hello {{name}}!\"}";
		Map<String, Object> context = Map.of("name", "Alice");

		JsonNode node = mapper.readTree(json);
		JsonNode substituted = SubstitutionUtil.substitute(node, context);

		assertEquals("Hello Alice!", substituted.get("greeting").asText());
	}

	@Test
	void testNestedReplacement() throws Exception {
		String json = "{\"user\":{\"message\":\"Welcome {{user.name}}!\"}}";
		Map<String, Object> user = Map.of("name", "Bob");
		Map<String, Object> context = Map.of("user", user);

		JsonNode node = mapper.readTree(json);
		JsonNode substituted = SubstitutionUtil.substitute(node, context);

		assertEquals("Welcome Bob!", substituted.get("user").get("message").asText());
	}

	@Test
	void testListReplacementEachBlock() throws Exception {
		String template = "{ \"itemsBlock\": \"{{#each items}}Item: {{this}}{{/each}}\" }";
		Map<String, Object> context = Map.of("items", List.of("A", "B", "C"));

		JsonNode node = mapper.readTree(template);
		JsonNode substituted = SubstitutionUtil.substitute(node, context);

		assertEquals("Item: AItem: BItem: C", substituted.get("itemsBlock").asText());
	}

	class User {
		String name;
		String email;
	}

	@Test
	void testNestedPojoReplacement() throws Exception {
		User user = new User();
		user.name = "Charlie";
		user.email = "charlie@example.com";

		String json = "{\"salutation\":\"Dear {{user.name}}, your email is {{user.email}}\"}";
		Map<String, Object> context = Map.of("user", user);

		JsonNode node = mapper.readTree(json);
		JsonNode substituted = SubstitutionUtil.substitute(node, context);

		assertEquals("Dear Charlie, your email is charlie@example.com", substituted.get("salutation").asText());
	}

	static class Greet {
		public String greeting;
	}

	@Test
	void testGenericReturnType() throws Exception {
		String json = "{\"greeting\":\"Hello {{name}}!\"}";
		Map<String, Object> context = Map.of("name", "Alice");

		Greet result = SubstitutionUtil.substitute(json, context, Greet.class);
		assertEquals("Hello Alice!", result.greeting);
	}

	@Test
	void testPlainTextSubstitution() {
		String template = "Welcome {{name}}!";
		Map<String, Object> context = Map.of("name", "Dana");

		String result = SubstitutionUtil.substitute(template, context, String.class);
		assertEquals("Welcome Dana!", result);
	}

	@Test
	void testMissingValueReplacement() throws Exception {
		String json = "{\"msg\":\"Hello {{unknown}}!\"}";
		Map<String, Object> context = Map.of("name", "Alice");

		JsonNode node = mapper.readTree(json);
		JsonNode substituted = SubstitutionUtil.substitute(node, context);

		// Should replace unknown with empty string
		assertEquals("Hello !", substituted.get("msg").asText());
	}

	@Test
	void testNullContext() throws Exception {
		String json = "{\"msg\":\"Hello {{name}}!\"}";

		JsonNode node = mapper.readTree(json);
		JsonNode substituted = SubstitutionUtil.substitute(node, null);

		// Should replace placeholders with empty string
		assertEquals("Hello !", substituted.get("msg").asText());
	}

	@Test
	void testEmptyListInEachBlock() throws Exception {
		String template = "{\"items\":\"{{#each items}}Item: {{this}}{{/each}}\"}";
		Map<String, Object> context = Map.of("items", Collections.emptyList());

		JsonNode node = mapper.readTree(template);
		JsonNode substituted = SubstitutionUtil.substitute(node, context);

		assertEquals("", substituted.get("items").asText());
	}

	@Test
	void testComplexNestedStructure() throws Exception {
		String json = """
				{
				  "body": {
				    "header": {"heading": "Hello {{user.name}}!"},
				    "content": {
				      "salutation": "Dear {{user.name}},",
				      "details": [
				        {"heading":"Info","fields":[{"name":"Email","value":"{{user.email}}"}]}
				      ]
				    }
				  }
				}
				""";
		Map<String, Object> user = Map.of("name", "Eve", "email", "eve@example.com");
		Map<String, Object> context = Map.of("user", user);

		JsonNode node = mapper.readTree(json);
		JsonNode substituted = SubstitutionUtil.substitute(node, context);

		assertEquals("Hello Eve!", substituted.get("body").get("header").get("heading").asText());
		assertEquals("Dear Eve,", substituted.get("body").get("content").get("salutation").asText());
		assertEquals("eve@example.com", substituted.get("body").get("content").get("details").get(0).get("fields")
				.get(0).get("value").asText());
	}

}