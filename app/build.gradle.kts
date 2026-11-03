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
        // CI skickar -PbuildNumber=<körningsnummer>; versionen blir då 1.1.<n>
        val baseVersion = "1.2"
        val buildNumber = (project.findProperty("buildNumber") as String?)?.toIntOrNull()
        versionCode = if (buildNumber != null) 100 + buildNumber else 3
        versionName = if (buildNumber != null) "$baseVersion.$buildNumber" else baseVersion
        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
    }
    signingConfigs {
        // Nyckel som ligger i repot så att varje CI-bygge kan uppdatera det förra (Obtainium).
        // Byt till en hemlig nyckel om appen ska publiceras brett.
        create("ci") {
            storeFile = file("ci.keystore")
            storePassword = "android"
            keyAlias = "batterywidget"
            keyPassword = "android"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("ci")
        }
    }
    buildFeatures {
        buildConfig = true
    }
}
