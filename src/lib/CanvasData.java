package lib;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import lib.GUIData.CheckBoxType;
import lib.GUIData.ListBoxType;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;
import lib.ShogiEngine.PointWithScore;
//-------------------------------------------------------------------------
// ----------------------- << Canvas, Canvas for Engine >> ----------------
// ------------------------------------------------------------------------
public class CanvasData {
	int baseXPosForItems;
	ShogiData sd;
	ShogiEngine se;
	GUIData gd;
	public CanvasBoard cv;
	public CanvasBoardForEngine cve;
	
	public CanvasData(int bPosX, ShogiData s, ShogiEngine sen, GUIData g) {
		baseXPosForItems = bPosX;
		sd = s;
		se = sen;
		gd = g;
		cv = new CanvasBoard();
		cve = new CanvasBoardForEngine();
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Canvas >> ------------------------------------
	// -------------------------------------------------------------------------
	public class CanvasBoard extends Canvas {
		public String imgFilePath = "./img/";
		public String imgFilePathBoard = imgFilePath + "board/";
		public String imgFilePathBackground = imgFilePath + "background/";
		int lastPointX;
		int lastPointY;
		public Boolean mousePressed;
		Boolean enableLastPoint;
		List<Point> drawList = new ArrayList<Point>();
		List<Point> drawListBase = new ArrayList<Point>();
		public List<Point> drawListTargetRightClick = new ArrayList<Point>();
		public List<Point> drawListBaseRightClick = new ArrayList<Point>();
		public List<Point> drawListLeftClick = new ArrayList<Point>();
		public List<PointWithScore> drawListForEngine = new ArrayList<PointWithScore>();
		public Image imgBoard;
		public Image imgBackground;
		public Image playerIcon[];
		public Image castleIcon;
		public Color clrFont;
		JLabel labelNumberRow[] = new JLabel[9];
		JLabel labelNumberCol[] = new JLabel[9];
		public CanvasBoard() {
			lastPointX = -1;
			lastPointY = -1;
			mousePressed = false;
			enableLastPoint = false;
			try {
				BufferedImage boardImage = ImageIO.read(new File(imgFilePathBoard + "shogi board.png"));
				imgBoard = boardImage.getScaledInstance((50+10)*9, (63+10)*9, java.awt.Image.SCALE_SMOOTH);
				boardImage = ImageIO.read(new File(imgFilePathBackground + "background.png"));
				imgBackground = boardImage.getScaledInstance(50*25, 63*12, java.awt.Image.SCALE_SMOOTH);
			} catch(IOException e) {
				imgBoard = null;
				imgBackground = null;
			}
			playerIcon = new Image[2];
			castleIcon = null;
			clrFont = new Color(0, 0, 0);
		}
		public void initializeSettings(int width, int height) {
			setBounds(0, 0, width, height);
		}
		public void paint(Graphics g) {
			drawBackground(g);
			drawShogiBoardBackground(g);
			drawShogiBoard(g);
			drawStrings(g);
			drawIcons(g);
			drawLastPoint(g);
			drawMovableArea(g);
			drawShogiKoma(g);
			drawNumOfKomaInHand(g);
			drawLeftClickedPoints(g);
			drawArrowsForKifuAnalysis(g);
			drawArrowsForRightClick(g);
			drawArrowForEngine(g);
		}
		public void drawBackground(Graphics g) {
			if(imgBackground != null) g.drawImage(imgBackground, 0, 0, this);
		}
		public void drawShogiBoard(Graphics g) {
			g.setColor(clrFont);
			for(int x=0; x<9; x++)
				for(int y=0; y<9; y++) {
					g.drawRect(x*(sd.iconWidth+10)+80, y*(sd.iconHeight+10)+20, sd.iconWidth+10, sd.iconHeight+10);
				}
		}
		public void drawShogiBoardBackground(Graphics g) {
			if(imgBoard != null) g.drawImage(imgBoard, 80, 20, this);
		}
		public void drawShogiKoma(Graphics g) {
			for(Koma k: sd.k) {
				g.drawImage(k.imgKoma, k.pos.x, k.pos.y, this);
			}
		}
		public void drawNumOfKomaInHand(Graphics g) {
			for(SenteGote sg: SenteGote.values()) {
				for(KomaType t: KomaType.values()) {
					if(sd.nok.numOfKoma[sg.id][t.id] == 0) continue;
					g.drawString(Integer.toString(sd.nok.numOfKoma[sg.id][t.id]),
							sd.nok.posNumOfKoma[sg.id][t.id].x,
							sd.nok.posNumOfKoma[sg.id][t.id].y);
				}
			}
		}
		public void drawIcons(Graphics g) {
			if(playerIcon[SenteGote.Sente.id] != null) g.drawImage(playerIcon[SenteGote.Sente.id], baseXPosForItems, 130, this);
			if(playerIcon[SenteGote.Gote.id] != null) g.drawImage(playerIcon[SenteGote.Gote.id], baseXPosForItems+130, 130, this);
			if(castleIcon != null) g.drawImage(castleIcon, baseXPosForItems+300, 110, this);
		}
		public void drawStrings(Graphics g) {
			g.setColor(clrFont);
			g.drawString("4000", baseXPosForItems+165, 585);
			g.drawString("-4000", baseXPosForItems+165, 695);
			g.drawString("Top 5 Best Moves", baseXPosForItems, 585);
			for(int x=0; x<9; x++) {
				g.drawString(labelNumberRow[x].getText(), labelNumberRow[x].getBounds().x, labelNumberRow[x].getBounds().y+10);
				g.drawString(labelNumberCol[x].getText(), labelNumberCol[x].getBounds().x, labelNumberCol[x].getBounds().y+10);
			}
			g.drawString("▲Sente", baseXPosForItems, 95);
			g.drawString("△Gote", baseXPosForItems+120, 95);
			// emphasize sente gote
			if(sd.turnIsSente) {
				g.drawRect(baseXPosForItems, 80, 100, 18);
			} else {
				g.drawRect(baseXPosForItems+120, 80, 100, 18);
			}
		}
		public void drawMovableArea(Graphics g) {
			if(mousePressed) {
				for(int x=1; x<10; x++) for(int y=1; y<10; y++) {
					if(!gd.checkBox[CheckBoxType.Reverse.id].isSelected()) {
						if(sd.selectedKoma.isMovable(x, y)) drawPoint(x, y, Color.pink);
					} else {
						if(sd.selectedKoma.isMovable(10-x, 10-y)) drawPoint(x, y, Color.pink);
					}
				}
			}
		}
		public void drawLastPoint(Graphics g) {
			if(enableLastPoint) {
				int X, Y;
				if(gd.checkBox[CheckBoxType.Reverse.id].isSelected()) {
					X = 10 - lastPointX;
					Y = 10 - lastPointY;
				} else {
					X = lastPointX;
					Y = lastPointY;
				}
				drawPoint(X, Y, Color.orange);
			}
		}
		public void drawArrowsForKifuAnalysis(Graphics g) {
			int index = 0;
			float bs = 10.0f;
			int red = 0;
			int green = 255;
			int blue = 255;
			if(se.isEngineOn) return;
			for(Point p: drawList) {
				Point pB = drawListBase.get(index);
				int pBX, pBY, pX, pY;
				if(gd.checkBox[CheckBoxType.Reverse.id].isSelected()) {
					pBX = 10 - pB.x;
					pBY = 10 - pB.y;
					pX = 10 - p.x;
					pY = 10 - p.y;
				} else {
					pBX = pB.x;
					pBY = pB.y;
					pX = p.x;
					pY = p.y;
				}
				
				Point pBase = new Point((9-pBX)*(sd.iconWidth+10)+85+sd.iconWidth/2, (pBY-1)*(sd.iconHeight+10)+25+sd.iconHeight/2);
				Point pTarget = new Point((9-pX)*(sd.iconWidth+10)+85+sd.iconWidth/2, (pY-1)*(sd.iconHeight+10)+25+sd.iconHeight/2);
				Arrow ar = new Arrow(pBase, pTarget);
				BasicStroke stroke = new BasicStroke(bs);
				g.setColor(new Color(red, green, blue));
				ar.draw((Graphics2D)g, stroke);
				index++;
				bs -= 2.0f;
				if(bs < 1.0f) bs = 1.0f;
				blue -= 40;
				green -= 40;
				if(blue < 40) blue = 40;
				if(green < 40) green = 40;
			}
		}
		public void drawArrowForEngine(Graphics g) {
			if(se.out == null) return;
			float bs = 10.0f;
			Color c;
			int red = 0;
			int green = 250;
			int blue = 0;
			int count = 0;
			// for safe access in multi thread
			synchronized(drawListForEngine) {
				for(PointWithScore ps: drawListForEngine) {
					if(count == 0) {
						cve.bestPointFromEngine.score = ps.score;
					}
					int pBX, pBY, pX, pY;
					if(gd.checkBox[CheckBoxType.Reverse.id].isSelected()) {
						pBX = 10 - ps.base.x;
						pBY = 10 - ps.base.y;
						pX = 10 - ps.target.x;
						pY = 10 - ps.target.y;
					} else {
						pBX = ps.base.x;
						pBY = ps.base.y;
						pX = ps.target.x;
						pY = ps.target.y;
					}
					Point pBase = new Point((9-pBX)*(sd.iconWidth+10)+85+sd.iconWidth/2, (pBY-1)*(sd.iconHeight+10)+25+sd.iconHeight/2);
					Point pTarget = new Point((9-pX)*(sd.iconWidth+10)+85+sd.iconWidth/2, (pY-1)*(sd.iconHeight+10)+25+sd.iconHeight/2);
					Arrow ar = new Arrow(pBase, pTarget);
					BasicStroke stroke = new BasicStroke(bs);
					c = new Color(red, green, blue);
					g.setColor(c);
					ar.draw((Graphics2D)g, stroke);
					bs -= 2.0f;
					green -= 80;
					count++;
					
					if(count == 3) break; // only top 3 are shown 
				}
			}
		}
		public void clearDrawListForEngine() {
			// for safe access in multi thread
			synchronized(drawListForEngine) {
				drawListForEngine.clear();
			}
		}
		public void drawArrowsForRightClick(Graphics g) {
			for(Point pTarget: drawListTargetRightClick) {
				Point pBase = drawListBaseRightClick.get(drawListTargetRightClick.indexOf(pTarget));
				Point pBaseConverted = convertMousePointToCentralSquare(pBase);
				Point pTargetConverted = convertMousePointToCentralSquare(pTarget);
				if(pBaseConverted.x == pTargetConverted.x && pBaseConverted.y == pTargetConverted.y) continue;
				Arrow ar = new Arrow(pBaseConverted, pTargetConverted);
				BasicStroke stroke = new BasicStroke(8.0f);
				g.setColor(Color.magenta);
				ar.draw((Graphics2D)g, stroke);
			}
		}
		public void drawLeftClickedPoints(Graphics g) {
			for(Point p: drawListLeftClick) {
				Point pShogiXY = convertMousePointToShogiXY(p);
				BasicStroke stroke = new BasicStroke(4.0f);
				Graphics2D g2 = (Graphics2D)g;
				g2.setStroke(stroke);
				g2.setColor(new Color(250,150,162));
				g2.drawOval((9-pShogiXY.x)*(sd.iconWidth+10)+25, (pShogiXY.y-1)*(sd.iconHeight+10)+32, 50, 50);
			}
		}
		public Point convertMousePointToCentralSquare(Point p) {
			Point pCalculated = new Point();
			int x = (p.x-25) / (sd.iconWidth+10);
			int y = (p.y-25) / (sd.iconHeight+10);
			pCalculated.x = 25+x*(sd.iconWidth+10) + sd.iconWidth/2;
			pCalculated.y = 25+y*(sd.iconHeight+10) + sd.iconHeight/2;
			
			return pCalculated;
		}
		public Point convertMousePointToShogiXY(Point p) {
			Point pCalculated = new Point();
			pCalculated.x = 9 - (p.x-25) / (sd.iconWidth+10);
			pCalculated.y = 1 + (p.y-25) / (sd.iconHeight+10);
			return pCalculated;
		}
		public void drawPoint(int x, int y, Color cl) {
			Graphics g = getGraphics();
			BasicStroke stroke = new BasicStroke(4.0f);
			Graphics2D g2 = (Graphics2D)g;
			g2.setStroke(stroke);
			g2.setColor(cl);
			g2.drawRect((9-x)*(sd.iconWidth+10)+82, (y-1)*(sd.iconHeight+10)+22, sd.iconWidth+6, sd.iconHeight+6);
		}
		public void setLastPoint(int px, int py, Boolean enable) {
			lastPointX = px;
			lastPointY = py;
			enableLastPoint = enable;
		}
		public void addDrawPoint(Point p1, Point p2) {
			drawList.add(p1);
			drawListBase.add(p2);
		}
		public void clearDrawPoint() {
			drawList.clear();
			drawListBase.clear();
		}
		public void clearDrawPointForRightClick() {
			drawListTargetRightClick.clear();
			drawListBaseRightClick.clear();
			drawListLeftClick.clear();
		}
		public class Arrow {
			private final Point start = new Point();
			private final Point end = new Point();
			private final Path2D arrowHead;
			
			protected Arrow(Point start, Point end) {
				this.start.setLocation(start);
				this.end.setLocation(end);
				arrowHead = makeArrowHead(new Dimension(8, 8));
			}
			protected Path2D makeArrowHead(Dimension size) {
				Path2D path = new Path2D.Double();
				double t = size.height;
				double w = size.width * .8;
				path.moveTo(0d, -w);
				path.lineTo(t, 0d);
				path.lineTo(0d, w);
				path.closePath();
				return path;
			}
			public void draw(Graphics2D g2, BasicStroke stroke) {
				g2.setStroke(stroke);
				g2.drawLine(start.x, start.y, end.x, end.y);
				arrowHead.transform(AffineTransform.getRotateInstance(end.getX() - start.getX(), end.getY() - start.getY()));
				arrowHead.transform(AffineTransform.getTranslateInstance(end.getX(), end.getY()));
				g2.fill(arrowHead);
				g2.draw(arrowHead);
			}
		}
		public void initializeNumberRowCol() {
			for(int x=0; x<9; x++) {
				labelNumberRow[x] = new JLabel();
				labelNumberCol[x] = new JLabel();
				labelNumberRow[x].setText(String.valueOf(9-x));
				labelNumberRow[x].setBounds(105 + x * (sd.iconWidth+10), 5, 10, 10);
				labelNumberCol[x].setText(String.valueOf(x+1));
				labelNumberCol[x].setBounds(625, 50 + x * (sd.iconHeight+10), 10, 10);
			}
		}
		public void reverseNumberRowCol(Boolean isReverse) {
			for(int x=0; x<9; x++) {
				if(isReverse) {
					labelNumberRow[x].setText(String.valueOf(x+1));
					labelNumberCol[x].setText(String.valueOf(9-x));
				}
				else {
					labelNumberRow[x].setText(String.valueOf(9-x));
					labelNumberCol[x].setText(String.valueOf(x+1));
				}
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Canvas for Engine >> -------------------------
	// -------------------------------------------------------------------------
	public class CanvasBoardForEngine extends Canvas {
		public static final int maxSizeOfKifu = 200;
		public static final int maxScoreOfEngine = 4000;
		public static final int sizeOfOval = 6;
		public BestPointData bestPointFromEngine;
		List<BestPointData> bestPointData = new ArrayList<BestPointData>();
		int selectedIndex;
		public CanvasBoardForEngine() {
			bestPointFromEngine = new BestPointData(se.getNumOfMultiPV());
			for(int index=0; index<maxSizeOfKifu; index++) {
				BestPointData bpt = new BestPointData(se.getNumOfMultiPV());
				bestPointData.add(bpt);
			}
		}
		public class BestPointData {
			int score;
			String[] moveName;
			BestPointData(int numOfMultiPV) {
				moveName = new String[numOfMultiPV];
				for(int index=0; index<numOfMultiPV; index++) moveName[index] = new String("");
			}
		}
		public void initializeSetting() {
			setBounds(baseXPosForItems+165, 590,330, 90);
			setBackground(Color.white);
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
			int index = gd.listBox[ListBoxType.Kifu.id].getSelectedIndex();
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
				int selectedIndex = gd.listBox[ListBoxType.Kifu.id].getSelectedIndex();
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
				gd.listModel[ListBoxType.Engine.id].set(index, bpd.moveName[index]);
			}
		}
	}
}