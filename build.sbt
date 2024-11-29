//name := """TubeLytics"""
//organization := "com.ytlytics"
//version := "1.0-SNAPSHOT"
//
//lazy val root = (project in file(".")).enablePlugins(PlayJava)
//
//scalaVersion := "2.13.15"
//javacOptions ++= Seq("-source", "11", "-target", "11")
//
//libraryDependencies ++= Seq(
//  // Play dependencies
//  guice,
//
//  // Core libraries
//  "org.json" % "json" % "20210307",
//  "org.mockito" % "mockito-core" % "4.0.0" % Test,
//  "junit" % "junit" % "4.13.2" % Test,
//  "org.apache.commons" % "commons-text" % "1.12.0"
//)
//
//libraryDependencies += "com.google.code.gson" % "gson" % "2.8.9"
//
//// Set Javadoc options to include private members
//doc / javacOptions ++= Seq("-private")
//
//// Include only .java files in `app` and `test` folders
//sources in (Compile, doc) := {
//  val srcDirs = Seq("app", "test") // Add more directories if needed
//  (sources in (Compile, doc)).value.filter(file =>
//    file.getName.endsWith(".java") && srcDirs.exists(dir => file.getPath.contains(s"/$dir/"))
//  )
//}
name := "TubeLytics"

organization := "com.ytlytics"

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.15"

// Enable Play Framework plugin
lazy val root = (project in file(".")).enablePlugins(PlayJava)

// Java version compatibility
javacOptions ++= Seq("-source", "11", "-target", "11")

// Dependencies
libraryDependencies ++= Seq(
  // Play dependencies
  guice,

  // Core libraries
  "com.typesafe.akka" %% "akka-stream" % "2.6.21",       // Updated to match Akka versions
  "com.typesafe.akka" %% "akka-actor" % "2.6.21",        // Updated to match Akka versions
  "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test, // Akka TestKit for testing actors

  "org.json" % "json" % "20210307",
  "org.apache.commons" % "commons-text" % "1.12.0",

  // Jackson (Scala and Core JSON handling)
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.3",

  // Gson for JSON parsing
  "com.google.code.gson" % "gson" % "2.8.9"
)

// Test dependencies
libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "4.0.0" % Test,
  "org.mockito" % "mockito-inline" % "4.0.0" % Test,
  "junit" % "junit" % "4.13.2" % Test
)

// Javadoc options
doc / javacOptions ++= Seq("-private")

// Include .java files in `app` and `test` folders for documentation
Compile / doc / sources := {
  val srcDirs = Seq("app", "test")
  (Compile / doc / sources).value.filter(file =>
    file.getName.endsWith(".java") && srcDirs.exists(dir => file.getPath.contains(s"/$dir/"))
  )
}
