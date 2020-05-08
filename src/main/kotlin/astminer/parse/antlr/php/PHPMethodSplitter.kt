package astminer.parse.antlr.php

import astminer.common.*
import astminer.common.model.*
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.decompressTypeLabel


//self-written, based on the PythonMethodSplitter.kt
//will not need the METHOD_RETURN_TYPE_NODE from JavaMethodSplitter.kt
//funcdef comes from PythonParser.g4, Python2.g4 and Python.g4 files
//methodDeclaration is from JavaParser.g4 and Java8Parser.g4

//TODO: investigate this to see why name node are returned as null. Might be because I commented out the METHOD_NAME_NODE

class PhpMethodSplitter : TreeMethodSplitter<SimpleNode> {

    companion object {
        //private const val METHOD_NODE = "namespaceStatement"

        //Try to change this to NAME, since the parser is only picking up names
        //private const val METHOD_NODE = "NAME"
        private const val METHOD_NODE = "functionDeclaration" //"funcdef"
        private const val METHOD_NAME_NODE = "identifier" //"NAME"

        private const val CLASS_DECLARATION_NODE = "classDeclaration" //"classdef"
        private const val CLASS_NAME_NODE = "identifier" //"NAME"

        private const val METHOD_PARAMETER_NODE = "formalParameterList" //"parameters"
        private const val METHOD_PARAMETER_INNER_NODE = "formalParameterList" //"typedargslist"
        private const val METHOD_SINGLE_PARAMETER_NODE = "formalParameter"//"tfpdef"
        private const val PARAMETER_NAME_NODE = "VarName"//"variableInitializer" //"NAME"
    }

    override fun splitIntoMethods(root: SimpleNode, filePath: String): Collection<MethodInfo<SimpleNode>> {
        println("running splitIntoMethods")
        println("current file path: $filePath")

        //NOTE: filePath can be passed in from extractFromMethods using it.filePath, with "it" being a filePath
        //This will require a lot of modification to splitIntoMethods in each of the methodSplitters and TreeSplittingModel.kt
        //TODO: leave this for later

        //retrieve dummy method info (places all global code under a method)
        val dummyMethodInfo = collectDummyMethodInfo(root, filePath)

        //extracts only the method nodes from all roots by checking if the type label is the same as the METHOD_NODE one given at the top
        val methodRoots = root.preOrder().filter {
            //for every node, decompressTypeLabel splits the
            //TODO: Look at getTypeLabel() for PHP. Probably uses SimpleNode class
            //decompressTypeLabel splits a type label, splitting it wherever there is |
            //last() gets the last element of the split
            //Only if the result is the same as a method node, filter()

            decompressTypeLabel(it.getTypeLabel()).last() == METHOD_NODE
        }
        //DEBUG: Added to see each methodRoot's info
        println("Method roots after extracting only the true method nodes: ${methodRoots}")
        for ((index, root) in methodRoots.withIndex()){
            print("Examining Method Node $index: ")
            root.prettyPrint();
        }

        var methodInfo = methodRoots.map { collectMethodInfo(it as SimpleNode) }

        //Still investigating dummy method, so comment this out if the dummy method causes errors
        //add dummy method in with other method Info
        methodInfo = methodInfo.toMutableList()
        methodInfo.add(dummyMethodInfo)
        //methodInfo.toMutableList().add(dummyMethodInfo)

        return methodInfo
    }

    //function to gather global code (represented with ANTLR "statement" under a dummy method)
    private fun collectDummyMethodInfo(root: SimpleNode, filePath: String): MethodInfo<SimpleNode>{
        //get just the final part of the filePath, (the file name)
        val fileParts = filePath.split("/")
        val fileNameWithExtension = fileParts[fileParts.size - 1]
        val fileNameExtensionParts = fileNameWithExtension.split(".")
        val fileName = fileNameExtensionParts[0]

        //extract the things that are not method nodes. Extract global scope lines. Investigate antlr to see what those are (topStatement|statement nodes).
        //add the end combine the list with the methodRoots
        val nonMethodRoots = root.preOrder().filter {
            //checks if the method roots contains statements not wrapped in functions
            decompressTypeLabel(it.getTypeLabel()).contains("topStatement") &&
            decompressTypeLabel(it.getTypeLabel()).contains("statement")
        }

        //cast nodes into simpleNodes
        var nonMethodRootsList = mutableListOf<SimpleNode>();
        for (statement in nonMethodRoots){
            nonMethodRootsList.add(statement as SimpleNode)
        }

        //for each nonMethodRoot, the "topstatement|" label may need to be removed because wrapping them in another node no longer makes them topStatements

        //a new header node to wrap the nonmethodroots in
        val newMethodRoot = SimpleNode("topStatement|" + METHOD_NODE, null, "null") //original type label: phpBlock

        newMethodRoot.setChildren(nonMethodRootsList) //newMethodRoot.setChildren(nonMethodRoots)

        //create a node that represents the nonMethodRoot nodes' name
        val rootMethodNameNode = SimpleNode(METHOD_NAME_NODE + "|Label", newMethodRoot, "include_" + fileName)

        //print the dummy node's info
        println("Dummy Node Info")
        newMethodRoot.prettyPrint()

        // dummy function info
        // ParameterList and ElementNode are empty because the dummy function will not have a class wrapper or parameters
        val rootMethodInfo =  MethodInfo(
            MethodNode<SimpleNode>(newMethodRoot, null, rootMethodNameNode),
            ElementNode<SimpleNode>(null, null),
            mutableListOf<ParameterNode<SimpleNode>>() //emptyList() //parametersList
        )
        //newMethodRoot.map{}

        return rootMethodInfo
    }

    private fun collectMethodInfo(methodNode: SimpleNode): MethodInfo<SimpleNode> {
        val methodName = methodNode.getChildOfType(METHOD_NAME_NODE) as? SimpleNode

        val classRoot = getEnclosingClass(methodNode)
        val className = classRoot?.getChildOfType(CLASS_NAME_NODE) as? SimpleNode

        //as? will typecast the parametersRoot to null
        val parametersRoot = methodNode.getChildOfType(METHOD_PARAMETER_NODE) as? SimpleNode
        val innerParametersRoot = parametersRoot?.getChildOfType(METHOD_PARAMETER_INNER_NODE) as? SimpleNode

        val parametersList = when {
            //innerParametersRoot != null -> getListOfParameters(innerParametersRoot)

            //The parametersRoot should only call getListOfParameters if it's not null
            //If this is the case, why are null nodes still passed to the function?
            parametersRoot != null -> getListOfParameters(parametersRoot)
            else -> emptyList()
        }

        return MethodInfo(
            MethodNode(methodNode, null, methodName),
            ElementNode(classRoot, className),
            parametersList
        )
    }

    private fun getEnclosingClass(node: SimpleNode): SimpleNode? {
        if (decompressTypeLabel(node.getTypeLabel()).last() == CLASS_DECLARATION_NODE) {
            return node
        }
        val parentNode = node.getParent() as? SimpleNode
        if (parentNode != null) {
            return getEnclosingClass(parentNode)
        }
        return null
    }

    private fun getListOfParameters(parameterRoot: SimpleNode): List<ParameterNode<SimpleNode>> {
        var rootChildren = parameterRoot.getChildrenOfType(METHOD_SINGLE_PARAMETER_NODE)
        for (node in rootChildren){
            println("Token: ${node.getToken()}")
            println("Type Label: ${node.getTypeLabel()}")
            println("isLeaf?: ${node.isLeaf()}")
            println("Full Path")
            println(node.prettyPrint())
        }


        //if the last type label is "VarName", a list of parameter nodes is returned
        //used if we directly found the parameter name
        if (decompressTypeLabel(parameterRoot.getTypeLabel()).last() == PARAMETER_NAME_NODE) {
            return listOf(ParameterNode(parameterRoot, null, parameterRoot))
        }

        var parameterNodeList = mutableListOf<ParameterNode<SimpleNode>>();
        var parameterChildren = parameterRoot.getChildrenOfType(METHOD_SINGLE_PARAMETER_NODE);

        for (child in parameterChildren){
            //If the node of the "FormalParameter" is "VarName", the parameter node has been found
            //ex. $section
            if (decompressTypeLabel(child.getTypeLabel()).last() == PARAMETER_NAME_NODE) {
                parameterNodeList.add(ParameterNode(child as SimpleNode, null, child))
            }
            else{
                //VarName not found yet, so check if child is VarName
                var deeperChild = child.getChildOfType(PARAMETER_NAME_NODE)
                if(deeperChild != null){
                    parameterNodeList.add(ParameterNode(child as SimpleNode, null, deeperChild as SimpleNode))
                }
                //once again, varName not found, so extract another set of children. Used to handle something like &$cid
                else{
                    //get all nodes that starts with variableInitializer -- ex. &$cid becomes $cid
                    var deeperChildren = child.getChildrenOfType("variableInitializer")
                    for (deeperLevelChild in deeperChildren){
                        if (decompressTypeLabel(deeperLevelChild.getTypeLabel()).last() == PARAMETER_NAME_NODE) {
                            parameterNodeList.add(ParameterNode(deeperLevelChild as SimpleNode, null, deeperLevelChild))
                        }
                    }
                }
            }

        }

        return parameterNodeList;

        //if "VarName" was not found, get children of type "FormalParameter"
        return parameterRoot.getChildrenOfType(METHOD_SINGLE_PARAMETER_NODE).map {
            //DEBUG: trying to solve this issue: kotlin.TypeCastException: null cannot be cast to non-null type astminer.parse.antlr.SimpleNode

            //If the node of the "FormalParameter" is "VarName", the parameter node has been found
            //ex. $section
            if (decompressTypeLabel(it.getTypeLabel()).last() == PARAMETER_NAME_NODE) {
                ParameterNode(it as SimpleNode, null, it)
            }
            //ex. &$cid
            else {
                //There's likely an issue with getChildOfType. it==null does nothing, so it must be an issue with the children
                var child = it.getChildOfType(PARAMETER_NAME_NODE)
                if(child == null){
                    println("null node found")
                    //var variableChildren = it.getChildrenOfType(PARAMETER_NAME_NODE);
                    var typeLabel = "variableInitializer" //"variableInitializer|VarName"
                    ParameterNode(it as SimpleNode, null, it.getChildOfType(typeLabel) as SimpleNode)
                }

                var child2 = it.getChildOfType("variableInitializer|VarName")
                if(child2 == null){
                    println("getting variableInitializer|VarName child did not work")

                }

                //get children of the current child node
                //var children = it.getChildrenOfType("variableInitializer|VarName")


                    //Original Line
                    ParameterNode(it as SimpleNode, null, it.getChildOfType(PARAMETER_NAME_NODE) as SimpleNode)

                //ParameterNode(it as SimpleNode, null, it.getChildOfType("variableInitializer|VarName") as SimpleNode)

            }
        }
    }
}

/*class PhpMethodSplitter : TreeMethodSplitter<SimpleNode> {
    companion object {
        private const val METHOD_NODE = "functionDeclaration"//"methodDeclaration"
        private const val METHOD_RETURN_TYPE_NODE = "typeTypeOrVoid"
        private const val METHOD_NAME_NODE = "IDENTIFIER"

        private const val CLASS_DECLARATION_NODE = "classDeclaration"
        private const val CLASS_NAME_NODE = "IDENTIFIER"

        private const val METHOD_PARAMETER_NODE = "formalParameters"
        private const val METHOD_PARAMETER_INNER_NODE = "formalParameterList"
        private val METHOD_SINGLE_PARAMETER_NODE = listOf("formalParameter", "lastFormalParameter")
        private const val PARAMETER_RETURN_TYPE_NODE = "typeType"
        private const val PARAMETER_NAME_NODE = "variableDeclaratorId"
    }

    override fun splitIntoMethods(root: SimpleNode): Collection<MethodInfo<SimpleNode>> {
        val methodRoots = root.preOrder().filter {
            decompressTypeLabel(it.getTypeLabel()).last() == METHOD_NODE
        }
        return methodRoots.map { collectMethodInfo(it as SimpleNode) }
    }

    private fun collectMethodInfo(methodNode: SimpleNode): MethodInfo<SimpleNode> {
        val methodName = methodNode.getChildOfType(METHOD_NAME_NODE) as? SimpleNode
        val methodReturnTypeNode =  methodNode.getChildOfType(METHOD_RETURN_TYPE_NODE) as? SimpleNode
        methodReturnTypeNode?.setToken(collectParameterToken(methodReturnTypeNode))

        val classRoot = getEnclosingClass(methodNode)
        val className = classRoot?.getChildOfType(CLASS_NAME_NODE) as? SimpleNode

        val parametersRoot = methodNode.getChildOfType(METHOD_PARAMETER_NODE) as? SimpleNode
        val innerParametersRoot = parametersRoot?.getChildOfType(METHOD_PARAMETER_INNER_NODE) as? SimpleNode

        val parametersList = when {
            innerParametersRoot != null -> getListOfParameters(innerParametersRoot)
            parametersRoot != null -> getListOfParameters(parametersRoot)
            else -> emptyList()
        }

        return MethodInfo(
            MethodNode(methodNode, methodReturnTypeNode, methodName),
            ElementNode(classRoot, className),
            parametersList
        )
    }

    private fun getEnclosingClass(node: SimpleNode): SimpleNode? {
        if (decompressTypeLabel(node.getTypeLabel()).last() == CLASS_DECLARATION_NODE) {
            return node
        }
        val parentNode = node.getParent() as? SimpleNode
        if (parentNode != null) {
            return getEnclosingClass(parentNode)
        }
        return null
    }

    private fun getListOfParameters(parametersRoot: SimpleNode): List<ParameterNode<SimpleNode>> {
        if (METHOD_SINGLE_PARAMETER_NODE.contains(decompressTypeLabel(parametersRoot.getTypeLabel()).last())) {
            return listOf(getParameterInfoFromNode(parametersRoot))
        }
        return parametersRoot.getChildren().filter {
            val firstType = decompressTypeLabel(it.getTypeLabel()).first()
            METHOD_SINGLE_PARAMETER_NODE.contains(firstType)
        }.map {
            getParameterInfoFromNode(it as SimpleNode)
        }
    }

    private fun getParameterInfoFromNode(parameterRoot: SimpleNode): ParameterNode<SimpleNode> {
        val returnTypeNode = parameterRoot.getChildOfType(PARAMETER_RETURN_TYPE_NODE) as? SimpleNode
        returnTypeNode?.setToken(collectParameterToken(returnTypeNode))
        return ParameterNode(
            parameterRoot,
            returnTypeNode,
            parameterRoot.getChildOfType(PARAMETER_NAME_NODE) as? SimpleNode
        )
    }

    private fun collectParameterToken(parameterRoot: SimpleNode): String {
        if (parameterRoot.isLeaf()) {
            return parameterRoot.getToken()
        }
        return parameterRoot.getChildren().joinToString(separator = "") { child ->
            collectParameterToken(child as SimpleNode)
        }
    }
}*/
