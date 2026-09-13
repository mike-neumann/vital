<script setup lang="ts">
import type { DocsCollectionItem } from '@nuxt/content'

definePageMeta({
  layout: 'docs'
})

const router = useRouter()
const route = useRoute()
const toast = useToast()
const { t } = useI18n()

const cleanPath = computed(() => route.path.replace(/\/+$/, '') || '/')

const { data: page, status } = await useAsyncData(
  () => `docs-page:${cleanPath.value}`,
  () => queryCollection('docs').path(cleanPath.value).first()
)

const title = computed<string | undefined>(() => page.value?.title)
const description = computed<string | undefined>(() => page.value?.description)

function checkPage(docs: DocsCollectionItem | null | undefined) {
  if (status.value !== 'pending' && !docs) {
    toast.add({
      id: 'docs-page-not-found-toast',
      title: t('layout.docs.not-found.title'),
      description: t('layout.docs.not-found.description')
    })
    router.push('/docs/about')
  }
}

useHead({
  titleTemplate: (title) => {
    const siteName = t('layout.docs.title')

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

watch([page, status], () => checkPage(page.value))
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
