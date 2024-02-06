/**
 * Interface to implement the Dispose pattern.
 * See https://en.wikipedia.org/wiki/Dispose_pattern.
 */
interface Disposer {
  /**
   * Call this to dispose of the resource.
   */
  public void dispose();
}