package com.example.angel.carnavigation.CameraUtils;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.renderscript.Allocation;

import java.nio.ByteBuffer;

public class Yuv2RgbConverter {

    private static int pixelCount = -1;
    private static ByteBuffer yuvBuffer;
    private static Allocation inputAllocation;
    private static Allocation outputAllocation;

    public static byte[] yuvToByteArray(Image image) {
        if (yuvBuffer == null){
            pixelCount = image.getCropRect().width() * image.getCropRect().height();
            // Bits per pixel is an average for the whole image, so it's useful to compute the size
            // of the full buffer but should not be used to determine pixel offsets
            int pixelSizeBits = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888);
            yuvBuffer = ByteBuffer.allocateDirect(pixelCount * pixelSizeBits / 8);
        }
        yuvBuffer.rewind();

        byte[] byteBuffer = imageToByteBuffer(image, yuvBuffer.array());

        return byteBuffer;

    }

    private static byte[] imageToByteBuffer(Image image, byte[] outputBuffer) {
        if ((image.getFormat() != ImageFormat.YUV_420_888)) throw new AssertionError();

        Rect imageCrop = image.getCropRect();
        Image.Plane[] imagePlanes = image.getPlanes();

        for (int i = 0; i < imagePlanes.length; i++) {
            int outputStride = 0;
            int outputOffset = 0;

            switch (i){
                case 0:
                    outputStride = 1;
                    outputOffset = 0;
                    break;
                case 1:
                    outputStride = 2;
                    outputOffset = pixelCount + 1;
                    break;
                case 2:
                    outputStride = 2;
                    outputOffset = pixelCount;
                    break;
            }

            ByteBuffer planeBuffer = imagePlanes[i].getBuffer();
            int rowStride = imagePlanes[i].getRowStride();
            int pixelStride = imagePlanes[i].getPixelStride();
            Rect planeCrop;

            if (i == 0) {
                planeCrop = imageCrop;
            } else {
                planeCrop = new Rect(
                        imageCrop.left / 2,
                        imageCrop.top / 2,
                        imageCrop.right / 2,
                        imageCrop.bottom / 2);
            }

            int planeWidth = planeCrop.width();
            int planeHeight = planeCrop.height();

            byte[] rowBuffer = new byte[imagePlanes[i].getRowStride()];

            int rowLength;

            if (pixelStride == 1 && outputStride == 1) {
                rowLength = planeWidth;
            } else {
                rowLength =  (planeWidth - 1) * pixelStride + 1;
            }

            for (int row=0; row<planeHeight; row++){
                // Move buffer position to the beginning of this row
                planeBuffer.position(
                        (row + planeCrop.top) * rowStride + planeCrop.left * pixelStride);

                if (pixelStride == 1 && outputStride == 1) {
                    // When there is a single stride value for pixel and output, we can just copy
                    // the entire row in a single step
                    planeBuffer.get(outputBuffer, outputOffset, rowLength);
                    outputOffset += rowLength;
                } else {
                    // When either pixel or output have a stride > 1 we must copy pixel by pixel
                    planeBuffer.get(rowBuffer, 0, rowLength);
                    for (int col = 0; col < planeWidth; col++){
                        outputBuffer[outputOffset] = rowBuffer[col * pixelStride];
                        outputOffset += outputStride;
                    }
                }
            }
        }
        return outputBuffer;
    }
}
