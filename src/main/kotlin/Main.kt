import java.net.*
import java.io.*
import org.jsoup.*
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
    val page = engine.postprocess(PageId(PageTitle.make(config, title), -1), wikiText, null).getPage()

    val summaryParagraphs = page.filterIsInstance(javaClass<WtParagraph>())
    val sections = page.filterIsInstance(javaClass<WtSection>())
    val article = Node(title, paragraphs = summaryParagraphs, subNodes = sections.map { it.toNode() })
    //println(page.text())
    println(article.toString())
}

fun WtSection.toNode(): Node = Node(name = (getHeading().single() as WtText).content.trim(),
        paragraphs = getBody().filterIsInstance(javaClass<WtParagraph>()),
        subNodes = getBody().filterIsInstance(javaClass<WtSection>()) map { it.toNode() })

fun WtNode.text(): String = when (this) {
    is WtText -> content
    is WtBold -> "*${single().text()}*"
    is WtItalics -> "/${subTextsCommaJoined()}/"
    is WtPageName -> subTextsCommaJoined()
    is WtLinkTitle -> subTextsCommaJoined()
    is WtParagraph -> subTexts().map { if (it.split("\n").count() <= 1) it else it + "\n" }.join("")
    is WtTagExtension -> "{${this.getName()}}"
    is WtInternalLink -> "${getTitle().text()}[${getTarget().text()}]"
    else -> {
        val subText = subTextsNewLineJoinedAndIndented()
        "{${javaClass.getSimpleName()}}${"\n" + subText}"
    }
}

fun WtNode.subTextsNewLineJoinedAndIndented() = subTexts().join("\n").indented()
fun URL.getText() = BufferedReader(InputStreamReader(openStream())).readText()
fun URL.getWikipediaContentAsMarkup() = Jsoup.parse(getText()).select("#wpTextbox1").text()

val WtText.content: String get() = getContent()
fun WtNode.subTexts() = map {it.text()}
fun WtNode.subTextsCommaJoined() = subTexts().join(", ")

fun String.indented(indentation: String = "\t") = this.split("\n").map { indentation + it }.join("\n")

class Node(val name: String, val paragraphs: List<WtParagraph>, val subNodes: List<Node>) {
    override fun toString(): String =
            "= ${name} ="+
                    "\n" +
                    "\n" + paragraphs.map { it.text() }.join("\n") +
                    "\n" +
                    "\n" + subNodes.map { it.toString() }.join("\n").indented()
}