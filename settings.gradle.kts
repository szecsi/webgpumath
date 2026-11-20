rootProject.name = "webgpumath"

// The entity-component-system core. Its sources are still in the ecs directory.
include("kog-ecs")
project(":kog-ecs").projectDir = file("ecs")

// What kog applications are built of, on top of webgpumath: render queue, textures, render targets.
include("kog-engine")

// Dear ImGui externals and DSL, on webgpumath vectors.
include("kimgui")
