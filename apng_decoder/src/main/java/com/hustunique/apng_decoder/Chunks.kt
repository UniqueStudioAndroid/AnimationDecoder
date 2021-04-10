@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.hustunique.apng_decoder

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

import org.jetbrains.annotations.TestOnly
import java.nio.ByteBuffer
import java.util.zip.CRC32

const val MIN_FRAME_INTERVAL = 10L

open class BaseChunk(
    val readOnlyBuffer: ByteBuffer
) : Readable {
    companion object {}

    open val dataLen: Int
        get() = readAt(0)

    open val type: Int
        get() = readAt(4)

    open val crc: Int
        get() = readAt(dataLen + 8)

    val chunkName: String
        get() = String(ByteBuffer.allocate(4).putInt(type).array())

    override fun read(): Byte = readOnlyBuffer.get()

    override fun available(): Int = dataLen + 12

    override fun toString(): String {
        return "${chunkName}(length: $dataLen)"
    }

    @TestOnly
    fun computeCRC(): Int {
        val crcEngine = CRC32()
        ByteBuffer.allocate(4).putInt(type).array().forEach {
            crcEngine.update(it.toInt())
        }
        crcEngine.update(readOnlyBuffer.array(), readOnlyBuffer.arrayOffset() + 8, dataLen)
        return crcEngine.value.toInt()
    }
}

class IHDRChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer) {
    fun makeFakeIHDRReadable(chunk: FCTLChunk): Readable {
        val buffer = ByteBuffer.allocate(readOnlyBuffer.capacity())
            .putInt(dataLen)
            .putInt(type)
            .putInt(chunk.width)
            .putInt(chunk.height)
            .put(readOnlyBuffer.array(), readOnlyBuffer.arrayOffset() + 16, 5)
        val crc = CRC32()
        crc.update(buffer.array(), 4, buffer.position() - 4)
        buffer.putInt(crc.value.toInt())
        return buffer.asReadable()
    }
}

class FDATChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer) {

    override val dataLen: Int
        get() = super.dataLen - 4

    override val type: Int
        get() = PngChunkType.TYPE_IDAT

    override val crc: Int
        get() = innerCRC

    private val innerCRC = let {
        val crcEngine = CRC32()
        ByteBuffer.allocate(4).putInt(type).array().forEach {
            crcEngine.update(it.toInt())
        }
        crcEngine.update(readOnlyBuffer.array(), readOnlyBuffer.arrayOffset() + 12, dataLen)
        crcEngine.value.toInt()
    }

    private val readable = listOf(
        dataLen.asReadable(),
        type.asReadable(),
        readOnlyBuffer.asReadable(12, dataLen),
        innerCRC.asReadable(),
    ).asReadable()

    override fun toString(): String {
        return "fdAT(length: $dataLen)"
    }

    override fun read(): Byte = readable.read()

    override fun available() = readable.available()
}

class ACTLChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer)

class FCTLChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer) {
    val width = readAt(12)
    val height = readAt(16)
    val xOffset = readAt(20)
    val yOffset = readAt(24)
    val delayed = let {
        val num = readAt(28, 2)
        val denum = readAt(30, 2)
        when {
            denum == 0 -> 10L
            num == 0 -> MIN_FRAME_INTERVAL
            else -> 1000L * num / denum
        }
    }
    val disposeOp = readAt(32, 1).toByte()
    val blendOp = readAt(33, 1).toByte()

    fun toFrameOptions(): FrameOptions = FrameOptions(width, height, xOffset, yOffset, delayed, disposeOp, blendOp = blendOp)

    override fun toString(): String {
        return super.toString() + " size = ($width, $height), offset = ($xOffset, $yOffset)"
    }
}

class IDATChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer)

internal fun BaseChunk.Companion.makeChunk(
    type: Int,
    readOnlyBuffer: ByteBuffer
) = when (type) {
    PngChunkType.TYPE_IHDR -> IHDRChunk(readOnlyBuffer)
    PngChunkType.TYPE_ACTL -> ACTLChunk(readOnlyBuffer)
    PngChunkType.TYPE_FCTL -> FCTLChunk(readOnlyBuffer)
    PngChunkType.TYPE_FDAT -> FDATChunk(readOnlyBuffer)
    PngChunkType.TYPE_IDAT -> IDATChunk(readOnlyBuffer)
    else -> BaseChunk(readOnlyBuffer)
}

internal fun BaseChunk.readAt(index: Int, len: Int = 4): Int {
    val position = readOnlyBuffer.position()
    readOnlyBuffer.position(index)
    val value = when (len) {
        4 -> readOnlyBuffer.int
        2 -> readOnlyBuffer.short.toUInt().toInt()
        else -> readOnlyBuffer.get().toUInt().toInt()
    }
    readOnlyBuffer.position(position)
    return value
}