import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import java.lang.ProcessBuilder;

val minecraftVersion = "1.19"
var pluginVersion = "1.0.0"

plugins {
    `java-library`
    //id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.papermc.paperweight.userdev") version "1.3.6"
    id("xyz.jpenilla.run-paper") version "1.0.6" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
}

group = "org.crypticplank.smp"
version = "$pluginVersion-SNAPSHOT-$minecraftVersion"
description = "Plugin for the our SMP server"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    maven("https://repo.dmulloy2.net/repository/public/")
    mavenCentral()
}

dependencies {
    paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:$minecraftVersion-R0.1-SNAPSHOT")
    implementation("com.comphenix.protocol:ProtocolLib:4.7.0")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    //implementation("com.discord4j:discord4j-core:3.2.2");
}

tasks.create("updateSubmodule") {
    val command = "git submodule foreach git pull"
    doLast {
        val process = ProcessBuilder()
            .command(command.split(" "))
            .directory(rootProject.projectDir)
            .start()
            .waitFor(60, TimeUnit.SECONDS)
    }
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }
//    build {
//        dependsOn(shadowJar)
//    }
    compileJava {
        dependsOn("updateSubmodule")
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
//    shadowJar {
//        // archiveBaseName.set("shadow")
//        mergeServiceFiles()
//        dependencies {
//            exclude(dependency("com.comphenix.protocol:ProtocolLib:4.7.0"))
//        }
//    }
}

// Configure plugin.yml generation
bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    name = "Smp"
    main = "org.crypticplank.smp.Smp"
    apiVersion = minecraftVersion
    // depend = listOf("ProtocolLib")
    authors = listOf("crypticplank")
    commands {
        register("reload") {
            description = "Reloads the smp config"
            aliases = listOf("smpreload")
            usage = "/<command>"
        }
        register("twitchchecklink") {
            description = "Adds yourself or others to twitch"
            aliases = listOf("twitchchecklink", "link")
            usage = "/<command>"
        }
        register("restart") {
            description = "Restart smp"
            aliases = listOf("smprestart")
            usage = "/<command>"
        }
        register("donate") {
            description = "Donate to the SMP developer"
            usage = "/<command>"
        }
        register("guest") {
            description = "Adds a guest to the server"
            usage = "/<command>"
        }
        register("update") {
            description = "Updates the SMP plugin"
            usage = "/<command>"
        }
    }
}