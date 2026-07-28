import path from 'node:path'
import { promises as fs } from 'node:fs'

export default defineSitemapEventHandler(async (event) => {
  const urls: { loc: string }[] = []
  const docs = await queryCollection(event, 'docs').all()

  urls.push(...docs.map(doc => ({
    loc: doc.path
  })))

  // Dokka pages
  const dokkaRoot = path.resolve('./public/dokka')

  async function scan(dir: string) {
    const entries = await fs.readdir(dir, { withFileTypes: true })

    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name)

      if (entry.isDirectory()) {
        await scan(fullPath)
      }

      if (entry.name.endsWith('.html')) {
        urls.push({
          loc: fullPath
            .replace(path.resolve('./public'), '')
            .replace(/\\/g, '/')
        })
      }
    }
  }

  await scan(dokkaRoot)

  return urls
})
