package com.rdev.rrepodocs.presentation.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rdev.rrepodocs.resources.Res
import com.rdev.rrepodocs.resources.inter_italic_variable
import com.rdev.rrepodocs.resources.inter_variable
import com.rdev.rrepodocs.resources.newsreader_italic_variable
import com.rdev.rrepodocs.resources.newsreader_variable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

private val LightColors = lightColorScheme(
    primary = Color(0xFF004AC6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF2563EB),
    onPrimaryContainer = Color(0xFFEEEFFF),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF1E293B),
    surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFDAE2FD),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFC3C6D7),
    secondary = Color(0xFF505F76),
    tertiary = Color(0xFF943700),
    error = Color(0xFFBA1A1A),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FA8C8),
    onPrimary = Color(0xFF101821),
    primaryContainer = Color(0xFF3E4B61),
    onPrimaryContainer = Color(0xFFE4E9F1),
    surface = Color(0xFF24292A),
    onSurface = Color(0xFFE5E8EA),
    surfaceContainerLow = Color(0xFF202627),
    surfaceContainerLowest = Color(0xFF2D3545),
    surfaceContainer = Color(0xFF505555),
    surfaceContainerHigh = Color(0xFF3A4148),
    surfaceContainerHighest = Color(0xFF46505E),
    onSurfaceVariant = Color(0xFFAFB5BB),
    outline = Color(0xFF353B3F),
    outlineVariant = Color(0xFF40474D),
    secondary = Color(0xFFD3E4FE),
    tertiary = Color(0xFFFFB596),
    error = Color(0xFFFFB4AB),
)

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun appTypography(): Typography {
    val uiFontFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.inter_variable, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.inter_variable, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.inter_italic_variable, FontWeight.Normal, FontStyle.Italic),
    )
    val editorialFontFamily = FontFamily(
        Font(Res.font.newsreader_variable, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.newsreader_variable, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.newsreader_variable, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.newsreader_italic_variable, FontWeight.Normal, FontStyle.Italic),
    )
    return remember(uiFontFamily, editorialFontFamily) {
        Typography(
            displayLarge = Typography().displayLarge.copy(
                fontFamily = editorialFontFamily,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            headlineLarge = Typography().headlineLarge.copy(
                fontFamily = editorialFontFamily,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            headlineMedium = Typography().headlineMedium.copy(
                fontFamily = editorialFontFamily,
                fontSize = 24.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Medium,
            ),
            titleLarge = Typography().titleLarge.copy(
                fontFamily = uiFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
            titleMedium = Typography().titleMedium.copy(
                fontFamily = uiFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
            bodyLarge = Typography().bodyLarge.copy(
                fontFamily = editorialFontFamily,
                fontSize = 18.sp,
                lineHeight = 31.sp,
            ),
            bodyMedium = Typography().bodyMedium.copy(
                fontFamily = uiFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            bodySmall = Typography().bodySmall.copy(
                fontFamily = uiFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
            labelMedium = Typography().labelMedium.copy(
                fontFamily = uiFontFamily,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Immutable
data class AppSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
)

private val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
@Immutable
data class AppColorTokens(
    val appBg: Color,
    val sidebarSurface: Color,
    val editorSurface: Color,
    val contextPaneSurface: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentBlueSubtle: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
)

private val LightAppColors = AppColorTokens(
    appBg = Color(0xFFF8FAFC),
    sidebarSurface = Color(0xFFF1F5F9),
    editorSurface = Color(0xFFFFFFFF),
    contextPaneSurface = Color(0xFFF8FAFC),
    borderSubtle = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF1E293B),
    textSecondary = Color(0xFF64748B),
    accentBlueSubtle = Color(0xFFEFF6FF),
    statusSuccess = Color(0xFF10B981),
    statusWarning = Color(0xFFF59E0B),
)

private val DarkAppColors = AppColorTokens(
    appBg = Color(0xFF24292A),
    sidebarSurface = Color(0xFF202627),
    editorSurface = Color(0xFF2D3545),
    contextPaneSurface = Color(0xFF505555),
    borderSubtle = Color(0xFF373F44),
    textPrimary = Color(0xFFE5E8EA),
    textSecondary = Color(0xFFAFB5BB),
    accentBlueSubtle = Color(0xFF39485E),
    statusSuccess = Color(0xFF34D399),
    statusWarning = Color(0xFFFBBF24),
)

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppThemeTokens {
    val spacing: AppSpacing
        @Composable get() = LocalAppSpacing.current
    val colors: AppColorTokens
        @Composable get() = LocalAppColors.current
}

@Composable
fun RRepoDocsTheme(content: @Composable () -> Unit) {
    val colors = DarkColors
    val appColors = DarkAppColors
    val typography = appTypography()
    CompositionLocalProvider(
        LocalAppSpacing provides AppSpacing(),
        LocalAppColors provides appColors,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = content,
        )
    }
}
