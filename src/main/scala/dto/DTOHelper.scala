package dto

import dto.YandexWeatherDTOs.WeatherResponse

object DTOHelper {

    val weatherByCondition = Map(
        "clear"                  -> "ясно ☀️",
        "partly-cloudy"          -> "малооблачно 🌤",
        "cloudy"                 -> "облачно с прояснениями ⛅️",
        "overcast"               -> "пасмурно ☁️",
        "drizzle"                -> "морось 🌧",
        "light-rain"             -> "небольшой дождь 🌧",
        "rain"                   -> "дождь 🌧",
        "moderate-rain"          -> "умеренно сильный дождь 🌧",
        "heavy-rain"             -> "сильный дождь 🌧",
        "continuous-heavy-rain"  -> "длительный сильный дождь 🌧",
        "showers"                -> "ливень 🌧",
        "wet-snow"               -> "дождь со снегом 🌨",
        "light-snow"             -> "небольшой снег 🌨",
        "snow"                   -> "снег 🌨",
        "snow-showers"           -> "снегопад 🌨",
        "hail"                   -> "град 🌧",
        "thunderstorm"           -> "гроза 🌩",
        "thunderstorm-with-rain" -> "дождь с грозой ⛈",
        "thunderstorm-with-hail" -> "гроза с градом ⛈"
    )

    val dayParts = Map(
        "night"   -> "ночь",
        "morning" -> "утро",
        "day"     -> "день",
        "evening" -> "вечер"
    )

    def humidity(percentage: Int): String = {
        if (percentage < 50) {
            s"${percentage}% 💧"
        } else s"$percentage% 💦"
    }

    def makeDate(dateStr: String): String = {
        dateStr.split('T').head.split('-').reverse.mkString(".")
    }

    def yandexDTOtoMainMessage(dto: WeatherResponse): String = {
        val humidity = DTOHelper.humidity(dto.fact.humidity)

        s"Дата: ${makeDate(dto.now_dt)}\n" ++
            s"Температура: ${dto.fact.temp}° (ощущается как ${dto.fact.feels_like}°)\n" ++
            s"${weatherByCondition.getOrElse(dto.fact.condition, "неизвестное состояние погоды").capitalize}\n" ++
            s"Ветер: ${dto.fact.wind_speed} 💨\n" ++
            s"Влажность: $humidity\n" ++
            s"Время восхода Солнца: ${dto.forecast.sunrise} 🌅\n" ++
            s"Время заката Солнца: ${dto.forecast.sunset} 🌇"

    }

    def yandexDTOtoForecastMessage(dto: WeatherResponse): String = {
        "Прогноз на ближайшее время: \n\n" ++
            s"${dto.forecast
                    .parts
                    .map { part =>
                        val humidity = DTOHelper.humidity(part.humidity)
                        "*" + dayParts.getOrElse(part.part_name, "ну и время у вас").capitalize + "*\n" ++
                            s"Температура: ${part.temp_avg}° (ощущается как ${part.feels_like}°)\n" ++
                            s"${weatherByCondition.getOrElse(dto.fact.condition, "неизвестное состояние погоды").capitalize}\n" ++
                            s"Ветер: ${dto.fact.wind_speed} 💨\n" ++
                            s"Влажность: $humidity\n" ++
                            s"Вероятность выпадения осадков: ${part.prec_prob}% \n"
                    }
                    .mkString("\n")}"
    }
}
