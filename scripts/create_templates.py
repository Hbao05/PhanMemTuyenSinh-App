"""
Tạo các file Excel template chuẩn cho phần mềm tuyển sinh.
Mỗi template gồm:
  - Sheet 1: Dữ liệu  (dòng tiêu đề + header + 2 dòng mẫu)
  - Sheet 2: Hướng dẫn (bảng mô tả từng cột)
"""

import os
from openpyxl import Workbook
from openpyxl.styles import (
    PatternFill, Font, Alignment, Border, Side, Protection
)
from openpyxl.utils import get_column_letter

# ── Màu sắc ──────────────────────────────────────────────
C_HEADER    = "1A3C5E"   # Xanh đậm  – tiêu đề tổng
C_SUBHDR    = "D0E4F5"   # Xanh nhạt – header cột
C_REQUIRED  = "C0392B"   # Đỏ        – cột bắt buộc (chữ)
C_SAMPLE    = "F5F5F5"   # Xám nhạt  – nền dòng mẫu
C_SAMPLE_F  = "7F8C8D"   # Xám đậm   – chữ dòng mẫu
C_NOTE_BG   = "FFF9E6"   # Vàng nhạt – nền hướng dẫn
C_WHITE     = "FFFFFF"
C_BLACK     = "000000"

OUT_DIR = os.path.join("src", "main", "resources", "templates")
os.makedirs(OUT_DIR, exist_ok=True)


# ── Helper styles ─────────────────────────────────────────
def fill(hex_color):
    return PatternFill("solid", fgColor=hex_color)

def border_thin():
    s = Side(style="thin", color="CCCCCC")
    return Border(left=s, right=s, top=s, bottom=s)

def border_medium_bottom():
    thin  = Side(style="thin",   color="CCCCCC")
    med   = Side(style="medium", color="1A3C5E")
    return Border(left=thin, right=thin, top=thin, bottom=med)

def center():
    return Alignment(horizontal="center", vertical="center", wrap_text=False)

def left_middle():
    return Alignment(horizontal="left", vertical="center", wrap_text=True)


def write_title_row(ws, text, col_count, row=1):
    """Dòng tiêu đề tổng – nền xanh đậm, chữ trắng đậm."""
    ws.row_dimensions[row].height = 28
    cell = ws.cell(row=row, column=1, value=text)
    cell.font      = Font(name="Segoe UI", bold=True, size=13, color=C_WHITE)
    cell.fill      = fill(C_HEADER)
    cell.alignment = Alignment(horizontal="center", vertical="center")
    ws.merge_cells(start_row=row, start_column=1,
                   end_row=row,   end_column=col_count)


def write_header_row(ws, headers, required_flags, row=2):
    """Dòng header cột – xanh nhạt; cột bắt buộc chữ đỏ."""
    ws.row_dimensions[row].height = 22
    for col_idx, (hdr, req) in enumerate(zip(headers, required_flags), start=1):
        cell = ws.cell(row=row, column=col_idx, value=hdr)
        cell.fill      = fill(C_SUBHDR)
        cell.font      = Font(name="Segoe UI", bold=True, size=11,
                              color=(C_REQUIRED if req else C_BLACK))
        cell.alignment = center()
        cell.border    = border_medium_bottom()


def write_sample_rows(ws, samples, start_row=3):
    """Các dòng mẫu – nền xám nhạt, chữ xám, in nghiêng."""
    for r_offset, row_data in enumerate(samples):
        r = start_row + r_offset
        ws.row_dimensions[r].height = 18
        for col_idx, val in enumerate(row_data, start=1):
            cell = ws.cell(row=r, column=col_idx, value=val)
            cell.font      = Font(name="Segoe UI", italic=True, size=11, color=C_SAMPLE_F)
            cell.fill      = fill(C_SAMPLE)
            cell.alignment = left_middle()
            cell.border    = border_thin()


def write_guide_sheet(ws, guide_data):
    """Sheet hướng dẫn: bảng 3 cột (Tên cột | Bắt buộc | Mô tả)."""
    # Tiêu đề
    ws.row_dimensions[1].height = 24
    t = ws.cell(row=1, column=1, value="HƯỚNG DẪN SỬ DỤNG TEMPLATE")
    t.font      = Font(name="Segoe UI", bold=True, size=13, color=C_WHITE)
    t.fill      = fill(C_HEADER)
    t.alignment = Alignment(horizontal="center", vertical="center")
    ws.merge_cells("A1:C1")

    # Header
    ws.row_dimensions[2].height = 20
    for col, hdr in enumerate(["Tên cột", "Bắt buộc", "Mô tả / Định dạng"], start=1):
        c = ws.cell(row=2, column=col, value=hdr)
        c.font      = Font(name="Segoe UI", bold=True, size=11, color=C_BLACK)
        c.fill      = fill(C_SUBHDR)
        c.alignment = center()
        c.border    = border_medium_bottom()

    # Nội dung
    for i, (col_name, req, desc) in enumerate(guide_data, start=3):
        ws.row_dimensions[i].height = 18
        for j, val in enumerate([col_name, req, desc], start=1):
            c = ws.cell(row=i, column=j, value=val)
            c.font      = Font(name="Segoe UI", size=11)
            c.fill      = fill(C_NOTE_BG)
            c.alignment = left_middle()
            c.border    = border_thin()

    ws.column_dimensions["A"].width = 22
    ws.column_dimensions["B"].width = 12
    ws.column_dimensions["C"].width = 60


def auto_width(ws, headers, extra=4):
    for i, h in enumerate(headers, start=1):
        col = get_column_letter(i)
        ws.column_dimensions[col].width = max(len(str(h)) + extra, 14)


# ═══════════════════════════════════════════════════════════
# TEMPLATE 1: NGÀNH ĐÀO TẠO
# ═══════════════════════════════════════════════════════════
def create_nganh_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "DANH SACH NGANH"
    ws.sheet_view.showGridLines = True

    headers  = ["maNganh (*)", "tenNganh (*)", "toHopGoc",
                "chiTieu (*)", "diemSan",
                "tuyenThang (Y/N)", "dgnl (Y/N)", "thpt (Y/N)", "vsat (Y/N)"]
    required = [True, True, False, True, False, False, False, False, False]
    samples  = [
        ["7480101", "Khoa học máy tính",    "A00, A01, D07", "120", "15.0", "Y", "Y", "Y", "N"],
        ["7480201", "Công nghệ thông tin",  "A00, A01, D01", "200", "16.5", "N", "Y", "Y", "Y"],
        ["7340301", "Kế toán",             "A00, A01, D01", "150", "14.0", "N", "N", "Y", "N"],
    ]

    write_title_row(ws,
        "TEMPLATE NGÀNH ĐÀO TẠO  |  Xóa dòng này + các dòng mẫu (xám) trước khi Import",
        len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)

    # Freeze header
    ws.freeze_panes = "A3"

    # Sheet hướng dẫn
    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [
        ("maNganh",        "Bắt buộc",   "Mã ngành 7 chữ số theo chuẩn Bộ GD&ĐT. VD: 7480101"),
        ("tenNganh",       "Bắt buộc",   "Tên ngành đầy đủ. VD: Khoa học máy tính"),
        ("toHopGoc",       "Không",       "Các tổ hợp xét tuyển, phân cách bằng dấu phẩy. VD: A00, A01, D07"),
        ("chiTieu",        "Bắt buộc",   "Số nguyên >= 0. VD: 120"),
        ("diemSan",        "Không",       "Điểm sàn (số thực). VD: 15.5 — để trống nếu chưa có"),
        ("tuyenThang",     "Không",       "Ngành có tuyển thẳng? Gõ chữ Y. Để trống = Không"),
        ("dgnl",           "Không",       "Xét điểm ĐGNL? Gõ Y hoặc để trống"),
        ("thpt",           "Không",       "Xét điểm thi THPT? Gõ Y hoặc để trống"),
        ("vsat",           "Không",       "Xét điểm V-SAT? Gõ Y hoặc để trống"),
        ("⚠ LƯU Ý",       "—",           "Xóa toàn bộ dòng chữ Xám (dòng mẫu) và dòng tiêu đề màu xanh đậm trước khi nhấn Import. Chỉ giữ lại dòng Header (màu xanh nhạt) và dữ liệu thật."),
    ])

    path = os.path.join(OUT_DIR, "template_nganh.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")


# ═══════════════════════════════════════════════════════════
# TEMPLATE 2: THÍ SINH
# ═══════════════════════════════════════════════════════════
def create_thisinh_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "DANH SACH THI SINH"

    headers  = ["cccd (*)", "soBaoDanh", "ho", "ten (*)",
                "ngaySinh (dd/MM/yyyy)", "gioiTinh (Nam/Nữ/Khác)",
                "doiTuong", "khuVuc", "dienThoai", "email"]
    required = [True, False, False, True, False, False, False, False, False, False]
    samples  = [
        ["001234567890", "TS00001", "Nguyễn Văn", "An",
         "01/01/2007", "Nam",  "01",  "3",   "0901234567", "an.nv@email.com"],
        ["098765432100", "TS00002", "Trần Thị",   "Bình",
         "15/06/2007", "Nữ",  "07",  "2NT", "0912345678", "binh.tt@email.com"],
        ["012345678901", "TS00003", "Lê Hoàng",   "Cường",
         "20/03/2006", "Nam",  "",    "1",   "",           ""],
    ]

    write_title_row(ws,
        "TEMPLATE DANH SÁCH THÍ SINH  |  Xóa dòng này + các dòng mẫu (xám) trước khi Import",
        len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)
    ws.freeze_panes = "A3"

    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [
        ("cccd",          "Bắt buộc",  "Số CCCD 12 chữ số (không có dấu cách). VD: 001234567890"),
        ("soBaoDanh",     "Không",      "Số báo danh thi THPT. VD: TS00001"),
        ("ho",            "Không",      "Họ và tên đệm. VD: Nguyễn Văn"),
        ("ten",           "Bắt buộc",  "Tên (chữ cuối). VD: An"),
        ("ngaySinh",      "Không",      "Định dạng dd/MM/yyyy. VD: 01/01/2007"),
        ("gioiTinh",      "Không",      "Nam | Nữ | Khác"),
        ("doiTuong",      "Không",      "Mã đối tượng ưu tiên theo quy chế. VD: 01, 02, 07"),
        ("khuVuc",        "Không",      "Khu vực ưu tiên. VD: 1, 2, 3, 2NT"),
        ("dienThoai",     "Không",      "Số điện thoại 10 chữ số. VD: 0901234567"),
        ("email",         "Không",      "Địa chỉ email. VD: an@email.com"),
        ("⚠ LƯU Ý",      "—",          "Xóa toàn bộ dòng chữ Xám (mẫu) và dòng tiêu đề màu xanh đậm trước khi Import."),
    ])

    path = os.path.join(OUT_DIR, "template_thisinh.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")


# ═══════════════════════════════════════════════════════════
# TEMPLATE 3: TỔ HỢP MÔN
# ═══════════════════════════════════════════════════════════
def create_tohopmon_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "DANH SACH TO HOP"

    headers  = ["maToHop (*)", "tenToHop (*)", "mon1 (*)", "mon2 (*)", "mon3 (*)"]
    required = [True, True, True, True, True]
    samples  = [
        ["A00", "Toán - Vật lý - Hóa học",        "Toán", "Vật lý",  "Hóa học"],
        ["A01", "Toán - Vật lý - Tiếng Anh",       "Toán", "Vật lý",  "Tiếng Anh"],
        ["B00", "Toán - Hóa học - Sinh học",        "Toán", "Hóa học", "Sinh học"],
        ["C00", "Ngữ văn - Lịch sử - Địa lý",      "Ngữ văn", "Lịch sử", "Địa lý"],
        ["D01", "Toán - Ngữ văn - Tiếng Anh",      "Toán", "Ngữ văn", "Tiếng Anh"],
        ["D07", "Toán - Hóa học - Tiếng Anh",      "Toán", "Hóa học", "Tiếng Anh"],
    ]

    write_title_row(ws,
        "TEMPLATE TỔ HỢP MÔN XÉT TUYỂN  |  Xóa dòng này + dòng mẫu trước khi Import",
        len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)
    ws.freeze_panes = "A3"

    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [
        ("maToHop",   "Bắt buộc", "Mã tổ hợp theo chuẩn Bộ GD. VD: A00, D07, C00"),
        ("tenToHop",  "Bắt buộc", "Tên đầy đủ tổ hợp. VD: Toán - Vật lý - Hóa học"),
        ("mon1",      "Bắt buộc", "Tên môn thứ nhất. VD: Toán"),
        ("mon2",      "Bắt buộc", "Tên môn thứ hai. VD: Vật lý"),
        ("mon3",      "Bắt buộc", "Tên môn thứ ba. VD: Hóa học"),
        ("⚠ LƯU Ý",  "—",        "Xóa dòng tiêu đề và dòng mẫu màu xám trước khi Import."),
    ])

    path = os.path.join(OUT_DIR, "template_tohopmon.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")


# ═══════════════════════════════════════════════════════════
# TEMPLATE 4: ĐIỂM THI
# ═══════════════════════════════════════════════════════════
def create_diemthi_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "DANH SACH DIEM THI"
    headers  = ["cccd (*)", "soBaoDanh", "phuongThuc", "diemToan", "diemLy", "diemHoa", "diemSinh", "diemSu", "diemDia", "diemVan", "n1Thi", "n1Cc", "cncn", "cnnn", "diemTiengAnh", "diemKtpl", "nl1", "nk1", "nk2"]
    required = [True, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False]
    samples  = [
        ["001234567890", "TS00001", "THPT", "8.5", "7.0", "9.0", "", "", "", "", "", "", "", "", "8.0", "", "", "", ""],
        ["098765432100", "TS00002", "DGNL", "", "", "", "", "", "", "", "", "", "", "", "", "", "850", "", ""]
    ]
    write_title_row(ws, "TEMPLATE ĐIỂM THI  |  Xóa dòng này + dòng mẫu trước khi Import", len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)
    ws.freeze_panes = "A3"
    
    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [("cccd", "Bắt buộc", "CCCD Thí sinh")])
    path = os.path.join(OUT_DIR, "template_diemthi.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")

# ═══════════════════════════════════════════════════════════
# TEMPLATE 5: NGUYỆN VỌNG
# ═══════════════════════════════════════════════════════════
def create_nguyenvong_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "DANH SACH NGUYEN VONG"
    headers  = ["cccd (*)", "maNganh (*)", "thuTuNguyenVong (*)", "diemThxt", "diemUtqd", "diemCong", "diemXetTuyen", "ketQua", "phuongThuc", "toHopMon"]
    required = [True, True, True, False, False, False, False, False, False, False]
    samples  = [
        ["001234567890", "7480101", "1", "24.5", "0", "0", "24.5", "Đỗ", "THPT", "A00"],
        ["001234567890", "7480201", "2", "23.0", "0", "0", "23.0", "Trượt", "THPT", "A01"]
    ]
    write_title_row(ws, "TEMPLATE NGUYỆN VỌNG  |  Xóa dòng này + dòng mẫu trước khi Import", len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)
    ws.freeze_panes = "A3"
    
    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [("cccd", "Bắt buộc", "CCCD Thí sinh")])
    path = os.path.join(OUT_DIR, "template_nguyenvong.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")

# ═══════════════════════════════════════════════════════════
# TEMPLATE 6: ĐIỂM CỘNG
# ═══════════════════════════════════════════════════════════
def create_diemcong_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "DANH SACH DIEM CONG"
    headers  = ["cccd (*)", "maNganh", "maToHop", "phuongThuc", "diemCc", "diemUtXt", "diemTong", "ghiChu"]
    required = [True, False, False, False, False, False, False, False]
    samples  = [
        ["001234567890", "7480101", "A00", "THPT", "1.5", "0.5", "2.0", "Chứng chỉ IELTS"],
    ]
    write_title_row(ws, "TEMPLATE ĐIỂM CỘNG  |  Xóa dòng này + dòng mẫu trước khi Import", len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)
    ws.freeze_panes = "A3"
    
    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [("cccd", "Bắt buộc", "CCCD Thí sinh")])
    path = os.path.join(OUT_DIR, "template_diemcong.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")

# ═══════════════════════════════════════════════════════════
# TEMPLATE 7: NGÀNH - TỔ HỢP
# ═══════════════════════════════════════════════════════════
def create_nganhtohop_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "NGANH - TO HOP"
    headers  = ["maNganh (*)", "maToHop (*)", "thMon1", "hsMon1", "thMon2", "hsMon2", "thMon3", "hsMon3", "doLech", "N1", "TO", "LI", "HO", "SI", "VA", "SU", "DI", "TI", "KHAC", "KTPL"]
    required = [True, True, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False, False]
    samples  = [
        ["7480101", "A00", "Toán", "2", "Vật lý", "1", "Hóa học", "1", "0", "0", "1", "1", "1", "0", "0", "0", "0", "0", "0", "0"],
    ]
    write_title_row(ws, "TEMPLATE NGÀNH - TỔ HỢP  |  Xóa dòng này + dòng mẫu trước khi Import", len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)
    ws.freeze_panes = "A3"
    
    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [("maNganh", "Bắt buộc", "Mã ngành")])
    path = os.path.join(OUT_DIR, "template_nganhtohop.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")

# ═══════════════════════════════════════════════════════════
# TEMPLATE 8: BẢNG QUY ĐỔI
# ═══════════════════════════════════════════════════════════
def create_bangquydoi_template():
    wb = Workbook()
    ws = wb.active
    ws.title = "BANG QUY DOI"
    headers  = ["maQuyDoi (*)", "phuongThuc", "toHop", "mon", "diemA", "diemB", "diemC", "diemD", "phanVi"]
    required = [True, False, False, False, False, False, False, False, False]
    samples  = [
        ["QD001", "DGNL", "A00", "Toán", "100", "90", "80", "70", "P90"],
    ]
    write_title_row(ws, "TEMPLATE BẢNG QUY ĐỔI  |  Xóa dòng này + dòng mẫu trước khi Import", len(headers))
    write_header_row(ws, headers, required)
    write_sample_rows(ws, samples)
    auto_width(ws, headers)
    ws.freeze_panes = "A3"
    
    wg = wb.create_sheet("HUONG DAN")
    write_guide_sheet(wg, [("maQuyDoi", "Bắt buộc", "Mã quy đổi")])
    path = os.path.join(OUT_DIR, "template_bangquydoi.xlsx")
    wb.save(path)
    print(f"  ✔ {path}")

# ═══════════════════════════════════════════════════════════
# MAIN
# ═══════════════════════════════════════════════════════════
if __name__ == "__main__":
    print("Đang tạo các file template Excel...")
    # create_nganh_template()
    # create_thisinh_template()
    # create_tohopmon_template()
    try:
        create_diemthi_template()
        create_nguyenvong_template()
        create_diemcong_template()
        create_nganhtohop_template()
        create_bangquydoi_template()
    except Exception as e:
        print(f"Lỗi: {e}")
    print("\nHoàn tất! Các file được lưu tại:", os.path.abspath(OUT_DIR))

