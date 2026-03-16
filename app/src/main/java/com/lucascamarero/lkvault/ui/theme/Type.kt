package com.lucascamarero.lkvault.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lucascamarero.lkvault.R

// HU-2: CONFIGURACIÓN DE TEMA Y DISEÑO BASE

// Roboto para texto en general
val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_bold)),
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_bold)),
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_bold)),
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_light)),
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_light)),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_light)),
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.5.sp
    )
)

// PlayFairDisplay para títulos y cabeceras (tipografía del logo)
val Typography2 = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_bold)),
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_bold)),
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_bold)),
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfairdisplay_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.5.sp
    )
)