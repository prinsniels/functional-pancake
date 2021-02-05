package ToDo

trait Cli {
  def run(): Unit
}

object Cli {
  def build(
      taskRepository: TaskRepository
  )(implicit
      cons: OwnConsole
  ): Cli = new Cli {

    val menu: String = """
            |Possible commands
            |-----------------
            |a   - add a to-do item
            |r   - remove a task by its number
            |e   - echo task
            |l   - view tasks
            |q   - quit
        """.stripMargin

    def run(): Unit =  {

      @scala.annotation.tailrec
      def loop(): Unit =
        prompt match {
          case "a" => create(); loop()
          case "r" => remove(); loop()
          case "e" => echo(); loop()
          case "l" => showAll(); loop()
          case "q" => ()
          case _   => loop()
        }

      loop()
    }

    def prompt: String =
      cons.getInputWithPrompt(menu)

    private def create(): Unit = {
      cons
        .getInputWithPrompt("Please give a activity description:")
        .pipe(inp => taskRepository.createOne(inp))

    }

    private def remove() =
      cons
        .getInputWithPrompt("please give an id <string>")
        .pipe(taskRepository.dropOneById(_))

    private def echo() =
      cons
        .getInputWithPrompt("please give an id <string>")
        .pipe(taskRepository.getOneById(_))
        .pipe({
          case None       => cons.putErrorLine("Not found")
          case Some(task) => cons.putStringLine(task.toString)
        })

    private def showAll() =
      taskRepository.getAll().foreach(x => cons.putStringLine(x.toString))

  }
}
