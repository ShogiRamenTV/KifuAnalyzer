package lib;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class CanvasBoardForEngine extends Canvas {
	public static final int maxSizeOfKifu = 200;
	public static final int maxScoreOfEngine = 4000;
	public static final int sizeOfOval = 6;
	public BestPointData bestPointFromEngine;
	List<BestPointData> bestPointData = new ArrayList<BestPointData>();
	int selectedIndex;
	ShogiEngine se;
	DefaultListModel<String> listModelEngine;
	JList<String> listBoxKifu;
	public CanvasBoardForEngine(ShogiEngine s, DefaultListModel<String> lmE, JList<String> lbK) {
		se = s;
		selectedIndex = 0;
		listModelEngine = lmE;
		listBoxKifu = lbK;
		bestPointFromEngine = new BestPointData(se.getNumOfMultiPV());
		for(int index=0; index<maxSizeOfKifu; index++) {
			BestPointData bpt = new BestPointData(se.getNumOfMultiPV());
			bestPointData.add(bpt);
		}
	}
	public void paint(Graphics g) {
		drawBaseField(g);
		getPointFromEngine();
		drawPointFromEngine(g);
		drawPointOfCurrentPosition(g);
	}
	private void drawBaseField(Graphics g) {
		g.setColor(Color.black);
		g.drawLine(0, this.getHeight()/2, this.getWidth(), this.getHeight()/2);
		g.drawRect(0, 0, this.getWidth(), this.getHeight());
	}
	private void getPointFromEngine() {
		if(!se.isEngineOn) return;
		int index = listBoxKifu.getSelectedIndex();
		if(bestPointFromEngine.score == 0) return;
		BestPointData bpd = bestPointData.get(index);
		bpd.score = bestPointFromEngine.score;
		for(index=0; index<se.getNumOfMultiPV(); index++) {
			bpd.moveName[index] = bestPointFromEngine.moveName[index];
		}
	}
	private void drawPointFromEngine(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		BasicStroke stroke = new BasicStroke(2.0f);
		g2.setStroke(stroke);
		g2.setColor(Color.blue);
		for(int index = 0; index<maxSizeOfKifu-1; index++) {
			BestPointData bpd = bestPointData.get(index);
			BestPointData bpd2 = bestPointData.get(index+1);
			if(bpd.score == 0 || bpd2.score == 0) continue;
			if(bpd.score > maxScoreOfEngine) bpd.score = maxScoreOfEngine;
			if(bpd.score < (-1 * maxScoreOfEngine) ) bpd.score = (-1 * maxScoreOfEngine);
			
			int convertedIndex = (int)((double)this.getWidth() * ((double)index/(double)maxSizeOfKifu));
			int convertedIndex2 = (int)((double)this.getWidth() * ((double)(index+1)/(double)maxSizeOfKifu));
			int convertedHeight = (int)((double)this.getHeight()/2 * (double)bpd.score/(double)maxScoreOfEngine);
			if((index%2) == 0) convertedHeight *= -1; 
			convertedHeight += this.getHeight()/2;
			int convertedHeight2 = (int)((double)this.getHeight()/2 * (double)bpd2.score/(double)maxScoreOfEngine);
			if(((index+1)%2) == 0) convertedHeight2 *= -1; 
			convertedHeight2 += this.getHeight()/2;
			g2.drawLine(convertedIndex, convertedHeight, convertedIndex2, convertedHeight2);
		}
	}
	private void drawPointOfCurrentPosition(Graphics g) {
		if(!se.isEngineOn) {
			int selectedIndex = listBoxKifu.getSelectedIndex();
			BestPointData bpd = bestPointData.get(selectedIndex);
			updateListBoxEngine(bpd);
			if(bpd.score == 0) return;
			int convertedIndex = (int)((double)this.getWidth() * ((double)selectedIndex/(double)maxSizeOfKifu));
			int convertedHeight = (int)((double)this.getHeight()/2 * (double)bpd.score/(double)maxScoreOfEngine);
			if((selectedIndex%2) == 0) convertedHeight *= -1; 
			convertedHeight += this.getHeight()/2;
			g.setColor(Color.red);
			g.drawOval(convertedIndex - sizeOfOval/2, convertedHeight-sizeOfOval/2, sizeOfOval, sizeOfOval);
			String s = Integer.toString(bpd.score);
			g.drawString(s, 0, 20);
		}
	}
	public void clearBestPointData() {
		for(int index=0; index<maxSizeOfKifu; index++) {
			BestPointData bpd = bestPointData.get(index);
			bpd.score = 0;
			for(int x=0; x<se.getNumOfMultiPV(); x++) bpd.moveName[x] = "";
		}
	}
	public void updateListBoxEngine(BestPointData bpd) {
		for(int index=0; index<se.getNumOfMultiPV(); index++) {
			//listModel[ListBoxType.Engine.id].set(index, bpd.moveName[index]);
			listModelEngine.set(index, bpd.moveName[index]);
		}
	}
}