package ToDo

object InMemTaskRepository extends TaskRepository {
  var nextId: Int = 0
  var state: Vector[Task] = Vector.empty[Task]

  override def createOne(v: String): Task = {
    val created = Task(nextId.toString(), v)

    state :+= created
    nextId += 1

    created
  }

  override def getOneById(id: String): Option[Task] = state.find(_.id == id)

  override def updateOne(t: Task): Task = {
    state = state.filterNot(_.id == t.id) :+ t
    t
  }

  override def dropOneById(id: String): Unit =
    state = state.filterNot(_.id == id)

  override def getAll() = state

}
