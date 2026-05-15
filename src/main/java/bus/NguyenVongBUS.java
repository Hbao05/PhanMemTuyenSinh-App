package bus;

import dao.*;
import entity.*;

import java.util.*;
import java.util.stream.Collectors;

public class NguyenVongBUS {

    private static final int ROWS_PER_PAGE = 20;

    private final NguyenVongDAO   dao           = new NguyenVongDAO();
    private final ThiSinhDAO      thiSinhDAO    = new ThiSinhDAO();
    private final NganhDAO        nganhDAO      = new NganhDAO();
    private final NganhToHopDAO   nganhToHopDAO = new NganhToHopDAO();
    private final DiemThiDAO      diemThiDAO    = new DiemThiDAO();
    private final DiemCongDAO     diemCongDAO   = new DiemCongDAO();
    private final BangQuyDoiBUS   bqdBUS        = new BangQuyDoiBUS();

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<NguyenVongXetTuyen> getList(int page, String filterNganh, String filterKetQua) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.getPaginatedList(offset, ROWS_PER_PAGE, filterNganh, filterKetQua);
    }

    public int calculateTotalPages(String filterNganh, String filterKetQua) {
        long total = dao.countList(filterNganh, filterKetQua);
        return (int) Math.max(1, Math.ceil((double) total / ROWS_PER_PAGE));
    }

    public long getTotalCount(String filterNganh, String filterKetQua) {
        return dao.countList(filterNganh, filterKetQua);
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<NguyenVongXetTuyen> search(int page, String keyword) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.search(offset, ROWS_PER_PAGE, keyword);
    }

    public int calculateSearchTotalPages(String keyword) {
        long total = dao.countSearch(keyword);
        return (int) Math.max(1, Math.ceil((double) total / ROWS_PER_PAGE));
    }

    public long getSearchCount(String keyword) {
        return dao.countSearch(keyword);
    }

    // ── GET BY ID ─────────────────────────────────────────
    public NguyenVongXetTuyen getById(int id) { return dao.getById(id); }

    // ── GET BY CCCD (xem kết quả) ────────────────────────
    public List<NguyenVongXetTuyen> getByCccd(String cccd) { return dao.getByCccd(cccd); }

    // ── THÊM ─────────────────────────────────────────────
    public String addNguyenVong(NguyenVongXetTuyen nv) {
        String err = validate(nv, -1);
        if (err != null) return "Error: " + err;
        autoFill(nv);
        if (dao.existsByNvKeys(nv.getNvKeys())) return "Error: Nguyen vong nay (CCCD + Nganh) da ton tai!";
        return dao.insert(nv) ? "Success" : "Error: Khong the them vao co so du lieu!";
    }

    // ── SỬA ──────────────────────────────────────────────
    public String updateNguyenVong(NguyenVongXetTuyen nv) {
        String err = validate(nv, nv.getIdNv());
        if (err != null) return "Error: " + err;
        autoFill(nv);
        if (dao.existsByNvKeysExcludeId(nv.getNvKeys(), nv.getIdNv()))
            return "Error: Nguyen vong nay (CCCD + Nganh) da ton tai!";
        return dao.update(nv) ? "Success" : "Error: Khong the cap nhat co so du lieu!";
    }

    // ── XÓA ──────────────────────────────────────────────
    public String deleteNguyenVong(int id) {
        return dao.delete(id) ? "Success" : "Error: Khong the xoa ban ghi nay!";
    }

    // ── VALIDATION ────────────────────────────────────────
    private String validate(NguyenVongXetTuyen nv, int excludeId) {
        String cccd = nv.getCccd() != null ? nv.getCccd().trim() : "";
        if (cccd.isEmpty()) return "CCCD khong duoc de trong!";
        if (!thiSinhDAO.checkCccdExists(cccd)) return "CCCD \"" + cccd + "\" khong ton tai!";

        String maNganh = nv.getMaNganh() != null ? nv.getMaNganh().trim() : "";
        if (maNganh.isEmpty()) return "Ma nganh khong duoc de trong!";
        if (!nganhDAO.existsByMaNganh(maNganh)) return "Ma nganh \"" + maNganh + "\" khong ton tai!";

        if (nv.getThuTuNguyenVong() <= 0) return "Thu tu nguyen vong phai lon hon 0!";
        if (nv.getPhuongThuc() == null || nv.getPhuongThuc().isBlank())
            return "Phuong thuc khong duoc de trong!";

        return null;
    }

    private void autoFill(NguyenVongXetTuyen nv) {
        nv.setCccd(nv.getCccd().trim());
        nv.setMaNganh(nv.getMaNganh().trim());
        nv.setNvKeys(nv.getCccd() + "|" + nv.getMaNganh());
    }

    // ── CHẠY XÉT TUYỂN ───────────────────────────────────
    /**
     * Tải toàn bộ dữ liệu cần thiết, chạy engine, lưu kết quả về DB.
     * callback.update(done, total) được gọi sau mỗi NV xử lý.
     */
    public String runXetTuyen(XetTuyenEngine.ProgressCallback callback) {
        try {
            List<NguyenVongXetTuyen> allNV = dao.getAll();
            if (allNV.isEmpty()) return "Error: Khong co nguyen vong nao de xet!";

            // Load dữ liệu hỗ trợ
            Map<String, DiemThiXetTuyen>  diemThiMap  = buildDiemThiMap(allNV);
            Map<String, NganhToHop>       nthMap      = buildNganhToHopMap(allNV);
            Map<String, DiemCongXetTuyen> dcMap       = buildDiemCongMap(allNV);
            Map<String, ThiSinh>          tsMap       = buildThiSinhMap(allNV);
            Map<String, Nganh>            nganhMap    = buildNganhMap(allNV);
            List<BangQuyDoi>              bqdList     = bqdBUS.getAll();

            List<NguyenVongXetTuyen> result = XetTuyenEngine.runAll(
                    allNV, diemThiMap, nthMap, dcMap, tsMap, nganhMap, bqdList, callback);

            boolean ok = dao.batchUpdate(result);
            if (!ok) return "Error: Loi khi luu ket qua xuong DB!";

            long trungTuyen = result.stream()
                    .filter(nv -> XetTuyenEngine.KQ_TRUNG_TUYEN.equals(nv.getKetQua())).count();
            long truot = result.stream()
                    .filter(nv -> XetTuyenEngine.KQ_TRUOT_NV.equals(nv.getKetQua())
                               || XetTuyenEngine.KQ_TRUOT_NGANH.equals(nv.getKetQua())).count();
            return "Success|Tong NV: " + result.size()
                 + " | Trung tuyen: " + trungTuyen + " | Truot: " + truot;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // ── LOAD MAP HELPERS ─────────────────────────────────
    private Map<String, DiemThiXetTuyen> buildDiemThiMap(List<NguyenVongXetTuyen> nvList) {
        Set<String> cccdSet = nvList.stream().map(NguyenVongXetTuyen::getCccd).collect(Collectors.toSet());
        // Load tất cả DiemThi và lọc theo cccd
        List<DiemThiXetTuyen> all = diemThiDAO.getPaginatedList(0, Integer.MAX_VALUE);
        return all.stream()
                .filter(dt -> cccdSet.contains(dt.getCccd()))
                .collect(Collectors.toMap(DiemThiXetTuyen::getCccd, dt -> dt, (a, b) -> a));
    }

    private Map<String, NganhToHop> buildNganhToHopMap(List<NguyenVongXetTuyen> nvList) {
        // key = maNganh|maToHop
        List<NganhToHop> all = nganhToHopDAO.getAll();
        if (all == null) return Map.of();
        return all.stream()
                .collect(Collectors.toMap(
                        nth -> nth.getMaNganh() + "|" + nth.getMaToHop(),
                        nth -> nth, (a, b) -> a));
    }

    private Map<String, DiemCongXetTuyen> buildDiemCongMap(List<NguyenVongXetTuyen> nvList) {
        // key = cccd|maNganh|maToHop|phuongThuc → matches DiemCongXetTuyen.dcKeys format
        List<DiemCongXetTuyen> all = diemCongDAO.getPaginatedList(0, Integer.MAX_VALUE);
        Map<String, DiemCongXetTuyen> map = new HashMap<>();
        for (DiemCongXetTuyen dc : all) {
            if (dc.getDcKeys() != null) map.put(dc.getDcKeys(), dc);
        }
        return map;
    }

    private Map<String, ThiSinh> buildThiSinhMap(List<NguyenVongXetTuyen> nvList) {
        Set<String> cccdSet = nvList.stream().map(NguyenVongXetTuyen::getCccd).collect(Collectors.toSet());
        Map<String, ThiSinh> map = new HashMap<>();
        for (String cccd : cccdSet) {
            ThiSinh ts = thiSinhDAO.getByCccd(cccd);
            if (ts != null) map.put(cccd, ts);
        }
        return map;
    }

    private Map<String, Nganh> buildNganhMap(List<NguyenVongXetTuyen> nvList) {
        List<Nganh> all = nganhDAO.getAll();
        if (all == null) return Map.of();
        return all.stream().collect(Collectors.toMap(Nganh::getMaNganh, n -> n, (a, b) -> a));
    }
}
