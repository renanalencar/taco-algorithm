import java.io.IOException;

/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Main implements ControlExperiment {

    public static void main(String[] args) throws IOException {
    	
    	LogExperiment log = LogExperiment.getInstance();

        if (MODE_EXECUTION == 1) { // experimento padrão
        	log.loadBuffersStandardExperiment();
            StandardExperiment se = new StandardExperiment();
            se.run_standard_experiment();  // sem uma instância como parâmetro é carregada a instância modelo defida em control.cpp
            log.flushFilesStandardExperiment();

        } else { // experimento com dados reais
        	log.loadBuffersStandardExperiment();
            RealExperiment re = new RealExperiment();
            re.run_real_experiment();
            
            log.flushFilesRealExperiment();
        }

    }
}
