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

fun runnable(r: () -> Unit): Runnable = Runnable { TODO("Not yet implemented") }

/**
 * Compute ith byte in value (Default byte order is BitEndian)
 * Note: i start from 1
 */
fun readByteInInt(i: Int, value: Int, isBigEndian: Boolean = true) : Byte {
    val idx = if (isBigEndian) i else 5 - i
    return ((value ushr idx * 8) and 0xFF).toByte()
}
