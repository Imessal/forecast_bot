import sbt.{ModuleID, _}

object Dependencies {

    lazy val ZioVersion        = "1.0.12"
    lazy val PureconfigVersion = "0.12.3"
    lazy val LiquibaseVersion  = "3.4.2"

    lazy val PostgresVersion = "42.2.8"
    lazy val CirceVersion    = "0.13.0"

    lazy val Http4sVersion = "0.21.7"

    lazy val LogbackVersion = "1.2.3"

    lazy val bot4sVersion = "5.4.2"
    lazy val canoeVersion = "0.6.0"

    lazy val pureconfig: Seq[ModuleID] = Seq(
        "com.github.pureconfig" %% "pureconfig" % PureconfigVersion
    )

    lazy val logback = "ch.qos.logback" % "logback-classic" % LogbackVersion

    lazy val zio: Seq[ModuleID] = Seq(
        "dev.zio"              %% "zio"               % ZioVersion,
        "dev.zio"              %% "zio-test"          % ZioVersion,
        "dev.zio"              %% "zio-test-sbt"      % ZioVersion,
        "dev.zio"              %% "zio-macros"        % ZioVersion,
        "dev.zio"              %% "zio-logging-slf4j" % "0.3.2",
        "io.github.kitlangton" %% "zio-magic"         % "0.3.12"
    )

    // config
    lazy val zioConfig: Seq[ModuleID] = Seq(
        "dev.zio" %% "zio-config"          % "1.0.5",
        "dev.zio" %% "zio-config-magnolia" % "1.0.5",
        "dev.zio" %% "zio-config-typesafe" % "1.0.5",
        "dev.zio" %% "zio-interop-cats"    % "3.1.1.0"
    )

    lazy val liquibase = "org.liquibase" % "liquibase-core" % LiquibaseVersion

    lazy val postgres = "org.postgresql" % "postgresql" % PostgresVersion

    // http4s
    lazy val http4sServer: Seq[ModuleID] = Seq(
        "org.http4s" %% "http4s-circe" % Http4sVersion
    )

    lazy val telegram = Seq(
        "org.augustjune" %% "canoe" % canoeVersion
    )

}
