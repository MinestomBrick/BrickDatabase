package com.gufli.brickutils.database.converters;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ComponentConverter implements AttributeConverter<Component, String> {

    @Override
    public String convertToDatabaseColumn(Component attribute) {
        return GsonComponentSerializer.gson().serialize(attribute);
    }

    @Override
    public Component convertToEntityAttribute(String dbData) {
        return GsonComponentSerializer.gson().deserialize(dbData);
    }
}