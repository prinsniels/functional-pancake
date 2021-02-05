package ToDo

trait OwnConsole {
  def getInputWithPrompt(prompt: String): String
  def putStringLine(line: String): Unit
  def putErrorLine(line: String): Unit
}

object OwnConsole {
  implicit def cons: OwnConsole = new OwnConsole {
    override def getInputWithPrompt(prompt: String): String =
      scala.io.StdIn.readLine(prompt)

    override def putStringLine(line: String): Unit =
      println(line)

    override def putErrorLine(line: String): Unit =
      scala.Console.err.println(line)

  }
}
