plugins {
	kotlin("jvm") version "1.8.22"
	id("io.freefair.lombok") version "5.3.3.3"
	id("org.openjfx.javafxplugin") version "0.0.14"
	application
}

group = "com.games.mw"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.1"
val javafxVersion = "17"

java {
	sourceCompatibility = JavaVersion.toVersion("17")
	targetCompatibility = JavaVersion.toVersion("17")
}

repositories {
	mavenCentral()
}

dependencies {
	// LWJGL Core (Audio)
	implementation("org.lwjgl:lwjgl:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-openal:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")

	// LWJGL Native Libraries for Windows
	implementation("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
	implementation("org.lwjgl:lwjgl-openal:$lwjglVersion:natives-windows")
	implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows")
	implementation("org.lwjgl:lwjgl-stb:$lwjglVersion:natives-windows")

	// Gson
	implementation("com.google.code.gson:gson:2.10")

	// JavaFX
	implementation("org.openjfx:javafx-controls:$javafxVersion")
	implementation("org.openjfx:javafx-graphics:$javafxVersion")
	implementation("org.openjfx:javafx-base:$javafxVersion")

	// Lombok
	compileOnly("org.projectlombok:lombok:1.18.20")
	annotationProcessor("org.projectlombok:lombok:1.18.20")
}

javafx {
	version = javafxVersion
	modules = listOf("javafx.controls", "javafx.graphics", "javafx.base")
}

application {
	mainClass.set("com.games.mw.core")
}

tasks {
	named<JavaExec>("run") {
		systemProperties["java.library.path"] = file("libs/natives").absolutePath
		systemProperties["org.lwjgl.util.Debug"] = "true"
		systemProperties["org.lwjgl.util.DebugLoader"] = "true"
	}
}
