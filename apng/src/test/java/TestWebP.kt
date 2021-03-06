import com.hustunique.animation_decoder.apng.webp.WebPParser
import org.jetbrains.annotations.TestOnly
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer

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

class TestWebP {

    @Test
    fun testDecode() {
        val buffer = ByteBuffer.wrap(File("ani_test.webp").readBytes())
        val parser = WebPParser<Int>()
        var count = 0
        parser.parse(buffer).createFrames {
            val file = File("ani_test$count.webp")
            if (!file.exists()) file.createNewFile()
            file.writeBytes(it.readBytes())
            count++
        }
    }
}