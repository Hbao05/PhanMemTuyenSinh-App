import pandas as pd
import openpyxl
import os
import warnings

# Bỏ qua các cảnh báo openpyxl
warnings.filterwarnings('ignore', category=UserWarning, module='openpyxl')

def migrate_nganh():
    print("⏳ Đang xử lý dữ liệu NGÀNH...")
    # 1. Đọc Chỉ tiêu (dòng 0 là title, dòng 1 là header)
    df_chitieu = pd.read_excel('docs/Chi tieu 2025.xlsx', header=1)
    df_chitieu.columns = df_chitieu.columns.str.strip()
    df_chitieu = df_chitieu.rename(columns={
        'MÃ CTĐT': 'maNganh', 
        'TÊN CTĐT': 'tenNganh', 
        'Chỉ tiêu chốt': 'chiTieu'
    })
    df_chitieu['maNganh'] = df_chitieu['maNganh'].astype(str)

    # 2. Đọc Ngưỡng đầu vào
    df_nguong = pd.read_excel('docs/Nguong dau vao 2025.xlsx')
    df_nguong.columns = df_nguong.columns.str.strip()
    df_nguong = df_nguong.rename(columns={
        'Mã xét tuyển': 'maNganh', 
        'Ngưỡng đầu vào': 'diemSan'
    })
    df_nguong['maNganh'] = df_nguong['maNganh'].astype(str)

    # 3. Đọc Tổ hợp gốc
    df_tohop = pd.read_excel('docs/tohopmon.xlsx')
    df_tohop.columns = df_tohop.columns.str.strip()
    df_tohop['MANGANH'] = df_tohop['MANGANH'].astype(str)
    # Gom nhóm theo MANGANH, nối các TEN_TO_HOP lại với nhau bằng dấu phẩy
    df_tohop_agg = df_tohop.groupby('MANGANH')['TEN_TO_HOP'].apply(lambda x: ', '.join(x.dropna().unique())).reset_index()
    df_tohop_agg = df_tohop_agg.rename(columns={'MANGANH': 'maNganh', 'TEN_TO_HOP': 'toHopGoc'})

    # 4. Gộp (Merge) dữ liệu theo Mã ngành
    df_nganh = pd.merge(df_chitieu, df_nguong[['maNganh', 'diemSan']], on='maNganh', how='left')
    df_nganh = pd.merge(df_nganh, df_tohop_agg, on='maNganh', how='left')

    # 5. Điền thông tin phương thức xét tuyển (Mặc định cho Y vài phương thức phổ biến)
    df_nganh['tuyenThang'] = 'Y'
    df_nganh['dgnl'] = 'Y'
    df_nganh['thpt'] = 'Y'
    df_nganh['vsat'] = '' # Để trống thay vì N để giống form

    # 6. Sắp xếp lại đúng 9 cột của template
    out_cols = ['maNganh', 'tenNganh', 'toHopGoc', 'chiTieu', 'diemSan', 'tuyenThang', 'dgnl', 'thpt', 'vsat']
    df_nganh = df_nganh[out_cols]
    df_nganh = df_nganh.fillna('')

    # 7. Ghi vào file template_nganh.xlsx
    wb = openpyxl.load_workbook('src/main/resources/templates/template_nganh.xlsx')
    ws = wb['DANH SACH NGANH']
    
    # Xóa 2 dòng dữ liệu mẫu (dòng 3 và 4)
    ws.delete_rows(3, 2)
    
    # Đổ dữ liệu mới vào
    for i, row in df_nganh.iterrows():
        ws.append(row.tolist())
    
    # Tự động xóa luôn dòng 1 (Title bôi xanh đậm) để khi user import không bị lỗi
    ws.delete_rows(1, 1)

    out_path = 'docs/template_nganh_Data.xlsx'
    wb.save(out_path)
    print(f"  ✅ Đã tạo thành công: {out_path} ({len(df_nganh)} ngành)")


def migrate_thisinh():
    print("⏳ Đang xử lý dữ liệu THÍ SINH...")
    df_ts = pd.read_excel('docs/Ds thi sinh.xlsx')
    df_ts.columns = df_ts.columns.str.strip()
    
    # Theo request trước đó: "Giới hạn 200 thí sinh để test trước"
    df_ts = df_ts.head(200)

    df_ts['CCCD'] = df_ts['CCCD'].astype(str).str.strip()
    
    # Tách Họ và Tên
    def split_name(name):
        name = str(name).strip()
        parts = name.rsplit(' ', 1)
        if len(parts) == 2:
            return parts[0], parts[1]
        return '', name # Nếu chỉ có 1 từ thì coi như là Tên

    ho_ten = df_ts['Họ Tên'].apply(split_name)
    df_ts['ho'] = ho_ten.apply(lambda x: x[0])
    df_ts['ten'] = ho_ten.apply(lambda x: x[1])
    
    # Chuẩn hóa ngày sinh (bỏ giờ phút giây nếu bị pandas parse thành datetime)
    df_ts['ngaySinh'] = pd.to_datetime(df_ts['Ngày sinh'], errors='coerce', dayfirst=True).dt.strftime('%d/%m/%Y')
    df_ts['ngaySinh'] = df_ts['ngaySinh'].fillna(df_ts['Ngày sinh'].astype(str)) # Fallback
    
    # Các trường để trống hoặc giả lập
    df_ts['dienThoai'] = ''
    df_ts['email'] = ''
    df_ts['soBaoDanh'] = df_ts['CCCD'] # Lấy tạm CCCD làm SBD
    
    df_ts = df_ts.rename(columns={
        'Giới tính': 'gioiTinh', 
        'ĐTƯT': 'doiTuong', 
        'KVƯT': 'khuVuc'
    })
    
    out_cols = ['CCCD', 'soBaoDanh', 'ho', 'ten', 'ngaySinh', 'gioiTinh', 'doiTuong', 'khuVuc', 'dienThoai', 'email']
    df_out = df_ts[out_cols]
    df_out = df_out.fillna('')

    # Đổ vào template
    wb = openpyxl.load_workbook('src/main/resources/templates/template_thisinh.xlsx')
    ws = wb['DANH SACH THI SINH']
    
    # Xóa 3 dòng mẫu
    ws.delete_rows(3, 3)
    
    for i, row in df_out.iterrows():
        ws.append(row.tolist())
        
    ws.delete_rows(1, 1) # Xóa dòng Title
    
    out_path = 'docs/template_thisinh_Data.xlsx'
    wb.save(out_path)
    print(f"  ✅ Đã tạo thành công: {out_path} (200 thí sinh theo giới hạn)")

if __name__ == "__main__":
    try:
        migrate_nganh()
        migrate_thisinh()
        print("\n🎉 HOÀN TẤT CHUYỂN ĐỔI DỮ LIỆU!")
        print("Bây giờ bạn có thể mở các file có đuôi '_Data.xlsx' trong mục docs/ để import thẳng vào ứng dụng.")
    except Exception as e:
        print(f"Lỗi hệ thống: {e}")
