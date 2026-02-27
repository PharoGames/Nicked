# Nicked

**Packet-level nickname plugin with skin changing, a full developer API, and PlaceholderAPI support.**

Nicked lets operators and players adopt a completely different identity on the server — a different name *and* a different skin — without the real player ever being exposed to others. Everything is handled at the packet level, so the change is seamless and requires no client mods.

---

## Features

<div class="grid cards" markdown>

-   **Packet-level nicking**

    ---

    Names are rewritten inside `PLAYER_INFO_UPDATE` packets before they reach clients. No client mods needed. Works with 1.20+ Paper and Spigot servers.

-   **Skin changing**

    ---

    When a nick is applied, Nicked fetches the corresponding Mojang skin asynchronously and rewrites it in the same packet. The nicked player even sees their own new skin (optional, enabled by default).

-   **Developer API**

    ---

    A clean, well-documented Java API lets other plugins check nick state, apply or remove nicks programmatically, and listen to five purpose-built events.

-   **PlaceholderAPI support**

    ---

    Three placeholders — `%nicked_displayname%`, `%nicked_is_nicked%`, and `%nicked_real_name%` — integrate with any chat, scoreboard, or tab-list plugin that supports PlaceholderAPI.

-   **Persistence**

    ---

    Nicks can survive player logouts and full server restarts. Persistent data is stored in `plugins/Nicked/nicks.yml` and restored automatically on join.

-   **Customisable messages**

    ---

    Every message is defined in `messages.yml` using MiniMessage format. Gradients, hex colours, hover events — anything MiniMessage supports is fair game.

</div>

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 21 or newer |
| Spigot / Paper | 1.20 or newer |
| PacketEvents | Bundled (no separate installation needed) |

**Optional dependencies**

| Plugin | Purpose |
|---|---|
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Enables `%nicked_*%` placeholders |
| ProtocolLib / ViaVersion / ViaBackwards / ViaRewind / Geyser-Spigot | Soft-depend listed for compatibility; no active integration |

---

## Quick Start

1. Drop `Nicked.jar` into your `plugins/` folder.
2. Restart the server.
3. Grant `nicked.admin` to yourself (OPs have it by default).
4. Run `/nick Dream` and watch your name and skin change instantly.
5. Run `/nick` again (no arguments) or `/unnick` to revert.

---

## Navigation

| Page | What you'll find |
|---|---|
| [Getting Started](getting-started.md) | Installation walkthrough, first-run tips |
| [Commands](commands.md) | Every command with arguments, examples, and edge cases |
| [Configuration](configuration.md) | Full `config.yml` reference |
| [Messages](messages.md) | Customising every plugin message |
| [Permissions](permissions.md) | All permission nodes |
| [Developer API](api/index.md) | Accessing the API, listening to events, code examples |
| [PlaceholderAPI](placeholderapi.md) | All three placeholders and usage examples |
|| [Publishing](publishing.md) | Modrinth setup, CI/CD, and release workflow |
