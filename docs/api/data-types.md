# Data Types

This page documents the supporting data types used throughout the Nicked API.

---

## NickInfo

**Package:** `com.nicked.api`  
**Type:** Record (immutable)

An immutable snapshot of a player's active nick state. Returned by [`NickedAPI.getNickInfo(UUID)`](methods.md#getnickinfo) and passed inside [`NickApplyEvent`](events.md#nickapplyevent).

Because `NickInfo` is a snapshot, it captures state at the moment it was created. If the player's nick changes after `getNickInfo` is called, the existing `NickInfo` object will not reflect that change — you must call `getNickInfo` again.

### Fields

| Field | Type | Description |
|---|---|---|
| `realUUID` | `UUID` | The player's real UUID. Never changes, regardless of nick state. |
| `realName` | `String` | The player's real username (e.g. `"Steve"`). |
| `nickedName` | `String` | The nick name currently in use (e.g. `"Dream"`). |
| `nickedSkin` | `SkinData` (nullable) | The skin applied with the nick. `null` if the Mojang skin fetch failed or has not yet completed. |
| `cause` | `NickCause` | The reason why this nick was applied. |
| `timestamp` | `long` | Unix timestamp (milliseconds) of when the nick was applied. |

### Accessing Fields

As a Java record, all fields are accessed via accessor methods with the same name as the field:

```java
NickInfo info = nicked.getNickInfo(player.getUniqueId()).orElseThrow();

UUID    real      = info.realUUID();
String  realName  = info.realName();
String  nick      = info.nickedName();
SkinData skin     = info.nickedSkin();   // may be null
NickCause cause   = info.cause();
long    when      = info.timestamp();
```

### Example: Displaying a nick summary

```java
Optional<NickInfo> opt = nicked.getNickInfo(player.getUniqueId());
if (opt.isPresent()) {
    NickInfo info = opt.get();
    player.sendMessage(
        info.realName() + " is disguised as " + info.nickedName()
        + (info.nickedSkin() != null ? " (with skin)" : " (no skin)")
        + " since " + new Date(info.timestamp())
    );
}
```

### Example: Checking the cause of a nick

```java
nicked.getNickInfo(player.getUniqueId()).ifPresent(info -> {
    if (info.cause() == NickCause.PLUGIN) {
        // This nick was applied by another plugin, not a command
    }
});
```

---

## SkinData

**Package:** `com.nicked.nick`  
**Type:** Record (immutable)

An immutable representation of a Minecraft player skin sourced from the Mojang API.

### Fields

| Field | Type | Description |
|---|---|---|
| `value` | `String` | Base64-encoded JSON texture blob. Contains the skin URL and metadata. |
| `signature` | `String` | Mojang-signed cryptographic signature of the `value`. Required for the client to accept and render the skin. May be an empty string for offline-mode servers or unsigned profiles. |

### Accessing Fields

```java
SkinData skin = info.nickedSkin();
if (skin != null) {
    String value     = skin.value();
    String signature = skin.signature();
}
```

### Notes

- You will rarely need to interact with `SkinData` directly unless you are building a custom skin pipeline.
- The `value` string, when Base64-decoded, is a JSON object of the form:

    ```json
    {
      "timestamp": 1234567890,
      "profileId": "...",
      "profileName": "...",
      "textures": {
        "SKIN": {
          "url": "http://textures.minecraft.net/texture/..."
        }
      }
    }
    ```

- If `signature` is an empty string (offline mode), some clients may fall back to the default Steve or Alex skin instead of rendering the custom texture.

---

## NickCause

**Package:** `com.nicked.nick`  
**Type:** Enum

Identifies why a nick was applied or removed. Passed to API methods and exposed on all nick events.

### Values

| Value | Description |
|---|---|
| `COMMAND` | The player executed `/nick` or `/unnick` themselves. |
| `PLUGIN` | Another plugin triggered the change via `NickedAPI.nickPlayer()` or `NickedAPI.unnickPlayer()`. |
| `RANDOM` | The nick was randomly selected — via `/nick` (no arguments) or `/nickall`. |
| `RESTART_RESTORE` | The nick was restored from persistent storage (`nicks.yml`) on server start or player join. |
| `CONSOLE` | A non-player sender (console, command block, etc.) triggered the change via a command such as `/nickother`. |

### Usage in Event Handlers

`NickCause` is particularly useful in event handlers for distinguishing between user-initiated and programmatic changes:

```java
@EventHandler
public void onNickChange(NickChangeEvent event) {
    switch (event.getCause()) {
        case COMMAND -> handlePlayerNick(event);
        case PLUGIN  -> handlePluginNick(event);
        case RANDOM  -> handleRandomNick(event);
        default      -> { /* ignore */ }
    }
}
```

### Usage in Conditional Logic

```java
// Only allow plugin-driven nicks, block manual command usage
@EventHandler
public void onNickChange(NickChangeEvent event) {
    if (event.getCause() != NickCause.PLUGIN) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("Manual nicking is disabled on this server.");
    }
}
```

```java
// Suppress the "nick restored" message for restart restores
@EventHandler
public void onNickApply(NickApplyEvent event) {
    if (event.getCause() == NickCause.RESTART_RESTORE) {
        return; // Don't announce restored nicks
    }
    Bukkit.broadcastMessage(event.getNickInfo().realName() + " is now disguised.");
}
```
