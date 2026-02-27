# Developer API

Nicked exposes a clean Java API that lets other plugins:

- Check whether a player is currently nicked
- Read a player's current nick state (name, skin, cause, timestamp)
- Apply or remove nicks programmatically
- Override how display names are resolved
- Intercept and cancel nick changes before they happen
- Respond after a nick has been fully applied

All public API classes live in the `com.nicked.api` and `com.nicked.api.event` packages.

---

## Adding Nicked as a Dependency

Nicked is published via [JitPack](https://jitpack.io/#PharoGames/Nicked). Declare it as a `compileOnly` (provided) dependency — it is not bundled into your plugin's jar.

Replace `TAG` with the latest release tag (e.g. `v1.0.0`). You can browse available versions at [jitpack.io/#PharoGames/Nicked](https://jitpack.io/#PharoGames/Nicked).

=== "Gradle (Groovy DSL)"

    ```groovy
    repositories {
        maven { url = 'https://jitpack.io' }
    }

    dependencies {
        compileOnly 'net.pharogames:Nicked:TAG:api'
    }
    ```

=== "Gradle (Kotlin DSL)"

    ```kotlin
    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        compileOnly("net.pharogames:Nicked:TAG:api")
    }
    ```

=== "Maven"

    ```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>net.pharogames</groupId>
            <artifactId>Nicked</artifactId>
            <version>TAG</version>
            <classifier>api</classifier>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    ```

!!! note "Why the `api` classifier?"
    The `api` classifier selects the thin jar (no shaded dependencies), which is what you want as a compile-time dependency. The default jar is the full server plugin with relocated internals.

Declare Nicked as a soft dependency in your own `plugin.yml` so Bukkit loads it before your plugin:

```yaml
softdepend:
  - Nicked
```

Use `softdepend` (not `depend`) unless your plugin **cannot function at all** without Nicked. Soft dependency allows your plugin to load even when Nicked is absent.

---

## Getting the API Instance

The API singleton is accessible through `NickedAPIProvider`:

```java
import com.nicked.api.NickedAPI;
import com.nicked.api.NickedAPIProvider;

public class MyPlugin extends JavaPlugin {

    private NickedAPI nicked;

    @Override
    public void onEnable() {
        // Safe approach: check if Nicked is enabled first
        if (getServer().getPluginManager().getPlugin("Nicked") == null) {
            getLogger().warning("Nicked is not installed. Nick features disabled.");
            return;
        }

        nicked = NickedAPIProvider.getAPI();
        getLogger().info("Hooked into Nicked API.");
    }
}
```

Alternatively, use `getAPIOrNull()` when you want to handle optional Nicked support inline:

```java
NickedAPI api = NickedAPIProvider.getAPIOrNull();
if (api != null) {
    // Nicked is available
}
```

!!! warning
    `NickedAPIProvider.getAPI()` throws `IllegalStateException` if called before Nicked has fully enabled (e.g. in `onLoad()`). Always access the API in `onEnable()` or later.

---

## Quick Examples

### Check if a player is nicked

```java
boolean isNicked = nicked.isNicked(player.getUniqueId());
```

### Get a player's display name

```java
// Returns nicked name if nicked, otherwise real name
String displayName = nicked.getDisplayName(player.getUniqueId());
```

### Nick a player from your plugin

```java
import com.nicked.nick.NickCause;

nicked.nickPlayer(player.getUniqueId(), "Notch", NickCause.PLUGIN);
```

### Remove a player's nick

```java
nicked.unnickPlayer(player.getUniqueId(), NickCause.PLUGIN);
```

### Read full nick info

```java
import com.nicked.api.NickInfo;
import java.util.Optional;

Optional<NickInfo> info = nicked.getNickInfo(player.getUniqueId());
info.ifPresent(nick -> {
    getLogger().info(nick.realName() + " is nicked as " + nick.nickedName());
});
```

### Listen to a nick event

```java
import com.nicked.api.event.NickChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NickListener implements Listener {

    @EventHandler
    public void onNickChange(NickChangeEvent event) {
        if (event.getNewNick().equalsIgnoreCase("Admin")) {
            event.setCancelled(true); // Block anyone from nicking as "Admin"
        }
    }
}
```

---

## API Surface Summary

| Component | Description |
|---|---|
| [`NickedAPI`](methods.md) | Main interface — 6 methods for querying and modifying nick state |
| [`NickedAPIProvider`](methods.md#nickedapiprovider) | Static singleton accessor |
| [`NickInfo`](data-types.md#nickinfo) | Immutable snapshot of a player's current nick state |
| [`SkinData`](data-types.md#skindata) | Immutable skin texture value and signature pair |
| [`NickCause`](data-types.md#nickcause) | Enum identifying why a nick was applied or removed |
| [`NickChangeEvent`](events.md#nickchangeevent) | Fired before a nick is applied — cancellable |
| [`NickApplyEvent`](events.md#nickapplyevent) | Fired after a nick is fully applied — not cancellable |
| [`NickRemoveEvent`](events.md#nickremoveevent) | Fired before a nick is removed — cancellable |
| [`NickResolveEvent`](events.md#nickresolveevent) | Fired when a display name is resolved — allows override |
| [`NickRandomSelectEvent`](events.md#nickrandomselectevent) | Fired when a random nick name is selected — cancellable, name is mutable |
