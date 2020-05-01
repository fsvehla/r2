package r2

import io.r2dbc.spi.Row
import java.{ lang => jl }
import r2.Read.UnexpectedNull

/**
  * Read is a data type that describes how to retrieve a value of type `A` from a R2DBC Row.
  */
sealed trait Read[+A] {
  def readNullable(row: Row, index: Int): Either[Throwable, Option[A]]

  final def read(row: Row, index: Int): Either[Throwable, A] =
    readNullable(row, index)
      .flatMap {
        case None    => Left(new UnexpectedNull())
        case Some(x) => Right(x)
      }
}

object Read {
  final case class UnexpectedNull() extends RuntimeException("Unexpected NULL")

  private def make[A](fn: (Row, Int) => A): Read[A] =
    new Read[A] {
      def readNullable(row: Row, index: Int): Either[Throwable, Option[A]] =
        try {
          fn(row, index) match {
            case null => Right(None)
            case x    => Right(Some(x))
          }
        } catch {
          case t: Throwable => Left(t)
        }
    }

  val String: Read[jl.String] =
   make((rs, i) => rs.get(i, classOf[jl.String]))
}
