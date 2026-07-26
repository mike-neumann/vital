<script setup lang="ts">
import { codeToHtml } from 'shiki'

const props = defineProps<{
  title: string
  lang: string
  code: string
}>()

const colorMode = useColorMode()

const isLoading = ref(true)
const html = ref('')

watchEffect(async () => {
  isLoading.value = true
  html.value = await codeToHtml(props.code.trim(), {
    lang: 'java',
    theme: colorMode.value === 'dark'
      ? 'github-dark'
      : 'github-light',
    transformers: [{
      pre: (node) => {
        (node.properties.style = '')
      }
    }]
  })
  isLoading.value = false
})
</script>

<template>
  <UCard>
    <div class="flex items-center justify-between mb-2">
      <span class="text-sm font-medium">{{ title }}</span>

      <UButton
        icon="i-lucide-copy"
        variant="ghost"
        @click="navigator.clipboard.writeText(code)"
      />
    </div>

    <div v-if="isLoading">
      <USkeleton
        v-for="line of code.split('\n')"
        :key="line"
        class="h-4 mb-1"
        :style="{ width: `${line.length}ch` }"
      />
    </div>
    <pre
      class="overflow-x-auto rounded text-sm text-white"
      v-html="html"
    />
  </UCard>
</template>
