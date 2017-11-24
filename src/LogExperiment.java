import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class LogExperiment {
	
  static LogExperiment logExperiment;
  static BufferedWriter f_real_sols;     // soluções reais dos dias para plotagem
  static BufferedWriter f_longests;
  static BufferedWriter f_total_costs;
 
 private LogExperiment() throws IOException{
	 // arquivo para gravação das soluções reais para plotagem
     this.f_real_sols = new BufferedWriter(new FileWriter("outs/plot_real_sols.txt"));
     //TODO Configurar precisão do float
     //f_real_sols << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

     this.f_longests = new BufferedWriter(new FileWriter("outs/longests.txt"));
     //TODO Configurar precisão do float
     //f_longests << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

     this.f_total_costs = new BufferedWriter(new FileWriter("outs/total_costs.txt"));
 }
 
 public static synchronized LogExperiment getInstance() throws IOException {
		if (logExperiment == null)
			logExperiment = new LogExperiment();

		return logExperiment;
	}
 public void writeF_LONGESTS(String data) throws IOException{
	 this.f_longests.write(data);
 }
 
 public void writeF_TOTAL_COSTS(String data) throws IOException{
	 this.f_total_costs.write(data);
 }
 
 public void writeF_REAL_SOLS(String data) throws IOException{
	 this.f_real_sols.write(data);
	
 }

public void closeFiles() throws IOException {
	this.f_longests.close();
	this.f_real_sols.close();
	this.f_total_costs.close();
	
}
 

 
}
