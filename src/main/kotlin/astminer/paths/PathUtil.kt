package astminer.paths

import astminer.common.model.*

fun toPathContext(path: ASTPath, getToken: (Node) -> String = { node -> node.getToken()}): PathContext {
    //add these "~" in order to help with csv delimiting

    val startToken = getToken(path.upwardNodes.first())
    //val startToken = "~" + getToken(path.upwardNodes.first()) + "~"
    print("Start Token: ")
    println(startToken)
    val endToken = getToken(path.downwardNodes.last())
    //val endToken = "~" + getToken(path.downwardNodes.last()) + "~"
    print("End Token: ")
    println(endToken)
    val astNodes = path.upwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.UP) } +
            path.downwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.DOWN) }
    //println("Printing AST nodes")
    //println(astNodes.toString())

    if (startToken == null || startToken == ""){
        println("startToken empty")
    }

    if(endToken == ""){
        println("endToken empty")
    }

    if(astNodes.isNullOrEmpty()){
        println("astnodes empty")
    }

    return PathContext(startToken, astNodes, endToken)
}
