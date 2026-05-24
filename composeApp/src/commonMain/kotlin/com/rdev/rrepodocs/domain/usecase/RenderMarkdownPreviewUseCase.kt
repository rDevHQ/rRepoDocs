package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.domain.model.MarkdownPreviewBlock

class RenderMarkdownPreviewUseCase {
    operator fun invoke(markdown: String): List<MarkdownPreviewBlock> {
        val lines = markdown.replace("\r\n", "\n").split('\n')
        val blocks = mutableListOf<MarkdownPreviewBlock>()
        var index = 0

        while (index < lines.size) {
            val line = lines[index]
            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                index++
                continue
            }

            if (trimmed.startsWith("```")) {
                val language = trimmed.removePrefix("```").trim().ifEmpty { null }
                index++
                val codeLines = mutableListOf<String>()
                while (index < lines.size && !lines[index].trim().startsWith("```")) {
                    codeLines += lines[index]
                    index++
                }
                if (index < lines.size && lines[index].trim().startsWith("```")) {
                    index++
                }
                blocks += MarkdownPreviewBlock.CodeFence(
                    language = language,
                    code = codeLines.joinToString("\n"),
                )
                continue
            }

            val headingMatch = headingRegex.matchEntire(trimmed)
            if (headingMatch != null) {
                blocks += MarkdownPreviewBlock.Heading(
                    level = headingMatch.groupValues[1].length.coerceIn(1, 6),
                    text = headingMatch.groupValues[2].trim(),
                )
                index++
                continue
            }

            if (horizontalRuleRegex.matches(trimmed)) {
                blocks += MarkdownPreviewBlock.HorizontalRule
                index++
                continue
            }

            if (isTableHeader(lines, index)) {
                val header = parseTableCells(lines[index])
                val rows = mutableListOf<List<String>>()
                index += 2 // header + separator
                while (index < lines.size && lines[index].contains("|")) {
                    rows += parseTableCells(lines[index])
                    index++
                }
                blocks += MarkdownPreviewBlock.Table(headers = header, rows = rows)
                continue
            }

            if (trimmed.startsWith(">")) {
                val quoteLines = mutableListOf<String>()
                while (index < lines.size && lines[index].trim().startsWith(">")) {
                    quoteLines += lines[index].trim().removePrefix(">").trim()
                    index++
                }
                blocks += MarkdownPreviewBlock.BlockQuote(quoteLines.joinToString("\n"))
                continue
            }

            val unorderedMatch = unorderedRegex.matchEntire(trimmed)
            if (unorderedMatch != null) {
                val items = mutableListOf<String>()
                while (index < lines.size) {
                    val match = unorderedRegex.matchEntire(lines[index].trim()) ?: break
                    items += match.groupValues[1].trim()
                    index++
                }
                blocks += MarkdownPreviewBlock.UnorderedList(items)
                continue
            }

            val orderedMatch = orderedRegex.matchEntire(trimmed)
            if (orderedMatch != null) {
                val items = mutableListOf<String>()
                while (index < lines.size) {
                    val match = orderedRegex.matchEntire(lines[index].trim()) ?: break
                    items += match.groupValues[1].trim()
                    index++
                }
                blocks += MarkdownPreviewBlock.OrderedList(items)
                continue
            }

            val paragraphBuilder = StringBuilder()
            var previousLineBreakHard = false
            while (index < lines.size) {
                val paragraphLine = lines[index]
                val paragraphTrimmed = paragraphLine.trim()
                if (paragraphTrimmed.isBlank() || startsNewBlock(lines, index)) {
                    break
                }

                val trimmedEnd = paragraphLine.trimEnd()
                val hasBackslashBreak = trimmedEnd.endsWith("\\")
                val hasTwoSpaceBreak = paragraphLine.endsWith("  ")
                val currentLineHardBreak = hasBackslashBreak || hasTwoSpaceBreak

                val normalized = if (hasBackslashBreak) {
                    paragraphTrimmed.removeSuffix("\\").trimEnd()
                } else {
                    paragraphTrimmed
                }

                if (paragraphBuilder.isNotEmpty()) {
                    paragraphBuilder.append(if (previousLineBreakHard) "\n" else " ")
                }
                paragraphBuilder.append(normalized)
                previousLineBreakHard = currentLineHardBreak
                index++
            }
            blocks += MarkdownPreviewBlock.Paragraph(paragraphBuilder.toString())
        }

        return blocks
    }

    private fun startsNewBlock(lines: List<String>, index: Int): Boolean {
        val line = lines[index].trim()
        if (line.startsWith("```") || line.startsWith(">")) return true
        if (headingRegex.matches(line)) return true
        if (horizontalRuleRegex.matches(line)) return true
        if (unorderedRegex.matches(line) || orderedRegex.matches(line)) return true
        if (isTableHeader(lines, index)) return true
        return false
    }

    private fun parseTableCells(line: String): List<String> {
        val content = line.trim()
        val cells = content
            .removePrefix("|")
            .removeSuffix("|")
            .split("|")
            .map { cell -> cell.trim() }
        return cells
    }

    private fun isTableHeader(lines: List<String>, index: Int): Boolean {
        if (index + 1 >= lines.size) return false
        val header = lines[index]
        val separator = lines[index + 1].trim()
        if (!header.contains("|")) return false
        return separator.matches(tableSeparatorRegex)
    }

    private companion object {
        val headingRegex = Regex("""^(#{1,6})\s+(.+)$""")
        val unorderedRegex = Regex("""^[-*+]\s+(.+)$""")
        val orderedRegex = Regex("""^\d+[.)]\s+(.+)$""")
        val horizontalRuleRegex = Regex("""^(\*{3,}|-{3,}|_{3,})$""")
        val tableSeparatorRegex = Regex("""^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$""")
    }
}
