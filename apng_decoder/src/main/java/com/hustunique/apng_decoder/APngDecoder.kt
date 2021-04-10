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
import java.io.InputStream
import java.lang.Exception
import java.nio.ByteBuffer
import kotlin.jvm.Throws

class APngDecoder {

    @Throws(IllegalStateException::class)
    fun decode(data: ByteBuffer) : InputStream {
        val obj = APngObject(data)
        return obj.frameDataStream(1)

//        return try {
//            BitmapFactory.decodeStream(stream)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
    }
}
