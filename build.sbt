name := "mandelbrot"

version := "0.1"

scalaVersion := "2.11.4"

scalacOptions ++= Seq(
"-unchecked",
"-deprecation",
"-Xlint",
"-feature",
"-encoding", "UTF-8"
)

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8"
)

