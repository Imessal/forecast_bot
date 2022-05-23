package dao.repository

import dao.entities.User
import db.zioDS
import io.getquill.context.ZioJdbc._
import io.getquill.{EntityQuery, Quoted}
import zio.{Has, Task, ULayer, ZLayer}

object UserRepo {

    type UserRepo = Has[Service]

    import db.Ctx._

    trait Service {
        def register(user: User): QIO[User]
        def registerTask(user: User): Task[User]
        def findUser(userId: Long): QIO[Option[User]]
        def findUserTask(userId: Long): Task[Option[User]]
        def findCityCode(userId: Long): QIO[Option[String]]
        def changeCity(user: User, cityCode: String): QIO[Unit]
        def changeCityTask(user: User, cityCode: String): Task[Unit]
    }

    class Impl extends Service {

        val userSchema: Quoted[EntityQuery[User]] = quote {
            querySchema[User](""""User"""")
        }

        override def register(user: User): QIO[User] = run {
            userSchema.insert(lift(user))
        } andThen(zio.ZIO.succeed(user))

        override def findCityCode(userId: Long): QIO[Option[String]] = run {
            userSchema.filter(_.id == lift(userId)).map(_.cityCode)
        }.map(_.headOption)

        override def changeCity(user: User, cityCode: String): QIO[Unit] = {
            val newUser = user.copy(cityCode = cityCode)
            run {
                userSchema.filter(_.id == lift(user.id)).update(lift(newUser))
            }.unit
        }

        override def findUser(userId: Long): QIO[Option[User]] = run {
            userSchema.filter(_.id == lift(userId))
        }.map(_.headOption)

        override def registerTask(user: User): Task[User] = register(user)
            .mapError(someError => new RuntimeException(s"failed with: $someError"))
            .provideLayer(zioDS)

        override def findUserTask(userId: Long): Task[Option[User]] = findUser(userId)
            .mapError(someError => new RuntimeException(s"failed with: $someError"))
            .provideLayer(zioDS)

        override def changeCityTask(user: User, cityCode: String): Task[Unit] = changeCity(user, cityCode)
            .mapError(someError => new RuntimeException(s"failed with: $someError"))
            .provideLayer(zioDS)
    }

    val live: ULayer[UserRepo] = ZLayer.succeed(new Impl)

}
