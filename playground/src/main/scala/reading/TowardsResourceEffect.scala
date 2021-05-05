import scala.io.Source
import java.io.File

object TowardsResourceEffect {

  /** This could result in not closing the resource, which will result
    * in problems
    * The resource should be closed, in the most basic solution we use this open in combination
    * with a Try clock :
    *   try {
    *     doWork
    *   } finally {
    *     close()
    *   }
    *
    * some drawbacks
    *     1. It’s not composable.
    *     2. It’s manual. You have to always look at the code, and check if all the resources you acquire are closed.
    *     3. It’s easy to forget about proper ordering when closing.
    *     4. If we’re closing more resources in the finally block, and something throws an exception there, the rest of the resources would remain open.
    */
  def openFileNaive(path: String): Source = {
    Source.fromFile(new File(path))
  }

  /** Now the resource is loaned to the handler, closing is done for the user, but composing needs to be done with
    * callbacks, resulting in a callback hell
    *
    * withFile(openFileNaive(one)){ one  =>
    *   withFile(openFileNaive(two)){ two =>
    *     doWork
    * }}
    */
  def withFile[T](resource: Source)(handler: Source => T): T = {
    try {
      handler(resource)
    } finally {
      resource.close()
    }
  }

  /** further generalization
    *
    * this does not allow composition, we need to further abstract for that
    */
  def withResource[R, T](resource: => R)(handler: R => T)(close: R => Unit): T =
    try handler(resource)
    finally close(resource)

  /** Code snippet only allows the usage inside a Use block like
    *
    * val server = Resource(new HttpServer {})(_.close())server.use { httpServer =>
    *  // ...
    * }
    *
    * It still does not allow sequencing, but this we can solve
    */

  trait Resource[R] { outer =>
    def use[U](f: R => U): U

    def flatMap[B](fa: R => Resource[B]): Resource[B] =
      /* flatmap creates a new Resource, whose resource calls the
      resources its called on in its use. We want to withhold the usage
      of the resources until the end of the application

      Baseline => We need to open A before we open B. That is what sequential
        processing means =D

       */
      new Resource[B] {
        override def use[U](f: B => U): U =
          outer.use(res1 => fa(res1).use(res2 => f(res2)))
      }

    def map[B](mapping: R => B): Resource[B] =
      /* The Functor: Apply the transformation on the resources its encapsulated value */
      new Resource[B] {
        override def use[U](f: B => U): U = outer.use(a => f(mapping(a)))
      }

  }

  object Resource {
    def apply[R](acquire: => R)(close: R => Unit) = new Resource[R] {
      override def use[U](f: R => U): U = {
        val resource = acquire
        try f(resource)
        finally close(resource)
      }
    }

    def pure[R](r: => R): Resource[R] = Resource(r)(_ => ())

  }

  def main(args: Array[String]): Unit = {
    println("--- own resource provider ---")
    val one =
      Resource(openFileNaive("fpm/src/main/resources/one.txt"))(_.close())

    val two =
      Resource(openFileNaive("fpm/src/main/resources/two.txt"))(_.close())

    one.map(x => x.getLines()).use(_.foreach(println))

    /** Cool thing is that we can start combining them
      */
    val resources: Resource[(Source, Source)]  = for {
      o <- one
      t <- two
    } yield (o, t)

    resources.use { case (a: Source, b: Source) =>
      a.getLines().zip(b.getLines()).foreach(println)
    }
  }

}
