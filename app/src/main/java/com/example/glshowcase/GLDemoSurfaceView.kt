package com.example.glshowcase

import android.content.Context
import android.opengl.GLSurfaceView

class GLDemoSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: GLDemoRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = GLDemoRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}