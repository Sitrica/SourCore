# External Managers

Drag these into your project. Use any and add the gradle.

Make sure to register them aswell.

```
if (pluginManager.isPluginEnabled("Citizens"))
	externalManagers.add(new CitizensManager(instance));
```