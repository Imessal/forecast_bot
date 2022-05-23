package service
import configuration.Config
import dao.entities.City
import dto.YandexWeatherDTOs._
import io.circe._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client._
import org.http4s.{EntityDecoder, EntityEncoder, Header, Method, Request, Uri}
import org.typelevel.ci.CIString
import zio.interop.catz._
import zio.interop.catz.implicits.rts.platform
import zio.{Has, Task, TaskManaged, UIO, ULayer, ZIO, ZLayer}

object ApiWeatherClient {
    implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]
    implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

    type ApiWeatherClient = Has[Service]

    implicit val zioRuntime: zio.Runtime[zio.ZEnv] =
        zio.Runtime.default
    implicit val dispatcher: cats.effect.std.Dispatcher[zio.Task] =
        zioRuntime
            .unsafeRun(
                cats.effect
                    .std
                    .Dispatcher[zio.Task]
                    .allocated
            )
            ._1

    trait Service {
        def makeRequest(city: City, config: Config): Task[WeatherResponse]
    }

    class Impl extends Service {
        def makeRequest(city: City, config: Config): Task[WeatherResponse] = {
            val req = Request[Task](method = Method.GET)
                .withUri(
                    Uri.fromString(
                        s"https://api.weather.yandex.ru/v2/informers?lat=${city.lat}&lon=${city.lon}&lang=ru"
                    ).toOption
                        .get
                )
                .withHeaders(
                    Header.Raw(name = CIString("X-Yandex-API-Key"), value = config.yandexWeather.apiKey)
                )
            for {
                clientR <- makeHttpClient
                resp    <- clientR.use(_.expect[WeatherResponse](req))
            } yield {
                resp
            }
        }
    }

    val live: ULayer[ApiWeatherClient] = ZLayer.succeed(new Impl)

    private def makeHttpClient: UIO[TaskManaged[Client[Task]]] =
        ZIO
            .runtime[Any]
            .map { implicit runtime =>
                BlazeClientBuilder
                    .apply[Task](platform.executor.asEC)
                    .resource
                    .toManaged
            }
}
