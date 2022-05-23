package const

import dao.entities.City

object Const {

    val Yekaterinburg: City   = City(lat = 56.50, lon = 50.35, code = "EKB", name = "Екатеринбург")
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
