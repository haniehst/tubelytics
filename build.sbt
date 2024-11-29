name := """TubeLytics"""
organization := "com.ytlytics"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"
javacOptions ++= Seq("-source", "11", "-target", "11")

libraryDependencies ++= Seq(
  // Play dependencies
  guice,

  // Core libraries
  "org.json" % "json" % "20210307",
  "org.mockito" % "mockito-core" % "4.0.0" % Test,
  "junit" % "junit" % "4.13.2" % Test,
  "org.apache.commons" % "commons-text" % "1.12.0"
)

libraryDependencies += "com.google.code.gson" % "gson" % "2.8.9"

// Set Javadoc options to include private members
doc / javacOptions ++= Seq("-private")

// Include only .java files in `app` and `test` folders
sources in (Compile, doc) := {
  val srcDirs = Seq("app", "test") // Add more directories if needed
  (sources in (Compile, doc)).value.filter(file =>
    file.getName.endsWith(".java") && srcDirs.exists(dir => file.getPath.contains(s"/$dir/"))
  )
}