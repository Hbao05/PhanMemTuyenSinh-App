package util;

import entity.DoiTuong;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DoiTuongConverter implements AttributeConverter<DoiTuong, String> {

    @Override
    public String convertToDatabaseColumn(DoiTuong doiTuong) {
        return doiTuong != null ? doiTuong.getMa() : null;
    }

    @Override
    public DoiTuong convertToEntityAttribute(String ma) {
        return ma != null ? DoiTuong.fromMa(ma) : null;
    }
}
