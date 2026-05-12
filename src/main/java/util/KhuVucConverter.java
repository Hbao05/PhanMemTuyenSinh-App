package util;
import entity.KhuVuc;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class KhuVucConverter implements AttributeConverter<KhuVuc, String> {

    @Override
    public String convertToDatabaseColumn(KhuVuc khuVuc) {
        if (khuVuc == null) {
            return null;
        }

        return khuVuc.getMa();
    }

    @Override
    public KhuVuc convertToEntityAttribute(String ma) {
        if (ma == null) {
            return null;
        }

        return KhuVuc.fromMa(ma);
    }
}
