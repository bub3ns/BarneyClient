package moscow.rockstar.render.gif;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Small, self-contained GIF89a decoder used by the loading overlay.
 * It intentionally accepts streams only; it does not open files, URLs, or
 * any other external resource by itself.
 */
public final class GifDecoder {
    public static final int STATUS_OK = 0;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OPEN_ERROR = 2;

    private static final int MAX_LZW_CODE_SIZE = 4096;

    private BufferedInputStream input;
    private int status;
    private int width;
    private int height;
    private boolean globalColorTablePresent;
    private int globalColorTableSize;
    private int backgroundColorIndex;
    private int pixelAspectRatio;
    private int[] globalColorTable;
    private int[] localColorTable;
    private int[] activeColorTable;
    private int backgroundColor;
    private int previousBackgroundColor;
    private boolean transparency;
    private int transparentColorIndex;
    private int delayMillis;
    private int disposalMethod;
    private int previousDisposalMethod;
    private Rectangle previousFrameBounds;
    private BufferedImage image;
    private BufferedImage previousImage;
    private byte[] pixelIndices;
    private int blockSize;
    private final List<Frame> frames = new ArrayList<>();

    private short[] prefixTable;
    private byte[] suffixTable;
    private byte[] pixelStack;
    private final byte[] block = new byte[256];

    public int read(InputStream stream) {
        reset();
        if (stream == null) {
            status = STATUS_OPEN_ERROR;
            return status;
        }

        input = stream instanceof BufferedInputStream
            ? (BufferedInputStream) stream
            : new BufferedInputStream(stream);
        try {
            readHeader();
            if (!hasError()) {
                readContents();
            }
        } catch (IOException exception) {
            status = STATUS_FORMAT_ERROR;
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
                // The decoded frames are still usable when closing the source fails.
            }
            input = null;
        }
        return status;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public int getDelay(int frameIndex) {
        return frameIndex >= 0 && frameIndex < frames.size() ? frames.get(frameIndex).delayMillis() : -1;
    }

    public BufferedImage getFrame(int frameIndex) {
        return frameIndex >= 0 && frameIndex < frames.size() ? frames.get(frameIndex).image() : null;
    }

    public BufferedImage getImage() {
        return getFrame(0);
    }

    public Dimension getSize() {
        return new Dimension(width, height);
    }

    public int getStatus() {
        return status;
    }

    public List<Frame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    private void reset() {
        status = STATUS_OK;
        width = 0;
        height = 0;
        globalColorTablePresent = false;
        globalColorTableSize = 0;
        backgroundColorIndex = 0;
        pixelAspectRatio = 0;
        globalColorTable = null;
        localColorTable = null;
        activeColorTable = null;
        backgroundColor = 0;
        previousBackgroundColor = 0;
        transparency = false;
        transparentColorIndex = 0;
        delayMillis = 0;
        disposalMethod = 0;
        previousDisposalMethod = 0;
        previousFrameBounds = null;
        image = null;
        previousImage = null;
        pixelIndices = null;
        blockSize = 0;
        frames.clear();
    }

    private boolean hasError() {
        return status != STATUS_OK;
    }

    private int readByte() throws IOException {
        int value = input.read();
        if (value < 0) {
            status = STATUS_FORMAT_ERROR;
            return 0;
        }
        return value;
    }

    private int readUnsignedShort() throws IOException {
        return readByte() | readByte() << 8;
    }

    private void readHeader() throws IOException {
        StringBuilder signature = new StringBuilder(6);
        for (int index = 0; index < 6; index++) {
            signature.append((char) readByte());
        }
        if (!signature.toString().startsWith("GIF")) {
            status = STATUS_FORMAT_ERROR;
            return;
        }

        width = readUnsignedShort();
        height = readUnsignedShort();
        int packedFields = readByte();
        globalColorTablePresent = (packedFields & 0x80) != 0;
        globalColorTableSize = 2 << (packedFields & 0x07);
        backgroundColorIndex = readByte();
        pixelAspectRatio = readByte();

        if (globalColorTablePresent && !hasError()) {
            globalColorTable = readColorTable(globalColorTableSize);
            if (globalColorTable != null && backgroundColorIndex < globalColorTable.length) {
                backgroundColor = globalColorTable[backgroundColorIndex];
            }
        }
    }

    private int[] readColorTable(int colorCount) throws IOException {
        int byteCount = 3 * colorCount;
        byte[] colorBytes = new byte[byteCount];
        int bytesRead = input.read(colorBytes);
        if (bytesRead < byteCount) {
            status = STATUS_FORMAT_ERROR;
            return null;
        }

        int[] colors = new int[256];
        int offset = 0;
        for (int index = 0; index < colorCount; index++) {
            int red = colorBytes[offset++] & 0xFF;
            int green = colorBytes[offset++] & 0xFF;
            int blue = colorBytes[offset++] & 0xFF;
            colors[index] = 0xFF000000 | red << 16 | green << 8 | blue;
        }
        return colors;
    }

    private void readContents() throws IOException {
        boolean finished = false;
        while (!finished && !hasError()) {
            int blockType = readByte();
            switch (blockType) {
                case 0x2C -> readImage();
                case 0x21 -> {
                    int extensionType = readByte();
                    switch (extensionType) {
                        case 0xF9 -> readGraphicControlExtension();
                        case 0xFF -> readApplicationExtension();
                        default -> skipSubBlocks();
                    }
                }
                case 0x3B -> finished = true;
                case 0x00 -> {
                    // A zero-sized extension is harmless padding in a number of encoders.
                }
                default -> status = STATUS_FORMAT_ERROR;
            }
        }
    }

    private void readGraphicControlExtension() throws IOException {
        readByte();
        int packedFields = readByte();
        disposalMethod = (packedFields & 0x1C) >> 2;
        if (disposalMethod == 0) {
            disposalMethod = 1;
        }
        transparency = (packedFields & 1) != 0;
        delayMillis = readUnsignedShort() * 10;
        transparentColorIndex = readByte();
        readByte();
    }

    private void readApplicationExtension() throws IOException {
        int length = readBlock();
        if (length == 11) {
            StringBuilder applicationId = new StringBuilder(11);
            for (int index = 0; index < 11; index++) {
                applicationId.append((char) block[index]);
            }
            if (applicationId.toString().equals("NETSCAPE2.0")
                || applicationId.toString().equals("ANIMEXTS1.0")) {
                do {
                    readBlock();
                    if (blockSize >= 3 && block[0] == 1) {
                        // The loop count is intentionally not exposed; frames are decoded fully.
                    }
                } while (blockSize > 0 && !hasError());
                return;
            }
        }
        skipSubBlocks();
    }

    private void readImage() throws IOException {
        int frameX = readUnsignedShort();
        int frameY = readUnsignedShort();
        int frameWidth = readUnsignedShort();
        int frameHeight = readUnsignedShort();
        int packedFields = readByte();

        boolean localColorTablePresent = (packedFields & 0x80) != 0;
        boolean interlaced = (packedFields & 0x40) != 0;
        int localColorTableSize = 2 << (packedFields & 0x07);

        localColorTable = null;

        if (localColorTablePresent) {
            localColorTable = readColorTable(localColorTableSize);
            activeColorTable = localColorTable;
        } else {
            activeColorTable = globalColorTable;
            if (backgroundColorIndex == transparentColorIndex) {
                backgroundColor = 0;
            }
        }

        if (activeColorTable == null) {
            status = STATUS_FORMAT_ERROR;
            return;
        }

        int savedTransparentColor = 0;
        if (transparency && transparentColorIndex < activeColorTable.length) {
            savedTransparentColor = activeColorTable[transparentColorIndex];
            activeColorTable[transparentColorIndex] = 0;
        }

        decodeBitmapData(frameWidth, frameHeight);
        if (!hasError()) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
            setPixels(frameX, frameY, frameWidth, frameHeight, interlaced);
            frames.add(new Frame(image, delayMillis));

            // The disposal instruction belongs to this frame and is applied
            // when the next frame is composed.
            previousDisposalMethod = disposalMethod;
            previousFrameBounds = new Rectangle(frameX, frameY, frameWidth, frameHeight);
            previousImage = image;
            previousBackgroundColor = backgroundColor;
        }

        if (transparency && transparentColorIndex < activeColorTable.length) {
            activeColorTable[transparentColorIndex] = savedTransparentColor;
        }
        resetFrameState();
    }

    private void resetFrameState() {
        transparency = false;
        delayMillis = 0;
        disposalMethod = 0;
        localColorTable = null;
        activeColorTable = null;
    }

    private void setPixels(int frameX, int frameY, int frameWidth, int frameHeight, boolean interlaced) {
        int[] destination = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        if (previousDisposalMethod > 0) {
            if (previousDisposalMethod == 3) {
                previousImage = frames.size() > 1 ? frames.get(frames.size() - 2).image() : null;
            }
            if (previousImage != null) {
                int[] previousPixels = ((DataBufferInt) previousImage.getRaster().getDataBuffer()).getData();
                System.arraycopy(previousPixels, 0, destination, 0, width * height);
            }
            if (previousDisposalMethod == 2 && previousFrameBounds != null) {
                Graphics2D graphics = image.createGraphics();
                Color clearColor = transparency
                    ? new Color(0, 0, 0, 0)
                    : new Color(previousBackgroundColor);
                graphics.setColor(clearColor);
                graphics.setComposite(AlphaComposite.Src);
                graphics.fill(previousFrameBounds);
                graphics.dispose();
            }
        }

        int pass = 1;
        int rowIncrement = 8;
        int row = 0;
        for (int sourceRow = 0; sourceRow < frameHeight; sourceRow++) {
            int destinationRow = sourceRow;
            if (interlaced) {
                if (row >= frameHeight) {
                    pass++;
                    switch (pass) {
                        case 2 -> {
                            row = 4;
                            break;
                        }
                        case 3 -> {
                            row = 2;
                            rowIncrement = 4;
                            break;
                        }
                        case 4 -> {
                            row = 1;
                            rowIncrement = 2;
                            break;
                        }
                        default -> {
                        }
                    }
                }
                destinationRow = row;
                row += rowIncrement;
            }

            destinationRow += frameY;
            if (destinationRow >= height) {
                continue;
            }

            int destinationIndex = destinationRow * width + frameX;
            int rowEnd = destinationIndex + frameWidth;
            int imageEnd = destinationRow * width + width;
            if (rowEnd > imageEnd) {
                rowEnd = imageEnd;
            }

            int sourceIndex = sourceRow * frameWidth;
            while (destinationIndex < rowEnd) {
                int color = activeColorTable[pixelIndices[sourceIndex++] & 0xFF];
                if (color != 0) {
                    destination[destinationIndex] = color;
                }
                destinationIndex++;
            }
        }
    }

    private void decodeBitmapData(int frameWidth, int frameHeight) throws IOException {
        int pixelCount = frameWidth * frameHeight;
        if (pixelIndices == null || pixelIndices.length < pixelCount) {
            pixelIndices = new byte[pixelCount];
        }
        if (prefixTable == null) {
            prefixTable = new short[MAX_LZW_CODE_SIZE];
        }
        if (suffixTable == null) {
            suffixTable = new byte[MAX_LZW_CODE_SIZE];
        }
        if (pixelStack == null) {
            pixelStack = new byte[MAX_LZW_CODE_SIZE + 1];
        }

        int minimumCodeSize = readByte();
        if (minimumCodeSize < 2 || minimumCodeSize > 8) {
            status = STATUS_FORMAT_ERROR;
            skipSubBlocks();
            return;
        }

        int clearCode = 1 << minimumCodeSize;
        int endOfInformationCode = clearCode + 1;
        int nextAvailableCode = clearCode + 2;
        int oldCode = -1;
        int codeSize = minimumCodeSize + 1;
        int codeMask = (1 << codeSize) - 1;

        for (int code = 0; code < clearCode; code++) {
            prefixTable[code] = 0;
            suffixTable[code] = (byte) code;
        }

        int datum = 0;
        int bits = 0;
        int blockIndex = 0;
        int bytesInBlock = 0;
        int first = 0;
        int stackSize = 0;
        int pixelIndex = 0;

        while (pixelIndex < pixelCount) {
            if (stackSize == 0) {
                while (bits < codeSize) {
                    if (bytesInBlock == 0) {
                        bytesInBlock = readBlock();
                        blockIndex = 0;
                        if (bytesInBlock <= 0) {
                            break;
                        }
                    }
                    datum += (block[blockIndex] & 0xFF) << bits;
                    bits += 8;
                    blockIndex++;
                    bytesInBlock--;
                }

                if (bits < codeSize) {
                    break;
                }

                int code = datum & codeMask;
                datum >>= codeSize;
                bits -= codeSize;

                if (code > nextAvailableCode || code == endOfInformationCode) {
                    break;
                }
                if (code == clearCode) {
                    codeSize = minimumCodeSize + 1;
                    codeMask = (1 << codeSize) - 1;
                    nextAvailableCode = clearCode + 2;
                    oldCode = -1;
                    continue;
                }
                if (oldCode == -1) {
                    pixelStack[stackSize++] = suffixTable[code];
                    oldCode = code;
                    first = code;
                    continue;
                }

                int inCode = code;
                if (code == nextAvailableCode) {
                    pixelStack[stackSize++] = (byte) first;
                    code = oldCode;
                }
                while (code > clearCode) {
                    pixelStack[stackSize++] = suffixTable[code];
                    code = prefixTable[code] & 0xFFFF;
                }
                first = suffixTable[code] & 0xFF;
                pixelStack[stackSize++] = (byte) first;

                if (nextAvailableCode < MAX_LZW_CODE_SIZE) {
                    prefixTable[nextAvailableCode] = (short) oldCode;
                    suffixTable[nextAvailableCode] = (byte) first;
                    nextAvailableCode++;
                    if ((nextAvailableCode & codeMask) == 0 && nextAvailableCode < MAX_LZW_CODE_SIZE) {
                        codeSize++;
                        codeMask += nextAvailableCode;
                    }
                }
                oldCode = inCode;
            }

            pixelIndices[pixelIndex++] = pixelStack[--stackSize];
        }

        while (pixelIndex < pixelCount) {
            pixelIndices[pixelIndex++] = 0;
        }
        skipSubBlocks();
    }

    private int readBlock() throws IOException {
        blockSize = readByte();
        if (blockSize <= 0) {
            return blockSize;
        }

        int bytesRead = 0;
        while (bytesRead < blockSize) {
            int read = input.read(block, bytesRead, blockSize - bytesRead);
            if (read == -1) {
                status = STATUS_FORMAT_ERROR;
                break;
            }
            bytesRead += read;
        }
        if (bytesRead < blockSize) {
            status = STATUS_FORMAT_ERROR;
        }
        return bytesRead;
    }

    private void skipSubBlocks() throws IOException {
        do {
            readBlock();
        } while (blockSize > 0 && !hasError());
    }

    public record Frame(BufferedImage image, int delayMillis) {
    }
}
