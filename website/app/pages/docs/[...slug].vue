<script setup lang="ts">
definePageMeta({
  layout: 'docs'
})

const route = useRoute()
const page = ref()

const title = computed(() => page.value?.title)
const description = computed(() => page.value?.description)

watchEffect(() => {
  queryCollection('docs').path(route.path).first().then(it => page.value = it)
})

useHead({
  titleTemplate: (title) => {
    const siteName = $t('layout.docs.title')

    return title
      ? `${title} · ${siteName}`
      : siteName
  }
})

useSeoMeta({
  title,
  description,
  ogTitle: title,
  ogDescription: description
})
</script>

<template>
  <div>
    <ContentRenderer
      v-if="page"
      :value="page"
    />
  </div>
</template>
