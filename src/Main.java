import java.io.IOException;

/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Main implements ControlExperiment {

    public static void main(String[] args) throws IOException {


        if (MODE_EXECUTION == 1) { // experimento padrão
            StandardExperiment se = new StandardExperiment();
            se.run_standard_experiment();  // sem uma instância como parâmetro é carregada a instância modelo defida em control.cpp

        } else { // experimento com dados reais
            RealExperiment re = new RealExperiment();
            re.run_real_experiment();
            LogExperiment log = LogExperiment.getInstance();
            log.flushFiles();
        }

    }
}
