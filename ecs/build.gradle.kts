// The entity-component-system core: World, Component and queries.
// It knows nothing of graphics, so a game server can use it as well as a client.

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
    nodejs()
  }
  // The sources are in commonMain: they are not specific to JavaScript.
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
