val scalaVersion = "2.13"
val sparkVersion = "4.0.0"
val scalaTestVersion = "3.2.18"
val testcontainersScalaVersion = "0.41.4"

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
    implementation("org.apache.spark:spark-sql-kafka-0-10_$scalaVersion:$sparkVersion") {
        exclude(group = "org.apache.kafka", module = "kafka-clients")
    }
    implementation("org.apache.spark:spark-avro_$scalaVersion:$sparkVersion")
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    testImplementation("org.apache.avro:avro:1.11.3")

    // Testing
    testImplementation("org.scalatest:scalatest_$scalaVersion:$scalaTestVersion")
    testImplementation("org.scalatestplus:junit-5-10_$scalaVersion:3.2.18.0")
    testImplementation("com.dimafeng:testcontainers-scala-scalatest_$scalaVersion:$testcontainersScalaVersion")
    testImplementation("com.dimafeng:testcontainers-scala-kafka_$scalaVersion:$testcontainersScalaVersion")
    testImplementation("org.awaitility:awaitility:4.2.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("analytics.dispatcher.JobDispatcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}