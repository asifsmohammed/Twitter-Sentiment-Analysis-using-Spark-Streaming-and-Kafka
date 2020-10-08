name := "Assignment3"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"

lazy val root = (project in file(".")).
  settings(
    name := "kafka",
    version := "1.0",
    scalaVersion := "2.12.8",
    mainClass in Compile := Some("spearkStreaming")
  )

resolvers += "SparkPackages" at "https://dl.bintray.com/spark-packages/maven"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "org.apache.kafka"%"kafka-clients"%"2.0.1",
  "org.scala-lang" % "scala-library" % "2.11.12" % "provided",
  "org.scala-lang" % "scala-library" % "2.11.12" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-core" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-streaming" % "2.4.0" % "provided",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "graphframes" % "graphframes" % "0.7.0-spark2.4-s_2.11",
  "com.typesafe" % "config" % "1.3.4"
)

libraryDependencies += "org.apache.bahir" %% "spark-streaming-twitter" % "2.3.2"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp"))
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}