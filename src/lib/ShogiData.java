package lib;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

// -------------------------------------------------------------------------
// ----------------------- << Shogi Board >> -------------------------------
// -------------------------------------------------------------------------
public class ShogiData {
	String imgFilePath = "./img/";
	String imgFilePathKoma = imgFilePath + "koma/";
	public enum SenteGote {
		Sente(0), Gote(1);
		public final int id;
		private SenteGote(final int id) {
			this.id = id;
		}
	};
	public enum KomaType {
		Pawn(0), Rance(1), Knight(2), Silver(3), Gold(4), Bishop(5), Rook(6), King(7), Empty(8);
		public final int id;
		private KomaType(final int id) {
			this.id = id;
		}
	};
	public String[] komaName = {"歩", "香", "桂", "銀", "金", "角", "飛", "王", "と", "成香", "成桂", "成銀", "金", "馬", "龍", "王"};
	public String[] senteGote = {"▲", "△"};
	BufferedImage img[][] = new BufferedImage[10][4];
	public int iconWidth;
	public int iconHeight;
	public Koma k[];
	public Koma selectedKoma = null;
	public Boolean turnIsSente;
	public List<Koma> listKomaOnBoard;
	public List<List<Koma>> listKomaOnHand;
	public List<Koma> listKomaOnHandForSente;
	public List<Koma> listKomaOnHandForGote;
	public NumOfKoma nok;
	public class NumOfKoma {
		public int numOfKoma[][];
		public Point posNumOfKoma[][];
		public NumOfKoma() {
			numOfKoma = new int[SenteGote.values().length][KomaType.values().length];
			posNumOfKoma = new Point[SenteGote.values().length][KomaType.values().length];
			for(SenteGote sg: SenteGote.values())
				for(KomaType t: KomaType.values()) {
					Point p = new Point();
					posNumOfKoma[sg.id][t.id] = p;
				}
		}
	}
	public ShogiData() {
		k = new Koma[40];
		turnIsSente = true;
		listKomaOnBoard = new ArrayList<Koma>();
		listKomaOnHandForSente = new ArrayList<Koma>();
		listKomaOnHandForGote = new ArrayList<Koma>();
		listKomaOnHand = new ArrayList<>();
		listKomaOnHand.add(listKomaOnHandForSente);
		listKomaOnHand.add(listKomaOnHandForGote);
		nok = new NumOfKoma();
	}
	public void initializeIcon() {
		try {
			img[KomaType.Pawn.id][0] = ImageIO.read(new File(imgFilePathKoma + "Pawn.png"));
			img[KomaType.Pawn.id][1] = ImageIO.read(new File(imgFilePathKoma + "Promoted Pawn.png"));
			img[KomaType.Pawn.id][2] = ImageIO.read(new File(imgFilePathKoma + "Pawn Gote.png"));
			img[KomaType.Pawn.id][3] = ImageIO.read(new File(imgFilePathKoma + "Promoted Pawn Gote.png"));
			img[KomaType.Rance.id][0] = ImageIO.read(new File(imgFilePathKoma + "Rance.png"));
			img[KomaType.Rance.id][1] = ImageIO.read(new File(imgFilePathKoma + "Promoted Rance.png"));
			img[KomaType.Rance.id][2] = ImageIO.read(new File(imgFilePathKoma + "Rance Gote.png"));
			img[KomaType.Rance.id][3] = ImageIO.read(new File(imgFilePathKoma + "Promoted Rance Gote.png"));
			img[KomaType.Knight.id][0] = ImageIO.read(new File(imgFilePathKoma + "Knight.png"));
			img[KomaType.Knight.id][1] = ImageIO.read(new File(imgFilePathKoma + "Promoted Knight.png"));
			img[KomaType.Knight.id][2] = ImageIO.read(new File(imgFilePathKoma + "Knight Gote.png"));
			img[KomaType.Knight.id][3] = ImageIO.read(new File(imgFilePathKoma + "Promoted Knight Gote.png"));
			img[KomaType.Silver.id][0] = ImageIO.read(new File(imgFilePathKoma + "Silver.png"));
			img[KomaType.Silver.id][1] = ImageIO.read(new File(imgFilePathKoma + "Promoted Silver.png"));
			img[KomaType.Silver.id][2] = ImageIO.read(new File(imgFilePathKoma + "Silver Gote.png"));
			img[KomaType.Silver.id][3] = ImageIO.read(new File(imgFilePathKoma + "Promoted Silver Gote.png"));
			img[KomaType.Gold.id][0] = ImageIO.read(new File(imgFilePathKoma + "Gold.png"));
			img[KomaType.Gold.id][2] = ImageIO.read(new File(imgFilePathKoma + "Gold Gote.png"));
			img[KomaType.Rook.id][0] = ImageIO.read(new File(imgFilePathKoma + "Rook.png"));
			img[KomaType.Rook.id][1] = ImageIO.read(new File(imgFilePathKoma + "Promoted Rook.png"));
			img[KomaType.Rook.id][2] = ImageIO.read(new File(imgFilePathKoma + "Rook Gote.png"));
			img[KomaType.Rook.id][3] = ImageIO.read(new File(imgFilePathKoma + "Promoted Rook Gote.png"));
			img[KomaType.Bishop.id][0] = ImageIO.read(new File(imgFilePathKoma + "Bishop.png"));
			img[KomaType.Bishop.id][1]= ImageIO.read(new File(imgFilePathKoma + "Promoted Bishop.png"));
			img[KomaType.Bishop.id][2] = ImageIO.read(new File(imgFilePathKoma + "Bishop Gote.png"));
			img[KomaType.Bishop.id][3]= ImageIO.read(new File(imgFilePathKoma + "Promoted Bishop Gote.png"));
			img[KomaType.King.id][0] = ImageIO.read(new File(imgFilePathKoma + "King.png"));
			img[KomaType.King.id][2] = ImageIO.read(new File(imgFilePathKoma + "King Gote.png"));
			iconWidth = img[KomaType.Pawn.id][0].getWidth();
			iconHeight = img[KomaType.Pawn.id][0].getHeight();
		} catch(IOException e) {
			System.out.println(e);
			return;
		}
	}
	public void viewKomaOnBoard(Boolean isReverse) {
		for(Koma k: listKomaOnBoard) {
			int X, Y;
			if(isReverse) {
				X = 10 - k.px;
				Y = 10 - k.py;
			} else {
				X = k.px;
				Y = k.py;
			}
			k.pos.x = (9-X)*(iconWidth+10)+85;
			k.pos.y = (Y-1)*(iconHeight+10)+25;
		}
	}
	public void viewKomaOnHand(Boolean isReverse) {
		for(SenteGote sg: SenteGote.values()) {
			for(KomaType t: KomaType.values()) {
				nok.numOfKoma[sg.id][t.id] = 0;
			}
		}
		
		for(Koma k: listKomaOnHand.get(SenteGote.Sente.id)) {
			if(!isReverse) {
				k.pos.x = (iconWidth+10)*10+50;
				k.pos.y = (iconHeight+10)*(k.type.id+1)+25;
				nok.posNumOfKoma[SenteGote.Sente.id][k.type.id].x = (iconWidth+10)*11+35;
				nok.posNumOfKoma[SenteGote.Sente.id][k.type.id].y = (iconHeight+10)*(k.type.id+1)+30;
			} else {
				k.pos.x = 10;
				k.pos.y = (iconHeight+10)*(7-k.type.id)+25;
				nok.posNumOfKoma[SenteGote.Sente.id][k.type.id].x = 55;
				nok.posNumOfKoma[SenteGote.Sente.id][k.type.id].y = (iconHeight+10)*(7-k.type.id)+70;
			}
			nok.numOfKoma[SenteGote.Sente.id][k.type.id]++;
		}
		
		for(Koma k: listKomaOnHand.get(SenteGote.Gote.id)) {
			if(!isReverse) {
				k.pos.x = 10;
				k.pos.y = (iconHeight+10)*(7-k.type.id)+25;
				nok.posNumOfKoma[SenteGote.Gote.id][k.type.id].x = 55;
				nok.posNumOfKoma[SenteGote.Gote.id][k.type.id].y = (iconHeight+10)*(7-k.type.id)+70;
			} else {
				k.pos.x = (iconWidth+10)*10+50;
				k.pos.y = (iconHeight+10)*(k.type.id+1)+25;
				nok.posNumOfKoma[SenteGote.Gote.id][k.type.id].x = (iconWidth+10)*11+35;
				nok.posNumOfKoma[SenteGote.Gote.id][k.type.id].y = (iconHeight+10)*(k.type.id+1)+30;
			}
			nok.numOfKoma[SenteGote.Gote.id][k.type.id]++;
		}
	}
	public void initializeKomaSetting() {
		// Sente
		for(int x=0; x<9; x++) {
			k[x] = new Koma(KomaType.Pawn, x+1, 7, 0, x);
		}
		k[9] = new Koma(KomaType.Rance, 9, 9, 0, 9);
		k[10] = new Koma(KomaType.Rance, 1, 9, 0, 10);
		k[11] = new Koma(KomaType.Knight, 8, 9, 0, 11);
		k[12] = new Koma(KomaType.Knight, 2, 9, 0, 12);
		k[13] = new Koma(KomaType.Silver, 7, 9, 0, 13);
		k[14] = new Koma(KomaType.Silver, 3, 9, 0, 14);
		k[15] = new Koma(KomaType.Gold, 6, 9, 0, 15);
		k[16] = new Koma(KomaType.Gold, 4, 9, 0, 16);
		k[17] = new Koma(KomaType.Rook, 2, 8, 0, 17);
		k[18] = new Koma(KomaType.Bishop, 8, 8, 0, 18);
		k[19] = new Koma(KomaType.King, 5, 9, 0, 19);
				
		// Gote
		for(int x=20; x<29; x++) {
			k[x] = new Koma(KomaType.Pawn, x-20+1, 3, 1, x);
		}
		k[29] = new Koma(KomaType.Rance, 9, 1, 1, 29);
		k[30] = new Koma(KomaType.Rance, 1, 1, 1, 30);
		k[31] = new Koma(KomaType.Knight, 8, 1, 1, 31);
		k[32] = new Koma(KomaType.Knight, 2, 1, 1, 32);
		k[33] = new Koma(KomaType.Silver, 7, 1, 1, 33);
		k[34] = new Koma(KomaType.Silver, 3, 1, 1, 34);
		k[35] = new Koma(KomaType.Gold, 6, 1, 1, 35);
		k[36] = new Koma(KomaType.Gold, 4, 1, 1, 36);
		k[37] = new Koma(KomaType.Rook, 8, 2, 1, 37);
		k[38] = new Koma(KomaType.Bishop, 2, 2, 1, 38);
		k[39] = new Koma(KomaType.King, 5, 1, 1, 39);
		
		for(int x=0; x<40; x++) {
			listKomaOnBoard.add(k[x]);
		}
	}
	public void resetAllKoma(Boolean isReverse) {
		turnIsSente = true;
		listKomaOnBoard.clear();
		for(SenteGote sg: SenteGote.values()) listKomaOnHand.get(sg.id).clear();
		for(int x=0; x<9; x++) {
			k[x].reset(KomaType.Pawn, x+1, 7, 0);
		}
		k[9].reset(KomaType.Rance, 9, 9, 0);
		k[10].reset(KomaType.Rance, 1, 9, 0);
		k[11].reset(KomaType.Knight, 8, 9, 0);
		k[12].reset(KomaType.Knight, 2, 9, 0);
		k[13].reset(KomaType.Silver, 7, 9, 0);
		k[14].reset(KomaType.Silver, 3, 9, 0);
		k[15].reset(KomaType.Gold, 6, 9, 0);
		k[16].reset(KomaType.Gold, 4, 9, 0);
		k[17].reset(KomaType.Rook, 2, 8, 0);
		k[18].reset(KomaType.Bishop, 8, 8, 0);
		k[19].reset(KomaType.King, 5, 9, 0);
		for(int x=20; x<29; x++) {
			k[x].reset(KomaType.Pawn, x-20+1, 3, 1);
		}
		k[29].reset(KomaType.Rance, 9, 1, 1);
		k[30].reset(KomaType.Rance, 1, 1, 1);
		k[31].reset(KomaType.Knight, 8, 1, 1);
		k[32].reset(KomaType.Knight, 2, 1, 1);
		k[33].reset(KomaType.Silver, 7, 1, 1);
		k[34].reset(KomaType.Silver, 3, 1, 1);
		k[35].reset(KomaType.Gold, 6, 1, 1);
		k[36].reset(KomaType.Gold, 4, 1, 1);
		k[37].reset(KomaType.Rook, 8, 2, 1);
		k[38].reset(KomaType.Bishop, 2, 2, 1);
		k[39].reset(KomaType.King, 5, 1, 1);
		
		for(int x=0; x<40; x++) {
			listKomaOnBoard.add(k[x]);
		}
		
		if(isReverse) {
			for(int x=0; x<40; x++) {
				k[x].reverseForReverseMode();
			}
		}
	}
	public Koma findTouchedKoma(Koma movedKoma) {
		for(Koma k: listKomaOnBoard) {
			if(k != movedKoma && k.px == movedKoma.px && k.py == movedKoma.py) {
				return k;
			}
		}
		
		return null;
	}
	public Koma findKoma(int x, int y) {
		for(Koma k: listKomaOnBoard) {
			if(k.px == x && k.py == y) return k;
		}
		return null;
	}
	public Koma findKomaInHand(KomaType type, Boolean isSente) {
		List<Koma> listKomaOnHand;
		if(isSente) {
			listKomaOnHand = listKomaOnHandForSente;
		} else {
			listKomaOnHand = listKomaOnHandForGote;
		}
		for(Koma k: listKomaOnHand) {
			if(k.type == type) return k;
		}
		
		return null;
	}
	public void putAllKomaInHand(Boolean isReverse, Boolean isRadioButtonSente) {
		resetAllKoma(isReverse);
		listKomaOnBoard.clear();
		for(int x=0; x<20; x++) {
			k[x].px = 0;
			k[x].py = k[x].type.id+2;
			if(isRadioButtonSente) {
				listKomaOnHandForSente.add(k[x]);
			} else {
				k[x].reverse();
				listKomaOnHandForGote.add(k[x]);
			}
		}
		for(int x=20; x<40; x++) {
			k[x].px = 10;
			k[x].py = 8-k[x].type.id;
			if(isRadioButtonSente) {
				k[x].reverse();
				listKomaOnHandForSente.add(k[x]);
			} else {
				listKomaOnHandForGote.add(k[x]);
			}
		}
	}
	public String createMoveKomaName(KomaType type, int sente, int x, int y, int preX, int preY, int promoted, int preP, int drop) {
		String s = senteGote[sente] + String.valueOf(x)+String.valueOf(y);
		if(preP == 0 && promoted == 1) {
			s += komaName[type.id] + "成";
		} else {
			s += komaName[type.id+8*preP];
		}
		if(drop == 1) s += "打";
		else {
			s += "(" + preX + preY + ")";
		}
		return s;
	}
	public class Koma {
		public Point pos;
		public BufferedImage imgKoma;
		public int px;
		public int py;
		public int promoted;
		public int sente;
		public int index;
		public int drop;
		Boolean forward;
		public KomaType type;
		public Koma(KomaType t, int x, int y, int s, int i) {
			pos = new Point();
			pos.x = (iconWidth+10)*(9-x)+25;
			pos.y = (iconHeight+10)*(y-1)+25;
			imgKoma = img[t.id][0+s*2];
			type = t;
			px = x;
			py = y;
			sente = s;
			promoted = 0;
			index = i;
			drop = 0;
		}
		public void reset(KomaType t, int x, int y, int s) {
			type = t;
			px = x;
			py = y;
			if(s == 0) forward = true;
			else forward = false;
			sente = s;
			promoted = 0;
			imgKoma = img[type.id][sente*2 + promoted];
		}
		public void promote() {
			if(type == KomaType.Gold || type == KomaType.King) return;
			if(promoted == 0) {
				if(forward && sente == 0) {
					imgKoma = img[type.id][0+1];
				} else if(forward && sente == 1) {
					imgKoma = img[type.id][0+1];
				} else if(!forward && sente == 0) {
					imgKoma = img[type.id][2+1];
				} else {
					imgKoma = img[type.id][2+1];
				}
				promoted = 1;
			} else {
				imgKoma = img[type.id][sente*2];
				promoted = 0;
			}
		}
		public void reverse() {
			if(sente == 0) {
				sente = 1;
			} else {
				sente = 0;
			}
			if(forward && sente == 0) {
				imgKoma = img[type.id][2 + promoted];
			} else if(forward && sente == 1) {
				imgKoma = img[type.id][2 + promoted];
			} else if(!forward && sente == 0) {
				imgKoma = img[type.id][0 + promoted];
			} else {
				imgKoma = img[type.id][0 + promoted];
			}
			forward = !forward;
		}
		public void reverseForReverseMode() {
			if(forward) {
				imgKoma = img[type.id][2 + promoted];
				forward = false;
			} else {
				imgKoma = img[type.id][0 + promoted];
				forward = true;
			}
		}
		public Boolean isMovable(int x, int y) {
			switch(type) {
			case Pawn:
				return isPawnMove(x, y);
			case Rance:
				return isRanceMove(x, y);
			case Knight:
				return isKnightMove(x, y);
			case Silver:
				return isSilverMove(x, y);
			case Gold:
				return isGoldMove(x, y);
			case Bishop:
				return isBishopMove(x, y);
			case Rook:
				return isRookMove(x, y);
			case King:
				return isKingMove(x, y);
			default:
				break;
			}
			
			return false;
		}
		public Boolean checkKomaExistence(int x, int y) {
			for(Koma k: listKomaOnBoard) if(k.px == x && k.py == y) return true;
			
			return false;
		}
		public Boolean isPawnMove(int x, int y) {
			if(promoted == 1) return isGoldMove(x, y);
			if(sente == 0) {
				if(x == px && y == (py-1)) return true;
			} else {
				if(x == px && y == (py+1)) return true;
			}
			return false;
		}
		public Boolean isRanceMove(int x, int y) {
			int ty;
			Boolean b;
			if(promoted == 1) return isGoldMove(x, y);
			if(x != px) return false;
			if(sente == 0) {
				if(y >= py) return false;
				ty = y;
				b = false;
				while(ty < py) {
					if(checkKomaExistence(x, ty)) b = true;
					ty++;
				}
				if(b == false) return true;
			} else {
				if(y <= py) return false;
				ty = y;
				b = false;
				while(ty > py) {
					if(checkKomaExistence(x, ty)) b = true;
					ty--;
				}
				if(b == false) return true;
			}
			return false;
		}
		public Boolean isKnightMove(int x, int y) {
			if(promoted == 1) return isGoldMove(x, y);
			if(sente == 0) {
				if( (x == (px+1) && y == (py-2)) || (x == (px-1)) && y == (py-2) ) return true;  
			} else {
				if( (x == (px+1) && y == (py+2)) || (x == (px-1)) && y == (py+2) ) return true;
			}
			return false;
		}
		public Boolean isSilverMove(int x, int y) {
			if(promoted == 1) return isGoldMove(x, y);
			if(sente == 0) {
				if( (x == px && y == (py-1)) || (x == (px+1) && y == (py-1)) || (x == (px-1) && y == (py-1)) || 
						(x == (px+1) && y == (py+1)) || (x == (px-1) && y == (py+1)) ) return true;
			} else {
				if( (x == px && y == (py+1)) || (x == (px+1) && y == (py-1)) || (x == (px-1) && y == (py-1)) || 
						(x == (px+1) && y == (py+1)) || (x == (px-1) && y == (py+1)) ) return true;
			}
			return false;
		}
		public Boolean isGoldMove(int x, int y) {
			if(sente == 0) {
				if( (x == px && y == (py-1)) || (x == px && y == (py+1)) || 
						(x == (px-1) && y == py) || (x == (px+1) && y == py) ||
						(x == (px+1) && y == (py-1)) || (x == (px-1) && y == (py-1)) ) return true;
			} else {
				if( (x == px && y == (py-1)) || (x == px && y == (py+1)) || 
						(x == (px-1) && y == py) || (x == (px+1) && y == py) ||
						(x == (px+1) && y == (py+1)) || (x == (px-1) && y == (py+1)) ) return true;
			}
			
			return false;
		}
		public Boolean isBishopMove(int x, int y) {
			int dx, dy;
			int tx, ty;
			Boolean b;
			if(promoted == 1) {
				if( (x == px && y == (py+1)) || (x == px && y == (py-1)) || 
						(x == (px+1) && y == py) || (x == (px-1) && y == py) ) return true;
			}
			
			dx = x - px;
			dy = y - py;
			if( dx != dy && dx != (-dy) ) return false;
			
			b = false;
			tx = x;
			ty = y;
			if(tx > px && ty > py) {
				while(tx > px) {
					if(checkKomaExistence(tx, ty)) b = true;
					tx--;
					ty--;
				}
			} else if(tx > px && ty < py) {
				while(tx > px) {
					if(checkKomaExistence(tx, ty)) b = true;
					tx--;
					ty++;
				}
			} else if(tx < px && ty > py) {
				while(tx < px) {
					if(checkKomaExistence(tx, ty)) b = true;
					tx++;
					ty--;
				}
			} else if(tx < px && ty < py) {
				while(tx < px) {
					if(checkKomaExistence(tx, ty)) b = true;
					tx++;
					ty++;
				}
			}
			if(b == true) return false;
			
			return true;
		}
		public Boolean isRookMove(int x, int y) {
			int tx, ty;
			Boolean b;
			if(promoted == 1) {
				if( (x == (px+1) && y == (py+1)) || (x == (px+1) && y == (py-1)) ||
						(x == (px-1) && y == (py+1)) || (x == (px-1) && y == (py-1)) ) return true;
			}
			
			if(x != px && y != py) return false;
			
			tx = x;
			ty = y;
			b = false;
			if(tx > px && ty == py) {
				while(tx > px) {
					if(checkKomaExistence(tx, ty)) b = true;
					tx--;
				}
			} else if(tx < px && ty == py) {
				while(tx < px) {
					if(checkKomaExistence(tx, ty)) b = true;
					tx++;
				}
			} else if(tx == px && ty > py) {
				while(ty > py) {
					if(checkKomaExistence(tx, ty)) b = true;
					ty--;
				}
			} else if(tx == px && ty < py) {
				while(ty < py) {
					if(checkKomaExistence(tx, ty)) b = true;
					ty++;
				}
			}
			
			if(b == true) return false;
			
			return true;
		}
		public Boolean isKingMove(int x, int y) {
			if( (x == (px-1) && y == (py-1)) || (x == (px-1) && y == py) || (x == (px-1) && y == (py+1)) ||
					(x == px && y == (py-1)) || (x == px && y == (py+1)) ||
					(x == (px+1) && y == (py-1)) || (x == (px+1) && y == py) || (x == (px+1) && y == (py+1)) ) return true;
			return false;
		}
		public Boolean confirmPromotion(int x, int y, int preX, int preY) {
			if(this.promoted == 1) return true;
			if(this.type == KomaType.Gold || this.type == KomaType.King ) return true;
			
			if(x <= 0 || x >= 10 || y <= 0 || y >= 10) return true;
			if(listKomaOnHand.get(SenteGote.Sente.id).indexOf(this) != -1 || listKomaOnHand.get(SenteGote.Gote.id).indexOf(this) != -1) return true;
			
			if( (this.sente == 0 && y <= 3) || (this.sente == 1 && y >= 7) ||
					(this.sente == 0 && preY <= 3) || (this.sente == 1 && preY >= 7) ) {
				JFrame jf = new JFrame();
				int result = JOptionPane.showConfirmDialog(jf, "Promote?");
				if(result == 0) { // yes
					this.promote();
					return true;
				} else if(result == 1) { // no
					return true;
				} else { // cancel
					return false;
				}
			}
			
			return true;
		}
		public Boolean moveKoma(int x, int y, int promoted) {
			int preX = this.px;
			int preY = this.py;
			
			if(x == preX && y == preY) {
				pos.x = (9-preX)*(iconWidth+10)+85;
				pos.y = (preY-1)*(iconHeight+10)+25;
				return false;
			}
			
			if(promoted == -1) {
				if(!confirmPromotion(x, y, preX, preY)) {
					// case of cancel
					pos.x = (9-preX)*(iconWidth+10)+85;
					pos.y = (preY-1)*(iconHeight+10)+25;
					return false;
				}
			}
			else if(promoted == 1 && this.promoted == 0) this.promote(); // no confirm when kifuData
			
			this.px = x;
			this.py = y;
			this.drop = 0;
			pos.x = (9-x)*(iconWidth+10)+85;
			pos.y = (y-1)*(iconHeight+10)+25;
			
			if(x>0 && x<10 && y>0 && y<10) {
				if( listKomaOnHand.get(SenteGote.Sente.id).indexOf(this) != -1 ) moveFromStoB();
				else if( listKomaOnHand.get(SenteGote.Gote.id).indexOf(this) != -1) moveFromGtoB();
				else moveFromBtoB();
			} 
			else if(x<=0) {
				if(listKomaOnBoard.indexOf(this) != -1) moveFromBtoS();
				else if(listKomaOnHand.get(SenteGote.Sente.id).indexOf(this) != -1) moveFromStoS();
				else moveFromGtoS();
				return false;
			} else {
				if(listKomaOnBoard.indexOf(this) != -1) moveFromBtoG();
				else if(listKomaOnHand.get(SenteGote.Gote.id).indexOf(this) != -1) moveFromGtoG();
				else moveFromStoG();
				return false;
			}
			
			return true;
		}
		// OnBoard -> OnBoard
		public void moveFromBtoB() {
			Koma k = findTouchedKoma(this);
			if(k != null) {
				listKomaOnBoard.remove(k);
				if(k.promoted == 1) k.promote();
				if(this.sente == 0) {
					if(k.sente == 1) k.reverse();
					k.px = 0;
					k.py = k.type.id+2;
					listKomaOnHand.get(SenteGote.Sente.id).add(k);
				} else {
					if(k.sente == 0) k.reverse();
					k.px = 10;
					k.py = 8-k.type.id;
					listKomaOnHand.get(SenteGote.Gote.id).add(k);
				}
			}
		}
		// OnHandForSente -> OnBoard
		public void moveFromStoB() {
			this.drop = 1;
			listKomaOnHand.get(SenteGote.Sente.id).remove(this);
			listKomaOnBoard.add(this);
		}
		// OnHandForGote -> OnBoard
		public void moveFromGtoB() {
			this.drop = 1;
			listKomaOnHand.get(SenteGote.Gote.id).remove(this);
			listKomaOnBoard.add(this);
		}
		// OnBoard -> OnHandForSente
		public void moveFromBtoS() {
			listKomaOnBoard.remove(this);
			
			if(this.sente == 1) this.reverse();
			if(this.promoted == 1) this.promote();
			listKomaOnHand.get(SenteGote.Sente.id).add(this);
		}
		// OnBoard -> OnHandForGote
		public void moveFromBtoG() {
			listKomaOnBoard.remove(this);
			
			if(this.sente == 0) this.reverse();
			if(this.promoted == 1) this.promote();	
			listKomaOnHand.get(SenteGote.Gote.id).add(this);
		}
		// OnHandForSente -> OnHandForGote
		public void moveFromStoG() {
			listKomaOnHand.get(SenteGote.Sente.id).remove(this);
			this.reverse();
			listKomaOnHand.get(SenteGote.Gote.id).add(this);
		}
		// OnHandForGote -> OnHandForSente
		public void moveFromGtoS() {
			listKomaOnHand.get(SenteGote.Gote.id).remove(this);
			this.reverse();
			listKomaOnHand.get(SenteGote.Sente.id).add(this);
		}
		// OnHandForSente -> OnHandForSente
		public void moveFromStoS() {
			// do nothing
		}
		// OnHandForGote -> OnHandForGote
		public void moveFromGtoG() {
			// do nothing
		}
	}
}
// -------------------------------------------------------------------------
// ----------------------- << Koma Action >> -------------------------------
// -------------------------------------------------------------------------
