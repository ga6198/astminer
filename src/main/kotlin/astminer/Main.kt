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

    //test astminer cli code
    //outputs to astminer/testOutput
    testAstminerCliCode();

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
    //val pathname = currentWorkingDirectory + "/testData/examples/php/test"
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/1" //admin.categories.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/2" //admin.contact.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/3" //admin.trash.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/4" //banner.php //SAFE, but methods not extracted
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/5" //CalendarCommon.php //UNSAFE
    //val pathname = currentWorkingDirectory + "/testData/examples/php_test/6" //CalendarCommon2013.php
    //val pathname = currentWorkingDirectory + "/testData/examples"//"/src/main/kotlin/astminer/files" //"~/astminer/src/main/kotlin/astminer/files"
    val pathname = currentWorkingDirectory + "/testData/examples" //"~/astminer/testData/examples/"
    val extension = "php"
    //val extension = "py"


    //IMPORTANT: ORIGINAL CODE THAT WAS CAUSING MEMORY OVERFLOW
    /*
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
     */
    val files = parser.parseWithExtensionForFiles(File(pathname), extension)
    for (file in files){
        //add file to list in order to work with parse() function
        val fileList = listOf<File>(File(file))

        //parse a single file for the root node (as a list)
        val roots = parser.parse(fileList)

        //extract the methods for the single file`
        try {
            extractFromMethods(roots, PhpMethodSplitter(), miner, storage)
            //extractFromMethods(roots, PythonMethodSplitter(), miner, storage)
        }
        catch (e: Exception) {
            println("Error occurred w/ extractFromMethods on file ${file}")
            e.printStackTrace()
        }
    }


    println("Finished extracting methods")

    // Save stored data on disk
    storage.save(Long.MAX_VALUE, Long.MAX_VALUE)
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
    //Note: rewrote as for loop to allocate less memory
    for (methodInfo in methods){
        val methodNameNode = methodInfo.method.nameNode ?: continue //if null, go to next iteration
        val methodRoot = methodInfo.method.root
        //This is the part that splits the method names into parts
        val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
        //println("Current label: $label")
        methodRoot.preOrder().forEach { it.setNormalizedToken() }
        methodNameNode.setNormalizedToken("METHOD_NAME")

        // Retrieve paths from every node individually
        val maxPathContexts = 500
        val paths = miner.retrievePaths(methodRoot).take(maxPathContexts)

        println("Num paths: ${paths.size}")

        val contexts = LabeledPathContexts(label, paths.map {
            toPathContext(it) { node ->
                node.getNormalizedToken()
            }
        })

        storage.store(LabeledPathContexts(label, paths.map {
            toPathContext(it) { node ->
                node.getNormalizedToken()
            }
        }))
    }

    /*
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
        //println("Current label: $label")
        methodRoot.preOrder().forEach { it.setNormalizedToken() }
        methodNameNode.setNormalizedToken("METHOD_NAME")

        // Retrieve paths from every node individually
        val maxPathContexts = 500
        val paths = miner.retrievePaths(methodRoot).take(maxPathContexts)

        println("Num paths: ${paths.size}")

        val contexts = LabeledPathContexts(label, paths.map {
            toPathContext(it) { node ->
                node.getNormalizedToken()
            }
        })

        storage.store(LabeledPathContexts(label, paths.map {
            toPathContext(it) { node ->
                node.getNormalizedToken()
            }
        }))
    }

     */
}
