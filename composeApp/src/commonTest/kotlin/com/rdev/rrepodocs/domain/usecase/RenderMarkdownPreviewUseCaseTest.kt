package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.domain.model.MarkdownPreviewBlock
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderMarkdownPreviewUseCaseTest {
    @Test
    fun preservesOrderedListNumbersAcrossSeparatedListBlocks() {
        val blocks = RenderMarkdownPreviewUseCase()(
            """
            1. First step

            Details for the first step.

            2. Second step

            Details for the second step.
            """.trimIndent(),
        )

        assertEquals(
            listOf(
                MarkdownPreviewBlock.OrderedList(items = listOf("First step"), startNumber = 1),
                MarkdownPreviewBlock.OrderedList(items = listOf("Second step"), startNumber = 2),
            ),
            blocks.filterIsInstance<MarkdownPreviewBlock.OrderedList>()
                .map { it.copy(sourceOffset = 0) },
        )
    }
}
