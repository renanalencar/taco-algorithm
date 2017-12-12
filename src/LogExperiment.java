import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class LogExperiment {
	
  static LogExperiment logExperiment;

  static BufferedWriter f_log_stand_exper;
  static BufferedWriter f_sols_aco; 
  static BufferedWriter teste; 
  
  	
  
  
  static BufferedWriter f_real_sols;     // soluções reais dos dias para plotagem
  static BufferedWriter f_longests;
  
  static BufferedWriter f_total_costs;
  static BufferedWriter f_simul_res; // arquivo com o resumo das simulações
  static BufferedWriter f_time_execs;
 public static BufferedWriter f_log_exper;
 static int i = 0;
 
 private LogExperiment() throws IOException{
	
	 
	
 }
 public void loadBuffersStandardExperiment() throws IOException {
	 this.f_sols_aco = new BufferedWriter(new FileWriter("outs/plot_best_sols_aco.txt"));
	 this.f_log_stand_exper = new BufferedWriter(new FileWriter("outs/log_stand_experiment.txt"));
	 this.teste = new BufferedWriter(new FileWriter("outs/teste.txt"));
 }
 public void loaBuffersdRealExperiment() throws IOException{
	 this.teste = new BufferedWriter(new FileWriter("outs/teste.txt"));
	 // arquivo para gravação das soluções reais para plotagem
     this.f_real_sols = new BufferedWriter(new FileWriter("outs/plot_real_sols.txt"));
     //TODO Configurar precisão do float
     //f_real_sols << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

     this.f_longests = new BufferedWriter(new FileWriter("outs/longests.txt"));
     //TODO Configurar precisão do float
     //f_longests << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

     this.f_total_costs = new BufferedWriter(new FileWriter("outs/total_costs.txt"));
     this.f_log_exper = new BufferedWriter(new FileWriter("outs/log_real_experiment.txt"));
     this.f_simul_res = new BufferedWriter(new FileWriter("outs/simulations_resume.txt"));

     //TODO Configuração precisão do float
     //f_simul_res << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

     this.f_time_execs = new BufferedWriter(new FileWriter("outs/time_execs.txt"));
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

public void flushFilesRealExperiment() throws IOException {
	this.f_longests.flush();
	this.f_real_sols.flush();
	this.f_total_costs.flush();
	this.f_log_exper.flush();
	this.f_time_execs.flush();
	this.f_simul_res.flush();
	this.teste.flush();
	
}
public void flushFilesStandardExperiment() throws IOException {
	this.f_sols_aco.flush();
	this.f_log_stand_exper.flush();
	this.teste.flush();
	
}


 

 
}
