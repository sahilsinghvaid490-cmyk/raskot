# GhostGirl

A Paper 1.21.x plugin that spawns a mysterious "ghost girl" figure near online
players every few minutes to build a horror/mystery atmosphere on a server.
She appears silently, lingers for a few seconds, and vanishes again — no
combat, no griefing, no permanent world changes.

## How it works

- A repeating task runs on a configurable interval (default: every 10 minutes).
- On each cycle, every eligible online player (not in spectator mode, without
  an active ghost girl already nearby) has a safe spawn location searched for
  within a configurable radius.
- The ghost girl is rendered as an invisible armor stand wearing pale
  leather armor and a head — a stable, vanilla-only "floating figure" effect
  that needs no external NPC plugin, skin server, or player account.
- An ambient sound, particles, and a brief Darkness effect on nearby players
  play when she appears (all individually toggleable).
- After a configurable lifetime (default: 10 seconds) she is removed
  automatically.
- All spawned entities are tracked in memory only; nothing is written to
  disk, and everything is cleaned up on player quit, config reload, and
  plugin disable.

## Requirements

- Minecraft/Paper **1.21.x**
- Java **21**
- Gradle (a system installation, or run `gradle wrapper` once inside the
  project to generate the wrapper scripts/jar for this machine)

## Building

```bash
gradle build
```

The compiled plugin jar will be at `build/libs/GhostGirl-1.0.0.jar`.
Drop it into your server's `plugins/` folder and restart (or `/reload`,
though a full restart is always safer for plugin jars).

> **Note on this delivery:** this project was written in a sandboxed
> environment without internet access, so the Paper API dependency could not
> be downloaded and a live Gradle build could not be executed here. The code
> was written and manually reviewed against the current Paper 1.21 API
> surface, but please run `gradle build` yourself the first time and let me
> know if anything doesn't compile so it can be fixed immediately.

## Commands

| Command             | Description                          | Permission        |
|----------------------|---------------------------------------|--------------------|
| `/ghostgirl`          | Shows plugin status and current config | `ghostgirl.admin` |
| `/ghostgirl reload`   | Reloads `config.yml`                  | `ghostgirl.admin` |

Both commands work from the console as well as in-game.
Tab completion is provided for the `reload` subcommand.

## Permissions

| Permission          | Description                                              | Default |
|----------------------|------------------------------------------------------------|---------|
| `ghostgirl.admin`    | Use `/ghostgirl` and `/ghostgirl reload`                  | `op`    |

## Configuration (`config.yml`)

```yaml
spawn:
  enabled: true
  interval-minutes: 10
  lifetime-seconds: 10
  radius: 5
  max-spawn-attempts: 10

effects:
  sound-enabled: true
  sound-name: "AMBIENT_CAVE"
  particle-enabled: true
  particle-name: "SOUL"
  particle-count: 15
  darkness-effect: true
  darkness-radius: 10
  darkness-duration-seconds: 4

messages:
  reload-success: "Configuration reloaded."
  status-header: "&5&lGhostGirl &7- Plugin Status"

features:
  announce-spawn: false
```

- `spawn.enabled` — master on/off switch for the whole spawn cycle.
- `spawn.interval-minutes` — how often the spawn cycle runs.
- `spawn.lifetime-seconds` — how long she stays before disappearing.
- `spawn.radius` — max distance from the player she can spawn.
- `spawn.max-spawn-attempts` — how many candidate locations to try per
  player before giving up safely (no error, just skipped).
- `effects.sound-name` / `effects.particle-name` — any valid
  `org.bukkit.Sound` / `org.bukkit.Particle` enum name; invalid names fall
  back to a sensible default and a warning is logged.
- `effects.darkness-radius` / `effects.darkness-duration-seconds` — controls
  the optional Darkness potion effect applied to nearby players.
- `features.announce-spawn` — if true, the targeted player gets a subtle
  chat message when she appears near them.

## Safety & performance

- No world scanning: only currently online players are considered.
- Spawn locations are only picked from already-loaded chunks.
- Spawn locations are checked for solid ground, headroom, and hazards
  (lava, water, magma, cactus) before use; if none is found within
  `max-spawn-attempts`, that cycle is skipped for that player with no error.
- Entities are non-solid, non-interactive, invulnerable, and never persist
  across restarts (`persistent = false`), so a crash or unclean shutdown
  can never leave a stray ghost girl entity behind in the world.
- All scheduled tasks are cancelled and all tracked entities are removed on
  plugin disable and on config reload.
