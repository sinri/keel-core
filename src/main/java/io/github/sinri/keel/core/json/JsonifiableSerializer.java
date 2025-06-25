package io.github.sinri.keel.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
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

    @Override
    public void serialize(UnmodifiableJsonifiableEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeRaw(value.toString());

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
}
