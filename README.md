# qcasm-plugin-gradle

A android asm plugin for fully buried points

# Usage
plugins block:
```Java
plugins {
    id("io.github.bingoliallen.qcasm-plugin") version "1.0.0"
}
```
Build script snippet for use in older Gradle versions or where dynamic configuration is required:

buildscript block:
```Java
apply plugin: "io.github.bingoliallen.qcasm-plugin"

buildscript {

  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }

  dependencies {
    classpath "io.github.bingoliallen.qcasm-plugin-gradle:plugin:1.0.0"
  }

}

```

# Configuration
The following configuration block is required.

Add in gradle.properties
```Java

task cleanCustomTransformCache(type: Delete) {
    delete 'build/intermediates/transforms/CustomTransform'
}

// 在打包任务之前执行清除操作
tasks.whenTaskAdded { task ->
    if (task.name.startsWith('assemble') && (task.name.endsWith('Debug') || task.name.startsWith('Release'))) {
        task.dependsOn cleanCustomTransformCache
    }
}

ext {
    QCAsmClassPrefixes = ["com.yourpackage.xxx.xxx"]
}

```

