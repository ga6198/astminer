package astminer.common.model

import java.io.File
import java.io.InputStream


interface Node {
    fun getTypeLabel(): String
    fun getChildren(): List<Node>
    fun getParent(): Node?
    fun getToken(): String
    fun isLeaf(): Boolean

    fun getMetadata(key: String): Any?
    fun setMetadata(key: String, value: Any)

    fun prettyPrint(indent: Int = 0, indentSymbol: String = "--") {
        repeat(indent) { print(indentSymbol) }
        print(getTypeLabel())
        if (getToken().isNotEmpty()) {
            println(" : ${getToken()}")
        } else {
            println()
        }
        getChildren().forEach { it.prettyPrint(indent + 1, indentSymbol) }
    }

    //Get the list of children nodes (List<Node>). Then only choose ones with matching type label string
    fun getChildrenOfType(typeLabel: String) = getChildren().filter { it.getTypeLabel() == typeLabel }

    //Get the first value of a List<Node> or returns null
    fun getChildOfType(typeLabel: String) = getChildrenOfType(typeLabel).firstOrNull()

    fun removeChildrenOfType(typeLabel: String)
}

interface Parser<T : Node> {
    /**
     * Parse input stream into an AST.
     * @param content input stream to parse
     * @return root of the AST
     */
    fun parse(content: InputStream): T?

    /**
     * Parse list of files.
     * @param files files to parse
     * @return list of AST roots, one for each parsed file
     */
    fun parse(files: List<File>): MutableList<ParseResult<T>> =
        files.map { ParseResult(parse(it.inputStream()), it.path) }.toMutableList()

    /**
     * Parse all files that pass [filter][filter] in [root folder][projectRoot] and its sub-folders.
     * @param projectRoot root folder containing files to parse
     * @param filter lambda expression that determines which files should be parsed
     * @return list of AST roots, one for each parsed file
     */
    fun parseProject(projectRoot: File, filter: (File) -> Boolean): List<ParseResult<T>> {
        //projectRoot.walkTopDown().forEach{
        projectRoot.walk().forEach{
            println(it)
        }


        val files = projectRoot.walkTopDown().filter(filter).toList()
        return parse(files)
    }

    /**
     * Parse all files that pass [filter][filter] in [root folder][projectRoot] and its sub-folders for FILES.
     * @param projectRoot root folder containing files to parse
     * @param filter lambda expression that determines which files should be parsed
     * @return list of files string
     */
    fun parseProjectForFiles(projectRoot: File, filter: (File) -> Boolean): List<String> {
        //projectRoot.walkTopDown().forEach{
        projectRoot.walk().forEach{
            println(it)
        }

        val files = projectRoot.walkTopDown().filter(filter).toList()
        val fileNames = files.map{file -> file.path}
        return fileNames
    }

    /**
     * Parse all files with given extension in [root folder][projectRoot] and its sub-folders.
     * @param projectRoot root folder containing files to parse
     * @param extension extension of files that should be parsed
     * @return list of AST roots, one for each parsed file
     */
    fun parseWithExtension(projectRoot: File, extension: String) = parseProject(projectRoot) { it.isFile && it.extension == extension }

    /**
     * Parse all files with given extension in [root folder][projectRoot] and its sub-folders as FILES
     * @param projectRoot root folder containing files to parse
     * @param extension extension of files that should be parsed
     * @return list of files strings
     */
    fun parseWithExtensionForFiles(projectRoot: File, extension: String) = parseProjectForFiles(projectRoot) { it.isFile && it.extension == extension }
}

data class ParseResult<T : Node>(val root: T?, val filePath: String)
