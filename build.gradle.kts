plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
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
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/IngSisTeam3-2025/printscript")
		authentication {
			create<org.gradle.authentication.http.HttpHeaderAuthentication>("header")
		}g
		credentials(org.gradle.api.credentials.HttpHeaderCredentials::class) {
			name = "Authorization"
			value = "Bearer ${File("/token.txt").readText().trim()}"
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("printscript:printscript-api:1.0.0")
	implementation("printscript:printscript-core:1.0.1")
	implementation("printscript:printscript-lexer:1.0.0")
	implementation("printscript:printscript-parser:1.0.0")
	implementation("printscript:printscript-validator:1.0.2")
	implementation("printscript:printscript-interpreter:1.0.3")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
