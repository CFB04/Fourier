import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

public class FourierSeriesCalculator {

    static double[] a;

    static final double BEST_GUESS_BASE_FREQ = 292.2579;
    static final double INIT_R = 4.0;

    static final double T = 0.2; // Total time (s), This is the time of the last sample minus time 0. The full time domain for this experiment was actually 0.2001999...
    static double SPS; // Seconds per sample
    static double dT; // ∆t
    static final int N_MAX = 40; // Highest Harmonic

    public static void main(String[] args) {

        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader("D1.txt"));
            String line = br.readLine();
            String[] values = line.split(",");
            a = Arrays.stream(values)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally
        {
            try {
                if(br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SPS = T/((double) a.length);
        dT = T/((double) a.length - 1);

        // [freq, amp, phase] for each harmonic
        double[][] seriesParams = new double[N_MAX][3];

        double freq = BEST_GUESS_BASE_FREQ;

        // Find the base frequency that minimizes squared error over the whole time domain with search
        int nPrecision = 10; // // Freq precision is initR/25^nPrecision
        for (int i = 1; i <= nPrecision; i++) freq = fourierLSSearch(INIT_R * Math.pow(0.04, i - 1), freq);

        // Calculate fourier transform at harmonics of base frequency
        for (int n = 1; n <= N_MAX; n++) {
            double[] temp = fourier(freq * n, n);
            seriesParams[n-1] = temp;
        }

        // Save values in separate arrays for easy display
        double[] freqs = new double[N_MAX];
        double[] amps = new double[N_MAX];
        double[] phases = new double[N_MAX];

        for (int i = 0; i < N_MAX; i++) {
            freqs[i] = seriesParams[i][0];
            amps[i] = seriesParams[i][1];
            phases[i] = seriesParams[i][2];
        }

        // Print values of the different harmonics
        DecimalFormat df = new DecimalFormat("0.00000000");
        Arrays.stream(freqs).forEach(e -> System.out.print(df.format(e) + ", " ));
        System.out.println();
        Arrays.stream(amps).forEach(e -> System.out.print(df.format(e) + ", " ));
        System.out.println();
        Arrays.stream(phases).forEach(e -> System.out.print(df.format(e) + ", " ));
        System.out.println();
    }

    public static double mag(double x1, double x2)
    {
        return Math.sqrt(x1*x1+x2*x2);
    }

    public static double[] fourier(double f, int harmonic)
    {
        double tMag;
        double tPhase;

        double sumA = 0;
        double sumB = 0;

        double C = 2.0 * Math.PI * f;

        for (int i = 0; i < a.length; i++){
            sumA += a[i] * Math.cos(C*i*SPS);
            sumB += a[i] * Math.sin(C*i*SPS);
        }

        sumA *= 2 * dT / T;
        sumB *= 2 * dT / T;

        tMag = mag(sumA, sumB);
        tPhase = Math.atan(sumB/sumA);

        return new double[]{f, tMag, tPhase}; // Frequency, Magnitude, Phase
    }

    public static double fourierLSSearch(double radius, double gf)
    {
        double[][] seriesParams = new double[N_MAX][3];

        double[] sums = new double[49];
        for (int i = 0; i < 49; i++) {
            double f = gf + (radius * (i - 24) / 25.0);
            for (int n = 1; n <= N_MAX; n++) {
                double[] temp = fourier(f * n, n);
                seriesParams[n-1] = temp;
            }

            for (int j = 0; j < 10001; j++) {
                double o = 0.0;
                for (int n = 0; n < 5; n++) o += seriesParams[n][1]*Math.cos(Math.PI*2*seriesParams[n][0]*(0.2 * j / 10000.0)-seriesParams[n][2]);

                double err = o - a[j];
                sums[i] += err * err;
            }
        }

        int min = 0;
        for (int i = 0; i < 49; i++) if(sums[i] < sums[min]) min = i;

        return gf + (radius * (min - 24) / 25);
    }
}
