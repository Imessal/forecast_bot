package dto

import io.circe._, io.circe.generic.semiauto._

object YandexWeatherDTOs {

    case class WeatherResponse(
        now: Long,
        now_dt: String,
        info: CityInfoYandexDTO,
        fact: FactYandexDTO,
        forecast: ForecastYandexDTO
    )

    object WeatherResponse {
        implicit val decoder: Decoder[WeatherResponse] = deriveDecoder[WeatherResponse]
    }

    case class CityInfoYandexDTO(lat: Double, lon: Double, url: String)

    object CityInfoYandexDTO {
        implicit val decoder: Decoder[CityInfoYandexDTO] = deriveDecoder[CityInfoYandexDTO]
    }

    case class FactYandexDTO(
        temp: Int,
        feels_like: Int,
        temp_water: Option[Int],
        icon: String,
        condition: String,
        wind_speed: Double,
        wind_gust: Double,
        wind_dir: String,
        pressure_mm: Int,
        pressure_pa: Int,
        humidity: Int,
        daytime: String,
        polar: Boolean,
        season: String,
        obs_time: Long
    )

    object FactYandexDTO {
        implicit val decoder: Decoder[FactYandexDTO] = deriveDecoder[FactYandexDTO]
    }

    case class ForecastYandexDTO(
        date: String,
        date_ts: Long,
        week: Int,
        sunrise: String,
        sunset: String,
        moon_code: Int,
        moon_text: String,
        parts: Seq[PartYandexDTO]
    )

    object ForecastYandexDTO {
        implicit val decoder: Decoder[ForecastYandexDTO] = deriveDecoder[ForecastYandexDTO]
    }

    case class PartYandexDTO(
        part_name: String,
        temp_min: Int,
        temp_max: Int,
        temp_avg: Int,
        feels_like: Int,
        icon: String,
        condition: String,
        daytime: String,
        polar: Boolean,
        wind_speed: Double,
        wind_gust: Double,
        wind_dir: String,
        pressure_mm: Int,
        pressure_pa: Int,
        humidity: Int,
        prec_mm: Double,
        prec_period: Int,
        prec_prob: Int
    )

    object PartYandexDTO {
        implicit val decoder: Decoder[PartYandexDTO] = deriveDecoder[PartYandexDTO]
    }

}
