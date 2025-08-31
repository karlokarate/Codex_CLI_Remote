# FishIT – **Codex Cli Remote**

**ROADMAP.md – Zielbild, Meilensteine, Aufgabenlisten & Abnahmekriterien**
*Version: 1.0 • Datum: 2025‑08‑31 • Repo‑Root: `codex_remote` • Produkt: FishIT / App: „Codex Cli Remote“*

> **Scope:** Diese Roadmap beschreibt alles, was nötig ist, um die App **lauffähig, performant und userfreundlich** zu bauen – von MVP bis Release und darüber hinaus.
> **Konventionen:** Änderungen erfolgen gemäß `AGENTS.md` (Backups `.old‑<UTC>`, Imports‑Audit, atomare Commits, Doku‑Pflege).

---

## 0) Zielbild (High‑Level)

**Ein‑Tap‑Remote für Codex‑CLI auf Windows/WSL2**:

* **Setup‑Wizard** speichert Verbindungsprofile (Host, User, Auth, MAC, Broadcast, FRITZ!Box‑Zugang) – **wiederverwendbar**.
* **Wake‑Buttons**:

    * **WOL (Magic‑Packet)** im LAN,
    * **FRITZ!Box TR‑064** (Hosts:1 → `X_AVM-DE_WakeOnLANByMACAddress`) im LAN.
    * „Aus dem Internet“: Trigger via **MyFRITZ!App** (UX‑Link/Info).
* **SSH‑Terminal** (PTY) mit **Macro‑Leiste** (z. B. „`cd ~/codex_remote && tmux new -A -s codex`“).
* **PC‑Setup‑Helper**: Schritt‑für‑Schritt‑Assistent + Live‑Tests (Ping/Port/WSL/SSH).
* **Sicher, robust, flott**: DataStore + Keystore, Host‑Key‑TOFU/Pinning, Network‑Security‑Config, WorkManager‑Flows.

---

## 1) Phasen & Meilensteine

### **P0 – Repo & Build‑Baseline (Tag: `v0.0.1` | ETA: kurz)**

**Deliverables**

* Initiales Android‑Projekt (Compose, M3), lauffähiger Debug‑Build.
* Grundgerüst: `MainActivity`, `Setup`‑Tab (Formular, Save), `Terminal`‑Tab (Einzelbefehl).
* WOL‑Senden (UDP‑Broadcast).
* DataStore‑Persistenz.
* Logging (Timber), Debug‑Konventionen (Start/OK/ERR + Kontext).

**Aufgaben**

* [ ] `settings.gradle.kts`, Root‑ & App‑`build.gradle.kts` (neueste **stable** Deps).
* [ ] `AndroidManifest.xml` mit **INTERNET**, `ACCESS_*_STATE`, optional `CHANGE_WIFI_MULTICAST_STATE`.
* [ ] `MainActivity` mit 2 Tabs (Setup/Terminal) + Theme.
* [ ] `WolService` (Magic‑Packet) + UI‑Button + Debug.
* [ ] `PreferencesStore` (DataStore) für: Host, User, Pass/Key‑Placeholder, MAC, Broadcast, FRITZ‑Host/User/Pass, Standard‑Pfad **`~/codex_remote`**.
* [ ] `CHANGELOG.md` initialisieren.

**Abnahme (DoD)**

* App startet, Setup speichert Felder, WOL sendet Paket, Build/Lint **grün**, CHANGELOG gepflegt.

---

### **P1 – LAN‑Vollfunktion & stabiler Terminal‑Pfad (Tag: `v0.1.0`)**

**Deliverables**

* **TR‑064**‑Wake via FRITZ!Box (LAN).
* **SSH‑Einzelbefehl** stabil (Timeout/Errors), **Connectivity‑Tests** (Ping/TCP:22).
* Setup‑Wizard mit **Validierung** (MAC/IP/Host), **„Testen“**‑Buttons (WOL/TR‑064/SSH/WSL).
* Network‑Security‑Config (Cleartext nur für FRITZ!Box‑Host).
* Macro‑Buttons (2–4 Slots) mit persistierten Kommandos.

**Aufgaben**

* [ ] `FritzTr064Client` (SOAP Envelope + Auth‑Schicht **Basic/Digest**; MVP: LAN Basic, P2: Digest).
* [ ] `Network Security Config`: Cleartext nur für `192.168.178.1` / `fritz.box`.
* [ ] `SshService.exec()` (Einzelbefehl) mit konfigurierbaren Timeouts, Fehlercodes, stdout/stderr.
* [ ] `DiagnosticsService`: Ping (Fallback TCP‑Probe), SSH‑Port‑Check, TR‑064‑Probe (`/tr64desc.xml`).
* [ ] Setup‑Wizard: Validierung (live), **Test‑Zeile** mit Status‑Badges + Debug‑Console.
* [ ] Macro‑Leiste in `Terminal` (persistente Befehle, z. B. `cd ~/codex_remote && tmux a -t codex`).

**Abnahme**

* TR‑064‑Wake funktioniert im LAN.
* SSH‑Test liefert Output (z. B. `wsl.exe --status` oder `uname -a`).
* Network‑Security‑Config aktiv, kein globales Cleartext.
* Macro‑Buttons führen persistierte Befehle korrekt aus.

---

### **P2 – PTY‑Terminal & Sicherheit (Tag: `v0.2.0`)**

**Deliverables**

* **Interaktives Terminal (PTY)**: Live‑Stream Output, Eingabe‑Zeile, Scrollback, ANSI‑Handling (mind. rudimentär).
* **SSH‑Key‑Auth** (ed25519), **Host‑Key‑TOFU** (persist), optional **Pinning**.
* **SecureStore**: Keystore‑gestützte Verschlüsselung von Passwörtern/Private Keys.
* **TR‑064 Digest‑Auth** voll implementiert (HTTP Digest oder Session‑SID‑Login).

**Aufgaben**

* [ ] `SshService.pty()` – SSHJ PTY Shell; Coroutine‑Flows für IO; Abbruch/Close robust.
* [ ] Terminal‑UI: Scroll, Copy, Clear, einfache ANSI‑Filter (später echtes Widget).
* [ ] `KnownHostsStore` (App‑intern): bei Erstverbindung Fingerprint anzeigen → persistieren.
* [ ] Key‑Import (SAF) + verschlüsselte Ablage (Security Crypto) + entsperren per App‑PIN/Biometric (optional).
* [ ] TR‑064 Digest/HTTPS: Authenticator oder SID‑Login (`login_sid.lua`) + SOAP‑Call.
* [ ] Logs härten: Secret‑Masking, Korrelation‑IDs (UUID v4).

**Abnahme**

* Terminal interaktiv, Latenz gefühlt < 200 ms im LAN.
* Key‑Auth läuft; Host‑Key gespeichert.
* FRITZ‑Wake auch mit Digest/HTTPS funktionsfähig.

---

### **P3 – PC‑Setup‑Helper & Auto‑Flows (Tag: `v0.3.0`)**

**Deliverables**

* **PC‑Setup‑Helper**: Assistent mit schrittweisen Anweisungen **und** automatischen Remote‑Befehlen (über SSH).
* **WorkManager‑Flows**: „Wake → Ping → SSH → WSL‑Status prüfen“.
* **Profil‑Management**: mehrere Zielrechner/Profiles (z. B. Home/Office).

**Aufgaben**

* [ ] Helper‑Steps (kopierbare Befehle + „Jetzt ausführen“):

    * Windows: OpenSSH‑Server, DefaultShell→WSL, Firewall, Tasks (Startup/Logon/Resume).
    * WSL: `tmux`, Alias `cdx`, Ordner `~/codex_remote`, Autostart‑Script.
* [ ] Auto‑Flow Job: Wake (WOL/TR‑064) → warte → Ping/Port → SSH → `cd ~/codex_remote && cdx`.
* [ ] Profiles CRUD + Schnellwechsel.

**Abnahme**

* Ein Klick „Auto‑Connect“ weckt PC im LAN und öffnet Terminal‑Session in `~/codex_remote`.
* Setup‑Helper kann PC‑Seite voll automatisieren (sofern User SSH‑Pass/Key bereitstellt).

---

### **P4 – UX‑Feinschliff, Leistung & Release (Tag: `v1.0.0`)**

**Deliverables**

* **Terminal‑Widget** (optional: `emulatorview`) für sauberes ANSI/PTY‑Rendering.
* **Performance‑Budget** & Tuning (Start < 1,5 s, Terminal‑Frame‑Drops minimal).
* **Onboarding** poliert (Leitfaden, Tailscale‑Hinweis, MyFRITZ!App‑Shortcut).
* **Release‑Build** (AAB), Signierung, Privacy‑Statement, minimaler Tracker‑Footprint.

**Aufgaben**

* [ ] UI/UX: haptisches Feedback, Fehlerbanner, leise Toasts → Snackbars.
* [ ] Terminal Copy/Paste/Share; Macro‑Editor mit Vorlagen & Reorder.
* [ ] Crash‑/ANR‑Reporting (ohne PII; optional selbst gehostet).
* [ ] Keystore‑Migrationspfad, Backup/Restore (optional, bewusst ohne Secrets).
* [ ] Play‑Listing, Screenshots, Versionierung.

**Abnahme**

* Beta‑Tester bestätigen Stabilität, klare Fehlertexte, flüssige Terminal‑Interaktion.
* Play Console Pre‑Launch Checks grün.

---

## 2) Queraufgaben (laufend)

* **Doku‑Pflege:** Jede Änderung → `CHANGELOG.md`; neue TODOs → `Roadmap.md`; Schnittstellenänderungen → `Architecture_overview.md`.
* **Security:** Secret‑Masking, keine Passwörter in Logs/Commits, Host‑Key‑Verifikation, Network‑Security‑Config.
* **Qualität:** Build/Lint/Tests grün; Imports aufgeräumt; keine ungenutzten Artefakte.
* **Performance:** Netzwerk‑Timeouts sinnvoll (WOL sofort, TR‑064 ≤ 3 s, SSH connect‑Timeout 10 s).
* **Kompatibilität:** minSdk 26+, targetSdk 35; nur **stable** Dependencies.

---

## 3) Detaillierte Aufgabenlisten

### 3.1 Networking & Wake

* [ ] **WOL**: UDP‑Broadcast (konfigurierbarer Broadcast, Port 9/7), Retry/Backoff.
* [ ] **TR‑064** (FRITZ!Box 6890 LTE kompatibel):

    * [ ] Hosts:1 `X_AVM-DE_WakeOnLANByMACAddress` (SOAP).
    * [ ] Auth: Basic (MVP) → Digest/HTTPS (P2).
    * [ ] Fehlerhandling: 401/403/500 & SOAP Faults → klare Diagnosen.
* [ ] **Network Security Config**: Cleartext nur für FRITZ‑Host.
* [ ] **Reachability**: Ping‑Ersatz (TCP:22), HTTP‑Probe `tr64desc.xml`.

### 3.2 SSH & Terminal

* [ ] **Einzelbefehl** (MVP): stdout/stderr, rc; Timeout.
* [ ] **PTY** (P2): Shell, Streams, Cancel; ANSI‑Handling, Scrollback; Input‑IME‑Kompatibilität.
* [ ] **Auth**: Password & ed25519‑Key (SAF‑Import, Keystore‑Speicherung).
* [ ] **Host‑Key**: TOFU (persist) + optional Pinning; Fingerprint‑UI.
* [ ] **Macros**: CRUD, Reorder, Platzhalter (`{PATH}`, `{USER}`); Default „`cd ~/codex_remote && tmux new -A -s codex`“.

### 3.3 Setup‑Wizard & Helper

* [ ] Profile (Host, User, Pass/KeyRef, MAC, Broadcast, FRITZ‑Creds, Standard‑Pfad `~/codex_remote`).
* [ ] Validierung & Inline‑Tests (WOL, TR‑064, SSH, WSL).
* [ ] Helper‑Steps: Befehle für Windows/WSL (aus App kopierbar/ausführbar über SSH).

### 3.4 Data & Security

* [ ] **PreferencesStore** (DataStore) + Migrations.
* [ ] **SecureStore** (Keystore) mit Biometric‑Unlock (optional).
* [ ] **Export/Import Profile** (ohne Secrets oder separat verschlüsselt).

### 3.5 Background & Reliability

* [ ] **WorkManager**: Auto‑Connect (Wake→Probe→SSH).
* [ ] **Failure‑Policy**: Exponential Backoff, klare Fehlermeldungen, manuelle Retry‑Buttons.

### 3.6 Observability

* [ ] **Debug‑Konsole** in der App (Terminal‑Pane).
* [ ] **Log‑Level‑Schalter** (Info/Debug).
* [ ] **Korrelation‑IDs** über Button‑Flows hinweg.

---

## 4) Akzeptanzkriterien (gesamt)

* **Onboarding**: In ≤ 2 Min ist ein Profil angelegt und getestet.
* **Wake**: Im LAN < 1 s bis Paket gesendet; TR‑064‑Wake bestätigt oder Fehler mit Handlungsvorschlag.
* **SSH**: Verbindet stabil; sauberer Fehler bei falschem Key/Pass; Host‑Key‑Prompt nur beim ersten Mal.
* **Terminal**: Interaktion ohne UI‑Ruckler; Macro‑Buttons funktionieren; Copy/Paste ok.
* **Sicherheit**: Keine Secrets im Log; Cleartext nur whitelisted; Host‑Key gespeichert.
* **Doku**: Vollständig & aktuell (Agents, Architecture, Roadmap, Changelog).

---

## 5) Risiken & Gegenmaßnahmen

| Risiko                                      | Auswirkung                   | Gegenmaßnahme                                                        |
| ------------------------------------------- | ---------------------------- | -------------------------------------------------------------------- |
| TR‑064 Digest/HTTPS variiert je FRITZ!OS    | Wake via Box fehlschlägt     | Implementiere Digest‑Auth & SID‑Login; Fallback: MyFRITZ!App‑Hinweis |
| WOL über WLAN‑Adapter am PC unzuverlässig   | PC wacht nicht auf           | Empfehlung: LAN‑Kabel; BIOS/UEFI & NIC‑Settings dokumentieren        |
| SSHJ/PTY Edgecases mit IME/Android‑Keyboard | Eingabe/Rendering fehlerhaft | Später auf Terminal‑Widget (`emulatorview`) umstellen                |
| Secrets in Logs                             | Security‑Leak                | Masking‑Filter, Log‑Review, Debug‑Level‑Schalter                     |
| Timeout‑Tuning                              | UX frustriert                | Sinnvolle Defaults + Cancel‑Buttons + klare Meldungen                |

---

## 6) CI/CD & Release

* [ ] **GitHub Actions**: CI‑Build (`./gradlew build`), Lint, (später) UI‑Tests.
* [ ] Signierung/Keystore‑Handling (lokal), `versionCode`/`versionName` bump per CI‑Task.
* [ ] **Beta‑Kanal**: intern/closed testing.
* [ ] Play‑Listing & Privacy‑Text (keine PII, keine Tracker by default).

---

## 7) Pflege & Prozesse (aus `AGENTS.md` übernommen)

* Vor **jeder** Änderung: Dokus lesen.
* Vor Patch: **`.old-<UTC>`‑Backups** anlegen und committen.
* Nach Patch: **Imports audit**, Build/Lint/Tests, Doku pflegen, Commit & Push.
* **Konventionelle Commits** + `CHANGELOG.md` jede Änderung dokumentieren.

---

## 8) Backlog (nach v1.0)

* Terminal‑Widget (vollwertig), Tabs/Multiplex (mehrere Sessions).
* Profile‑Export verschlüsselt (inter‑device).
* Quick‑Tiles/Widget: „Wake + Connect“.
* Optionaler „Agent“ auf PC zur Selbstheilung (Port‑Proxy, Dienstecheck).
* Lokalisierung (DE/EN), Accessibility‑Pass.

---

### Nächste Schritte (sofort)

1. **P0 Aufgabenliste** abarbeiten (Skeleton bereits erstellt → prüfen/ergänzen).
2. **P1 TR‑064** (LAN) + **Network Security Config** implementieren.
3. **P1 Diagnostics** + **Macro‑Leiste** finalisieren.
4. `CHANGELOG.md` aktualisieren, Tag `v0.1.0` vorbereiten.

**Ende – ROADMAP.md**
