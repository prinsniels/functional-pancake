import cats.effect.IO

object TowardsResourceEffectWithCats {

  object BasicIO {
    class SomeAwsSdkJavaClient {
      println("Opening connections")
      def use: Unit = println("Using")
      def close: Unit = println("Closing connections")
    } // we should probably use Blocker here, but let's forget about that detail for now

    def businessLogic(client: SomeAwsSdkJavaClient): IO[Unit] =
      for {
        _ <- IO(client.use)
      } yield ()

    def businessLogicWithError(client: SomeAwsSdkJavaClient): IO[Unit] =
      for {
        _ <- IO.raiseError(new RuntimeException("OMG!"))
        _ <- IO(client.use)
      } yield ()

    def program: IO[Unit] =
      for {
        client <- IO(new SomeAwsSdkJavaClient)
        _ <- businessLogic(client)
        _ <- IO(client.close)
      } yield ()

    def programWithError: IO[Unit] =
      for {
        client <- IO(new SomeAwsSdkJavaClient)
        _ <- businessLogicWithError(client)
        _ <- IO(client.close)
      } yield ()

    def programWithErrorAndAttempt: IO[Unit] =
      for {
        client <- IO(new SomeAwsSdkJavaClient)
        e <- businessLogicWithError(
          client
        ).attempt // IO[Either[Throwable, Unit]]
        _ <- IO(client.close)
        _ <- IO.fromEither(e)
      } yield ()
  }

  def main(args: Array[String]): Unit = {
    BasicIO.program.unsafeRunSync();

    /** note:
      *    - that this is not closing the connection
      *    - the program stops when the error is thrown
      */
    
    //  BasicIO.programWithError.unsafeRunSync();
    

    // this works but does not counter the cancel functionality 
    // used in the IO, the code could timeout without closing 
    // the open connection
    // -  BasicIO.programWithErrorAndAttempt.unsafeRunSync();
  }

}
