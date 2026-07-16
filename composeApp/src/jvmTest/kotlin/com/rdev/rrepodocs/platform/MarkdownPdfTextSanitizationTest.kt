package com.rdev.rrepodocs.platform

import java.io.File
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MarkdownPdfTextSanitizationTest {
    @Test
    fun preservesEnDashSupportedByThePdfFont() {
        assertEquals(
            "- 1–2 msk hackad persilja",
            sanitizePdfText("- 1–2 msk hackad persilja"),
        )
    }

    @Test
    fun replacesCharactersUnsupportedByThePdfFont() {
        assertEquals("Persilja ?", sanitizePdfText("Persilja 🌿"))
    }

    @Test
    fun exportsMarkdownTablesAsCellsInsteadOfRawMarkdownRows() {
        val output = File.createTempFile("rrepodocs-table-", ".pdf")
        try {
            writePdf(
                file = output,
                title = "Album",
                markdown = """
                    | Datum | Titel | Typ | Bolag | Katalognummer | Land | Format | Kommentar |
                    |---|---|---|---|---|---|---|---|
                    | 1955 | Jim Reeves Sings | Studioalbum | Abbott | LP-5001 | USA | Mono | Tidiga Abbott-inspelningar |
                    | 1956-02 | Singing Down the Lane | Studioalbum | RCA Victor | LPM-1256 | USA | Mono | |
                    | 1956-10 | Bimbo | Studio-/samlingsalbum | RCA Victor | LPM-1410 | USA | Mono | Bygger delvis på tidigare singlar |
                """.trimIndent(),
            )

            val text = PDDocument.load(output).use { PDFTextStripper().getText(it) }
            assertContains(text, "Jim Reeves Sings")
            assertContains(text, "Bimbo")
            assertContains(text, "Bygger delvis på")
            assertFalse(text.contains("|---"))
        } finally {
            output.delete()
        }
    }
}
