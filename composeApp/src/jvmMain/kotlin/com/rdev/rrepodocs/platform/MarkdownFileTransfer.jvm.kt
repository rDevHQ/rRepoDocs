package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.MarkdownPreviewBlock
import com.rdev.rrepodocs.domain.usecase.RenderMarkdownPreviewUseCase
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterJob
import java.io.File
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font

actual fun pickMarkdownFileForImport(): ImportedMarkdownFile? {
    val dialog = FileDialog(null as Frame?, "Import Markdown File", FileDialog.LOAD).apply {
        file = "*.md;*.markdown"
        filenameFilter = java.io.FilenameFilter { _, name -> name.isMarkdownFileName() }
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val fileName = dialog.file ?: return null
    val file = File(directory, fileName)
    return ImportedMarkdownFile(
        fileName = file.name,
        content = file.readText(),
    )
}

actual fun exportMarkdownFile(
    defaultFileName: String,
    content: String,
): String? {
    val dialog = FileDialog(null as Frame?, "Export Markdown File", FileDialog.SAVE).apply {
        file = defaultFileName.ensureMarkdownFileName()
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val selectedFileName = dialog.file ?: return null
    val file = File(directory, selectedFileName.ensureMarkdownFileName())
    file.writeText(content)
    return file.absolutePath
}

actual fun exportPdfFile(
    defaultFileName: String,
    title: String,
    content: String,
): String? {
    val dialog = FileDialog(null as Frame?, "Download as PDF", FileDialog.SAVE).apply {
        file = defaultFileName.ensurePdfFileName()
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val selectedFileName = dialog.file ?: return null
    val file = File(directory, selectedFileName.ensurePdfFileName())
    writePdf(file, title, content)
    return file.absolutePath
}

actual fun printMarkdownPreview(
    title: String,
    content: String,
): Boolean {
    val printerJob = PrinterJob.getPrinterJob().apply {
        jobName = title.ifBlank { "rRepoDocs preview" }
        setPrintable(MarkdownPreviewPrintable(RenderMarkdownPreviewUseCase()(content)))
    }
    if (!printerJob.printDialog()) return false
    printerJob.print()
    return true
}

internal fun writePdf(file: File, title: String, markdown: String) {
    PDDocument().use { document ->
        PdfDocumentWriter(document, title).write(markdown)
        document.save(file)
    }
}

private class PdfDocumentWriter(
    private val document: PDDocument,
    private val title: String,
) {
    private val pageWidth = PDRectangle.LETTER.width
    private val pageHeight = PDRectangle.LETTER.height
    private val margin = 54f
    private var page: PDPage? = null
    private var stream: PDPageContentStream? = null
    private var cursorY = 0f

    fun write(markdown: String) {
        document.documentInformation.title = title
        startPage()
        val lines = markdown.replace("\r\n", "\n").lines()
        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            val table = parseMarkdownTable(lines, index)
            if (table != null) {
                writeTable(table)
                index = table.nextLineIndex
                continue
            }
            val heading = Regex("^(#{1,6})\\s+(.+)$").matchEntire(line)
            when {
                heading != null -> {
                    val level = heading.groupValues[1].length
                    writeLine(
                        cleanMarkdown(heading.groupValues[2]),
                        PDType1Font.HELVETICA_BOLD,
                        (18f - (level - 1) * 1.4f).coerceAtLeast(12f),
                        22f,
                    )
                }
                line.isBlank() -> writeLine("", PDType1Font.HELVETICA, 10f, 9f)
                else -> writeLine(cleanMarkdown(line), PDType1Font.HELVETICA, 10.5f, 15f)
            }
            index++
        }
        stream?.close()
    }

    private fun writeLine(text: String, font: PDType1Font, fontSize: Float, lineHeight: Float) {
        val availableWidth = pageWidth - margin * 2
        val lines = wrap(sanitizePdfText(text, font), font, fontSize, availableWidth).ifEmpty { listOf("") }
        lines.forEach { wrappedLine ->
            if (cursorY - lineHeight < margin) startPage()
            stream!!.beginText()
            stream!!.setFont(font, fontSize)
            stream!!.newLineAtOffset(margin, cursorY)
            stream!!.showText(wrappedLine)
            stream!!.endText()
            cursorY -= lineHeight
        }
    }

    private fun startPage() {
        stream?.close()
        page = PDPage(PDRectangle.LETTER).also(document::addPage)
        stream = PDPageContentStream(document, page)
        cursorY = pageHeight - margin
    }

    private fun writeTable(table: PdfMarkdownTable) {
        val columnWidths = tableColumnWidths(table.headers.size)
        writeTableRow(table.headers, columnWidths, isHeader = true)
        table.rows.forEach { row -> writeTableRow(row, columnWidths, isHeader = false) }
        cursorY -= 6f
    }

    private fun writeTableRow(cells: List<String>, columnWidths: List<Float>, isHeader: Boolean) {
        val font = if (isHeader) PDType1Font.HELVETICA_BOLD else PDType1Font.HELVETICA
        val fontSize = if (isHeader) 7.5f else 7f
        val lineHeight = if (isHeader) 9f else 8.5f
        val padding = 3f
        val cellLines = columnWidths.indices.map { columnIndex ->
            wrap(
                sanitizePdfText(cleanMarkdown(cells.getOrElse(columnIndex) { "" }), font),
                font,
                fontSize,
                columnWidths[columnIndex] - padding * 2,
            ).ifEmpty { listOf("") }
        }
        val rowHeight = cellLines.maxOf { it.size } * lineHeight + padding * 2
        if (cursorY - rowHeight < margin) startPage()

        var x = margin
        cellLines.forEachIndexed { columnIndex, lines ->
            val columnWidth = columnWidths[columnIndex]
            stream!!.addRect(x, cursorY - rowHeight, columnWidth, rowHeight)
            stream!!.stroke()
            lines.forEachIndexed { lineIndex, value ->
                stream!!.beginText()
                stream!!.setFont(font, fontSize)
                stream!!.newLineAtOffset(x + padding, cursorY - padding - fontSize - lineIndex * lineHeight)
                stream!!.showText(value)
                stream!!.endText()
            }
            x += columnWidth
        }
        cursorY -= rowHeight
    }

    private fun tableColumnWidths(columnCount: Int): List<Float> {
        val availableWidth = pageWidth - margin * 2
        val weights = if (columnCount == 8) {
            listOf(0.7f, 1.5f, 1.1f, 0.85f, 1.1f, 0.6f, 0.8f, 1.8f)
        } else {
            List(columnCount) { 1f }
        }
        val totalWeight = weights.sum()
        return weights.map { availableWidth * it / totalWeight }
    }
}

private data class PdfMarkdownTable(
    val headers: List<String>,
    val rows: List<List<String>>,
    val nextLineIndex: Int,
)

private fun parseMarkdownTable(lines: List<String>, startIndex: Int): PdfMarkdownTable? {
    if (startIndex + 1 >= lines.size || !lines[startIndex].contains("|") || !lines[startIndex + 1].trim().matches(pdfTableSeparatorRegex)) {
        return null
    }
    var index = startIndex + 2
    val rows = mutableListOf<List<String>>()
    while (index < lines.size && lines[index].contains("|")) {
        rows += parsePdfTableCells(lines[index])
        index++
    }
    return PdfMarkdownTable(
        headers = parsePdfTableCells(lines[startIndex]),
        rows = rows,
        nextLineIndex = index,
    )
}

private fun parsePdfTableCells(line: String): List<String> = line
    .trim()
    .removePrefix("|")
    .removeSuffix("|")
    .split("|")
    .map { it.trim() }

private val pdfTableSeparatorRegex = Regex("""^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$""")

private fun wrap(text: String, font: PDType1Font, fontSize: Float, maxWidth: Float): List<String> {
    if (text.isEmpty()) return emptyList()
    val lines = mutableListOf<String>()
    var current = ""
    text.split(Regex("(?<=\\s)")).forEach { word ->
        val candidate = current + word
        if (current.isNotEmpty() && font.getStringWidth(candidate) / 1000f * fontSize > maxWidth) {
            lines += current.trimEnd()
            current = word.trimStart()
        } else {
            current = candidate
        }
    }
    if (current.isNotBlank()) lines += current.trimEnd()
    return lines
}

private fun cleanMarkdown(line: String): String {
    return line
        .replace(Regex("^\\s*[-*+]\\s+"), "- ")
        .replace(Regex("^\\s*\\d+[.)]\\s+"), "")
        .replace(Regex("!?(\\[[^]]*])\\(([^)]+)\\)"), "$1")
        .replace("**", "")
        .replace("__", "")
        .replace("`", "")
        .replace("*", "")
        .replace("_", "")
        .replace("\t", "    ")
        .trim()
}

internal fun sanitizePdfText(text: String, font: PDType1Font = PDType1Font.HELVETICA): String {
    return buildString {
        text.codePoints().forEach { codePoint ->
            val character = String(Character.toChars(codePoint))
            append(if (runCatching { font.encode(character) }.isSuccess) character else "?")
        }
    }
}

private class MarkdownPreviewPrintable(
    private val blocks: List<MarkdownPreviewBlock>,
) : Printable {
    override fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
        val pages = layoutPreview(pageFormat)
        val page = pages.getOrNull(pageIndex) ?: return Printable.NO_SUCH_PAGE
        val canvas = graphics as Graphics2D
        canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        page.forEach { operation -> operation.draw(canvas) }
        return Printable.PAGE_EXISTS
    }

    private fun layoutPreview(pageFormat: PageFormat): List<List<PreviewPrintOperation>> {
        val measureGraphics = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        return try {
            PreviewPrintLayout(
                blocks = blocks,
                graphics = measureGraphics,
                left = pageFormat.imageableX.toFloat() + 8f,
                top = pageFormat.imageableY.toFloat() + 8f,
                width = pageFormat.imageableWidth.toFloat() - 16f,
                bottom = (pageFormat.imageableY + pageFormat.imageableHeight - 8).toFloat(),
            ).build()
        } finally {
            measureGraphics.dispose()
        }
    }
}

private class PreviewPrintLayout(
    private val blocks: List<MarkdownPreviewBlock>,
    private val graphics: Graphics2D,
    private val left: Float,
    private val top: Float,
    private val width: Float,
    private val bottom: Float,
) {
    private val pages = mutableListOf(mutableListOf<PreviewPrintOperation>())
    private var y = top

    fun build(): List<List<PreviewPrintOperation>> {
        blocks.forEachIndexed { index, block ->
            if (index > 0) addSpace(14f)
            when (block) {
                is MarkdownPreviewBlock.Heading -> addHeading(block)
                is MarkdownPreviewBlock.Paragraph -> addParagraph(block.text)
                is MarkdownPreviewBlock.UnorderedList -> block.items.forEach { addParagraph("• ${previewText(it)}", indent = 16f) }
                is MarkdownPreviewBlock.OrderedList -> block.items.forEachIndexed { itemIndex, item ->
                    addParagraph("${itemIndex + 1}. ${previewText(item)}", indent = 16f)
                }
                is MarkdownPreviewBlock.CodeFence -> addCode(block)
                is MarkdownPreviewBlock.BlockQuote -> addQuote(block.text)
                is MarkdownPreviewBlock.Table -> addTable(block)
                MarkdownPreviewBlock.HorizontalRule -> addRule()
            }
        }
        return pages.filter { it.isNotEmpty() }.ifEmpty { listOf(emptyList()) }
    }

    private fun addHeading(block: MarkdownPreviewBlock.Heading) {
        val size = when (block.level) { 1 -> 22f; 2 -> 17f; 3 -> 15f; else -> 13f }
        addText(previewText(block.text), Font(Font.SANS_SERIF, Font.BOLD, size.toInt()), Color(35, 35, 39), 0f, size + 7f)
        if (block.level <= 2) {
            ensure(8f)
            pages.last() += PreviewPrintOperation.Rule(left, y + 2f, width, Color(190, 187, 195))
            y += 8f
        }
    }

    private fun addParagraph(text: String, indent: Float = 0f) {
        addText(previewText(text), Font(Font.SANS_SERIF, Font.PLAIN, 12), Color(38, 37, 42), indent, 17f)
    }

    private fun addCode(block: MarkdownPreviewBlock.CodeFence) {
        if (!block.language.isNullOrBlank()) {
            addText(block.language.lowercase(), Font(Font.SANS_SERIF, Font.BOLD, 9), Color(94, 90, 101), 8f, 13f)
        }
        val font = Font(Font.MONOSPACED, Font.PLAIN, 10)
        block.code.lines().ifEmpty { listOf("") }.forEach { line ->
            addText(line, font, Color(46, 45, 52), 8f, 14f, background = Color(241, 239, 244))
        }
    }

    private fun addQuote(text: String) {
        val startY = y
        addText(previewText(text), Font(Font.SERIF, Font.ITALIC, 12), Color(88, 84, 94), 14f, 17f)
        pages.last() += PreviewPrintOperation.QuoteBar(left, startY - 10f, (y - startY).coerceAtLeast(16f), Color(150, 145, 157))
    }

    private fun addTable(table: MarkdownPreviewBlock.Table) {
        addText(table.headers.joinToString("  |  ") { previewText(it) }, Font(Font.SANS_SERIF, Font.BOLD, 10), Color(45, 43, 49), 4f, 15f, background = Color(235, 233, 239))
        table.rows.forEach { row ->
            addText(row.joinToString("  |  ") { previewText(it) }, Font(Font.SANS_SERIF, Font.PLAIN, 10), Color(45, 43, 49), 4f, 15f)
        }
    }

    private fun addRule() {
        ensure(10f)
        pages.last() += PreviewPrintOperation.Rule(left, y + 2f, width, Color(196, 192, 201))
        y += 10f
    }

    private fun addText(
        text: String,
        font: Font,
        color: Color,
        indent: Float,
        lineHeight: Float,
        background: Color? = null,
    ) {
        val lines = wrapForPrint(text, font, width - indent)
        lines.ifEmpty { listOf("") }.forEach { line ->
            ensure(lineHeight)
            background?.let { pages.last() += PreviewPrintOperation.Background(left + indent - 4f, y - lineHeight + 3f, width - indent + 4f, lineHeight, it) }
            pages.last() += PreviewPrintOperation.Text(left + indent, y, line, font, color)
            y += lineHeight
        }
    }

    private fun addSpace(amount: Float) {
        ensure(amount)
        y += amount
    }

    private fun ensure(height: Float) {
        if (y + height <= bottom) return
        pages.add(mutableListOf())
        y = top
    }

    private fun wrapForPrint(text: String, font: Font, maxWidth: Float): List<String> {
        if (text.isEmpty()) return emptyList()
        val metrics = graphics.getFontMetrics(font)
        val lines = mutableListOf<String>()
        var current = ""
        text.split(Regex("(?<=\\s)")).forEach { word ->
            val candidate = current + word
            if (current.isNotEmpty() && metrics.stringWidth(candidate) > maxWidth) {
                lines += current.trimEnd()
                current = word.trimStart()
            } else {
                current = candidate
            }
        }
        if (current.isNotBlank()) lines += current.trimEnd()
        return lines
    }
}

private sealed interface PreviewPrintOperation {
    fun draw(graphics: Graphics2D)

    data class Text(val x: Float, val y: Float, val value: String, val font: Font, val color: Color) : PreviewPrintOperation {
        override fun draw(graphics: Graphics2D) {
            graphics.font = font
            graphics.color = color
            graphics.drawString(value, x, y)
        }
    }

    data class Rule(val x: Float, val y: Float, val width: Float, val color: Color) : PreviewPrintOperation {
        override fun draw(graphics: Graphics2D) {
            graphics.color = color
            graphics.drawLine(x.toInt(), y.toInt(), (x + width).toInt(), y.toInt())
        }
    }

    data class Background(val x: Float, val y: Float, val width: Float, val height: Float, val color: Color) : PreviewPrintOperation {
        override fun draw(graphics: Graphics2D) {
            graphics.color = color
            graphics.fillRect(x.toInt(), y.toInt(), width.toInt(), height.toInt())
        }
    }

    data class QuoteBar(val x: Float, val y: Float, val height: Float, val color: Color) : PreviewPrintOperation {
        override fun draw(graphics: Graphics2D) {
            graphics.color = color
            graphics.fillRect(x.toInt(), y.toInt(), 3, height.toInt())
        }
    }
}

private fun previewText(value: String): String {
    return value
        .replace(Regex("\\[([^]]+)]\\(https?://[^)]+\\)"), "$1")
        .replace("**", "")
        .replace("__", "")
        .replace("~~", "")
        .replace("`", "")
        .replace(Regex("(?<!\\*)\\*([^*]+)\\*(?!\\*)"), "$1")
        .replace(Regex("(?<!_)_([^_]+)_(?!_)"), "$1")
}

private fun String.isMarkdownFileName(): Boolean {
    return endsWith(".md", ignoreCase = true) || endsWith(".markdown", ignoreCase = true)
}

private fun String.ensureMarkdownFileName(): String {
    if (isMarkdownFileName()) return this
    return "$this.md"
}

private fun String.ensurePdfFileName(): String {
    return replace(Regex("\\.(md|markdown)$", RegexOption.IGNORE_CASE), "").let { base ->
        if (base.endsWith(".pdf", ignoreCase = true)) base else "$base.pdf"
    }
}
