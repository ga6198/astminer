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

class PhpParser : Parser<SimpleNode> {
    override fun parse(content: InputStream): SimpleNode? {
        return try {
            val lexer = PhpLexer(CharStreams.fromStream(content))
            lexer.removeErrorListeners()
            val tokens = CommonTokenStream(lexer)
            val parser = PhpParser(tokens)
            parser.removeErrorListeners()
            val context = parser.compilationUnit()
            convertAntlrTree(context, PhpParser.ruleNames, PhpParser.VOCABULARY)
        } catch (e: Exception) {
            null
        }
    }
}
