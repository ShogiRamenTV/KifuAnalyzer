package lib;

public class BestPointData {
		int score;
		String[] moveName;
		BestPointData(int numOfMultiPV) {
			moveName = new String[numOfMultiPV];
			for(int index=0; index<numOfMultiPV; index++) moveName[index] = new String("");
		}
	}