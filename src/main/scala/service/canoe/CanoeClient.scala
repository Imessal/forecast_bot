package service.canoe

import canoe.api.TelegramClient
import configuration.{Config, Configuration}
import zio.{Has, Task, ZIO, ZLayer}
import zio.interop.catz._

object CanoeClient {

    type CanoeClient = Has[TelegramClient[Task]]

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

    val live: ZLayer[Any with Configuration, Throwable, CanoeClient] =
        ZLayer.fromManaged {
            for {
                config <- ZIO.service[Config].toManaged_
                client <- ZIO
                    .runtime[Any]
                    .toManaged_
                    .flatMap { implicit rts =>
                        TelegramClient[Task](
                            config.telegramBot.token,
                            rts.platform.executor.asEC
                        ).toManaged
                    }
            } yield client
        }
}
