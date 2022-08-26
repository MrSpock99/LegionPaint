package com.example.legionpaint

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class DrawPoint(
    val x: Float,
    val y: Float,
    val color: Int
): Parcelable