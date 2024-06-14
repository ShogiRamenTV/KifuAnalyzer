package lib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;

import lib.KifuDataBase.KifuData;
import lib.ListBoxData.ListBoxType;
import lib.ShogiData.SenteGote;
import lib.TextBoxData.TextBoxType;

// -------------------------------------------------------------------------
// ----------------------- << Player Data >> -----------------------------
// -------------------------------------------------------------------------
public class PlayerDataBase {
	String imgFilePath = "./img/";
	String imgFilePathPlayerIcon = imgFilePath + "playerIcon/";
	List<PlayerData> playerDataBase = new ArrayList<PlayerData>();
	DefaultListModel<String> listModel[];
	JList<String> listBox[];
	JTextField textBox[];
	KifuDataBase kdb;
	CanvasBoard cv;
	public PlayerDataBase(DefaultListModel<String> lm[], JList<String> lb[],
			JTextField tb[], KifuDataBase kb, CanvasBoard c) {
		listModel = lm;
		listBox = lb;
		textBox = tb;
		kdb = kb;
		cv = c;
	}
	
	public class PlayerData {
		String playerName;
		List<GameResult> grList = new ArrayList<GameResult>();
		
		PlayerData(String name) {
			playerName = name;
		}
	}
	public class GameResult {
		String strategy;
		String castle;
		int isPlayerWin;
		Boolean isSente;
		
		GameResult() {}
	}
	public class GameResultCount {
		String str;
		int cnt;
		int senteWinCnt = 0;
		int senteLoseCnt = 0;
		int senteDrawCnt = 0;
		int goteWinCnt = 0;
		int goteLoseCnt = 0;
		int goteDrawCnt = 0;
		GameResultCount(String s, int isPlayerWin, Boolean isSente) {
			str = s;
			cnt = 1;
			if(isPlayerWin == 1) {
				if(isSente) {
					senteWinCnt = 1;
				} else {
					goteWinCnt = 1;
				}
			} else if(isPlayerWin == 0) {
				if(isSente) {
					senteLoseCnt = 1;
				} else {
					goteLoseCnt = 1;
				}
			} else {
				if(isSente) {
					senteDrawCnt = 1;
				} else {
					goteDrawCnt = 1;
				}
			}
		}
	}
	public void createPlayerDataBase() {
		listModel[ListBoxType.Player.id].clear();
		listBox[ListBoxType.Player.id].setModel(listModel[ListBoxType.Player.id]);
		playerDataBase.clear();
		
		for(KifuData kd: kdb.kifuDB) {
			Boolean foundS = false;
			Boolean foundG = false;
			GameResult grS = new GameResult();
			GameResult grG = new GameResult();
			grS.strategy = kd.strategyName;
			grG.strategy = kd.strategyName;
			grS.isSente = true;
			grG.isSente = false;
			if(kd.isSenteWin == 1) {
				grS.isPlayerWin = 1;
				grG.isPlayerWin = 0;
			}
			else if(kd.isSenteWin == 0) {
				grS.isPlayerWin = 0;
				grG.isPlayerWin = 1;
			} else { // Draw
				grS.isPlayerWin = -1;
				grG.isPlayerWin = -1;
			}
			grS.castle = kd.castleName[SenteGote.Sente.id];
			grG.castle = kd.castleName[SenteGote.Gote.id];
			
			for(PlayerData pd: playerDataBase) {
				if(kd.playerName[SenteGote.Sente.id].equals(pd.playerName)) {
					foundS = true;
					pd.grList.add(grS);
				} else if(kd.playerName[SenteGote.Gote.id].equals(pd.playerName)) {
					foundG = true;
					pd.grList.add(grG);
				}
			}
			
			if(!foundS) {
				PlayerData pd = new PlayerData(kd.playerName[SenteGote.Sente.id]);
				pd.grList.add(grS);
				playerDataBase.add(pd);
			}
			if(!foundG) {
				PlayerData pd = new PlayerData(kd.playerName[SenteGote.Gote.id]);
				pd.grList.add(grG);
				playerDataBase.add(pd);
			}
		}
		
		Collections.sort(
				playerDataBase,
				new Comparator<PlayerData>() {
					@Override
					public int compare(PlayerData obj1, PlayerData obj2) {
						return obj2.grList.size() - obj1.grList.size();
					}
				}
				);
		
		String str = "<Total:" + playerDataBase.size() + " Players>";
		listModel[ListBoxType.Player.id].addElement(str);
		listModel[ListBoxType.Player.id].addElement("---------");
		
		for(PlayerData pd: playerDataBase) {
			listModel[ListBoxType.Player.id].addElement(pd.playerName);
		}
		listBox[ListBoxType.Player.id].setModel(listModel[ListBoxType.Player.id]);
	}
	public void updateListBox2ByPlayerName() {
		int selectedIndex = listBox[ListBoxType.Player.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		
		PlayerData pd = playerDataBase.get(selectedIndex);
		textBox[TextBoxType.Player1.id].setText(pd.playerName);
		updatePlayerIcon();
		
		// count strategy data
		List<GameResultCount> grcList = new ArrayList<GameResultCount>();
		countStrategyDataByPlayerData(grcList, pd);
		
		// count castle data
		List<StringCount> strList = new ArrayList<StringCount>();
		countCastleDataByPlayerData(strList, pd);
		
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
		
		listModel[ListBoxType.Info.id].addElement("<" + pd.playerName + "'s Winning Rate>");
		
		int totalCnt = 0;
		int totalWinCnt = 0;
		int totalLoseCnt = 0;
		int totalDrawCnt = 0;
		int totalSenteWinCnt = 0;
		int totalGoteWinCnt = 0;
		int totalSenteLoseCnt = 0;
		int totalGoteLoseCnt = 0;
		int totalSenteDrawCnt = 0;
		int totalGoteDrawCnt = 0;
		for(GameResultCount grc: grcList) {
			totalCnt += grc.cnt;
			totalWinCnt += grc.senteWinCnt + grc.goteWinCnt;
			totalLoseCnt += grc.senteLoseCnt + grc.goteLoseCnt;
			totalDrawCnt += grc.senteDrawCnt + grc.goteDrawCnt;
			totalSenteWinCnt += grc.senteWinCnt;
			totalGoteWinCnt += grc.goteWinCnt;
			totalSenteLoseCnt += grc.senteLoseCnt;
			totalGoteLoseCnt += grc.goteLoseCnt;
			totalSenteDrawCnt += grc.senteDrawCnt;
			totalGoteDrawCnt += grc.goteDrawCnt;
		}
		String str = "Total " + String.format("%d games %d Win:%d Lose %d Draw", totalCnt, totalWinCnt, totalLoseCnt, totalDrawCnt);
		Double d = (double)totalWinCnt/(double)(totalWinCnt+totalLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		listModel[ListBoxType.Info.id].addElement(str);
		
		str = String.format("Sente %d games %d Win:%d Lose %d Draw", totalSenteWinCnt+totalSenteLoseCnt+totalSenteDrawCnt, totalSenteWinCnt, totalSenteLoseCnt, totalSenteDrawCnt);
		d = (double)totalSenteWinCnt/(double)(totalSenteWinCnt+totalSenteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		listModel[ListBoxType.Info.id].addElement(str);
		str = String.format("Gote %d games %d Win:%d Lose: %d Draw", totalGoteWinCnt+totalGoteLoseCnt+totalGoteDrawCnt, totalGoteWinCnt, totalGoteLoseCnt, totalGoteDrawCnt);
		d = (double)totalGoteWinCnt/(double)(totalGoteWinCnt+totalGoteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		listModel[ListBoxType.Info.id].addElement(str);
		
		listModel[ListBoxType.Info.id].addElement("---------");
		str = "Total Strategies: " + grcList.size() + " patterns";
		listModel[ListBoxType.Info.id].addElement(str);
		for(GameResultCount grc: grcList) {
			str = grc.str;
			d = (double)(grc.senteWinCnt+grc.goteWinCnt)/(double)(grc.cnt)*100;
			str += ":" + String.format("%d", grc.cnt) + " games";
			str += "(Winning Rate" + String.format("%.0f", d) + "%)";
			listModel[ListBoxType.Info.id].addElement(str);
		}
		
		listModel[ListBoxType.Info.id].addElement("---------");
		for(StringCount sc: strList) {
			str = sc.str;
			str += ":" + String.format("%d", sc.cnt) + " games";
			listModel[ListBoxType.Info.id].addElement(str);
		}
		
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}
	public void countStrategyDataByPlayerData(List<GameResultCount> grcList, PlayerData pd) {
		for(GameResult gr: pd.grList) {
			Boolean found = false;
			for(GameResultCount grc: grcList) {
				if(grc.str.equals(gr.strategy)) {
					found = true;
					grc.cnt++;
					if(gr.isPlayerWin == 1) {
						if(gr.isSente) grc.senteWinCnt++;
						else grc.goteWinCnt++;
					} else if(gr.isPlayerWin == 0) {
						if(gr.isSente) grc.senteLoseCnt++;
						else grc.goteLoseCnt++;
					} else {
						if(gr.isSente) grc.senteDrawCnt++;
						else grc.goteDrawCnt++;
					}
				}
			}
			if(!found) {
				GameResultCount grc = new GameResultCount(gr.strategy, gr.isPlayerWin, gr.isSente);
				grcList.add(grc);
			}
		}
		
		Collections.sort(
				grcList,
				new Comparator<GameResultCount>() {
					@Override
					public int compare(GameResultCount obj1, GameResultCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
	}
	public void countCastleDataByPlayerData(List<StringCount> strList, PlayerData pd) {
		for(GameResult gr: pd.grList) {
			Boolean found = false;
			for(StringCount sc: strList) {
				if(sc.str.equals(gr.castle)) {
					found = true;
					sc.cnt++;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(gr.castle, 1);
				strList.add(sc);
			}
		}
		Collections.sort(
				strList,
				new Comparator<StringCount>() {
					@Override
					public int compare(StringCount obj1, StringCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
	}
	public void updatePlayerIcon() {
		String playerName[] = new String[2];
		playerName[SenteGote.Sente.id] = new String(textBox[TextBoxType.Player1.id].getText());
		playerName[SenteGote.Gote.id] = new String(textBox[TextBoxType.Player2.id].getText());
		
		for(SenteGote sg: SenteGote.values()) {
			try {
				BufferedImage img = ImageIO.read(new File(imgFilePathPlayerIcon + playerName[sg.id] + ".jpg"));
				cv.playerIcon[sg.id] = img.getScaledInstance(100, 133, java.awt.Image.SCALE_SMOOTH);
			} catch(IOException e) {
				cv.playerIcon[sg.id] = null;
			}
		}
		cv.repaint();
	}
	public void initializePlayerIcon() {
		for(SenteGote sg: SenteGote.values()) cv.playerIcon[sg.id] = null;
	}
}