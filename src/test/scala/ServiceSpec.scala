import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._

class ServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"

  override def config = testConfig

  override val logger = NoLogging

  val valueId = 1.toLong
  val testValue = "1234"
  val mapEntry = MapEntry(valueId, testValue)

  "Service" should "return a not found response when the key isn't in the map" in {
    Get(s"/map/$valueId") ~> routes ~> check {
      status shouldBe NotFound
      contentType shouldBe `text/plain(UTF-8)`
      //responseAs[MapEntry] shouldBe mapEntry
    }
  }

    ignore should "put values in the map" in {
      Post(s"/map", MapEntry(valueId, testValue)) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        //responseAs[IpPairSummary] shouldBe ipPairSummary
      }
    }
}


