/*
 * Created by Evgeniya Zemlyanaya (@zzemlyanaya), ZZen Studio
 *  * Copyright (c) 2021 . All rights reserved.
 */

package ru.zzenstudio.monochrome

import android.graphics.Color

fun getHex(colorCode: Int) = "#" + String.format("%08X", colorCode)

fun parseColorString(color: String): Int {
    var colorString = color
    val a: Int
    var r: Int
    val g: Int
    var b = 0
    if (colorString.startsWith("#")) {
        colorString = colorString.substring(1)
    }
    when {
        colorString.isEmpty() -> {
            r = 0
            a = 255
            g = 0
        }
        colorString.length <= 2 -> {
            a = 255
            r = 0
            b = colorString.toInt(16)
            g = 0
        }
        colorString.length == 3 -> {
            a = 255
            r = colorString.substring(0, 1).toInt(16)
            g = colorString.substring(1, 2).toInt(16)
            b = colorString.substring(2, 3).toInt(16)
        }
        colorString.length == 4 -> {
            a = 255
            r = colorString.substring(0, 2).toInt(16)
            g = r
            r = 0
            b = colorString.substring(2, 4).toInt(16)
        }
        colorString.length == 5 -> {
            a = 255
            r = colorString.substring(0, 1).toInt(16)
            g = colorString.substring(1, 3).toInt(16)
            b = colorString.substring(3, 5).toInt(16)
        }
        colorString.length == 6 -> {
            a = 255
            r = colorString.substring(0, 2).toInt(16)
            g = colorString.substring(2, 4).toInt(16)
            b = colorString.substring(4, 6).toInt(16)
        }
        colorString.length == 7 -> {
            a = colorString.substring(0, 1).toInt(16)
            r = colorString.substring(1, 3).toInt(16)
            g = colorString.substring(3, 5).toInt(16)
            b = colorString.substring(5, 7).toInt(16)
        }
        colorString.length == 8 -> {
            a = colorString.substring(0, 2).toInt(16)
            r = colorString.substring(2, 4).toInt(16)
            g = colorString.substring(4, 6).toInt(16)
            b = colorString.substring(6, 8).toInt(16)
        }
        else -> {
            b = -1
            g = -1
            r = -1
            a = -1
        }
    }
    return Color.argb(a, r, g, b)
}