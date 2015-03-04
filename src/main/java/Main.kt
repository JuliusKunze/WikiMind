import java.net.*
import java.io.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.jsoup.select.*
import org.sweble.wikitext.engine.*
import org.sweble.wikitext.engine.utils.*
import org.sweble.wikitext.parser.nodes.*

fun main(args: Array<String>) {
    val language = "de"
    val title = "Stringtheorie"
    val url = URL("http://${language}.wikipedia.org/w/index.php?title=${title}&action=edit")

    val wikiText = url.getWikipediaContentAsMarkup()

    val config = DefaultConfigEnWp.generate()
    val engine = WtEngineImpl(config)
    val cp = engine.postprocess(PageId(PageTitle.make(config, title), -1), wikiText, null)
    println(cp.text())
}

private fun WtNode.text(indentLevel: Int = 0): String = " ".repeat(2 * indentLevel) + when (this) {
    is WtText -> content
    is WtBold -> "*${single().text()}*"
    is WtItalics -> "/${subTextsCommaJoined()}/"
    is WtPageName -> subTextsCommaJoined()
    is WtLinkTitle -> subTextsCommaJoined()
    is WtParagraph -> map {it.text()}.join("")
    is WtInternalLink -> "${getTitle().text()}[${getTarget().text()}]"
    else -> "{${javaClass.getSimpleName()}}${map {"\n" + it.text(indentLevel = indentLevel + 1)}.join("")}"
}

fun URL.getText() = BufferedReader(InputStreamReader(openStream())).readText()
fun URL.getWikipediaContentAsMarkup() = Jsoup.parse(getText()).select("#wpTextbox1").text()

val WtText.content: String get() = getContent()
fun WtNode.subTexts() = map {it.text()}
fun WtNode.subTextsCommaJoined() = subTexts().join(", ")