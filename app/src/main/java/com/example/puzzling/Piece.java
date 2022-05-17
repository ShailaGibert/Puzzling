package com.example.puzzling;

import android.content.Context;

public class Piece extends androidx.appcompat.widget.AppCompatImageView  {
    public int xCoord;
    public int yCoord;
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;

    public Piece(Context context) { super(context); }
}
