# Permissions

All Nicked permission nodes default to **op** — operators have every permission out of the box. You can grant or deny individual nodes using any permissions plugin (LuckPerms, PermissionsEx, etc.).

---

## Node Reference

| Permission | Default | Description |
|---|---|---|
| `nicked.admin` | op | Grants all Nicked permissions at once (parent node). |
| `nicked.command.nick` | op | Allows use of `/nick`. |
| `nicked.command.nickall` | op | Allows use of `/nickall`. |
| `nicked.command.unnick` | op | Allows use of `/unnick`. |
| `nicked.command.nickother` | op | Allows use of `/nickother`. |
| `nicked.command.realname` | op | Allows use of `/realname`. |

---

## `nicked.admin`

The parent permission. Granting this single node to a player or group is equivalent to granting all six nodes below it.

```yaml
# plugin.yml definition (for reference)
nicked.admin:
  description: Grants access to all Nicked commands.
  default: op
  children:
    nicked.command.nick: true
    nicked.command.nickall: true
    nicked.command.unnick: true
    nicked.command.nickother: true
    nicked.command.realname: true
```

---

## Individual Command Permissions

### `nicked.command.nick`

Required to run `/nick`. Allows a player to nick themselves with a specific name, pick a random nick, or toggle their nick off.

### `nicked.command.nickall`

Required to run `/nickall`. Allows the sender to randomly nick every online player at once. This is a powerful command — restrict it carefully.

### `nicked.command.unnick`

Required to run `/unnick`. Allows a player to remove their own active nick.

### `nicked.command.nickother`

Required to run `/nickother`. Allows the sender to nick any online player with any name. This is a moderator-level permission.

### `nicked.command.realname`

Required to run `/realname`. Allows the sender to look up the real username of a nicked player.

---

## LuckPerms Examples

### Grant admin access to a group

```bash
/lp group admin permission set nicked.admin true
```

### Grant individual commands to a group

```bash
/lp group moderator permission set nicked.command.nick true
/lp group moderator permission set nicked.command.unnick true
/lp group moderator permission set nicked.command.realname true
```

### Deny a specific command even for ops

```bash
/lp group default permission set nicked.command.nickall false
```

---

## Notes

- Permissions are checked at command execution time, not at login. Changes made via a permissions plugin take effect immediately without a restart.
- The `nicked.admin` parent node uses Bukkit's child permission inheritance. Most modern permissions plugins respect this, but if a plugin does not, grant the individual `nicked.command.*` nodes explicitly.
