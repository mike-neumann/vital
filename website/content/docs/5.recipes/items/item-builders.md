---
title: Recipes for item builders
description: This page defines a recipe (pre-written code snippet) for item builders, that can be copied by a developer instead of manually writing it.
navigation:
  title: Item builders
---

# Item builders

## Normal item builder

Below is an example of an item created using the item builder.  

```java
final var item = VitalItemStack.builder()
  .type(Material.COBBLESTONE)
  .name("My custom item")
  .amount(1)
  .unbreakable(true)
  .lore({"Line 1", "Line 2"})
  .itemFlags({ItemFlag.HIDE_ATTRIBUTES})
  .enchantments(Map.of(Enchantment.DURABILITY, 1, Enchantment.MENDING, 1))
  .build();
```

## Head item builder

Below is an example of a head item created using the head item builder.  

```java
final var head = VitalHeadItemStack.builder()
  .owningPlayer(somePlayer)
  .type(Material.COBBLESTONE)
  .name("My custom item")
  .amount(1)
  .unbreakable(true)
  .lore({"Line 1", "Line 2"})
  .itemFlags({ItemFlag.HIDE_ATTRIBUTES})
  .enchantments(Map.of(Enchantment.DURABILITY, 1, Enchantment.MENDING, 1))
  .build();
```
