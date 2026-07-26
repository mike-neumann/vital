<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const { availableLocales, setLocale, locale } = useI18n()

useSeoMeta({
  ogImage: 'ogImage.png',
  twitterCard: 'summary_large_image'
})

useHead({
  titleTemplate: (title) => {
    const siteName = $t('layout.default.title')

    return title
      ? `${title} · ${siteName}`
      : siteName
  },
  meta: [
    { name: 'viewport', content: 'width=device-width, initial-scale=1' }
  ],
  link: [
    { rel: 'icon', href: '/favicon.ico' }
  ],
  htmlAttrs: {
    lang: locale
  }
})

const navItems = computed<NavigationMenuItem[]>(() => ([
  {
    icon: 'i-lucide-home',
    label: $t('layout.item.home'),
    to: '/'
  },
  {
    icon: 'i-lucide-list',
    label: $t('layout.item.all-features'),
    to: '/all-features'
  },
  {
    icon: 'i-lucide-book-open',
    label: $t('layout.item.docs'),
    to: '/docs'
  },
  {
    icon: 'i-lucide-code',
    label: $t('layout.item.dokka'),
    href: '/dokka',
    target: '_blank',
    external: true
  }
]))

const languageDropdownMenuItems = computed<NavigationMenuItem[]>(() => availableLocales.map(it => ({
  label: $t(`layout.language.${it}`),
  onSelect: () => setLocale(it)
})))
</script>

<template>
  <div>
    <UApp>
      <UHeader>
        <template #left>
          <NuxtLink to="/">
            <AppLogo class="w-auto h-12 shrink-0" />
          </NuxtLink>
        </template>

        <UNavigationMenu :items="navItems" />

        <template #body>
          <UNavigationMenu
            :items="navItems"
            orientation="vertical"
          />
        </template>

        <template #right>
          <UDropdownMenu :items="languageDropdownMenuItems">
            <UButton
              icon="i-lucide-globe"
              variant="ghost"
              color="neutral"
              trailing-icon="i-lucide-chevron-down"
            >
              {{ $t(`layout.language.${locale}`) }}
            </UButton>
          </UDropdownMenu>

          <UColorModeButton />

          <UButton
            to="https://github.com/mike-neumann/vital"
            target="_blank"
            icon="i-simple-icons-github"
            aria-label="GitHub"
            color="neutral"
            variant="ghost"
          />
        </template>
      </UHeader>

      <NuxtLayout>
        <UMain>
          <NuxtPage />
        </UMain>
      </NuxtLayout>

      <USeparator icon="i-simple-icons-nuxtdotjs" />

      <UFooter>
        <template #left>
          <p class="text-sm text-muted">
            This site was built with Nuxt UI • © {{ new Date().getFullYear() }}
          </p>
        </template>

        <template #right>
          <UButton
            to="https://github.com/nuxt-ui-templates/starter"
            target="_blank"
            icon="i-simple-icons-github"
            aria-label="GitHub"
            color="neutral"
            variant="ghost"
          />
        </template>
      </UFooter>
    </UApp>
  </div>
</template>
