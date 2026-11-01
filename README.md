# 🌀 Timer Master Lab

This an app for taking time trials and take race times with side-by-side.
Define your athletes in a start list and mark their start and stop time, the classification will be calculated and you
can export it into PDF files.

This is perfect if you are a coach or a club trainer with several athletes that compete each others.

Enjoy my small project.

---

## TODO

## Technical Specifications

### 1. Start List Import, Export & Sharing

* **File Format:** Use structured `.json` for importing and exporting start lists.
* **Import/Export:** Allow users to import a `.json` start list to populate the race state and export the current start
  list state to a `.json` file.
* **Android Native Sharing:** Implement the Android System Share Sheet (`Intent.ACTION_SEND` with `FileProvider`) so
  users can directly share the exported JSON file via **WhatsApp**, Email, Google Drive, or other installed messaging
  apps in a single tap.

### 2. High-Stress & Tablet-Optimized Live Race UI

* **Concurrency Handling:** Redesign the active time-trial screen to seamlessly manage simultaneous participant
  departures and arrivals without UI lag or confusion.
* **Ergonomic Design & Touch Safety:** Use extra-large, well-spaced buttons and touch targets to completely prevent
  misclicks during fast-paced, high-pressure race operations.
* **Tablet-First Split Layout:** Optimize for tablet screen real estate using a multi-pane layout (e.g., upcoming start
  queue on the left, live finish line & active racers on the right).
* **Visual Clarity:** Implement clear status color-coding (e.g., *Queued*, *On Course*, *Finished*) and ensure all key
  race-control actions are strictly 1-tap operations.

### 3. Persistent & Crash-Safe Application Logging

* **Settings Toggle:** Add a dedicated setting to enable/disable file-based app logging.
* **Auto-Start Logging:** Logging must initialize and begin recording immediately at application startup.
* **Crash-Safe File Persistence:** Ensure log entries are continuously flushed/persisted to local disk storage (or
  captured via custom uncaught exception handlers) so logs remain preserved and retrievable even if the app crashes
  unexpectedly.
