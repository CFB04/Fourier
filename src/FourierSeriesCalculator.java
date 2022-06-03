
import java.text.DecimalFormat;
import java.util.Arrays;

public class FourierSeriesCalculator {
    static FourierSettings fS;

    public static void main(String[] args) {
//        WaveGen.gen(getArrayFromFile("D1.txt"), 50000f, 10D, "DOrig");

        fS = new FourierSettings(10.0, 40, 0.20, 0.00002, 40, 2.0f, 44100f);
        calcFourierSeries("D1.txt", "DBase", fS, 292.2579);
    }

    public static void calcFourierSeries(String fileIn, String fileOutName, FourierSettings fourierSettings, double bestGuessBaseFreq)
    {
        // Array of recorded values
        double[] aWave = Util.getArrayFromFile(fileIn);

        // [freq, amp, phase] for each harmonic
        double[][] seriesParams = new double[fourierSettings.N_HARMONICS][3];

        double freq = bestGuessBaseFreq;

        // Find the base frequency that minimizes squared error over the whole time domain with search
        freq = fourierErrorSearch(fourierSettings.INIT_RAD, freq, aWave, 49, 10, false, fourierSettings);

        // Calculate fourier transform at harmonics of base frequency
        for (int n = 1; n <= fourierSettings.N_HARMONICS; n++) {
            double[] temp = fourier(freq * n, aWave, fourierSettings);
            seriesParams[n-1] = temp;
        }

        // Save values in separate arrays for easy display
        double[] freqs = new double[fourierSettings.N_HARMONICS];
        double[] amps = new double[fourierSettings.N_HARMONICS];
        double[] phases = new double[fourierSettings.N_HARMONICS];

        for (int i = 0; i < fourierSettings.N_HARMONICS; i++) {
            freqs[i] = seriesParams[i][0];
            amps[i] = seriesParams[i][1];
            phases[i] = seriesParams[i][2];
        }

        System.out.println("Frequency of Minimum ERROR: " + freq);

        // Print values of the different harmonics
        DecimalFormat df = new DecimalFormat("0.000000000");
        Arrays.stream(freqs).forEach(e -> System.out.print(df.format(e) + ", " ));
        System.out.println();
        Arrays.stream(amps).forEach(e -> System.out.print(df.format(e) + ", " ));
        System.out.println();
        Arrays.stream(phases).forEach(e -> System.out.print(df.format(e) + ", " ));
        System.out.println();

        WaveGen.gen(freqs, amps, phases, fourierSettings.SAMPLE_RATE, fourierSettings.OUT_TIME, fileOutName);
    }

    public static double[] fourier(double f, double[] aWave, FourierSettings fS)
    {
        double tMag;
        double tPhase;

        double sumA = 0;
        double sumB = 0;

        double C = 2.0 * Math.PI * f;

        for (int i = 0; i < aWave.length; i++){
            sumA += aWave[i] * Math.cos(C*i*fS.DELTA_TIME);
            sumB += aWave[i] * Math.sin(C*i*fS.DELTA_TIME);
        }

        double m = 2 * fS.DELTA_TIME / fS.TOTAL_TIME;
        sumA *= m;
        sumB *= m;

        tMag = Util.mag(sumA, sumB);
        tPhase = Math.atan(sumB/sumA);

        return new double[]{f, tMag, tPhase}; // Frequency, Magnitude, Phase
    }

    public static double fourierErrorSearch(double radius, double guessFreq, double[] aWave, int nSamplesPerIteration, int iterations, boolean squareError, FourierSettings fS)
    {
        double[][] seriesParams = new double[fS.N_HARMONICS_ERR][3];

        return Util.evenSampledMinimumSearch(guessFreq, radius, nSamplesPerIteration, iterations, (x) ->
        {
            double f = x[0];
            for (int n = 1; n <= fS.N_HARMONICS_ERR; n++) {
                double[] temp = fourier(f * n, aWave, fS);
                seriesParams[n-1] = temp;
            }

            double tErr = 0.0;

            for (int i = 0; i < aWave.length; i++) {
                double w = 0.0;

                for(int n = 0; n < fS.N_HARMONICS_ERR; n++) w += seriesParams[n][1] * Math.cos(Math.PI * 2 * seriesParams[n][0] * (i * fS.DELTA_TIME) - seriesParams[n][2]);

                double err = w - aWave[i];

                // Absolute error is probably better because it conforms to outliers less and doesn't raise complexity as this is a simple search
                if(squareError) err *= err;
                else err = Math.abs(err);

                tErr += err;
            }

            return tErr;
        });
    }

    public static class FourierSettings
    {
        // Input settings
        final double INIT_RAD; // Initial freq radius for LS error search
        final int N_HARMONICS_ERR; // Number of harmonics to use to calculate error
        final double TOTAL_TIME; // Time of experiment
        final double DELTA_TIME; // Time between each sample

        // Output settings
        final int N_HARMONICS; // Number of harmonics to calculate
        final float OUT_TIME; // Total time of output file
        final float SAMPLE_RATE; // Sample rate of output file

        public FourierSettings(double initRad, int nHarmonicsErr, double totalTime, double deltaTime, int nHarmonics, float outTime, float sampleRate) {
            this.INIT_RAD = initRad;
            this.N_HARMONICS_ERR = nHarmonicsErr;
            this.TOTAL_TIME = totalTime;
            this.DELTA_TIME = deltaTime;
            this.N_HARMONICS = nHarmonics;
            this.OUT_TIME = outTime;
            this.SAMPLE_RATE = sampleRate;
        }
    }
}
