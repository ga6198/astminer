package astminer.parse.antlr.php

import astminer.common.*
import astminer.common.model.*
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.decompressTypeLabel


//self-written, based on the PythonMethodSplitter.kt
//will not need the METHOD_RETURN_TYPE_NODE from JavaMethodSplitter.kt
//funcdef comes from PythonParser.g4, Python2.g4 and Python.g4 files
//methodDeclaration is from JavaParser.g4 and Java8Parser.g4

class PhpMethodSplitter : TreeMethodSplitter<SimpleNode> {

    companion object {
        //private const val METHOD_NODE = "namespaceStatement"
        private const val METHOD_NODE = "functionDeclaration" //"funcdef"
        private const val METHOD_NAME_NODE = "identifier" //"NAME"

        private const val CLASS_DECLARATION_NODE = "classDeclaration" //"classdef"
        private const val CLASS_NAME_NODE = "identifier" //"NAME"

        private const val METHOD_PARAMETER_NODE = "formalParameterList" //"parameters"
        //private const val METHOD_PARAMETER_INNER_NODE = "formalParameterList" //"typedargslist"
        private const val METHOD_SINGLE_PARAMETER_NODE = "formalParameter"//"tfpdef"
        private const val PARAMETER_NAME_NODE = "variableInitializer" //"NAME"
    }

    override fun splitIntoMethods(root: SimpleNode): Collection<MethodInfo<SimpleNode>> {
        println("running splitIntoMethods")

        val methodRoots = root.preOrder().filter {
            //for every node, decompressTypeLabel splits the
            //TODO: Look at getTypeLabel() for PHP. Probably uses SimpleNode class
            //decompressTypeLabel splits a type label, splitting it wherever there is |
            //last() gets the last element of the split
            //Only if the result is the same as a method node, filter()

            val temp = decompressTypeLabel(it.getTypeLabel())
            val temp2 = decompressTypeLabel(it.getTypeLabel()).last()
            println("decompressTypeLabel(it.getTypeLabel()): $temp")
            println("decompressTypeLabel(it.getTypeLabel()).last(): $temp2") //These all print CAPITAL letters
            val lookFor = "functionDeclaration"
            val lookFor2 = "FUNCTIONDECLARATION"
            /*if(decompressTypeLabel(it.getTypeLabel()).last().equals(lookFor) || decompressTypeLabel(it.getTypeLabel()).equals(lookFor) ){
                print("Method node caught!")
                //there could be an issue w/ the methodnode being lower case, but the decompresstypelabel being capital
            }

            if(decompressTypeLabel(it.getTypeLabel()).last().equals(lookFor2) || decompressTypeLabel(it.getTypeLabel()).equals(lookFor2) ){
                print("Method node 2 caught!")
                //there could be an issue w/ the methodnode being lower case, but the decompresstypelabel being capital
            }*/


            decompressTypeLabel(it.getTypeLabel()).last() == METHOD_NODE
        }
        return methodRoots.map { collectMethodInfo(it as SimpleNode) }
    }

    private fun collectMethodInfo(methodNode: SimpleNode): MethodInfo<SimpleNode> {
        val methodName = methodNode.getChildOfType(METHOD_NAME_NODE) as? SimpleNode

        val classRoot = getEnclosingClass(methodNode)
        val className = classRoot?.getChildOfType(CLASS_NAME_NODE) as? SimpleNode

        val parametersRoot = methodNode.getChildOfType(METHOD_PARAMETER_NODE) as? SimpleNode
        //val innerParametersRoot = parametersRoot?.getChildOfType(METHOD_PARAMETER_INNER_NODE) as? SimpleNode

        val parametersList = when {
            //innerParametersRoot != null -> getListOfParameters(innerParametersRoot)
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
        if (decompressTypeLabel(parameterRoot.getTypeLabel()).last() == PARAMETER_NAME_NODE) {
            return listOf(ParameterNode(parameterRoot, null, parameterRoot))
        }
        return parameterRoot.getChildrenOfType(METHOD_SINGLE_PARAMETER_NODE).map {
            if (decompressTypeLabel(it.getTypeLabel()).last() == PARAMETER_NAME_NODE) {
                ParameterNode(it as SimpleNode, null, it)
            } else {
                ParameterNode(it as SimpleNode, null, it.getChildOfType(PARAMETER_NAME_NODE) as SimpleNode)
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
