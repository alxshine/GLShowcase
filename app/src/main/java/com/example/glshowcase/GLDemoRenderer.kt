package com.example.glshowcase

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLDemoRenderer : GLSurfaceView.Renderer {
    private lateinit var mPyramid: Pyramid

    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val movementMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        mPyramid = Pyramid()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(p0: GL10?) {
        val scratch = FloatArray(16)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        Matrix.setRotateM(movementMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.translateM(movementMatrix, 0, 0f, -.5f, 0f)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 1.5f, -6f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, movementMatrix, 0)
        mPyramid.draw(scratch)
    }
}

fun loadShader(type: Int, shaderCode: String): Int {

    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    return GLES20.glCreateShader(type).also { shader ->

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }
}