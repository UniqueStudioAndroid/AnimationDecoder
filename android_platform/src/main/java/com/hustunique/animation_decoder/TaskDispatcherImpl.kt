package com.hustunique.animation_decoder

import android.util.Log
import com.hustunique.animation_decoder.api.Task
import com.hustunique.animation_decoder.api.TaskDispatcher
import java.util.concurrent.Executors

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

class TaskDispatcherImpl : TaskDispatcher {

    companion object {
        private const val TAG = "TaskDispatcherImpl"
    }

    val executor = Executors.newFixedThreadPool(5)


    override fun dispatch(t: Task) {
        executor.execute {
            Log.i(TAG, "dispatch: start at ${Thread.currentThread()}")
            val s = System.currentTimeMillis()
            t.run()
            Log.i(
                TAG,
                "dispatch: end cost ${System.currentTimeMillis() - s} ${Thread.currentThread()}"
            )
        }
    }

}