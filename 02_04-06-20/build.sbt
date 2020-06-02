name := """kubernetes-play-example"""
organization := "com.gu"

version := "1.1-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin)

scalaVersion := "2.13.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

dockerUsername := Some("mrbbarton")
dockerExposedPorts += 9000

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.gu.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.gu.binders._"
