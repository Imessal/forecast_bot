import Dependencies.{liquibase, logback}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
    .settings(
        name         := "forecast_bot",
        version      := "0.1",
        scalaVersion := "2.13.3",
        libraryDependencies ++= Dependencies.zio,
        libraryDependencies ++= Dependencies.pureconfig,
        libraryDependencies ++= Dependencies.zioConfig,
        libraryDependencies ++= Dependencies.telegram,
        libraryDependencies ++= Seq(
            "io.getquill"          %% "quill-jdbc-zio" % "3.12.0",
            "io.github.kitlangton" %% "zio-magic"      % "0.3.11",
            "org.postgresql"        % "postgresql"     % "42.3.1"
        ),
        libraryDependencies ++= Seq(
            liquibase,
            logback
        )
    )
