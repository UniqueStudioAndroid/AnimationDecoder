package com.hustunique.animation_decoder.api

import kotlin.math.acos

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

class AnimatedImage<DT>(
    val loop: Int = 0,
    val backgroundColor: Int = 0x00000000
) {

    val frames = mutableListOf<Frame<DT>>()

    val onFrameCompletedAction = { frame: Frame<DT> ->
        waitList.remove(frame)
        frames.add(frame)
        if (waitList.isEmpty()) {
            onCompletedActions.forEach {
                it(this)
            }
            onCompletedActions.clear()
        }
    }

    private val waitList = mutableListOf<Frame<DT>>()

    private val onCompletedActions = mutableListOf<(AnimatedImage<DT>) -> Unit>()

    fun addFrame(frame: Frame<DT>) {
        frame.onCompletedActions.add(onFrameCompletedAction)
        waitList.add(frame)
    }

    fun onCompleted(action: (AnimatedImage<DT>) -> Unit) {
        onCompletedActions.add(action)
    }

}
