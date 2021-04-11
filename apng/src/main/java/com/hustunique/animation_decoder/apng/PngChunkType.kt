package com.hustunique.animation_decoder.apng

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

internal object PngChunkType {
    const val TYPE_IHDR = 0x49484452
    const val TYPE_IDAT = 0x49444154
    const val TYPE_IEND = 0x49454E44
    const val TYPE_PLTE = 0x504C5445
    const val TYPE_ACTL = 0x6163544C
    const val TYPE_FCTL = 0x6663544C
    const val TYPE_FDAT = 0x66644154
    const val TYPE_TEXT = 0x74455874
}