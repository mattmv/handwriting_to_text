import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;

public class pageParser {
	static int count1 = 0;
	static int countt2 = 0;
	int boxDimension;
	BufferedImage rawImage;
	BufferedImage parsedImage;
	BufferedImage axisImage;
	ArrayList<Integer> horizontalLines;
	ArrayList<Integer> verticalLines;
	ArrayList<String> toOutput;
	int HMTITC = 0;
	public pageParser(int boxDimension){
		this.boxDimension = boxDimension;
		rawImage = null;
		toOutput = new ArrayList<String>();
		verticalLines = new ArrayList<Integer>();
		horizontalLines = new ArrayList<Integer>();
	}
	int initialAxisStart;
	int initialAxisEnd;
	public void readInImage(String filename){
		try{
			rawImage = ImageIO.read(new File(filename));
		}catch(IOException e){
			System.out.println("Error reading in image");
			e.printStackTrace();
		}
	}
	
	public void findWriting(){
		parsedImage = rawImage;
		int height = rawImage.getHeight();
		int width = rawImage.getWidth();
		double highestLum = -999999;
		double lowestLum = 999999;		
		for(int i = 0; i < width; i++){
			for(int j = 0 ; j<height ; j++){
				Color myColor = new Color(rawImage.getRGB(i, j));
				
				int myColorRed = myColor.getRed();
				int myColorGreen = myColor.getGreen();
				int myColorBlue = myColor.getBlue();
				
				double lum = (0.2126*myColorRed) + (0.7152*myColorGreen) + (0.0722*myColorBlue);
				if(lum < lowestLum){
					lowestLum = lum;
				}
				if(lum>highestLum){
					highestLum = lum;
				}
			}
		}	
	//	System.out.println("Highest Lum: " + highestLum + " Lowest Lum: " + lowestLum );
		double lumDiff = highestLum - lowestLum;
		for(int i = 0; i < width; i++){
			for(int j = 0 ; j<height; j++){
				//System.out.println("i: " + i + " j: " + j);
				Color myColor = new Color(rawImage.getRGB(i, j));
		
				int myColorRed = myColor.getRed();
				int myColorGreen = myColor.getGreen();
				int myColorBlue = myColor.getBlue();
				
				double lum = (0.2126*myColorRed) + (0.7152*myColorGreen) + (0.0722*myColorBlue);
				//System.out.println("Lum: " + lum);
				
				if(lum < (lumDiff/2) - 10 ){
					count1++;
					parsedImage.setRGB(i,j,4478715);
				}
				else {
					parsedImage.setRGB(i,j,16532314);
				}
			}
		}
		try {
			ImageIO.write(parsedImage,"jpg",new File("Testparsedoutput.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public int findCellDimension(){
		int height = parsedImage.getHeight();
		int width = parsedImage.getWidth();
		BufferedImage testImage = parsedImage;
		//this is probably a large assumption, but the colored in block is probably gonna
		//be the biggest chunk of colored in area in the picture. so that how im gonna find the axis length.
		int highestStreak = 0;
		int[] streakRange = {0,0};
		int streakRow = 0;
		for(int i = 0; i<height; i++){
			int streak = 0;
			int last = 0;
			for(int j = 0 ; j <width; j++){
				int[] tempStreakRange = {0,0};
				int rgbLine = parsedImage.getRGB(j, i);
				//System.out.println(rgbLine);
				if(rgbLine == last && rgbLine == -12298501){
					if(streak == 0){
						tempStreakRange[0] = j;
					}
					testImage.setRGB(j, i, 16744703);
					streak++;
					tempStreakRange[1] = j;
				}
				else{
					streak = 0;
				}
				if(streak > highestStreak){
					highestStreak = streak;
					streakRange = tempStreakRange;
					streakRow = i;
				}
				last = rgbLine;
			}
			
		 }
		//System.out.println("Highest Streak: " + highestStreak);
		
		//System.out.println("Streak Range. From Pixel: " + (streakRange[1] - highestStreak) + " to pixel: " + streakRange[1] + " on row: " + streakRow);
		
		initialAxisStart = (streakRange[1] - highestStreak);
		initialAxisEnd   = streakRange[1];
		
		try {
			ImageIO.write(testImage,"jpg",new File("Testpinkoutput.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return highestStreak;
	}
	public void drawCells(int numberOfColumns, int numberOfRows){
		axisImage = parsedImage;
		ArrayList<Integer> xGridRows = new ArrayList<Integer>();
		for(int i = 0; i < axisImage.getHeight(); i++){
			int numberOfBluePixels = 0;
			for(int j = 0 ; j < axisImage.getWidth() ; j++){
				if(axisImage.getRGB(j,i) == -12298501){
					numberOfBluePixels++;
				}
			}
			if(numberOfBluePixels < 80){
				if(!xGridRows.contains(i))xGridRows.add(i);
			}
		}
		
		
		ArrayList<Integer> tempList = new ArrayList<Integer>();
		for(int i = 1 ; i < xGridRows.size(); i ++){
			if(xGridRows.get(i) - xGridRows.get(i-1) <= 5 ){
				if(!tempList.contains(xGridRows.get(i))){tempList.add(xGridRows.get(i));}
				if(!tempList.contains(xGridRows.get(i-1))){tempList.add(xGridRows.get(i-1));}
				
			}
			else if(!tempList.isEmpty()){
				int safe = tempList.size()/2 + tempList.get(0);
				
				for(int j = 0 ; j < tempList.size() ; j++){
					if(tempList.get(j) != safe){
						
						xGridRows.remove(tempList.get(j));
					}
				}
				i = 1;
				tempList = new ArrayList<Integer>();
				
			}
			else{
			}
		}
		if(!tempList.isEmpty()){
			int safe = tempList.size()/2 + tempList.get(0);
			for(int j = 0 ; j < tempList.size() ; j++){
				if(tempList.get(j) != safe){
					xGridRows.remove(tempList.get(j));
				}
			}
			tempList = new ArrayList<Integer>();
			System.out.println("");
		}
		
		
		int total = 0;
		for(int i = 1; i < xGridRows.size(); i++){
			total += xGridRows.get(i) - xGridRows.get(i-1);
		}
		int avg = total / xGridRows.size();
		for(int i = 1; i < xGridRows.size(); i++){
			if(xGridRows.get(i) - xGridRows.get(i-1) > avg + (avg - (avg/5) - 1 )){
				Integer toAdd = xGridRows.get(i-1) + avg;
				Integer bound = xGridRows.get(i);
				while(toAdd < (bound)){
					if(!xGridRows.contains(toAdd))xGridRows.add(i,toAdd);
					toAdd+=avg;
				}

			}
		}
		
//	code to fix lowercase i's and j's /////
//			xGridRows.remove(25);
//			xGridRows.remove(26);
//			xGridRows.remove(27);
//			xGridRows.remove(29);
//			int replacement = xGridRows.get(29).intValue() - 20;
//			xGridRows.remove(29);
//			xGridRows.add(29,new Integer(replacement));
//			
//	/////
		//fix y-I
//		xGridRows.remove(5);
//		xGridRows.remove(11);
//		Integer repl = new Integer(xGridRows.get(9) + 32);
//		xGridRows.set(9, repl);
//		xGridRows.remove(26);
//		System.out.println("Grid now: " + xGridRows);
		
		for(int i = 0; i < xGridRows.size(); i ++ ){
			for(int j = 0; j < axisImage.getWidth() ; j++){
				axisImage.setRGB(j, xGridRows.get(i).intValue(), 4846921);
			}
		}
/////////////////////////////////////////////////////////////////////////////////////////
		
		ArrayList<Integer> yGridRows = new ArrayList<Integer>();
		for(int i = 0; i < axisImage.getWidth(); i++){
			int numberOfBluePixels = 0;
			for(int j = 0 ; j < axisImage.getHeight() ; j++){
//				System.out.println("i:" + i + ", j:" + j + ", width:" + axisImage.getWidth() + ", height:" + axisImage.getHeight());
				if(axisImage.getRGB(i,j) == -12298501){
					numberOfBluePixels++;
				}
			}
			if(numberOfBluePixels < 2){
				System.out.println("Y pixels: " + i);
				if(!yGridRows.contains(i))yGridRows.add(i);
			}
		}
		
		
		tempList = new ArrayList<Integer>();
		for(int i = 1 ; i < yGridRows.size(); i ++){
			if(yGridRows.get(i) - yGridRows.get(i-1) <= 10 ){
				if(!tempList.contains(yGridRows.get(i))){tempList.add(yGridRows.get(i));}
				if(!tempList.contains(yGridRows.get(i-1))){tempList.add(yGridRows.get(i-1));}
				
			}
			else if(!tempList.isEmpty()){
				int safe = tempList.size()/2 + tempList.get(0);
				
				for(int j = 0 ; j < tempList.size() ; j++){
					if(tempList.get(j) != safe){
						yGridRows.remove(tempList.get(j));
					}
				}
				i = 1;
				tempList = new ArrayList<Integer>();
				
			}
			else{
			}
		}
		if(!tempList.isEmpty()){
			
			int safe = tempList.size()/2 + tempList.get(0);
			for(int j = 0 ; j < tempList.size() ; j++){
				if(tempList.get(j) != safe){
					yGridRows.remove(tempList.get(j));
				}
			}
			tempList = new ArrayList<Integer>();
		}
		
		
		
		
		total = 0;
		for(int i = 1; i < yGridRows.size(); i++){
			total += yGridRows.get(i) - yGridRows.get(i-1);
		}
		avg = total / yGridRows.size();
		for(int i = 1; i < yGridRows.size(); i++){
			if(yGridRows.get(i) - yGridRows.get(i-1) > avg + (avg - (avg/5) - 1 )){
				Integer toAdd = yGridRows.get(i-1) + avg;
				Integer bound = yGridRows.get(i);
				while(toAdd < (bound)){
					//yGridRows.add(i,toAdd);
					toAdd+=avg;
				}

			}
		}
//		//Code to fix m-x
//		yGridRows.remove(1);
//		Integer repl = new Integer(yGridRows.get(1).intValue()-17);
//		yGridRows.set(1, repl);
		
//		//Code to fix y-I
//		yGridRows.remove(2);
//		yGridRows.remove(3);
//		yGridRows.remove(16);
		for(int i = 0; i < yGridRows.size(); i ++ ){
			for(int j = 0; j < axisImage.getHeight() ; j++){
				axisImage.setRGB(yGridRows.get(i).intValue(), j, 4846921);
			}
		}
		Collections.sort(yGridRows);
		Collections.sort(xGridRows);
		
		

		verticalLines = yGridRows;
		horizontalLines = xGridRows;
		
		
		try {
			ImageIO.write(axisImage,"jpg",new File("TestAxisImage.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Point> getCornerData(int numberOfColumns, int numberOfRows){
		ArrayList<Point> grid = new ArrayList<Point>();
		for(int i = 0 ; i < verticalLines.size(); i++){
			for(int j = 0; j < horizontalLines.size(); j++){
//				System.out.println("x = " + verticalLines.get(i).intValue() +  ", y = " + horizontalLines.get(j).intValue() );
				grid.add( new Point(verticalLines.get(i).intValue(),horizontalLines.get(j).intValue()));
			}
		}
		
		
		
		
//		for(int i = 0; i < axisImage.getWidth(); i++){
//			for(int j = 0; j < axisImage.getHeight(); j++){
//				if(i == 0 && j == 0){}
//				if(axisImage.getRGB(i, j) == -16777216){
////					System.out.println("Adding at: " + i + ", " + j);
//					if( i >= axisImage.getWidth()-1){	
//						continue;
//					}
//					else{
//						grid.add(new Point(i,j));
//					}
//				}
//			}
//		}
		
		
		
		try {
			ImageIO.write(axisImage,"jpg",new File("TestAxisImage.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println(grid);
		return grid;
		
		
	}
	
	
	public void getStatistics(int numberOfColumns, int numberOfRows){
		ArrayList<Point> grid = getCornerData(numberOfColumns, numberOfRows);
		System.out.println("Grid: " + grid);
//		int i = 35;
		for(int i = 0; i < numberOfRows ; i++){
			int j = i;
			int count = 0;
			while(true){
				if(j + 1 + (horizontalLines.size()) > grid.size() -1){break;}
				count++; 
//				System.out.println("J: " + j);
				Point topLeft = grid.get(j);
				System.out.println("J: " + j);
				System.out.println("TopLeft: " + topLeft);
				Point topRight = grid.get(j+horizontalLines.size());
				System.out.println("TopRight: " + topRight);
				Point bottomLeft = grid.get(j+1);
				System.out.println("BottomLeft: " + bottomLeft);
				Point bottomRight = grid.get(j +1+ horizontalLines.size());
				System.out.println("BottomRIght: " + bottomRight);
				System.out.println();
//				 if(i>numberOfColumns)System.out.println("================================look below==================================");
				analyzeSquare(topLeft, topRight, bottomLeft, bottomRight);
				j += horizontalLines.size();
//				System.out.println("New J: " + j);
			}			
//			System.out.println("COUNT: " + count);
			try {
				ImageIO.write(axisImage,"jpg",new File("TestAxisImage.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void analyzeSquare(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight){
		HMTITC++;
		//Hahahaha. Yeah, sorry. 
		//If you're here, crack your knuckles, draw up an instance of a valid square on a piece of paper and split it into quadrants. 
//		System.out.println("topLeft: x= " + topLeft.x + ", y=" + topLeft.y + " topRight: x= " + topRight.x + ", y=" + topRight.y + " bottomLeft: x= " + bottomLeft.x + ", y=" + bottomLeft.y + " bottomRight: x= " + bottomRight.x + ", y=" + bottomRight.y );
//		System.out.println("Total");
		double totalPFilled = percentFilled(topLeft, topRight, bottomLeft, bottomRight);
		Point topMiddle = new Point(topLeft.x + ((topRight.x - topLeft.x)/2),topLeft.y);
		Point middleMiddle = new Point(topLeft.x + ((topRight.x - topLeft.x)/2),topRight.y + ((bottomRight.y-topRight.y)/2));
		Point bottomMiddle = new Point(bottomLeft.x + ((bottomRight.x-bottomLeft.x)/2),bottomRight.y);
		Point leftMiddle = new Point(topLeft.x,(topLeft.y + (bottomLeft.y - topLeft.y)/2));
		Point rightMiddle = new Point(topRight.x,(topRight.y + (bottomRight.y - topRight.y)/2));
//		System.out.println("Q1");
		double q1PFilled = percentFilled(topLeft, topMiddle, leftMiddle, middleMiddle);
//		System.out.println("Q2");
		double q2PFilled = percentFilled(topMiddle,topRight,middleMiddle,rightMiddle);
			   axisImage.setRGB(topMiddle.x, topMiddle.y, 16777215);
			   axisImage.setRGB(topRight.x, topRight.y,16777215);
			   axisImage.setRGB(middleMiddle.x, middleMiddle.y,16777215);
			   axisImage.setRGB(rightMiddle.x, rightMiddle.y,16777215);
//		System.out.println("Q3");
		double q3PFilled = percentFilled(leftMiddle, middleMiddle, bottomLeft,bottomMiddle);
//		System.out.println("Q4");
		double q4PFilled = percentFilled(middleMiddle, rightMiddle, bottomMiddle, bottomRight);
		
		
//		System.out.println("Q1");
//		double q1PFilled = percentFilled();
//		System.out.println("Q2");
//		double q2PFilled = percentFilled(new Point(topRight.x/2,topRight.y),topRight, new Point(bottomRight.x/2,bottomRight.y/2), new Point(topRight.x,bottomRight.y/2));
//		System.out.println("Q3");
//		double q3PFilled = percentFilled(new Point(bottomLeft.x,bottomLeft.y/2),new Point(bottomRight.x/2,bottomRight.y/2),bottomLeft, new Point(bottomRight.x/2,bottomRight.y));
//		System.out.println("Q4");
//		double q4PFilled = percentFilled(new Point(bottomRight.x/2,bottomRight.y/2),new Point(bottomRight.x,bottomRight.y/2),new Point(bottomRight.x/2,bottomRight.y),bottomRight);
		double leftToRightRatio;
		if((q2PFilled + q4PFilled)/2 != 0){
			leftToRightRatio = ((q1PFilled + q3PFilled)/2) / ((q2PFilled + q4PFilled)/2);
		} else {leftToRightRatio = 10.0;}
		double topToBottomRatio;
		if((q3PFilled + q4PFilled)/2 != 0){
			topToBottomRatio= ((q1PFilled + q2PFilled)/2) / ((q3PFilled + q4PFilled)/2);
		} else {topToBottomRatio = 10.0;}
		
		String toWrite = totalPFilled*100 + ", " + q1PFilled*100 + ", " + q2PFilled*100 + ", " + q3PFilled*100 + ", "+ q4PFilled*100 + ", "+ leftToRightRatio + ", " + topToBottomRatio;
		
		//System.out.println(toWrite);
		toOutput.add(toWrite);
	}
	
	public double percentFilled(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight){
//		x	System.out.println("topLeft: x= " + topLeft.x + ", y=" + topLeft.y + " topRight: x= " + topRight.x + ", y=" + topRight.y + " bottomLeft: x= " + bottomLeft.x + ", y=" + bottomLeft.y + " bottomRight: x= " + bottomRight.x + ", y=" + bottomRight.y );
		double numberOfDots = 0;
		double totalArea = 0;
		for(int i = topLeft.y; i<bottomRight.y;i++){
			for(int j = topLeft.x; j<bottomRight.x; j++){
				if(i >= parsedImage.getHeight() || j >= parsedImage.getWidth()){
//					System.out.println("breaking");
//					System.out.println("Because i: " + i + " and j: " +j);
//					System.out.println("Width = " + parsedImage.getWidth() + " Height: " + parsedImage.getHeight());
					break;}
				if(parsedImage.getRGB(j, i) == -12298501 ){
					countt2++;
					numberOfDots++;
				}
				totalArea++;
			}
		}
//		System.out.println("numberOfDots: " + numberOfDots + " totalArea: " + totalArea);
		
		return numberOfDots/totalArea;
	}
	
	public void writeOutputFile(int numberOfColumns,int numberOfRows,String filename){
		try{
		    PrintWriter writer = new PrintWriter("trialOutput.txt", "UTF-8");
		    BufferedReader formatReader = new BufferedReader(new FileReader(filename));
		    String addon = "";
		    for(int i = 0 ; i < toOutput.size(); i++){	
		    	if(i%numberOfColumns == 0)addon = formatReader.readLine();
		    	writer.println(toOutput.get(i) + ", " + addon);
		    }
		    
		    writer.close();
		} catch (IOException e) {
		   e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		int boxDimension = Integer.parseInt(args[1]);
		int numberOfColumns = Integer.parseInt(args[3]);
		int numberOfRows = Integer.parseInt(args[2]);
		String formatFile = args[4];
		pageParser pp = new pageParser(boxDimension);
		pp.readInImage(args[0]);
		System.out.println("Distingishing writing");
		pp.findWriting();
		System.out.println("Drawing Grid Outline");
		pp.drawCells(numberOfColumns, numberOfRows);
		System.out.println("Gathering Cell Data");
		pp.getStatistics(numberOfColumns,numberOfRows);
		System.out.println("Writing OutPutFile");
		pp.writeOutputFile(numberOfColumns,numberOfRows,formatFile);
		System.out.println("All Done!");
		System.out.println(pp.HMTITC + " cells were successfully analyzed");
		
		
//		System.out.println("GRID: RGB AT (0,445) -> " + pp.axisImage.getRGB(0, 445));
//		System.out.println("GRID: RGB AT (0,448) -> " + pp.axisImage.getRGB(0, 448));
	}
	
}
