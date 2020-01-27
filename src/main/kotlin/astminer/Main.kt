package astminer

import astminer.examples.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

// TODO: remove main as we have CLI and example there
fun main(args: Array<String>) {
    runExamples()
}

fun runExamples() {
    /*code2vecJavaMethods()
    allJavaFilesGumTree()
    allJavaFiles()
    allJavaMethods()
    allPythonFiles()
    allJavaScriptFiles()*/
    //allCppFiles()

    println("PHP files")

    val trainDir = "./files/train"
    val testDir = "./files/test"
    val valDir = "./files/val"

    val trainOutDir = "./output/train"
    val testOutDir = "./output/test"
    val valOutDir = "./output/val"

    allPhpFiles(trainDir, trainOutDir) //pass in directories
    allPhpFiles(testDir, testOutDir)
    allPhpFiles(valDir, valOutDir)

    val nodeTypesDir = trainOutDir + "node_types.csv"
    val pathContextsDir = trainOutDir + "path_contexts_0.csv"
    val pathsDir = trainOutDir + "paths.csv"
    val tokensDir = trainOutDir + "tokens.csv"
    val outputFile = trainOutDir + "phpdata.train.raw.txt" //need to replace this with passed in argument from preprocess.sh

    val command = "python3 extract.py $pathContextsDir $tokensDir $pathsDir $nodeTypesDir $outputFile"
    println("Command: $command")
    val process = Runtime.getRuntime().exec(command)
    process.waitFor()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val message = reader.lines().collect(Collectors.joining("\n"))
    println(message)

    /*AllJavaFiles.runExample()

    allJavaAsts()
    */
	

}
