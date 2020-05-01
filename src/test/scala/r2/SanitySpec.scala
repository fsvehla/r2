package r2

import zio._
import zio.system.System
import zio.test._
import zio.test.Assertion._
import zio.interop.reactivestreams._

import io.r2dbc.spi.ConnectionFactories
import org.reactivestreams.Publisher

object SanitySpec extends DefaultRunnableSpec {
  def awaitCompletion(publisher: Publisher[Void]): IO[Throwable, Unit] =
    publisher.toStream().runDrain

  def mono[A](publisher: Publisher[A]): IO[Throwable, A] =
    publisher.toStream().runHead.flatMap {
      case Some(v) => IO.succeed(v)
      case None    => IO.fail(new NoSuchElementException(s"Publisher completed without yielding an element"))
    }

  val spec = suite("Sanity tests")(
    testM("can connect to Postgres") {
      case class ConnectionInfo(host: String, port: String, username: String, password: String, database: String)

      def requiredEnv(key: String) = {
        system
          .env(key)
          .orDie
          .flatMap(v => ZIO.fromOption(v))
          .orElseFail(new NoSuchElementException(s"Required key '$key' not found in environment of ${ scala.sys.env }"))
          .provideLayer(System.live)
      }

      val connectionInfo = for {
        host     <- requiredEnv("R2_TEST_POSTGRES_HOST")
        port     <- requiredEnv("R2_TEST_POSTGRES_PORT")
        username <- requiredEnv("R2_TEST_POSTGRES_USERNAME")
        password <- requiredEnv("R2_TEST_POSTGRES_PASSWORD")
        database <- requiredEnv("R2_TEST_POSTGRES_DATABASE")
      } yield ConnectionInfo(host, port, username, password, database)

      def buildUrl(info: ConnectionInfo) = {
        s"r2dbc:postgresql://${ info.username }${ if(info.password == "") "" else s":${ info.password }" }@${info.host}:${info.port}/${info.database}"
      }

      for {
        info    <- connectionInfo
        url      = buildUrl(info)
        _       <- console.putStrLn(s"Connection info: $info")
        _       <- console.putStrLn(s"URL: $url")
        factory <- Task(ConnectionFactories.get(url))
        xs      <- Managed.make(mono(factory.create))(conn => awaitCompletion(conn.close).orDie).use { conn =>
                     conn.createStatement("SELECT VERSION()").execute.toStream().flatMap { result =>
                       result.map { (row, _) => row.get(0, classOf[String]) }.toStream()
                     }.runCollect
                   }
        _       <- console.putStrLn(s"Postgres version: ${ xs }")
      } yield {
        assertCompletes
      }
    }
  )
}
