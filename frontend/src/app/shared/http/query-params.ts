/** Reads a positive integer query param, falling back to `defaultValue` when absent or invalid. */
export function intQueryParam(value: string | null, defaultValue: number): number {
  const parsed = Number(value);
  return value !== null && Number.isInteger(parsed) && parsed >= 0 ? parsed : defaultValue;
}
