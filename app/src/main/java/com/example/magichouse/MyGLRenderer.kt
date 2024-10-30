package com.example.magichouse

import android.content.Context
import android.graphics.Bitmap
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.compose.material3.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10Ext

const val MyGLRendererTag: String = "MyGLRenderer" // There must be a better compile-time way ...

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var m_MagicCard: MagicCard

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var aspectRatio: Float = 0f
    private var angle: Float = 0f;

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES31.glEnable(GLES31.GL_DEPTH_TEST)

        printGLVersion(MyGLRendererTag)
        printGlError(MyGLRendererTag, "Surface Created Error")

        // Initiate Random Card
        CardFetcher(context).apply {
            GetRandomCard().apply {
                m_MagicCard = MagicCard(context, first, second)
            }
        }
    }

    override fun onDrawFrame(unused: GL10) {

        // Redraw background color
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)
        GLES31.glClearColor(0.0f,0.0f,0.0f,1.0f)

        // Create Model Matrix
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -165f)
        Matrix.rotateM(modelMatrix,0, angle, 0f, 1f, 0f)

        // Create View Matrix
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(viewMatrix, 0, 0f,0f,0f,0f,0f,-1f, 0f, 1f, 0f)

        // Create Projection Matrix
        val projMatrix = FloatArray(16)
        Matrix.perspectiveM(projMatrix, 0, 45f, aspectRatio, 0.1f, 10_000f)

        m_MagicCard.draw(model = modelMatrix, view = viewMatrix, projection = projMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES31.glViewport(0, 0, width, height)
        aspectRatio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 3f, 7f)
    }

    fun onAngleChanged(updateAngle: Float) {
        angle = updateAngle
    }

    fun onCardUpdated(first: Bitmap, second: Bitmap) {
        m_MagicCard = MagicCard(context, first, second)
    }

}