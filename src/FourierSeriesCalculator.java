import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

public class FourierSeriesCalculator {
    static FourierSettings fS;

    public static void main(String[] args) {
        fS = new FourierSettings(4.0, 100, 2.0, 0.00002, 100, 2.0f, 44100f);
        calcFourierSeries("D1.txt", "DBase", fS, 292.2579);
    }

    public static double mag(double x1, double x2)
    {
        return Math.sqrt(x1*x1+x2*x2);
    }

    public static void calcFourierSeries(String fileIn, String fileOutName, FourierSettings fourierSettings, double bestGuessBaseFreq)
    {
//        // File settings
//        final String INPUT_FILE; // Full name of input file (including filetype extension)
//        final String OUTPUT_FILE; // Name of output file (excluding filetype extension)

        // Array of recorded values
        double[] aWave = getArrayFromFile(fileIn);
        //double dT = recordingTime/((double) aWave.length - 1); // - 1 Because if recording is for example 2 seconds long with 10 samples/s, there would be 21 samples if there's a sample at the extremes of 0s and 2s (which there was for this experiment)

        // [freq, amp, phase] for each harmonic
        double[][] seriesParams = new double[fourierSettings.N_HARMONICS][3];

        double freq = bestGuessBaseFreq;

        // Find the base frequency that minimizes squared error over the whole time domain with search
        int nPrecision = 10; // // Freq precision is INIT_R/25^nPrecision
        for (int i = 1; i <= nPrecision; i++) freq = fourierLSSearch(fourierSettings.INIT_RAD * Math.pow(0.04, i - 1), freq, aWave, fourierSettings);

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

        System.out.println("Freq of LS ERROR: " + freq);

        // Print values of the different harmonics
        DecimalFormat df = new DecimalFormat("0.000000");
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
            sumA += aWave[i] * Math.cos(C*i*fS.DELTA_TIME); // TODO Used to be SPS not dT, if broken change back
            sumB += aWave[i] * Math.sin(C*i*fS.DELTA_TIME);
        }

        double m = 2 * fS.DELTA_TIME / fS.TOTAL_TIME;
        sumA *= m;
        sumB *= m;

        tMag = mag(sumA, sumB);
        tPhase = Math.atan(sumB/sumA);

        return new double[]{f, tMag, tPhase}; // Frequency, Magnitude, Phase
    }

    public static double fourierLSSearch(double radius, double guessFreq, double[] aWave, FourierSettings fS)
    {
        double[][] seriesParams = new double[fS.N_HARMONICS_LS][3];

        double[] sums = new double[49];
        for (int i = 0; i < 49; i++) {
            double f = guessFreq + (radius * (i - 24) / 25.0);

            for (int n = 1; n <= fS.N_HARMONICS_LS; n++) {
                double[] temp = fourier(f * n, aWave, fS);
                seriesParams[n-1] = temp;
            }

            for (int j = 0; j < aWave.length; j++) {
                double o = 0.0;
                for (int n = 0; n < fS.N_HARMONICS_LS; n++) {
                    o += seriesParams[n][1] * Math.cos(Math.PI * 2 * seriesParams[n][0] * (j * fS.DELTA_TIME) - seriesParams[n][2]);
                }

                double err = o - aWave[j];
                sums[i] += err * err;
            }
        }

        int min = 0;
        for (int i = 0; i < 49; i++) if(sums[i] < sums[min]) min = i;

        return guessFreq + (radius * (min - 24) / 25);
    }

    public static double[] getArrayFromFile(String fileName)
    {
        double[] out;
        String line = "";

        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(fileName));
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally
        {
            String[] values = line.split(",");
            out = Arrays.stream(values)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            try {
                if(br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return out;
    }

    public static class FourierSettings
    {
        // Input settings
        final double INIT_RAD; // Initial freq radius for LS error search
        final int N_HARMONICS_LS; // Number of harmonics to use to calculate LS error
        final double TOTAL_TIME; // Time of experiment
        final double DELTA_TIME; // Time between each sample

        // Output settings
        final int N_HARMONICS; // Number of harmonics to calculate
        final float OUT_TIME; // Total time of output file
        final float SAMPLE_RATE; // Sample rate of output file

        public FourierSettings(double initRad, int nHarmonicsLS, double totalTime, double deltaTime, int nHarmonics, float outTime, float sampleRate) {
            this.INIT_RAD = initRad;
            this.N_HARMONICS_LS = nHarmonicsLS;
            this.TOTAL_TIME = totalTime;
            this.DELTA_TIME = deltaTime;
            this.N_HARMONICS = nHarmonics;
            this.OUT_TIME = outTime;
            this.SAMPLE_RATE = sampleRate;
        }
    }
}
