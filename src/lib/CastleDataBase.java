package lib;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import lib.KifuDataBase.KifuData;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;

//-------------------------------------------------------------------------
// ----------------------- << Castle Data >> -----------------------------
// -------------------------------------------------------------------------

public class CastleDataBase {
	String strategyFilePathBase = "./strategy/";
	public String castleFilePath = "castle/";
	String imgFilePath = "./img/";
	String imgFilePathCastleIcon = imgFilePath + "castleIcon/";
	List<CastleData> castleDataBase = new ArrayList<CastleData>();
	public List<StringCount> castleCountData = new ArrayList<StringCount>();
	DefaultListModel<String> listModelCastle;
	JList<String> listBoxCastle;
	DefaultListModel<String> listModelPlayer;
	JList<String> listBoxPlayer;
	DefaultListModel<String> listModelInfo;
	JList<String> listBoxInfo;
	JList<String> listBoxStrategy;
	JTextField textBoxCastle;
	KifuDataBase kifuDataBase;
	CanvasBoard cv;
	StrategyDataBase strategyDataBase;
	public CastleDataBase(DefaultListModel<String> lmC, JList<String> lbC,
			DefaultListModel<String> lmP, JList<String> lbP,
			DefaultListModel<String> lmI, JList<String> lbI, JList<String> lbS,
			JTextField tC, CanvasBoard c,
			KifuDataBase kdb, StrategyDataBase sdb) {
		listModelCastle = lmC;
		listBoxCastle = lbC;
		listModelPlayer = lmP;
		listBoxPlayer = lbP;
		listModelInfo = lmI;
		listBoxInfo = lbI;
		listBoxStrategy = lbS;
		textBoxCastle = tC;
		kifuDataBase = kdb;
		cv = c;
		strategyDataBase = sdb;
	}
	
	public class CastleData {	
		String name;
		int data[][] = new int[25][4];
		public CastleData(String castleName) {
			name = castleName;
		}
	}
	public void actionForCastle(ShogiData sd, Boolean isRadioButtonSente) {
		Path path;
		String fileName;
		int index = 1;
		
		if(textBoxCastle.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Castle name is empty");
			return;
		}
		
		while(true) {
			fileName = strategyDataBase.strategyFilePathBase + castleFilePath + String.format("castle%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBoxCastle.getText() + "\n");
			for(Koma k: sd.k) {
				if((k.type == KomaType.King) && isRadioButtonSente && k.sente == 0) {
					saveListKomaAroundKing(sd, k, fw);
				}
				if((k.type == KomaType.King) && !isRadioButtonSente && k.sente == 1) {
					saveListKomaAroundKing(sd, k, fw);
				}
			}
			fw.close();
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public void loadCastleData() {
		System.out.print("Loading Castle Data ... ");
		castleDataBase.clear();
		try {
			int fileIndex = 1;
			while(true) {
				String fileName = strategyFilePathBase + castleFilePath + "castle" + String.format("%03d", fileIndex) + ".txt";
				File file = new File(fileName);
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String content;
				content = br.readLine(); // name
				CastleData castleData = new CastleData(content);
				int i = 0;
				while((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content,",");
					while(st.hasMoreTokens()) {
						castleData.data[i][0] =  Integer.parseInt(st.nextToken()); // sente
						castleData.data[i][1] = Integer.parseInt(st.nextToken()); // x
						castleData.data[i][2] = Integer.parseInt(st.nextToken()); // y
						castleData.data[i][3] = Integer.parseInt(st.nextToken()); // type
						i++;
					}
				}
				br.close();
				castleDataBase.add(castleData);
				fileIndex++;
				//System.out.println(castleData.name);
			}
		} catch(FileNotFoundException en) {
			//System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		System.out.println("Completed.");
	}
	public void countCastle() {
		listModelCastle.clear();
		listBoxCastle.setModel(listModelCastle);
		castleCountData.clear();
		
		for(KifuData kd: kifuDataBase.kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kd.castleName[SenteGote.Sente.id])) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kd.castleName[SenteGote.Sente.id], kd.isSenteWin);
				castleCountData.add(sc);
			}
		}
		for(KifuData kd: kifuDataBase.kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kd.castleName[SenteGote.Gote.id])) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kd.castleName[SenteGote.Gote.id], kd.isSenteWin);
				castleCountData.add(sc);
			}
		}
		
		Collections.sort(
				castleCountData,
				new Comparator<StringCount>() {
					@Override
					public int compare(StringCount obj1, StringCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
		
		int totalCnt = 0;
		for(StringCount sc: castleCountData) {	
			totalCnt += sc.cnt;
		}
		String str = "<Total:" + String.format("%2d", totalCnt)+" Castles>";
		listModelCastle.addElement(str);
		listModelCastle.addElement("----------");
		for(StringCount sc: castleCountData) {
			str = sc.str;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			listModelCastle.addElement(str);
		}
		listBoxCastle.setModel(listModelCastle);
	}
	public void updateListBoxInfoByCastle() {
		int selectedIndex = listBoxCastle.getSelectedIndex()-2;
		if(selectedIndex < 0) {
			initializeCastleIcon();
			return;
		}
		int selectedIndexStrategy = listBoxStrategy.getSelectedIndex();
		String strategyName = "";
		if(selectedIndexStrategy >= 2) {
			StringCount sc = strategyDataBase.strategyCountData.get(selectedIndexStrategy-2);
			strategyName = sc.str;
		}
		int selectedIndexPlayer = listBoxPlayer.getSelectedIndex();
		String playerName = "";
		if(selectedIndexPlayer >= 2) {
			playerName = listModelPlayer.getElementAt(selectedIndexPlayer);
			//System.out.println(playerName);
		}
		
		StringCount sc = castleCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String castleName = sc.str;
		updateCastleIcon();
		textBoxCastle.setText(castleName);
		
		listModelInfo.clear();
		listBoxInfo.setModel(listModelInfo);

		if(strategyName.equals("")) listModelInfo.addElement("<"+ castleName + "'s Kifu>");
		else listModelInfo.addElement("<"+ strategyName + "(" + castleName + ")"+"'s Kifu>");
		listModelInfo.addElement("-------------");
		for(KifuData kd: kifuDataBase.kifuDB) {
			if(kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName)) {
				String str = String.format("kf%03d:000:%s:", kd.index, kd.year);
				str += kd.playerName[SenteGote.Sente.id] + "(" + kd.castleName[SenteGote.Sente.id] + ")" + " vs " + kd.playerName[SenteGote.Gote.id] + "(" + kd.castleName[SenteGote.Gote.id] + ")";
				if(kd.isSenteWin == 1) str+="(Sente Win)";
				else if(kd.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				
				if(strategyName.equals("") && playerName.equals("")) {
					listModelInfo.addElement(str);
				} else if(!strategyName.equals("") && !playerName.equals("")) {
					if(kd.strategyName.equals(strategyName) && str.contains(playerName)) listModelInfo.addElement(str);
				} else if(!strategyName.equals("")) {
					if(kd.strategyName.equals(strategyName)) listModelInfo.addElement(str);
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) listModelInfo.addElement(str);
				}
			}
		}
		listBoxInfo.setModel(listModelInfo);
	}
	public String checkCastle(ShogiData sd, Boolean isSente) {
		for(Koma k: sd.k) {
			for(CastleData cd: castleDataBase) {
				if((k.type == KomaType.King) && isSente && k.sente == 0) {
					if(isSameCastle(sd, cd, k.sente)) {
						//System.out.println("castle" + String.format("%03d", castleDataBase.indexOf(cd)+1) + ".txt matched");
						return cd.name;
					}
				}
				if((k.type == KomaType.King) && !isSente && k.sente == 1) {
					if(isSameCastle(sd, cd, k.sente)) {
						//System.out.println("castle" + String.format("%03d", castleDataBase.indexOf(cd)+1) + ".txt matched");
						return cd.name;
					}
				}
			}
		}
		
		return "";
	}
	public Boolean isSameCastle(ShogiData sd, CastleData cd, int sente) {
		Boolean matched = false;
		for(int[] data: cd.data) {
			//Koma k = getKomaByPosition(sd, data[1], data[2], data[0]);
			Koma k = sd.findKoma(data[1], data[2], data[0]);
			if(k == null) {
				if(data[3] == KomaType.Empty.id && data[0] == sente) {
					matched = true;
				}
				else {
					return false;
				}
			}
			else if(k.type.id == data[3] && sente == data[0]) {
				matched = true;
			} else {
				return false;
			}
		}
		if(matched) return true;
		else return false;
	}
	public void saveListKomaAroundKing(ShogiData sd, Koma king, FileWriter fw) {
		Koma k;
		try {
			for(int x=-2; x<=2; x++) 
				for(int y=-2; y<=2; y++) {
					//k = getKomaByPosition(sd, king.px+x, king.py+y, king.sente);
					k = sd.findKoma(king.px+x, king.py+y, king.sente);
					if(k != null) {
						fw.write(k.sente + "," + k.px + "," + k.py + "," + k.type.id + "\n");
					} else {
						fw.write(king.sente + "," + String.valueOf(king.px+x) + "," + String.valueOf(king.py+y) + "," + KomaType.Empty.id + "\n");
					}
				}
		
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void updateCastleIcon() {
		int selectedIndex = listBoxCastle.getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		StringCount sc = castleCountData.get(selectedIndex);
		String castleName = sc.str;
		try {
			BufferedImage img = ImageIO.read(new File(imgFilePathCastleIcon + castleName + ".jpg"));
			cv.castleIcon = img.getScaledInstance(120, 160, java.awt.Image.SCALE_SMOOTH);
		} catch(IOException e) {
			cv.castleIcon = null;
		}
		cv.repaint();
	}
	public void initializeCastleIcon() {
		cv.castleIcon = null;
	}
}