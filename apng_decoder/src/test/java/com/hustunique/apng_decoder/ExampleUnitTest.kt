package com.hustunique.apng_decoder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun run() {
        val file = File("elephant.png")
        val decoder = APngDecoder()
        val input = decoder.decode(ByteBuffer.wrap(file.readBytes()))
        val file2 = File("elephant2.png")
        if (file2.exists()) {
            file2.delete()
            file2.createNewFile()
        }
        val fos = FileOutputStream(file2)
        val data = input.readBytes()
        fos.write(data)
    }

    @Test
    fun testTrunkCRCInFile() {
        val data = File("elephant2.png").readBytes()
        val data2 = ByteArray(data.size - 8)
        data.copyInto(data2, 0, 4, data.size - 4)
        val crc = CRC32().apply {
            reset()
            update(data2)
        }
        val buffer = ByteBuffer.wrap(data)
        buffer.position(data.size - 4)
        val value = buffer.int
        println(String.format("%x %x", value, crc.value))
        assert(value == crc.value.toInt())
    }

    @Test
    fun testCRC2() {
        val data = ByteBuffer.wrap(File("elephant.png").readBytes())
        val obj = APngObject(data)
        val frame = obj.getFrame(1)
        frame.chunks.forEach {
            println(it)
            assertEquals(it.crc, it.computeCRC())
        }
    }
}