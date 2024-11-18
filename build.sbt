name := """TubeLytics"""
organization := "com.ytlytics"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"
javacOptions ++= Seq("-source", "11", "-target", "11")

libraryDependencies += guice
libraryDependencies += "org.json" % "json" % "20210307"
libraryDependencies += "org.mockito" % "mockito-core" % "4.0.0" % Test
libraryDependencies += "junit" % "junit" % "4.13.2" % Test
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "4.4.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "4.4.0" classifier "models"
libraryDependencies += "org.apache.commons" % "commons-text" % "1.12.0"

// Set Javadoc options to include private members
doc / javacOptions ++= Seq("-private")

// Include only .java files in `app` and `test` folders
sources in (Compile, doc) := {
  val srcDirs = Seq("app", "test") // Add more directories if needed
  (sources in (Compile, doc)).value.filter(file =>
    file.getName.endsWith(".java") && srcDirs.exists(dir => file.getPath.contains(s"/$dir/"))
  )
}
