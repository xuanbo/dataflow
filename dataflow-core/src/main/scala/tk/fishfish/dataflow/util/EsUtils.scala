package tk.fishfish.dataflow.util

import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.http.HttpHeaders

import java.util.function.Consumer
import scala.beans.BeanProperty

/**
 * es工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object EsUtils {

  val esUrlPrefix = "jdbc:es://"
  val opendistroUrlPrefix = "jdbc:es:opendistro://"

  def translate(endpoint: String, sql: String, user: String, password: String): String = {
    val esQuery = new EsQuery()
    esQuery.query = sql
    val resp = HttpUtils.post(s"$endpoint/_sql/translate", esQuery, classOf[ObjectNode], new Consumer[HttpHeaders] {
      override def accept(headers: HttpHeaders): Unit = headers.setBasicAuth(user, password)
    })
    if (resp.getStatusCode.is2xxSuccessful()) {
      resp.getBody.get("query").toString
    } else {
      throw new RuntimeException(s"sql转化为dsl失败: $sql")
    }
  }

}

class EsQuery {

  @BeanProperty
  var query: String = _

}
