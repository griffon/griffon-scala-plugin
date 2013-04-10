/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */

includeTargets << griffonScript('_GriffonPackage')
includePluginScript('scala', '_ScalaCommon')

target(scalaRepl: "Run Scala REPL") {
    depends(packageApp)
    
    addUrlIfNotPresent classLoader.parent, projectMainClassesDir
    addUrlIfNotPresent classLoader.parent, resourcesDir

    def scalaClasspath = classLoader.getURLs().collect([]){ it.toString() }
    classLoader.parent.getURLs().collect(scalaClasspath){ it.toString() }

    classLoader.loadClass("scala.tools.nsc.MainGenericRunner").main(["-cp", scalaClasspath.join(File.pathSeparator)] as String[])
}
setDefaultTarget(scalaRepl)
