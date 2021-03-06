package com.hustunique.animation_decoder

import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import com.hustunique.animation_decoder.api.AnimatedImage
import com.hustunique.animation_decoder.api.FrameBlendOptions
import com.hustunique.animation_decoder.api.FrameDisposeOptions

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

    private var mAnimatedImage: AnimatedImage<Bitmap>? = null

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

    constructor(animatedImage: AnimatedImage<Bitmap>) : this() {
        this.mAnimatedImage = animatedImage
    }

    override fun draw(canvas: Canvas) {
        if (mStarting) {
            mStarting = false
            mRunning = true
        }
        if (bounds.width() != mBitmap.width || bounds.height() != mBitmap.height) {
            resetBitmap()
        }
        if (mAnimatedImage == null) {
            return
        }
        if (mAnimatedImage?.loop ?: 0 == 0
            || (mCurIdx / (mAnimatedImage?.frames?.size ?: 1)) < mAnimatedImage?.loop ?: 0
        ) {
            mAnimatedImage?.frames?.let {
                it[mCurIdx++ % it.size]
            }?.run {
                options?.run {
                    when (blendOp) {
                        FrameBlendOptions.BLEND_OP_SRC -> {
                            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                        }
                        FrameBlendOptions.BLEND_OP_SRC_OVER -> {
                            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                        }
                    }
                    when (disposeOp) {
                        FrameDisposeOptions.DISPOSE_OP_NONE -> {
                            mCanvas.drawBitmap(image, xOffsetF, yOffsetF, mPaint)
                            canvas.drawBitmap(mBitmap, 0f, 0f, null)
                        }
                        FrameDisposeOptions.DISPOSE_OP_BACKGROUND -> {
                            mCanvas.drawBitmap(image, xOffsetF, yOffsetF, mPaint)
                            canvas.drawBitmap(mBitmap, 0f, 0f, null)
                            mCanvas.drawRect(
                                Rect(
                                    xOffset,
                                    yOffset,
                                    xOffset + width,
                                    yOffset + height,
                                ), mPaint.apply {
                                    color = mAnimatedImage?.backgroundColor ?: Color.TRANSPARENT
                                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                                })
                            if (mCurIdx == mAnimatedImage?.frames?.size) {
                                mCanvas.drawBitmap(image, xOffsetF, yOffsetF, null)
                            }
                            Log.i(TAG, "draw: dispose background")
                        }
                        FrameDisposeOptions.DISPOSE_OP_PREVIOUS -> {
                            canvas.drawBitmap(mBitmap, 0f, 0f, null)
                            canvas.drawBitmap(image, xOffsetF, yOffsetF, mPaint)
                            if (mCurIdx == mAnimatedImage?.frames?.size) {
                                mCanvas.drawBitmap(image, xOffsetF, yOffsetF, null)
                            }
                        }
                    }
                    scheduleSelf(mUpdater, nextAnimationTime(delayInMillis))
                    mPaint.xfermode = null
                }
            }
        } else {
            canvas.drawBitmap(mBitmap, 0f, 0f, null)
            mRunning = false
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