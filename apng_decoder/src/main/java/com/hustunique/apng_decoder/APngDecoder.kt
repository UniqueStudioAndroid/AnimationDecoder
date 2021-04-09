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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Throws

class APngDecoder {

    companion object {
        private const val PNG_SIGNATURE = (0x89_50_4E_47 shl 32) + 0x0D_0A_1A_0A
    }

    private fun readAndUnBox(box: ByteBuffer): BaseChunk {
        val len = box.int
        val type = box.int
        val position = box.position()
        box.position(position + len)
        val crc = box.int
        // TODO: check crc here
        return BaseChunk.makeChunk(type, len, position)
    }

    private fun readAndUnBoxAll(data: ByteBuffer) : List<BaseChunk> {
        val chunks = ArrayList<BaseChunk>()
        while (data.remaining() > 0) {
            chunks += readAndUnBox(data)
        }
        return chunks
    }

    private fun readAndCheckSignature(data: ByteBuffer) {
        val signature = data.long
        check(signature == PNG_SIGNATURE) { "Not a PNG file!!!" }
    }

    @Throws(IllegalStateException::class)
    fun decode(data: ByteBuffer): Bitmap {
        readAndCheckSignature(data)
        val chunks = readAndUnBoxAll(data)
        data.rewind()
        return BitmapFactory.decodeByteArray(data.array(), 0, data.remaining())
    }
}

// signature
// acTL
// IDAT
// fcTL
// fDAT
// ...
// IEND
