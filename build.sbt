name := "second"

version := "0.1"

scalaVersion := "2.11.12"


//val sparkVersion = "2.3.1"
//val sparkVersion = "2.2.2"
val sparkVersion = "2.4.0"


lazy val root = (project in file(".")).
  settings(
    name := "second",
    version := "1.0",
    scalaVersion := "2.12.8",
    mainClass in Compile := Some("second")
  )

resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.12" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-core" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-streaming" % "2.4.0" % "provided",
  "com.typesafe" % "config" % "1.3.4"
)

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
libraryDependencies += "org.apache.bahir" %% "spark-streaming-twitter" % "2.3.2"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}