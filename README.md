<img width="2520" height="360" alt="mob_blacklist_banner" src="https://github.com/user-attachments/assets/133c904f-df02-42df-af67-451f8713cca0" /><br>
**A simple mod that allows you to blacklist/disable mobs from spawning inside your world, directly from chat/commands.**
<br>

Version Compatability:
- 26.1.2 Fabric
- Forge & Fabric support for (26.1.2 + 1.21.x) will be added soon.

Requires:
- Fabric API

---

| Command                                                      | Description                                                                                                              |
| ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------ |
| `/mob_blacklist add <mob_name>`                                | Adds a specific mob (e.g., `phantom`) to the blacklist, stopping it from spawning.                             |
| `/mob_blacklist remove <mob_name>`                             | Removes a mob from the blacklist, allowing it to spawn naturally again.                                                  |
| `/mob_blacklist list`                                        | Displays a comma-separated list of all currently blacklisted mobs in this world.                                      |
| `/mob_blacklist toggle`                                      | Globally turns the blacklist functionality ON or OFF for the active world.                                               |
| `/mob_blacklist logs`                                        | Displays a list of each blacklisted mob type and exactly how many times it was blocked.                        |
| `/mob_blacklist logs reset`                                  | Resets all prevention counts back to 0 for this world.                                                        |
| `/mob_blacklist clear_world`                                 | From all loaded chunks across all dimensions (Overworld, Nether, End) and kills every blacklisted mob currently alive. |
| `/mob_blacklist clear_world <mob_id>`                        | Instantly removes all loaded instances of one specific blacklisted mob type from the world.                               |
| `/mob_blacklist settings allow_spawning_from_creative true`  | Allows players in Creative Mode to bypass the filter and use spawn eggs normally.                                        |
| `/mob_blacklist settings allow_spawning_from_creative false` | Blocks players from using blacklisted mob spawn eggs                     |
