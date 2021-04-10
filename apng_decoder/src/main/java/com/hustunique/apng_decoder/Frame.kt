package com.hustunique.apng_decoder

import android.graphics.Bitmap

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

data class Frame(
    val image: Bitmap,
    val options: FrameOptions
)

data class FrameOptions(
    val width: Int,
    val height: Int,
    val xOffset: Int,
    val yOffset: Int,
    val delayInMillis: Long,
    val disposeOp: Byte,
    val blendOp: Byte
) {
    init {
        check(disposeOp < 3) {
            "error png format(disposeOp out of range)"
        }
        check(blendOp < 2) {
            "error png format(blendOp out of range)"
        }
    }
}