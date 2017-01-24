name := """DocClassification"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

lazy val webJarsPlay = file("..").getAbsoluteFile.toURI

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.2",
  "org.json" % "json" % "20160810",
  "com.github.axet" % "wget" % "1.4.3",
  "org.apache.spark" % "spark-core_2.11" % "2.0.1",
  "org.apache.spark" % "spark-sql_2.11" % "2.0.1",
  "org.apache.spark" % "spark-mllib_2.11" % "2.0.1",
  "com.google.code.gson" % "gson" % "2.5",
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "html5shiv" % "3.7.3",
  "org.mongodb.morphia" % "morphia" % "1.2.1",
  "org.webjars.bower" % "angular" % "1.6.1",
  //"org.webjars.bower" % "angular-ui-bootstrap" % "2.3.0",
  "org.webjars.bower" % "ngstorage" % "0.3.11",
  "org.webjars.bower" % "angular-resource" % "1.6.1",
  "org.webjars.bower" % "angular-checklist-model" % "0.10.0",
  "org.webjars.bower" % "angular-route" % "1.6.1"
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
)

routesGenerator := InjectedRoutesGenerator