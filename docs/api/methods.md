# API Methods

This page documents the `NickedAPI` interface and the `NickedAPIProvider` accessor class.

All methods are thread-safe for reading (`isNicked`, `getNickInfo`, `getDisplayName`, `getRealName`). Methods that mutate state (`nickPlayer`, `unnickPlayer`) **must be called from the main server thread**, as they fire Bukkit events and modify player state.

---

## NickedAPI

**Package:** `com.nicked.api`  
**Type:** Interface

Obtain an instance via [`NickedAPIProvider.getAPI()`](#nickedapiprovider).

---

### `getDisplayName`

```java
String getDisplayName(UUID uuid)
```

Returns the display name for the given player UUID.

- If the player is currently nicked, returns their **nick name**.
- If the player is not nicked, returns their **real name**.
- Fires [`NickResolveEvent`](events.md#nickresolveevent) before returning, allowing other plugins to override the resolved value.

**Parameters**

| Name | Type | Description |
|---|---|---|
| `uuid` | `UUID` | The player's UUID. May belong to an offline player. |

**Returns:** `String` — the resolved display name, never `null`.

**Example**

```java
String name = nicked.getDisplayName(player.getUniqueId());
player.sendMessage("You see: " + name);
```

---

### `isNicked`

```java
boolean isNicked(UUID uuid)
```

Returns `true` if the player is currently nicked.

!!! note
    This returns `true` as soon as `nickPlayer` is called, even before the asynchronous skin fetch completes. The `NickInfo` snapshot obtained via `getNickInfo` may have a `null` `nickedSkin` field during this window.

**Parameters**

| Name | Type | Description |
|---|---|---|
| `uuid` | `UUID` | The player's UUID. |

**Returns:** `boolean`

**Example**

```java
if (nicked.isNicked(player.getUniqueId())) {
    player.sendMessage("You are currently disguised.");
}
```

---

### `getNickInfo`

```java
Optional<NickInfo> getNickInfo(UUID uuid)
```

Returns an immutable snapshot of the player's current nick state, or an empty `Optional` if the player is not nicked.

The returned [`NickInfo`](data-types.md#nickinfo) captures the state at the moment of the call. It will not update if the player's nick changes afterwards — call `getNickInfo` again to get a fresh snapshot.

**Parameters**

| Name | Type | Description |
|---|---|---|
| `uuid` | `UUID` | The player's UUID. |

**Returns:** `Optional<NickInfo>` — present if the player is nicked, empty otherwise.

**Example**

```java
nicked.getNickInfo(player.getUniqueId()).ifPresent(info -> {
    getLogger().info(
        info.realName() + " is nicked as " + info.nickedName()
        + " (cause: " + info.cause() + ")"
    );
});
```

---

### `nickPlayer`

```java
void nickPlayer(UUID uuid, String newName, NickCause cause)
```

Applies a nick to the specified player.

**Behaviour:**

1. Validates that the player is online. If not, logs a warning and returns immediately without storing any data.
2. Fires [`NickChangeEvent`](events.md#nickchangeevent) synchronously. If the event is cancelled, the call is a no-op.
3. Stores a `NickData` entry immediately, so `isNicked(uuid)` returns `true` right away.
4. Sets the Bukkit display name and tab-list name.
5. Fetches the Mojang skin for `newName` **asynchronously**.
6. Once the skin fetch completes (or fails):
   - Updates the nick entry with the skin.
   - Saves to `nicks.yml` if persistence is enabled.
   - Applies the server-side `GameProfile` modification if `internal_name_change` is enabled.
   - Sends `PLAYER_INFO_UPDATE` packets to all viewers.
   - Sends a `RESPAWN` packet to the nicked player (if `skin_change_for_self` is enabled).
7. Fires [`NickApplyEvent`](events.md#nickapplyevent) on the main thread.

If the Mojang API request fails, the nick remains active without a skin change, and the player receives the `skin_fetch_failed` message.

**Parameters**

| Name | Type | Description |
|---|---|---|
| `uuid` | `UUID` | The UUID of the player to nick. The player must be online. |
| `newName` | `String` | The nick name to apply. |
| `cause` | `NickCause` | The reason for the nick change. Use `NickCause.PLUGIN` for API-driven changes. |

**Returns:** `void`

!!! warning "Main thread only"
    This method fires Bukkit events and modifies player state. Call it from the main server thread. If you need to call it from an async context, use `Bukkit.getScheduler().runTask(plugin, () -> nicked.nickPlayer(...))`.

**Example**

```java
import com.nicked.nick.NickCause;

// Nick the player as "Notch" via your plugin
nicked.nickPlayer(player.getUniqueId(), "Notch", NickCause.PLUGIN);
```

---

### `unnickPlayer`

```java
void unnickPlayer(UUID uuid, NickCause cause)
```

Removes the active nick from the specified player.

**Behaviour:**

1. Does nothing if the player is not currently nicked.
2. Fires [`NickRemoveEvent`](events.md#nickremoveevent) synchronously. If the event is cancelled, the nick is left in place.
3. Removes the nick from the in-memory store.
4. Removes the entry from `nicks.yml` if persistence is enabled.
5. Restores the Bukkit display name to the real name.
6. Restores the server-side `GameProfile` if `internal_name_change` was active.
7. Refreshes packets for all viewers (restoring the original skin).

**Parameters**

| Name | Type | Description |
|---|---|---|
| `uuid` | `UUID` | The UUID of the player to unnick. |
| `cause` | `NickCause` | The reason for the removal. Use `NickCause.PLUGIN` for API-driven removals. |

**Returns:** `void`

!!! warning "Main thread only"
    Same threading constraint as `nickPlayer`.

**Example**

```java
nicked.unnickPlayer(player.getUniqueId(), NickCause.PLUGIN);
```

---

### `getRealName`

```java
String getRealName(UUID uuid)
```

Returns the real username for the given UUID, **regardless of nick state**.

- For online players, returns `Player#getName()`.
- For offline players, resolves from persistent storage, falling back to a UUID-derived string if the player is completely unknown.

**Parameters**

| Name | Type | Description |
|---|---|---|
| `uuid` | `UUID` | The player's UUID. |

**Returns:** `String` — the real name, never `null`.

**Example**

```java
String real = nicked.getRealName(player.getUniqueId());
// Even if the player is nicked as "Dream", this returns "Steve"
```

---

## NickedAPIProvider

**Package:** `com.nicked.api`  
**Type:** Class (static utility)

### `getAPI`

```java
public static NickedAPI getAPI()
```

Returns the active `NickedAPI` instance.

**Returns:** `NickedAPI`  
**Throws:** `IllegalStateException` if called before the Nicked plugin has finished enabling.

---

### `getAPIOrNull`

```java
public static NickedAPI getAPIOrNull()
```

Returns the active `NickedAPI` instance, or `null` if the Nicked plugin is not enabled.

**Returns:** `NickedAPI` or `null`

---

### `setInstance` (internal)

```java
public static void setInstance(NickedAPI api)
```

Registers or clears the API singleton. Called exclusively by the Nicked plugin internals during `onEnable()` and `onDisable()`. **Do not call this from external plugins.**
