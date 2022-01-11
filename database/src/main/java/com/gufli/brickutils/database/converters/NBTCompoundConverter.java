package com.gufli.brickutils.database.converters;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

@Converter(autoApply = true)
public class NBTCompoundConverter implements AttributeConverter<NBTCompound, String> {

    @Override
    public String convertToDatabaseColumn(NBTCompound attribute) {
        return attribute.toSNBT();
    }

    @Override
    public NBTCompound convertToEntityAttribute(String dbData) {
        try ( Reader r = new StringReader(dbData); ) {
            return (NBTCompound) new SNBTParser(r).parse();
        } catch (IOException | NBTException e) {
            e.printStackTrace();
        }
        return null;
    }
}