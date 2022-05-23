package service

import configuration.{Config, Configuration}
import const.Const
import dao.entities.User
import dao.repository.UserRepo
import dao.repository.UserRepo.UserRepo
import db.{DataSource, zioDS}
import dto.YandexWeatherDTOs.WeatherResponse
import service.ApiWeatherClient.ApiWeatherClient
import zio.{Has, Task, ZIO, ZLayer}

object ApiWeatherService {

    type ApiWeatherService = Has[Service]

    trait Service {
        def registerUser(user: User): ZIO[DataSource, Throwable, Unit]
        def registerUserTask(user: User): Task[Unit]
        def getWeatherForecast(userId: Long): ZIO[DataSource, Throwable, WeatherResponse]
        def getWeatherForecastTask(userId: Long): Task[WeatherResponse]
    }

    class Impl(weatherClient: ApiWeatherClient.Service, userRepo: UserRepo.Service, config: Config) extends Service {
        override def registerUser(user: User): ZIO[DataSource, Throwable, Unit] = {
            userRepo.register(user).unit
        }

        override def getWeatherForecast(userId: Long): ZIO[DataSource, Throwable, WeatherResponse] = for {
            userCity <- userRepo.findCityCode(userId).map(_.getOrElse("MSK"))
            city = Const.citiesByCode.getOrElse(userCity, Const.Moscow)
            response <- weatherClient.makeRequest(city, config)
        } yield response

        override def registerUserTask(user: User): Task[Unit] = registerUser(user)
            .mapError(someError => new RuntimeException(s"failed with: $someError"))
            .provideLayer(zioDS)

        override def getWeatherForecastTask(userId: Long): Task[WeatherResponse] = getWeatherForecast(userId)
            .mapError(someError => new RuntimeException(s"failed with: ${someError.getMessage}"))
            .provideLayer(zioDS)
    }

    val live: ZLayer[ApiWeatherClient with UserRepo with Configuration, Nothing, ApiWeatherService] =
        ZLayer.fromManaged {
            for {
                weatherClient <- ZIO.service[ApiWeatherClient.Service].toManaged_
                userRepo <- ZIO.service[UserRepo.Service].toManaged_
                config <- ZIO.service[Config].toManaged_
            } yield new Impl(weatherClient, userRepo, config)
        }

}
