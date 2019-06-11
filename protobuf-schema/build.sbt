import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import EndpointsSettings._
import LocalCrossProject._

val `protobuf-schema` =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("protobuf-schema"))
    .settings(
      publishSettings,
      `scala 2.11 to latest`,
      name := "endpoints-algebra-protobuf-schema",
      addScalaTestCrossDependency,
      libraryDependencies += "org.scala-lang.modules" %%% "scala-collection-compat" % "0.3.0"
    )

val `protobuf-schema-js` = `protobuf-schema`.js
val `protobuf-schema-jvm` = `protobuf-schema`.jvm

lazy val `protobuf-schema-protoless` =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("protobuf-schema-protoless"))
    .settings(
      publishSettings,
      `scala 2.11 to 2.12`,
      name := "endpoints-protobuf-schema-protoless",
      resolvers += Resolver.bintrayRepo("julien-lafont", "maven"),
      libraryDependencies ++= Seq(
        "io.protoless" %% "protoless-core" % "0.0.7",
        "io.protoless" %% "protoless-generic" % "0.0.7"
      )
    )
    .dependsOnLocalCrossProjects("algebra-protoless") // Needed only because of ProtolessCodec, but that class doesn’t depend on the algebra
    .dependsOnLocalCrossProjectsWithScope("protobuf-schema" -> "test->test;compile->compile")

lazy val `protobuf-schema-protoless-js` = `protobuf-schema-protoless`.js
lazy val `protobuf-schema-protoless-jvm` = `protobuf-schema-protoless`.jvm

