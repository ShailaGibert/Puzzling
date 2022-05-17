package com.example.puzzling;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Puzzle extends AppCompatActivity {

    ArrayList<Piece> pieces;
    //private int rows, columns;
    String assetName = "bobby-burch-145906-unsplash.jpg";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puzzle);
        final RelativeLayout layout = findViewById(R.id.relativeLayout);
        ImageView imageView = findViewById(R.id.puzzle);
        imageView.post(() -> {
            configure(assetName, imageView);
            pieces = splitImage(4, 3);
            PieceManagement pieceManagement = new PieceManagement(Puzzle.this);

            Collections.shuffle(pieces);
            for (Piece piece : pieces) {
                piece.setOnTouchListener(pieceManagement);
                layout.addView(piece);
                // randomize position, on the bottom of the screen
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                        piece.getLayoutParams();
                lParams.leftMargin = new Random().nextInt(layout.getWidth() -
                        piece.pieceWidth);
                lParams.topMargin = layout.getHeight() - piece.pieceHeight;
                piece.setLayoutParams(lParams);
            }
            imageView.setAlpha(0.5f);
        });

    }

    private ArrayList<Piece> splitImage(int rows, int columns) {

        int pieceAmount = rows * columns;

        ImageView imageView = findViewById(R.id.puzzle);
        ArrayList<Piece> pieces = new ArrayList<>(pieceAmount);

        // Gets the bitmap from image
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        int[] dimensions = obtainImageBitmaps(imageView);
        int leftBitmapDimension = dimensions[0];
        int rightBitmapDimension = dimensions[1];
        int widthBitmapDimension = dimensions[2];
        int heightBitmapDimension = dimensions[3];

        int newWidth = widthBitmapDimension - 2 * abs(leftBitmapDimension);
        int newHeight = heightBitmapDimension - 2 * abs(rightBitmapDimension);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, widthBitmapDimension, heightBitmapDimension, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(leftBitmapDimension), abs(rightBitmapDimension), newWidth, newHeight);

        // Gets the pieces' height and width
        int pieceWidth = newWidth/columns;
        int pieceHeight = newHeight/rows;

        // Appends each piece to the resulting array
        int yCoordinate = 0;
        for (int row = 0; row < rows; row++) {
            int xCoordinate = 0;
            for (int column = 0; column < columns; column++) {
                int offsetX = 0;
                int offsetY = 0;
                if (column > 0) {
                    offsetX = pieceWidth / 3;
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3;
                }

                // applies the movement
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoordinate - offsetX,
                        yCoordinate - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);
                Piece piece = new Piece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoordinate - offsetX + imageView.getLeft();
                piece.yCoord = yCoordinate - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX,
                        pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                //
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row != 0) {
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3f, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6f, offsetY -
                            bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6f * 5,
                            offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX)
                                    / 3f * 2, offsetY);
                }
                path.lineTo(pieceBitmap.getWidth(), offsetY);

                if (column != columns - 1) {
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() -
                            offsetY) / 3f);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize,offsetY +
                            (pieceBitmap.getHeight() - offsetY) / 6f, pieceBitmap.getWidth() -
                            bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6f * 5,
                            pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY)
                                    / 3f * 2);
                }
                path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());

                if (row != rows - 1) {
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3f * 2,
                            pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6f * 5,
                            pieceBitmap.getHeight() - bumpSize, offsetX +
                                    (pieceBitmap.getWidth() - offsetX) / 6f,
                            pieceBitmap.getHeight() - bumpSize, offsetX +
                                    (pieceBitmap.getWidth() - offsetX) / 3f,
                            pieceBitmap.getHeight());
                }
                path.lineTo(offsetX, pieceBitmap.getHeight());

                if (column != 0) {
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3f * 2);
                    path.cubicTo(offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() -
                            offsetY) / 6f * 5, offsetX - bumpSize, offsetY +
                            (pieceBitmap.getHeight() - offsetY) / 6f, offsetX, offsetY +
                            (pieceBitmap.getHeight() - offsetY) / 3f);
                }
                path.close();

                Paint paint = new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                piece.setImageBitmap(puzzlePiece);

                pieces.add(piece);
                xCoordinate += pieceWidth;
            }
            yCoordinate += pieceHeight;
        }

        return pieces;
    }

    private void configure(String assetName, ImageView imageView) {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        AssetManager am = getAssets();
        try {
            InputStream is = am.open("img/" + assetName);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scale = Math.min(photoW/targetW, photoH/targetH);

            is.reset();

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scale;

            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(-1, -1, -1,
                    -1), bmOptions);
            imageView.setImageBitmap(bitmap);
            imageView.setAlpha(0.5f);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int[] obtainImageBitmaps(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (imgViewH - actH) /2;
        int left;
        left = (imgViewW - actW) /2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

    public void checkGameOver() {
        if (isPuzzleCompleted()) {
            Toast.makeText(this, "You did it!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isPuzzleCompleted() {
        for (Piece piece : pieces) {
            if (piece.canMove) {
                return false;
            }
        }
        return true;
    }
}