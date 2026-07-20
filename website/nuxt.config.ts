// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "@nuxtjs/i18n",
    "@nuxtjs/seo",
    "@nuxt/content"
  ],

  app: {
    pageTransition: {
      name: "page",
      mode: "out-in"
    }
  },

  content: {
    build: {
      markdown: {
        highlight: {
          langs: ["java", "kotlin", "groovy", "yaml", "properties"],
          theme: {
            default: "github-light",
            dark: "github-dark"
          }
        }
      }
    }
  },

  i18n: {
    defaultLocale: "en",
    strategy: "no_prefix",
    locales: [
      { code: "en", language: "en-US", file: "en.json" },
      { code: "de", language: "de-DE", file: "de.json" }
    ]
  },

  devtools: {
    enabled: true
  },

  css: ["~/assets/css/main.css"],

  routeRules: {
    "/": { prerender: true }
  },

  compatibilityDate: "2026-06-30",

  eslint: {
    config: {
      stylistic: {
        commaDangle: "never",
        braceStyle: "1tbs"
      }
    }
  }
})
