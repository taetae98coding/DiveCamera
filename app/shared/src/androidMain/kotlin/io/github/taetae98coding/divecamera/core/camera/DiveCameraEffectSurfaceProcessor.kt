package io.github.taetae98coding.divecamera.core.camera

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraEffect
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import androidx.core.util.Consumer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.Executor

private const val TAG = "DiveEffectGL"

// CameraEffect는 추상 클래스라 프리뷰/녹화 대상에 SurfaceProcessor를 붙이는 서브클래스로 감싼다.
internal class DiveCameraEffect(
    processor: DiveCameraEffectSurfaceProcessor,
    executor: Executor,
    errorListener: Consumer<Throwable>,
) : CameraEffect(PREVIEW or VIDEO_CAPTURE, executor, processor, errorListener)

// 카메라 프레임(프리뷰 + 녹화)에 다이빙 색보정 매트릭스를 OpenGL 셰이더로 실시간 적용하는 프로세서.
// CameraEffect(PREVIEW | VIDEO_CAPTURE)의 SurfaceProcessor로 사용해 프리뷰에 보이는 그대로 녹화된다.
internal class DiveCameraEffectSurfaceProcessor : SurfaceProcessor {
    private val glThread = HandlerThread("DiveEffectGL").apply { start() }
    private val glHandler = Handler(glThread.looper)
    private val glExecutor = Executor { command -> glHandler.post(command) }

    @Volatile
    private var colorMatrix: FloatArray = IDENTITY_COLOR_MATRIX

    private var glContext: GlContext? = null
    private var inputSurfaceTexture: SurfaceTexture? = null
    private var inputTextureId: Int = 0
    private val outputSurfaces = mutableMapOf<SurfaceOutput, EGLSurface>()

    private val textureTransform = FloatArray(16)
    private val colorMatrixColumnMajor = FloatArray(16)
    private val colorOffset = FloatArray(4)

    fun setColorMatrix(matrix: FloatArray) {
        colorMatrix = matrix.copyOf()
    }

    override fun onInputSurface(request: SurfaceRequest) {
        glExecutor.execute {
            val context =
                ensureGlContext() ?: run {
                    request.willNotProvideSurface()
                    return@execute
                }

            val textureId = context.externalTextureId
            val surfaceTexture = SurfaceTexture(textureId)
            surfaceTexture.setDefaultBufferSize(request.resolution.width, request.resolution.height)
            surfaceTexture.setOnFrameAvailableListener({ glHandler.post { drawFrame(it) } }, glHandler)

            val surface = Surface(surfaceTexture)
            request.provideSurface(surface, glExecutor) {
                surfaceTexture.setOnFrameAvailableListener(null)
                surfaceTexture.release()
                surface.release()
                if (inputSurfaceTexture === surfaceTexture) {
                    inputSurfaceTexture = null
                }
            }

            inputSurfaceTexture = surfaceTexture
            inputTextureId = textureId
        }
    }

    override fun onOutputSurface(surfaceOutput: SurfaceOutput) {
        glExecutor.execute {
            val context =
                ensureGlContext() ?: run {
                    surfaceOutput.close()
                    return@execute
                }

            val surface =
                surfaceOutput.getSurface(glExecutor, Consumer { event -> onOutputSurfaceClosed(surfaceOutput, event) })
            val eglSurface = context.createWindowSurface(surface)
            if (eglSurface != null) {
                outputSurfaces[surfaceOutput] = eglSurface
            } else {
                surfaceOutput.close()
            }
        }
    }

    private fun onOutputSurfaceClosed(
        surfaceOutput: SurfaceOutput,
        @Suppress("UNUSED_PARAMETER") event: SurfaceOutput.Event,
    ) {
        glExecutor.execute {
            outputSurfaces.remove(surfaceOutput)?.let { glContext?.destroyWindowSurface(it) }
            surfaceOutput.close()
        }
    }

    private fun drawFrame(surfaceTexture: SurfaceTexture) {
        val context = glContext ?: return
        if (surfaceTexture !== inputSurfaceTexture) return

        try {
            surfaceTexture.updateTexImage()
        } catch (exception: RuntimeException) {
            Log.w(TAG, "updateTexImage failed", exception)
            return
        }
        surfaceTexture.getTransformMatrix(textureTransform)

        toColumnMajor(colorMatrix, colorMatrixColumnMajor, colorOffset)

        val iterator = outputSurfaces.entries.iterator()
        while (iterator.hasNext()) {
            val (surfaceOutput, eglSurface) = iterator.next()

            if (!context.makeCurrent(eglSurface)) {
                continue
            }

            // SurfaceOutput의 크롭/회전/미러링을 SurfaceTexture 변환에 합성한다.
            surfaceOutput.updateTransformMatrix(context.outputTransform, textureTransform)

            GLES20.glViewport(0, 0, surfaceOutput.size.width, surfaceOutput.size.height)
            context.draw(inputTextureId, context.outputTransform, colorMatrixColumnMajor, colorOffset)

            EGLExt.eglPresentationTimeANDROID(context.eglDisplay, eglSurface, surfaceTexture.timestamp)
            EGL14.eglSwapBuffers(context.eglDisplay, eglSurface)
        }
    }

    private fun ensureGlContext(): GlContext? {
        glContext?.let { return it }

        return runCatching { GlContext() }
            .onFailure { Log.e(TAG, "failed to init GL context", it) }
            .getOrNull()
            ?.also { glContext = it }
    }

    private fun release() {
        glContext?.let { context ->
            outputSurfaces.values.forEach { context.destroyWindowSurface(it) }
            outputSurfaces.clear()
            inputSurfaceTexture?.release()
            inputSurfaceTexture = null
            context.release()
        }
        glContext = null
    }

    // CameraEffect 해제 시 호출한다.
    fun close() {
        glExecutor.execute { release() }
        glThread.quitSafely()
    }

    // ColorMatrix(4x5, offset 0..255) → GLES2 column-major mat4 + 0..1 offset 으로 변환.
    private fun toColumnMajor(
        matrix: FloatArray,
        outMatrix: FloatArray,
        outOffset: FloatArray,
    ) {
        // 행(row-major) [0..3],[5..8],[10..13],[15..18] → 열 우선.
        val rows = intArrayOf(0, 5, 10, 15)
        for (col in 0 until 4) {
            for (row in 0 until 4) {
                outMatrix[col * 4 + row] = matrix[rows[row] + col]
            }
        }
        outOffset[0] = matrix[4] / 255F
        outOffset[1] = matrix[9] / 255F
        outOffset[2] = matrix[14] / 255F
        outOffset[3] = matrix[19] / 255F
    }

    companion object {
        val IDENTITY_COLOR_MATRIX =
            floatArrayOf(
                1F,
                0F,
                0F,
                0F,
                0F,
                0F,
                1F,
                0F,
                0F,
                0F,
                0F,
                0F,
                1F,
                0F,
                0F,
                0F,
                0F,
                0F,
                1F,
                0F,
            )
    }
}

// EGL/GLES2 상태를 캡슐화한다. GL 스레드에서만 접근한다.
private class GlContext {
    val eglDisplay: EGLDisplay
    private val eglConfig: EGLConfig
    private val eglContext: EGLContext
    private val tempEglSurface: EGLSurface

    val externalTextureId: Int
    val outputTransform = FloatArray(16)

    private val program: Int
    private val positionHandle: Int
    private val texCoordHandle: Int
    private val texTransformHandle: Int
    private val colorMatrixHandle: Int
    private val colorOffsetHandle: Int
    private val textureHandle: Int

    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer

    init {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        check(eglDisplay != EGL14.EGL_NO_DISPLAY) { "no EGL display" }
        val version = IntArray(2)
        check(EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) { "eglInitialize failed" }

        val configAttributes =
            intArrayOf(
                EGL14.EGL_RED_SIZE,
                8,
                EGL14.EGL_GREEN_SIZE,
                8,
                EGL14.EGL_BLUE_SIZE,
                8,
                EGL14.EGL_ALPHA_SIZE,
                8,
                EGL14.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,
                EGLExt.EGL_RECORDABLE_ANDROID,
                1,
                EGL14.EGL_NONE,
            )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        check(EGL14.eglChooseConfig(eglDisplay, configAttributes, 0, configs, 0, 1, numConfigs, 0) && numConfigs[0] > 0) {
            "eglChooseConfig failed"
        }
        eglConfig = configs[0]!!

        val contextAttributes = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttributes, 0)
        check(eglContext != EGL14.EGL_NO_CONTEXT) { "eglCreateContext failed" }

        val pbufferAttributes = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
        tempEglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, pbufferAttributes, 0)
        check(tempEglSurface != EGL14.EGL_NO_SURFACE) { "eglCreatePbufferSurface failed" }
        check(EGL14.eglMakeCurrent(eglDisplay, tempEglSurface, tempEglSurface, eglContext)) { "eglMakeCurrent failed" }

        program = createProgram()
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        texTransformHandle = GLES20.glGetUniformLocation(program, "uTexTransform")
        colorMatrixHandle = GLES20.glGetUniformLocation(program, "uColorMatrix")
        colorOffsetHandle = GLES20.glGetUniformLocation(program, "uColorOffset")
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")

        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        externalTextureId = textureIds[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, externalTextureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        vertexBuffer = createFloatBuffer(FULLSCREEN_VERTICES)
        texCoordBuffer = createFloatBuffer(TEX_COORDS)
    }

    fun createWindowSurface(surface: Surface): EGLSurface? {
        val eglSurface =
            runCatching {
                EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, intArrayOf(EGL14.EGL_NONE), 0)
            }.getOrNull()

        return eglSurface?.takeIf { it != EGL14.EGL_NO_SURFACE }
    }

    fun destroyWindowSurface(eglSurface: EGLSurface) {
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
    }

    fun makeCurrent(eglSurface: EGLSurface): Boolean = EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

    fun draw(
        externalTextureId: Int,
        texTransform: FloatArray,
        colorMatrixColumnMajor: FloatArray,
        colorOffset: FloatArray,
    ) {
        GLES20.glClearColor(0F, 0F, 0F, 1F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, externalTextureId)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glUniformMatrix4fv(texTransformHandle, 1, false, texTransform, 0)
        GLES20.glUniformMatrix4fv(colorMatrixHandle, 1, false, colorMatrixColumnMajor, 0)
        GLES20.glUniform4fv(colorOffsetHandle, 1, colorOffset, 0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    fun release() {
        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        GLES20.glDeleteProgram(program)
        GLES20.glDeleteTextures(1, intArrayOf(externalTextureId), 0)
        EGL14.eglDestroySurface(eglDisplay, tempEglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
    }

    private fun createProgram(): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        check(linkStatus[0] == GLES20.GL_TRUE) { "program link failed: ${GLES20.glGetProgramInfoLog(program)}" }

        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        return program
    }

    private fun compileShader(
        type: Int,
        source: String,
    ): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        check(compileStatus[0] == GLES20.GL_TRUE) { "shader compile failed: ${GLES20.glGetShaderInfoLog(shader)}" }
        return shader
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer =
        ByteBuffer
            .allocateDirect(data.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }

    private companion object {
        val FULLSCREEN_VERTICES = floatArrayOf(-1F, -1F, 1F, -1F, -1F, 1F, 1F, 1F)
        val TEX_COORDS = floatArrayOf(0F, 0F, 1F, 0F, 0F, 1F, 1F, 1F)

        const val VERTEX_SHADER =
            """
            attribute vec4 aPosition;
            attribute vec4 aTexCoord;
            uniform mat4 uTexTransform;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = (uTexTransform * aTexCoord).xy;
            }
            """

        const val FRAGMENT_SHADER =
            """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES uTexture;
            uniform mat4 uColorMatrix;
            uniform vec4 uColorOffset;
            varying vec2 vTexCoord;
            void main() {
                vec4 color = texture2D(uTexture, vTexCoord);
                vec4 corrected = uColorMatrix * color + uColorOffset;
                gl_FragColor = clamp(vec4(corrected.rgb, color.a), 0.0, 1.0);
            }
            """
    }
}
