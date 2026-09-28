plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* [SC] DO NOT EDIT */

stonecutter.current?.let { active ->
    tasks.register("buildActive") {
        group = "project"
        dependsOn(stonecutter.tasks.named("build") { metadata.project == active.project })
    }
}