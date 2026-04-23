- Changed gravestones to not finish being collected if an error occurs in the collection process
  - If an error occurs, the gravestone will remain in a partially collected state,
    so that any remaining data can be collected in future if the issue is fixed
- Improved order of events in the gravestone creation process
  - Items should no longer be lost when certain types of errors occur
- Improved clarity of the descriptions of some configs
- Changed 'Spawn Gravestones in Creative' config to be enabled by default
- Minor logging improvements
- Fixed the first gravestone created in a world per player not being saved correctly
- Fixed the `/gravestones getdata player` command not working in some situations