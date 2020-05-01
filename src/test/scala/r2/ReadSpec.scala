package r2

import zio._
import zio.system.System
import zio.test._
import zio.test.Assertion._
import zio.interop.reactivestreams._

object ReadSpec extends DefaultRunnableSpec {
  val spec = suite("Read[+A]")(
    testM("reads from a given position") {
      for {
        source <- TestDbUtils.testDataSource
        answer <- source.connection.use { conn =>
                    conn
                      .createStatement("""SELECT 'Hi'""")
                      .execute
                      .toStream()
                      .flatMap { result =>
                        result.map { (row, _) =>
                          Read.String.read(row, 0)
                        }.toStream()
                      }.runHead
                  }
      } yield {
        assert(answer)(isSome(equalTo(Right("Hi"))))
      }
    }
  )
}
