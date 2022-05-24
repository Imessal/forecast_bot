package const

import canoe.models.InputFile
import dao.entities.City

object Const {

    object Stickers {

        val WindEarthFireStrickerMessage: InputFile.Existing =
            InputFile.Existing(key = "CAACAgIAAxkBAAEEz5hijItEQIffBfPyFK6R0_NrqR9obQACRwEAApAAAVAg3CkzICIOccckBA")

        val NoTimeForGamesStickerMessage: InputFile.Existing =
            InputFile.Existing(key = "CAACAgIAAxkBAAEEz5ZijIsNd6xUokmyKo7LrFvCOlnPeQACYAEAApAAAVAgpM6ikg8vdOMkBA")
    }

    object Cities {

        val Yekaterinburg: City   = City(lat = 56.50, lon = 60.35, code = "EKB", name = "Екатеринбург")
        val Moscow: City          = City(lat = 55.45, lon = 37.3656, code = "MSK", name = "Москва")
        val SaintPetersburg: City = City(lat = 59.57, lon = 30.19, code = "SPB", name = "Санкт-Петербург")

        val citiesByCode = Map(
            "EKB" -> Yekaterinburg,
            "MSK" -> Moscow,
            "SPB" -> SaintPetersburg
        )

        val citiesByTitle: Map[String, City] = citiesByCode
            .values
            .map { city =>
                city.name -> city
            }
            .toMap
    }

}
