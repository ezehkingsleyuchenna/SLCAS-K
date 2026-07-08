# Smart Library Circulation & Automation System (SLCAS)

A Java Swing desktop application for managing library items, users, borrowing, and searching.

## Project Structure

- `SLCAS/` - Java source code, compile script, and run script
- `data/` - persisted JSON data (`items.json`, `users.json`)

## Requirements

- Windows OS (batch scripts are provided)
- Java JDK 12 (project scripts currently point to `jdk-12.0.1`)

## Java Path Used by Scripts

The scripts use these exact executables:

- `C:\Program Files\Java\jdk-12.0.1\bin\javac.exe`
- `C:\Program Files\Java\jdk-12.0.1\bin\java.exe`

If Java is installed in a different location/version, edit:

- `SLCAS/compile.bat`
- `SLCAS/run.bat`

## Install / Setup

1. Install JDK 12 (or update the batch files to your installed JDK path).
2. Open a terminal in the workspace root.
3. Move into the app folder:

```bat
cd SLCAS
```

## Build

From inside `SLCAS`:

```bat
compile.bat
```

This compiles all Java files from `src/` into `bin/`.

## Run

From inside `SLCAS`:

```bat
run.bat
```

The GUI application will launch.

## First Run Behavior

- The app loads data from the `data/` folder at the workspace root.
- If no items exist, sample records are seeded automatically.
- Seeded data is saved back to `data/items.json` and `data/users.json`.

## Optional Manual Compile/Run (Without Scripts)

From the workspace root:

```bat
mkdir SLCAS\bin
for /r SLCAS\src %f in (*.java) do @echo "%f" >> SLCAS\sources.txt
"C:\Program Files\Java\jdk-12.0.1\bin\javac.exe" -encoding UTF-8 -d SLCAS\bin @SLCAS\sources.txt
cd SLCAS
"C:\Program Files\Java\jdk-12.0.1\bin\java.exe" -cp bin Main
```

Delete `SLCAS\sources.txt` after compiling if needed.
