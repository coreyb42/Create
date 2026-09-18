# Create: Contributor Guide

## Project overview

This repository is the NeoForge edition of Create. It is currently based on
Minecraft `1.21.1`; the active porting branch is `mc26.2/dev`, targeting the
latest released Minecraft version at branch creation. Confirm the current
stable Minecraft release and NeoForge support before beginning a version bump.

The project uses Gradle, ModDevGradle, and Java 21. Always invoke the checked-in
Gradle wrapper (`./gradlew`), not a system Gradle installation.

## Repository layout

- `src/main/java/com/simibubi/create/` — mod implementation and registrations.
- `src/main/resources/` — authored assets, data, mixin configuration, and access
  transformers.
- `src/main/templates/` — resource templates expanded during `processResources`;
  the NeoForge mod metadata originates here.
- `src/main/java-templates/` — Java templates processed by Blossom.
- `src/generated/resources/` — data-generator output. It is source-controlled,
  but should be changed by running data generation rather than by hand.
- `gradle.properties` — Minecraft, NeoForge, mappings, Create, and dependency
  versions. Treat this as the primary version matrix.
- `build.gradle` — plugins, repositories, dependencies, game runs, generated
  metadata, publishing, and build behavior.

`Ponder/` is optional. When present, `settings.gradle` substitutes the published
Ponder dependency with the local composite build; do not rely on that directory
being available to other contributors or CI.

## Common commands

Run these from the repository root:

```sh
./gradlew compileJava       # Fast Java/API compatibility check
./gradlew build             # Compile, process resources, and package the mod
./gradlew runClient         # Launch a development client for manual smoke tests
./gradlew runServer         # Launch a headless development server
./gradlew runGameTestServer # Run NeoForge GameTests, when applicable
./gradlew runData           # Regenerate data into src/generated/resources
```

Use `./gradlew clean build` for a clean verification only when needed; it removes
local build output and is slower. Development worlds and logs are under `run/`
and are intentionally ignored by Git.

## Version-upgrade workflow

1. Record the target stable Minecraft and matching NeoForge version. Check the
   official release notes and NeoForge's supported-version documentation.
2. Update the version matrix in `gradle.properties`: `minecraft_version`,
   `neo_version`, mappings, and every dependency whose coordinates include the
   Minecraft version.
3. Update fixed Minecraft-version coordinates in `build.gradle` as well. Review
   optional integrations separately; prefer temporarily disabling an optional
   development dependency to hiding a required production incompatibility.
4. Update resource/data-pack metadata and template values required by the target
   Minecraft version. Keep generated metadata generated from its template.
5. Resolve compile errors according to the target NeoForge and Minecraft APIs.
   Preserve Create's existing registration and data-generation conventions.
6. Run `./gradlew compileJava`, then `./gradlew build`. Run `./gradlew runData`
   whenever generators or generated data are affected, and review every resulting
   change under `src/generated/resources/`.
7. Manually smoke-test `runClient` (startup, a new world, key Create mechanics,
   and the relevant changed feature). Use `runGameTestServer` for changes covered
   by GameTests.

Do not claim a port is complete on compilation alone: resource loading,
registries, mixins, datagen, and runtime integrations can fail only at launch.

## Change conventions

- Keep changes narrowly scoped; do not mix broad formatting or unrelated cleanup
  into migration commits.
- Follow the surrounding Java style and existing registration patterns. Use
  explicit imports and keep package boundaries intact.
- Do not edit `build/`, `.gradle/`, `run/`, or IDE metadata. Do not commit local
  worlds, logs, generated mixin-debug output, or credentials/tokens.
- Treat `src/generated/resources/` as generated output: make source or generator
  changes first, run `runData`, then commit the intentional generated diff.
- Preserve `META-INF/accesstransformer.cfg` and mixin entries unless the API
  migration specifically requires a change; runtime-test any change to either.
- The build can publish when release credentials are supplied. Never run publish
  tasks or alter release configuration unless the task explicitly asks for it.

## Before handing off work

Report the target versions, files changed, commands run, and results. Call out
unverified runtime paths, optional integrations that remain unavailable, and
generated-resource diffs that need review.
