package com.luxpmsoft.luxaipoc.widget.decoder

interface IVideoFrameExtractor {
    fun onCurrentFrameExtracted(currentFrame: Frame, maxFrame: Int, decodeCount: Int)
    fun onAllFrameExtracted(processedFrameCount: Int, processedTimeMs: Long)
}