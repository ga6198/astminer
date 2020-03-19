package astminer.parse.antlr

import astminer.common.model.Node

class SimpleNode(private val typeLabel: String, private var parent: Node?, private var token: String?) : Node {
    private val metadata: MutableMap<String, Any> = HashMap()

    private var children: List<Node> = emptyList()

    //implementing a print function that prints all members
    fun printInfo(){
        println("SimpleNode Info")
        println("TypeLabel: $typeLabel")
        println("Parent: $parent")
        println("Token: $token")
        println("Children: $children")
        println("Metadata: $metadata")
    }

    fun setChildren(newChildren: List<Node>) {
        children = newChildren
        children.forEach { (it as SimpleNode).setParent(this) }
    }

    fun setParent(newParent: Node?) {
        parent = newParent
    }

    override fun getTypeLabel(): String {
        return typeLabel
    }

    override fun getChildren(): List<Node> {
        return children
    }

    override fun getParent(): Node? {
        return parent
    }

    override fun getToken(): String {
        return token ?: "null"
    }

    fun setToken(newToken: String) {
        token = newToken
    }

    override fun isLeaf(): Boolean {
        return children.isEmpty()
    }

    override fun getMetadata(key: String): Any? {
        return metadata[key]
    }

    override fun setMetadata(key: String, value: Any) {
        metadata[key] = value
    }

    override fun getChildrenOfType(typeLabel: String) = getChildren().filter {
        decompressTypeLabel(it.getTypeLabel()).firstOrNull() == typeLabel
    }
}
