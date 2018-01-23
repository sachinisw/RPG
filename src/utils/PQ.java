package utils;

import java.text.DecimalFormat;
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
		
		
	}
}
