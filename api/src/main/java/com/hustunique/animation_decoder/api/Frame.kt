package com.hustunique.animation_decoder.api


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

class Frame<DT>(
//    val image: DT,
    val options: FrameOptions?,
) {

    val completed: Boolean
        get() = imgWrapper != null


    val image: DT
        get() = imgWrapper!!

    val onCompletedActions: MutableList<(Frame<DT>) -> Unit> = mutableListOf()

    var imgWrapper: DT? = null
        set(value) {
            field = value
            if (value != null) {
                onCompletedActions.forEach {
                    it(this)
                }
                onCompletedActions.clear()
            }
        }
}

data class FrameOptions(
    val width: Int,
    val height: Int,
    val xOffset: Int,
    val yOffset: Int,
    val delayInMillis: Long,
    val disposeOp: FrameDisposeOptions,
    val blendOp: FrameBlendOptions,
) {
    val xOffsetF by lazy { xOffset.toFloat() }
    val yOffsetF by lazy { yOffset.toFloat() }
}

enum class FrameBlendOptions {

    BLEND_OP_SRC, BLEND_OP_SRC_OVER

}

enum class FrameDisposeOptions {

    DISPOSE_OP_NONE, DISPOSE_OP_BACKGROUND, DISPOSE_OP_PREVIOUS

}
