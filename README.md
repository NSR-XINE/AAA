# Developer & System Workstation

A multi-module Android application designed as an event-driven system administration dashboard. It interfaces with Android/Linux system nodes via `/sys` and `/proc` and executes privileged operations using Root permissions and the Shizuku/Libsu API.

## Project Structure

- **`:app`** - Main Application module containing the high-fidelity Compose dashboard layout and navigation shell.
- **`:core:engine`** - Internal utilities, thread-safe asynchronous `SystemEventBus`, custom `DispatcherProvider`, and the Shizuku/Root `ShellExecutor` bridge interface.
- **`:feature:monitor`** - Log ingestion pipeline. Connects to Ktor WebSockets, chunks incoming log lines every 50ms (or 200 items) on `Dispatchers.IO`, and tokenizes keywords (ERROR, WARN, SUCCESS) using concurrent Regex matching on `Dispatchers.Default` before passing immutable models to Compose.
- **`:feature:mapper`** - Hardware node scanner. Inspects block devices, thermals, and core frequencies under `/sys` and `/proc`, updating UI state and dispatching alerts to the shared event bus.
- **`:feature:automation`** - Reactive automation rules engine. Listens to the `SystemEventBus` and executes automated shell scripts whenever specific thresholds (e.g., `ThermalCritical` at >= 80°C) are hit.

---

## Architecture Diagram & Flow

```
[Ktor WebSocket / Mock Feed]
             │
             ▼ (Raw Logs)
   [feature:monitor pipeline] ──(Regex parsing: Default)──► [LogConsoleScreen]
             │
             ▼ (Event Dispatch)
      [SystemEventBus] ◄─── (Metrics updates) ─── [feature:mapper scanner]
             │
             ▼ (Triggers rule match)
    [feature:automation] ──(Runs action)──► [core:engine ShellExecutor] ──► [Device OS Shell]
```

## How to Build & Run

1. Clone or navigate to the project directory:
   ```bash
   cd /data/data/com.termux/files/home/DeveloperWorkstation
   ```

2. Open the project in **Android Studio (Koala 2024.1+)**.
3. Let Gradle sync and resolve all dependency catalogs defined in `gradle/libs.versions.toml`.
4. Run the `:app` configuration on a device/emulator.
5. Grant root rights to the app (using Magisk/APatch) or run Shizuku in the background to activate full shell access. The application will gracefully fall back to user-space command execution and demo metrics if root/Shizuku is unavailable.
