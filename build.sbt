lazy val root = (project in file("."))
  .settings(name := "play-micro")
  .aggregate(webGateway)
  .settings(commonSettings: _*)

organization in ThisBuild := "com.example"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "4.0.0"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"


lazy val webGateway = (project in file("web-gateway"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala && LagomPlay)
  .dependsOn(`hello-api`)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      lagomScaladslKafkaBroker,
      lagomScaladslKafkaClient,
      macwire,
      scalaTest,
      guice,
      "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final",
      "com.lightbend.play" %% "play-socket-io" % "1.0.0-beta-2"
    )
  )


lazy val `hello-api` = (project in file("hello-api"))
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomScaladslApi
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomScaladslKafkaBroker,
      "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.81",
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      lagomScaladslKafkaClient,
      lagomScaladslBroker,
      macwire
    )
  )
  .dependsOn(`hello-api`)


  def commonSettings: Seq[Setting[_]] = Seq()