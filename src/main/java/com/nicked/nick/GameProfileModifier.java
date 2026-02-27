package com.nicked.nick;

import com.nicked.NickedPlugin;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/**
 * <strong>DANGER:</strong> Modifies the server-side GameProfile via reflection.
 *
 * <p>When enabled this makes the nick change visible to ALL server-side systems:
 * other plugins, commands targeting by name, and any system that reads the game
 * profile directly. Data corruption is possible if other plugins store data by
 * player name. This feature is <strong>disabled by default</strong>.</p>
 *
 * <p>All Mojang Authlib classes are accessed via reflection to avoid a hard
 * compile-time dependency on the bundled server classes.</p>
 */
public final class GameProfileModifier {

    private final NickedPlugin plugin;
    private final Map<UUID, Object> originalProfiles = new HashMap<>();

    public GameProfileModifier(NickedPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Replaces the game profile name and skin for the given player.
     *
     * @param player     the player to modify
     * @param nickedName the fake name to apply
     * @param skin       the skin to apply, or {@code null} to leave unchanged
     */
    public void applyNick(Player player, String nickedName, SkinData skin) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(nickedName, "nickedName");

        try {
            Object profile = getGameProfile(player);
            if (profile == null) {
                return;
            }

            originalProfiles.putIfAbsent(player.getUniqueId(), cloneProfile(profile));
            setProfileName(profile, nickedName);

            if (skin != null) {
                replaceSkinProperty(profile, skin);
            }
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to modify GameProfile for player " + player.getName(), e);
        }
    }

    /**
     * Restores the original game profile for the given player.
     *
     * @param player the player to restore
     */
    public void restoreNick(Player player) {
        Objects.requireNonNull(player, "player");
        Object original = originalProfiles.remove(player.getUniqueId());
        if (original == null) {
            return;
        }
        try {
            Object current = getGameProfile(player);
            if (current == null) {
                return;
            }
            String originalName = getProfileName(original);
            setProfileName(current, originalName);

            Object originalProps = getProperties(original);
            Object currentProps = getProperties(current);
            if (originalProps != null && currentProps != null) {
                invokeMethod(currentProps, "removeAll", String.class, "textures");
                Object originalTextures = invokeMethod(originalProps, "get", Object.class, "textures");
                if (originalTextures != null) {
                    invokeMethod(currentProps, "putAll", Object.class, "textures", originalTextures);
                }
            }
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to restore GameProfile for player " + player.getName(), e);
        }
    }

    /** Restores all modified profiles. Called on plugin disable. */
    public void restoreAll() {
        for (UUID uuid : originalProfiles.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                restoreNick(player);
            }
        }
        originalProfiles.clear();
    }

    private Object getGameProfile(Player player) throws ReflectiveOperationException {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object handle = getHandle.invoke(player);

        Field profileField = findField(handle.getClass(), "gameProfile");
        if (profileField == null) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not find gameProfile field on " + handle.getClass().getName());
            return null;
        }
        profileField.setAccessible(true);
        return profileField.get(handle);
    }

    private void setProfileName(Object profile, String name) throws ReflectiveOperationException {
        Field nameField = findField(profile.getClass(), "name");
        if (nameField == null) {
            plugin.getLogger().log(Level.WARNING, "Could not find 'name' field on GameProfile");
            return;
        }
        nameField.setAccessible(true);
        nameField.set(profile, name);
    }

    private String getProfileName(Object profile) throws ReflectiveOperationException {
        Method getName = profile.getClass().getMethod("getName");
        return (String) getName.invoke(profile);
    }

    private Object getProperties(Object profile) throws ReflectiveOperationException {
        Method getProperties = profile.getClass().getMethod("getProperties");
        return getProperties.invoke(profile);
    }

    private void replaceSkinProperty(Object profile, SkinData skin) throws ReflectiveOperationException {
        Object properties = getProperties(profile);
        if (properties == null) {
            return;
        }

        invokeMethod(properties, "removeAll", String.class, "textures");

        Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
        Object property = propertyClass
                .getConstructor(String.class, String.class, String.class)
                .newInstance("textures", skin.value(), skin.signature());

        Method put = properties.getClass().getMethod("put", Object.class, Object.class);
        put.invoke(properties, "textures", property);
    }

    /**
     * Creates a shallow clone of the GameProfile including its name and properties.
     * Properties (skin textures) are copied by reference from the Multimap, which is
     * sufficient for restoration since we replace entries rather than mutating values.
     */
    private Object cloneProfile(Object profile) throws ReflectiveOperationException {
        Method getId = profile.getClass().getMethod("getId");
        Method getName = profile.getClass().getMethod("getName");
        UUID id = (UUID) getId.invoke(profile);
        String name = (String) getName.invoke(profile);
        Object clone = profile.getClass().getConstructor(UUID.class, String.class).newInstance(id, name);

        Object srcProperties = getProperties(profile);
        Object dstProperties = getProperties(clone);
        if (srcProperties != null && dstProperties != null) {
            Method putAll = dstProperties.getClass().getMethod("putAll", Object.class);
            putAll.invoke(dstProperties, srcProperties);
        }
        return clone;
    }

    private Object invokeMethod(Object target, String methodName, Class<?> argType, Object... args)
            throws ReflectiveOperationException {
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(methodName)) {
                try {
                    m.setAccessible(true);
                    return m.invoke(target, args);
                } catch (IllegalArgumentException ignored) {
                    // try next overload
                }
            }
        }
        return null;
    }

    private Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
