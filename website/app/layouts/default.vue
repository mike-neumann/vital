<script setup lang="ts">
const route = useRoute()
const { t } = useI18n()

const title = computed<string>(() => t(`layout.view.${String(route.name)}.title`))
const description = computed<string>(() => t(`layout.view.${String(route.name)}.description`))

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
