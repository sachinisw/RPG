package utils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.PriorityQueue;

import graph.PriorityVertex;
import graph.PriorityVertexComparator;
import graph.StateVertex;

public class PQ {

	public static void main(String[] args) {
		PriorityQueue<PriorityVertex> stus = new PriorityQueue<>(10, new PriorityVertexComparator());
		PriorityVertex pv1= new PriorityVertex(new StateVertex(), 1, 0);
		PriorityVertex pv2= new PriorityVertex(new StateVertex(), 2, Integer.MAX_VALUE);
		PriorityVertex pv3= new PriorityVertex(new StateVertex(), 3, Integer.MAX_VALUE);
		PriorityVertex pv4= new PriorityVertex(new StateVertex(), 4, 1);

		stus.add(pv1);
		stus.add(pv2);
		stus.add(pv3);
		stus.add(pv4);
		
		for (PriorityVertex pv : stus) {
			System.out.println(pv);
		}
		
		System.out.println("------removing--------------");
		while(!stus.isEmpty()) {
			System.out.println(stus.poll());
		}
		System.out.println(Integer.MAX_VALUE);
		System.out.println(Integer.MAX_VALUE+1);//overflow
		
		String df = new DecimalFormat(".###").format(0.5235);
		System.out.println(df);
		
		String ad = "ON XXXX XX,CLEAR O,ONTABLE P,ONTABLE O,CLEAR P,HANDEMPTY,CLEAR XXXX,ONTABLE XX";
		String ag = "CLEAR O,CLEAR P,ONTABLE XX,ONTABLE P,HANDEMPTY,ON O XX";
		
		String[] adP= ad.split(",");
		String[] agP= ag.split(",");
		
		System.out.println(isStateEqualWithDeception(agP, adP));
		
	}
	
	private static boolean isStateEqualWithDeception(String[] agState, String[] adState){
		int [] check = new int[agState.length];
		for (int i=0; i<agState.length; i++) {
			for (int j=0; j<adState.length; j++) {
				if(!adState[j].contains("XXXX")){
					if(agState[i].equalsIgnoreCase(adState[j])){
						check[i]=1;
					}
				}else if(adState[j].contains("XXXX")){
					String partsAd[] = adState[j].split(" ");
					String optAd = partsAd[0].trim();
					String partsAg[] = agState[i].split(" ");
					String optAg = partsAg[0].trim();
					if(partsAd.length==partsAg.length){
						int count = 0;
						for(int x=1; x<partsAd.length; x++){
							if(partsAd[x].equals("XXXX") && partsAg[x].equals("XX")){
								count++;
							}else if(partsAd[x].equals(partsAg[x])){
								count++;
							}
						}
						if(optAd.equalsIgnoreCase(optAg) && count==partsAg.length-1){
							check[i]=1;
						}
					}
				}
			}
			System.out.print(agState[i]+"    ");
			System.out.println(Arrays.toString(check));
		}
		int sum = 0;
		for(int i=0; i<check.length; i++){
			sum += check[i];
		}
		return (sum==check.length);
	}
}
