<script setup lang="ts">
definePageMeta({
  layout: "docs"
})

const route = useRoute()
const page = ref()

watchEffect(() => {
  queryCollection("docs").path(route.path).first().then(it => page.value = it)
})

useSeoMeta({
  title: page.value?.title,
  description: page.value?.description
})
</script>

<template>
  <div>
    <ContentRenderer v-if="page" :value="page" />
  </div>
</template>
