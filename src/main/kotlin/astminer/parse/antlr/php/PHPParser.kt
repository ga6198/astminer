package astminer.parse.antlr.php

import astminer.common.model.Parser
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.convertAntlrTree
import me.vovak.antlr.parser.PhpLexer
import me.vovak.antlr.parser.PhpParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.LexerATNSimulator
import org.antlr.v4.runtime.atn.ParserATNSimulator
import org.antlr.v4.runtime.atn.PredictionContextCache
import java.io.InputStream


class PhpMainParser : Parser<SimpleNode> {
    override fun parse(content: InputStream): SimpleNode? {
        //getting ast from antlr
        //https://stackoverflow.com/questions/4931346/how-to-output-the-ast-built-using-antlr

        return try {
            val lexer = PhpLexer(CharStreams.fromStream(content))

            //reset atn simulator to clear cache and prevent mem overflow
            lexer.interpreter = LexerATNSimulator(lexer, lexer.atn, lexer.interpreter.decisionToDFA, PredictionContextCache())
            lexer.interpreter.clearDFA()
            lexer.removeErrorListeners()
            val tokens = CommonTokenStream(lexer)
            val parser = PhpParser(tokens)

            //reset atn simulator to clear cache and prevent mem overflow
            parser.interpreter = ParserATNSimulator(parser, parser.atn, parser.interpreter.decisionToDFA, PredictionContextCache())
            parser.interpreter.clearDFA()
            parser.removeErrorListeners()
            //val context = parser.compilationUnit() //original before I commented it out
            //not sure if this declaration is correct, but convertAntlrTree does take a parser rule context. Look at the javascript folder for more help
            //maybe try to search for function that returns a ParserRuleContext inside PhpParser? convertAntlrTree takes a ParserRuleContext as a param
            //val context = parser.htmlElements() //functional, but might not be what I want //parser.context //parser.ruleContext instead?
            //CommonTree tree = (CommonTree)parser.parse().getTree();
            //val context = parser.expression()
            //val context = parser.htmlElementOrPhpBlock()
            val context = parser.phpBlock()


            //returns a simpleNode. Will run for each file, meaning files are converted into nodes
            convertAntlrTree(context, PhpParser.ruleNames, PhpParser.VOCABULARY)
        } catch (e: Exception) {
            e.printStackTrace() //show error
            println("exception reached")
            null
        }
    }
}
