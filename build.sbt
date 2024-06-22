name := "keyspaces-parquet"
version := "0.1"
scalaVersion := "2.12.18"
val sparkVersion = "3.1.2"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.1",
  "com.github.jnr" % "jnr-posix" % "3.1.7",
  "joda-time" % "joda-time" % "2.10.10",
   "org.apache.spark" %% "spark-streaming" % sparkVersion,
   "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
   "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
   "org.apache.hadoop" % "hadoop-aws" % "3.2.1"
)
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.2.1"
libraryDependencies += "org.apache.hadoop" % "hadoop-hdfs" % "3.2.1"
fork in run := true
javaOptions ++= Seq(
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED"
  )






