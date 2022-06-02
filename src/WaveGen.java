import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;

public class WaveGen {
    public static void gen(double[] f, double[] a, double[] p, float sampleRate, float t, String filename)
    {
        int samples = (int) (t * sampleRate);

        ByteBuffer bb = ByteBuffer.allocate(samples * Float.BYTES);

        double max = f(f,a,p,0);
        for (int i = 0; i < samples; i++) {
            double v = Math.abs(f(f,a,p,i / sampleRate));
            if(v > max) max = v;
        }

        for (int i = 0; i < samples; i++) {
            float v = (float) (f(f,a,p,i / sampleRate) / max);
            v *= Integer.MAX_VALUE;
            bb.putInt((int) v);
        }

        ByteArrayInputStream iS = new ByteArrayInputStream(bb.array());
        AudioFormat aF = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, Integer.SIZE, 1, Integer.BYTES, sampleRate, true);
        AudioInputStream ais = new AudioInputStream(iS, aF, samples);

        File fileOut = new File(filename + ".wav");
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeInputStream(iS);
            closeInputStream(ais);
        }
    }

    public static void closeInputStream(Closeable c)
    {
        if(c != null)
        {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static double f(double[] f, double[] a, double[] p, double x)
    {
        double o = 0.0;
        for (int i = 0; i < Math.min(Math.min(f.length, a.length), p.length); i++) {
            o += a[i]*Math.cos(Math.PI*2*f[i]*x-p[i]);
        }
        return o;
    }
}
