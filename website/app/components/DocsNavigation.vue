<script setup lang="ts">
defineProps<{
  showContentSearch: boolean
  search: typeof useSearchCollection
  navigation: typeof queryCollectionNavigation
}>()

const route = useRoute()
</script>

<template>
  <ClientOnly>
    <UContentSearchButton
      class="mb-5 w-full"
      :collapsed="false"
    />
    <UContentSearch
      v-if="showContentSearch"
      :navigation="navigation"
      :search="search.search"
      :search-status="search.status.value"
    />
  </ClientOnly>

  <UContentNavigation
    :key="route.fullPath"
    :navigation="navigation"
    type="single"
    highlight
    default-open
  />
</template>
