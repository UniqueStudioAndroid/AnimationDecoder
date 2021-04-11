package com.hustunique.animation_decoder.apng

import com.hustunique.animation_decoder.core.*
import java.nio.ByteBuffer

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

class APngDecodable constructor(
    header: IHDRChunk,
    val actl: ACTLChunk?,
    defaultFrame: List<IDATChunk>,
    val frames: List<RawFrameData>,
    others: List<BaseChunk>
) : PngDecodable(header, defaultFrame, others) {


    override fun createFrames(decodeAction: DecodeAction) = frames.map { frameData ->
        APngFrame(
            decodeAction(
                readable {
                    add(SIGNATURE.asReadable())
                    add(header.makeFakeIHDRReadable(frameData.fctl))
                    addAll(frameData.chunks.map { it.asReadable() })
                    addAll(others.map { it.asReadable() })
                }.asStream()
            ),
            frameData.fctl.toFrameOptions()
        )
    }

    class Builder {

        private var header: IHDRChunk? = null
        private var actl: ACTLChunk? = null
        private val defaultFrame: MutableList<IDATChunk> = mutableListOf()
        private val frames: MutableList<RawFrameData> = mutableListOf()
        private val others: MutableList<BaseChunk> = mutableListOf()

        fun setHeader(ihdrChunk: IHDRChunk) = apply { header = ihdrChunk }

        fun setACTL(actlChunk: ACTLChunk) = apply {
            check(actl == null) { "Two acTL Chunk" }
            actl = actlChunk
        }

        fun addFrame(rawFrameData: RawFrameData) = apply {
            check(header != null) { "Header not initialized" }
            frames.add(rawFrameData)
        }

        fun addDefaultFrame(frame: IDATChunk) = apply { defaultFrame.add(frame) }

        fun addOthers(othersChunk: BaseChunk) = apply { others.add(othersChunk) }

        fun build(): PngDecodable {
            check(header != null) {
                "Png file has no IHDRChunk"
            }
            return if (actl == null) {
                PngDecodable(header!!, defaultFrame, others)
            } else {
                APngDecodable(header!!, actl, defaultFrame, frames, others)
            }
        }
    }
}

open class PngDecodable constructor(
    val header: IHDRChunk,
    val defaultFrame: List<IDATChunk>,
    val others: List<BaseChunk>
) : Decodable {

    companion object {
        val SIGNATURE = ByteBuffer.allocate(8).putLong(APngParser.PNG_SIGNATURE)
    }

    override fun createFrames(decodeAction: DecodeAction) =
        listOf(Frame(decodeAction(readable {
            add(SIGNATURE.asReadable())
            add(header.asReadable())
            addAll(defaultFrame.map { it.asReadable() })
            addAll(others.map { it.asReadable() })
        }.asStream())))

}