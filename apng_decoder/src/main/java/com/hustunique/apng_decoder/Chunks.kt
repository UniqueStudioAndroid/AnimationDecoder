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

open class BaseChunk(
    val readOnlyBuffer: ByteBuffer
) : Readable {
    companion object {}

    open val length: Int
        get() = readOnlyBuffer.let {
            it.position(0)
            it.int
        }

    open val type: Int
        get() = readOnlyBuffer.let {
            it.position(4)
            it.int
        }

    open val crc: Int
        get() = readOnlyBuffer.let {
            it.position(length + 8)
            it.int
        }

    val chunkName: String
        get() = String(ByteBuffer.allocate(4).putInt(type).array())

    override fun read(): Byte = readOnlyBuffer.get()

    override fun size(): Int = length + 12

    override fun toString(): String {
        return "${chunkName}(length: $length)"
    }

}

class IHDRChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer)

class FDATChunk(readOnlyBuffer: ByteBuffer) : BaseChunk(readOnlyBuffer) {

    override val length: Int
        get() = super.length

    override val type: Int
        get() = PngChunkType.TYPE_IDAT

    override val crc: Int
        get() = super.crc

    override fun toString(): String {
        return "fdAT(length: $length)"
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
