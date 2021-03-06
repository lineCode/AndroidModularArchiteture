package com.effective.android.base.view.list.listener

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup


import androidx.recyclerview.widget.RecyclerView

import com.effective.android.base.view.list.MediaList
import com.effective.android.base.view.list.MediaType
import com.effective.android.base.view.list.interfaces.GifManager
import com.effective.android.base.view.list.interfaces.LifeItem
import com.effective.android.base.view.list.interfaces.MediaItem
import com.effective.android.base.view.list.interfaces.VideoItem

import java.util.LinkedList

import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE

/**
 * 多媒体滑动事件
 * created by yummylau on 2018/4/16
 */
class MediaScrollListener(private val mMediaList: MediaList, val gifManager: GifManager) : RecyclerView.OnScrollListener() {
    private var selectedItem: MediaItem? = null

    private val attachMediaItems = LinkedList<MediaItem>()
    private val lifeItems = LinkedList<LifeItem>()


    init {
        this.mMediaList.addOnScrollListener(this)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        for (iLifeItem in lifeItems) {
            val isIdle = newState == SCROLL_STATE_IDLE
            iLifeItem.onScrollStatus(!isIdle)
        }

        if (recyclerView.scrollState == SCROLL_STATE_IDLE) {
            filterInValidItem(attachMediaItems)
            val validItems = calValidMediaItem(attachMediaItems)

            if (validItems.isEmpty()) {
                if (selectedItem != null) {
                    selectedItem!!.onUnselected()
                    selectedItem = null
                }
            } else {
                val containSelectedItem = containMediaItem(validItems, selectedItem)
                if (!containSelectedItem) {
                    unselectedItem(selectedItem)
                    selectedItem = validItems[0]
                    selectedItem(selectedItem)
                }
            }
        }

    }


    private fun hasAttachedVideo(): Boolean {
        return selectedItem != null && selectedItem!!.itemType() === MediaType.VIDEO
    }

    private fun isValidMedia(mediaItem: MediaItem): Boolean {
        return mediaItem.itemType() !== MediaType.INVALID
    }


    private fun filterInValidItem(attachHolders: LinkedList<MediaItem>?) {
        if (attachHolders == null || attachHolders.isEmpty()) {
            return
        }

        var i = 0
        while (i < attachHolders.size) {
            if (!isValidMedia(attachHolders[i])) {
                attachHolders.removeAt(i)
                i--
            }
            i++
        }
    }

    private fun unselectedItem(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            return
        }
        mediaItem.onUnselected()
    }

    private fun selectedItem(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            return
        }
        mediaItem.onSelected()
    }

    private fun containMediaItem(mediaItems: List<MediaItem>?, item: MediaItem?): Boolean {
        if (mediaItems == null || mediaItems.isEmpty() || item == null) {
            return false
        }

        for (mediaItem in mediaItems) {
            if (TextUtils.equals(mediaItem.itemKey(), item.itemKey())) {
                return true
            }
        }

        return false
    }


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
    }

    fun syncMediaListVisibilityChanged(visibility: Int) {

    }

    fun syncResume() {
        if (hasAttachedVideo()) {
            (selectedItem as VideoItem).onResume()
        }

        if (gifManager.attached()) {
            gifManager.onResume()
        }
    }

    fun syncPause() {
        if (hasAttachedVideo()) {
            (selectedItem as VideoItem).onPause()
        }

        if (gifManager.attached()) {
            gifManager.onPause()
        }
    }

    fun syncDestroy() {
        if (hasAttachedVideo()) {
            (selectedItem as VideoItem).onRelease()
        }
        gifManager.onRelease()
    }

    fun syncMediaListChildAttachedToWindow(child: View) {
        val holder = mMediaList.getChildViewHolder(child)
        if (holder is MediaItem) {
            attachMediaItems.addFirst(holder as MediaItem)
        }
        if (holder is LifeItem) {
            lifeItems.add(holder as LifeItem)
            (holder as LifeItem).onViewAttach()
        }
    }

    fun syncMediaListChildDetachedFromWindow(child: View) {
        val holder = mMediaList.getChildViewHolder(child)
        if (holder is MediaItem) {

            val mediaItem = holder as MediaItem

            if (selectedItem != null && TextUtils.equals(selectedItem!!.itemKey(), mediaItem.itemKey())) {
                unselectedItem(mediaItem)
                selectedItem = null
            }

            attachMediaItems.remove(mediaItem)
        }

        if (holder is LifeItem) {
            if (lifeItems.contains(holder)) {
                lifeItems.remove(holder)
            }
            (holder as LifeItem).onViewDetach()
        }
    }

    private fun calValidMediaItem(attachHolders: LinkedList<MediaItem>): List<MediaItem> {
        val result = LinkedList<MediaItem>()
        var bestWeight = 0f

        for (i in attachHolders.indices.reversed()) {
            val measureView = attachHolders[i].measureView() ?: continue
            val measuredHeight = measureView.measuredHeight
            val visibleHeight = getChildVisibleHeightInParent(measureView, mMediaList)

            if (visibleHeight * 2 >= measuredHeight) {
                val weight = visibleHeight / measuredHeight
                if (weight >= bestWeight) {
                    bestWeight = weight
                    result.addFirst(attachHolders[i])
                } else {
                    result.add(attachHolders[i])
                }
            }
        }
        return result
    }

    /**
     * getGlobalVisibleRect 有时会出错
     */
    private fun getChildVisibleHeightInParent(child: View?, root: View?): Float {
        if (child == null || root == null) {
            return 0f
        }
        var childVisibleTop = Math.max(0, child.top).toFloat()

        //递归到目标的根布局去计算child顶部到root的距离
        var childParent = child.parent
        while (childParent is ViewGroup && childParent !== root) {
            childVisibleTop += childParent.top.toFloat()
            childParent = childParent.getParent()
        }

        var childVisibleBottom = childVisibleTop + child.measuredHeight
        childVisibleBottom = Math.min(root.measuredHeight.toFloat(), childVisibleBottom)

        childVisibleTop = Math.max(0f, childVisibleTop)

        return childVisibleBottom - childVisibleTop
    }
}
