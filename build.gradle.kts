import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import nu.studer.gradle.jooq.JooqEdition
import nu.studer.gradle.jooq.JooqGenerate


plugins {
    id("nu.studer.jooq") version("10.1.1")
    id("org.flywaydb.flyway") version("11.12.0")
    id("org.springframework.boot") version("3.5.5")
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
}


val postgresVersion = "16.10"
val telegramBotVersion = "6.9.7.1"

group = "ru.template.telegram.bot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.clean {
    delete("src/main/java")
}

val flywayMigration = configurations.create("flywayMigration")

flyway {
    validateOnMigrate = false
    configurations = arrayOf("flywayMigration")
    url = "jdbc:postgresql://5.188.140.80:5432/telebot_kazino_db"
    user = "telebot_kazino"
    password = "xS+(S;g8C8J46:O"
}

dependencies {
    //flywayMigration("org.postgresql:postgresql:$postgresVersion")
    //jooqGenerator("org.postgresql:postgresql:$postgresVersion")
    runtimeOnly("org.postgresql:postgresql")

    //Классические стартеры spring boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.telegram:telegrambots:$telegramBotVersion")
    implementation("org.telegram:telegrambotsextensions:$telegramBotVersion")
    implementation("org.telegram:telegrambots-spring-boot-starter:$telegramBotVersion")

    // зависимости, которые помогут сгенерить метаданные
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

// Настройка для JOOQ, в которой описано правило формирования POJO классов для формирования запросов при помощи DSL кода
jooq {
    edition.set(JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = flyway.url
                    user = flyway.user
                    password = flyway.password
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = false
                        isFluentSetters = false
                        isJavaBeansGettersAndSetters = false
                        isSerializablePojos = true
                        isVarargSetters = false
                        isPojos = true
                        isNonnullAnnotation = true
                        isUdts = false
                        isRoutines = false
                        isIndexes = false
                        isRelations = true
                        isPojosEqualsAndHashCode = true
                    }
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "flyway_schema_history"
                    }
                    target.apply {
                        // Пакет куда отрпавляются сгенерированные классы
                        packageName = "ru.template.telegram.bot.kotlin.template.domain"
                        directory = "src/main/java"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }

    // таска для генерации JOOQ классов
    tasks.named<JooqGenerate>("generateJooq").configure {
        inputs.files(fileTree("src/main/resources/db/migration"))
            .withPropertyName("migrations")
            .withPathSensitivity(PathSensitivity.RELATIVE)
        allInputsDeclared.set(true)
        outputs.upToDateWhen { false }
    }
}
kotlin {
    jvmToolchain(24)
}