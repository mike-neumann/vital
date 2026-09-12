<script setup lang="ts">
import type { PageCardProps } from '#ui/components/PageCard.vue'

const { t } = useI18n()

const features = computed<PageCardProps[]>(() => [{
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-commands.title'),
  description: t('view.all-features.cta.card.vital-commands.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-configs.title'),
  description: t('view.all-features.cta.card.vital-configs.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-holograms.title'),
  description: t('view.all-features.cta.card.vital-holograms.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-inventories.title'),
  description: t('view.all-features.cta.card.vital-inventories.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-items.title'),
  description: t('view.all-features.cta.card.vital-items.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-localization.title'),
  description: t('view.all-features.cta.card.vital-localization.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-minigames.title'),
  description: t('view.all-features.cta.card.vital-minigames.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-players.title'),
  description: t('view.all-features.cta.card.vital-players.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-scoreboards.title'),
  description: t('view.all-features.cta.card.vital-scoreboards.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-statistics.title'),
  description: t('view.all-features.cta.card.vital-statistics.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-tasks.title'),
  description: t('view.all-features.cta.card.vital-tasks.description')
}, {
  icon: 'i-lucide-circle',
  title: t('view.all-features.cta.card.vital-utils.title'),
  description: t('view.all-features.cta.card.vital-utils.description')
}])
</script>

<template>
  <div>
    <UPageSection>
      <UPageCTA
        :title="t('view.all-features.cta.title')"
        :description="t('view.all-features.cta.description')"
        variant="subtle"
      >
        <UPageGrid>
          <UPageCard
            v-for="(feature, i) of features"
            :key="i"
            :title="feature.title"
            :description="feature.description"
            class="col-span-full"
          />
        </UPageGrid>
      </UPageCTA>
    </UPageSection>
  </div>
</template>

<style scoped>

</style>
