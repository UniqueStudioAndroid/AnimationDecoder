package com.hustunique.animation_decoder.apng

import com.hustunique.animation_decoder.core.Decodable
import com.hustunique.animation_decoder.core.Parser
import java.nio.ByteBuffer

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

/**
 * Represent a png file, extract from [data]
 */
class APngParser<DT>() : Parser<DT> {
    private var rawFrame: RawFrameData? = null

    companion object {
        const val PNG_SIGNATURE = (0x89_50_4E_47 shl 32) + 0x0D_0A_1A_0A
    }

    private fun readAndCheckSignature(data: ByteBuffer) {
        val signature = data.long
        check(signature == PNG_SIGNATURE) { "Not a PNG file!!!" }
    }

    private fun readAndUnBox(box: ByteBuffer): BaseChunk {
        val pos = box.position()
        val chunkLen = box.int + 12
        val type = box.int
        // must add slice call
        val chunk =
            BaseChunk.makeChunk(
                type,
                ByteBuffer.wrap(box.array(), pos, chunkLen).slice()
            )
        box.position(pos + chunkLen)
        return chunk
    }

    private fun process(builder: APngDecodable.Builder<DT>, chunk: BaseChunk) {
        when (chunk) {
            is IHDRChunk -> {
                builder.setHeader(chunk)
            }
            is ACTLChunk -> {
                builder.setACTL(chunk)
            }
            is FCTLChunk -> {
                rawFrame?.apply { builder.addFrame(this) }
                rawFrame = RawFrameData(chunk, ArrayList())
            }
            is FDATChunk -> {
                check(rawFrame != null) { "fdAT Chunk before fcTL chunk" }
                rawFrame?.chunks?.add(chunk)
            }
            is IDATChunk -> rawFrame?.chunks?.add(chunk)
            else -> builder.addOthers(chunk)
        }
    }

    override fun handles(data: ByteBuffer): Boolean {
        return true
    }

    override fun parse(data: ByteBuffer): Decodable<DT> {
        reset()
        readAndCheckSignature(data)
        val aPngParsedObjectBuilder = APngDecodable.Builder<DT>()
        while (data.remaining() > 0) {
            val chunk = readAndUnBox(data)
            process(aPngParsedObjectBuilder, chunk)
        }
        rawFrame?.let {
            aPngParsedObjectBuilder.addFrame(it)
        }
        return aPngParsedObjectBuilder.build()
    }

    private fun reset() {
        rawFrame = null
    }
}