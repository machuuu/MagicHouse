package com.example.magichouse

import android.content.Context
import android.opengl.GLES31
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
const val COORDS_PER_NORMAL = 3
const val COORD_PER_TEXTURE = 2

const val TriangleTag: String = "GLTriangle"
const val MagicCardTag: String = "GLMagicCard"

class Triangle {

    private var triangleCoords = floatArrayOf(     // in counterclockwise order:
        0.0f, 0.622008459f, 0.0f,      // top
        -0.5f, -0.311004243f, 0.0f,    // bottom left
        0.5f, -0.311004243f, 0.0f      // bottom right
    )

    private val vertexShaderCode =
        "#version 310 es\n" +
                "uniform mat4 uMVPMatrix;" +
                "in vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode =
        "#version 310 es\n" +
                "precision mediump float;" +
                "uniform vec4 vColor;" +
                "out vec4 fragColor;" +
                "void main() {" +
                "  fragColor = vColor;" +
                "}"

    private var m_Program: Int
    private var m_VAO: IntArray
    private var m_VBO: IntArray
    private val m_vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val m_vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val m_color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    // Use to access and set the view transformation
    //private var vPMatrixHandle: Int = 0

    init {
        val vertexShader: Int = loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        m_Program = GLES31.glCreateProgram().also {
            // add the vertex shader to program
            GLES31.glAttachShader(it, vertexShader)
            // add the fragment shader to program
            GLES31.glAttachShader(it, fragmentShader)
            // creates OpenGL ES program executables
            GLES31.glLinkProgram(it)
            printGlError(TriangleTag, "Error Shader Creating Program")
        }

        // Use 'apply' for VAO generation and binding
        m_VAO = IntArray(1).apply {
            GLES31.glGenVertexArrays(1, this, 0)
            GLES31.glBindVertexArray(this[0])
            printGlError(TriangleTag, "Error Generating VAO")
        }

        // Use 'apply' to generate VBO and bind vertex data
        m_VBO = IntArray(1).apply {
            GLES31.glGenBuffers(1, this, 0)
            GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, this[0])

            ByteBuffer.allocateDirect(triangleCoords.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(triangleCoords)
                    position(0)
                }.run {
                    // Transfer vertex data to GPU buffer
                    GLES31.glBufferData(
                        GLES31.GL_ARRAY_BUFFER,
                        triangleCoords.size * 4,
                        this,
                        GLES31.GL_STATIC_DRAW
                    )
                }
            printGlError(TriangleTag, "Error Generating VBO")

            // Specify the layout of the vertex data (position attribute 0)
            GLES31.glVertexAttribPointer(0, COORDS_PER_VERTEX, GLES31.GL_FLOAT, false, 0, 0)
            GLES31.glEnableVertexAttribArray(0)
            printGlError(TriangleTag, "Error Allocating VBO to VAO")
        }

        // Unbind when it has been setup
        GLES31.glBindVertexArray(0)
        printGlError(TriangleTag, "Error Initializing")
    }

    fun draw(mvpMatrix: FloatArray) {

        // Use shader program
        GLES31.glUseProgram(m_Program)

        // Bind vertex array that contains structure of vertex data for drawing
        GLES31.glBindVertexArray(m_VAO[0])

        // Add color to shader program
        GLES31.glUniform4fv(GLES31.glGetUniformLocation(m_Program, "vColor"), 1, m_color, 0)

        // Add MVP to shader program
        GLES31.glUniformMatrix4fv(GLES31.glGetUniformLocation(m_Program, "uMVPMatrix"), 1, false, mvpMatrix, 0)

        // Draw Arrays defined by the VAC
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, triangleCoords.size / COORDS_PER_VERTEX)

        // Remove active vertex array
        GLES31.glBindVertexArray(0)

        // Stop using program
        GLES31.glUseProgram(0)
    }
}

class Square {

    private var squareCoords = floatArrayOf(
        -0.5f,  0.5f, 0.0f,      // top left
        -0.5f, -0.5f, 0.0f,      // bottom left
        0.5f, -0.5f, 0.0f,      // bottom right
        0.5f,  0.5f, 0.0f       // top right
    )

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    // initialize vertex byte buffer for shape coordinates
    private val vertexBuffer: FloatBuffer =
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
                position(0)
            }
        }

    // initialize byte buffer for the draw list
    private val drawListBuffer: ShortBuffer =
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }
}

class MagicCard(private val context: Context)
{
    private var m_VBO: IntArray
    private var m_VAO: IntArray
    private var m_Program: Int

    private val m_Width = 63f       // mm
    private val m_Height = 88f      // mm
    private val m_Depth = 1f        // mm

    private val m_Color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    val m_Vertices = floatArrayOf(
        // positions               // normals           // texture coords
        -m_Width / 2, -m_Height / 2, -m_Depth / 2,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,
        m_Width / 2, -m_Height / 2, -m_Depth / 2,  0.0f,  0.0f, -1.0f,  1.0f,  0.0f,
        m_Width / 2,  m_Height / 2, -m_Depth / 2,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
        m_Width / 2,  m_Height / 2, -m_Depth / 2,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
        -m_Width / 2,  m_Height / 2, -m_Depth / 2,  0.0f,  0.0f, -1.0f,  0.0f,  1.0f,
        -m_Width / 2, -m_Height / 2, -m_Depth / 2,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,

        -m_Width / 2, -m_Height / 2,  m_Depth / 2,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,
        m_Width / 2, -m_Height / 2,  m_Depth / 2,  0.0f,  0.0f,  1.0f,  1.0f,  0.0f,
        m_Width / 2,  m_Height / 2,  m_Depth / 2,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
        m_Width / 2,  m_Height / 2,  m_Depth / 2,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
        -m_Width / 2,  m_Height / 2,  m_Depth / 2,  0.0f,  0.0f,  1.0f,  0.0f,  1.0f,
        -m_Width / 2, -m_Height / 2,  m_Depth / 2,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,

        -m_Width / 2,  m_Height / 2,  m_Depth / 2, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
        -m_Width / 2,  m_Height / 2, -m_Depth / 2, -1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
        -m_Width / 2, -m_Height / 2, -m_Depth / 2, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
        -m_Width / 2, -m_Height / 2, -m_Depth / 2, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
        -m_Width / 2, -m_Height / 2,  m_Depth / 2, -1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
        -m_Width / 2,  m_Height / 2,  m_Depth / 2, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,

        m_Width / 2,  m_Height / 2,  m_Depth / 2,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
        m_Width / 2,  m_Height / 2, -m_Depth / 2,  1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
        m_Width / 2, -m_Height / 2, -m_Depth / 2,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
        m_Width / 2, -m_Height / 2, -m_Depth / 2,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
        m_Width / 2, -m_Height / 2,  m_Depth / 2,  1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
        m_Width / 2,  m_Height / 2,  m_Depth / 2,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,

        -m_Width / 2, -m_Height / 2, -m_Depth / 2,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,
        m_Width / 2, -m_Height / 2, -m_Depth / 2,  0.0f, -1.0f,  0.0f,  1.0f,  1.0f,
        m_Width / 2, -m_Height / 2,  m_Depth / 2,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
        m_Width / 2, -m_Height / 2,  m_Depth / 2,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
        -m_Width / 2, -m_Height / 2,  m_Depth / 2,  0.0f, -1.0f,  0.0f,  0.0f,  0.0f,
        -m_Width / 2, -m_Height / 2, -m_Depth / 2,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,

        -m_Width / 2,  m_Height / 2, -m_Depth / 2,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f,
        m_Width / 2,  m_Height / 2, -m_Depth / 2,  0.0f,  1.0f,  0.0f,  1.0f,  1.0f,
        m_Width / 2,  m_Height / 2,  m_Depth / 2,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
        m_Width / 2,  m_Height / 2,  m_Depth / 2,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
        -m_Width / 2,  m_Height / 2,  m_Depth / 2,  0.0f,  1.0f,  0.0f,  0.0f,  0.0f,
        -m_Width / 2,  m_Height / 2, -m_Depth / 2,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f
    )


    init {

        val vertexShader: Int = loadShader(GLES31.GL_VERTEX_SHADER, loadShaderFromAssets(context, "magiccard_vertex.glsl"))
        val fragmentShader: Int = loadShader(GLES31.GL_FRAGMENT_SHADER, loadShaderFromAssets(context, "magiccard_fragment.glsl"))

        // create empty OpenGL ES Program
        m_Program = GLES31.glCreateProgram().also {
            // add the vertex shader to program
            GLES31.glAttachShader(it, vertexShader)
            // add the fragment shader to program
            GLES31.glAttachShader(it, fragmentShader)
            // creates OpenGL ES program executables
            GLES31.glLinkProgram(it)
            printGlError(MagicCardTag, "Error Shader Creating Program")
        }

        // Use 'apply' for VAO generation and binding
        m_VAO = IntArray(1).apply {
            GLES31.glGenVertexArrays(1, this, 0)
            GLES31.glBindVertexArray(this[0])
            printGlError(MagicCardTag, "Error Generating VAO")
        }

        // Use 'apply' to generate VBO and bind vertex data
        m_VBO = IntArray(1).apply {
            GLES31.glGenBuffers(1, this, 0)
            GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, this[0])

            ByteBuffer.allocateDirect(m_Vertices.size * 4) // 4 bytes in a float
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(m_Vertices)
                    position(0)
                }.run {
                    // Transfer vertex data to GPU buffer
                    GLES31.glBufferData(
                        GLES31.GL_ARRAY_BUFFER,
                        m_Vertices.size * 4,
                        this,
                        GLES31.GL_STATIC_DRAW
                    )
                }
            printGlError(MagicCardTag, "Error Generating VBO")

            // Specify the layout of the vertex data (position attribute 0)
            GLES31.glVertexAttribPointer(0, COORDS_PER_VERTEX, GLES31.GL_FLOAT, false, 8 * 4, 0 * 4) // 8 wide, start 0
            GLES31.glEnableVertexAttribArray(0)
            GLES31.glVertexAttribPointer(1, COORDS_PER_NORMAL, GLES31.GL_FLOAT, false, 8 * 4, 3 * 4) // 8 wide, start 3
            GLES31.glEnableVertexAttribArray(1)
            GLES31.glVertexAttribPointer(2, COORD_PER_TEXTURE, GLES31.GL_FLOAT, false, 8 * 4, 6 * 4) // 8 wide, start 6
            GLES31.glEnableVertexAttribArray(2)

            printGlError(MagicCardTag, "Error Allocating VBO to VAO")
        }

        // Unbind when it has been setup
        GLES31.glBindVertexArray(0)
        printGlError(MagicCardTag, "Error Initializing")
    }

    val m_TextureScale = FloatArray(4).apply {
        this[0] = 1.0f  // First row, first column
        this[1] = 0.0f  // First row, second column
        this[2] = 0.0f  // Second row, first column
        this[3] = 1.0f  // Second row, second column
    }
    val lightConstant: Float = 1.0f;
    val lightLinear: Float = 0.09f;
    val lightQuadratic: Float = 0.032f;
    val lightPos = floatArrayOf(0f, 0f, 0f)
    val lightAmbient = floatArrayOf(0.2f, 0.2f, 0.2f)
    val lightDiffuse = floatArrayOf(0.5f, 0.5f, 0.5f)
    val lightSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)
    val objectFrontColor = floatArrayOf(1.0f, 0.5f, 1.0f);
    val objectBackColor = floatArrayOf(0.0f, 1.0f, 1.0f);
    val objectShininess = 32.0f;
    val viewPos = floatArrayOf(0f, 0f, 0f)


    fun draw(model: FloatArray, view: FloatArray, projection: FloatArray) {

        // Use shader program
        GLES31.glUseProgram(m_Program)
        printGlError(MagicCardTag, "Use Program")

        // Bind vertex array that contains structure of vertex data for drawing
        GLES31.glBindVertexArray(m_VAO[0])
        printGlError(MagicCardTag, "Bind Vertex Array")

        // Add color to shader program
        //GLES31.glUniform4fv(GLES31.glGetUniformLocation(m_Program, "uColor"), 1, m_Color, 0)
        //printGlError(MagicCardTag, "Bind Vertex Array")

        // Add MVP to shader program
        GLES31.glUniformMatrix4fv(GLES31.glGetUniformLocation(m_Program, "uModel"), 1, false, model, 0)
        GLES31.glUniformMatrix4fv(GLES31.glGetUniformLocation(m_Program, "uView"), 1, false, view, 0)
        GLES31.glUniformMatrix4fv(GLES31.glGetUniformLocation(m_Program, "uProjection"), 1, false, projection, 0)
        //GLES31.glUniformMatrix2fv(GLES31.glGetUniformLocation(m_Program, "uTextureScale"), 1, false, m_TextureScale, 0)
        printGlError(MagicCardTag, "MVP Stuff")

        // View / Camera Pos
//        GLES31.glUniform3fv(GLES31.glGetUniformLocation(m_Program, "viewPos"), 1, viewPos, 0);
//        printGlError(MagicCardTag, "View POS")

        // Lighting Specification
        GLES31.glUniform3fv(GLES31.glGetUniformLocation(m_Program, "light.position"), 1, lightPos, 0);
        GLES31.glUniform3fv(GLES31.glGetUniformLocation(m_Program, "light.ambient"), 1, lightAmbient, 0);
        GLES31.glUniform3fv(GLES31.glGetUniformLocation(m_Program, "light.diffuse"), 1, lightDiffuse, 0);
        GLES31.glUniform3fv(GLES31.glGetUniformLocation(m_Program, "light.specular"), 1, lightSpecular, 0);
        GLES31.glUniform1f(GLES31.glGetUniformLocation(m_Program, "light.constant"), lightConstant)
        GLES31.glUniform1f(GLES31.glGetUniformLocation(m_Program, "light.linear"), lightLinear)
        GLES31.glUniform1f(GLES31.glGetUniformLocation(m_Program, "light.quadratic"), lightQuadratic)
        printGlError(MagicCardTag, "Light")

        // Material Information
        GLES31.glUniform3fv(GLES31.glGetUniformLocation(m_Program, "object.frontcolor"), 1, objectFrontColor, 0)
        GLES31.glUniform3fv(GLES31.glGetUniformLocation(m_Program, "object.backcolor"), 1, objectBackColor, 0)
        GLES31.glUniform1f(GLES31.glGetUniformLocation(m_Program, "object.shininess"), objectShininess)
        printGlError(MagicCardTag, "Object")

        // Draw Arrays defined by the VAC
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, m_Vertices.size / (COORDS_PER_VERTEX + COORDS_PER_NORMAL + COORD_PER_TEXTURE))
        printGlError(MagicCardTag, "Draw Arrays")

        // Remove active vertex array
        GLES31.glBindVertexArray(0)

        // Stop using program
        GLES31.glUseProgram(0)
    }

}