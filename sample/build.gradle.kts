/**
 * =======================================================================
 * = Description: openCRX/Sample
 * = Name: build.gradle.kts
 * = Copyright:   (c) 2020 CRIXP AG
 * =======================================================================
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2020, CRIXP Corp., Switzerland
 * All rights reserved.
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
 * * Neither the name of CRIXP Corp. nor the names of the contributors
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

plugins {
	java
	`java-library`
	eclipse
	war
	distribution
}
apply(plugin = "ear")
apply(plugin = "opencrx")

repositories {
	mavenCentral()
	jcenter()
    maven {
        url = uri("https://www.openmdx.org/repos/releases")
    }
    maven {
        url = uri("https://www.opencrx.org/repos/releases")
    }
}

group = "org.opencrx.sample"
version = "4.3.0"

eclipse {
	project {
    	name = "openCRX ~ Sample (jre-" + JavaVersion.current() + ")"
    }
}

fun getProjectImplementationVersion(): String {
	return project.getVersion().toString();
}

val opencrxVersion = "4.3.0"

val earlib by configurations
val opencrxCoreConfig by configurations
val opencrxCoreModels by configurations

dependencies {
	opencrxCoreConfig("org.opencrx:opencrx-core-config:$opencrxVersion")
    opencrxCoreModels("org.opencrx:opencrx-core-models:$opencrxVersion")
	implementation("org.opencrx:opencrx-core:$opencrxVersion")
	earlib("org.opencrx:opencrx-core:$opencrxVersion")
	earlib(fileTree("../jre-" + JavaVersion.current() + "/" + project.getName() + "/lib") { include("*.jar") })
	// test
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testRuntimeOnly(earlib)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter:5.6.0")
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir("src/data/org.opencrx.sample/WEB-INF/classes")
            srcDir("$buildDir/generated/sources/java/main")
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

tasks.register<Copy>("prepare-wizards") {
	from(zipTree(configurations.getByName("opencrxCoreConfig").singleFile)) {
		include("org.opencrx/WEB-INF/classes/**/*.java");
		eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(3).toTypedArray()) }
	}
	into("$buildDir/generated/sources/java/main")
	includeEmptyDirs = false	
}

tasks.withType<JavaCompile> {
	dependsOn("generate-model","prepare-wizards")
}

tasks.register<org.opencrx.gradle.ArchiveTask>("opencrx-sample.jar") {
	destinationDirectory.set(File(deliverDir, "lib"))
	archiveFileName.set("opencrx-sample.jar")
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
		File(buildDir, "classes/java/main"),
		File(buildDir, "src/main/resources"),
		zipTree(File(buildDir, "generated/sources/model/opencrx-sample.openmdx-xmi.zip")),
		"src/main/resources"
	)
	include(
		"org/opencrx/sample/**",
        "META-INF/"
	)
}

tasks.register("deliverables") {
	dependsOn("opencrx-sample.jar")
}

distributions {
    main {
    	distributionBaseName.set("opencrx-" + getProjectImplementationVersion() + "-sample-jre-" + JavaVersion.current())
        contents {
        	// .
        	from(".") { into("opencrx"); include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts") }
            from("src") { into("opencrx/src") }
            // etc
            from("etc") { into("opencrx/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts" ) }
            // jre-1.8 
            from("../jre-" + JavaVersion.current() + "/opencrx/lib") { into("jre-" + JavaVersion.current() + "/opencrx/lib") }
        }
    }
}
