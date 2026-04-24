# Contributing — Porting Mods

This repo is a home for Ago-era Wurm Unlimited mods ported to [Garward's WurmModLoader](https://github.com/Garward/WurmModLoader). If you've got a mod you want to see work on the modern loader, PRs are welcome.

## Before you start

- Pick a mod that hasn't been ported yet. Check `mods/` and `client-mods/` first, then the "Known gaps" section of the [CHANGELOG](CHANGELOG.md). Ago's upstream corpus lives at [ago1024/WurmServerModLauncher](https://github.com/ago1024/WurmServerModLauncher) and many community mods were hosted on the old Wurm Unlimited forums.
- Confirm the mod's original license permits redistribution. Preserve author attribution in the source headers.
- Get the framework building locally first: see [WurmModLoader](https://github.com/Garward/WurmModLoader) for the server side and [WurmModLoader-Client](https://github.com/Garward/WurmModLoader-Client) for client-side mods. You'll need their built JARs in this repo's `libs/` folder (the framework build scripts auto-sync these when a sibling checkout exists).

## Porting checklist

### 1. Drop the source in

```
mods/<modname>/
├── build.gradle.kts            # or build.gradle — either works
├── src/main/java/...           # mod source
└── src/dist/                   # optional: .properties / .config / .json shipped alongside the jar
```

Client mods live in `client-mods/<modname>/` instead. Use an existing port as your starting template — `betterfarm` is a good reference for a modernized server mod, `bagofholding` for something simpler, `action` for client-side.

### 2. Register it in `settings.gradle.kts`

```kotlin
include("mods:<modname>")
project(":mods:<modname>").projectDir = file("mods/<modname>")
```

### 3. Rewrite Ago listeners as events

The single biggest change from Ago's loader is the event system. Old:

```java
public class MyMod implements WurmServerMod, ServerStartedListener {
    @Override public void onServerStarted() { ... }
}
```

New:

```java
public class MyMod implements WurmServerMod {
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) { ... }
}
```

All the event classes live in `com.garward.wurmmodloader.api.events.*` — browse the framework repo's API jar to see what's available. Subscribe with `@SubscribeEvent` on a public method taking a single event parameter.

If the framework doesn't expose the hook you need yet, two options:
- Open an issue on [WurmModLoader](https://github.com/Garward/WurmModLoader) with the use case — most bytecode-level gaps belong in the framework, not individual mods.
- Fall back to the legacy bridge (`compileOnly` against `wurmmodloader-legacy` + `ModListener` etc.) as a temporary measure. Several in-tree ports still do this while new events are added.

### 4. Drop redundant boilerplate

- No need to ship javassist — the framework handles all bytecode work.
- Mods should NOT import `com.wurmonline.*` classes inside bytecode patches or `@PreInit` code. Type-importing them in event handlers is fine.
- Config files should go in `src/dist/` (picked up automatically by the build template) and land as `mods/<modname>.properties` in the distribution.

### 5. Build and verify

```bash
./gradlew :mods:<modname>:build
```

A successful build produces a drag-and-drop folder at `mods/<modname>/dist/`:

```
dist/mods/<modname>/<modname>.jar
dist/mods/<modname>.properties
```

Copy that `dist/` contents into your server's root (replacing anything that clashes) and smoke-test it in-game. The [PLAYER_GUIDE](mods/PLAYER_GUIDE.md) covers install layout in more detail.

### 6. Open the PR

Include in the description:
- What the mod does (one or two sentences).
- Original author and link to the upstream source.
- What had to change during the port (listener → event rewrites, removed bytecode hacks, etc.).
- Whether it still uses the legacy bridge, and why.

Small commits are fine. No strict format required on messages — just make them readable. 

## What not to PR here

- Framework changes (new events, new bytecode patches, core bug fixes) — those go to the [main WurmModLoader repo](https://github.com/Garward/WurmModLoader).
- Client framework changes — [WurmModLoader-Client](https://github.com/Garward/WurmModLoader-Client).
- Brand-new original mods that aren't ports — fine to host on your own, but this repo is scoped to Ago-ecosystem ports and community compatibility work.

## Questions

Open an issue or ping on the main WurmModLoader repo's discussions. Porting questions, missing-event requests, and "is this mod already being worked on" checks are all welcome.
