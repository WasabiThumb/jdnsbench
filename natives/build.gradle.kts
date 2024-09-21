import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("dev.welbyseely.gradle-cmake-plugin") version "0.1.0"
}

cmake {
    val os = DefaultNativePlatform.getCurrentOperatingSystem()
    val win = os.isWindows || os.run {
        if (isMacOsX) throw Error("OSX is not supported")
        if (!isLinux) throw Error("OS is not Windows or Linux")
        false
    }

    sourceFolder = File(projectDir, "src")
    generator = if (win) "Visual Studio 15 2017" else "Unix Makefiles"
    if (win) platform = "x64"
    buildSharedLibs = true
    buildConfig = "Release"
    buildTarget = "jdnsbench"
}
