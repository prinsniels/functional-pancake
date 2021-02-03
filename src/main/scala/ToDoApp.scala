import Domain.Task

import scala.annotation.tailrec
import scala.io.StdIn.readLine

object ToDoApp extends App {

  // return the maximum task number or 0
  val maxTaskNumber: Seq[Task] => Int = x => x.map(_.id).maxOption.getOrElse(0)

  val getTask: (Seq[Task], Int) => Option[Task] = (tasks, id) => tasks.find(_.id == id)

  def echoTask(task: Option[Task]): Unit = task match {
    case Some(task) => println(s"${task.id} -> ${task.name}")
    case None => println("No task there!")
  }

  def showHelp(): Unit = {
    val text = """
                 |Possible commands
                 |-----------------
                 |h                - show help
                 |add:[task name]  - add a to-do item
                 |rm:[task number] - remove a task by its number
                 |e:[task number]  - echo task
                 |ls               - view tasks
                 |q                - quit
        """.stripMargin
    println(text)
  }

  val addTaskRegex = "^add:(.+)".r
  val removeTaskRegex = raw"^rm:(\d+)".r
  val showTaskRegex = raw"^e:(\d+)".r

  @tailrec
  def mainApp(tasks: Vector[Task]): Unit = {

    val userInput = readLine("Tell me:")

    if (userInput == "q") ()
    else {
      userInput match {
        case addTaskRegex(task)  =>
          println(s"add ${task}")
          mainApp(Task(maxTaskNumber(tasks) + 1, task) +: tasks)
        case removeTaskRegex(taskNumber) =>
          println(s"remove ${taskNumber}")
          mainApp(tasks.filter(_.id != taskNumber.toInt)) // this is safe due to the nature of the regex
        case showTaskRegex(taskNumber) =>
          println(s"show task  ${taskNumber}")
          echoTask(getTask(tasks, taskNumber.toInt))
          mainApp(tasks)
        case "ls" =>
          tasks.foreach(t => echoTask(Some(t)))
          mainApp(tasks)
        case "h" =>
          showHelp()
          mainApp(tasks)
        case _ =>
          println(s"Could not understand ${userInput}")
          showHelp()
          mainApp(tasks)
      }
    }
  }

  mainApp(Vector.empty[Task])

}