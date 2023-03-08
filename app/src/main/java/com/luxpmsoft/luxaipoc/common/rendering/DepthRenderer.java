package com.luxpmsoft.luxaipoc.common.rendering;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.ar.core.Camera;
import com.luxpmsoft.luxaipoc.rawdepth.DepthData;

import java.io.IOException;
import java.nio.FloatBuffer;

public class DepthRenderer {
    private static final String TAG = DepthRenderer.class.getSimpleName();

    // Shader names.
    private static final String VERTEX_SHADER_NAME = "shaders/depth_point_cloud.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/depth_point_cloud.frag";

    public static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * DepthData.FLOATS_PER_POINT;
    private static final int INITIAL_BUFFER_POINTS = 1000;

    private int arrayBuffer;
    private int arrayBufferSize;

    private int programName;
    private int positionAttribute;
    private int modelViewProjectionUniform;
    private int pointSizeUniform;

    private int numPoints = 0;

    public DepthRenderer() {}

    public void createOnGlThread(Context context) throws IOException {
        try {
            ShaderUtil.checkGLError(TAG, "Bind");

            int[] buffers = new int[1];
            GLES20.glGenBuffers(1, buffers, 0);
            arrayBuffer = buffers[0];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBuffer);

            arrayBufferSize = INITIAL_BUFFER_POINTS * BYTES_PER_POINT;
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arrayBufferSize, null, GLES20.GL_DYNAMIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            ShaderUtil.checkGLError(TAG, "Create");

            int vertexShader =
                    ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
            int fragmentShader =
                    ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);

            programName = GLES20.glCreateProgram();
            GLES20.glAttachShader(programName, vertexShader);
            GLES20.glAttachShader(programName, fragmentShader);
            GLES20.glLinkProgram(programName);
            GLES20.glUseProgram(programName);

            ShaderUtil.checkGLError(TAG, "Program");

            positionAttribute = GLES20.glGetAttribLocation(programName, "a_Position");
            modelViewProjectionUniform = GLES20.glGetUniformLocation(programName, "u_ModelViewProjection");
            // Sets the point size, in pixels.
            pointSizeUniform = GLES20.glGetUniformLocation(programName, "u_PointSize");

            ShaderUtil.checkGLError(TAG, "Init complete");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Update the OpenGL buffer contents to the provided point. Repeated calls with the same point
     * cloud will be ignored.
     */
    public void update(FloatBuffer points) {
        ShaderUtil.checkGLError(TAG, "Update");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBuffer);

        // If the array buffer is not large enough to fit the new point cloud, resize it.
        points.rewind();
        numPoints = points.remaining() / DepthData.FLOATS_PER_POINT;
        if (numPoints * BYTES_PER_POINT > arrayBufferSize) {
            while (numPoints * BYTES_PER_POINT > arrayBufferSize) {
                arrayBufferSize *= 2;
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arrayBufferSize, null, GLES20.GL_DYNAMIC_DRAW);
        }

        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, 0, numPoints * BYTES_PER_POINT, points);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        ShaderUtil.checkGLError(TAG, "Update complete");
    }

    /** Render the point cloud. The ARCore point cloud is given in world space. */
    public void draw(Camera camera) {
        float[] projectionMatrix = new float[16];
        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);
        float[] viewMatrix = new float[16];
        camera.getViewMatrix(viewMatrix, 0);
        float[] viewProjection = new float[16];
        Matrix.multiplyMM(viewProjection, 0, projectionMatrix, 0, viewMatrix, 0);

        ShaderUtil.checkGLError(TAG, "Draw");

        GLES20.glUseProgram(programName);
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBuffer);
        GLES20.glVertexAttribPointer(positionAttribute, 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, viewProjection, 0);
        // Set point size to 5 pixels.
        GLES20.glUniform1f(pointSizeUniform, 3.0f);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, numPoints);
        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        ShaderUtil.checkGLError(TAG, "Draw complete");
    }


}

