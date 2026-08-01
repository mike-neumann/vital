---
title: Recipe for localization
description: This page defines a recipe (pre-written code snippet) for localization, that can be copied by a developer instead of manually writing it.
navigation:
  title: Localization
---

# Localization

Below is an example on how to use the `vital-localization` module in your plugin.

Get translated text for a player:
```java
VitalLocalizationModule.Spigot.INSTANCE.t(myPlayer, "my.resource.key.name", anArrayOfPossibleParametersForYourResourceKey);
```

Get translated text without a player:
```java
VitalLocalizationModule.Spigot.INSTANCE.t(Locale.ENGLISH, "my.resource.key.name", anArrayOfPossibleParametersForYourResourceKey);
```

Set the language of a player:
```java
VitalLocalizationModule.Spigot.INSTANCE.setLocale(myPlayer, Locale.ENGLISH);
```

Updating a locale for a Spigot player will automatically also update his entire inventory and update all items from `vital-items` that are named with a resource key in their info annotation.  
