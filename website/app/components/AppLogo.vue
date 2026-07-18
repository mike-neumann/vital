<script setup lang="ts">
const colorMode = useColorMode()
const src = computed(() => colorMode.value === "light" ? "/logo_text--light.png" : "/logo_text--dark.png")
</script>

<template>
  <ClientOnly>
    <img :src="src">
  </ClientOnly>
</template>
