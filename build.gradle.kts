buildscript {
    val API_SECRET by extra("G-vsuWxFZV-uMzVGtIsYgyTnHU8")
    val API_KEY by extra(294917159268946)
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}