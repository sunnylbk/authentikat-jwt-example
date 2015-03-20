name := "authentikat-jwt-example"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.6"

javaOptions ++= Seq(
  "-Xms512M", "-Xmx2G", "-Xss1M",
  "-XX:+CMSClassUnloadingEnabled",
  "-XX:+UseConcMarkSweepGC"
)

scalacOptions ++= Seq(
  "-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature", //"-optimise",
  "-Xmigration", //"–Xverify", "-Xcheck-null", "-Ystatistics",
  "-Yinline-warnings", "-Ywarn-dead-code", "-Ydead-code"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.1.1" % "test",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.9",
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9",
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.1",
  "io.spray" %% "spray-can" % "1.3.1",
  "io.spray" %% "spray-http" % "1.3.1",
  "io.spray" %% "spray-json" % "1.3.1",
  "io.spray" %% "spray-client" % "1.3.1"
)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe simple" at "http://repo.typesafe.com/typesafe/simple/maven-releases/"
)