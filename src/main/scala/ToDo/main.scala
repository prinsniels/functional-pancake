package ToDo

object main extends App {
   val program = Cli.build(InMemTaskRepository)
   program.run()
}
