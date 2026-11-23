# Claude OBJ viewer

A MIDP 2.0 / CLDC 1.1 MIDlet that builds Claude's mesh with the JSR-184 (M3G) API.

## Model and texture

`assets/claude.obj`, `assets/claude.mtl`, and `assets/playa5nu.png` are the source model files. The OBJ contains 453 source vertices, 551 UV coordinates, 453 normals, and 770 triangles. `tools/obj_to_java.py` generates `src/ClaudeModel.java`, combining OBJ position/UV/normal indices into the 719 render vertices needed at UV seams. The generated class builds an M3G mesh and applies the UV-mapped texture.

To regenerate the Java mesh after changing the OBJ, run:

```text
python tools/obj_to_java.py assets/claude.obj src/ClaudeModel.java
```

Keep the PNG beside the OBJ for Blender and in the NetBeans resource folder at `res/gta3me/playa5nu.png` so it is packaged at `/gta3me/playa5nu.png` in the JAR. The MTL's diffuse map points to `playa5nu.png`.

## Build

Build the project once in NetBeans. Make sure `res` is configured as the project's resource folder and that `res/gta3me/playa5nu.png` is included in the packaged JAR.

## Controls

- Left/right: rotate the model.
- Up/down: move the camera closer or farther away.
- On the error screen, up/down scroll through pages of the complete wrapped error message.
