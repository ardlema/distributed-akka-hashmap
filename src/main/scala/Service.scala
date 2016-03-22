import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}

case class MapEntry(key: Long, value: String)

trait Protocols extends DefaultJsonProtocol {
  implicit val ipInfoFormat = jsonFormat2(MapEntry.apply)
}

trait Service extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter
  var hashmap = Map[Long, String]()

  val routes = {
    //TODO: Add logging
    pathPrefix("map") {
      (get & path(Segment)) {id =>
        complete {
          getIdFromMap(id.toLong).map[ToResponseMarshallable] {
            case Right(value) => value
            case Left(errorCode) => errorCode match {
              case NotFound.intValue => NotFound -> NotFound.defaultMessage
              case _ => BadRequest -> StatusCode.int2StatusCode(errorCode).defaultMessage
            }
            }
          }
        }
      }
    }

  def getIdFromMap(id: Long): Future[Either[Int, MapEntry]] =
    hashmap.get(id).map(value => Future.successful(Right(MapEntry(id,
      value)))) getOrElse Future.successful(Left(NotFound.intValue))
}

object AkkaHttpMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
