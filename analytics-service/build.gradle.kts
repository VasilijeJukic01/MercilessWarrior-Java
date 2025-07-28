val scalaVersion = "2.13"
val sparkVersion = "4.0.0"

plugins {
    scala
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.12")
    implementation("org.apache.avro:avro:1.11.3")

    // Spark
    implementation("org.apache.spark:spark-core_$scalaVersion:$sparkVersion")
    implementation("org.apache.spark:spark-sql_$scalaVersion:$sparkVersion")
    implementation("org.apache.spark:spark-sql-kafka-0-10_$scalaVersion:$sparkVersion")
    implementation("org.apache.spark:spark-avro_$scalaVersion:$sparkVersion")
}

application {
    mainClass.set("analytics.Main")
}