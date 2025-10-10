plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter.tasks {
    order("publishModrinth")
}

stonecutter active "1.21.9"