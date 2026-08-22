---
title: Recipe for interactive items
description: This page defines a recipe (pre-written code snippet) for an interactable item, that can be copied by a developer instead of manually writing it.
navigation:
  title: Interactable items
---

# Interactable items

Below is an example of an item that your players can interact with.  
The item will have a cooldown of 1 second.  

```java [MyItem.java]
@VitalItem.Info(type = Material.STICK, name = "My item", cooldown = 1_000)
public class MyItem extends VitalItem {
	@Override
	public void onRightClick(PlayerInteractEvent e) {
		e.getPlayer().sendMessage("You have right-clicked the item!");
	}

	@Override
	public void onLeftClick(PlayerInteractEvent e) {
		e.getPlayer().sendMessage("You have left-clicked the item!");
	}

	@Override
	public void onCooldown(PlayerInteractEvent e) {
		e.getPlayer().sendMessage("The item is still on cooldown!");
	}
}
```
