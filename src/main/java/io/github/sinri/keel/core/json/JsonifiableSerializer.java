package io.github.sinri.keel.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.jackson.DatabindCodec;

import java.io.IOException;

/**
 * 针对 {@link UnmodifiableJsonifiableEntity} 适用类进行 Jackson Databind Serialization。
 * 应当在程序入口，{@link UnmodifiableJsonifiableEntity}相关类未曾使用之先，调用 {@link JsonifiableSerializer#register()}注册。
 *
 * @since 4.1.0
 */
public class JsonifiableSerializer extends JsonSerializer<UnmodifiableJsonifiableEntity> {
    public static void register() {
        // 注册序列化器
        DatabindCodec.mapper()
                     .registerModule(new SimpleModule()
                             .addSerializer(UnmodifiableJsonifiableEntity.class, new JsonifiableSerializer()));
    }

    //    public static void main(String[] args) {
    //        register();
    //        var t1=new T1(new JsonObject()
    //                .put("t1",1)
    //                .put("t2",new JsonObject()
    //                        .put("k","v")
    //                )
    //        );
    //        System.out.println(t1);
    //        System.out.println(t1.getT2());
    //        var j=new JsonObject().put("data",t1.getT2());
    //        System.out.println(j);
    //    }

    @Override
    public void serialize(UnmodifiableJsonifiableEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(value.toString());
        gen.writeTree(jsonNode);

        //        gen.writeStartObject();
        //        gen.writeNumberField("time", logItem.getTime());
        //
        //        if (logItem.getNanoPartOfTime() != null) {
        //            gen.writeNumberField("nanoPartOfTime", logItem.getNanoPartOfTime());
        //        }
        //
        //        gen.writeArrayFieldStart("contents");
        //        for (LogContent content : logItem.getContents()) {
        //            gen.writeObject(content);
        //        }
        //        gen.writeEndArray();
        //
        //        gen.writeEndObject();
    }


    //    private static class T1 extends UnmodifiableJsonifiableEntityImpl {
    //
    //        public T1(@Nonnull JsonObject jsonObject) {
    //            super(jsonObject);
    //        }
    //
    //        public T2 getT2(){
    //            return new T2(readJsonObject("t2"));
    //        }
    //
    //        public static class T2 extends UnmodifiableJsonifiableEntityImpl {
    //
    //            public T2(@Nonnull JsonObject jsonObject) {
    //                super(jsonObject);
    //            }
    //
    //
    //        }
    //    }
}
