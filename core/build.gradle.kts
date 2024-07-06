plugins {
	kotlin("jvm") version "1.8.22"
}

group = "com.games.mw"
version = "1.0-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.toVersion("10")
	targetCompatibility = JavaVersion.toVersion("10")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
	implementation("org.lwjgl.lwjgl:lwjgl:2.9.3")
	implementation("net.java.jutils:jutils:1.0.0")
	implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.3")
	implementation("mysql:mysql-connector-java:8.0.29")
	implementation("com.google.code.gson:gson:2.10")
	implementation("org.mindrot:jbcrypt:0.4")
}
