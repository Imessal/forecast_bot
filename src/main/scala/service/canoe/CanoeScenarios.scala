package service.canoe

import canoe.api._
import canoe.api.models.Keyboard
import canoe.models.{Chat, KeyboardButton, ReplyKeyboardMarkup}
import canoe.syntax._
import const.Const
import dao.entities.User
import dao.repository.UserRepo
import dao.repository.UserRepo.UserRepo
import db.DataSource
import dto.DTOHelper
import service.ApiWeatherService
import service.ApiWeatherService.ApiWeatherService
import zio.{Has, Task, ZIO, ZLayer}

object CanoeScenarios {

    type DataSourceTask[A] = ZIO[DataSource, Throwable, A]
    type CanoeScenarios    = Has[Service]
    type Client            = Has[TelegramClient[Task]]
    type UserData          = (Long, String)

    trait Service {
        def greetings: Scenario[Task, Unit]
    }

    class Impl(
        canoeClient: TelegramClient[Task],
        apiWeatherService: ApiWeatherService.Service,
        userRepo: UserRepo.Service
    ) extends Service {

        implicit private val client: TelegramClient[Task] = canoeClient

        val greetingKeyboardMarkup: ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(buttonColumn =
            Seq(KeyboardButton("Екатеринбург"), KeyboardButton("Москва"), KeyboardButton("Санкт-Петербург"))
        )
        val greetingKeyboard: Keyboard.Reply = Keyboard.Reply(greetingKeyboardMarkup)

        val cityMarkup: ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(buttonColumn =
            Seq(KeyboardButton("Узнать прогноз"), KeyboardButton("Сменить город"))
        )
        val cityKeyboard: Keyboard.Reply = Keyboard.Reply(cityMarkup)

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
                _       <- Scenario.eval(chat.send("Жду приказаний", keyboard = cityKeyboard))
                command <- Scenario.expect(text)

                _ <- command match {
                    case "Узнать прогноз" =>
                        (for {
                            weather <- Scenario.eval(apiWeatherService.getWeatherForecastTask(user.id))
                            _       <- Scenario.eval(chat.send(DTOHelper.yandexDTOtoMainMessage(weather)))
                            _       <- Scenario.eval(chat.send(DTOHelper.yandexDTOtoForecastMessage(weather)))
                        } yield ()) >> getWeather(chat, user)

                    case "Сменить город" => changeCity(chat, user)
                    case _ =>
                        Scenario.eval(chat.send("Я не знаю такой команды")) >> getWeather(
                            chat: Chat,
                            user: User
                        )
                }

            } yield ()

        def registerUser(chat: Chat, userData: UserData): Scenario[Task, User] = {
            for {
                _      <- Scenario.eval(chat.send("В каком городе смотрим погоду?", keyboard = greetingKeyboard))
                city   <- Scenario.expect(text)
                exists <- Scenario.pure(Const.citiesByTitle.contains(city))
                res <-
                    if (exists) Scenario.eval {
                        val userCity = Const.citiesByTitle(city)
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
                _      <- Scenario.eval(chat.send("В каком городе смотрим погоду?", keyboard = greetingKeyboard))
                city   <- Scenario.expect(text)
                exists <- Scenario.pure(Const.citiesByTitle.contains(city))
                res <-
                    if (exists) Scenario.eval {
                        val newCity = Const.citiesByTitle(city)
                        userRepo.changeCityTask(user, newCity.code)
                    } >> getWeather(chat, user)
                    else
                        Scenario.eval(chat.send("Я не знаю такого города, выбери другой")) >> changeCity(
                            chat: Chat,
                            user: User
                        )
            } yield res
        }

        def parseBadReq(chat: Chat): Scenario[Task, Unit] = {
            for {
                _ <- Scenario.eval(chat.send("Я не понимаю, попробуй ещё раз"))
            } yield ()
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
