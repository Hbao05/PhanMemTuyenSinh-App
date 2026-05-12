package entity;

public enum KhuVuc {
    DOI_TUONG_1("1", 1.0),
    DOI_TUONG_2NT("2NT", 0.75),
    DOI_TUONG_2("2", 0.5),
    DOI_TUONG_3("3", 0.0);

    private final String ma;
    private final double diemCong;

    KhuVuc(String ma, double diemCong) {
        this.ma = ma;
        this.diemCong = diemCong;
    }

    public String getMa() {
        return ma;
    }

    public double getDiemCong() {
        return diemCong;
    }

    public static KhuVuc fromMa(String ma) {
        for (KhuVuc dt : values()) {
            if (dt.ma.equals(ma)) {
                return dt;
            }
        }

        throw new IllegalArgumentException("Mã khu vực không hợp lệ: " + ma);
    }
}
