package bbs.hustunique.animation_decoder.gif

import com.hustunique.animation_decoder.api.AnimatedImage
import com.hustunique.animation_decoder.core.Decodable
import com.hustunique.animation_decoder.core.Decoder
import com.hustunique.animation_decoder.core.FrameDecoder
import com.hustunique.animation_decoder.core.exceptions.FormatException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Copyright (C) 2021 Ski
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

class GifParser : Decoder {
    private fun parse(data: ByteBuffer): Decodable {
        data.position(6)

        val descriptor = parseLogicalDescriptor(data)
        val globalColorTable = parseGlobalTableColor(data, descriptor)

        var type = data.get()
        while (type != GifChunkType.TYPE_TRAILER) {
            when (type) {
                GifChunkType.TYPE_EXT_INTRODUCE -> {
                    val label = data.get()
                    when (label) {
                        GifChunkType.TYPE_GRAPHIC_EXT -> println("H1")
//                        GifChunkType.TYPE_COMMENT_EXT or
//                                GifChunkType.TYPE_APPLICATION_EXT or
//                                GifChunkType.TYPE_TEXT_EXT -> throw FormatException("Text Not supported")
                        else -> throw FormatException("Unrecognized label = $label")
                    }
                }
                GifChunkType.TYPE_IMAGE_DESC -> println("image")
                else -> throw FormatException("Unrecognized type = $type")
            }
            type = data.get()
        }

        return GifDecodable()
    }

    override fun handles(data: ByteBuffer): Boolean {
        val head = (data.int.toLong() shl 16) or data.short.toLong()
        data.position(0)
        return head == GifChunkType.TYPE_HEAD_87A ||
                head == GifChunkType.TYPE_HEAD_89A
    }

    override fun <T> decode(data: ByteBuffer, frameDecoder: FrameDecoder<T>): AnimatedImage<T> {
        TODO("Not yet implemented")
    }

    private fun parseLogicalDescriptor(buffer: ByteBuffer): LogicalScreenDescriptor {
        val data = ByteBuffer.wrap(
            buffer.array(),
            buffer.arrayOffset() + buffer.position(),
            LogicalScreenDescriptor.SIZE
        ).slice().order(ByteOrder.LITTLE_ENDIAN)
        buffer.forward(LogicalScreenDescriptor.SIZE)
        return LogicalScreenDescriptor(data)
    }

    private fun parseGlobalTableColor(
        buffer: ByteBuffer,
        descriptor: LogicalScreenDescriptor
    ): ByteBuffer? {
        if (!descriptor.useGlobalColor) return null
        val data = ByteBuffer.wrap(
            buffer.array(),
            buffer.arrayOffset() + buffer.position(),
            descriptor.globalTableSize * 3
        ).slice().order(ByteOrder.LITTLE_ENDIAN)
        buffer.forward(data.capacity())
        return data
    }
}
