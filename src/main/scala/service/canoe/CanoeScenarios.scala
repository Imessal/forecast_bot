package service.canoe

import canoe.api._
import canoe.api.models.Keyboard
import canoe.models.messages.{TelegramMessage, TextMessage}
import canoe.models.outgoing.StickerContent
import canoe.models.{Chat, KeyboardButton, ReplyKeyboardMarkup}
import canoe.syntax._
import cats.Functor
import const.Const._
import dao.entities.User
import dao.repository.UserRepo
import dao.repository.UserRepo.UserRepo
import db.DataSource
import dto.DTOHelper
import service.ApiWeatherService
import service.ApiWeatherService.ApiWeatherService
import zio.interop.catz._
import zio.{Has, Task, ZIO, ZLayer}

object CanoeScenarios {

    type DataSourceTask[A] = ZIO[DataSource, Throwable, A]
    type CanoeScenarios    = Has[Service]
    type Client            = Has[TelegramClient[Task]]
    type UserData          = (Long, String)

    sealed trait ErrorMsg

    trait Service {
        def greetings: Scenario[Task, Unit]
    }

    class Impl(
        canoeClient: TelegramClient[Task],
        apiWeatherService: ApiWeatherService.Service,
        userRepo: UserRepo.Service
    ) extends Service {

        implicit private val client: TelegramClient[Task] = canoeClient

        private val windEarthFireSticker  = StickerContent(Stickers.WindEarthFireStrickerMessage)
        private val noTimeForGamesSticker = StickerContent(Stickers.NoTimeForGamesStickerMessage)

        private val greetingKeyboardMarkup: ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(buttonColumn =
            Seq(KeyboardButton("Екатеринбург"), KeyboardButton("Москва"), KeyboardButton("Санкт-Петербург"))
        )
        private val greetingKeyboard: Keyboard.Reply = Keyboard.Reply(greetingKeyboardMarkup)

        private val cityMarkup: ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(buttonColumn =
            Seq(KeyboardButton("Узнать прогноз"), KeyboardButton("Сменить город"))
        )
        private val cityKeyboard: Keyboard.Reply = Keyboard.Reply(cityMarkup)

        def greetings: Scenario[Task, Unit] =
            for {
                msg <- Scenario.expect(command("start"))
                userIdName = msg.from.map(u => (u.id, u.firstName)).get // todo опасно
                chat       = msg.chat
                userExists <- Scenario.eval(userRepo.findUserTask(userIdName._1))
                _ <- userExists match {
                    case Some(user) => getWeather(chat, user)
                    case None       => registerUser(chat, userIdName).flatMap(user => getWeather(chat, user))
                }
            } yield ()

        def getWeather(chat: Chat, user: User): Scenario[Task, Unit] =
            for {
                _       <- Scenario.eval(chat.send("Приказывай, Вождь", keyboard = cityKeyboard))
                command <- safeRequest(chat)

                _ <- command.text match {
                    case "Узнать прогноз" =>
                        (for {
                            _       <- Scenario.eval(chat.send(windEarthFireSticker))
                            weather <- Scenario.eval(apiWeatherService.getWeatherForecastTask(user.id))
                            _       <- Scenario.eval(chat.send(DTOHelper.yandexDTOtoMainMessage(weather)))
                            _       <- Scenario.eval(chat.send(DTOHelper.yandexDTOtoForecastMessage(weather)))
                        } yield ()) >> getWeather(chat, user)

                    case "Сменить город" => changeCity(chat, user)
                    case _ =>
                        Scenario.eval(chat.send("Не шути, Вождь")) >> getWeather(
                            chat: Chat,
                            user: User
                        )
                }

            } yield ()

        def registerUser(chat: Chat, userData: UserData): Scenario[Task, User] = {
            for {
                _      <- Scenario.eval(chat.send("Приказывай..."))
                _      <- Scenario.eval(chat.send("В каком городе смотрим погоду?", keyboard = greetingKeyboard))
                city   <- safeRequest(chat).map(_.text)
                exists <- Scenario.pure(Cities.citiesByTitle.contains(city))
                res <-
                    if (exists) Scenario.eval {
                        val userCity = Cities.citiesByTitle(city)
                        val user     = User(id = userData._1, name = userData._2, cityCode = userCity.code)
                        userRepo.registerTask(user)
                    }
                    else
                        Scenario.eval(chat.send("Я не знаю такого города, выбери другой")) >> registerUser(
                            chat,
                            userData
                        )
            } yield res
        }

        def changeCity(chat: Chat, user: User): Scenario[Task, Unit] = {
            for {
                _      <- Scenario.eval(chat.send("Да, Вождь?"))
                _      <- Scenario.eval(chat.send("В каком городе смотрим погоду?", keyboard = greetingKeyboard))
                city   <- safeRequest(chat).map(_.text)
                exists <- Scenario.pure(Cities.citiesByTitle.contains(city))
                res <-
                    if (exists) Scenario.eval {
                        val newCity = Cities.citiesByTitle(city)
                        userRepo.changeCityTask(user, newCity.code)
                    } >> getWeather(chat, user)
                    else
                        Scenario.eval(chat.send("Я не знаю такого города, выбери другой")) >> changeCity(
                            chat: Chat,
                            user: User
                        )
            } yield res
        }

        def safeRequest(chat: Chat): Scenario[Task, TextMessage] = {
            for {
                msg     <- Scenario.expect(any)
                textMsg <- Scenario.eval(processBadReq[Task](msg))
                res <- textMsg match {
                    case Left(_)      => Scenario.eval(chat.send(noTimeForGamesSticker)) >> safeRequest(chat)
                    case Right(value) => Scenario.eval(Task(value))
                }
            } yield res
        }

        private def processBadReq[F[_]: TelegramClient: Functor](
            msg: TelegramMessage
        ): Task[Either[ErrorMsg, TextMessage]] = {
            case object Error extends ErrorMsg
            msg match {
                case textMessage: TextMessage => Task(Right(textMessage))
                case _                        => Task(Left(Error))
            }
        }

    }

    val live: ZLayer[Client with ApiWeatherService with UserRepo, Nothing, CanoeScenarios] =
        ZLayer.fromManaged {
            for {
                client         <- ZIO.service[TelegramClient[Task]].toManaged_
                weatherService <- ZIO.service[ApiWeatherService.Service].toManaged_
                repo           <- ZIO.service[UserRepo.Service].toManaged_
            } yield new Impl(client, weatherService, repo)
        }

}
