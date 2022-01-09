package com.gufli.brickdatabase.converters;

import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.StringReader;

@Converter(autoApply = true)
public class ItemStackConverter implements AttributeConverter<ItemStack, String> {

    @Override
    public String convertToDatabaseColumn(ItemStack attribute) {
        return attribute.toItemNBT().toSNBT();
    }

    @Override
    public ItemStack convertToEntityAttribute(String dbData) {
        try {
            return ItemStack.fromItemNBT((NBTCompound) new SNBTParser(new StringReader(dbData)).parse());
        } catch (NBTException e) {
            e.printStackTrace();
        }
        return null;
    }

}