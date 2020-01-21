package astminer.parse.antlr.php

import astminer.common.model.Parser
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.convertAntlrTree
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.CharStreams
import java.io.InputStream
import java.lang.Exception
import me.vovak.antlr.parser.PhpLexer
import me.vovak.antlr.parser.PhpParser

import me.vovak.antlr.parser.Java8Lexer
import me.vovak.antlr.parser.Java8Parser
import org.antlr.runtime.tree.*

class PhpMainParser : Parser<SimpleNode> {
    override fun parse(content: InputStream): SimpleNode? {
        //getting ast from antlr
        //https://stackoverflow.com/questions/4931346/how-to-output-the-ast-built-using-antlr

        return try {
            val lexer = PhpLexer(CharStreams.fromStream(content))
            lexer.removeErrorListeners()
            val tokens = CommonTokenStream(lexer)
            val parser = PhpParser(tokens)
            parser.removeErrorListeners()
            //val context = parser.compilationUnit() //original before I commented it out
            //not sure if this declaration is correct, but convertAntlrTree does take a parser rule context. Look at the javascript folder for more help
            //maybe try to search for function that returns a ParserRuleContext inside PhpParser? convertAntlrTree takes a ParserRuleContext as a param
            val context = parser.htmlElements() //functional, but might not be what I want //parser.context //parser.ruleContext instead?
            //CommonTree tree = (CommonTree)parser.parse().getTree();

            convertAntlrTree(context, PhpParser.ruleNames, PhpParser.VOCABULARY)
        } catch (e: Exception) {
            print("exception reached")
            null
        }
    }
}
