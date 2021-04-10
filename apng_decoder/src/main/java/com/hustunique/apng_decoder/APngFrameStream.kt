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

import java.io.InputStream
import java.nio.ByteBuffer

class APngFrameStream(
    header: IHDRChunk,
    rawFrameData: RawFrameData,
    others: List<BaseChunk>
) :
    InputStream() {

    companion object {
        private val SIGNATURE = ByteBuffer.allocate(8).putLong(APngObject.PNG_SIGNATURE)
    }

    private val readableChunkList = mutableListOf<Readable>().apply {
        add(SIGNATURE.asReadOnlyBuffer().asReadable())
        add(header.makeFakeIHDRReadable(rawFrameData.fctl))
        addAll(rawFrameData.chunks)
        addAll(others)
    }.asReadable()


    @ExperimentalUnsignedTypes
    override fun read() =
        if (readableChunkList.available() > 0) readableChunkList.read().toUByte().toInt()
        else -1

    override fun available(): Int = readableChunkList.available()
}

//fun APngObject.frameDataStream(frameIndex: Int): APngFrameStream {
//    check(frameIndex < frameSize())
//    return APngFrameStream(getHeader(), getFrame(frameIndex), getOthersChunk())
//}
