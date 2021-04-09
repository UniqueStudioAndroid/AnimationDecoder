package com.hustunique.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hustunique.apng_decoder.APngDecoder
import com.hustunique.myapplication.databinding.ActivityMainBinding
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decoder = APngDecoder()
        val bitmap = decoder.decode(ByteBuffer.wrap(assets.open("elephant.png").readBytes()))

        setContentView(binding.root)
        binding.img.setImageBitmap(bitmap)
    }
}