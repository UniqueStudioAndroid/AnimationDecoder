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

import com.hustunique.apng_decoder.PngChunkType.TYPE_ACTL
import com.hustunique.apng_decoder.PngChunkType.TYPE_FCTL
import com.hustunique.apng_decoder.PngChunkType.TYPE_FDAT
import com.hustunique.apng_decoder.PngChunkType.TYPE_IDAT
import com.hustunique.apng_decoder.PngChunkType.TYPE_IEND
import com.hustunique.apng_decoder.PngChunkType.TYPE_IHDR
import com.hustunique.apng_decoder.PngChunkType.TYPE_PLTE
import com.hustunique.apng_decoder.PngChunkType.TYPE_TEXT
import java.nio.ByteBuffer

sealed class BaseChunk(
    val length: Int,
    val position: Int
) {
    override fun toString(): String {
        return "${javaClass.simpleName}($position, $length)"
    }

    companion object {

    }
}

class IHDRChunk(length: Int, position: Int) : BaseChunk(length, position)

class PLTEChunk(length: Int, position: Int) : BaseChunk(length, position)

class IDATChunk(length: Int, position: Int) : BaseChunk(length, position)

class IENDChunk(length: Int, position: Int) : BaseChunk(length, position)

class FDATChunk(length: Int, position: Int) : BaseChunk(length, position)

class ACTLChunk(length: Int, position: Int) : BaseChunk(length, position)

class FCTLChunk(length: Int, position: Int) : BaseChunk(length, position)

class TEXTChunk(length: Int, position: Int) : BaseChunk(length, position)

class UnknownChunk(length: Int, position: Int) : BaseChunk(length, position)

internal fun BaseChunk.Companion.makeChunk(type: Int, len: Int, position: Int) = when (type) {
    TYPE_IHDR -> IHDRChunk(len, position)
    TYPE_IDAT -> IDATChunk(len, position)
    TYPE_PLTE -> PLTEChunk(len, position)
    TYPE_IEND -> IENDChunk(len, position)
    TYPE_ACTL -> ACTLChunk(len, position)
    TYPE_FCTL -> FCTLChunk(len, position)
    TYPE_FDAT -> FDATChunk(len, position)
    TYPE_TEXT -> TEXTChunk(len, position)
    else -> {
        println(String.format("%x", type))
        UnknownChunk(len, position)
    }
}
