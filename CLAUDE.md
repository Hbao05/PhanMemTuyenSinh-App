# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Hệ Thống Quản Lý Tuyển Sinh 2026** — a Java Swing desktop application for managing university admissions: candidates, majors, exam subject groups, and admission decisions.

**Stack:** Java 21 · Maven · Hibernate 6 (JPA) · MySQL 8.0 · Apache POI · Lombok · jBCrypt · JFreeChart

## Commands

### Build & Run
```bash
mvn clean compile         # Compile
mvn clean package         # Build JAR
```
Entry point: `MainApp.main()` — sets system L&F and launches `MainFrame`. Run from IDE. (The `LoginFrame` startup path is currently commented out for development convenience.)

### Database
```bash
docker-compose up -d      # Start MySQL (localhost:3306, DB: xettuyen2026)
docker-compose down       # Stop
```
Hibernate credentials (hibernate.cfg.xml): `root` / `root`. Docker also exposes `user123` / `password123`.

### Utility Classes (run from IDE)
- `AlterSchema.java` — migrate schema
- `CheckDB.java` / `CheckSchema.java` — inspect DB state
- `ClearDB.java` / `ClearData.java` — wipe all records
- `CreateTables.java` / `DropConstraint.java` / `HardDelete.java` — DDL helpers
- `SeedAdminUser.java` — create default admin account
- `InspectExcel.java` / `python scripts/inspect_data.py` — inspect Excel file structure
- `TestCount.java` / `TestFetch.java` / `TestImport.java` — manual integration tests (no formal test suite; `src/test/` does not exist)

## Architecture

3-tier layered desktop app (no Spring):

```
gui.*      →  bus.*      →  dao.*        →  entity.*   →  MySQL
(Swing UI)   (Business)    (Hibernate)     (JPA POJOs)
```

**`MainApp.java`** — true entry point; currently opens `MainFrame` directly. The full flow (disabled in dev) is: `LoginFrame` → 3-attempt lockout with 30s delay, BCrypt auth → `MainFrame`.

**`app/Session.java`** — static holder for the logged-in `NguoiDung`; `isAdmin()` drives sidebar menu visibility.

**`gui/`** — `MainFrame` uses `CardLayout` to host 9 domain panels. Custom widgets in `gui/component/` (`CustomButton`, `CustomTable`, `CustomTextField`, `CustomComboBox`). All colors/fonts in `gui/style/UIConstants.java`.

**`bus/`** — Validation, pagination (20 rows/page via `ROWS_PER_PAGE`), and import orchestration. `ThiSinhBUS.ImportCandidateResult` tracks success/duplicate/failure counts. `XetTuyenEngine` contains static methods for admission score calculation.

**`dao/`** — Hibernate CRUD via `HibernateUtil.getSessionFactory()`. Pattern: try-with-resources session → beginTransaction → persist/merge/remove → commit, rollback on exception.

**`entity/`** — 9 JPA-annotated entities:

| Entity | Table | Notes |
|---|---|---|
| `ThiSinh` | `xt_thisinhxettuyen25` | Candidates; cccd is unique key |
| `Nganh` | `xt_nganh` | Majors; holds chiTieu (quota) and score thresholds |
| `ToHopMonThi` | `xt_tohop_monthi` | Exam subject groups (3 subjects) |
| `NganhToHop` | `xt_nganh_tohop` | Major ↔ subject mapping with per-subject coefficients |
| `NguyenVongXetTuyen` | `xt_nguyenvongxettuyen` | Admission preferences; stores computed scores + ketQua |
| `DiemThiXetTuyen` | `xt_diemthixettuyen` | Exam scores; one row per candidate |
| `DiemCongXetTuyen` | `xt_diemcongxetuyen` | Bonus points per candidate+major+method |
| `BangQuyDoi` | `xt_bangquydoi` | Score conversion tables |
| `NguoiDung` | `xt_nguoidung` | User accounts; quyen = ADMIN \| USER |

**`util/`** — `HibernateUtil.java` (singleton SessionFactory), `ExcelUtil.java` (POI batch reader, 1000 rows/batch using `excel-streaming-reader` for large files), `PasswordUtil.java` (BCrypt wrap), `ProvinceUtil.java` (province/area data), `SubjectUtil.java` (subject name helpers).

Excel import templates are in `src/main/resources/data_import/`: `thisinh_import.xlsx`, `nganh_import.xlsx`, `tohopmon_import.xlsx`, `nganhtohop_import.xlsx`, `nguyenvongxettuyen.xlsx`, `diemcongxettuyen.xlsx`, `xt_diemthixettuyen.xlsx`, `xt_bangquydoi.xlsx`.

## Admission Score Formula

`XetTuyenEngine` computes:

```
ĐTHXT  = weighted sum of 3 subject scores per NganhToHop coefficients
ĐƯT    = area priority (0.25–0.75) + category priority (0–2.0), capped at 3.0
ĐXT    = ĐTHXT + ĐC (DiemCong) + ĐƯT
```

Candidates are ranked by ĐXT descending; `chiTieu` slots per major determine admission (`ketQua`).

## Key Conventions

- `hibernate.cfg.xml` uses `hbm2ddl.auto=update` — schema evolves automatically; never drop tables manually.
- Relationships use `FetchType.LAZY`; always have an open Hibernate session when traversing associations.
- Lombok `@Data` / `@Getter` / `@Setter` on all entities — do not write manual getters/setters.
- Passwords are BCrypt-hashed (`PasswordUtil.hash` / `PasswordUtil.verify`); never store plaintext.
- All BUS pagination uses `offset = (page-1) * ROWS_PER_PAGE` and a matching `calculateTotalPages()`.
