package com.effective.android.video.core

import com.effective.android.video.bean.VideoStatus
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player

/**
 * 视图层通过该接口接受播放器信息
 * created by yummylau 2019/04/20
 */
interface VideoReceivingLayer {

    fun attachedControlLayer(controlLayer: VideoControlLayer)

    fun attachPlayer(player: Player)

    fun detachPlayer(player: Player)

    fun releaseControlLayer()

    fun onVideoStatus(status: VideoStatus?, message: String?)

    fun onBufferPosition(bufferedPosition: Long)

    fun onFirstInit()

    fun clickToPlay()

    fun clickToPause()
}
