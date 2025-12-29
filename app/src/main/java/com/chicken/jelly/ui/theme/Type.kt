package com.chicken.jelly.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chicken.jelly.R

val RoadRage = FontFamily(Font(R.font.roadrage))

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = RoadRage,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = RoadRage,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RoadRage,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
)
