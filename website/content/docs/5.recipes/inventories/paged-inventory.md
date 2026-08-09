---
title: Recipe for a paged inventory
description: This page defines a recipe (pre-written code snippet) for a paged inventory, that can be copied by a developer instead of manually writing it.
navigation:
  title: Paged inventory
---

# Paged inventory

Below is an example of a paged inventory (9x3 slots).

```java [MyPagedInventory.java]
@VitalPagedInventory.Info(fromSlot = 10, toSlot = 16)
@VitalInventory.Info(type = VitalInventory.Type.GENERIC_9X3, name = "My paged inventory")
public class MyPagedInventory extends VitalPagedInventory {
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

		// Use this lifecycle to set dynamic items that may change when this inventory is updated.
	}

	@Override
	public void onPageChange(int page, Player player) {
		final var someData = List.of("Element 1", "Element 2", "Element 3");
		final var pageContent = sliceForPage(player, someData);

		final var fromSlot = getInfo(VitalPagedInventory.Info.class);
		for (int i = 0; i < pageContent.size(); I++) {
			final var content = pageContent.get(i);
			final var slot = fromSlot + i;
			
			final var item = new ItemStack(Material.PAPER);
			item.setDisplayName(content);
			setItem(slot, item, (e) -> {
				player.sendMessage("You have clicked the paged item '" + content + "'!");
			});
		}

		// Use this lifecycle to set dynamic items for your paged content.
		// Normally, you would only set items in the configured "fromSlot" and "toSlot" here.
	}
}
```
