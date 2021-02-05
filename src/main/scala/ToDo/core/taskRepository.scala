package ToDo
/**
  * Port towards task repository, 
  * used to serve the implementation of the use casus in 
  * the core. This will only guide the implementation, the actual
  * implementation of this code will live in the layer it
  * lives in
  */
trait TaskRepository {

  def createOne(v: String): Task

  def getOneById(id: String): Option[Task]

  def getAll(): Vector[Task]

  def updateOne(t: Task): Task

  def dropOneById(id: String): Unit

}
