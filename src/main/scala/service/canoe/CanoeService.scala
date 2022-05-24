package service.canoe

import canoe.api.{Bot, TelegramClient}
import service.canoe.CanoeScenarios.CanoeScenarios
import zio.interop.catz._
import zio.logging.Logger
import zio.{Has, Task, ZIO, ZLayer}

object CanoeService {

    type CanoeService = Has[Service]

    type ZLogger = Has[Logger[String]]

    trait Service {
        def start: Task[Unit]
    }

    class Impl(canoeScenarios: CanoeScenarios.Service, canoeClient: TelegramClient[Task], logger: Logger[String])
        extends Service {

        implicit val canoe: TelegramClient[Task] = canoeClient

        override def start: Task[Unit] = {
            logger.info("Starting Telegram bot...") *>
                Bot
                    .polling[Task]
                    .follow(
                        canoeScenarios.greetings
                    )
                    .compile
                    .drain
        }
    }

    val live: ZLayer[ZLogger with CanoeScenarios with CanoeClient.CanoeClient, Throwable, Has[Unit]] =
        ZLayer.fromManaged {
            for {
                logger    <- ZIO.service[Logger[String]].toManaged_
                client    <- ZIO.service[TelegramClient[Task]].toManaged_
                scenarios <- ZIO.service[CanoeScenarios.Service].toManaged_
                service = new Impl(scenarios, client, logger)
                start <- service.start.toManaged_
            } yield start
        }
}
