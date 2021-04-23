@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.hustunique.animation_decoder.gif

import java.nio.ByteBuffer

open class BaseChunk(val buffer: ByteBuffer) {

    protected fun readAt(index: Int, len: Int = 4): Int {
        val position = buffer.position()
        buffer.position(index)
        val value = when (len) {
            4 -> buffer.int
            2 -> buffer.short.toInt()
            else -> buffer.get().toInt()
        }
        buffer.position(position)
        return value
    }
}

class LogicalScreenDescriptor(buffer: ByteBuffer) : BaseChunk(buffer) {
    val width = readAt(0, 2)
    val height = readAt(2, 2)
    val bgColorIndex = readAt(5, 1)
    val pixelRatio = readAt(6, 1)
    val useGlobalColor: Boolean
    val bitsPerColor: Int
    val sortedColor: Boolean
    val globalTableSize: Int

    init {
        var flag = readAt(4, 1)

        useGlobalColor = (flag and 0x1) == 1
        flag = flag shr 1

        bitsPerColor = (flag and 0x7) + 1
        flag = flag shr 3

        sortedColor = (flag and 0x1) == 1
        flag = flag shr 1

        globalTableSize = pow(2, (flag and 0x7) + 1)
    }

    companion object {
        const val SIZE = 7
    }
}

fun pow(x: Int, a: Int): Int {
    if (a < 0) return 0
    var result = 1
    for (i in 0 until a) {
        result *= x
    }
    return result
}

internal fun ByteBuffer.forward(size: Int) {
    position(position() + size)
}
