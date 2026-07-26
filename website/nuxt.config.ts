// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    '@nuxt/eslint',
    '@nuxt/ui',
    '@nuxtjs/i18n',
    '@nuxtjs/seo',
    '@nuxt/content',
    '@nuxtjs/robots',
    '@nuxtjs/sitemap'
  ],

  devtools: {
    enabled: true
  },

  app: {
    head: {
      meta: [
        {
          name: 'theme-color',
          content: '#00dc82'
        },
        {
          name: 'theme-color',
          content: '#18181b',
          media: '(prefers-color-scheme: dark)'
        }
      ]
    },
    pageTransition: {
      name: 'page',
      mode: 'out-in'
    },
    layoutTransition: {
      name: 'page',
      mode: 'out-in'
    }
  },

  css: ['~/assets/css/main.css'],

  site: {
    url: 'https://vitalframework.dev'
  },

  content: {
    build: {
      markdown: {
        highlight: {
          langs: ['java', 'kotlin', 'groovy', 'yaml', 'properties'],
          theme: {
            default: 'github-light',
            dark: 'github-dark'
          }
        }
      }
    }
  },

  routeRules: {
    '/': { prerender: true }
  },

  compatibilityDate: '2026-06-30',

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  },

  i18n: {
    defaultLocale: 'en',
    strategy: 'no_prefix',
    locales: [
      { code: 'en', language: 'en-US', file: 'en.json' },
      { code: 'de', language: 'de-DE', file: 'de.json' }
    ]
  },

  sitemap: {
    sources: [
      '/api/__sitemap__/urls'
    ]
  }
})
