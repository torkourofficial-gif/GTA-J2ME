# Claude 3D street prototype

A MIDP 2.0 / CLDC 1.1 prototype built with the JSR-184 (M3G) API. It combines Claude's textured mesh, a small low-poly city block, a third-person camera, keyboard movement, jumping, and idle/walk/jump motion.

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

- Arrow keys: move in four directions.
- 5 or the device's fire key: jump.
- The HUD reports the current IDLE, WALK, or JUMP state.
- On the error screen, up/down scroll through pages of the complete wrapped error message.

Claude's OBJ is a single unrigged mesh. The prototype animates the whole mesh with a small procedural idle/walk sway and bob; jumping moves the character through the scene. It does not yet have independently animated limbs.
