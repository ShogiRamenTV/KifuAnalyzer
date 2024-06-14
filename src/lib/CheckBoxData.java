package lib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import lib.ShogiData.Koma;

public class CheckBoxData {
	public enum CheckBoxType {
		Edit(0), Reverse(1), Draw(2);
		public final int id;
		private CheckBoxType(final int id) {
			this.id = id;
		}
	};
	public JCheckBox checkBox[] = new JCheckBox[CheckBoxType.values().length];
	public JRadioButton radioButtonSente = new JRadioButton("Sente", true);
	public JRadioButton radioButtonGote = new JRadioButton("Gote");
	public JComboBox<String> comboBox;
	int baseXPosForItems;
	ShogiData sd;
	CanvasBoard cv;
	CanvasBoardForEngine cve;
	
	public CheckBoxData(int bPosX, ShogiData s, CanvasBoard c, CanvasBoardForEngine ce) {
		baseXPosForItems = bPosX;
		sd = s;
		cv = c;
		cve = ce;
	}
	public void update(CanvasBoard c, CanvasBoardForEngine ce) {
		cv = c;
		cve = ce;
	}
	public void clearCheckBox() {
		for(CheckBoxType cb: CheckBoxType.values()) {
			checkBox[cb.id].setSelected(false);
		}
		radioButtonSente.setSelected(true);
	}
	public void initializeCheckBox() {
		for(CheckBoxType cb: CheckBoxType.values()) {
			checkBox[cb.id] = new JCheckBox(cb.name());
		}
		checkBox[CheckBoxType.Edit.id].setBounds(baseXPosForItems+140, 35, 60, 12);
		checkBox[CheckBoxType.Reverse.id].setBounds(baseXPosForItems+190, 35, 80, 12);
		checkBox[CheckBoxType.Reverse.id].addActionListener(checkActionListener);
		checkBox[CheckBoxType.Draw.id].setBounds(baseXPosForItems+80, 35, 80, 12);
		radioButtonSente.setBounds(baseXPosForItems+360, 75, 70, 14);
		radioButtonGote.setBounds(baseXPosForItems+420, 75, 70, 14);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButtonSente);
		buttonGroup.add(radioButtonGote);
		comboBox = new JComboBox<>();
		comboBox.addItem("");
		comboBox.addItem("2023");
		comboBox.addItem("2022");
		comboBox.addItem("all");
		comboBox.setBounds(baseXPosForItems+80, 8, 100, 25);
	}
	
	private ActionListener checkActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        	for(Koma k: sd.k) {
        		k.reverseForReverseMode();
        	}
        	sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
        	sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
        	cv.reverseNumberRowCol(checkBox[CheckBoxType.Reverse.id].isSelected());
        	cv.repaint();
        	cve.repaint();
        }
    };
}