package com.hustunique.animation_decoder.gif

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

object GifChunkType {

    const val TYPE_HEAD_87A = 0x474946383761
    const val TYPE_HEAD_89A = 0x474946383961

    const val TYPE_GRAPHIC_EXT = 0xF9.toByte()

    const val TYPE_EXT_INTRODUCE = 0x21.toByte()

    const val TYPE_TEXT_EXT = 0x01.toByte()
    const val TYPE_IMAGE_DESC = 0x2C.toByte()
    const val TYPE_APPLICATION_EXT = 0xFF.toByte()
    const val TYPE_COMMENT_EXT = 0xFE.toByte()

    const val TYPE_TRAILER = 0x3B.toByte()
}