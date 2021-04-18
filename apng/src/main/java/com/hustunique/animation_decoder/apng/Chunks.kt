@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.hustunique.animation_decoder.apng

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

import com.hustunique.animation_decoder.api.FrameBlendOptions
import com.hustunique.animation_decoder.api.FrameDisposeOptions
import com.hustunique.animation_decoder.api.FrameOptions
import com.hustunique.animation_decoder.core.Readable
import com.hustunique.animation_decoder.core.asReadable
import com.hustunique.animation_decoder.core.exceptions.FormatException
import java.nio.ByteBuffer
import java.text.Format
import java.util.zip.CRC32

/**
 * Base Chunk for all chunk types
 * Consists of four parts (sequentially):
 * [dataLen], [type], actual data, [crc]
 */
open class BaseChunk(val buffer: ByteBuffer) {

    // We want to discard ';' here, but the stupid compiler complains about it
    companion object;

    open val dataLen: Int
        get() = readAt(0)

    open val type: Int
        get() = readAt(4)

    open val crc: Int
        get() = readAt(dataLen + 8)

    val chunkName: String
        get() = String(ByteBuffer.allocate(4).putInt(type).array())

    /**
     * Compute CRC for current chunk
     * Used for check whether [crc] is valid (if needed)
     */
    fun computeCRC(): Int {
        val crcEngine = CRC32()
        ByteBuffer.allocate(4).putInt(type).array().forEach {
            crcEngine.update(it.toInt())
        }
        crcEngine.update(buffer.array(), buffer.arrayOffset() + 8, dataLen)
        return crcEngine.value.toInt()
    }

    /**
     * Read byte/short/int from [buffer] at [index]
     *
     * @return data that cast to [Int]
     */
    protected fun readAt(index: Int, len: Int = 4): Int {
        val position = buffer.position()
        buffer.position(index)
        val value = when (len) {
            4 -> buffer.int
            2 -> buffer.short.toUInt().toInt()
            else -> buffer.get().toUInt().toInt()
        }
        buffer.position(position)
        return value
    }

    /**
     * Translate BaseChunk object to a READ ONLY [Readable]
     *
     * NOTE: make a copy of buffer here in order to support parallel reading
     */
    open fun asReadable() = object : Readable {
        private val readOnlyBuffer = buffer
            .duplicate()
            .asReadOnlyBuffer()
            .apply {
                rewind()
            }

        override fun read() = readOnlyBuffer.get()

        override fun available() = readOnlyBuffer.remaining()
    }

    override fun toString(): String {
        return "${chunkName}(length: $dataLen)"
    }
}

class IHDRChunk(buffer: ByteBuffer) : BaseChunk(buffer)

class IDATChunk(buffer: ByteBuffer) : BaseChunk(buffer)

class ACTLChunk(buffer: ByteBuffer) : BaseChunk(buffer) {
    val loop: Int by lazy {
        readAt(4)
    }
}

class FDATChunk(buffer: ByteBuffer) : BaseChunk(buffer) {

    override val dataLen: Int
        get() = super.dataLen - 4

    /**
     * Pretend that I am IDAT chunk,
     * so system's bitmap decoder can recognize me
     */
    override val type: Int
        get() = PngChunkType.TYPE_IDAT

    override val crc: Int
        get() = innerCRC

    /**
     * Because of changing [type] and skip first 4 byte,
     * we need to compute crc again
     */
    private val innerCRC by lazy {
        val crcEngine = CRC32()
        ByteBuffer.allocate(4).putInt(type).array().forEach {
            crcEngine.update(it.toInt())
        }
        crcEngine.update(buffer.array(), buffer.arrayOffset() + 12, dataLen)
        crcEngine.value.toInt()
    }

    /**
     * Translate FDATChunk object to a READ ONLY [Readable]
     *
     * NOTE: make a copy of buffer here in order to support parallel reading
     */
    override fun asReadable() =
        listOf(
            dataLen.asReadable(),
            type.asReadable(),
            buffer.asReadable(12, dataLen),
            crc.asReadable(),
        ).asReadable()

    override fun toString(): String {
        return "fdAT(length: $dataLen)"
    }
}

class FCTLChunk(buffer: ByteBuffer) : BaseChunk(buffer) {

    val width = readAt(12)
    val height = readAt(16)
    val xOffset = readAt(20)
    val yOffset = readAt(24)
    val delayed : Long by lazy {
        val num = readAt(28, 2)
        val denum = readAt(30, 2)
        when {
            denum == 0 -> 10L
            num == 0 -> 0L
            else -> 1000L * num / denum
        }
    }
    val disposeOp : FrameDisposeOptions by lazy {
        when (val op = readAt(32, 1)) {
            0 -> FrameDisposeOptions.DISPOSE_OP_NONE
            1 -> FrameDisposeOptions.DISPOSE_OP_BACKGROUND
            2 -> FrameDisposeOptions.DISPOSE_OP_PREVIOUS
            else -> throw FormatException("DisposeOp $op not valid!")
        }
    }
    val blendOp : FrameBlendOptions by lazy {
        when (val op = readAt(33, 1)) {
            0 -> FrameBlendOptions.BLEND_OP_SRC
            1 -> FrameBlendOptions.BLEND_OP_SRC_OVER
            else -> throw FormatException("BlendOp $op not valid!")
        }
    }

    fun toFrameOptions(): FrameOptions =
        FrameOptions(width, height, xOffset, yOffset, delayed, disposeOp, blendOp)

    override fun toString(): String {
        return super.toString() + " size = ($width, $height), offset = ($xOffset, $yOffset)"
    }
}

internal fun BaseChunk.Companion.makeChunk(
    type: Int,
    buffer: ByteBuffer
) = when (type) {
    PngChunkType.TYPE_IHDR -> IHDRChunk(
        buffer
    )
    PngChunkType.TYPE_ACTL -> ACTLChunk(
        buffer
    )
    PngChunkType.TYPE_FCTL -> FCTLChunk(
        buffer
    )
    PngChunkType.TYPE_FDAT -> FDATChunk(
        buffer
    )
    PngChunkType.TYPE_IDAT -> IDATChunk(
        buffer
    )
    else -> BaseChunk(buffer)
}

internal fun IHDRChunk.makeFakeIHDRReadable(chunk: FCTLChunk): Readable {
    val buffer = ByteBuffer.allocate(buffer.capacity())
        .putInt(dataLen)
        .putInt(type)
        .putInt(chunk.width)
        .putInt(chunk.height)
        .put(buffer.array(), buffer.arrayOffset() + 16, 5)
    val crc = CRC32()
    crc.update(buffer.array(), 4, buffer.position() - 4)
    buffer.putInt(crc.value.toInt())
    return buffer.asReadable()
}
