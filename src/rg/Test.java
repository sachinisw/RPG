package rg;

import java.util.Arrays;

public class Test {
	public static void main(String[] args) {
		String st = "(pmove_A-B)(at k)";
			if(st.contains("_")) {
				String parts [] = st.split("\\)\\(");
				String neg = "";
				System.out.println(Arrays.toString(parts));
				for (String s : parts) {
					if(s.startsWith("(p")) {
						s = "( not " + s + "))";
						System.out.println(s);
					}else {
						s = "("+s;
					}
					neg+= s;
				}
				System.out.println(neg);
			}
	}
}
