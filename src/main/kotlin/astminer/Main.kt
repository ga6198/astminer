package astminer

import astminer.examples.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

import astminer.common.getNormalizedToken
import astminer.common.model.*
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.parse.antlr.python.PythonMethodSplitter
import astminer.parse.antlr.python.PythonParser
import astminer.parse.antlr.php.PhpMethodSplitter
import astminer.parse.antlr.php.PhpMainParser
//import astminer.parser.antlr.php.*
import astminer.parse.cpp.FuzzyCppParser
import astminer.parse.cpp.FuzzyMethodSplitter
import astminer.parse.java.GumTreeJavaParser
import astminer.parse.java.GumTreeMethodSplitter
import astminer.paths.Code2VecPathStorage
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import astminer.paths.toPathContext

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

    //test astminer cli code
    //outputs to astminer/testOutput
    testAstminerCliCode();

    /*
    //outputs to astminer/src/main/kotlin/astminer/output
    val trainArray = getDirectories("train")
    runPythonCommand(trainArray)
    val testArray = getDirectories("test")
    runPythonCommand(testArray)
    val valArray = getDirectories("val")
    runPythonCommand(valArray)
     */
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

//---------------------Astminer CLI code
fun testAstminerCliCode(){
    //added code from Code2VecExtractor to help with debugging
    val outputDir = File("testOutput")
    val miner = PathMiner(PathRetrievalSettings(5, 5))

    val outputDirForLanguage = outputDir.resolve("php")
    outputDirForLanguage.mkdir()
    val storage = Code2VecPathStorage(outputDirForLanguage.path, false, 100)

    println("OutputDirForLanguage: ${outputDirForLanguage.path}")

    val parser = PhpMainParser()
    //val parser = PythonParser()
    println("Finished creating parser")
    val currentWorkingDirectory = System.getProperty("user.dir")
    println(currentWorkingDirectory)
    val pathname = currentWorkingDirectory + "/testData/examples/php/test"
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/1" //admin.categories.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/2" //admin.contact.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/3" //admin.trash.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/4" //banner.php //SAFE, but methods not extracted
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/5" //CalendarCommon.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/6" //CalendarCommon2013.php
    //val pathname = currentWorkingDirectory + "/testData/examples"//"/src/main/kotlin/astminer/files" //"~/astminer/src/main/kotlin/astminer/files"
    //val pathname = currentWorkingDirectory + "/testData/examples" //"~/astminer/testData/examples/"
    val extension = "php"
    //val extension = "py"
    val roots = parser.parseWithExtension(File(pathname), extension)
    println("Finished parsing")
    try {
        extractFromMethods(roots, PhpMethodSplitter(), miner, storage)
        //extractFromMethods(roots, PythonMethodSplitter(), miner, storage)
    }
    catch (e: Exception) {
        println("Error occurred w/ extractFromMethods")
        e.printStackTrace()
    }
    println("Finished extracting methods")

    //print("Press enter to continue ")
    //val stringInput = readLine()
    ///----------------
}

//
fun <T : Node> extractFromMethods(
    roots: List<ParseResult<T>>,
    methodSplitter: TreeMethodSplitter<T>,
    miner: PathMiner,
    storage: Code2VecPathStorage
) {
    println("Roots: $roots")

    val methods = roots.mapNotNull {
        println("Current Root FilePath: ${it.filePath}")
        it.root
    }.flatMap {
        methodSplitter.splitIntoMethods(it)
    }
    println("Methods: $methods")
    //Split method names so they are divided by |
    methods.forEach { methodInfo ->
        //added this to check an issue where all method names are null
        if(methodInfo.method.nameNode != null){
            print("Found a method name that is not null: ")
            println(methodInfo.method.nameNode)
        }
        val methodNameNode = methodInfo.method.nameNode ?: return@forEach
        val methodRoot = methodInfo.method.root
        //This is the part that splits the method names into parts
        val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
        println("Current label: $label")
        methodRoot.preOrder().forEach { it.setNormalizedToken() }
        methodNameNode.setNormalizedToken("METHOD_NAME")

        // Retrieve paths from every node individually
        val maxPathContexts = 500
        val paths = miner.retrievePaths(methodRoot).take(maxPathContexts)
        storage.store(LabeledPathContexts(label, paths.map {
            toPathContext(it) { node ->
                node.getNormalizedToken()
            }
        }))
    }
}
