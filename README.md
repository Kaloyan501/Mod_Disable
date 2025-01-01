<div>
  <img src="https://github.com/Kaloyan501/Mod_Disable/assets/68351222/704482e3-eb9c-4ad4-a0e8-6d8c0cd0d3bd" width="250" height="250"></img> 
</div>

# Mod_Disable
A simple Minecraft Mod to disable recipes and items from other mods.

# Commands
  - /disable_mod - main command to interface with the mod
  - /disable_mod <enable/disable> item - enables/disables a specific item
  - /disable_mod <enable/disable> namespace - Disables all items within a namespace (for example, all items starting with minecraft: )
  - /disable_mod config DefaultDisabledItemsListFromPlayerUUID <UUID> - generates a default disabled items list for a modpack, allowing large servers to not lag while searching for namespaces for new players. First, disable all namespaces you want to be disabled for new players for your player, then provide your UUID. to this command.
  - /disable_mod init - Copies the default disabled items list to the player that initiated the command's UUID. Use this instead of manually disabling every mod namespace.
  - /disable_mod reinit <UUID> - Reinits a corrupted disabled items list by doing the same as init. Note: This will delete the selected player's mod unlock progress!

*Important!*
For the disabled items list to be created, the /disable_mod init command must be run by every new player trough chat, quest or other means.

<h1>The creator of this program is not responsible for any damage done by it and it does not come with ANY warranty.</h1>
