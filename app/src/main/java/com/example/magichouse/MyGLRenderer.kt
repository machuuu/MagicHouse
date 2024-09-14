package com.example.magichouse

import android.content.Context
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10Ext

const val MyGLRendererTag: String = "MyGLRenderer" // There must be a better compile-time way ...

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var m_Triangle: Triangle
    private lateinit var m_Square: Square
    private lateinit var m_MagicCard: MagicCard

    @Volatile
    var angle: Float = 0f

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var aspectRatio: Float = 0f

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        printGLVersion(MyGLRendererTag)
        printGlError(MyGLRendererTag, "Surface Created Error")

        m_Triangle = Triangle()
        m_MagicCard = MagicCard(context)
        //m_Square = Square()
    }

    override fun onDrawFrame(unused: GL10) {

        // Redraw background color
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)

        // Create Model Matrix
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -250f)
        Matrix.rotateM(modelMatrix,0, 0f, 0f, 1f, 0f)

        // Create View Matrix
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(viewMatrix, 0, 0f,0f,0f,0f,0f,-1f, 0f, 1f, 0f)

        // Create Projection Matrix
        val projMatrix = FloatArray(16)
        Matrix.perspectiveM(projMatrix, 0, 45f, aspectRatio, 0.1f, 10_000f)

        // Construct the MVP Matrix
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0)

        m_MagicCard.draw(mvpMatrix)


//        // Redraw background color
//        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)
//
//        // Set the camera position (View matrix)
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
//
//        // Calculate the projection and view transformation
//        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
//
//        val scratch = FloatArray(16)
//
//        // Create a rotation for the triangle
//        // long time = SystemClock.uptimeMillis() % 4000L;
//        // float angle = 0.090f * ((int) time);
//        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)
//
//        // Combine the rotation matrix with the projection and camera view
//        // Note that the mvpMatrix factor *must be first* in order
//        // for the matrix multiplication product to be correct.
//        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)
//
//        // Draw shape
//        m_Triangle.draw(scratch)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES31.glViewport(0, 0, width, height)

        aspectRatio = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 3f, 7f)
    }


}