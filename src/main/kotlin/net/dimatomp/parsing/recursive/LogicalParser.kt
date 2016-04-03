package net.dimatomp.parsing.recursive

import net.dimatomp.parsing.recursive.LogicalReservedToken.*

/**
 * Created by dimatomp on 22.03.16.
 */
data class ParseTree(val text: String, val children: Array<out ParseTree>)

fun parseTree(text: String, vararg children: ParseTree): ParseTree = ParseTree(text, children)

interface Nonterminal<Token> {
    fun buildTree(analyzer: LexicalAnalyzer<Token>): ParseTree
}

enum class LogicalNonterminal: Nonterminal<LogicalToken> {
    OR_N {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = parseTree("OR", AND_N.buildTree(analyzer), OR_P.buildTree(analyzer))
    },
    OR_P {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = matchPrime(analyzer, "OR'", OR, OR_N)
    },
    AND_N {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = parseTree("AND", XOR_N.buildTree(analyzer), AND_P.buildTree(analyzer))
    },
    AND_P {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = matchPrime(analyzer, "AND'", AND, AND_N)
    },
    XOR_N {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = parseTree("XOR", NOT_N.buildTree(analyzer), XOR_P.buildTree(analyzer))
    },
    XOR_P {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = matchPrime(analyzer, "XOR'", XOR, XOR_N)
    },
    NOT_N {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = matchPrime(analyzer, "NOT", NOT, NOT_N) { parseTree("NOT", UNIT.buildTree(analyzer)) }
    },
    UNIT {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree = when (analyzer.curToken) {
            L_BRACKET -> {
                analyzer.nextToken()
                val inside = OR_N.buildTree(analyzer)
                assert(analyzer.curToken == R_BRACKET)
                analyzer.nextToken()
                parseTree("UNIT", parseTree(L_BRACKET.text.toString()), inside, parseTree(R_BRACKET.text.toString()))
            }
            is LogicalNameChar -> parseTree("UNIT", NAME.buildTree(analyzer))
            else -> throw IllegalStateException("At ${analyzer.curPosition}: unexpected symbol '${analyzer.curToken.text}'")
        }
    },
    NAME {
        override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree
                = matchPrime(analyzer, "NAME", { it is LogicalNameChar }, NAME)
    };

    private val eps = "ε"

    protected fun matchPrime(analyzer: LexicalAnalyzer<LogicalToken>,
                             name: String,
                             token: (LogicalToken) -> Boolean,
                             next: LogicalNonterminal,
                             default: () -> ParseTree = { parseTree(name, parseTree(eps)) }): ParseTree {
        return if (token(analyzer.curToken)) {
            val text = analyzer.curToken.text
            analyzer.nextToken()
            parseTree(name, parseTree("$text"), next.buildTree(analyzer))
        } else default()
    }

    protected fun matchPrime(analyzer: LexicalAnalyzer<LogicalToken>,
                             name: String,
                             token: LogicalReservedToken,
                             next: LogicalNonterminal,
                             default: () -> ParseTree = { parseTree(name, parseTree(eps)) }): ParseTree
            = matchPrime(analyzer, name, { token == it }, next, default)
}

interface LexicalParser<Token> {
    fun buildTree(analyzer: LexicalAnalyzer<Token>): ParseTree
}

object LogicalParser: LexicalParser<LogicalToken> {
    override fun buildTree(analyzer: LexicalAnalyzer<LogicalToken>): ParseTree {
        val result = LogicalNonterminal.OR_N.buildTree(analyzer)
        assert(analyzer.curToken == END)
        return result
    }
}
