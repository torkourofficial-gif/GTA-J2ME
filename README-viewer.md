# CS.m3g viewer

A MIDP 2.0 / CLDC 1.1 MIDlet for previewing the repository's `CS.m3g` model through the JSR-184 (M3G) API.

## Configure and build

Copy the example config and edit it with paths from your Java ME SDK:

```powershell
Copy-Item build.properties.example build.properties
```

Set these properties:

- `j2me.bootclasspath` to the CLDC 1.1, MIDP 2.0, and JSR-184/M3G API JARs from your SDK.
- `preverify.executable` to that SDK's `preverify` tool.

Then from the repository root run:

```powershell
ant clean package
```

The Ant build calls `tools/prepare.ps1`, which searches the project recursively for `CS.m3g` and copies the first match to `res/CS.m3g`. It then compiles, preverifies, and packages the model and MIDlet into `dist/CSViewer.jar` and `dist/CSViewer.jad`.

Install `dist/CSViewer.jad` in an emulator that supports MIDP 2.0, CLDC 1.1, and JSR-184/M3G.

## Controls

- Left/right: rotate the model.
- Up/down: move the camera closer or farther away.

The viewer uses the scene's `World` when present. It places the camera at a default inspection distance and wraps a top-level node in a new scene if the file has no `World`.

