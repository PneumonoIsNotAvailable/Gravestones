plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter.tasks {
    order("publishModrinth")
}

stonecutter active "26.1"