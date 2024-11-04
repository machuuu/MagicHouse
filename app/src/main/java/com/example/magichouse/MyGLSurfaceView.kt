package com.example.magichouse

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOUCH_SCALE_FACTOR: Float = 180.0f / (320f * 2)

class MyGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer: MyGLRenderer = MyGLRenderer(context)
    private var previousX = 0f
    private var previousY = 0f
    private var thresholdReached = false
    private var angle = 0f
        set(value) {
            field = value % 360
            if (field < 0) field += 360
            onAngleChanged(field)
        }

    init {
        // Create an OpenGL ES 3.2 context
        setEGLContextClientVersion(3)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x: Float = e.x
        val y: Float = e.y

        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx: Float = x - previousX
                var dy: Float = y - previousY

                // Reverse direction of rotation above the mid-line
                if (y > height / 2) {
                    dx *= -1
                }

                // Reverse direction of rotation to the left of the mid-line
                if (x < width / 2) {
                    dy *= -1
                }

                angle += (dx + dy) * TOUCH_SCALE_FACTOR
                renderer.onAngleChanged(angle)
                requestRender()
            }
        }

        previousX = x
        previousY = y
        return true
    }

    fun updateCard(onComplete: () -> Unit) {
        switchResource(onComplete)
    }

    private fun onAngleChanged(newAngle: Float) {
        // do nothing
    }

    private fun switchResource(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (firstBitmap, secondBitmap) = performNetworkRequest()
                queueEvent {
                    updateUI(firstBitmap, secondBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    private fun performNetworkRequest(): Pair<Bitmap, Bitmap> {
        return CardFetcher(context).GetRandomCard()
    }

    private fun updateUI(first: Bitmap, second: Bitmap) {
        renderer.onCardUpdated(first, second)
        requestRender()
        Log.e("MyGLSurfaceView", "Network result updated UI.")
    }
}
