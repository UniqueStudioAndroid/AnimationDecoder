package com.hustunique.animation_decoder.awebp

import com.hustunique.animation_decoder.api.AnimatedImage
import com.hustunique.animation_decoder.api.Frame
import com.hustunique.animation_decoder.api.TaskDispatcher
import com.hustunique.animation_decoder.core.*
import com.hustunique.animation_decoder.core.exceptions.DecodeFailException

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
    override fun createAnimatedImage(
        taskDispatcher: TaskDispatcher,
        decodeAction: DecodeAction<DT>
    ): AnimatedImage<DT> = AnimatedImage<DT>(
        loop = animChunk.loopCount,
        backgroundColor = animChunk.color
    ).apply {
        anmfChunks.map { chunk ->
            addFrame(Frame<DT>(chunk.toFrameOptions()).also {
                taskDispatcher.dispatch {
                    it.imgWrapper = decodeAction(
                        readable {
                            add(WebPChunkType.TYPE_RIFF.asReadable())
                            add((chunk.size.toInt() - 12).asReadable(true))
                            add(WebPChunkType.TYPE_WEBP.asReadable())
                            add(chunk.frameData.asReadable())
                        }.asStream()
                    ) ?: throw DecodeFailException()
                }
            }
            )
        }
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