import { ValueOrEmptyPipe } from './value-or-empty-pipe';

describe('ValueOrEmptyPipe', () => {
  it('create an instance', () => {
    const pipe = new ValueOrEmptyPipe();
    expect(pipe).toBeTruthy();
  });

  it.each([null, undefined, ''])('returns "--" when the value is %p', (value) => {
    const pipe = new ValueOrEmptyPipe();
    expect(pipe.transform(value)).toBe('--');
  });

  it.each(['label', 0, 42, false, true])('returns the value unchanged for %p', (value) => {
    const pipe = new ValueOrEmptyPipe();
    expect(pipe.transform(value)).toBe(value);
  });
});
