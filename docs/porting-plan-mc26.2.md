# Create porting plan: Minecraft 26.2 / NeoForge 26.2

## Status and decision

**Status: discovery complete; implementation is blocked at the required-library
gate.** This branch remains on the 1.21.1 source and dependency matrix until the
gate is resolved. Do not change `minecraft_version` yet: a direct change makes
the build unable to resolve Create's required Ponder, Flywheel, and Vanillin
artifacts.

The target is Minecraft Java **26.2**, the latest stable Minecraft release at
research time (2026-09-17), with NeoForge **26.2.0.88** selected initially from
the current NeoForge release metadata. Reconfirm and pin the newest compatible
NeoForge patch immediately before implementation; a newer patch may supersede
this value.

The next game drop, 26.3, had release candidates at research time and is not in
scope for this branch.

## Evidence collected

| Area | Finding | Consequence |
| --- | --- | --- |
| Minecraft | Minecraft 26.2 is released. | Correct stable game target. |
| NeoForge | Stable 26.2 artifacts are published through `26.2.0.88`. | The loader/toolchain target is available. |
| Create upstream | `Creators-of-Create/Create` has no 26.1 or 26.2 development branch; only `mc1.21.1/dev` is upstream. | There is no upstream port to merge or use as an implementation reference. |
| Ponder | Published metadata ends at `1.0.87+mc1.21.1`; source has `mc26.1/dev`, not a 26.2 branch. | Required dependency unavailable for 26.2. |
| Flywheel / Vanillin | The `flywheel-neoforge-26.2` and `vanillin-neoforge-26.2` Maven paths return 404; Flywheel source has `26.1.2/dev`, not 26.2. | Required client rendering dependencies unavailable for 26.2. |
| Registrate | Source has a `26.2/dev` branch, although the snapshot Maven metadata does not yet advertise a 26.2 release. | Possible source-composite candidate, but not a published dependency to assume. |
| Runtime/toolchain | Minecraft 26.2 requires Java 25 at Gradle runtime. | Upgrade all target builds from Java 21 before resolving or compiling 26.2. |
| Flywheel build system | Flywheel's legacy Loom build cannot resolve 26.2 official Mojang mappings; NeoForge 26.2's ModDev configuration uses NeoForm instead. | Port Flywheel's build/mapping setup before its Java source can be compiled. |

Primary sources:

- [Minecraft Java Edition 26.2 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2)
- [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml)
- [NeoForge 26.2 artifact index](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.2.0.88/)
- [Create upstream branches](https://github.com/Creators-of-Create/Create/branches)
- [Ponder branches](https://github.com/Creators-of-Create/Ponder/branches)
- [Flywheel branches](https://github.com/Engine-Room/Flywheel/branches)
- [Registrate branches](https://github.com/tterrag1098/Registrate/branches)

The Maven availability checks above were performed against the existing
repository URLs on 2026-09-17. Re-run them before each implementation attempt;
these artifacts can appear after this document is written.

## Repository impact assessment

The migration is a substantial API port, not merely a version-property change.
The current code directly imports approximately 1,907 Minecraft, 701 NeoForge,
235 Flywheel, 93 Registrate, and 77 Ponder source files. The project also has a
57-line access transformer, one mixin configuration with many core and client
mixins, and 15 test-related source files. Treat mixins, access transformers, and
rendering code as high-risk workstreams.

Required runtime dependencies are Flywheel (client) and Ponder (both sides).
Registrate is embedded through `jarJar`. The following are development or
optional integration dependencies and must not block a core Create compile:
JEI, Curios, Sodium, CC: Tweaked, Farmer's Delight, Vanillin, Architectury,
FTB Chunks/Teams/Library, JourneyMap, and Xaero's World Map.

## Milestones

### 0. Dependency gate — no source migration before this passes

1. Check for published 26.2 artifacts for Ponder, Flywheel, Vanillin, and
   Registrate in their canonical Maven repositories.
2. If upstream publishes compatible versions, record the exact coordinates in a
   dependency matrix in the implementation PR and use those artifacts.
3. If a required dependency has an appropriate source branch but no release,
   obtain explicit approval from its maintainers before using a pinned source
   composite build, local Maven publication, or fork. Do not silently substitute
   an older 26.1 artifact.
4. If Ponder and Flywheel remain at 26.1, pause this 26.2 port. The viable
   alternatives are waiting for the dependencies or deliberately retargeting the
   branch to 26.1.2 after a separate target decision.
5. For Flywheel specifically, complete the Loom-to-26.2 mapping/build-system
   migration (or use an upstream-supported Loom/NeoForm configuration) before
   attempting Java API migration. Its current legacy Loom layout does not find
   Mojang mappings for 26.2.

**Exit criteria:** Gradle resolves exact target artifacts for NeoForge, Ponder,
Flywheel, Vanillin, and Registrate without local unpublished state; companion
builds run with Java 25 and establish target mappings.

### 1. Establish a minimal reproducible 26.2 build

1. Update `gradle.properties`: `minecraft_version`, `neo_version`, mappings,
   Registrate, Ponder, Flywheel, Vanillin, and every supported dependency
   version.
2. Update hard-coded 1.21.1 coordinates in `build.gradle`, especially the
   JourneyMap and JourneyMap API development dependencies.
3. Update `neoforge.mods.toml` dependency version ranges and reasons that embed
   1.21.1-specific version numbers.
4. Update `pack.mcmeta` and any data/resource pack versions from the official
   26.2 release notes. Confirm the exact values from the target game, rather
   than carrying values from a snapshot.
5. Isolate unavailable optional development dependencies behind Gradle feature
   flags or remove them only from the local development classpath. Keep their
   production compatibility code compiling where its APIs are available.
6. Run `./gradlew compileJava --stacktrace` and capture the first complete error
   set before changing source APIs.

**Exit criteria:** `compileJava` resolves dependencies and produces a categorized
compiler-error baseline, or succeeds.

### 2. Port loader and Minecraft APIs in coherent batches

Port in this order, committing each independently:

1. Bootstrap, mod initialization, event registration, registries, networking,
   capabilities/attachments, and configuration.
2. Core gameplay systems: blocks, items, block entities, entities, recipes,
   menus, fluids, world generation, commands, and saved data.
3. Data generation and generated resources. Run `./gradlew runData` only after
   the generator layer compiles; review and commit intentional changes under
   `src/generated/resources/`.
4. Client/rendering: Flywheel integration, models, shaders, particles, GUI,
   overlays, and Ponder scenes.
5. Optional integrations, one dependency at a time: JEI, Curios, ComputerCraft,
   Farmer's Delight, Sodium, JourneyMap, Xaero, FTB, and the remaining `compat`
   packages.

For every batch, start from the target NeoForge/Minecraft source and official
migration documentation. Do not weaken mixin injector requirements to make a
startup failure disappear; update the target method, injection point, or remove
the obsolete behavior with a documented rationale.

**Exit criteria:** `./gradlew compileJava` succeeds with core required
dependencies; optional integrations have a recorded supported/deferred state.

### 3. Runtime and data verification

1. Run `./gradlew build --stacktrace`.
2. Run `./gradlew runData`; inspect all generated-resource changes, then rerun
   `build` to ensure generated assets and metadata are packaged correctly.
3. Run `./gradlew runGameTestServer` and repair relevant failures. Add GameTests
   for migration regressions where feasible.
4. Run `./gradlew runClient`; test startup, new-world creation, loading an
   existing test world, registries, Ponder, kinetic rendering, fluids,
   contraptions, trains, menus, and packaging/recipes.
5. Run `./gradlew runServer` and connect with a development client to cover
   dedicated-server startup and networking paths.
6. Repeat client smoke tests with each available optional integration enabled.

**Exit criteria:** clean `build`, data generation reviewed, GameTests pass or
have documented failures, and client plus dedicated-server smoke tests pass.

### 4. Release readiness

1. Finalize the supported dependency matrix and metadata ranges.
2. Update `changelog.md` with the port scope and any intentionally deferred
   integration.
3. Build a release candidate locally; inspect its manifest, embedded Registrate
   artifact, and generated `neoforge.mods.toml`.
4. Do not invoke publishing tasks until release credentials, target version,
   changelog, and distribution approval are explicitly supplied.

## First implementation ticket after the dependency gate

**Title:** `build: establish the Minecraft 26.2 dependency matrix`

**Scope:** only Gradle properties/build scripts, metadata, pack metadata, and
documented availability switches. No broad Java API edits and no generated-data
rewrite.

**Acceptance criteria:** all target required artifacts resolve; `compileJava`
runs far enough to yield a reproducible compiler baseline; the commit identifies
the exact NeoForge and companion-library versions used; unavailable optional
integrations are explicitly disabled only for the development environment.

## Risks and controls

| Risk | Control |
| --- | --- |
| Required 26.2 libraries are not released. | Stop at milestone 0; do not create an unreproducible local port. |
| 26.2 API changes break mixins/access transformers at runtime. | Port and launch-test these early; retain injector strictness. |
| Optional development mods hide core errors. | Establish a minimal required runtime first, then re-enable integrations individually. |
| Datagen causes broad opaque diffs. | Run it only after generator compilation and review output separately. |
| Minecraft releases 26.3 during the effort. | Keep 26.2 as the frozen branch target; open a separate decision/ticket for 26.3 rather than retargeting mid-port. |

## Immediate next action

Recheck the required dependency artifacts. If compatible Ponder, Flywheel, and
Vanillin releases are available, begin milestone 1. If not, the next decision is
whether to wait for their 26.2 ports or retarget this branch to the viable 26.1.2
ecosystem; that decision requires user/project-owner direction.
