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
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;
import lib.ShogiEngine.PointWithScore;
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
	ShogiData sd;
	int baseXPosForItems = 720;
	JLabel labelNumberRow[] = new JLabel[9];
	JLabel labelNumberCol[] = new JLabel[9];
	JCheckBox checkBox;
	ShogiEngine shogiEngine;
	CanvasBoardForEngine cve;
	public CanvasBoard(ShogiData s, JCheckBox cb, ShogiEngine se, CanvasBoardForEngine ce) {
		lastPointX = -1;
		lastPointY = -1;
		mousePressed = false;
		enableLastPoint = false;
		sd = s;
		checkBox = cb;
		shogiEngine = se;
		cve = ce;
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
				if(!checkBox.isSelected()) {
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
			if(checkBox.isSelected()) {
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
		if(shogiEngine.isEngineOn) return;
		for(Point p: drawList) {
			Point pB = drawListBase.get(index);
			int pBX, pBY, pX, pY;
			if(checkBox.isSelected()) {
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
		if(shogiEngine.out == null) return;
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
				if(checkBox.isSelected()) {
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