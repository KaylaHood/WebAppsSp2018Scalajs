version := "1.0-SNAPSHOT"

// Server sub-project - this is where the Play stuff is
lazy val server = (project in file("server")).settings(commonSettings).settings(
  name := "CSCI3345-S18-server",
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    guice,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
//    "edu.trinity" %% "swiftvis2" % "0.1.0-SNAPSHOT",

    "com.typesafe.play" %% "play-json" % "2.6.9",

    "com.typesafe.play" %% "play-slick" % "3.0.3",
    "mysql" % "mysql-connector-java" % "6.0.6",
    "com.typesafe.slick" %% "slick-codegen" % "3.2.3"
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

// Client sub-project - this is where the Scala.js stuff is
lazy val client = (project in file("client")).settings(commonSettings).settings(
  name := "CSCI3345-S18-client",
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    "org.querki" %%% "jquery-facade" % "1.2"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

// Shared subproject - this is where you put anything for both server and client
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(commonSettings,
  name := "CSCI3345-S18-shared")
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.12.5",
  organization := "edu.trinity"
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}


resolvers += Resolver.sonatypeRepo("snapshots")

