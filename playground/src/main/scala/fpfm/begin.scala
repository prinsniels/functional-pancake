package fpfm

import scala.concurrent.Future

import scala.concurrent.ExecutionContext

object begin {
  trait Terminal[F[_]] {
    def read: F[String]
    def put(t: String): F[Unit]
  }

  type Now[T] = T

  object TerminalSync extends Terminal[Now] {
    override def read: Now[String] = scala.io.StdIn.readLine()
    override def put(t: String): Now[Unit] = println(t)
  }

  object TerminalAsync extends Terminal[Future] {

    import ExecutionContext.Implicits.global
    
    override def read: Future[String] = Future {
        scala.io.StdIn.readLine()
    }

    override def put(t: String): Future[Unit] = Future {
        println(t)
    }

  }

  def main(args: Array[String]): Unit = {
      TerminalSync.put("what is your name?")
      val name = TerminalSync.read
      TerminalSync.put(s"Hello ${name}")

    //   TerminalAsync.put("what is your name?")
    //   val name = TerminalAsync.read
    //   TerminalAsync.put(s"Hello ${name}")
  }
}
