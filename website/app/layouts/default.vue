<script setup lang="ts">
import type {NavigationMenuItem} from "@nuxt/ui";

const { t, availableLocales, setLocale, locale } = useI18n()

const navItems = computed<NavigationMenuItem[]>(() => ([
  {
    icon: "i-lucide-home",
    label: t("layout.item.home"),
    to: "/"
  },
  {
    icon: "i-lucide-book-open",
    label: t("layout.item.docs"),
    to: "/docs"
  }
]))

const languageDropdownMenuItems = computed<NavigationMenuItem[]>(() => availableLocales.map(it => ({
  label: t(`layout.language.${it}`),
  onSelect: () => setLocale(it)
})))
</script>

<template>
  <UHeader>
    <template #left>
      <NuxtLink to="/">
        <AppLogo class="w-auto h-12 shrink-0" />
      </NuxtLink>
    </template>

    <UNavigationMenu :items="navItems" />

    <template #right>
      <UDropdownMenu :items="languageDropdownMenuItems">
        <UButton icon="i-lucide-globe" variant="ghost" color="neutral" trailing-icon="i-lucide-chevron-down">
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

  <UMain>
    <div>
      <slot />
    </div>
  </UMain>

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
</template>

<style scoped>

</style>
