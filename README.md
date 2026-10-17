<div>
  <img src="https://github.com/Kaloyan501/Mod_Disable/assets/68351222/704482e3-eb9c-4ad4-a0e8-6d8c0cd0d3bd" width="250" height="250"></img> 
</div>

# Mod_Disable

A simple Minecraft Mod to disable recipes and items from other mods.

# Commands

- /disable_mod - main command to interface with the mod
- /disable_mod <enable/disable> item - enables/disables a specific item
- /disable_mod <enable/disable> namespace - Disables all items within a namespace (for example, all items starting with
  minecraft: )
- /disable_mod config DefaultDisabledItemsListFromPlayerUUID <UUID> - generates a default disabled items list for a
  modpack, allowing large servers to not lag while searching for namespaces for new players. First, disable all
  namespaces you want to be disabled for new players for your player, then provide your UUID. to this command.
- /disable_mod init - Copies the default disabled items list to the player that initiated the command's UUID. Use this
  instead of manually disabling every mod namespace.
- /disable_mod reinit <UUID> - Reinits a corrupted disabled items list by doing the same as init. Note: This will delete
  the selected player's mod unlock progress!
- /disable_mod disable/enable all - disables/enables all items currently registered in the item registry.

*Important!*
For the disabled items list to be created, the /disable_mod init command must be run by every new player trough chat,
quest or other means.

<h1>The creator of this program is not responsible for any damage done by it and it does not come with ANY warranty.</h1>

# Testing

This mod tests itself through real Minecraft on both supported loaders (Fabric and NeoForge). Every new feature must include tests.

## Running Tests

### All loaders (recommended)
```bash
./gradlew check
```
Runs unit tests for `common`, `fabric`, and `neoforge`, plus Fabric game tests that boot a real Minecraft server.

### Specific loader
```bash
./gradlew :fabric:test   # Fabric unit + game tests
./gradlew :neoforge:test # NeoForge unit tests
./gradlew :common:test   # Common unit tests
```

## Test Structure

| Module | Test Type | Location | Runs Through Minecraft? |
|--------|-----------|----------|------------------------|
| `common` | Unit tests | `src/test/java` | No (pure logic) |
| `fabric` | Unit tests | `src/test/java` | No (pure logic) |
| `fabric` | Game tests | `src/gametest/java` | **Yes** (real Minecraft server) |
| `neoforge` | Unit tests | `src/test/java` | No (pure logic) |

### Test Coverage

Tests cover every major component of the mod:

- **RecipeManager** — disable/enable item, namespace, all items; JSON persistence; idempotency
- **FileSecurity** — checksum computation and consistency
- **StaticPathStorage** — world path get/set
- **PlayerItemHashmapper** — JSON load/save, default list intersection
- **CopyFolderContents** — recursive folder copy with replacement
- **RecursiveFolderDeleter** — recursive deletion
- **Migration_110_Json_Check** — UUID filename validation
- **Command registration** — verified accessible via reflection
- **Game tests (Fabric)** — vanilla item registry, mock player creation in a real test level

### Adding New Features

1. Write unit tests for any new pure-logic classes in `common/src/test/java`
2. Write unit tests for loader-specific logic in `fabric/src/test/java` or `neoforge/src/test/java`
3. Write game tests for any in-Minecraft behavior (item use, commands, events) in `fabric/src/gametest/java` (Fabric) or via `RegisterGameTestsEvent` (NeoForge)
4. All tests must pass on both loaders before merging
