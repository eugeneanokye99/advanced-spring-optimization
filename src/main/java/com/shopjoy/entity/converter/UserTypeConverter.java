package com.shopjoy.entity.converter;

import com.shopjoy.entity.UserType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserTypeConverter implements AttributeConverter<UserType, String> {

    @Override
    public String convertToDatabaseColumn(UserType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public UserType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return UserType.fromString(dbData);
    }
}
