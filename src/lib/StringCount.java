package lib;

import java.awt.Point;

public class StringCount {
	public String str;
	public int cnt;
	public Point target;
	public Point base;
	int index;
	public int senteWinCnt;
	public StringCount(String s, int isSenteWin) {
		str = s;
		cnt = 1;
		if(isSenteWin == 1) {
			senteWinCnt = 1;
		}
		else if(isSenteWin == 0) {
			senteWinCnt = 0;
		}
	}
}