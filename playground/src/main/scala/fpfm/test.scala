package fpfm

object test extends App {

  trait MyOption {
    def +(other: MyOption): MyOption
  }

  object MyOption {
    case class MySome(value: Int) extends MyOption {

      override def +(other: MyOption): MyOption = other match {
        case MyNone    => MyNone
        case MySome(v) => MySome(value + v)
      }
    }

    case object MyNone extends MyOption {
      override def +(other: MyOption): MyOption = this
    }

    def apply(v: Int): MyOption = MySome(v)
  }

  // this does not allow for generalization over type (str etc, lets code this in)
  println(MyOption(10) + MyOption(4))

}

object test2 extends App {

  /** make it possible to add use the options with other
    * data types, like string, float etc
    */
  trait MyOption[+T]

  object MyOption {
    case class MySome[T](v: T) extends MyOption[T]
    case object MyNone extends MyOption[Nothing]

    def apply[T](v: T) = new MySome(v)
  }

  import MyOption._

  println(MySome(1))
  println(MySome("1"))

  // now lets introduce where we want to use is
  def toInt(v: String): MyOption[Int] = {
    try {
      MyOption(v.toInt)
    } catch {
      case e: Exception => MyNone
    }
  }

  println(toInt("4"))
}

object test3 extends App {

  /** make it possible to modify the part in the container
    */
  trait MyOption[+T] {
    def map[B](f: T => B): MyOption[B]
  }

  object MyOption {
    case class MySome[T](v: T) extends MyOption[T] {
      override def map[B](f: T => B): MyOption[B] = MyOption(f(v))
    }
    case object MyNone extends MyOption[Nothing] {
      override def map[B](f: Nothing => B): MyOption[B] = this
    }
    def apply[T](v: T) = new MySome(v)
  }

  import MyOption._

  println(MySome(1))
  println(MySome("1"))

  def toInt(v: String): MyOption[Int] = {
    try {
      MyOption(v.toInt)
    } catch {
      case e: Exception => MyNone
    }
  }

  println(toInt("4").map(_ * 10 + 2))
  println(toInt("four").map(_ * 10 + 2))
}

object test4 extends App {

  /** make it possible to
    * chain computations on the effect
    */
  trait MyOption[+T] {
    def map[B](f: T => B): MyOption[B]
    def flatMap[B](f: T => MyOption[B]): MyOption[B]
  }

  object MyOption {
    case class MySome[T](v: T) extends MyOption[T] {
      override def map[B](f: T => B): MyOption[B] = MyOption(f(v))
      override def flatMap[B](f: T => MyOption[B]): MyOption[B] = f(v)
    }
    case object MyNone extends MyOption[Nothing] {
      override def map[B](f: Nothing => B): MyOption[B] = this
      override def flatMap[B](f: Nothing => MyOption[B]): MyOption[B] = this
    }
    def apply[T](v: T) = new MySome(v)
  }

  import MyOption._

  def toInt(v: String): MyOption[Int] = {
    try {
      MyOption(v.toInt)
    } catch {
      case e: Exception => MyNone
    }
  }

  println(toInt("4").flatMap(i => toInt("6").map(j => i + j)))

  val res: MyOption[Int] = for {
    i <- toInt("4")
    j <- toInt("6")
  } yield (i + j)
  println(res)
}
