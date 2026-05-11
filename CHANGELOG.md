- Added 'Technical' config category
  - Contains 'Console Info', 'Spawn Gravestones with Keep Inventory', and the new 'Allow Fake Players'
  - This category is for highly technical things that may cause issues or unintended behavior. Change at your own risk!
- Added 'Allow Fake Players' config
  - 'Fake Players' are player-like objects used by mods to simulate player behavior.
  For example, Deployers in Create
  - Previously, they were fully blacklisted from interacting with gravestones
  - Note that Fake Players are not intentionally supported,
  and could cause major issues (for example, items collected by Deployers are completely lost!)
- Major API improvements
  - Reworked events and add several new events
  - Improved documentation
- Improved 'Console Info' logs
  - All events now have an ID that is logged if 'Console Info' is enabled,
  so that the exact listener that caused an issue can be identified easier
  - Logs have been made clearer and more detailed
- Fixed gravestone collection not updating cached 'Console Info' value
- (26.1) Added Backpacked support
- (26.1) Added Trinkets support through Trinkets Updated
- (26.1) Added Resource Backpacks support
- (1.20) Added Galosphere support