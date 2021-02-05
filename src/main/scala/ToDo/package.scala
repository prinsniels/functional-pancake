package object ToDo {
  final implicit class Chains[A](a: A) {
    def pipe[B](ab: A => B): B = ab(a)
  }
}
