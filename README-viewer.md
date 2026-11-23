# Claude 3D street prototype

A MIDP 2.0 / CLDC 1.1 prototype built with the JSR-184 (M3G) API. It combines Claude's textured mesh, a detailed low-poly city block with generated surface textures, a scrolling procedural cloud sky, a movable third-person camera, keyboard movement, jumping, and a procedural CPU rig.

## Model and texture

`assets/claude.obj`, `assets/claude.mtl`, and `assets/playa5nu.png` are the source model files. The OBJ contains 453 source vertices, 551 UV coordinates, 453 normals, and 770 triangles. `tools/obj_to_java.py` generates `src/ClaudeModel.java`, combining OBJ position/UV/normal indices into the 719 render vertices needed at UV seams. The generated class builds an M3G mesh and applies the UV-mapped texture.

To regenerate the Java mesh and its packed data class after changing the OBJ, run:

```text
python tools/obj_to_java.py assets/claude.obj src/ClaudeModel.java
```

The generator writes both `ClaudeModel.java` and `ClaudeModelData.java`. It packs mesh values into compact Base64 strings and decodes them during class initialization so the model data stays out of oversized Java initializer methods.

Keep the PNG beside the OBJ for Blender and in the NetBeans resource folder at `res/gta3me/playa5nu.png` so it is packaged at `/gta3me/playa5nu.png` in the JAR. The MTL's diffuse map points to `playa5nu.png`.

## Build

Build the project once in NetBeans. Make sure `res` is configured as the project's resource folder and that `res/gta3me/playa5nu.png` is included in the packaged JAR.

## Controls

- D-pad: rotate the camera continuously while held; movement stays aligned to the map axes.
- Keypad 2/4/6/8: move on the map; 5 or the device's fire key: jump.
- Keypad 1/3: zoom the camera.
- The HUD reports the current IDLE, WALK, RUN, SPRINT, JUMP, or LAND state.
- On the error screen, up/down scroll through pages of the complete wrapped error message.

Claude's OBJ is a single static mesh with no imported skeleton or animation clips. The generated Java rig identifies the separate arm shells by OBJ connectivity, then deforms them independently from the leg and foot vertices. The procedural CPU rig uses keyframed, speed-blended WALK, RUN, and RUNFAST cycles with smooth cyclic interpolation, plus one-shot STARTWALK, mirrored RUNSTOP, jump, and landing poses. The arm swing counterbalances the legs, with separate elbow, knee, ankle, pelvis, torso, and head compensation; transitions are blended rather than snapped.

The city surface, building walls, windows, sidewalks, foliage, tree bark, and street furniture use small deterministic textures generated at startup. The sky is a generated blue panorama with layered noise clouds that scroll as the camera turns.
