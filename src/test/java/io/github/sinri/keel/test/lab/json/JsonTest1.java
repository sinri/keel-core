package io.github.sinri.keel.test.lab.json;

import io.vertx.core.json.Json;

public class JsonTest1 {
    public static void main(String[] args) {
        show("null");
        show("0");
        show("\"1\"");
        show("3.3");
        show("3.34f");
        show("[1,2]");
        show("{\"d\":\"f\"}");
    }

    private static void show(String jsonPart) {
        Object o = Json.decodeValue(jsonPart);

        String name;
        if (o == null) {
            name = null;
        } else {
            Class<?> aClass = o.getClass();
            name = aClass.getName();
        }
        System.out.println("`" + jsonPart + "` as " + name + ": " + o);
    }
}
