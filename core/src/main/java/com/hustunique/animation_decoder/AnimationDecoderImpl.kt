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
import com.hustunique.animation_decoder.core.FrameDecoder
import com.hustunique.animation_decoder.core.Decoder
import java.io.File
import java.nio.ByteBuffer
import kotlin.jvm.Throws

class AnimationDecoderImpl<T>(
    private val decoders: List<Decoder>,
    private val frameDecoder: FrameDecoder<T>
) : AnimationDecoder<T> {

    fun decode(path: String): AnimatedImage<T> = decode(ByteBuffer.wrap(File(path).readBytes()))

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    override fun decode(data: ByteBuffer): AnimatedImage<T> {
        val decoder = decoders.firstOrNull { it.handles(data) }
        require(decoder != null)
        return decoder.decode(data, frameDecoder)
    }
}
