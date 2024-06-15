package lib;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import lib.EditProperty.PropertyType;

// -------------------------------------------------------------------------
// ---------------------------- << Color >> -------------------------------
// -------------------------------------------------------------------------
public class ColorDataBase {
	public enum ColorSetType {
		Default(0), Sakura(1), GreenTea(2), BlueSky(3), Evening(4), Universe(5);
		private final int id;		
		private ColorSetType(final int id) {
			this.id = id;
		}
	};
	public enum ButtonType {
		Initialize(0), Save(1), Strategy(2), Castle(3), Tesuji(4), Kifu(5);
		public final int id;
		private ButtonType(final int id) {
			this.id = id;
		}
	};
	public Color buttonColor;
	public Color buttonFocusedColor;
	ColorSet listColorSet[] = new ColorSet[ColorSetType.values().length];
	
	EditProperty ep;
	CanvasData cd;
	JButton button[];
	
	public ColorDataBase(EditProperty e, CanvasData c, JButton b[]) {
		ep = e;
		cd = c;
		button = b;
	}
	public class ColorSet {
		Color button;
		Color buttonFocused;
		Color buttonBorder;
		Color font;
		Image board;
		Image background;
		ColorSet(Color b, Color bf, Color bb, Color f) {
			button = b;
			buttonFocused = bf;
			buttonBorder = bb;
			font = f;
			board = null;
			background = null;
		}
	}
	public void actionForSetColor(JFrame fr) {
		String[] selectvalues = new String[ColorSetType.values().length];
		for(ColorSetType cst: ColorSetType.values()) selectvalues[cst.id] = cst.name();
		int select = JOptionPane.showOptionDialog(
				fr,
				"Which color style?",
				"Color Style",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				selectvalues,
				selectvalues[0]
				);
		if (select == JOptionPane.CLOSED_OPTION) {
			System.out.println("cancel");
		} else {
			ep.setProperty(PropertyType.Color.name(), Integer.valueOf(select).toString());
			setColor();
		}
	}
	public void initializeColorSet() {
		listColorSet[ColorSetType.Default.id] = new ColorSet(
				new Color(235, 235, 235), // button;
				new Color(215, 215, 215), // buttonFocused;
				new Color(0, 0, 0),		 // buttonBorder;
				new Color(0, 0, 0)		 // font;
				);
		listColorSet[ColorSetType.Sakura.id] = new ColorSet(
				new Color(230, 205, 227), 
				new Color(229, 171, 190), 
				new Color(201, 117, 134),
				new Color(201, 67, 84)
				);
		listColorSet[ColorSetType.GreenTea.id] = new ColorSet(
				new Color(205, 230, 227), 
				new Color(171, 229, 190), 
				new Color(117, 201, 134),
				new Color(255, 255, 255)
				);
		listColorSet[ColorSetType.BlueSky.id] = new ColorSet(
				new Color(160, 216, 239), 
				new Color(171, 190, 229), 
				new Color(117, 134, 201),
				new Color(0, 0, 0)	
				);
		listColorSet[ColorSetType.Evening.id] = new ColorSet(
				new Color(255, 255, 0), 
				new Color(255, 215, 0), 
				new Color(255, 140, 0),
				new Color(255, 255, 255)
				);
		listColorSet[ColorSetType.Universe.id] = new ColorSet(
				new Color(255, 255, 0), 
				new Color(255, 215, 0), 
				new Color(255, 140, 0),
				new Color(255, 255, 255)
				);
		initializeImageSet();
		setColor();
	}
	public void initializeImageSet() {
		for(ColorSetType cst: ColorSetType.values()) {
			try {
				String fileName = cd.cv.imgFilePathBoard + "shogi board" + cst.id + ".png";
				BufferedImage img = ImageIO.read(new File(fileName));
				listColorSet[cst.id].board = img.getScaledInstance((50+10)*9, (63+10)*9, java.awt.Image.SCALE_SMOOTH);
				fileName = cd.cv.imgFilePathBackground + "background" + cst.id + ".png";
				img = ImageIO.read(new File(fileName));
				listColorSet[cst.id].background = img.getScaledInstance(50*25, 63*12, java.awt.Image.SCALE_SMOOTH);
			} catch(IOException e) {
				listColorSet[cst.id].board = null;
				listColorSet[cst.id].background = null;
			}
		}
	}
	public void setColor() {
		String value = ep.loadProperty(PropertyType.Color.name());
		int select;
		if(value == null) select = ColorSetType.Default.id;
		else select = Integer.parseInt(value);
		
		buttonColor = listColorSet[select].button;
		buttonFocusedColor = listColorSet[select].buttonFocused;
		LineBorder border = new LineBorder(listColorSet[select].buttonBorder, 1, true);
		for(ButtonType bt: ButtonType.values()) {
			button[bt.id].setBackground(buttonColor);
			button[bt.id].setBorder(border);
		}
		cd.cv.imgBoard = listColorSet[select].board;
		cd.cv.imgBackground = listColorSet[select].background;
		cd.cv.clrFont =  listColorSet[select].font;
		cd.cv.repaint();
	}
}