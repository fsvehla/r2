package r2.util

import zio.interop.reactivestreams._
import zio.stream._
import zio.test.Assertion._
import zio.test._

object ReactiveStreamsUtilsSpec extends DefaultRunnableSpec {
  import ReactiveStreamsUtils._

  val spec = suite("ReactiveStreamsUtils")(
    suite("mono")(
      testM("returns a single element") {
        for {
          publisher <- Stream.succeed(42).toPublisher
          head      <- mono(publisher)
        } yield {
          assert(head)(equalTo(42))
        }
      },

      testM("fails if the stream completes without emitting an element") {
        for {
          publisher <- Stream[Int]().toPublisher
          error     <- mono(publisher).flip
        } yield {
          assert(error)(isSubtype[NoSuchElementException](hasMessage(containsString("Stream completed"))))
        }
      }
    ),

    suite("awaitCompletion")(
      testM("returns after the stream completes") {
        for {
          publisher <- Stream[Void]().toPublisher
          _         <- awaitCompletion(publisher)
        } yield {
          assertCompletes
        }
      }
    )
  )
}
