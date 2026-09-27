/** Old or malformed stored values stay visible as text, never become arbitrary links. */
export function socialProfileUrl(value: string, network: 'instagram' | 'facebook'): string | null {
  const text = value.trim();
  const handle = text.replace(/^@/, '');
  if (!text.includes(':') && !text.includes('/') && /^[A-Za-z0-9_.]{1,50}$/.test(handle)) {
    if (network === 'instagram' && handle.length > 30) return null;
    return `https://www.${network}.com/${encodeURIComponent(handle)}`;
  }
  const domain = new RegExp(`^(www\\.|m\\.)?${network}\\.com/`);
  try {
    const url = new URL(domain.test(text) ? 'https://' + text : text);
    const hosts = network === 'instagram' ? ['instagram.com', 'www.instagram.com'] : ['facebook.com', 'www.facebook.com', 'm.facebook.com'];
    if (url.protocol !== 'https:' || !hosts.includes(url.hostname) || url.username || url.password || url.port || url.hash) return null;
    const named = /^\/[A-Za-z0-9_.]+\/?$/.test(url.pathname) && !url.search
      && !['/l.php', '/login', '/login.php', '/sharer.php', '/share.php'].includes(url.pathname.replace(/\/$/, ''));
    const id = network === 'facebook' && url.pathname === '/profile.php' && /^\?id=[0-9]{1,30}$/.test(url.search);
    return named || id ? url.href : null;
  } catch { return null; }
}
