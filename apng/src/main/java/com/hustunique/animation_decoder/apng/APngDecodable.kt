package com.hustunique.animation_decoder.apng

import com.hustunique.animation_decoder.api.AnimatedImage
import com.hustunique.animation_decoder.api.Frame
import com.hustunique.animation_decoder.api.TaskDispatcher
import com.hustunique.animation_decoder.core.*
import com.hustunique.animation_decoder.core.exceptions.DecodeFailException
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

class APngDecodable<DT> constructor(
    header: IHDRChunk,
    val actl: ACTLChunk,
    defaultFrame: List<IDATChunk>,
    val frames: List<RawFrameData>,
    others: List<BaseChunk>
) : PngDecodable<DT>(header, defaultFrame, others) {


    override fun createAnimatedImage(
        taskDispatcher: TaskDispatcher,
        decodeAction: DecodeAction<DT>
    ) =
        AnimatedImage<DT>(
            loop = actl.loop
        ).apply {
            this@APngDecodable.frames.map { frameData ->
                addFrame(Frame<DT>(
                    frameData.fctl.toFrameOptions()
                ).also {
                    taskDispatcher.dispatch {
                        it.imgWrapper = decodeAction(
                            readable {
                                add(SIGNATURE.asReadable())
                                add(header.makeFakeIHDRReadable(frameData.fctl))
                                addAll(frameData.chunks.map { it.asReadable() })
                                addAll(others.map { it.asReadable() })
                            }.asStream()
                        ) ?: throw DecodeFailException()
                    }
                })
            }
        }

    class Builder<DT> {

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

        fun build(): PngDecodable<DT> {
            check(header != null) {
                "Png file has no IHDRChunk"
            }
            return if (actl == null) {
                PngDecodable(header!!, defaultFrame, others)
            } else {
                APngDecodable(header!!, actl!!, defaultFrame, frames, others)
            }
        }
    }
}

open class PngDecodable<DT> constructor(
    val header: IHDRChunk,
    val defaultFrame: List<IDATChunk>,
    val others: List<BaseChunk>
) : Decodable<DT> {

    companion object {
        val SIGNATURE = ByteBuffer.allocate(8).putLong(APngParser.PNG_SIGNATURE)
    }

    override fun createAnimatedImage(
        taskDispatcher: TaskDispatcher,
        decodeAction: DecodeAction<DT>
    ) =
        AnimatedImage<DT>(
        ).apply {
            addFrame(Frame<DT>(null).also {
                it.onCompletedActions.add(onFrameCompletedAction)
                taskDispatcher.dispatch {
                    it.imgWrapper = decodeAction(readable {
                        add(SIGNATURE.asReadable())
                        add(header.asReadable())
                        addAll(defaultFrame.map { it.asReadable() })
                        addAll(others.map { it.asReadable() })
                    }.asStream()) ?: throw DecodeFailException()
                }
            }
            )
        }
}