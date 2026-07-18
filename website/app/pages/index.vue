<script setup lang="ts">
import Code from "~/components/Code.vue";

const heroLinks = computed(() => [{
  label: $t("view.index.hero.get-started"),
  to: "/docs",
  target: "_blank",
  trailingIcon: "i-lucide-book-open",
  size: "xl"
}])

const heroCode = `
@Vital.Info(
  name = "myplugin",
  description = "My awesome Vital plugin",
  version = "1.0.0",
  apiVersion = "1.21",
  environment = "Vital.PluginEnvironment.PAPER"
)
public class MyPlugin {}
`

const features = computed(() => [
  {
    icon: "i-lucide-cog",
    title: $t("view.index.features.features.autoconfigured.title"),
    description: $t("view.index.features.features.autoconfigured.description")
  }, {
    icon: "i-lucide-zap",
    title: $t("view.index.features.features.powerful.title"),
    description: $t("view.index.features.features.powerful.description")
  }, {
    icon: "i-lucide-eye",
    title: $t("view.index.features.features.readable.title"),
    description: $t("view.index.features.features.readable.description")
  }, {
    icon: "i-lucide-blocks",
    title: $t("view.index.features.features.modular.title"),
    description: $t("view.index.features.features.modular.description")
  }, {
    icon: "i-lucide-boxes",
    title: $t("view.index.features.features.multiplatform-ready.title"),
    description: $t("view.index.features.features.multiplatform-ready.description")
  }, {
    icon: "i-lucide-feather",
    title: $t("view.index.features.features.lightweight.title"),
    description: $t("view.index.features.features.lightweight.description")
  }
])
</script>

<template>
  <div>
    <UPageHero
      :title="$t('view.index.hero.title')"
      :description="$t('layout.description')"
      orientation="horizontal"
      :links="heroLinks"
    >
      <Code :title="$t('view.index.hero.code.title')" lang="java" :code="heroCode" />
    </UPageHero>

    <UPageSection
      id="features"
      :title="$t('view.index.features.title')"
      :description="$t('view.index.features.description')"
      :features="features"
    />

    <UPageSection>
      <UPageCTA
        :title="$t('view.index.cta.title')"
        :description="$t('view.index.cta.description')"
        variant="subtle"
        orientation="horizontal"
      >
        <UPageGrid>
          <UPageCard
            :title="$t('view.index.cta.cards.documentation.title')"
            :description="$t('view.index.cta.cards.documentation.description')"
            icon="i-lucide-book-open"
            class="col-span-full"
            to="/docs"
          />
        </UPageGrid>
      </UPageCTA>
    </UPageSection>
  </div>
</template>
