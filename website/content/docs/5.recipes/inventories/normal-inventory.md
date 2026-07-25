---
title: Recipe for a normal inventory.
description: This page defines a recipe (pre-written code snippet) for an inventory, that can be copied by a developer instead of manually writing it.
navigation:
  title: Normal inventory
---

# Inventory

Below is an example of a normal one-paged inventory (9x3 slots).

```java [MyInventory.java]
@VitalInventory.Info(type = VitalInventory.Type.GENERIC_9X3, name = "My inventory")
public class MyInventory extends VitalInventory {
	@Override
	public void onUpdate() {
		// This item doesn’t do anything.
		setItem(0, new ItemStack(Material.STICK));
	}

	@Override
	public void onUpdate(Player player) {
		// This item can be clicked.
		setItem(1, new ItemStack(Material.COBBLESTONE), (e) -> {
			// Do something when this item is clicked.
			player.sendMessage("You have clicked the item in slot 1");
		});
	}
}
```
