

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("app.cash.sqldelight") version "2.0.1"

}

kotlin {

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation("io.ktor:ktor-client-core:2.3.4")
            implementation("io.ktor:ktor-client-cio:2.3.4") // Or android engine
            implementation("io.ktor:ktor-client-logging:2.3.4")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
            implementation("app.cash.sqldelight:runtime:2.0.1")

            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0") // or latest
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
            implementation("app.cash.turbine:turbine:0.12.1")
            implementation("org.xerial:sqlite-jdbc:3.36.0.3")
            implementation("junit:junit:4.13.2")
            implementation("io.ktor:ktor-client-mock:2.3.5") // use same version as your ktor-client

        }
        androidMain.dependencies {
            implementation("app.cash.sqldelight:android-driver:2.0.1")
            implementation("androidx.datastore:datastore-preferences:1.0.0")
        }

    }
}


android {
    namespace = "dev.drivemode.techtest"
    compileSdk = 34
    defaultConfig {
        minSdk = 31
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("database")
        }
    }
}


// --- Added by tests setup ---
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-mock:2.3.12")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
        }
    }
}
// --- End tests setup ---
