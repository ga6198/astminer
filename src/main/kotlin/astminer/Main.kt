package astminer

import astminer.examples.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

//function to run external process and redirect output
fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}

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

    //need to get the path to the current directory (current working directory)
    /*val workingDir = System.getProperty("user.dir")

    val mainFileDir = workingDir + "/src/main/kotlin/astminer"

    println("PHP files")

    //base dirs
    val filesDir = mainFileDir + "/files/"
    val outDir = mainFileDir + "/output/"

    val trainDir = filesDir + "train"
    val testDir = filesDir + "test"
    val valDir = filesDir + "val"

    val trainOutDir = outDir + "train"
    val testOutDir = outDir + "test"
    val valOutDir = outDir + "val"

    allPhpFiles(trainDir, trainOutDir) //pass in directories
    allPhpFiles(testDir, testOutDir)
    allPhpFiles(valDir, valOutDir)

    val nodeTypesDir = trainOutDir + "/node_types.csv"
    val pathContextsDir = trainOutDir + "/path_contexts_0.csv"
    val pathsDir = trainOutDir + "/paths.csv"
    val tokensDir = trainOutDir + "/tokens.csv"
    val outputFile = trainOutDir + "/phpdata.train.raw.txt" //need to replace this with passed in argument from preprocess.sh

    val command = "python3 extract.py $pathContextsDir $tokensDir $pathsDir $nodeTypesDir $outputFile"
    println("Command: $command")
    val process = Runtime.getRuntime().exec(command)
    process.waitFor()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val message = reader.lines().collect(Collectors.joining("\n"))
    println(message)*/

    /*AllJavaFiles.runExample()

    allJavaAsts()
    */
    val trainArray = getDirectories("train")
    runPythonCommand(trainArray)
    val testArray = getDirectories("test")
    runPythonCommand(testArray)
    val valArray = getDirectories("val")
    runPythonCommand(valArray)
}

//TODO: pass in output file name from code2vec preprocess.sh
fun getDirectories(dataSetType : String): Array<String> {
    //need to get the path to the current directory (current working directory)
    val workingDir = System.getProperty("user.dir")

    val mainFileDir = workingDir + "/src/main/kotlin/astminer"

    println("PHP files")

    //base dirs
    val filesDir = mainFileDir + "/files/"
    val outDir = mainFileDir + "/output/"

    //data set dir. ex trainDir, testDir, valdir; ex. filesDir + "train"
    val dataSetDir = filesDir + dataSetType
    val dataSetOutDir = outDir + dataSetType

    //run parser on the current data set, ex the training set
    allPhpFiles(dataSetDir, dataSetOutDir) //pass in directories

    //output file directories
    val nodeTypesDir = dataSetOutDir + "/node_types.csv"
    val pathContextsDir = dataSetOutDir + "/path_contexts_0.csv"
    val pathsDir = dataSetOutDir + "/paths.csv"
    val tokensDir = dataSetOutDir + "/tokens.csv"
    val outputFile = dataSetOutDir + "/phpdata." + dataSetType + ".raw.txt" //need to replace this with passed in argument from preprocess.sh

    val outputsArray = arrayOf(nodeTypesDir, pathContextsDir, pathsDir, tokensDir, outputFile)
    return outputsArray
}

//run the script which transforms astminer csv output to code2vec format
fun runPythonCommand(args: Array<String>){
    var command = "python3 extract.py"

    //add arguments onto the command
    for (arg in args){
        command = command + " " + arg
    }

    //run the actual python file
    //val command = "python3 extract.py $pathContextsDir $tokensDir $pathsDir $nodeTypesDir $outputFile"
    println("Command: $command")
    val process = Runtime.getRuntime().exec(command)
    process.waitFor()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val message = reader.lines().collect(Collectors.joining("\n"))
    println(message)

}
