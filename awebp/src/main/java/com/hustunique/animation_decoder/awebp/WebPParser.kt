package com.hustunique.animation_decoder.awebp

import com.hustunique.animation_decoder.core.Decodable
import com.hustunique.animation_decoder.core.Parser
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

class WebPParser<DT> : Parser<DT> {

    private fun unBox(box: ByteBuffer): BaseChunk {
        val pos = box.position()
        val type = box.int.reverseBytes()
        val size = box.int
        val buffer =
            ByteBuffer.wrap(box.array(), pos, size + 8).slice().order(ByteOrder.LITTLE_ENDIAN)
        box.position(pos + size + 8)
        return BaseChunk.makeChunk(type, buffer)
    }

    @ExperimentalUnsignedTypes
    override fun parse(data: ByteBuffer): Decodable<DT> {
        data.order(ByteOrder.LITTLE_ENDIAN)
        data.position(4)
        val size = data.int.toUInt()
        data.position(12)

        val builder = WebPDecodable.Builder<DT>()
//        val list = ArrayList<BaseChunk>()
        while (data.remaining() > 0) {
            val chunk = unBox(data)
            when (chunk) {
                is ANMFChunk -> builder.addANMF(chunk)
                is ANIMChunk -> builder.setANIM(chunk)
                else -> {
                }
            }
            println(chunk)
        }
        return builder.build()
    }

    override fun handles(data: ByteBuffer): Boolean {
        val pos = data.position()
        val head = data.int
        data.int
        val desc = data.int
        data.position(pos)
        return head == 0x52494646 && desc == 0x57454250
    }

    private fun getName(data: Int) =
        String(ByteBuffer.allocate(4).putInt(data.reverseBytes()).array())
}

fun Int.reverseBytes(): Int {
    val v0 = ((this ushr 0) and 0xFF)
    val v1 = ((this ushr 8) and 0xFF)
    val v2 = ((this ushr 16) and 0xFF)
    val v3 = ((this ushr 24) and 0xFF)
    return (v0 shl 24) or (v1 shl 16) or (v2 shl 8) or (v3 shl 0)
}