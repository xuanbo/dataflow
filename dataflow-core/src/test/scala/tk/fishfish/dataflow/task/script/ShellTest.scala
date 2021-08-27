package tk.fishfish.dataflow.task.script

import org.junit.Test
import tk.fishfish.dataflow.util.IOUtils

import java.io.File
import scala.sys.process._

/**
 * 测试Shell
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class ShellTest {

  @Test
  def run(): Unit = {
    val code = IOUtils.using(ProcessLogger(new File("a.txt")))("tree" ! _)
    println(code)
  }

}
