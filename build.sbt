name := """DocClassification"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, LauncherJarPlugin)

scalaVersion := "2.11.7"

lazy val webJarsPlay = file("..").getAbsoluteFile.toURI

libraryDependencies ++= Seq(
  filters,
  javaJdbc,
  cache,
  javaWs,
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "html5shiv" % "3.7.3",
  "org.mongodb.morphia" % "morphia" % "1.2.1",
  "org.webjars.bower" % "angular" % "1.6.1",
  "org.webjars.bower" % "ngstorage" % "0.3.11",
  "org.webjars.bower" % "angular-resource" % "1.6.1",
  "org.webjars.bower" % "angular-checklist-model" % "0.10.0",
  "org.webjars.bower" % "angular-route" % "1.6.1",
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.8.1",
  "nz.ac.waikato.cms.weka" % "LibSVM" % "1.0.10")

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
)

unmanagedResourceDirectories in (Compile, runMain) <+=  baseDirectory ( _ /"../myresources")

routesGenerator := InjectedRoutesGenerator