package net.dimatomp.parsing.recursive

import java.io.EOFException
import java.io.InputStream
import java.io.Reader

/**
 * Created by dimatomp on 22.03.16.
 */

abstract class LexicalAnalyzer<Token> {
    abstract var curToken: Token
        protected set
    abstract var curPosition: Int
        protected set

    protected abstract fun nextTokenImpl(): Token

    fun nextToken(): Token {
        curToken = nextTokenImpl()
        return curToken
    }
}

interface LogicalToken {
    val text: Char
}
enum class LogicalReservedToken(override val text: Char): LogicalToken {
    NOT('!'),
    AND('&'),
    XOR('^'),
    OR('|'),
    L_BRACKET('('),
    R_BRACKET(')'),
    START(0.toChar()),
    END((-1).toChar())
}
data class LogicalNameChar(override val text: Char): LogicalToken

class LogicalAnalyzer(val input: Reader): LexicalAnalyzer<LogicalToken>() {
    var curChar: Char = LogicalReservedToken.START.text
        private set
    override var curPosition: Int = 0
        protected set
    override var curToken: LogicalToken = LogicalReservedToken.START
        protected set

    init {
        nextToken()
    }

    constructor(stream: InputStream): this(stream.reader());

    fun nextChar() {
        try {
            curChar = input.read().toChar()
            curPosition++
        } catch (e: EOFException) {
            curChar = LogicalReservedToken.END.text
        }
    }

    override fun nextTokenImpl(): LogicalToken {
        do {
            nextChar()
        } while (curChar.isWhitespace())
        for (token in LogicalReservedToken.values())
            if (curChar == token.text)
                return token
        val result = LogicalNameChar(curChar)
        return result
    }
}
