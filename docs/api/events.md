# Events

Nicked fires five custom Bukkit events at key points in the nick lifecycle. All events extend `org.bukkit.event.Event` and follow standard Bukkit listener registration.

**Package:** `com.nicked.api.event`

---

## Listening to Events

Register your listener the standard Bukkit way:

```java
getServer().getPluginManager().registerEvents(new MyListener(), this);
```

```java
import com.nicked.api.event.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {

    @EventHandler
    public void onNickChange(NickChangeEvent event) {
        // ...
    }
}
```

---

## Event Overview

| Event | Cancellable | Fired |
|---|---|---|
| [`NickChangeEvent`](#nickchangeevent) | Yes | Before a nick is applied |
| [`NickApplyEvent`](#nickapplyevent) | No | After a nick is fully applied |
| [`NickRemoveEvent`](#nickremoveevent) | Yes | Before a nick is removed |
| [`NickResolveEvent`](#nickresolveevent) | No | When `getDisplayName` is called on the API |
| [`NickRandomSelectEvent`](#nickrandomselectevent) | Yes | When a random nick name has been selected |

---

## NickChangeEvent

Fired **before** a nick is applied to a player. Cancelling this event prevents the nick from being applied.

**Cancellable:** Yes  
**Class:** `com.nicked.api.event.NickChangeEvent`

### Fields & Methods

| Method | Return Type | Description |
|---|---|---|
| `getPlayer()` | `Player` | The player being nicked. |
| `getOldNick()` | `String` (nullable) | The player's previous nick name, or `null` if they were not nicked before. |
| `getNewNick()` | `String` | The nick name being applied. |
| `getCause()` | `NickCause` | The reason for this nick change. |
| `isCancelled()` | `boolean` | Whether the event has been cancelled. |
| `setCancelled(boolean)` | `void` | Cancel or un-cancel the event. |

### When it fires

- `/nick <name>` (manual nick)
- `/nick` (random nick)
- `/nickall` (once per player)
- `/nickother <player> <nick>`
- `NickedAPI.nickPlayer()`

### Example: Blocking a reserved name

```java
@EventHandler
public void onNickChange(NickChangeEvent event) {
    if (event.getNewNick().equalsIgnoreCase("Admin")) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You cannot nick as 'Admin'.");
    }
}
```

### Example: Logging nick changes

```java
@EventHandler
public void onNickChange(NickChangeEvent event) {
    String old = event.getOldNick() != null ? event.getOldNick() : "(none)";
    getLogger().info(
        event.getPlayer().getName() + " nicking: "
        + old + " -> " + event.getNewNick()
        + " [" + event.getCause() + "]"
    );
}
```

---

## NickApplyEvent

Fired **after** a nick has been fully applied. At this point:

- The skin has been fetched (or the fetch has failed).
- Packets have been sent to all viewers.
- The `NickInfo` snapshot reflects the final state.

This event **cannot be cancelled**. Use [`NickChangeEvent`](#nickchangeevent) to prevent a nick from being applied.

**Cancellable:** No  
**Class:** `com.nicked.api.event.NickApplyEvent`

### Fields & Methods

| Method | Return Type | Description |
|---|---|---|
| `getPlayer()` | `Player` | The player who was nicked. |
| `getNickInfo()` | `NickInfo` | Immutable snapshot of the applied nick state. |
| `getCause()` | `NickCause` | The reason for this nick change. |

### Example: Announcing nicks to the server

```java
@EventHandler
public void onNickApply(NickApplyEvent event) {
    NickInfo info = event.getNickInfo();
    if (event.getCause() == NickCause.COMMAND) {
        Bukkit.broadcastMessage(info.realName() + " is now disguised.");
    }
}
```

### Example: Checking if skin was successfully applied

```java
@EventHandler
public void onNickApply(NickApplyEvent event) {
    NickInfo info = event.getNickInfo();
    if (info.nickedSkin() == null) {
        getLogger().warning("Nick applied without skin: " + info.nickedName());
    }
}
```

---

## NickRemoveEvent

Fired **before** a player's nick is removed. Cancelling this event leaves the nick in place.

**Cancellable:** Yes  
**Class:** `com.nicked.api.event.NickRemoveEvent`

### Fields & Methods

| Method | Return Type | Description |
|---|---|---|
| `getPlayer()` | `Player` | The player being unnicked. |
| `getNickedName()` | `String` | The nick name being removed. |
| `getCause()` | `NickCause` | The reason for this removal. |
| `isCancelled()` | `boolean` | Whether the event has been cancelled. |
| `setCancelled(boolean)` | `void` | Cancel or un-cancel the event. |

### When it fires

- `/unnick`
- `/nick` (toggle off, no arguments when already nicked)
- `NickedAPI.unnickPlayer()`

### Example: Preventing a player from removing their own nick

```java
@EventHandler
public void onNickRemove(NickRemoveEvent event) {
    if (event.getCause() == NickCause.COMMAND
            && isInGame(event.getPlayer())) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You cannot remove your nick during a game.");
    }
}
```

---

## NickResolveEvent

Fired whenever [`NickedAPI.getDisplayName(UUID)`](methods.md#getdisplayname) is called. Listening plugins can override the resolved name by calling `setResolvedName(String)`.

This is useful for integrating Nicked with chat formatting plugins, scoreboards, or any other system that needs a customised view of the display name beyond what Nicked provides by default.

**Cancellable:** No  
**Class:** `com.nicked.api.event.NickResolveEvent`

### Fields & Methods

| Method | Return Type | Description |
|---|---|---|
| `getPlayerUUID()` | `UUID` | The UUID of the player being resolved. |
| `getRealName()` | `String` | The player's real username, regardless of nick state. |
| `getResolvedName()` | `String` | The currently resolved display name (nick name if nicked, real name otherwise). |
| `setResolvedName(String)` | `void` | Overrides the name returned by `getDisplayName()`. |

### Example: Prefixing all resolved names with a rank

```java
@EventHandler
public void onNickResolve(NickResolveEvent event) {
    String rank = getRank(event.getPlayerUUID()); // your own method
    event.setResolvedName("[" + rank + "] " + event.getResolvedName());
}
```

!!! note
    `NickResolveEvent` fires on every call to `getDisplayName`. Keep your handler lightweight to avoid performance impact on systems that call it frequently (e.g. per-line chat formatting).

---

## NickRandomSelectEvent

Fired when a random nick name has been chosen for a player, **before** it is applied. The event allows you to replace the selected name or cancel the random nick entirely.

**Cancellable:** Yes  
**Class:** `com.nicked.api.event.NickRandomSelectEvent`

### Fields & Methods

| Method | Return Type | Description |
|---|---|---|
| `getPlayer()` | `Player` | The player receiving the random nick. |
| `getSelectedName()` | `String` | The randomly chosen nick name. |
| `setSelectedName(String)` | `void` | Replaces the randomly selected name with a custom one. The new name is still subject to `NickChangeEvent`. |
| `getCause()` | `NickCause` | The cause — typically `NickCause.RANDOM`. |
| `isCancelled()` | `boolean` | Whether the event has been cancelled. |
| `setCancelled(boolean)` | `void` | Cancelling aborts the random nick for this player. |

### When it fires

- `/nick` (no arguments, not currently nicked)
- `/nickall` (once per player)

### Example: Always assigning names from a custom pool

```java
private static final List<String> POOL = List.of("Hero", "Shadow", "Ghost");

@EventHandler
public void onRandomNick(NickRandomSelectEvent event) {
    String pick = POOL.get(ThreadLocalRandom.current().nextInt(POOL.size()));
    event.setSelectedName(pick);
}
```

### Example: Preventing specific players from random nicking

```java
@EventHandler
public void onRandomNick(NickRandomSelectEvent event) {
    if (event.getPlayer().hasPermission("myserver.no-random-nick")) {
        event.setCancelled(true);
    }
}
```

---

## Event Firing Order

For a standard `/nick <name>` invocation:

```
NickChangeEvent (cancellable)
    ↓ (if not cancelled)
[async skin fetch]
    ↓
NickApplyEvent (main thread, after skin step)
```

For a `/nick` (random) invocation:

```
NickRandomSelectEvent (cancellable)
    ↓ (if not cancelled)
NickChangeEvent (cancellable)
    ↓ (if not cancelled)
[async skin fetch]
    ↓
NickApplyEvent (main thread, after skin step)
```

For `/unnick`:

```
NickRemoveEvent (cancellable)
    ↓ (if not cancelled)
[packets restored, skin reverted]
```

For `NickedAPI.getDisplayName()`:

```
NickResolveEvent (not cancellable, name is mutable)
    ↓
returns resolvedName
```
