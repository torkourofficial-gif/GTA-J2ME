# CS.m3g viewer

A MIDP 2.0 / CLDC 1.1 MIDlet for previewing the repository's `CS.m3g` model through the JSR-184 (M3G) API.

## Model resource

`CS.m3g` is stored in M3G 1.0 format. The same file is included at `res/CS.m3g` so the normal NetBeans Java ME build packages it at the JAR resource root. The MIDlet loads it with `Loader.load("/CS.m3g")`.

Build the project once in NetBeans. If the viewer reports that `/CS.m3g` is missing from the JAR, make sure `res` is configured as the project's resource folder and that `res/CS.m3g` is included in the packaged JAR.

## Blender exporter

In Blender, use **File → Export → M3G** and leave **Lighting → Export Fog** unchecked. With fog enabled, the exporter writes M3G 1.1; without fog it writes M3G 1.0. `Export Fog` is off by default in the linked add-on.

## Controls

- Left/right: rotate the model.
- Up/down: move the camera closer or farther away.
- On the error screen, up/down scroll through pages of the complete wrapped error message.

