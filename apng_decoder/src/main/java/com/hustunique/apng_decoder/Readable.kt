package com.hustunique.apng_decoder

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


interface Readable {

    fun read(): Byte

    fun size(): Int

}

internal fun List<Readable>.asReadable(): Readable = object : Readable {
    private val readableSizeList = this@asReadable.map { it.size() }.runningReduce { acc, i ->
        acc + i
    }

    private var curPos = 0

    private var curReadableIdx = 0

    private var curChunk = this@asReadable[curReadableIdx]
    override fun read(): Byte {
        curChunk = if (curPos++ < readableSizeList[curReadableIdx - 1]) curChunk
        else this@asReadable[curReadableIdx++]
        return curChunk.read()
    }

    override fun size(): Int = readableSizeList.last()

}
