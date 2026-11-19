# GTA-J2ME

A Java ME / JSR-184 (M3G) 3D demake prototype.

## First playable milestone

The initial scene is intentionally small and procedural:

- A compact urban test block with a road and simple buildings.
- CJ assembled from separate low-poly body parts in Java.
- A larger procedural city with pedestrian sidewalks and street traffic.
- Up to eighteen low-poly pedestrians with distinct clothing/skin combinations; nearby NPCs are created first and distant AI is suspended.
- Pedestrians wander, flee from CJ, or confront and shove him; punch interactions are arcade-style and non-graphic.
- Nine traffic vehicles in several body colors patrol the three main avenues, can stop for ten seconds, and can be taken over.
- Taking a stopped vehicle plays a short driver-ejection/entry sequence; the former driver exits, recovers and runs away instead of despawning.
- The player uses a low-cost articulated ragdoll with speed-scaled knockback, gravity, joint motion, ground bounce and damping when struck by moving traffic. Traffic follows separate directional lanes and changes lanes gradually to avoid slower cars.
- Third-person camera, walking, jumping, melee combat, selectable weapons and vehicle driving.

## Controls

| Key | Action |
| --- | --- |
| 2 / 8 | Move forward / backward |
| 4 / 6 | Strafe left / right |
| D-pad left / right | Orbit camera |
| D-pad up / down | Adjust camera elevation |
| 5 | Jump |
| 1 | Punch when unarmed / fire selected weapon |
| 7 | Cycle fists, pistol, shotgun and SMG |
| * | Switch protagonist |
| 3 | Enter nearby parked/stopped car / exit car |

When driving, 2 / 8 accelerate and reverse, while 4 / 6 steer.

## Project layout

- `src/gta/MainMIDlet.java` — MIDlet entry point.
- `src/gta/GameCanvas.java` — input, game loop, scene and camera.
- `src/gta/ModelFactory.java` — procedural M3G box geometry.
- `src/gta/CJModel.java` — articulated player model.
- `src/gta/CarModel.java` — low-poly sedan model.
- `src/gta/PedestrianModel.java` — lightweight pedestrian model, appearance presets and ragdoll poses.

## Build notes

The project targets a Java ME SDK that provides MIDP 2.0 and JSR-184
(Mobile 3D Graphics API). Add the device/runtime's M3G API library to the
project classpath and configure `gta.MainMIDlet` as the MIDlet entry point.

This is an early prototype. Device-specific M3G support, memory use and
rendering performance still need to be tested on the target handset.
