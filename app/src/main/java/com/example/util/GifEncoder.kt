package com.example.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Pure Kotlin Animated GIF Encoder (GIF89a standard).
 * Supports multi-frame encoding, custom frame delays, and infinite looping.
 */
class GifEncoder {

    private var width: Int = 0
    private var height: Int = 0
    private var delayCentiseconds: Int = 20 // 20 cs = 200 ms
    private var repeatCount: Int = 0 // 0 = infinite loop
    private val frames = mutableListOf<Bitmap>()

    fun setDelay(ms: Int) {
        delayCentiseconds = (ms / 10).coerceAtLeast(2)
    }

    fun setRepeat(repeat: Int) {
        repeatCount = repeat
    }

    fun addFrame(bitmap: Bitmap): Boolean {
        frames.add(bitmap)
        return true
    }

    /**
     * Encodes all added frames to the output stream.
     */
    fun encode(outputStream: OutputStream): Boolean {
        if (frames.isEmpty()) return false
        try {
            width = frames[0].width
            height = frames[0].height

            // 1. GIF Header "GIF89a"
            writeString(outputStream, "GIF89a")

            // We generate a global 256-color palette using 6x6x6 color cube + 40 shades of gray
            val palette = generateUniformPalette()

            // 2. Logical Screen Descriptor (7 bytes)
            writeShort(outputStream, width)
            writeShort(outputStream, height)
            // Packed field: Global Color Table Flag (1), Color Resolution (111 = 8 bits), Sort (0), GCT Size (111 = 256 colors)
            outputStream.write(0xF7)
            outputStream.write(0) // Background color index
            outputStream.write(0) // Pixel aspect ratio

            // 3. Global Color Table (256 * 3 = 768 bytes)
            outputStream.write(palette)

            // 4. Netscape 2.0 Application Extension for Infinite Looping (19 bytes)
            if (repeatCount >= 0) {
                outputStream.write(0x21) // Extension Introducer
                outputStream.write(0xFF) // Application Extension Label
                outputStream.write(11)   // Block Size
                writeString(outputStream, "NETSCAPE2.0")
                outputStream.write(3)    // Sub-block Length
                outputStream.write(1)    // Sub-block ID
                writeShort(outputStream, repeatCount) // Loop count (0 = infinite)
                outputStream.write(0)    // Block Terminator
            }

            // 5. Write each frame
            for (frame in frames) {
                writeFrame(frame, outputStream)
            }

            // 6. GIF Trailer (1 byte)
            outputStream.write(0x3B)
            outputStream.flush()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun writeFrame(frame: Bitmap, outputStream: OutputStream) {
        // Graphic Control Extension (8 bytes)
        outputStream.write(0x21) // Extension Introducer
        outputStream.write(0xF9) // Graphic Control Label
        outputStream.write(4)    // Block Size
        // Packed: Disposal Method = 2 (Restore to background), User Input = 0, Transparent Color = 0
        outputStream.write(0x08)
        writeShort(outputStream, delayCentiseconds) // Frame delay in 1/100 sec
        outputStream.write(0)    // Transparent color index
        outputStream.write(0)    // Block Terminator

        // Image Descriptor (10 bytes)
        outputStream.write(0x2C) // Image Separator
        writeShort(outputStream, 0) // Left position
        writeShort(outputStream, 0) // Top position
        writeShort(outputStream, width)
        writeShort(outputStream, height)
        outputStream.write(0)    // Packed field: No local color table, not interlaced

        // Convert Bitmap pixels to 8-bit palette indices
        val pixels = IntArray(width * height)
        frame.getPixels(pixels, 0, width, 0, 0, width, height)
        val indexedPixels = ByteArray(pixels.size)
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            indexedPixels[i] = mapRgbToPaletteIndex(r, g, b).toByte()
        }

        // LZW Compression
        val lzw = LzwEncoder(width, height, indexedPixels, 8)
        lzw.encode(outputStream)
    }

    private fun writeString(os: OutputStream, s: String) {
        for (c in s) {
            os.write(c.code)
        }
    }

    private fun writeShort(os: OutputStream, value: Int) {
        os.write(value and 0xFF)
        os.write((value shr 8) and 0xFF)
    }

    companion object {
        /**
         * Generates a 256-color palette (6x6x6 color cube = 216 colors, plus 40 grayscale levels).
         */
        private fun generateUniformPalette(): ByteArray {
            val palette = ByteArray(256 * 3)
            var index = 0

            // 6x6x6 Color Cube (216 colors)
            for (r in 0 until 6) {
                for (g in 0 until 6) {
                    for (b in 0 until 6) {
                        palette[index * 3] = (r * 255 / 5).toByte()
                        palette[index * 3 + 1] = (g * 255 / 5).toByte()
                        palette[index * 3 + 2] = (b * 255 / 5).toByte()
                        index++
                    }
                }
            }

            // 40 Grayscale Shades (fill remaining entries up to 256)
            for (i in 0 until 40) {
                val gray = (i * 255 / 39).toByte()
                palette[index * 3] = gray
                palette[index * 3 + 1] = gray
                palette[index * 3 + 2] = gray
                index++
            }

            return palette
        }

        /**
         * Maps an RGB color to the closest index in our uniform palette.
         */
        private fun mapRgbToPaletteIndex(r: Int, g: Int, b: Int): Int {
            // Check if pixel is close to grayscale
            val diffRG = kotlin.math.abs(r - g)
            val diffGB = kotlin.math.abs(g - b)
            val diffRB = kotlin.math.abs(r - b)
            if (diffRG < 10 && diffGB < 10 && diffRB < 10) {
                val gray = (r + g + b) / 3
                val grayIndex = (gray * 39 / 255).coerceIn(0, 39)
                return 216 + grayIndex
            }

            // Quantize to 6x6x6 color cube
            val r6 = (r * 5 + 127) / 255
            val g6 = (g * 5 + 127) / 255
            val b6 = (b * 5 + 127) / 255
            return (r6 * 36) + (g6 * 6) + b6
        }
    }

    /**
     * LZW Encoder for GIF89a.
     * Compresses indexed pixel data into LZW variable-length codes and blocks.
     */
    private class LzwEncoder(
        private val imgW: Int,
        private val imgH: Int,
        private val pixAry: ByteArray,
        private val initCodeSize: Int
    ) {
        private var remaining = imgW * imgH
        private var curPixel = 0

        // LZW parameters
        private var clearCode = 1 shl initCodeSize
        private var eofCode = clearCode + 1
        private var nBits = initCodeSize + 1
        private var maxCode = (1 shl nBits) - 1

        private val hSize = 5003 // 80% occupancy hash table
        private val hTab = IntArray(hSize)
        private val codeTab = IntArray(hSize)

        private var freeEnt = 0
        private var clearFlg = false

        // Output buffer
        private var curAccum = 0
        private var curBits = 0
        private val accum = ByteArray(256)
        private var aCount = 0

        fun encode(os: OutputStream) {
            os.write(initCodeSize) // Initial code size byte

            remaining = imgW * imgH
            curPixel = 0

            compress(initCodeSize + 1, os)
            os.write(0) // End of sub-blocks
        }

        private fun nextPixel(): Int {
            if (remaining == 0) return -1
            remaining--
            val pix = pixAry[curPixel++].toInt() and 0xFF
            return pix
        }

        private fun compress(initBits: Int, os: OutputStream) {
            nBits = initBits
            maxCode = (1 shl nBits) - 1
            clearCode = 1 shl (initBits - 1)
            eofCode = clearCode + 1
            freeEnt = clearCode + 2
            aCount = 0

            var ent = nextPixel()
            var hshift = 0
            var fcode = hSize
            while (fcode < 65536) {
                hshift++
                fcode *= 2
            }
            hshift = 8 - hshift

            for (i in 0 until hSize) hTab[i] = -1

            output(clearCode, os)

            var c = nextPixel()
            while (c != -1) {
                fcode = (c shl 12) + ent
                var i = (c shl hshift) xor ent

                if (hTab[i] == fcode) {
                    ent = codeTab[i]
                    c = nextPixel()
                    continue
                } else if (hTab[i] >= 0) {
                    var disp = hSize - i
                    if (i == 0) disp = 1
                    var matched = false
                    do {
                        i -= disp
                        if (i < 0) i += hSize
                        if (hTab[i] == fcode) {
                            ent = codeTab[i]
                            matched = true
                            break
                        }
                    } while (hTab[i] >= 0)
                    if (matched) {
                        c = nextPixel()
                        continue
                    }
                }

                output(ent, os)
                ent = c
                if (freeEnt < 4096) {
                    codeTab[i] = freeEnt++
                    hTab[i] = fcode
                } else {
                    clBlock(os)
                }
                c = nextPixel()
            }

            output(ent, os)
            output(eofCode, os)
        }

        private fun output(code: Int, os: OutputStream) {
            curAccum = curAccum or (code shl curBits)
            curBits += nBits

            while (curBits >= 8) {
                charOut((curAccum and 0xFF).toByte(), os)
                curAccum = curAccum shr 8
                curBits -= 8
            }

            if (freeEnt > maxCode || clearFlg) {
                if (clearFlg) {
                    nBits = initCodeSize + 1
                    maxCode = (1 shl nBits) - 1
                    clearFlg = false
                } else {
                    nBits++
                    maxCode = if (nBits == 12) 4096 else (1 shl nBits) - 1
                }
            }

            if (code == eofCode) {
                while (curBits > 0) {
                    charOut((curAccum and 0xFF).toByte(), os)
                    curAccum = curAccum shr 8
                    curBits -= 8
                }
                flushChar(os)
            }
        }

        private fun clBlock(os: OutputStream) {
            for (i in 0 until hSize) hTab[i] = -1
            freeEnt = clearCode + 2
            clearFlg = true
            output(clearCode, os)
        }

        private fun charOut(c: Byte, os: OutputStream) {
            accum[aCount++] = c
            if (aCount >= 254) flushChar(os)
        }

        private fun flushChar(os: OutputStream) {
            if (aCount > 0) {
                os.write(aCount)
                os.write(accum, 0, aCount)
                aCount = 0
            }
        }
    }
}
