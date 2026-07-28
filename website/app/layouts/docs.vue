<script setup lang="ts">
import type { BreadcrumbItem } from '#ui/components/Breadcrumb.vue'
import type { ContentNavigationItem } from '@nuxt/content'

const route = useRoute()
const { locale, defaultLocale } = useI18n()
const search = useSearchCollection('docs')

const navigation = ref<ContentNavigationItem[]>([])

const breadcrumb = computed<BreadcrumbItem>(() => findBreadcrumb(navigation.value, route.path).map(it => ({ label: it.title, to: it.path })))

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

watchEffect(() => {
  queryCollectionNavigation('docs').then((it) => {
    // Navigation will ALWAYS have at least one element, which is the ROOT node.
    // Because we don't want to have the root node, we will discard it and only use its direct children.
    navigation.value = it[0]?.children ?? []
  })
})
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
          <UBreadcrumb :items="breadcrumb" />

          <div class="pt-3">
            <slot />
          </div>
        </div>
      </div>
    </UPage>
  </div>
</template>
