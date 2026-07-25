<script setup lang="ts">
import Code from "~/components/Code.vue";

const heroLinks = computed(() => [{
  icon: "i-lucide-book-open",
  label: $t("view.index.hero.get-started"),
  trailingIcon: "i-lucide-external-link",
  to: "/docs",
  target: "_blank",
  size: "xl"
}, {
  icon: "i-lucide-star",
  label: $t("view.index.hero.features"),
  to: "#features",
  size: "xl",
  variant: "subtle"
}, {
  icon: "i-lucide-list",
  label: $t("view.index.hero.all-features"),
  to: "/all-features",
  size: "xl",
  variant: "subtle"
}])

const heroCode = `
@Vital.Info(
  name = "myplugin",
  description = "My awesome Vital plugin",
  version = "1.0.0",
  apiVersion = "1.21",
  environment = "Vital.PluginEnvironment.PAPER"
)
public class MyPlugin {
}
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

const plannedFeatures = computed(() => [
  {
    icon: "i-lucide-layers-plus",
    title: $t("view.index.planned-features.features.more-platforms.title"),
    description: $t("view.index.planned-features.features.more-platforms.description")
  },
  {
    icon: "i-lucide-compass",
    title: $t("view.index.planned-features.features.web-initializer.title"),
    description: $t("view.index.planned-features.features.web-initializer.description")
  },
  {
    icon: "i-lucide-users-round",
    title: $t("view.index.planned-features.features.npcs.title"),
    description: $t("view.index.planned-features.features.npcs.description")
  }
])
</script>

<template>
  <div>
    <UPageHero
      :title="$t('view.index.hero.title')"
      :description="$t('layout.view.index.description')"
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

    <USeparator />

    <UPageSection
      id="planned-features"
      :title="$t('view.index.planned-features.title')"
      :description="$t('view.index.planned-features.description')"
      :features="plannedFeatures"
    />
  </div>
</template>
