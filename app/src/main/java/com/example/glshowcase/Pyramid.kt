package com.example.glshowcase

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
var triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 1f, 0.0f,     // 0 - top
    -.5f, 0f, -.5f,     // 1 - front left
    -.5f, 0f, .5f,      // 2 - back left
    .5f, 0f, -.5f,      // 3 - front right
    .5f, 0f, .5f        // 4 - back right
)

class Pyramid {

    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
    private val edgeColor = floatArrayOf(0.4f, 0.5f, 0f, 1f)
    private val faceOrder = shortArrayOf(
        0, 1, 3,  // front
        0, 1, 2,  // left
        0, 2, 3,  // back
        0, 3, 4,  // right
        1, 2, 3,  // bottom first half
        2, 3, 4   // bottom second half
    )
    private val edgeOrder = shortArrayOf(
        0, 1,
        0, 2,
        0, 3,
        0, 4,
        1, 2,
        1, 3,
        2, 4,
        3, 4
    )

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(triangleCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private var faceBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(faceOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(faceOrder)
                position(0)
            }
        }

    private var edgeBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(edgeOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(edgeOrder)
                position(0)
            }
        }

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    private var mProgram: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }

    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var mvpMatrixHandle: Int = 0

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // projection matrix is same for edges and faces
        mvpMatrixHandle =
            GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
                GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
            }

        // get handle for position and color
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        // set buffer for drawing faces
        GLES20.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(positionHandle)

        // draw faces
        GLES20.glUniform4fv(colorHandle, 1, color, 0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            faceOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            faceBuffer
        )

        // draw edges
        GLES20.glUniform4fv(colorHandle, 1, edgeColor, 0)
        GLES20.glDrawElements(
            GLES20.GL_LINES,
            edgeOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            edgeBuffer
        )

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}