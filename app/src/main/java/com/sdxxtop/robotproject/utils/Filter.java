package com.sdxxtop.robotproject.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

/**
 * Created by Administrator on 2018/9/21.
 */

public class Filter {

    //     黑白效果函数
    public static Bitmap changeToGray(Bitmap bitmap, int type) {

        int width, height;
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        Bitmap grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(grayBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true); // 设置抗锯齿

        //一，数组矩阵的方法

    /*float[] array = {1, 0, 0, 0, 100,
                     0, 1, 0, 0, 100,
					 0, 0, 1, 0, 0,
					 0, 0, 0, 1, 0};
	ColorMatrix colorMatrix = new ColorMatrix(array);
	*/
        float[] colorFilter = getColorFilter(type);
        //二，把饱和度设置为0 就可以得到灰色（黑白)的图片
        ColorMatrix colorMatrix = new ColorMatrix(colorFilter);
//        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return grayBitmap;
    }

    public static float[] getColorFilter(int type) {
        float[] floats = new float[0];
        switch (type) {
            case 0:   //宝丽来彩色
                floats = new float[]{1.438f, -0.062f, -0.062f, 0, 0,
                        -0.122f, 1.378f, -0.122f, 0, 0,
                        -0.016f, -0.016f, 1.483f, 0, 0,
                        -0.03f, 0.05f, -0.02f, 1, 0};
                break;
            case 1:   // 怀旧
                floats = new float[]{0.393f, 0.769f, 0.189f, 0, 0,
                        0.349f, 0.686f, 0.168f, 0, 0,
                        0.272f, 0.534f, 0.131f, 0, 0,
                        0, 0, 0, 1, 0};
                break;
            case 2:   // 泛红
                floats = new float[]{2f, 0, 0, 0, 0,
                        0, 1f, 0, 0, 0,
                        0, 0, 1f, 0, 0,
                        0, 0, 0, 1f, 0};
                break;
            case 3:   // 荧光绿
                floats = new float[]{1f, 0, 0, 0, 0,
                        0, 1.4f, 0, 0, 0,
                        0, 0, 1f, 0, 0,
                        0, 0, 0, 1f, 0};
                break;
            case 4:   // 宝石蓝
                floats = new float[]{1f, 0, 0, 0, 0,
                        0, 1f, 0, 0, 0,
                        0, 0, 1.6f, 0, 0,
                        0, 0, 0, 1f, 0};
                break;
            case 5:   // 泛黄
                floats = new float[]{1f, 0, 0, 0, 50f,
                        0, 1f, 0, 0, 50f,
                        0, 0, 1f, 0, 0,
                        0, 0, 0, 1f, 0};
                break;
        }
        return floats;
    }

    // 图片素描效果
    public static Bitmap sketch(Bitmap bmp) {
        // 创建新Bitmap
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height]; // 存储变换图像
        int[] linpix = new int[width * height]; // 存储灰度图像
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        // 灰度图像
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {  // 拉普拉斯算子模板 { 0, -1, 0, -1, -5, -1, 0, -1, 0
                // 获取前一个像素颜色
                pixColor = pixels[width * j + i];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                // 灰度图像
                int gray = (int) (0.3 * pixR + 0.59 * pixG + 0.11 * pixB);
                linpix[width * j + i] = Color.argb(255, gray, gray, gray);
                // 图像反向
                gray = 255 - gray;
                pixels[width * j + i] = Color.argb(255, gray, gray, gray);
            }
        }
        int radius = Math.min(width / 2, height / 2);
        int[] copixels = gaussBlur(pixels, width, height, 10, 10 / 3); // 高斯模糊
        // 采用半径10
        int[] result = colorDodge(linpix, copixels); // 素描图像 颜色减淡
        bitmap.setPixels(result, 0, width, 0, 0, width, height);
        return bitmap;
    }

    // 高斯模糊
    public static int[] gaussBlur(int[] data, int width, int height, int radius, float sigma) {
        float pa = (float) (1 / (Math.sqrt(2 * Math.PI) * sigma));
        float pb = -1.0f / (2 * sigma * sigma);

        // generate the Gauss Matrix
        float[] gaussMatrix = new float[radius * 2 + 1];
        float gaussSum = 0f;
        for (int i = 0, x = -radius; x <= radius; ++x, ++i) {
            float g = (float) (pa * Math.exp(pb * x * x));
            gaussMatrix[i] = g;
            gaussSum += g;
        }
        for (int i = 0, length = gaussMatrix.length; i < length; ++i) {
            gaussMatrix[i] /= gaussSum;
        }

        // x direction
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = x + j;
                    if (k >= 0 && k < width) {
                        int index = y * width + k;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];
                        gaussSum += gaussMatrix[j + radius];
                    }
                }

                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);
                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }

        // y direction
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = y + j;
                    if (k >= 0 && k < height) {
                        int index = k * width + x;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];
                        gaussSum += gaussMatrix[j + radius];
                    }
                }

                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);
                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }
        return data;
    }

    // 颜色减淡
    public static int[] colorDodge(int[] baseColor, int[] mixColor) {
        for (int i = 0, length = baseColor.length; i < length; ++i) {
            int bColor = baseColor[i];
            int br = (bColor & 0x00ff0000) >> 16;
            int bg = (bColor & 0x0000ff00) >> 8;
            int bb = (bColor & 0x000000ff);

            int mColor = mixColor[i];
            int mr = (mColor & 0x00ff0000) >> 16;
            int mg = (mColor & 0x0000ff00) >> 8;
            int mb = (mColor & 0x000000ff);

            int nr = colorDodgeFormular(br, mr);
            int ng = colorDodgeFormular(bg, mg);
            int nb = colorDodgeFormular(bb, mb);
            baseColor[i] = nr << 16 | ng << 8 | nb | 0xff000000;
        }
        return baseColor;
    }

    private static int colorDodgeFormular(int base, int mix) {
        int result = base + (base * mix) / (255 - mix);
        result = result > 255 ? 255 : result;
        return result;
    }
}
