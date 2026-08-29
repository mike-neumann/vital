<script setup lang="ts">
import type { DocsCollectionItem } from '@nuxt/content'

definePageMeta({
  layout: 'docs'
})

const router = useRouter()
const route = useRoute()
const toast = useToast()

const title = computed(() => page.value?.title)
const description = computed(() => page.value?.description)

const { data: page } = await useAsyncData(`docs-page:${route.fullPath}`, () => queryCollection('docs').path(route.path).first())

function checkPage(docs: DocsCollectionItem | null | undefined) {
  if (!docs) {
    toast.add({
      id: 'docs-page-not-found-toast',
      title: $t('layout.docs.not-found.title'),
      description: $t('layout.docs.not-found.description')
    })
    router.push('/docs/about')
  }
}

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

watch(page, checkPage)
onMounted(() => checkPage(page.value))
</script>

<template>
  <div>
    <ContentRenderer
      v-if="page"
      :value="page"
    />
  </div>
</template>
