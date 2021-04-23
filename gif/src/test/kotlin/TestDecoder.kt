import bbs.hustunique.animation_decoder.gif.GifParser
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer

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

class TestDecoder {
    @Test
    fun runParse() {
        val data = ByteBuffer.wrap(File("test_gif.gif").readBytes())
//        val parser = GifParser<Int>()
//        check(parser.handles(data))
//
//        parser.parse(data)
        println("OK")
    }
}