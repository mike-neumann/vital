<script setup lang="ts">
defineProps<{
  showContentSearch: boolean
  search: typeof useSearchCollection
  navigation: typeof queryCollectionNavigation
}>()
</script>

<template>
  <ClientOnly>
    <UContentSearchButton class="mb-5 w-full" :collapsed="false" />
    <UContentSearch v-if="showContentSearch" :navigation="navigation" :search="search.search" :search-status="search.status.value" />
  </ClientOnly>

  <UContentNavigation :navigation="navigation" type="single" highlight />
</template>
