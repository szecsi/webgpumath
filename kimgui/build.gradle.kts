// Dear ImGui for Kotlin: externals of the jsimgui WebAssembly build, generated flags,
// and a DSL with blocks for Begin/End pairs. The DSL edits webgpumath vectors directly.

plugins {
  kotlin("multiplatform")
  `maven-publish`
}

group = rootProject.group
version = rootProject.version

repositories {
  mavenCentral()
}

kotlin {
  js {
    browser()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        api(project(":"))
        // the WebAssembly build of Dear ImGui, with a WebGPU backend
        api(npm("@mori2003/jsimgui", "0.14.0"))
      }
    }
  }
}

// `gradlew publish` writes into the same Maven repository as webgpumath.
publishing {
  repositories {
    maven {
      name = "pages"
      url = uri(rootProject.layout.projectDirectory.dir("docs/maven"))
    }
  }
}
