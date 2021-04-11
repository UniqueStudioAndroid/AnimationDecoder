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

import com.hustunique.apng_decoder.core.Frame
import com.hustunique.apng_decoder.core.FrameDecoder
import com.hustunique.apng_decoder.core.Parser
import java.io.File
import java.nio.ByteBuffer

class AnimationDecoder(private val parsers: List<Parser>, private val frameDecoder: FrameDecoder) {

    fun decode(path: String): List<Frame> = decode(ByteBuffer.wrap(File(path).readBytes()))

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    fun decode(data: ByteBuffer): List<Frame> {
        val parsedObj = parsers.first { it.handles(data) }.parse(data)
        return parsedObj.createFrames {
            frameDecoder.decode(it)
        }
    }
}
