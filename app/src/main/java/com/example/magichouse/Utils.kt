package com.example.magichouse

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

fun loadShaderFromAssets(context: Context, fileName: String): String {
    val assetManager = context.assets
    val inputStream = assetManager.open("shaders/$fileName")

    val reader = BufferedReader(InputStreamReader(inputStream))
    val shaderCode = StringBuilder()

    var line: String? = reader.readLine()
    while (line != null) {
        shaderCode.append(line).append("\n")
        line = reader.readLine()
    }

    reader.close()
    return shaderCode.toString()
}