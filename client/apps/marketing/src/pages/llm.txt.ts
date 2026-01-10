import type { APIRoute } from 'astro';

const SITE_URL = new URL(import.meta.env.SITE);

function generatePageList(pages: [string, any][], lang: 'en' | 'es', siteUrl: URL): string[] {
  return pages
    .filter(([path]) => path.startsWith(`./${lang}/`))
    .map(([path, page]) => ({
      frontmatter: page.frontmatter,
      slug: path.split('/').pop()?.replace('.mdx', '') ?? ''
    }))
    .filter(({ frontmatter }) => frontmatter.title && frontmatter.description)
    .map(({ frontmatter, slug }) => {
      const pageUrl = new URL(`${lang}/${slug}`, siteUrl).toString();
      return `- [${frontmatter.title}](${pageUrl}) – ${frontmatter.description}`;
    });
}

export const GET: APIRoute = async () => {
  const pages = Object.entries(await import.meta.glob('./(en|es)/*.mdx', { eager: true }));

  const lines = [
    '# ProFileTailors',
    '> ProFileTailors is a resume generator that helps you create a professional resume in minutes.',
    ''
  ];

  const englishPages = generatePageList(pages, 'en', SITE_URL);
  if (englishPages.length > 0) {
    lines.push('## English Pages', ...englishPages, '');
  }

  const spanishPages = generatePageList(pages, 'es', SITE_URL);
  if (spanishPages.length > 0) {
    lines.push('## Spanish Pages', ...spanishPages, '');
  }

  const body = lines.join('\n');

  return new Response(body, {
    headers: { 'Content-Type': 'text/plain; charset=utf-8' }
  });
};
