<script setup lang="ts">
const route = useRoute()

const title = computed(() => $t(`layout.view.${String(route.name)}.title`))
const description = computed(() => $t(`layout.view.${String(route.name)}.description`))

useSeoMeta({
  title,
  description,
  ogTitle: title,
  ogDescription: description
})
</script>

<template>
  <div>
    <slot />
  </div>
</template>
