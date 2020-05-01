package r2

import zio._
import zio.system.System
import zio.test._
import zio.test.Assertion._

import zio.interop.reactivestreams._

object DataSourceSpec extends DefaultRunnableSpec {
  val spec = suite("DataSource")(
    testM("returns fresh connections") {
      for {
        source       <- TestDbUtils.testDataSource
        getPid        = source.connection.use { conn =>
                          conn.createStatement("SELECT pg_backend_pid()").execute.toStream().flatMap { result =>
                            result.map { (row, _) => row.get(0, classOf[java.lang.Integer]) }.toStream()
                          }.runHead
                        }
        (pid1, pid2) <- getPid zipPar getPid
      } yield {
        assert(pid1)(isSome(anything)) &&
        assert(pid2)(isSome(anything)) &&
        assert(pid1)(not(equalTo(pid2)))
      }
    }
  )
}

object TestDbUtils {
  val testDataSource =
    for {
      host     <- requiredEnv("R2_TEST_POSTGRES_HOST")
      port     <- requiredEnv("R2_TEST_POSTGRES_PORT")
      username <- requiredEnv("R2_TEST_POSTGRES_USERNAME")
      password <- requiredEnv("R2_TEST_POSTGRES_PASSWORD")
      database <- requiredEnv("R2_TEST_POSTGRES_DATABASE")
      url       = s"r2dbc:postgresql://${ username }${ if(password == "") "" else s":${ password }" }@${ host }:${ port }/${ database }"
      source   <- DataSource.make(url)
    } yield source

  private def requiredEnv(key: String) = {
    system
      .env(key)
      .orDie
      .flatMap(v => ZIO.fromOption(v))
      .orElseFail(new NoSuchElementException(s"Required key '$key' not found in environment of ${ scala.sys.env }"))
      .provideLayer(System.live)
  }
}
