plugins {
	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.jetbrains.kotlinx.kover") version "0.6.1"
	id("io.gitlab.arturbosch.detekt") version "1.23.6"
	id("com.diffplug.spotless") version "6.25.0"
}

group = "com.example.snippetsearcher"
version = "0.0.1-SNAPSHOT"
description = "Execution service for Snippet Searcher platform"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenLocal()
	mavenCentral()
	maven {
		name = "Printscript"
		url = uri("https://maven.pkg.github.com/IngSisTeam3-2025/printscript")
		credentials {
			username = System.getenv("GPR_USER")
			password = System.getenv("GPR_KEY")
		}
	}
	maven {
		name = "RedisStreamsMvc"
		url = uri("https://maven.pkg.github.com/austral-ingsis/redis-streams-mvc")
		credentials {
			username = System.getenv("GPR_USER")
			password = System.getenv("GPR_KEY")
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("printscript:printscript-api:1.0.0")
	implementation("printscript:printscript-core:1.0.1")
	implementation("printscript:printscript-lexer:1.0.0")
	implementation("printscript:printscript-parser:1.0.0")
	implementation("printscript:printscript-validator:1.0.2")
	implementation("printscript:printscript-interpreter:1.0.3")

	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.austral.ingsis:redis-streams-mvc:0.1.13")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("com.h2database:h2")
	testImplementation("io.mockk:mockk:1.13.12")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

detekt {
	buildUponDefaultConfig = true
	config.setFrom(files("$rootDir/detekt/detekt.yml"))
	allRules = false
	autoCorrect = false
}


spotless {
	kotlin {
		target("**/*.kt")
		targetExclude("build/**/*.kt")
		ktlint().editorConfigOverride(
			mapOf(
				"indent_size" to "4",
				"insert_final_newline" to "true"
			)
		)
		trimTrailingWhitespace()
	}
}

kover {
	htmlReport {
		onCheck.set(true)
	}
	verify {
		rule {
			bound {
				minValue = 56
			}
		}
	}
}

tasks.test {
	useJUnitPlatform()
}

tasks.spotlessCheck {
	mustRunAfter(tasks.clean)
}

tasks.detekt {
	mustRunAfter(tasks.spotlessCheck)
}

tasks.test {
	mustRunAfter(tasks.detekt)
}

tasks.named("koverVerify") {
	mustRunAfter(tasks.test)
}

tasks.check {
	dependsOn(tasks.spotlessCheck)
	dependsOn(tasks.detekt)
	dependsOn(tasks.test)
	dependsOn(tasks.named("koverVerify"))
}

tasks.named("build") {
	dependsOn(tasks.check)
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
	jvmTarget = "1.8"
	classpath = files()
	include("**/*.kt")
	exclude("**/*.class")
	setSource(files("src/main/kotlin", "src/test/kotlin"))
}
