package ToDo

trait Crud {
  def createOne(data: String): Task

  def getById(id: String): Option[Task]

  def deleteById(id: String): Unit
}

object Crud {
  def dsl(
      taskRepository: TaskRepository
  ): Crud = new Crud {

    override def createOne(v: String): Task =
      taskRepository.createOne(v)

    override def getById(id: String): Option[Task] =
      taskRepository.getOneById(id)

    override def deleteById(id: String): Unit =
      taskRepository.dropOneById(id)

  }

}
