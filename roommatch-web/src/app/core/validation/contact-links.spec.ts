import { socialProfileUrl } from './contact-links';

describe('social contact links', () => {
  it('supports handles and profile URLs on the intended network only', () => {
    expect(socialProfileUrl('@persona.peru', 'instagram')).toBe('https://www.instagram.com/persona.peru');
    expect(socialProfileUrl('www.facebook.com/persona', 'facebook')).toBe('https://www.facebook.com/persona');
    expect(socialProfileUrl('https://www.facebook.com/profile.php?id=123', 'facebook')).toContain('id=123');
  });
  it.each(['javascript:alert(1)', 'data:text/html,test', 'https://instagram.com.evil.test/person',
    'https://evil.test@instagram.com/person', 'https://instagram.com/person?redirect=https://evil.test',
    'http://instagram.com/person', 'https://facebook.com/l.php?u=https://evil.test'])('does not turn %s into an actionable profile link', value => {
    expect(socialProfileUrl(value, 'instagram')).toBeNull();
  });
});
