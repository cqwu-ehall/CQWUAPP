package com.ucpeo.meal.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;

/**
 * Created on 2016/2/24.
 * 生成二维码的工具类
 */
public class MakeQRCodeUtil {
    /**
     * 转换位图
     *
     * @param matrix  原图
     * @param alpha 透明度 0 透明 1半透明 2不透明
     * @return
     */
    private static Bitmap bitMatrix2Bitmap(BitMatrix matrix, int alpha) {
        int WIDTH = matrix.getWidth();
        int HEIGHT = matrix.getHeight();
        int[] pixels = new int[WIDTH * HEIGHT];
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < HEIGHT; x++) {
                int color;
                switch (alpha){
                    case 0:
                        color = 0x00FFFFFF;
                        break;
                    case 1:
                        color = 0x80FFFFFF;
                        break;
                    default:
                        color = 0xFFFFFFFF;
                        break;
                }
                if (matrix.get(x, y)) {
                    // 有内容的部分，颜色设置为黑色，当然这里可以自己修改成喜欢的颜色
                    color = 0xFF000000;
                }
                pixels[x + (y * WIDTH)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        return bitmap;
    }

    /**
     * 设置透明度
     *
     * @param alpha    透明度
     * @return Paint    画笔
     */
    private static Paint setAlpha(int alpha) {
        // 建立Paint 物件
        Paint vPaint = new Paint();
        vPaint .setStyle( Paint.Style.STROKE );   //空心
        vPaint .setAlpha(alpha);   //0—255
        return vPaint;
    }

    /**
     * 设置透明度
     *
     * @param alpha    透明度  1 不透明 0 半透明
     * @return Paint    画笔
     */
    private static Paint selectAlpha(int alpha) {
        if(alpha == 0){
            return setAlpha(75);
        }else{
            return setAlpha(255);
        }
    }

    /**
     * 添加logo
     *
     * @param src 原图
     * @param logo logo图
     * @return 效果图
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (logo == null) {
            return src;
        }
        // 获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        // logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    /**
     * 从资源文件中获取图片
     *
     * @param context    上下文
     * @param drawableId 资源文件id
     * @return
     */
    public static Bitmap gainBitmap(Context context, int drawableId) {
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
                drawableId);
        return bmp;
    }

    /**
     * 在图片右下角添加水印
     *
     * @param srcBMP  原图
     * @param markBMP 水印图片
     * @return 合成水印后的图片
     */
    public static Bitmap composeWatermark(Bitmap srcBMP, Bitmap markBMP) {
        if (srcBMP == null) {
            return null;
        }
        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap newb = Bitmap.createBitmap(srcBMP.getWidth(),
                srcBMP.getHeight(), Bitmap.Config.ARGB_8888);

        //获取透明度
        Paint vPaint = selectAlpha(0);

        Canvas cv = new Canvas(newb);
        // 在 0，0坐标开始画入原图
        cv.drawBitmap(srcBMP, 0, 0, null);
        // 在原图的右下角画入水印
        cv.drawBitmap(markBMP, srcBMP.getWidth() - markBMP.getWidth() * 3 / 5,
                srcBMP.getHeight() * 3 / 7, vPaint);
        // 保存
        cv.save();
        // 存储
        cv.restore();
        return newb;
    }

    /**
     * 给二维码图片加背景
     *
     * @param foreground 原图
     * @param background 背景图
     * @return 效果图
     */
    public static Bitmap addBackground(Bitmap foreground, Bitmap background) {
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int fgWidth = foreground.getWidth();
        int fgHeight = foreground.getHeight();
        Bitmap newmap = Bitmap
                .createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newmap);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(foreground, (bgWidth - fgWidth) / 2,
                (bgHeight - fgHeight) * 2 / 5 + 70, null);
        canvas.save();
        canvas.restore();
        return newmap;
    }

    /**
     * 生成带logo的二维码
     *
     * @param logo logo图
     * @param content   内容
     * @param width 宽度
     * @param height 高度
     * @param alpha 透明度
     * @param isEdge 去白边    true 去除 false 保留
     * @return 二维码
     */
    public static Bitmap generateQRCode(Bitmap logo, String content, int width, int height, int alpha, boolean isEdge) {
        return  addLogo(generateQRCode(content, width, height,alpha,isEdge), logo);
    }

    /**
     * 生成不带logo的二维码
     *
     * @param content 内容
     * @param width 宽度
     * @param height 高度
     * @param alpha 透明度
     * @param isEdge 去白边    true 去除 false 保留
     * @return 二维码
     */
    public static Bitmap generateQRCode(String content, int width, int height, int alpha, boolean isEdge) {
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            // 设置编码方式utf-8
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //设置二维码的纠错级别为h
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix matrix;
            if(isEdge){
                matrix = new QRCodeWriter().encode(content,BarcodeFormat.QR_CODE, width, height, hints);
            }else{
                matrix = new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE, width, height, hints);
            }
            return bitMatrix2Bitmap(matrix, alpha);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加文字
     *
     * @param bmpSrc 二维码图
     * @param text 文字
     * @return 效果图
     */
    public static Bitmap addTextToBitmap(Bitmap bmpSrc, String text) {
        int srcWidth = bmpSrc.getWidth();
        int srcHeight = bmpSrc.getHeight();

        // 先计算text所需要的height
        int textSize = 20;
        int padding = 3;
        int textLinePadding = 1;
        // 每行的文字
        int perLineWords = (srcWidth - 2 * padding) / textSize;
        int lineNum = text.length() / perLineWords;
        lineNum = text.length() % perLineWords == 0 ? lineNum : lineNum + 1;
        int textTotalHeight = lineNum * (textSize + textLinePadding) + 2 * padding;

        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight + textTotalHeight,
                Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bmpSrc, 0, 0, null);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setTextSize(textSize);
            String lineText;
            for (int i = 0, startY = srcHeight + textSize, start, end; i < lineNum; i++) {
                start = i * perLineWords;
                end = start + perLineWords;
                lineText = text.substring(start, end > text.length() ? text.length() : end);
                canvas.drawText(lineText, padding, startY, paint);
                startY += textSize + textLinePadding;
            }
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }
}
