package com.hustunique.apng_decoder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log

/**
 * Copyright (C) 2021 xiaoyuxuan
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

class AnimatedImageDrawable() : Drawable(), Animatable {

    companion object {
        private const val TAG = "AnimatedImageDrawable"
        private const val ANIMATION_GAP = 200
    }

    private var starting = false

    @Volatile
    private var running = false

    private var bitmapList: List<Bitmap>? = null

    private var curIdx = 0

    private var updater = Runnable {
        if (running) {
            invalidateSelf()
        }
    }


    constructor(bitmapList: List<Bitmap>) : this() {
        this.bitmapList = bitmapList
    }


    override fun draw(canvas: Canvas) {
        if (starting) {
            starting = false
            running = true
        }
        bitmapList?.let {
            it.get(curIdx++ % it.size).apply {
                canvas.drawBitmap(
                    this,
                    curIdx.toFloat() * 10 % width,
                    curIdx.toFloat() * 10 % height,
                    null
                )
            }
        }

        Log.i(TAG, "draw: ${System.currentTimeMillis()}")

        scheduleSelf(updater, nextAnimationTime())
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun start() {
        starting = true
        invalidateSelf()
    }

    override fun stop() {
        running = false
    }

    override fun isRunning(): Boolean = running

    private fun nextAnimationTime(): Long = SystemClock.uptimeMillis() + ANIMATION_GAP
}