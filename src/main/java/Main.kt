import java.net.*
import java.io.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.jsoup.select.*
import org.sweble.wikitext.engine.*
import org.sweble.wikitext.engine.utils.*
import org.sweble.wikitext.parser.nodes.*

fun main(args: Array<String>) {
    val title = "Tree"
    val url = URL("http://en.wikipedia.org/w/index.php?title=${title}&action=edit")

    val wikiText = url.getWikipediaContentAsMarkup()
    val tempFile = getTempFile()
    tempFile.createNewFile()
    tempFile.writeText(wikiText)

    val config = DefaultConfigEnWp.generate()
    val engine = WtEngineImpl(config)
    val pageTitle = PageTitle.make(config, tempFile.path)
    val pageId = PageId(pageTitle, -1)
    val cp = engine.postprocess(pageId, wikiText, null)
    println(cp.text())
}

private fun WtNode.text(indentLevel: Int = 0): String = " ".repeat(2 * indentLevel) + when (this) {
    is WtText -> content
    is WtBold -> "*${single().text()}*"
    is WtItalics -> "/${subTextsCommaJoined()}/"
    is WtPageName -> subTextsCommaJoined()
    is WtLinkTitle -> subTextsCommaJoined()
    is WtInternalLink -> "${getTitle().text()}[${getTarget().text()}]"
    else -> "{${javaClass.getSimpleName()}}${map {"\n" + it.text(indentLevel = indentLevel + 1)}.join("")}"
}

fun URL.getText() = BufferedReader(InputStreamReader(openStream())).readText()
fun URL.getWikipediaContentAsMarkup() = Jsoup.parse(getText()).select("#wpTextbox1").text()

val WtText.content: String get() = getContent()
fun WtNode.subTexts(indentLevel : Int = 0) = map {it.text(indentLevel)}
fun WtNode.subTextsCommaJoined(indentLevel : Int = 0) = subTexts(indentLevel = indentLevel).join(", ")

fun getTempFile() = File(File(File("C:"), "tmp"), "tempFile.txt")