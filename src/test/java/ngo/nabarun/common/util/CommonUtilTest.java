package ngo.nabarun.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CommonUtilTest {

    public static class Simple {
        public String name;
        public int value;
    }

    @Test
    public void testGetURLToFileName() {
        String url = "https://example.com/path/to/file.txt";
        String name = CommonUtil.getURLToFileName(url);
        assertEquals("file.txt", name);
    }

    @Test
    public void testJsonToPojoAndToMap() throws Exception {
        String json = "{\"name\":\"bob\",\"value\":42}";
        Simple s = CommonUtil.jsonToPojo(json, Simple.class);
        assertNotNull(s);
        assertEquals("bob", s.name);
        assertEquals(42, s.value);

        Map<String, Object> map = CommonUtil.toMap(s);
        assertEquals("bob", map.get("name"));
    }

    @Test
    public void testConvertToTypeWithTypeReference() {
        Map<String, Object> input = Map.of("a", 1, "b", 2);
        Map<String, Integer> converted = CommonUtil.convertToType(input, new TypeReference<>() {});
        assertEquals(1, converted.get("a"));
    }

    @Test
    public void testDefaultIfNullAndAreEqual() {
        String a = null;
        String b = "fallback";
        assertEquals("fallback", CommonUtil.defaultIfNull(a, b));
        assertTrue(CommonUtil.areEqual(null, null));
        assertFalse(CommonUtil.areEqual(null, "x"));
    }

    @Test
    public void testDistinctByKeyPredicate() {
        List<Map<String, Object>> list = List.of(
                Map.of("id", 1, "n", "a"),
                Map.of("id", 2, "n", "b"),
                Map.of("id", 1, "n", "c")
        );
        List<Map<String, Object>> distinct = list.stream()
                .filter(CommonUtil.distinctByKey(m -> m.get("id")))
                .collect(Collectors.toList());
        assertEquals(2, distinct.size());
    }

    @Test
    public void testIsNotNull() {
        assertTrue(CommonUtil.isNotNull("x"));
        assertFalse(CommonUtil.isNotNull("   "));
        assertTrue(CommonUtil.isNotNull(123));
    }

    @Test
    public void testToJSONString() {
        String s = CommonUtil.toJSONString(Map.of("k", "v"), true);
        assertTrue(s.contains("k") && s.contains("v"));
    }

    @Test
    public void testGetEnvProperty() {
        String val = CommonUtil.getEnvProperty("NON_EXISTENT_ENV_VAR", "def");
        assertEquals("def", val);
    }
}
