package r2.util

import zio.test.Assertion._
import zio.test._

import org.reactivestreams.{Publisher, Subscriber, Subscription}

object ReactiveStreamsUtilsSpec extends DefaultRunnableSpec {
  import ReactiveStreamsUtils._

  val spec = suite("ReactiveStreamsUtils")(
    suite("mono")(
      testM("returns a single element") {
        for {
          head <- mono(EmitSingle)
        } yield {
          assert(head)(equalTo(42))
        }
      },

      testM("fails if the stream completes without emitting an element") {
        for {
          error <- mono(EmitComplete).flip
        } yield {
          assert(error)(isSubtype[NoSuchElementException](hasMessage(containsString("Stream completed"))))
        }
      }
    ),

    suite("awaitCompletion")(
      testM("returns after the stream completes") {
        for {
          _ <- awaitCompletion(EmitComplete)
        } yield {
          assertCompletes
        }
      }
    )
  )

  object EmitSingle extends Publisher[Int] {
    def subscribe(subscriber: Subscriber[_ >: Int]) = {
      subscriber.onSubscribe(new Subscription {
        def request(n: Long): Unit = {
          subscriber.onNext(42)
          subscriber.onComplete()
        }

        def cancel(): Unit = ()
      })
    }
  }

  object EmitComplete extends Publisher[Void] {
    def subscribe(subscriber: Subscriber[_ >: Void]) = {
      subscriber.onSubscribe(new Subscription {
        def request(n: Long): Unit = subscriber.onComplete()
        def cancel(): Unit = ()
      })
    }
  }
}
