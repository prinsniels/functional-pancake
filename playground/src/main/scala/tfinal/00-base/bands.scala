
import PFunc._
import scala.annotation.tailrec

object PFunc {
  implicit class Chain[A](v: A) {
    def pipe[B](f: A => B): B = f(v)
  }
}

object DataTypes {
  case class Band(id: String, name: String) {
    def withUpdatedName(newName: String): Band =
      copy(name = newName)
  }

}

object Algebras {
  import DataTypes._

  trait BandAlgebra {
    def createOne(name: String): Band

    def readOneById(id: String): Option[Band]

    def updateOne(band: Band): Band

    def deleteOne(id: String): Unit
  }

  /** Only the interface belongs to the algebra's
    * The interpreter of the Repo should be a different layer, to  allow for clean architecture
    */
  trait BandRepoAlgebra {
    def addOne(name: String): Band

    def readOneById(id: String): Option[Band]

    def updateOne(band: Band): Band

    def deleteOne(id: String): Unit
  }
}

object Interpreters {

  import DataTypes._
  import Algebras._

  class BandService(
      bandRepo: BandRepoAlgebra // this is required to make this implementation work
  ) extends BandAlgebra {

    override def createOne(name: String): Band =
      bandRepo.addOne(name)

    override def readOneById(id: String): Option[Band] =
      bandRepo.readOneById(id)

    override def updateOne(band: Band): Band =
      bandRepo.updateOne(band)

    override def deleteOne(id: String): Unit =
      bandRepo.deleteOne(id)

  }
}

object Interfaces {
  import Algebras._
  import DataTypes._

  trait mConsole {
    def putString(value: String): Unit

    def getWithPrompt(prompt: String): String

  }

  class tConsole extends mConsole {

    def putString(value: String): Unit = println(value)

    def getWithPrompt(prompt: String): String = scala.io.StdIn.readLine(prompt)

  }

  object inMemBandRepo extends BandRepoAlgebra {
    var state: Vector[Band] = Vector()
    var nextId: Int = 0

    override def addOne(name: String): DataTypes.Band = {
      val created = Band(nextId.toString(), name)
      state :+= created
      nextId += 1
      created
    }

    override def readOneById(id: String): Option[DataTypes.Band] =
      state.find(_.id == id)

    override def updateOne(band: DataTypes.Band): DataTypes.Band = {
      state = state.filterNot(_.id == band.id) :+ band
      band
    }

    override def deleteOne(id: String): Unit = state =
      state.filterNot(_.id == id)

  }
}

object Programs {
  import DataTypes._
  import Interpreters._
  import Interfaces._

  class CrudApp(
      bandService: BandService,
      cons: mConsole
  ) {

    val menu = """
    | !!! Band registration !!!
    | c         => register a new one
    | d         => delete a band 
    | un        => update a band name
    | r         => show a band
    |
    | q         => please stop!!!
    |
    | ----------------------------
    |"""

    def run(): Unit = {
      @tailrec
      def loop: Unit =
        cons.getWithPrompt(menu) match {
          case "c" => createOne; loop
          case "d" => deleteOne; loop
          case "un" => updateBandName; loop
          case "r" => getOneById; loop
          case "q" => ()
          case _   => loop
        }
      loop
    }

    private def createOne: Unit = {
      // logic getting a name an creating a band
      cons
        .getWithPrompt("Enter a name:")
        .pipe(name => bandService.createOne(name))
    }

    private def getOneById: Unit = {
      /// logic for getting one
      cons
        .getWithPrompt("Enter a id:")
        .pipe(id => bandService.readOneById(id))
        .pipe(x =>
          x match {
            case None       => cons.putString("band not found!!")
            case Some(band) => cons.putString(band.toString())
          }
        )
    }

    private def updateBandName: Unit = {
      val id = cons.getWithPrompt("Please enter an Id:")
      val band: Option[Band] = bandService.readOneById(id)
      val newName: String = cons.getWithPrompt("please give a new name")
      val newBand: Option[Band] = band.map(_.withUpdatedName(newName))
      bandService.updateOne(newBand.get)
    }

    private def deleteOne: Unit = {
      cons.getWithPrompt("Enter a id").pipe(id => bandService.deleteOne(id))
    }
  }

  object CrudApp {
    def apply(bandService: BandService, cons: mConsole) =
      new CrudApp(bandService, cons)
  }
}

object runner {
  import Programs._
  import Interpreters._
  import Interfaces._

  def main(args: Array[String]): Unit = {
    CrudApp(
      bandService = new BandService(
        bandRepo = inMemBandRepo
      ),
      new tConsole()
    ).run()
  }
}
