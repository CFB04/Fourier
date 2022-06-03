import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class Util {
    public static void closeCloseable(Closeable c)
    {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public double[] normalizeArray(double[] a)
    {
        double[] out = a;

        double max = a[0];
        for (double value : a) {
            double v = Math.abs(value);
            if (v > max) max = v;
        }

        for (double value : out) {
            value /= max;
        }

        return out;
    }

    public static double mag(double x1, double x2)
    {
        return Math.sqrt(x1*x1+x2*x2);
    }

    public int[] byteArrayToIntArray(byte[] bytes)
    {
        return ByteBuffer.allocate(bytes.length).put(bytes).asIntBuffer().array();
    }

    public void convertFileType(File fileIn, AudioFileFormat.Type targetFormat, String fileOutName)
    {
        File fileOut = new File("soundFiles/output/" + fileOutName + "." + targetFormat.getExtension());
        AudioInputStream aISIn = null;

        try{
            AudioFileFormat inFormat = AudioSystem.getAudioFileFormat(fileIn);

            if(inFormat.getType().equals(targetFormat)) {
                System.out.println("File already target type");
                return;
            }

            aISIn = AudioSystem.getAudioInputStream(fileIn);
            aISIn.reset();

            if(AudioSystem.isFileTypeSupported(targetFormat, aISIn))
            {
                AudioSystem.write(aISIn, targetFormat, fileOut);
                System.out.println("Conversion successful");
            } else System.out.println("Conversion not supported");

        } catch (IOException | UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        } finally {
            closeCloseable(aISIn);
        }
    }

    public static double[] getArrayFromFile(String fileName)
    {
        double[] out;
        String line = "";

        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader("soundFiles/input/" + fileName));
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

    public byte[] getBytesFromWav(String fileName)
    {
        AudioInputStream aIS = null;
        AudioFileFormat aFF = null;
        byte[] bytes = {};
        ByteBuffer bb;

        try {
            aIS = AudioSystem.getAudioInputStream(new File("soundFiles/input" + fileName));
            aFF = AudioSystem.getAudioFileFormat(aIS);
            bytes = aIS.readAllBytes();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeCloseable(aIS);
        }

        return bytes;
    }
}
