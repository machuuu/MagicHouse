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

    private lateinit var m_Triangle: Triangle
    private lateinit var m_Square: Square
    private lateinit var m_MagicCard: MagicCard

    @Volatile
    var angle: Float = 0f
        set(value) {
            field = value % 360
            if (field < 0) {
                field += 360
            }
            onAngleChanged(value)
        }

    private var thresholdReached = false

    fun onAngleChanged(newValue: Float) {
        // Normalize the angle to a range between 0 and 360 degrees
        val normalizedAngle = newValue % 360

        // Check if the angle has crossed the 270 degrees threshold
        if (normalizedAngle >= 270 && !thresholdReached) {
            // Trigger the resource switch when reaching 270 degrees
            switchResource()

            // Set thresholdReached to true to avoid repeated switching
            thresholdReached = true
        } else if (normalizedAngle < 270) {
            // Reset thresholdReached when we go below 270 degrees again
            thresholdReached = false
        }

        Log.e(MyGLRendererTag, "Angle changed to: $newValue (Normalized: $normalizedAngle)")
    }

    fun switchResource() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                // Perform the network request in the IO context
//                val (firstBitmap, secondBitmap) = performNetworkRequest()
//
//                // Switch back to the Main thread to update the UI
//                withContext(Dispatchers.Main) {
//                    updateUI(firstBitmap, secondBitmap)
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun performNetworkRequest(): Pair<Bitmap, Bitmap> {
        // Initiate Random Card
        return CardFetcher(context).GetRandomCard()
    }

    fun updateUI(first: Bitmap, second: Bitmap) {
        // Update the UI with the result of the network call
        m_MagicCard.update(first, second)

        Log.e(MyGLRendererTag,"Network result: ")
    }

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var aspectRatio: Float = 0f

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

        // Initiate the fun triangle ...
        m_Triangle = Triangle()
    }

    override fun onDrawFrame(unused: GL10) {

        // Redraw background color
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)
        GLES31.glClearColor(0.0f,0.0f,0.0f,1.0f)

        // Create Model Matrix
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -250f)
        Matrix.rotateM(modelMatrix,0, angle, 0f, 1f, 0f)

        // Create View Matrix
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(viewMatrix, 0, 0f,0f,0f,0f,0f,-1f, 0f, 1f, 0f)

        // Create Projection Matrix
        val projMatrix = FloatArray(16)
        Matrix.perspectiveM(projMatrix, 0, 45f, aspectRatio, 0.1f, 10_000f)

        // Construct the MVP Matrix
        //val mvpMatrix = FloatArray(16)
        //Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        //Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0)

        m_MagicCard.draw(model = modelMatrix, view = viewMatrix, projection = projMatrix)


//        // Redraw background color
//        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
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