import fs from 'node:fs'
import path from 'node:path'

function getDocRoutes(dir = 'content/docs', base = '/docs'): string[] {
  if (!fs.existsSync(dir)) {
    return []
  }

  const entries = fs.readdirSync(dir, { withFileTypes: true })
  const routes: string[] = []
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name)
    const cleanName = entry.name.replace(/^\d+\./, '')
    if (entry.isDirectory()) {
      routes.push(...getDocRoutes(fullPath, `${base}/${cleanName}`))
    } else if (entry.isFile() && entry.name.endsWith('.md')) {
      if (cleanName === 'index.md') {
        routes.push(base)
      } else {
        const pageName = cleanName.replace(/\.md$/, '')
        routes.push(`${base}/${pageName}`)
      }
    }
  }
  return routes
}

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

  runtimeConfig: {
    public: {
      githubUrl: 'https://github.com/mike-neumann/vital',
      githubCreateWebdocIssueUrl: 'https://github.com/mike-neumann/vital/issues/new?template=webdoc-bug-report.yaml',
      githubCreateFrameworkIssueUrl: 'https://github.com/mike-neumann/vital/issues/new?template=framework-bug-report.yaml',
      estimatedWordsPerMinute: 150
    }
  },

  routeRules: {
    '/': { prerender: true },
    '/docs/**': { prerender: true }
  },

  compatibilityDate: '2026-06-30',

  nitro: {
    prerender: {
      crawlLinks: true,
      routes: ['/', ...getDocRoutes()],
      ignore: ['/dokka', '/dokka/**']
    }
  },

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
