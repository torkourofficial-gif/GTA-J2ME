# CS.m3g viewer

A MIDP 2.0 / CLDC 1.1 MIDlet for previewing the repository's `CS.m3g` model through the JSR-184 (M3G) API.

## Model format

`CS.m3g` is stored in the repository as M3G 1.0 for compatibility with J2ME-Loader and older JSR-184 runtimes. The file is already in the correct format; `tools/prepare.ps1` only copies it to `res/CS.m3g` so the normal NetBeans packaging step can include it at the JAR resource root.

## Blender exporter

In Blender, use **File → Export → M3G** and leave **Lighting → Export Fog** unchecked. The exporter writes M3G 1.1 when fog is enabled; without fog it writes M3G 1.0. `Export Fog` is off by default in the linked add-on.

## NetBeans build

Keep `tools/prepare.ps1` in the project and run it as a pre-build step before NetBeans packages resources. Then let NetBeans compile and package the project once. The repository's `build.xml` target is available only as a standalone Ant alternative for projects without an existing NetBeans build.

## Controls

- Left/right: rotate the model.
- Up/down: move the camera closer or farther away.

