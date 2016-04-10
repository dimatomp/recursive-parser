package net.dimatomp.parsing.recursive

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by dimatomp on 29.03.16.
 */
private val mapper: ObjectMapper = jacksonObjectMapper()

@WebServlet(value = "/buildtree")
class JsonServlet: HttpServlet() {
    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        val analyzer: LexicalAnalyzer<LogicalToken> = LogicalAnalyzer(req!!.reader)
        val result = LogicalParser.buildTree(analyzer)
        resp!!.contentType = "application/json"
        resp.characterEncoding = "UTF-8"
        mapper.writeValue(resp.writer, result)
    }
}