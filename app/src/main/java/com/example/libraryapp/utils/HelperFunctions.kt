package com.example.libraryapp.utils

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun randomColor(): Color {
    return Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
}