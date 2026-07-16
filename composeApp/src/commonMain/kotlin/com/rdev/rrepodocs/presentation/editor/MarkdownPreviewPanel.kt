package com.rdev.rrepodocs.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdev.rrepodocs.domain.model.MarkdownPreviewBlock
import com.rdev.rrepodocs.domain.usecase.RenderMarkdownPreviewUseCase
import com.rdev.rrepodocs.presentation.app.AppThemeTokens

@Composable
fun MarkdownPreviewPanel(
    markdown: String,
    showTitle: Boolean = true,
    onNavigateToSource: ((Int) -> Unit)? = null,
    sourceScrollProgress: Float? = null,
    modifier: Modifier = Modifier,
) {
    val renderer = remember { RenderMarkdownPreviewUseCase() }
    val blocks = remember(markdown) { renderer(markdown) }
    val previewScrollState = rememberScrollState()

    LaunchedEffect(sourceScrollProgress, previewScrollState.maxValue) {
        sourceScrollProgress?.let { progress ->
            previewScrollState.scrollTo((previewScrollState.maxValue * progress).toInt())
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .verticalScroll(previewScrollState)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                if (showTitle) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (markdown.isBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "No preview content yet.\nStart writing Markdown in the editor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }
                if (blocks.isNotEmpty()) {
                    blocks.forEach { block ->
                        MarkdownPreviewBlockView(
                            block = block,
                            onClick = block.sourceOffsetOrNull()?.let { sourceOffset ->
                                onNavigateToSource?.let { { it(sourceOffset) } }
                            },
                        )
                    }
                } else if (markdown.isNotBlank()) {
                    Text(
                        text = "Rendering preview...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkdownPreviewBlockView(
    block: MarkdownPreviewBlock,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    when (block) {
        is MarkdownPreviewBlock.Heading -> {
            val textStyle = when (block.level) {
                1 -> MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold)
                2 -> MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
                3 -> MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium)
                4 -> MaterialTheme.typography.headlineMedium.copy(lineHeight = 30.sp, fontWeight = FontWeight.Medium)
                5 -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, lineHeight = 28.sp)
                else -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, lineHeight = 28.sp)
            }
            Column(
                modifier = clickableModifier,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MarkdownLinkText(
                    text = block.text,
                    style = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.padding(top = if (block.level <= 2) 8.dp else 2.dp),
                )
                if (block.level <= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
                    )
                }
            }
        }

        is MarkdownPreviewBlock.Paragraph -> {
            MarkdownLinkText(
                text = block.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 29.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = clickableModifier,
            )
        }

        is MarkdownPreviewBlock.UnorderedList -> {
            Column(
                modifier = clickableModifier.padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                block.items.forEach { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        MarkdownLinkText(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 17.sp,
                                lineHeight = 29.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        is MarkdownPreviewBlock.OrderedList -> {
            Column(
                modifier = clickableModifier.padding(start = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                block.items.forEachIndexed { index, item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "${block.startNumber + index}.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        MarkdownLinkText(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 17.sp,
                                lineHeight = 29.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        is MarkdownPreviewBlock.CodeFence -> {
            Column(
                modifier = clickableModifier,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (!block.language.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = block.language.lowercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
                Text(
                    text = block.code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(10.dp),
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.54f),
                            shape = RoundedCornerShape(10.dp),
                        )
                        .padding(AppThemeTokens.spacing.md),
                )
            }
        }

        is MarkdownPreviewBlock.BlockQuote -> {
            Row(
                modifier = clickableModifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(999.dp)),
                )
                MarkdownLinkText(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 31.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        is MarkdownPreviewBlock.Table -> {
            val scrollState = rememberScrollState()
            Column(
                modifier = clickableModifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                    ),
            ) {
                MarkdownPreviewTableRow(
                    cells = block.headers,
                    header = true,
                )
                block.rows.forEach { row ->
                    MarkdownPreviewTableRow(
                        cells = row,
                        header = false,
                    )
                }
            }
        }

        MarkdownPreviewBlock.HorizontalRule -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            )
        }
    }
}

private fun MarkdownPreviewBlock.sourceOffsetOrNull(): Int? = when (this) {
    is MarkdownPreviewBlock.Heading -> sourceOffset
    is MarkdownPreviewBlock.Paragraph -> sourceOffset
    is MarkdownPreviewBlock.UnorderedList -> sourceOffset
    is MarkdownPreviewBlock.OrderedList -> sourceOffset
    is MarkdownPreviewBlock.CodeFence -> sourceOffset
    is MarkdownPreviewBlock.BlockQuote -> sourceOffset
    is MarkdownPreviewBlock.Table -> sourceOffset
    MarkdownPreviewBlock.HorizontalRule -> null
}

@Composable
private fun MarkdownPreviewTableRow(
    cells: List<String>,
    header: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (header) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                },
                shape = RoundedCornerShape(0.dp),
            ),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cell,
                style = if (header) {
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                } else {
                    MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                },
                modifier = Modifier
                    .width(210.dp)
                    .then(
                        if (index < cells.lastIndex) {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun MarkdownLinkText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f)
    val annotated = remember(text, linkColor, codeBackground) {
        buildMarkdownAnnotatedString(
            text = text,
            linkColor = linkColor,
            inlineCodeBackground = codeBackground,
        )
    }
    Text(
        text = annotated,
        style = style,
        modifier = modifier,
    )
}

internal fun buildMarkdownAnnotatedString(
    text: String,
    linkColor: Color,
    inlineCodeBackground: Color,
): AnnotatedString = buildAnnotatedString {
    var lastIndex = 0
    markdownInlineRegex.findAll(text).forEach { match ->
        append(text.substring(lastIndex, match.range.first))
        val token = match.value
        when {
            token.startsWith("[") -> {
                val linkMatch = linkRegex.matchEntire(token)
                if (linkMatch != null) {
                    val label = linkMatch.groupValues[1]
                    val url = linkMatch.groupValues[2]
                    withLink(
                        LinkAnnotation.Url(
                            url = url,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.None,
                                ),
                            ),
                        ),
                    ) {
                        append(label)
                    }
                } else {
                    append(token)
                }
            }

            token.startsWith("`") -> {
                val code = token.removePrefix("`").removeSuffix("`")
                val start = length
                append(code)
                addStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = inlineCodeBackground,
                    ),
                    start = start,
                    end = length,
                )
            }

            token.startsWith("***") || token.startsWith("___") -> {
                val boldItalic = token.drop(3).dropLast(3)
                val start = length
                append(boldItalic)
                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                    ),
                    start = start,
                    end = length,
                )
            }

            token.startsWith("**") || token.startsWith("__") -> {
                val bold = token.drop(2).dropLast(2)
                val start = length
                append(bold)
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = start,
                    end = length,
                )
            }

            token.startsWith("~~") -> {
                val strike = token.drop(2).dropLast(2)
                val start = length
                append(strike)
                addStyle(
                    style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                    start = start,
                    end = length,
                )
            }

            token.startsWith("*") || token.startsWith("_") -> {
                val italic = token.drop(1).dropLast(1)
                val start = length
                append(italic)
                addStyle(
                    style = SpanStyle(fontStyle = FontStyle.Italic),
                    start = start,
                    end = length,
                )
            }

            else -> append(token)
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

private val linkRegex = Regex("""\[([^\]]+)]\((https?://[^)]+)\)""")
private val markdownInlineRegex = Regex(
    """(\[[^\]]+]\(https?://[^)]+\)|`[^`]+`|\*\*\*[^*\n]+\*\*\*|___[^_\n]+___|\*\*[^*\n]+\*\*|__[^_\n]+__|~~[^~\n]+~~|\*[^*\n]+\*|_[^_\n]+_)""",
)
