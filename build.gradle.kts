/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.yarn.task.YarnTask
import com.saagie.technologies.SaagieTechnologiesPackageGradlePlugin
import com.saagie.technologies.TYPE
import com.saagie.technologies.modifiedProjects
import net.thauvin.erik.gradle.semver.SemverIncrementBuildMetaTask

plugins {
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("com.bmuschko.docker-remote-api") version "6.1.1"
    id("com.github.node-gradle.node") version "3.2.1"
    id("org.kordamp.gradle.project") version "0.46.0"
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath("com.saagie:technologiesplugin:1.3.14")
    }
}
apply<SaagieTechnologiesPackageGradlePlugin>()

config {
    info {
        name = "Technologies"
        description = "All technologies for Saagie"
        inceptionYear = "2019"
        vendor = "Saagie"

        scm {
            url = "https://github.com/saagie/technologies"
        }

        links {
            website = "https://www.saagie.com"
            scm = "https://github.com/saagie/technologies"
        }

        licensing {
            licenses {
                license {
                    id = "Apache-2.0"
                }
            }
        }

        organization {
            name = "Saagie"
            url = "http://www.saagie.com"
        }
    }
}

configure(subprojects) {
    apply<NodePlugin>()

    configure<NodeExtension> {
        download.set(true)
        version.set("16.14.2")
        npmVersion.set("8.5.0")
        yarnVersion.set("1.22.18")
    }

    tasks {
        val yarn_install by getting {
            (this as YarnTask).args.addAll("--cache-folder", File(File(project.projectDir, ".cache"), "yarn").toString())
        }
    }
}

tasks.register("buildModifiedJobs") {
    group = "technologies"
    description = "Build only modified jobs"

    val modifiedProjects = modifiedProjects(TYPE.CONNECTION_TYPE, subprojects) + modifiedProjects(TYPE.JOB, subprojects)

    logger.info(this.description)
    dependsOn("incrementBuildMeta")
    logger.debug("$modifiedProjects")
    modifiedProjects.forEach {
        dependsOn("${it.path}:buildTechnology")
    }
    finalizedBy(":packageAllVersions")
}

tasks.register("buildModifiedApps") {
    group = "technologies"
    description = "Build only modified apps"

    val modifiedProjects = modifiedProjects(TYPE.APP, subprojects)

    logger.info(this.description)
    dependsOn("incrementBuildMeta")
    logger.debug("$modifiedProjects")
    modifiedProjects.forEach {
        dependsOn("${it.path}:buildTechnology")
    }
    finalizedBy(":packageAllVersions")
}

tasks.register("localBuildModifiedJobs") {
    group = "technologies"
    description = "Local build only modified jobs without push"

    val modifiedProjects = modifiedProjects(TYPE.CONNECTION_TYPE, subprojects) + modifiedProjects(TYPE.JOB, subprojects)

    logger.info(this.description)
    dependsOn("incrementBuildMeta")
    logger.debug("$modifiedProjects")
    modifiedProjects.forEach {
        dependsOn("${it.path}:testTechnology")
    }
}

tasks.register("localBuildModifiedApps") {
    group = "technologies"
    description = "Local build only modified apps without push"

    val modifiedProjects = modifiedProjects(TYPE.APP, subprojects)

    logger.info(this.description)
    dependsOn("incrementBuildMeta")
    logger.debug("$modifiedProjects")
    modifiedProjects.forEach {
        dependsOn("${it.path}:testTechnology")
    }
}

tasks.register("buildAllTechnologies") {
    group = "technologies"
    description = "Build all technologies"

    logger.info(this.description)
    dependsOn("incrementBuildMeta")
    subprojects.forEach {
        dependsOn("${it.path}:buildTechnology")
    }
    finalizedBy(":packageAllVersions")
}

tasks {
    "incrementBuildMeta"(SemverIncrementBuildMetaTask::class) {
        doFirst {
            if (buildMeta == "master") {
                buildMeta = ""
            }
            this.project.version = version as String
        }

        doLast {
            with(File("version.properties")) {
                val version = File("versions")
                this.readLines()
                        .drop(2)
                        .forEach { version.appendText(it + "\n") }
                this.delete()
                version.renameTo(this)
            }
        }
    }
}
