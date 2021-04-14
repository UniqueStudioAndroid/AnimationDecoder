package com.hustunique.animation_decoder.apng.webp

import com.hustunique.animation_decoder.api.Frame
import com.hustunique.animation_decoder.apng.PngDecodable
import com.hustunique.animation_decoder.apng.makeFakeIHDRReadable
import com.hustunique.animation_decoder.apng.webp.WebPChunkType
import com.hustunique.animation_decoder.core.*
import com.hustunique.animation_decoder.core.exceptions.DecodeFailException
import java.io.File

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

class WebPDecodable<DT> constructor(
    val animChunk: ANIMChunk,
    val anmfChunks: List<ANMFChunk>,
) : Decodable<DT> {
    override fun createFrames(decodeAction: DecodeAction<DT>): List<Frame<DT>> = anmfChunks.map {
//        val data = readable {
//            add(WebPChunkType.TYPE_RIFF.asReadable())
//            add((it.size.toInt() - 4).asReadable())
//            add(WebPChunkType.TYPE_WEBP.asReadable())
//            add(it.frameData.asReadable())
//        }.asStream()
        WebPFrame(
            decodeAction(
                readable {
                    add(WebPChunkType.TYPE_RIFF.asReadable())
                    add((it.size.toInt() - 12).asReadable(true))
                    add(WebPChunkType.TYPE_WEBP.asReadable())
                    add(it.frameData.asReadable())
                }.asStream()
            ) ?: throw DecodeFailException(),
            it.toFrameOptions()
        )
    }

    class Builder<DT> {
        private var animChunk: ANIMChunk? = null
        private val anmfChunks = ArrayList<ANMFChunk>()

        fun setANIM(animChunk: ANIMChunk) = apply {
            check(this.animChunk == null)
            this.animChunk = animChunk
        }

        fun addANMF(anmfChunk: ANMFChunk) = apply { anmfChunks.add(anmfChunk) }

        fun build(): WebPDecodable<DT> = let {
            check(animChunk != null)
            WebPDecodable(animChunk!!, anmfChunks)
        }
    }
}