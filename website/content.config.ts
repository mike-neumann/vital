import { defineContentConfig, defineCollection } from '@nuxt/content'
import { z } from 'zod'

export default defineContentConfig({
  collections: {
    docs: defineCollection({
      type: 'page',
      source: '**/*.{md,yml}',
      schema: z.object({
        rawbody: z.string()
      })
    })
  }
})
