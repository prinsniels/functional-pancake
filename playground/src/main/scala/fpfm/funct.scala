package fpfm

import javax.crypto.Mac

object func extends App {

  trait RNG {
    def nextInt: (Int, RNG)
  }

  case class SimpleRNG(seed: Long) extends RNG {
    override def nextInt: (Int, RNG) = {
      val newSeed = (seed * 0x5deece66dL + 0xbL) & 0xffffffffffffL
      val nextRNG = SimpleRNG(newSeed)
      val n = (newSeed >>> 16).toInt
      (n, nextRNG)
    }
  }

  def nonNegativeInt(rng: RNG): (Int, RNG) = {
    val (i, nwRng) = rng.nextInt
    (if (i < 0) -(i + 1) else i, nwRng)
  }

  def double(rng: RNG): (Double, RNG) = {
    val (i, nwRng) = nonNegativeInt(rng)
    (i.toDouble / Int.MaxValue, nwRng)
  }

  def intDouble(rng: RNG): ((Int, Double), RNG) = {
    val (i, nwrnga) = rng.nextInt
    val (d, nwrngb) = double(nwrnga)
    ((i, d), nwrngb)
  }

  def doubleInt(rng: RNG): ((Double, Int), RNG) = {
    val ((i, d), nwrng) = intDouble(rng)
    ((d, i), nwrng)
  }

  def double3(rng: RNG): ((Double, Double, Double), RNG) = {
    val (a, ra) = double(rng)
    val (b, rb) = double(ra)
    val (c, rc) = double(rb)
    ((a, b, c), rc)
  }

  def ints(count: Int)(rng: RNG): (List[Int], RNG) =
    (0 to count).foldLeft((List.empty[Int], rng)) {
      case ((acc, r), _) => {
        val (a, ra) = r.nextInt
        (a :: acc, ra)
      }
    }

  println(intDouble(SimpleRNG(1)))
  println(doubleInt(SimpleRNG(1)))
  println(double3(SimpleRNG(1)))
  println(ints(4)(SimpleRNG(1)))

  type Rand[+A] = RNG => (A, RNG)

  def unit[A](a: A): Rand[A] = rng => (a, rng)

  def map[A, B](s: Rand[A])(f: A => B): Rand[B] =
    rng => {
      val (a, rng2) = s(rng)
      (f(a), rng2)
    }

  def nwDouble: Rand[Double] = map(nonNegativeInt)(_.toDouble / Int.MaxValue)

  println(nwDouble(SimpleRNG(1)))

  def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = rng =>
    {
      val (va, ran) = ra(rng)
      val (vb, rbn) = rb(ran)
      (f(va, vb), rbn)
    }

  println(map2(nonNegativeInt, double)((_, _))(SimpleRNG(1)))

  def sequence[A](fs: List[Rand[A]]): Rand[List[A]] =
    fs.foldRight(unit(List.empty[A]))((f, acc) => map2(f, acc)(_ :: _))

  def _ints(count: Int): Rand[List[Int]] = sequence(
    List.fill(count)((x: RNG) => x.nextInt)
  )

  println(_ints(4)(SimpleRNG(1)))

  def flatMap[A, B](fa: Rand[A])(f: A => Rand[B]): Rand[B] = { rng =>
    {
      val (i, r) = fa(rng)
      f(i)(r)
    }
  }

  def nonNegativeLessThan(n: Int): Rand[Int] = {
    flatMap(nonNegativeInt) { i =>
      val mod = i % n
      if (i + (n - 1) - mod >= 0) unit(mod) else nonNegativeLessThan(n)
    }
  }

  println(nonNegativeLessThan(10)(SimpleRNG(1)))

  def _map[A, B](s: Rand[A])(f: A => B): Rand[B] =
    flatMap(s)(x => unit(f(x)))
}

object candy extends App {
  case class State[S, +A](r: S => (S, A)) {
    def map[B](f: A => B): State[S, B] =
      flatMap(a => State.unit(f(a)))

    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      flatMap(a => sb.map(b => f(a, b)))

    def flatMap[B](f: A => State[S, B]): State[S, B] = State(s => {
      val (s1, a) = r(s)
      f(a).r(s1)
    })
  }

  object State {
    def unit[S, A](a: A): State[S, A] = State(s => (s, a))

    // this means chain
    def sequence[S, A](fs: List[State[S, A]]): State[S, List[A]] =
      fs.foldRight(unit[S, List[A]](List()))((f, acc) => f.map2(acc)(_ :: _))
  }

  sealed trait Input
  case object Coin extends Input
  case object Turn extends Input

  case class Machine(locked: Boolean, candies: Int, coins: Int)

  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] = ???

  type C = (Int, Int)

  def step(i: Input)(m: Machine): Machine =
    i match {
      case Coin if (m.locked && m.candies > 0) =>
        m.copy(locked = false, coins = m.coins + 1)
      case Turn if !m.locked => m.copy(locked = true, candies = m.candies - 1)
      case _                 => m
    }
}
