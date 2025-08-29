tasks.withType<Test> {
    useJUnitPlatform {
        includeTags("unit")
        excludeTags("integration")
    }
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val integrationTest by tasks.creating(Test::class) {
    group = "verification"
    description = "Runs integration tests."
    useJUnitPlatform {
        includeTags("integration")
        excludeTags("unit")
    }
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "skipped", "failed")
    }
    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(integrationTest)
}