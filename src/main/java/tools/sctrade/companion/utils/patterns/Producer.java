package tools.sctrade.companion.utils.patterns;

import java.util.concurrent.BlockingQueue;
import tools.sctrade.companion.exceptions.ThreadingException;

public abstract class Producer<T> {
  private BlockingQueue<T> queue;

  protected Producer(BlockingQueue<T> queue) {
    this.queue = queue;
  }

  protected void produce(T item) {
    try {
      queue.put(item);
    } catch (InterruptedException e) {
      throw new ThreadingException(e);
    }
  }
}
