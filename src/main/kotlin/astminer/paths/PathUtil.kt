package astminer.paths

import astminer.common.model.*

fun toPathContext(path: ASTPath, getToken: (Node) -> String = { node -> node.getToken()}): PathContext {
    //add these "~" in order to help with csv delimiting

    //val startToken = getToken(path.upwardNodes.first())
    val startToken = "~" + getToken(path.upwardNodes.first()) + "~"
    println("Start Token")
    println(startToken)
    val endToken = "~" + getToken(path.downwardNodes.last()) + "~"
    println("End Token")
    println(endToken)
    val astNodes = path.upwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.UP) } +
            path.downwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.DOWN) }
    //println("Printing AST nodes")
    //println(astNodes.toString())
    return PathContext(startToken, astNodes, endToken)
}
