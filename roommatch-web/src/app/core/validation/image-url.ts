import { environment } from '../../../environments/environment';

// Defense at the DOM boundary. The server remains responsible for saved URL policy.
export function imageUrl(value: unknown): string | null {
  if (typeof value !== 'string' || !value.trim()) return null;
  const trimmed = value.trim();
  if (trimmed.length > 255 || /[\s\u0000-\u001f\u007f]/.test(trimmed)) return null;
  try {
    const url = new URL(trimmed);
    if (!(url.protocol === 'https:' || !environment.production && url.protocol === 'http:') ||
        !url.hostname || url.username || url.password || url.hash || url.port === '0' || url.href.length > 255) return null;
    return url.href;
  } catch { return null; }
}
