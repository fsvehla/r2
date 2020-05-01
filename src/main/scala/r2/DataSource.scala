package r2

import zio.{IO, Managed}

import io.r2dbc.spi.{Connection, ConnectionFactories, ConnectionFactory}
import r2.util.ReactiveStreamsUtils._

final class DataSource private (factory: ConnectionFactory) {
  val connection: Managed[Throwable, Connection] =
    Managed.make(mono(factory.create))(c => awaitCompletion(c.close).orDie)

  /**
    * A connection that will be rolled back.
    * In lieu of first-class transactions useful in tests.
    */
  val rollBackConnection: Managed[Throwable, Connection] =
    connection
      .flatMap { connection =>
        Managed.make(awaitCompletion(connection.beginTransaction)) { _ =>
          awaitCompletion(connection.rollbackTransaction).orDie
        } as connection
      }
}

object DataSource {
  def make(url: String): IO[IllegalStateException, DataSource] =
    IO(ConnectionFactories.get(url))
      .refineToOrDie[IllegalStateException]
      .map(f => new DataSource(f))
}
