<script setup lang="ts">
const search = useSearchCollection("docs")

const navigation = ref()

watchEffect(() => {
  queryCollectionNavigation("docs").then(it => {
    // Navigation will ALWAYS have at least one element, which is the ROOT node.
    // Because we don't want to have the root node, we will discard it and only use its direct children.
    navigation.value = it[0]!.children
  })
})
</script>

<template>
  <div>
    <UPage class="mt-5">
      <template #left>
        <UPageAside>
          <div class="pl-5">
            <ClientOnly>
              <UContentSearchButton class="mb-5 w-full" :collapsed="false" />
              <UContentSearch :navigation="navigation" :search="search.search" :search-status="search.status" />
            </ClientOnly>

            <UContentNavigation :navigation="navigation" type="single" highlight />
          </div>
        </UPageAside>
      </template>

      <slot />
    </UPage>
  </div>
</template>
