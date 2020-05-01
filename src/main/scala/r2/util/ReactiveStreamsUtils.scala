package r2.util

import zio.IO

import org.reactivestreams.Publisher
import zio.interop.reactivestreams._

object ReactiveStreamsUtils {
  def mono[A](publisher: Publisher[A]): IO[Throwable, A] =
    publisher
      .toStream()
      .runHead
      .flatMap {
        case Some(v) => IO.succeed(v)
        case None    => IO.fail(new NoSuchElementException("Stream completed without producting an element"))
      }

  def awaitCompletion(publisher: Publisher[Void]): IO[Throwable, Unit] =
    publisher
      .toStream()
      .runDrain
}
