package net.mamoe.mirai.selenium.test

import java.io.OutputStream
import java.io.RandomAccessFile

class RAFOutputStream(
    val raf: RandomAccessFile
) : OutputStream() {
    override fun close() {
        raf.setLength(raf.filePointer)
        raf.close()
    }

    override fun write(b: Int) {
        raf.write(b)
    }

    override fun write(b: ByteArray) {
        raf.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        raf.write(b, off, len)
    }
}
