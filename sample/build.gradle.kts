/**
 * =======================================================================
 * = Description: openCRX/Sample
 * = Name: build.gradle.kts
 * = Copyright: the original authors.
 * =======================================================================
 * This software is published under the BSD license
 * as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of openCRX team nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */

import java.util.*
import java.io.*

plugins {
	java
	`java-library`
	eclipse
}
apply(plugin = "ear")
apply(plugin = "opencrx")

repositories {
	mavenCentral()
    maven {
        url = uri("https://www.openmdx.org/repos/releases")
    }
    maven {
        url = uri("https://www.opencrx.org/repos/releases")
    }
}

group = "org.opencrx.sample"
version = "6.0.2"

var env = Properties()
env.load(FileInputStream(File(project.getRootDir(), "build.properties")))
val targetPlatform = JavaVersion.valueOf(env.getProperty("target.platform"))

eclipse {
	project {
    	name = "openCRX 6 ~ Sample"
    }
}

fun getProjectImplementationVersion(): String {
	return project.getVersion().toString();
}

fun getDeliverDir(): File {
	return layout.buildDirectory.getAsFile().get();
}

val opencrxVersion = "6.0.2"

val earlib by configurations
val testRuntimeOnly by configurations
testRuntimeOnly.extendsFrom(earlib)
val opencrxCoreConfig by configurations
val opencrxCoreModels by configurations

// Store
val sampleStore = configurations.create("sampleStore")
val openmdxVersion = "4.19.6"

dependencies {
	opencrxCoreConfig("org.opencrx:opencrx-core-config:$opencrxVersion")
    opencrxCoreModels("org.opencrx:opencrx-core-models:$opencrxVersion")
	implementation("org.opencrx:opencrx-core:$opencrxVersion")
	earlib("org.opencrx:opencrx-core:$opencrxVersion")
	earlib(fileTree(File(getDeliverDir(), "lib")) { include("*.jar"); exclude("opencrx-client.jar", "opencrx-core-config.jar", "opencrx-core.jar", "*-sources.jar" ) } )
	// test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter:5.10.0")
    // store
    sampleStore("org.opencrx:opencrx-client:$opencrxVersion")
    sampleStore("org.openmdx:openmdx-base:$openmdxVersion")
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir("src/data/org.opencrx.sample/WEB-INF/classes")
            srcDir(layout.buildDirectory.dir("generated/sources/java/main"))
        }
        resources {
        	srcDir("src/main/resources")
        }
    }
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "4G"
}

tasks {
	assemble {
		dependsOn("opencrx.ear")
	}
}

tasks.withType<Copy> {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE    
}

tasks.register<Copy>("prepare-wizards") {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE    
	from(zipTree(configurations.getByName("opencrxCoreConfig").singleFile)) {
		include("org.opencrx/WEB-INF/classes/**/*.java");
		eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(3).toTypedArray()) }
	}
	into(layout.buildDirectory.dir("generated/sources/java/main"))
	includeEmptyDirs = false	
}

tasks.withType<JavaCompile> {
	dependsOn("generate-model","prepare-wizards")
}

tasks.register<org.opencrx.gradle.ArchiveTask>("opencrx-sample.jar") {
    dependsOn("classes")
	mustRunAfter("opencrx-core.jar","opencrx-client.jar","opencrx-core-config.jar","opencrx-config.jar")
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("opencrx-sample.jar")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    includeEmptyDirs = false		
	manifest {
        attributes(
        	getManifest(
        		"openCRX/Sample Extension Library",
        		"opencrx-sample"
        	)
        )
    }
	from(
		layout.buildDirectory.dir("classes/java/main"),
		layout.buildDirectory.dir("src/main/resources"),
		zipTree(layout.buildDirectory.dir("generated/sources/model/opencrx-sample.openmdx-xmi.zip")),
		"src/main/resources"
	)
	include(
		"org/opencrx/sample/**",
        "META-INF/"
	)
}

tasks.register<org.opencrx.gradle.ArchiveTask>("opencrx-store.war") {
    dependsOn("opencrx-sample.jar")
	destinationDirectory.set(File(getDeliverDir(), "deployment-unit"))
	archiveFileName.set(getWebAppName("store") + ".war")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	includeEmptyDirs = false
	manifest {
	    attributes(
	    	getManifest(
	    		"openCRX/Store WAR",
	    		"opencrx-store.war"
	    	)
	    )
	}
	from(File("src/war/opencrx-sample-store.war")) { include("**/*.*"); exclude("pics/**"); filter { line -> archiveFilter(line) } }
	from(File("src/war/opencrx-sample-store.war")) { include("pics/**"); }
	from(layout.buildDirectory.dir("classes/java/main")) { include("org/opencrx/sample/store/**"); into("WEB-INF/classes") }
    from(sampleStore) { into("WEB-INF/lib"); }
}

tasks.register<org.opencrx.gradle.ArchiveTask>("opencrx-mycontact.war") {
    dependsOn("opencrx-sample.jar")
	mustRunAfter("opencrx-store.war")
	destinationDirectory.set(File(getDeliverDir(), "deployment-unit"))
	archiveFileName.set(getWebAppName("mycontact") + ".war")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	includeEmptyDirs = false
	manifest {
	    attributes(
	    	getManifest(
	    		"openCRX/Mycontact WAR",
	    		"opencrx-mycontact.war"
	    	)
	    )
	}
	from(File("src/war/opencrx-sample-mycontact.war")) { include("**/*.*"); filter { line -> archiveFilter(line) } }
}

tasks.register("deliverables") {
	dependsOn("opencrx-store.war","opencrx-mycontact.war")
}

tasks.named<Ear>("ear") {
    dependsOn("opencrx.ear")
}
