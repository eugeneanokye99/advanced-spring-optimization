package com.shopjoy.entity.converter;

import com.shopjoy.entity.AddressType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AddressTypeConverter implements AttributeConverter<AddressType, String> {

    @Override
    public String convertToDatabaseColumn(AddressType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public AddressType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return AddressType.fromString(dbData);
    }
}
