package com.example.magichouse

import android.opengl.GLES31
import android.util.Log

fun printGlError(tag: String, msg: String) {
    var error: Int
    while (GLES31.glGetError().also { error = it } != GLES31.GL_NO_ERROR) {
        Log.e(tag, "Tag: ${tag} \nMessage: ${msg} \nOpenGL Error: ${getGLErrorString(error)}")
        //throw RuntimeException("OpenGL Error encountered: ${getGLErrorString(error)}")
    }
}

fun getGLErrorString(error: Int): String {
    return when (error) {
        GLES31.GL_INVALID_ENUM -> "GL_INVALID_ENUM"
        GLES31.GL_INVALID_VALUE -> "GL_INVALID_VALUE"
        GLES31.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
        GLES31.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
        GLES31.GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
        else -> "Unknown error code: $error"
    }
}

fun printGLVersion(tag: String)
{
    // Create a minimum supported OpenGL ES context, then check:
    GLES31.glGetString(GLES31.GL_VERSION).also {
        Log.w(tag, "Version: $it")
    }
}

fun loadShader(type: Int, shaderCode: String): Int {
    return GLES31.glCreateShader(type).also { shader ->
        // add the source code to the shader and compile it
        GLES31.glShaderSource(shader, shaderCode)
        GLES31.glCompileShader(shader)
        printGlError("GLUtils", "Error Loading Shader")
    }
}
