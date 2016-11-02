name := """DocClassification"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

lazy val webJarsPlay = file("..").getAbsoluteFile.toURI

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.apache.spark" % "spark-core_2.11" % "2.0.1",
  "org.apache.spark" % "spark-sql_2.11" % "2.0.1",
  "org.apache.spark" % "spark-mllib_2.11" % "2.0.1",
  "com.google.code.gson" % "gson" % "2.5",
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "html5shiv" % "3.7.3",
  "org.mongodb.morphia" % "morphia" % "1.2.1",
  "org.webjars" % "angularjs" % "1.5.8",
  "org.webjars" % "angular-ui-bootstrap" % "1.3.3"
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
)

routesGenerator := InjectedRoutesGenerator