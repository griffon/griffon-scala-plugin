import org.codehaus.griffon.cli.GriffonScriptRunner as GSR
import org.codehaus.griffon.plugins.GriffonPluginUtils

includeTargets << griffonScript("Init")
includePluginScript("scala", "_ScalaCommon")

testReportsDir = griffonSettings.testReportsDir

target(default: "Run Scala tests") {
    depends(parseArguments)
    def scalaTestSrc = new File("${basedir}/test/scala")
    if(!scalaTestSrc.exists() || !scalaTestSrc.list().size()) {
        ant.echo(message: "[scala] No Scala test sources were found.")
        return
    }
    compileScalaTest()
    ant.mkdir(dir: testReportsDir)

    def pattern = ~/.*Tests\.scala/
    def suites = []
    def findSuites
    findSuites = {
        it.eachFileMatch(pattern) { f ->
            def suite = f.absolutePath - scalaTestSrc.absolutePath - File.separator - ".scala"
            suites << suite.replaceAll(File.separator,".")
        }
        it.eachDir(findSuites)
    } 
    findSuites(scalaTestSrc)

    def scalaTestClassesDir = new File(griffonSettings.testClassesDir, "scala")
    ant.path(id: "scala.run.classpath") {
        path(refid: "scala.test.classpath")
        pathelement(location: "${classesDir.absolutePath}")
        pathelement(location: "${scalaTestClassesDir.absolutePath}")
    }

    // TODO include/exclude tags
    Map scalaTestParams = [:]
    if(buildConfig.scala?.test?.parallel) scalaTestParams.parallel = true
    if(buildConfig.scala?.test?.numthreads) scalaTestParams.numthreads = buildConfig.scala.test.numthreads
    if(buildConfig.scala?.test?.haltonfailure) scalaTestParams.haltonfailure = true
    if(buildConfig.scala?.test?.fork) scalaTestParams.fork = true
    if(buildConfig.scala?.test?.maxmemory) scalaTestParams.maxmemory = buildConfig.scala.test.maxmemory
    if(buildConfig.scala?.test?.membersonly) scalaTestParams.membersonly = buildConfig.scala.test.membersonly
    if(buildConfig.scala?.test?.wildcard) scalaTestParams.wildcard = buildConfig.scala.test.wildcard
    def stdoutConfig = buildConfig.scala?.test?.reporter?.stdout ?: "FAB"
    def scalaConfig = buildConfig.scala?.test?.config ?: [:]

    ant.scalatest(scalaTestParams){
        scalaConfig.each { k, v ->
            config(name: k, value: v)
        }
        runpath {
            ant.project.references["scala.run.classpath"].each { p ->
                pathelement(location: p)
            }
        }
        if(argsMap.suite) {
            suite(classname: argsMap.suite)
        } else {
            suites.each{ classname -> suite(classname: classname) }
        }
        if(buildConfig.scala?.test?.jvmargs) { 
            buildConfig.scala.test.jvmargs.each { jvmargvalue ->
                jvmarg(value: jvmargvalue)
            }
        }
        reporter(type: "xml", directory: testReportsDir)
        reporter(type: "stdout", config: stdoutConfig)
        if(buildConfig.scala?.test?.reporter?.file) { 
            reporter(type: "file", filename: buildConfig.scala.test.reporter.file)
        }
        if(buildConfig.scala?.test?.reporter?.reporterclass) { 
            reporter(type: "reporterclass", classname: buildConfig.scala.test.reporter.reporterclass)
        }
    }
}
