import configuration._
import dao.repository.UserRepo
import db._
import service.canoe.{CanoeClient, CanoeScenarios, CanoeService}
import service.{ApiWeatherClient, ApiWeatherService}
import zio._
import zio.logging.slf4j.Slf4jLogger
import zio.magic._

object ZMain extends App {

    val appEnv =
        Configuration.live >+>
            UserRepo.live >+>
            CanoeClient.live >+>
            ApiWeatherClient.live >+>
            ApiWeatherService.live >+>
            Slf4jLogger.make((_, msg) => msg) >+>
            CanoeScenarios.live >+>
            CanoeService.live


    val app = for {
        _ <- performMigration
    } yield ()

    val magically: ZIO[Any, Throwable, Unit] =
        app.inject(
            Configuration.live,
            LiquibaseService.liquibaseLayer,
            LiquibaseService.live,
            zioDS
        )

    override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
        magically.andThen(appEnv.build.useForever).exitCode
    }

}
