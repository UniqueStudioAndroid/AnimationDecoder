package com.hustunique.apng_decoder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

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
}