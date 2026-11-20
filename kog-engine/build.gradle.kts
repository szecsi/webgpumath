// What kog applications are built of, on top of webgpumath: the render queue, shaders and
// textures loaded from files, render targets. webgpumath binds data to shaders; this
// decides what is drawn, in what order, and into what.

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
        // its types (Pass, UniformProvider, Texture...) are in the signatures of this module
        api(project(":"))
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
