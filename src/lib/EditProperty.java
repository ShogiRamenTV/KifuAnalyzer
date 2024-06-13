package lib;

import java.awt.FileDialog;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.JFrame;

public class EditProperty {
	String propertyFile = "KifuAnalyzer.properties";
	public enum PropertyType {
		Engine, Color;
	};
	public EditProperty() {
		
	}
	public String loadProperty(String key) {
		Properties settings = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			settings.load(in);
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
		return settings.getProperty(key);
	}
	public void setPropertyForEngine(JFrame fr) {
		Path path = Paths.get("").toAbsolutePath();
		FileDialog fd = new FileDialog(fr, "Load", FileDialog.LOAD);
		fd.setDirectory(path.toString());
		fd.setVisible(true);
		if(fd.getFile() == null) return;
		String fileName = fd.getDirectory() + fd.getFile();
		setProperty(PropertyType.Engine.name(), fileName);
	}
	public void setProperty(String key, String value) {
		Properties properties = new Properties();
		try {
			for(PropertyType pt: PropertyType.values()) {
				String str = loadProperty(pt.name());
				if(pt.name().equals(key)) {
					properties.setProperty(key, value);
				} else {
					if(str == null) continue;
					properties.setProperty(pt.name(), str);
				}
			}
			properties.store(new FileOutputStream(propertyFile), "Comments");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}