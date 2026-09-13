import { passwordError } from './password-policy';

describe('Registration password policy', () => {
  it('accepts long phrases with spaces without forced punctuation', () => {
    expect(passwordError('mi primera casa tiene muchas ventanas')).toBeNull();
  });
  it('counts characters and UTF-8 bytes separately', () => {
    expect(passwordError('🔑'.repeat(8))).not.toBeNull();
    expect(passwordError('abc' + '🔑'.repeat(17))).toBeNull();
    expect(passwordError('abc' + '🔑'.repeat(18))).not.toBeNull();
  });
  it('rejects short and common passwords', () => {
    expect(passwordError('seis12')).not.toBeNull();
    expect(passwordError('123456789012345')).not.toBeNull();
  });
});
