package com.gufli.brickdatabase.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Pos;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PosConverter implements AttributeConverter<Pos, String> {

    private final static JsonDeserializer<Pos> deserializer = (json, typeOfT, context) -> {
        JsonObject obj = json.getAsJsonObject();
        return new Pos(
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble(),
                obj.get("yaw").getAsFloat(),
                obj.get("pitch").getAsFloat()
        );
    };

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Pos.class, deserializer)
            .create();

    @Override
    public String convertToDatabaseColumn(Pos attribute) {
        return gson.toJson(attribute);
    }

    @Override
    public Pos convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData, Pos.class);
    }
}