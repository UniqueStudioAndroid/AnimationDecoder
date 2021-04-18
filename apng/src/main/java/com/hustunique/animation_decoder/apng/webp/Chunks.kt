@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.hustunique.animation_decoder.apng.webp

import com.hustunique.animation_decoder.core.Readable
import com.hustunique.animation_decoder.core.asReadable
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Copyright (C) 2021 little-csd
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

open class BaseChunk(val buffer: ByteBuffer) {
    companion object;

    val type: UInt
        get() = readAt(0)

    val size: UInt
        get() = readAt(4)

    protected fun readAt(index: Int, len: Int = 4): UInt {
        val position = buffer.position()
        buffer.position(index)
        val value = when (len) {
            4 -> buffer.int.toUInt()
            3 -> buffer.short.toUInt() + (buffer.get().toUInt() shl 16)
            2 -> buffer.short.toUInt()
            else -> buffer.get().toUInt()
        }
        buffer.position(position)
        return value
    }

    internal fun asReadable() = object : Readable {
        private val readOnlyBuffer = buffer
            .duplicate()
            .asReadOnlyBuffer()
            .apply {
                rewind()
            }

        override fun read() = readOnlyBuffer.get()

        override fun available() = readOnlyBuffer.remaining()
    }

    override fun toString(): String = "Type: %s, Size: %d".format(
        String(
            ByteBuffer.allocate(4).putInt(type.toInt().reverseBytes()).array()
        ),
        size.toInt()
    )
}

class ANIMChunk(buffer: ByteBuffer) : BaseChunk(buffer) {
    val color: UInt
        get() = readAt(8)

    val loopCount: UInt
        get() = readAt(12, 2)
}

class ANMFChunk(buffer: ByteBuffer) : BaseChunk(buffer) {
    val frameX: UInt
        get() = readAt(8, 3) * 2U
    val frameY: UInt
        get() = readAt(11, 3) * 2U
    val width: UInt
        get() = readAt(14, 3) + 1U
    val height: UInt
        get() = readAt(17, 3) + 1U
    val duration: UInt
        get() = readAt(20, 3)
    val blendOp: Byte
        get() = ((readAt(23, 1) shr 1) and 0x1U).toByte()
    val disposeOp: Byte
        get() = (readAt(23, 1) and 0x1U).toByte()

    val frameData: BaseChunk
        get() = BaseChunk(
            ByteBuffer.wrap(
                buffer.array(),
                buffer.arrayOffset() + 24,
                buffer.capacity() - 24
            ).slice().order(ByteOrder.LITTLE_ENDIAN)
        )

    fun toFrameOptions() = WebPOptions(
        width,
        height,
        frameX,
        frameY,
        duration,
        disposeOp,
        blendOp
    )
}


internal fun BaseChunk.Companion.makeChunk(
    type: Int,
    buffer: ByteBuffer
) = when (type) {
    WebPChunkType.TYPE_ANIM -> ANIMChunk(buffer)
    WebPChunkType.TYPE_ANMF -> ANMFChunk(buffer)
    else -> BaseChunk(buffer)
}