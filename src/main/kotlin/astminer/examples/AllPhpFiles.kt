package astminer.examples

import astminer.common.model.LabeledPathContexts
import astminer.parse.antlr.php.PhpMainParser
import astminer.paths.CsvPathStorage
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import astminer.paths.toPathContext
import java.io.File

fun allPhpFiles(folderPath: String = "./testData/examples", outputPath: String = "out_examples/allPhpFilesAntlr") {
    val folder = folderPath
    val outputDir = outputPath

    //I think this will let us go down a max of 5 paths
    val miner = PathMiner(PathRetrievalSettings(5, 5))
    val storage = CsvPathStorage(outputDir)

    File(folder).forFilesWithSuffix(".php") {file ->
        val node = PhpMainParser().parse(file.inputStream()) ?: return@forFilesWithSuffix
        val paths = miner.retrievePaths(node)

        storage.store(LabeledPathContexts(file.path, paths.map { toPathContext(it) }))
    }

    storage.save()
}