import java.net.*
import java.io.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.jsoup.select.*
import org.sweble.wikitext.engine.*
import org.sweble.wikitext.engine.utils.*
import org.sweble.wikitext.parser.nodes.WtText
import org.sweble.wikitext.parser.nodes.WtNode

fun main(args: Array<String>) {
    val url = URL("http://de.wikipedia.org/w/index.php?title=Stringtheorie&action=edit")

    val wikiText = url.getWikipediaContentAsMarkup()
    val tempFile = getTempFile()
    tempFile.createNewFile()
    tempFile.writeText(wikiText)

    val config = DefaultConfigEnWp.generate()
    val engine = WtEngineImpl(config)
    val pageTitle = PageTitle.make(config, tempFile.path)
    val pageId = PageId(pageTitle, -1)
    val cp = engine.postprocess(pageId, wikiText, null)
    println((cp[0][0][0] as WtText).getContent())
}

fun WtNode.toPlainText(): String {
    if (this.any()) return this.map { it.toPlainText() }.join("/n")

    return when (this) {
        is WtText -> content
        else -> this.javaClass.getName()
    }
}

fun URL.getText() = BufferedReader(InputStreamReader(openStream())).readText()
fun URL.getWikipediaContentAsMarkup() = Jsoup.parse(getText()).select("#wpTextbox1").text()

val WtText.content: String get() = getContent()

fun getTempFile() = File(File(File("C:"), "tmp"), "tempFile.txt")