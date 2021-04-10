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

class IHDRChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer)

class FDATChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer) {

    override val dataLen: Int
        get() = super.dataLen - 4

    override val type: Int
        get() = PngChunkType.TYPE_IDAT

    override val crc: Int
        get() = innerCRC

    private val innerCRC: Int

    init {
        val crcEngine = CRC32()
        ByteBuffer.allocate(4).putInt(type).array().forEach {
            crcEngine.update(it.toInt())
        }
        crcEngine.update(readOnlyBuffer.array(), readOnlyBuffer.arrayOffset() + 12, dataLen)
        innerCRC = crcEngine.value.toInt()
    }

    private val readable = listOf(
        dataLen.asReadable(),
        type.asReadable(),
        readOnlyBuffer.asReadable(12),
        crc.asReadable(),
    ).asReadable()

    override fun toString(): String {
        return "fdAT(length: $dataLen)"
    }

    override fun read(): Byte = readable.read()

    override fun available() = readable.available()
}

class ACTLChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer)

class FCTLChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer)

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

internal fun BaseChunk.readAt(index: Int): Int {
    val position = readOnlyBuffer.position()
    readOnlyBuffer.position(index)
    val value = readOnlyBuffer.int
    readOnlyBuffer.position(position)
    return value
}