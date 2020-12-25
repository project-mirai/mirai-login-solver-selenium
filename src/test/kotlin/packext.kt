@file:JvmName("PackExt")

package net.mamoe.mirai.selenium.test

import java.io.File
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun File.raf(): RandomAccessFile = RandomAccessFile(this, "rw")
fun OutputStream.zip() = ZipOutputStream(this)
fun File.read(): String = bufferedReader().use { it.readLine() }
fun File.write(msg: String) = bufferedWriter().use { it.append(msg) }

fun main() {
    val modifyTime = File("build/ext-modify-time")
    val dir = File("src/mirai-selenium-ext")
    runCatching {
        if (modifyTime.read() == dir.lastModified().toString()) {
            println("Skipped")
            return
        }
    }

    val resources = dir.toPath()
    val output = File("src/main/resources/mirai-selenium-ext.zip")
    output.parentFile.mkdirs()
    println("Zipping...")
    RAFOutputStream(output.raf()).buffered().zip().use { zip ->
        Files.walk(resources).filter {
            it.fileName.toString() != "index.d.ts"
        }.forEach { path ->
            val name = resources.relativize(path).toString().replace('\\', '/')
            if (name != "/" && name.isNotBlank()) {
                if (Files.isDirectory(path)) {
                    zip.putNextEntry(ZipEntry("$name/"))
                } else {
                    zip.putNextEntry(ZipEntry(name))
                    Files.newInputStream(path).use { it.copyTo(zip) }
                    println(" <- $name")
                }
            }
        }
    }
    modifyTime.write(dir.lastModified().toString())
    File("src/main/resources/mirai-selenium-ext.update-time").bufferedWriter().use { writer ->
        writer.append(System.currentTimeMillis().toString())
    }
}
