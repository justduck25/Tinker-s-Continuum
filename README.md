# Tinkers' Construct — NeoForge 26.1 Port

> Modify all the things, then do it again!

This repository contains a community port of **Tinkers' Construct 4** to **Minecraft 26.1 / NeoForge 26.1**.

The NeoForge 26.1 port is maintained by **justduck** and is based on the original Tinkers' Construct project by SlimeKnights. This is a community port, not an official SlimeKnights release.

## Port target

| Component | Version |
|---|---|
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.78 |
| Java | 25 |
| Gradle | 9.1 |

The port preserves the original gameplay, recipes, materials, tools, modifiers, fluids, structures, world generation and client features wherever the 26.1 API allows it.

## Documentation

For documentation about writing addons or working with Tinkers' Construct datapacks, see the [SlimeKnights documentation](https://slimeknights.github.io/docs/).

For the original project and official releases, see the [Tinkers' Construct project page](https://slimeknights.github.io/projects/#tinkers-construct).

## Building from source

Requirements:

- Git available on the system `PATH`.
- JDK 25.
- A working internet connection for Gradle dependencies and Minecraft/NeoForge artifacts.

From the `Tcon4` directory, run:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat processResources
.\gradlew.bat runData
.\gradlew.bat runClientData
```

To start the development client:

```powershell
.\gradlew.bat runClient
```

The generated resources are written under `src/generated`. Do not edit generated files manually; update the corresponding data provider or source resource and run datagen again.

## Issue reporting

Please include the following information:

- Minecraft version: `26.1.2`.
- NeoForge version/build: `26.1.2.78`.
- Tinkers' Construct port version or commit.
- Versions of other mods that may be related to the issue.
- Exact steps to reproduce the problem.
- Relevant screenshots or video.
- For crashes or runtime errors, attach the relevant `latest.log`, `debug.log`, or crash report.

Please note whether the issue is specific to this NeoForge 26.1 port or also occurs in the original upstream project.

## Credits and license

Tinkers' Construct is an original project by [SlimeKnights](https://github.com/SlimeKnights).

The NeoForge 26.1 port in this repository is maintained by **justduck**.

Code, textures and binaries are licensed under the [MIT License](https://tldrlegal.com/license/mit-license), unless a different license is noted in the relevant file or asset.

You may use the mod in a modpack. Modpack authors are responsible for user support for their packs. Official support from the original project applies to official upstream builds, not custom port builds.

## Jar signing

Some jars from official build servers may be signed. Under no circumstances does anyone have permission to verify signatures on jars from other mods. Signing is for informational purposes only.
