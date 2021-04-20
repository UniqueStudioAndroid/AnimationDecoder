package com.hustunique.animation_decoder

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

import com.hustunique.animation_decoder.api.AnimatedImage
import com.hustunique.animation_decoder.api.AnimationDecoder
import com.hustunique.animation_decoder.api.TaskDispatcher
import com.hustunique.animation_decoder.core.FrameDecoder
import com.hustunique.animation_decoder.core.Parser
import java.io.File
import java.nio.ByteBuffer

class AnimationDecoderImpl<DT>(
    private val parsers: List<Parser<DT>>,
    private val frameDecoder: FrameDecoder<DT>,
    private val taskDispatcher: TaskDispatcher
) : AnimationDecoder<DT> {


    fun decode(path: String): AnimatedImage<DT> = decode(ByteBuffer.wrap(File(path).readBytes()))

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    override fun decode(data: ByteBuffer): AnimatedImage<DT> {
        val parsedObj = parsers.first { it.handles(data) }.parse(data)
        return parsedObj.createAnimatedImage(taskDispatcher) {
            frameDecoder.decode(it)
        }
    }
}
