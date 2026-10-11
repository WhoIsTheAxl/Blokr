# Blokr: Android port of Minosoft

## Product direction
Blokr is an Android-native adaptation of Minosoft with a Blokr UI and Android-focused optimizations. **Reuse upstream engine implementations**, rather than rebuilding protocol, world, entities, and gameplay independently.

## Existing modules
- `app`: Android host, activities, UIManager, UIFactory, lifecycle coordination, platform services and OpenGL ES bootstrap.
- `minosoft-core`: **currently a partial source port**, not a complete engine and not a binary dependency on the full upstream Minosoft client. This module is the staging location for portable upstream engine code. Preserve upstream packages and license headers where applicable.

## Dependency direction
```
app (Android UI / platform / graphics)
  -> minosoft-core (portable upstream engine + temporary porting helpers)
```
Portable engine code must not import `com.blokr.*`, Android Activity, desktop windowing, or desktop OpenGL. Platform-specific services enter through `EnginePlatform` (filesystem, monotonic clock, logging); extend this boundary only as concrete upstream dependencies require.

## Porting rules
1. Identify an upstream subsystem and trace its imports/dependencies.
2. Import upstream source with attribution, tracking original revision and any Android patches.
3. Keep platform adaptations at the boundary, not scattered throughout gameplay code.
4. Connect a real subsystem end-to-end before claiming functionality.
5. Retire temporary `portable` classes only after the upstream replacement is integrated and tested.
6. Keep StartActivity's working staged startup untouched unless a tested change is explicitly required.
7. Do not register a fake MinosoftRuntime. BridgeState.UNAVAILABLE is correct until a real runtime is integrated.
8. Profile before optimizing. Preserve GPLv3+ obligations and upstream notices.

## Milestones
- Port upstream Minecraft session state machine and protocol handlers.
- Integrate upstream world/chunk/registry code.
- Feed real world data into Android OpenGL ES rendering.
- Connect input and lifecycle to an actual playable session.

## Current limitations
No real upstream session runtime is attached, no complete chunk packet pipeline is connected, and terrain cannot yet be rendered. The platform boundary is infrastructure, not a gameplay implementation.
