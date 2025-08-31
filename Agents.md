# FishIT – **Codex Cli Remote**

**AGENTS.md – Verhaltensregeln, Arbeitsablauf & sichere Änderungsroutine**
*Version: 1.1 • Datum: 2025‑08‑31 • Projektordner: `codex_remote` • Produkt: FishIT / App: „Codex Cli Remote“*

> **Zweck dieses Dokuments**
> Dieses **Master‑Dokument** definiert, wie Codex (gpt‑5) und Mitwirkende am Projekt arbeiten: Reihenfolge, Sicherheitsnetz (Backups), Commit‑/Push‑Regeln, Debug‑Pflichten, Prüf‑ und Checklisten. **Architektur‑Details** und **Roadmap/TODOs** sind **ausgelagert** in `Architecture_overview.md` und `Roadmap.md`. **Jede Änderung** erfordert ein Update von `CHANGELOG.md`.

---

## 0) Kanonische Namen & Pfade

* **Organisation/Label:** **FishIT**
* **App‑Name:** **Codex Cli Remote**
* **Projektordner (Repo‑Root):** `codex_remote`
* **Standard‑Remote‑Pfad (auf dem Zielrechner):** `~/codex_remote`
* **Dokumente (immer zuerst lesen):**

    1. `AGENTS.md` (dieses Dokument)
    2. `Architecture_overview.md`
    3. `Roadmap.md`
    4. `CHANGELOG.md`

---

## 1) Goldene Regeln (immer)

1. **Doku zuerst.** Vor **jedem** Eingriff: `AGENTS.md`, `Architecture_overview.md`, `Roadmap.md`, `CHANGELOG.md` vollständig lesen.
2. **Ganzes Modul einlesen.** Bei Fixes/Features nie nur die „betroffene Stelle“, sondern **immer das gesamte Modul** öffnen, um Seiteneffekte zu vermeiden.
3. **Nichts zerstören.** Bestehende Funktionen dürfen **nicht** gebrochen werden – **außer** es ist **explizit gefordert** oder **technisch zwingend**. Dann: **Benachrichtigen** (Commit‑Nachricht + CHANGELOG „BREAKING“) und **sauber aufräumen** (keine toten Codezeilen zurücklassen).
4. **Backups vor Änderungen.** Vor **jedem Patch** die betroffenen Dateien als `.old‑<UTC‑Zeitstempel>` sichern und mitcommitten (siehe §2).
5. **Imports nachziehen.** Nach **jedem** Patch: fehlende Imports ergänzen, **doppelte/obsolet** entfernen.
6. **Debug‑Pflicht.** Jeder Codepfad muss nachvollziehbare **Debug‑Ausgaben** liefern (Start/OK/ERR + Kontext, keine Secrets).
7. **Atomar & nachvollziehbar.** Kleine, abgeschlossene Schritte; **Conventional Commits**; alles **sofort pushen**.
8. **Immer rückrollbar.** Durch `.old`‑Backups, Git‑Tag vor Eingriff und saubere Commits ist eine **Wiederherstellung** jederzeit möglich.

---

## 2) Sicheres Änderungs‑Ritual (Backup → Patch → Prüfungen → Doku → Commit/Push)

> **UTC‑Zeitstempel‑Format:** `YYYYMMDD-HHMMSSZ` (z. B. `20250831-142045Z`)

### 2.1 Dateibackup (immer, vor dem Edit)

* Für **jede** zu ändernde Datei `<PFAD>/<NAME>.<ext>`:

    * Kopie im selben Verzeichnis:
      `<NAME>.<ext>.old-<UTC>`  (Beispiel: `MainActivity.kt.old-20250831-142045Z`)
* `.old-<UTC>`‑Dateien **committen**, damit die Historie in GitHub **1:1 nachvollziehbar** bleibt.

### 2.2 Git‑Sicherungs‑Tag (empfohlen)

* Vor dem ersten Backup optional Tag setzen: `prechange/<UTC>` (annotated), um ein **Sprung‑Label** zu haben.

### 2.3 Patchen (gesamtes Modul)

* Modul **vollständig** einlesen → Änderung implementieren → **keine** halben Rewrites.
* **Bestehende APIs** unverändert lassen; wenn nötig: Deprecation‑Hülle/Adapter, **Breaking** sauber kommunizieren.

### 2.4 Import‑Audit (Pflicht)

* Fehlende Imports ergänzen; Doppelte/Obsolete entfernen; IDE‑„Optimize Imports“‑Äquivalent anwenden.

### 2.5 Prüfungen

* **Build**: `./gradlew build` (oder projektspezifische Build‑Tasks).
* **Lint/Tests** falls vorhanden.
* Fehler? → **zurück zu 2.3** (keine halbfertigen Commits).

### 2.6 Doku aktualisieren (immer)

* `CHANGELOG.md`: prägnante Einträge (siehe §6).
* `Roadmap.md`: neue TODOs/Folgearbeiten eintragen (keine Details hier in Agents).
* `Architecture_overview.md`: **nur** wenn öffentliche Schnittstellen/Zuständigkeiten geändert wurden.

### 2.7 Commit & Push (sofort)

* **Backup‑Commit** (optional separat) und **Implementierungs‑Commit** (Conventional Commits, §6).
* **Push** auf `origin <branch>` (Standard: `main` oder Feature‑Branch je nach Repo‑Policy).

---

## 3) Debug‑Standard (Pflicht)

* **Tagging:** `TAG="FishIT:<Modul>"`
* **Korrelation:** Für Nutzeraktionen (`Wake`, `SSH`, `WSL`) **`corrId` (UUIDv4)** erzeugen und in allen Logs mitführen.
* **Struktur:**

    * `▶ <op> ctx=<k=v,...>` (Start)
    * `✔ <op> ctx=<...>` (Erfolg)
    * `✖ <op> err=<msg> ctx=<...>` (Fehler)
* **Kontext immer mitgeben:** `host`, `user`, `mac`, `bcast`, `port`, `path="~/codex_remote"`, etc.
* **Sicherheitsregel:** **Nie** Passwörter/Keys/Fingerprints im Klartext loggen.

*Beispiel‑Helfer (Pseudokotlin):*

```kotlin
object LogX {
  fun start(tag: String, op: String, ctx: Map<String, Any?> = emptyMap()) =
    Timber.tag(tag).i("▶ %s %s", op, ctx)
  fun ok(tag: String, op: String, ctx: Map<String, Any?> = emptyMap()) =
    Timber.tag(tag).i("✔ %s %s", op, ctx)
  fun err(tag: String, op: String, t: Throwable, ctx: Map<String, Any?> = emptyMap()) =
    Timber.tag(tag).e(t, "✖ %s %s", op, ctx)
}
```

---

## 4) Import‑/Abhängigkeits‑Regeln

* **Neueste stabile** Artefakte verwenden (Versionen stehen in `build.gradle.kts`; Pflege via Roadmap/Changelog).
* Nach **jedem Patch**:

    * **Fehlende** Abhängigkeiten hinzufügen,
    * **Doppelte/obsolet** entfernen,
    * **Konflikte** (BOM vs. direkte Versionen) bereinigen,
    * **API‑Änderungen** der Dependencies gegen Architektur prüfen (Doku anpassen, falls relevant – aber nicht hier in Agents).

---

## 5) Umgang mit Build‑/Crash‑Logs (Useruploads)

1. **Log lesen → Hypothese bilden** (NullPointer, Missing Import, API‑Change, Race…).
2. **Ganzes Modul öffnen** (nicht nur Stacktrace‑Zeile).
3. **Patch** gemäß §2 → **Import‑Audit**.
4. **Build/Lint/Tests** laufen lassen.
5. **CHANGELOG.md** + **Roadmap.md** aktualisieren (Root‑Ursache, Nacharbeiten).

---

## 6) Commits, Tags & Changelog

* **Conventional Commits** (de/en gemischt ok; Einheitlichkeit pro Repo anstreben):

    * `feat(<scope>): …` neue Funktion
    * `fix(<scope>): …` Fehlerbehebung
    * `refactor(<scope>): …` ohne Verhaltensänderung
    * `chore(build|deps|ci): …` Build/Tooling
    * `docs(<file>): …` Dokuänderung
    * `perf(<scope>): …` Performance
    * **Breaking:** `feat!: …` oder `fix!: …` **plus** Absatz „BREAKING CHANGES:“ in Commit‑Body
* **Backup‑Konvention:**

    * **Vor Änderung**: optional Tag `prechange/<UTC>`
    * **Backup‑Datei(en)**: `*.old-<UTC>` im selben Verzeichnis
    * **Commit**: `chore(backup): snapshot before <topic>`
* **Changelog‑Pflicht:** Jeder Commit‑Satz mündet in **CHANGELOG.md** (kurz: *Was*, *Warum*, *Wie geprüft*).

---

## 7) Standard‑Checklisten

### 7.1 Vor dem Patch

* [ ] Doku gelesen (Agents/Arch/Roadmap/Changelog).
* [ ] Scope & Auswirkungen verstanden.
* [ ] Betroffene Module identifiziert.

### 7.2 Während des Patches

* [ ] `.old-<UTC>`‑Backups **erstellt** (alle Dateien).
* [ ] **Gesamtes Modul** editiert; Debug‑Logs ergänzt.
* [ ] **Imports** aktualisiert.

### 7.3 Nach dem Patch

* [ ] `./gradlew build` **grün** (und Lint/Tests, falls vorhanden).
* [ ] `CHANGELOG.md` aktualisiert.
* [ ] **Roadmap**: neue TODOs/Follow‑Ups erfasst.
* [ ] **Architektur** aktualisiert (falls Schnittstellen/Zuständigkeit betroffen).
* [ ] Commits & Push.

---

## 8) Codex‑Befehls‑Vorlagen (automatisiert, **gpt‑5**)

> **Hinweis:** Diese Vorlagen **führen alle Schritte selbstständig** aus – inkl. `.old`‑Backups, Import‑Audit‑Hinweisen, Commit & Push.
> Ersetze `<DATEI>`, `<TOPIC>` und ggf. den Branch.

### 8.1 Einzeldatei‑Änderung (mit Backup)

```bash
codex edit --commit --model gpt-5 --goal - <<'GOAL'
TASK: <TOPIC> (single-file patch with safe backup)
CONTEXT:
- Read AGENTS.md, Architecture_overview.md, Roadmap.md, CHANGELOG.md first.
- Project root: codex_remote
REQUIREMENTS:
1) Create .old backup with UTC timestamp alongside the target:
   <DATEI>.old-<YYYYMMDD-HHMMSSZ>
2) Apply the fix/feature to <DATEI> without breaking existing behavior.
3) Audit imports: add missing, remove duplicates/obsolete.
4) Update CHANGELOG.md (what/why/how-tested).
5) (If interfaces changed) leave a TODO in Roadmap.md and a note in Architecture_overview.md.
6) Git: add backups and changes, commit, push.
COMMIT STYLE:
- chore(backup): snapshot before <TOPIC>
- feat|fix(<scope>): <TOPIC>
GOAL
```

### 8.2 Mehrere Dateien (atomar)

```bash
codex edit --commit --model gpt-5 --goal - <<'GOAL'
TASK: <TOPIC> (multi-file patch, safe & atomic)
STEPS:
- For every file to modify, create .old-<UTC> backup.
- Edit full modules, not just lines.
- Run imports audit across touched modules.
- Update CHANGELOG.md; add Roadmap TODOs if follow-ups.
- Commit and push.
CONVENTIONS:
- Optional annotated tag prechange/<UTC> (if repo policy permits).
- Commits: chore(backup): snapshot before <TOPIC> ; feat|fix(<scope>): <TOPIC>
GOAL
```

### 8.3 Build‑Fail/Crash‑Fix

```bash
codex edit --commit --model gpt-5 --goal - <<'GOAL'
BUILD FIX: <short description>
INPUTS: (user-provided logs)
POLICY:
- Read complete module(s) implicated by the stacktrace.
- Create .old-<UTC> backups for all files to be edited.
- Implement fix, add/clean imports.
- Run build checks; summarize what was broken and why.
- Update CHANGELOG.md; put follow-ups into Roadmap.md.
- Commit & push (backup + fix).
GOAL
```

---

## 9) Wiederherstellung (Rollback) – drei Wege

1. **Direkt aus dem Repo:** jeweilige `.old-<UTC>` zurückkopieren und committen.
2. **Git‑Tag:** Auf `prechange/<UTC>` auschecken.
3. **Revert‑Commit:** den fix‑Commit rückgängig machen (GitHub UI oder CLI).

---

## 10) Was **nicht** in dieses Dokument gehört

* Architektur‑Diagramme, Sequenzflüsse, Modul‑Schnittstellen → **`Architecture_overview.md`**
* Roadmap, Sprints, offene Punkte/TODOs → **`Roadmap.md`**
* Chronik einzelner Änderungen → **`CHANGELOG.md`**

---

## 11) Definition of Done (DoD)

* `.old-<UTC>`‑Backups vorhanden & committed.
* Build/Lint/Tests **grün**.
* **Imports** korrekt (keine Unused/Doppelten).
* **Debug‑Logs** vorhanden (Start/OK/ERR + Kontext, keine Secrets).
* `CHANGELOG.md` aktualisiert; `Roadmap.md`/`Architecture_overview.md` bei Bedarf angepasst.
* Commits und Push erfolgt.

---

### Kurzfassung für Codex (gpt‑5)

1. **Doks lesen** → 2) **Backups `.old-<UTC>`** → 3) **Patch ganzes Modul** → 4) **Imports audit** → 5) **Build/Lint/Tests** → 6) **CHANGELOG/ROADMAP/ARCH** aktualisieren → 7) **Commit & Push**.

---

**Ende – AGENTS.md**
