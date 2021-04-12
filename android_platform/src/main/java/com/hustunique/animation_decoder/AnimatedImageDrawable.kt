package com.hustunique.animation_decoder

import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import com.hustunique.animation_decoder.apng.APngFrame
import com.hustunique.animation_decoder.api.Frame

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
        private const val ANIMATION_GAP = 200L
    }

    private var mStarting = false

    @Volatile
    private var mRunning = false

    private var mAPngFrameList: List<APngFrame<Bitmap>>? = null

    private var mCurIdx = 0

    private val mPaint = Paint()

    private var mUpdater = Runnable {
        if (mRunning) {
            invalidateSelf()
        }
    }

    constructor(aPngFrameList: List<Frame<Bitmap>>) : this() {
        this.mAPngFrameList = aPngFrameList.map { it as APngFrame<Bitmap> }
    }

    override fun draw(canvas: Canvas) {
        if (mStarting) {
            mStarting = false
            mRunning = true
        }
        mAPngFrameList?.let {
            it[mCurIdx++ % it.size]
        }?.run {
            options.run {
                canvas.drawBitmap(image, xOffsetF, yOffsetF, mPaint)
                scheduleSelf(mUpdater, nextAnimationTime(delayInMillis))
            }
        }
        Log.i(TAG, "draw: ${System.currentTimeMillis()}")
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun start() {
        mStarting = true
        invalidateSelf()
    }

    override fun stop() {
        mRunning = false
        mStarting = false
    }

    override fun isRunning(): Boolean = mRunning

    private fun nextAnimationTime(delayInMillis: Long = ANIMATION_GAP): Long =
        SystemClock.uptimeMillis() + delayInMillis
}