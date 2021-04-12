package com.hustunique.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hustunique.animation_decoder.AnimatedImageDrawable
import com.hustunique.animation_decoder.Obj
import com.hustunique.myapplication.databinding.ActivityMainBinding
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val buffer = ByteBuffer.wrap(assets.open("elephant.png").readBytes())//.array()
//        val bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
        val frameList = Obj.decoder.decode(buffer)
        val d = AnimatedImageDrawable(frameList)
        binding.img.setImageDrawable(d)
        binding.button.setOnClickListener {
            if (d.isRunning) {
                d.stop()
            } else {
                d.start()
            }
        }
    }
}