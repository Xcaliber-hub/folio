package com.folio.typewriter.ui.editor

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.folio.typewriter.R
import kotlin.random.Random

class TypewriterSounds(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val keys = listOf(R.raw.key_a, R.raw.key_b, R.raw.key_c).map { pool.load(context, it, 1) }
    private val ding = pool.load(context, R.raw.ding, 1)
    private val ret = pool.load(context, R.raw.carriage_return, 1)

    fun key() {
        // Slight pitch + volume wobble so no two strikes sound the same.
        val v = 0.55f + Random.nextFloat() * 0.25f
        pool.play(keys.random(), v, v, 1, 0, 0.92f + Random.nextFloat() * 0.16f)
    }
    fun ding() { pool.play(ding, 0.6f, 0.6f, 2, 0, 1f) }
    fun carriageReturn() { pool.play(ret, 0.7f, 0.7f, 2, 0, 1f) }
    fun release() = pool.release()
}
