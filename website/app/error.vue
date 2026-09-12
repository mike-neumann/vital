<script setup lang="ts">
import type { NuxtError } from '#app'

defineProps<{
  error: NuxtError
}>()

const { t } = useI18n()
</script>

<template>
  <NuxtLayout>
    <UContainer class="flex min-h-screen items-center justify-center">
      <div class="text-center">
        <h1 class="text-8xl font-bold">
          {{ error.status }}
        </h1>

        <p class="mt-4 text-2xl">
          {{ error.statusText }}
        </p>

        <UButton
          class="mt-8"
          to="/"
          @click="clearError({ redirect: '/' })"
        >
          {{ t("layout.error.home") }}
        </UButton>
      </div>
    </UContainer>
  </NuxtLayout>
</template>
