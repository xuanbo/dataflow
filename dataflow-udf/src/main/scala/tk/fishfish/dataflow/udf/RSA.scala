package tk.fishfish.dataflow.udf

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions
import tk.fishfish.dataflow.exception.UDFException

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64
import javax.crypto.Cipher

/**
 * RSA加解密
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object RSA {

  val factory: KeyFactory = KeyFactory.getInstance("RSA")

}

class RSAEncode extends UDF {

  override def name(): String = "rsa_encode"

  override def udf: UserDefinedFunction = functions.udf(
    (value: String, publicKey: String) => {
      if (value == null || publicKey == null) {
        value
      } else {
        try {
          val publicKeyBytes = Base64.getDecoder.decode(publicKey)
          val rsaPublicKey = RSA.factory.generatePublic(new X509EncodedKeySpec(publicKeyBytes))
          val cipher = Cipher.getInstance("RSA")
          cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
          val encoded = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8))
          Base64.getEncoder.encodeToString(encoded)
        } catch {
          case e: Exception => throw new UDFException(s"UDF[${name()}]执行错误，value: $value, publicKey: $publicKey", e)
        }
      }
    }
  )

}

class RSADecode extends UDF {

  override def name(): String = "rsa_decode"

  override def udf: UserDefinedFunction = functions.udf(
    (value: String, privateKey: String) => {
      if (value == null || privateKey == null) {
        value
      } else {
        try {
          val privateKeyBytes = Base64.getDecoder.decode(privateKey)
          val encryptedBytes = Base64.getDecoder.decode(value)
          val rsaPrivateKey = RSA.factory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes))
          val cipher = Cipher.getInstance("RSA")
          cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey)
          val decrypted = cipher.doFinal(encryptedBytes)
          new String(decrypted)
        } catch {
          case e: Exception => throw new UDFException(s"UDF[${name()}]执行错误，value: $value, privateKey: $privateKey", e)
        }
      }
    }
  )

}
