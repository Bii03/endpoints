import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import EndpointsSettings._
import LocalCrossProject._

val algebra =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("algebra"))
    .settings(
      publishSettings,
      `scala 2.11 to latest`,
      name := "endpoints-algebra",
      libraryDependencies ++= Seq(
        "com.github.tomakehurst" % "wiremock" % "2.22.0" % Test,
        "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
      )
    )
    .dependsOnLocalCrossProjectsWithScope("json-schema" -> "test->test;compile->compile")

val `algebra-js` = algebra.js

val `algebra-jvm` = algebra.jvm

val `algebra-circe` =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("algebra-circe"))
    .settings(
      publishSettings,
      `scala 2.11 to 2.12`,
      name := "endpoints-algebra-circe",
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-parser" % circeVersion,
        "io.circe" %%% "circe-generic" % circeVersion % Test,
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" % Test cross CrossVersion.full)
      )
    )
    .dependsOn(`algebra` % "test->test;compile->compile")

val `algebra-circe-js` = `algebra-circe`.js

val `algebra-circe-jvm` = `algebra-circe`.jvm

val `algebra-protoless` =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("algebra-protoless"))
    .settings(
      publishSettings,
      `scala 2.11 to 2.12`,
      name := "endpoints-algebra-protoless",
      libraryDependencies ++= Seq(
        "io.protoless" %% "protoless-core" % "0.0.7",
        "io.protoless" %% "protoless-generic" % "0.0.7"
      )
    )
    .dependsOn(`algebra` % "test->test;compile->compile")

val `algebra-protoless-js` = `algebra-protoless`.js

val `algebra-protoless-jvm` = `algebra-protoless`.jvm

val `algebra-playjson` =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("algebra-playjson"))
    .settings(
      publishSettings,
      `scala 2.11 to 2.12`,
      name := "endpoints-algebra-playjson",
      libraryDependencies += "com.typesafe.play" %%% "play-json" % playjsonVersion
    )
    .dependsOn(`algebra` % "test->test;compile->compile")

val `algebra-playjson-js` = `algebra-playjson`.js
val `algebra-playjson-jvm` = `algebra-playjson`.jvm
