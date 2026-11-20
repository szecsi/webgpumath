import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
  kotlin("multiplatform") version "2.4.20"
  kotlin("plugin.serialization") version "2.4.20"
  `maven-publish`
}

group = "vision.gears"
version = "0.3.0"

repositories {
  mavenCentral()
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
  rootProject.the<YarnRootExtension>().yarnLockMismatchReportProperty.set(YarnLockMismatchReport.WARNING)
  rootProject.the<YarnRootExtension>().reportNewYarnLockProperty.set(false)
  rootProject.the<YarnRootExtension>().yarnLockAutoReplaceProperty.set(false)
}

kotlin {
  js {
    browser()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
        implementation(npm("wgsl_reflect", "1.6.0"))
      }
    }
  }
}

// `gradlew publish` writes the Maven repository that GitHub Pages serves from docs/.
publishing {
  repositories {
    maven {
      name = "pages"
      url = uri(layout.projectDirectory.dir("docs/maven"))
    }
  }
}
