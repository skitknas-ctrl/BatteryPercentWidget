import java.time.LocalDate

plugins {
    id("com.android.application")
}

// Byggdatum som visas på informationssidan
val buildDate: String = LocalDate.now().toString()

android {
    namespace = "se.dick.batterywidget"
    compileSdk = 36
    defaultConfig {
        applicationId = "se.dick.batterywidget"
        minSdk = 31
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
    }
    buildFeatures {
        buildConfig = true
    }
}
