/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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
