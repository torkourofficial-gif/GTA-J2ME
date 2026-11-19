# GTA-J2ME

A Java ME / JSR-184 (M3G) 3D demake prototype.

## First playable milestone

The initial scene is intentionally small and procedural:

- A compact urban test block with a road and simple buildings.
- CJ assembled from separate low-poly body parts in Java.
- One procedural sedan.
- Third-person camera.
- Walking, strafing, jumping, punching and entering/exiting the car.
- Driving controls reuse the numeric keypad.

## Controls

| Key | Action |
| --- | --- |
| 2 / 8 | Move forward / backward |
| 4 / 6 | Strafe left / right |
| D-pad left / right | Orbit camera |
| D-pad up / down | Adjust camera elevation |
| 5 | Jump |
| 1 | Punch |
| 3 | Enter nearby car / exit car |

When driving, 2 / 8 accelerate and reverse, while 4 / 6 steer.

## Project layout

- `src/gta/MainMIDlet.java` — MIDlet entry point.
- `src/gta/GameCanvas.java` — input, game loop, scene and camera.
- `src/gta/ModelFactory.java` — procedural M3G box geometry.
- `src/gta/CJModel.java` — articulated player model.
- `src/gta/CarModel.java` — first vehicle model.

## Build notes

The project targets a Java ME SDK that provides MIDP 2.0 and JSR-184
(Mobile 3D Graphics API). Add the device/runtime's M3G API library to the
project classpath and configure `gta.MainMIDlet` as the MIDlet entry point.

This is an early prototype. Device-specific M3G support, memory use and
rendering performance still need to be tested on the target handset.
