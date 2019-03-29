package ffplan;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlanExtractor {

	private ArrayList<Plan> planSet;
	private int emptyLineCounter;
	
	public PlanExtractor (){
		this.planSet = new ArrayList<>();
		this.emptyLineCounter = 0;
	}
		
	public void readFFPlanOutput(String filename){

		String outStr="";
		boolean startReadingLine = false;
		ArrayList<String> planSteps = new ArrayList<>();
		
		Pattern p = Pattern.compile("\\s{1,}[0-9]{1,}:");
				

		try {
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			Plan aPlan = new Plan(planSteps, filename);
			this.emptyLineCounter = 0;
			
			while((outStr = bufferedReader.readLine()) != null) {
				if(outStr.equalsIgnoreCase("ff: found legal plan as follows")){
					startReadingLine = true;
				}
				
				if((startReadingLine) && (!outStr.equalsIgnoreCase("ff: found legal plan as follows"))){
					Matcher m = p.matcher(outStr);
					if(m.find()){
						aPlan.addPlanStep(outStr);
					}
				}else if(outStr.equalsIgnoreCase("ff: goal can be simplified to TRUE. The empty plan solves it")){
					aPlan.addPlanStep("Goal Reached");
				}
				
				if((startReadingLine) && (outStr.isEmpty())){
					emptyLineCounter++;
				}
				
				if(emptyLineCounter > 1){
					startReadingLine = false;
				}

			}    
			planSet.add(aPlan);
			bufferedReader.close();

		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void printPlans(){
		for(int i=0; i<this.planSet.size(); i++){
			Plan p = this.planSet.get(i);
			System.out.println(p);
		}
	}
	
	public ArrayList<String> getPlanIDs(){
		ArrayList<String> ids = new ArrayList<>();
		
		for(int i=0; i<this.planSet.size(); i++){
			Plan p = this.planSet.get(i);
			ids.add(p.getPlanID());
		}
		
		return ids;
	}
	
	public ArrayList<Plan> getPlanSet(){
		return this.planSet;
	}
}


