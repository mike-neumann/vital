<script setup lang="ts">
import type { BreadcrumbItem } from '#ui/components/Breadcrumb.vue'
import type { ContentNavigationItem } from '@nuxt/content'

const route = useRoute()
const { locale, defaultLocale } = useI18n()
const siteConfig = useSiteConfig()
const config = useRuntimeConfig()
const search = useSearchCollection('docs')

const open = ref(true)

const { data: navigation } = await useAsyncData('docs-navigation', async () => (await queryCollectionNavigation('docs'))?.[0]?.children ?? [])
const { data: estimatedReadTime } = await useAsyncData(() => `docs-page-estimated-read-time:${route.fullPath}`, async () => {
  const page = await queryCollection('docs').path(route.path).first()
  if (!page?.body) {
    return undefined
  }

  const words = page.rawbody.trim().split(/\s+/).length
  return Math.max(1, Math.ceil(words / config.public.estimatedWordsPerMinute))
})

const breadcrumb = computed<BreadcrumbItem>(() => findBreadcrumb(navigation.value ?? [], route.path).map(it => ({ label: it.title, to: it.path })))
const createIssueUrl = computed(() => {
  const pageUrl = encodeURIComponent(`${siteConfig.url}${route.path}`)
  return `${config.public.githubCreateWebdocIssueUrl}&page=${pageUrl}`
})

function findBreadcrumb(items: ContentNavigationItem[], path: string, parents: ContentNavigationItem[] = []): BreadcrumbItem[] {
  for (const item of items) {
    const current = [...parents, item]

    if (item.path === path) {
      return current
    }

    if (item.children) {
      const result = findBreadcrumb(item.children, path, current)
      if (result.length > 0) return result
    }
  }

  return []
}
</script>

<template>
  <div>
    <div
      v-if="locale !== defaultLocale"
      class="sticky"
    >
      <UBanner
        :title="$t('layout.docs.language-warning')"
        color="warning"
      />
    </div>

    <UPage class="mt-5">
      <template #left>
        <UPageAside>
          <div class="pl-5">
            <DocsNavigation
              :search="search"
              :navigation="navigation"
              show-content-search
            />
          </div>
        </UPageAside>
      </template>

      <div class="pl-5 pr-5">
        <div class="lg:hidden">
          <DocsNavigation
            :search="search"
            :navigation="navigation"
            :show-content-search="false"
          />

          <div class="pt-5 pb-5">
            <USeparator />
          </div>
        </div>

        <div class="pl-2">
          <div>
            <UBreadcrumb :items="breadcrumb" />
            <UBadge
              class="mt-2"
              icon="i-lucide-glasses"
              :label="$t('layout.docs.estimated-read-time', { estimatedReadTime }, estimatedReadTime)"
              variant="outline"
              color="info"
            />
          </div>

          <div class="pt-3 mt-2">
            <slot />
          </div>
        </div>
      </div>
    </UPage>

    <UCard class="fixed bottom-6 right-6 z-50 w-80">
      <div class="flex items-center justify-between">
        <div>
          <p class="font-medium flex items-center justify-between gap-3">
            <UIcon name="i-lucide-info" />

            {{ $t('layout.docs.create-issue.title') }}
          </p>
        </div>

        <UButton
          variant="ghost"
          color="neutral"
          square
          :icon="open ? 'i-lucide-chevron-down' : 'i-lucide-chevron-up'"
          @click="open = !open"
        />
      </div>

      <UCollapsible :open="open">
        <template #content>
          <div class="mt-3 space-y-3">
            <p class="text-sm text-muted">
              {{ $t('layout.docs.create-issue.description') }}
            </p>

            <USeparator />

            <p class="text-sm text-muted font-extrabold">
              {{ $t('layout.docs.create-issue.sub-description') }}
            </p>

            <UButton
              :to="createIssueUrl"
              target="_blank"
              icon="i-simple-icons-github"
              trailing-icon="i-lucide-external-link"
              block
            >
              {{ $t('layout.docs.create-issue.button') }}
            </UButton>
          </div>
        </template>
      </UCollapsible>
    </UCard>
  </div>
</template>
