package astminer.paths

import astminer.common.storage.writeLinesToFile
import scala.concurrent.Await.result
import java.io.File


class Code2SeqPathStorage(
        outputFolderPath: String,
        batchMode: Boolean = true,
        fragmentsPerBatch: Long = DEFAULT_FRAGMENTS_PER_BATCH
) : CountingPathStorage<String>(outputFolderPath, batchMode, fragmentsPerBatch) {
    fun isCamelCase(s: String): Boolean{
        if (s.contains("_")){
            return false
        }

        val uppercase = s.map { it.toUpperCase() }.joinToString()
        // if the string is all uppercase
        if (s == uppercase){
            return false
        }

        val lowercase = s.map{it.toLowerCase()}.joinToString()
        // if the string is all lowercase
        if (s == lowercase){
            return false
        }

        // if the string has both uppercase and lowercase
        return true
    }

    fun encodeTokens(token: String): String{
        var splitTokens = listOf<String>()
        if (token.contains("_")){
            splitTokens = token.split("_")
        }
        else if(isCamelCase(token)){
            val subtokens = mutableListOf<String>()
            //split when a capital letter comes
            for (subtoken in token.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
                subtokens.add(subtoken)
            }
            splitTokens = subtokens
        }
        else{
            splitTokens = listOf<String>(token)
        }

        val separatedTokens = splitTokens.joinToString(separator = "|")
        return separatedTokens
    }

    fun retrieveNodeTypes(path: List<Long>): String{
        val nodes = mutableListOf<String>()
        for (nodeId in path){
            val node = orientedNodeTypesMap.lookUpValue(nodeId)
            //println(node)
            if (node != null) {
                nodes.add(node.typeLabel)
            }
        }
        val separatedNodes = nodes.joinToString(separator = "|")
        return separatedNodes
    }

    //need to convert ids back to tokens and separate tokens with |
    override fun dumpPathContexts(file: File, tokensLimit: Long, pathsLimit: Long) {
        val lines = mutableListOf<String>()
        labeledPathContextIdsList.forEach { labeledPathContextIds ->
            val pathContextIdsString = labeledPathContextIds.pathContexts.filter {
                tokensMap.getIdRank(it.startTokenId) <= tokensLimit &&
                        tokensMap.getIdRank(it.endTokenId) <= tokensLimit &&
                        pathsMap.getIdRank(it.pathId) <= pathsLimit
            }
            for (pathContextId in pathContextIdsString){
                val startToken = tokensMap.lookUpValue(pathContextId.startTokenId)
                val separatedStartToken = startToken?.let { encodeTokens(it) }

                val path = pathsMap.lookUpValue(pathContextId.pathId)
                //println(path)
                val separatedPath = path?.let { retrieveNodeTypes(it) }

                //println(separatedStartToken)
                val endToken = tokensMap.lookUpValue(pathContextId.endTokenId)
                val separatedEndToken = endToken?.let { encodeTokens(it) }

                lines.add("${labeledPathContextIds.label} $separatedStartToken,$separatedPath,$separatedEndToken")
            }
                    /*.joinToString(separator = " ") { pathContextId ->
                "${pathContextId.startTokenId},${pathContextId.pathId},${pathContextId.endTokenId}"
            }*/
            //lines.add("${labeledPathContextIds.label} $pathContextIdsString")
        }

        writeLinesToFile(lines, file)
    }

}
