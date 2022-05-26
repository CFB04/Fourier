import java.util.Arrays;

public class WaveGen {
    public static void main(String[] args) {
        double[] f = {350.0, 700.0, 1050.0, 1400.0, 1750.0};
        double[] a = {1.0, 0.5, 0.7, 0.3, 0.5};
        double[] p = {0, 0.8, -0.7, 1.5, -0.4};

        double[] l = new double[10001];

        for (int i = 0; i < 10001; i++) {
            l[i] = f(f,a,p,0.2 * i / 10000.0);
        }

        //System.out.println(Arrays.toString(l));
    }

    public static double f(double[] f, double[] a, double[] p, double x)
    {
        double o = 0.0;
        for (int i = 0; i < 5; i++) {
            o += a[i]*Math.cos(Math.PI*2*f[i]*x-p[i]);
        }
        return o;
    }
}
