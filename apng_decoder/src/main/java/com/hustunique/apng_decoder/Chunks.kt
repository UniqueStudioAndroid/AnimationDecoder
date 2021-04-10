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

import java.nio.ByteBuffer
import java.util.zip.CRC32

open class BaseChunk(
    val readOnlyBuffer: ByteBuffer
) : Readable {
    companion object {}

    open val length: Int
        get() = readAt(0)

    open val type: Int
        get() = readAt(4)

    open val crc: Int
        get() = readAt(length + 8)

    val chunkName: String
        get() = String(ByteBuffer.allocate(4).putInt(type).array())

    override fun read(): Byte = readOnlyBuffer.get()

    override fun size(): Int = length + 12

    override fun toString(): String {
        return "${chunkName}(length: $length)"
    }

    open fun read() = readOnlyBuffer.get()
}

class IHDRChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer)

class FDATChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer) {

    override val length: Int
        get() = super.length - 4

    override val type: Int
        get() = PngChunkType.TYPE_IDAT

    override val crc: Int
        get() = innerCRC

    private val innerCRC: Int

    init {
        val crcEngine = CRC32()
        crcEngine.update(type)
        crcEngine.update(readOnlyBuffer.array(), 12, length)
        innerCRC = crcEngine.value.toInt()
    }

    override fun toString(): String {
        return "fdAT(length: $length)"
    }

    private var position = 0
    override fun read() : Byte {
        val len = length
        if (position in 8 until len)
        position++
        return readByteInInt(position - length - 12, )
    }
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

internal fun BaseChunk.readAt(index: Int) : Int {
    val position = readOnlyBuffer.position()
    readOnlyBuffer.position(index)
    val value = readOnlyBuffer.int
    readOnlyBuffer.position(position)
    return value
}