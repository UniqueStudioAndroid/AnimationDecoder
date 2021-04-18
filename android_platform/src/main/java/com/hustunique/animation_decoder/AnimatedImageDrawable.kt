package com.hustunique.animation_decoder

import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import com.hustunique.animation_decoder.api.Frame
import com.hustunique.animation_decoder.apng.APngFrame
import com.hustunique.animation_decoder.apng.APngFrameOptions
import com.hustunique.animation_decoder.awebp.WebPFrame

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

    private var mAPngFrameList: List<WebPFrame<Bitmap>>? = null

    private var mCurIdx = 0

    private val mPaint = Paint()

    private var mUpdater = Runnable {
        if (mRunning) {
            invalidateSelf()
        }
    }

    private var mBitmap: Bitmap = bounds.let {
        Bitmap.createBitmap(
            if (it.width() > 0) it.width() else 1,
            if (it.height() > 0) it.height() else 1,
            Bitmap.Config.ARGB_8888
        )
    }

    private var mCanvas = Canvas(mBitmap)

    constructor(aPngFrameList: List<Frame<Bitmap>>) : this() {
        this.mAPngFrameList = aPngFrameList.map { it as WebPFrame<Bitmap> }
    }

    override fun draw(canvas: Canvas) {
        if (mStarting) {
            mStarting = false
            mRunning = true
        }
        if (bounds.width() != mBitmap.width || bounds.height() != mBitmap.height) {
            resetBitmap()
        }
//        if (mCurIdx < mAPngFrameList?.size ?: 0) {
            mAPngFrameList?.let {
                it[mCurIdx++ % it.size]
            }?.run {
                options.run {
//                    when (options.blendOp) {
//                        APngFrameOptions.APNG_FRAME_BLEND_OP_SOURCE -> {
//                            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
//                        }
//                        APngFrameOptions.APNG_FRAME_BLEND_OP_OVER -> {
                            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
//                        }
//                        else -> throw IllegalStateException()
//                    }
                    when (options.disposeOp) {
                        APngFrameOptions.APNG_FRAME_DISPOSE_OP_NONE -> {
                            mCanvas.drawBitmap(image, xOffsetF, yOffsetF, mPaint)
                            canvas.drawBitmap(mBitmap, 0f, 0f, null)
                        }
                        APngFrameOptions.APNG_FRAME_DISPOSE_OP_BACKGROUND -> {
                            mCanvas.drawBitmap(image, xOffsetF, yOffsetF, mPaint)
                            canvas.drawBitmap(mBitmap, 0f, 0f, null)
                            mCanvas.drawRect(
                                Rect(
                                    xOffset.toInt(),
                                    yOffset.toInt(),
                                    (xOffset + width).toInt(),
                                    (yOffset + height).toInt()
                                ), mPaint.apply {
                                    color = Color.TRANSPARENT
                                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                                })
                            if (mCurIdx == mAPngFrameList?.size) {
                                mCanvas.drawBitmap(image, xOffsetF, yOffsetF, null)
                            }
                            Log.i(TAG, "draw: dispose background")
                        }
                        APngFrameOptions.APNG_FRAME_DISPOSE_OP_PREVIOUS -> {
                            canvas.drawBitmap(mBitmap, 0f, 0f, null)
                            canvas.drawBitmap(image, xOffsetF, yOffsetF, mPaint)
                            if (mCurIdx == mAPngFrameList?.size) {
                                mCanvas.drawBitmap(image, xOffsetF, yOffsetF, null)
                            }
                        }
                        else -> throw IllegalStateException()
                    }
                    scheduleSelf(mUpdater, nextAnimationTime(delayInMillis.toLong()))
                    mPaint.xfermode = null
                }
            }
//        } else {
//            canvas.drawBitmap(mBitmap, 0f, 0f, null)
//            mRunning = false
//        }
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
        mCurIdx = 0
        mStarting = true
        invalidateSelf()
    }

    override fun stop() {
        mRunning = false
        mStarting = false
    }

    override fun isRunning(): Boolean = mRunning

    private fun resetBitmap(force: Boolean = false) {
        if (!force) {
            if (mBitmap.width == bounds.width() || mBitmap.height == bounds.height()) {
                return
            }
        }
        bounds.let {
            mBitmap = Bitmap.createBitmap(
                if (it.width() > 0) it.width() else 1,
                if (it.height() > 0) it.height() else 1,
                Bitmap.Config.ARGB_8888
            )
        }
        mCanvas = Canvas(mBitmap)
    }

    private fun nextAnimationTime(delayInMillis: Long = ANIMATION_GAP): Long =
        SystemClock.uptimeMillis() + delayInMillis
}