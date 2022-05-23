import zio._
import zio.config.ReadError
import zio.config.typesafe.TypesafeConfig

package object configuration {
    case class Config(liquibase: LiquibaseConfig, db2: DbConfig, telegramBot: TelegramBot, yandexWeather: YandexWeather)

    case class LiquibaseConfig(changeLog: String)
    case class DbConfig(driver: String, url: String, user: String, password: String)
    case class YandexWeather(apiKey: String) extends AnyVal
    case class TelegramBot(token: String)

    import zio.config.magnolia.DeriveConfigDescriptor.descriptor

    private val configDescriptor = descriptor[Config]

    type Configuration = zio.Has[Config]

    object Configuration {
        val live: Layer[ReadError[String], Configuration] = TypesafeConfig.fromDefaultLoader(configDescriptor)
    }
}
