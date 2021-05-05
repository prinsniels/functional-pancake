package fpfm

import scala.concurrent.duration._
import cats.Monad

object app {
//   trait Monad[T] {
//     def pure[A](value: A): Monad[A]
//     def map[A, B](f: A => B): Monad[B]
//     def flatMap[A, B](f: A => Monad[B]): Monad[B]
//     def foreach(): Unit
//   }

  final case class Epoch(millis: Long) extends AnyVal {
    def +(d: FiniteDuration): Epoch = Epoch(millis + d.toMillis)
    def -(e: Epoch): FiniteDuration = (millis - e.millis).millis
  }

  trait Drone[F[_]] {
    def getBacklog: F[Int]
    def getAgents: F[Int]
  }

  type NonEmptyList[T] = List[T]

  final case class MachineNode(id: String)

  trait Machines[F[_]] {
    def getTime: F[Epoch]
    def getManaged: F[NonEmptyList[MachineNode]]
    def getAlive: F[Map[MachineNode, Epoch]]
    def start(node: MachineNode): F[MachineNode]
    def stop(node: MachineNode): F[MachineNode]
  }

  final case class WorldView(
      backlog: Int,
      agents: Int,
      managed: NonEmptyList[MachineNode],
      alive: Map[MachineNode, Epoch],
      pending: Map[MachineNode, Epoch],
      time: Epoch
  )

  trait DynAgents[F[_]] {
    def initial: F[WorldView]
    def update(old: WorldView): F[WorldView]
    def act(world: WorldView): F[WorldView]
  }

//  final class DynAgentsModule[F[_]: Monad](d: Drone[F], m: Machines[F])
//      extends DynAgents[F] {
//
////    override def initial: F[Int] = for {
////      i <- d.getAgents
////      j <- d.getBacklog
////    } yield (i + j)
//
//    override def update(old: WorldView): F[WorldView] = ???
//
//    override def act(world: WorldView): F[WorldView] = ???
//
//  }
}
