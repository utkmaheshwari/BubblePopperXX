package com.example.bubblepopperxx;

public class Letter {
	public String item;
	public int xToMove, yToMove, xPos, yPos;
	public boolean colorBit;

	public Letter(String l,int middle) {
		item = l;
		xToMove = yToMove = xPos = 0;
		this.yPos=middle;
		colorBit = true;
	}
}
