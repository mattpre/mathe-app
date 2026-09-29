package ch.matheapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Lila = Color(0xFF7B61FF)
val LilaHell = Color(0xFFE9E4FF)
val Gelb = Color(0xFFFFC83D)
val Gruen = Color(0xFF2FB463)
val GruenHell = Color(0xFFD9F5E3)
val Rot = Color(0xFFFF5C5C)
val RotHell = Color(0xFFFFE0E0)
val Orange = Color(0xFFFF9F43)
val Tuerkis = Color(0xFF1FB5C9)
val Hintergrund = Color(0xFFFFF8E7)
val TextFarbe = Color(0xFF2D2A4A)

private val Farben = lightColorScheme(
    primary = Lila,
    onPrimary = Color.White,
    secondary = Gelb,
    onSecondary = TextFarbe,
    background = Hintergrund,
    onBackground = TextFarbe,
    surface = Color.White,
    onSurface = TextFarbe,
    error = Rot,
)

private val Schrift = Typography(
    displayLarge = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.ExtraBold),
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 18.sp),
)

@Composable
fun MatheAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Farben, typography = Schrift, content = content)
}
