---
title: Recipe for a global hologram.
description: Recipe for a global hologram.
navigation:
  title: Global hologram
---

# Global holograms

Below is an example on how to create a global hologram.  
Global holograms should be used when you want to display data / text that will be the same for all of your players on your server.

Use the `VitalHologramService` bean to create and delete your holograms.

```java
vitalHologramService.createGlobalHologram(
	myLocation,
  () -> new VitalHologram.Line("Line 1"),
  () -> new VitalHologram.Line("Line 2"),
  () -> new VitalHologram.Line("Line 3")
);
```

To update your global hologram, simply use the `update()` method.
