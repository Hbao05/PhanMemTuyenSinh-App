package entity;

public enum DoiTuong {

    DT01("01", 2.0),
    DT02("02", 2.0),
    DT03("03", 2.0),
    DT04("04", 2.0),
    DT05("05", 2.0),

    DT06("06", 1.0),
    DT07("07", 1.0);

    private final String ma;
    private final double diemCong;

    DoiTuong(String ma, double diemCong) {
        this.ma = ma;
        this.diemCong = diemCong;
    }

    public String getMa() {
        return ma;
    }

    public double getDiemCong() {
        return diemCong;
    }

    public static DoiTuong fromMa(String ma) {
        for (DoiTuong dt : values()) {
            if (dt.ma.equals(ma) || dt.name().equalsIgnoreCase(ma)) {
                return dt;
            }
        }
        return null; // fallback to null instead of throwing exception to prevent DB fetch crash
    }
}
