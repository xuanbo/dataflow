package tk.fishfish.dataflow.task.script

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.core.env.Environment
import tk.fishfish.dataflow.exception.ShellException
import tk.fishfish.dataflow.task.{Argument, Result, Task}
import tk.fishfish.dataflow.util.{IOUtils, StringTemplate, Validation}

import java.nio.file.Paths
import java.util.UUID

class Shell extends Task {

  private val logger: Logger = LoggerFactory.getLogger(classOf[Shell])

  private var outputDir: String = _
  private var outputName: String = _

  override def name(): String = "SHELL"

  def execute(argument: Argument): Result = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")

    var command = argument.input.getString("command")
    Validation.nonEmpty(command, "配置 [argument.input.command] 不能为空")

    command = StringTemplate.render(command, argument.context.toMap)

    val executionId = argument.context.get("executionId") match {
      case Some(value) => value.toString
      case None => UUID.randomUUID().toString
    }
    val nodeId = argument.context.get("nodeId") match {
      case Some(value) => value.toString
      case None => UUID.randomUUID().toString
    }
    // outputDir/{executionId}/{nodeId}/outputName
    val outputPath = Paths.get(outputDir, executionId, nodeId, outputName)
    FileUtils.forceMkdir(outputPath.toFile.getParentFile)

    logger.info(s"执行命令: $command, 输出路径: ${outputPath.toString}")

    import scala.sys.process._
    val code = IOUtils.using(ProcessLogger(outputPath.toFile))(command ! _)
    if (code != 0) {
      throw new ShellException(s"执行命令: $command 进程退出码错误: $code")
    }

    Result.empty()
  }

  override def setSparkSession(spark: SparkSession): Unit = {}

  override def setEnv(env: Environment): Unit = {
    outputDir = env.getProperty("dataflow.shell.output.dir", "shell-output")
    outputName = env.getProperty("dataflow.shell.output.name", "output.txt")
  }

}