import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("net.minecrell.licenser") version "0.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.2.1"
    id("com.github.johnrengelman.shadow") version "2.0.2"
}

group = "eu.mikroskeem.debug"
version = "0.0.4-SNAPSHOT"

val gradleWrapperVersion = "4.6"

val bstatsVersion = "1.2"
val shurikenVersion = "0.0.1-SNAPSHOT"
val picomavenVersion = "0.0.2-SNAPSHOT"

val paperApiVersion = "1.12.2-R0.1-SNAPSHOT"
val providersLibVersion = "0.0.5-SNAPSHOT"
val groovyVersion = "2.4.14"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.destroystokyo.com/repository/maven-public/")
    maven("https://repo.bstats.org/content/groups/public/")
    maven("https://repo.wut.ee/repository/mikroskeem-repo/")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:$bstatsVersion")
    implementation("eu.mikroskeem:shuriken.common:$shurikenVersion")
    implementation("eu.mikroskeem:picomaven:$picomavenVersion")

    compileOnly("com.destroystokyo.paper:paper-api:$paperApiVersion")
    compileOnly("eu.mikroskeem.providerslib:api:$providersLibVersion")
    compileOnly("org.codehaus.groovy:groovy-jsr223:$groovyVersion")
}

license {
    header = rootProject.file("etc/HEADER")
    filter.include("**/*.java")
}

bukkit {
    name = "BukkitGroovy"
    description = "Evaluate Groovy script with a command"
    main = "eu.mikroskeem.debug.bukkitgroovy.Main"
    authors = listOf("mikroskeem")
    website = "https://mikroskeem.eu"
    softDepend = listOf("ProvidersLib")

    commands {
        "groovyscript" {
            description = "§8§l[§a§lBG§8§l]§7 Evaluate Groovy script"
            permission = "bukkitgroovy.use"
            usage = "§8/§a<command> §8[§acode§8/§aurl§8]"
            aliases = listOf("groovy", "gr")
        }
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    val targetPackage = "eu.mikroskeem.debug.bukkitgroovy.lib"
    val relocations = listOf(
            "org.bstats",
            "eu.mikroskeem.shuriken",
            "eu.mikroskeem.picomaven",
            "org.apache.maven",
            "org.codehaus.plexus",
            "okhttp3",
            "okio"
    )

    relocations.forEach {
        relocate(it, "$targetPackage.$it")
    }

    exclude("META-INF/maven/**")
}

val wrapper by tasks.getting(Wrapper::class) {
    gradleVersion = gradleWrapperVersion
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
}

tasks["build"].dependsOn("licenseFormat", shadowJar)
