package com.hustunique.animation_decoder.core

import java.nio.BufferUnderflowException
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

/**
 * An interface represents all readable source.
 *
 * NOTE: Thread Safe
 */
interface Readable {

    /**
     * Read next byte of its source
     * Throw [BufferUnderflowException] if no byte is available
     *
     * @return next byte of this readable source.
     */
    fun read(): Byte

    /**
     * @return bytes count available for read
     */
    fun available(): Int
}

/**
 * Merge a List<[Readable]> object into a Readable one
 * NOTE: Default byte order is BitEndian
 */
fun List<Readable>.asReadable(): Readable = object : Readable {

    private var curPos = 0

    private var curReadableIdx = 0

    private var curChunk = this@asReadable[curReadableIdx++]

    private val readableSizeList = this@asReadable
        .map { it.available() }
        .runningReduce { acc, i ->
            acc + i
        }

    override fun read(): Byte {
        curChunk = if (curPos++ < readableSizeList[curReadableIdx - 1]) curChunk
        else this@asReadable[curReadableIdx++]
        return curChunk.read()
    }

    override fun available(): Int = readableSizeList.last() - curPos
}

/**
 * Translate Int object to a Readable
 * NOTE: Default byte order is BitEndian
 */
fun Int.asReadable(isBigEndian: Boolean = false): Readable = object : Readable {

    private var curIndex = 0

    override fun read(): Byte {
        val idx = if (isBigEndian) curIndex else 3 - curIndex
        curIndex++
        return ((this@asReadable ushr (idx * 8)) and 0xFF).toByte()
    }

    override fun available(): Int {
        return 4 - curIndex
    }
}

fun Byte.asReadable(): Readable = object : Readable {
    private var hasRead = false

    override fun read(): Byte {
        return this@asReadable
    }

    override fun available() = if (hasRead) 0 else 1
}

/**
 * Wrap [ByteBuffer] object to a Readable
 * NOTE: Default byte order is BitEndian
 */
fun ByteBuffer.asReadable(offset: Int = 0, size: Int = capacity()): Readable =
    object : Readable {
        private val readByteBuffer = this@asReadable.let {
            it.position(offset)
            it.limit(size + offset)
            it.slice()
        }

        override fun read(): Byte = readByteBuffer.get()

        override fun available(): Int = readByteBuffer.remaining()
    }

@OptIn(ExperimentalStdlibApi::class)
fun readable(builderAction: MutableList<Readable>.() -> Unit): Readable =
    buildList(builderAction).asReadable()

fun Readable.asStream() = ReadableStream(this)