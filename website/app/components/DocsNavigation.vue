<script setup lang="ts">
import type { ContentSearchFn, ContentSearchStatus } from '#ui/components/content/ContentSearch.vue'
import type { ContentNavigationItem } from '@nuxt/content'

defineProps<{
  showContentSearch: boolean
  search: ContentSearchFn
  searchStatus: ContentSearchStatus
  navigation: ContentNavigationItem[]
}>()

const route = useRoute()
</script>

<template>
  <div>
    <ClientOnly>
      <UContentSearchButton
        class="mb-5 w-full"
        :collapsed="false"
      />
      <UContentSearch
        v-if="showContentSearch"
        :navigation="navigation"
        :search="search"
        :search-status="searchStatus"
      />
    </ClientOnly>

    <UContentNavigation
      :key="route.fullPath"
      :navigation="navigation"
      type="single"
      highlight
      default-open
    />
  </div>
</template>
