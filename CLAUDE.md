# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Hệ Thống Quản Lý Tuyển Sinh 2026** — a Java Swing desktop application for managing university admissions: candidates, majors, exam subject groups, and admission decisions.

**Stack:** Java 21 · Maven · Hibernate 6 (JPA) · MySQL 8.0 · Apache POI · Lombok

## Commands

### Build & Run
```bash
mvn clean compile         # Compile
mvn clean package         # Build JAR/WAR
```
Run the application by executing `gui.MainFrame.main()` from your IDE.

### Database
```bash
docker-compose up -d      # Start MySQL (localhost:3306, DB: xettuyen2026)
docker-compose down       # Stop
```
Credentials: `user123` / `password123` (root: `root`/`root`)

### Utility Scripts (run directly from IDE or mvn exec)
- `AlterSchema.java` — migrate schema
- `CheckDB.java` / `CheckSchema.java` — inspect DB state
- `ClearDB.java` — wipe records
- `TestDB.java` — test connection and import Excel data
- `python scripts/inspect_data.py` — inspect Excel file structure

## Architecture

3-tier layered desktop app (no Spring):

```
gui.*      →  bus.*    →  dao.*    →  entity.*  →  MySQL
(Swing UI)   (Business)  (Hibernate)  (JPA POJOs)
```

**`gui/`** — Swing panels, one per management domain. `MainFrame.java` is the entry point; it uses `CardLayout` to switch between 9 screens. Custom components (`CustomButton`, `CustomTable`, etc.) live in `gui/component/`. All colors/fonts are defined in `gui/style/UIConstants.java`.

**`bus/`** — Business logic: validation, pagination (50 rows/page), import orchestration. `ThiSinhBUS` has an `ImportCandidateResult` inner class tracking import success/duplicate/failure counts.

**`dao/`** — Hibernate CRUD via `HibernateUtil.getSessionFactory()`. Standard pattern: open session → begin transaction → operate → commit/rollback.

**`entity/`** — 8 JPA-annotated entities:
| Entity | Table | Purpose |
|---|---|---|
| `ThiSinh` | `xt_thisinhxettuyen25` | Candidates |
| `Nganh` | `xt_nganh` | Majors |
| `ToHopMonThi` | `xt_tohop_monthi` | Exam subject groups |
| `NganhToHop` | `xt_nganh_tohop` | Major ↔ subject mapping |
| `NguoiDung` | `xt_nguoidung` | User accounts |
| `NguyenVongXetTuyen` | — | Admission preferences |
| `DiemThiXetTuyen` | — | Exam scores |
| `DiemCongXetTuyen` | — | Bonus points |

**`util/`** — `HibernateUtil.java` (singleton SessionFactory), `ExcelUtil.java` (Apache POI read/write for `.xlsx` import templates in `docs/`).

## Key Conventions

- `hibernate.cfg.xml` sets `hbm2ddl.auto=update` — schema evolves automatically; avoid dropping tables manually.
- Relationships use `FetchType.LAZY`; open a Hibernate session when accessing lazy-loaded collections.
- All Hibernate sessions follow try-with-resources or explicit close in `finally`.
- Lombok `@Data` / `@Getter` / `@Setter` reduces boilerplate on entities — do not write manual getters/setters.
- Excel import templates live in `docs/` (e.g., `Ds thi sinh.xlsx`, `Chi tieu 2025.xlsx`).

## Work in Progress

Screens for User Management (menu 1), Exam Scores (6), Bonus Points (7), Admissions (8), and Conversion Table (9) are placeholders. Implemented screens: Candidates (2), Majors (3), Subject Groups (4), Major-Subject Mapping (5).
