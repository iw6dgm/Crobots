package it.joshua.crobots.impl;

import it.joshua.crobots.data.CONST;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import org.apache.log4j.Logger;

public class Manager {

    private static Logger logger = Logger.getLogger(Manager.class);
    private MessageDigest md = null;
    private Runtime shellCrobots = null;
    private final int bufferSize;

    public int getBufferSize() {
        return bufferSize;
    }

    private Manager(Builder builder) {
        try {
            md = MessageDigest.getInstance(CONST.DIGEST_ALGO);
            shellCrobots = Runtime.getRuntime();
            bufferSize = builder.getBufferSize();
        } catch (Exception e1) {
            logger.error("", e1);
            throw new RuntimeException(e1);
        }
    }
    
    public static class Builder implements it.joshua.crobots.Builder<Manager> {
        
        private final int bufferSize;
        
        public Builder(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        @Override
        public Manager build() {
            return new Manager(this);
        }

        public int getBufferSize() {
            return bufferSize;
        }
        
    }

    public String encrypt(String data) {

        String cs = CONST.SERVER_KEY + data;
        StringBuffer sb = new StringBuffer();
        try {
            md.update(cs.getBytes("UTF-8"));
            //String checksum = new String(temp);
            byte[] bytes = md.digest();

            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xff & bytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            logger.error("", e1);
        }


        return sb.toString();

    }

    /**
     *
     * Esegue un processo esterno e ritorna l'output del processo.
     *
     * @param cmdLine nome eseguibile completo di path e parametri.
     * @return ERROR se ci sono stati problemi altrimenti l'output del comando
     *
     */
    public String[] cmdExec(String cmdline) {
        try {

            if ((cmdline != null) && (cmdline.length() > 0)) {

                logger.debug(cmdline);

                Process p = shellCrobots.exec(cmdline);
                p.waitFor();
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String[] output = new String[bufferSize];
                int count = 0;
                output[count++] = input.readLine();

                while (input.ready()) {
                    output[count++] = input.readLine();
                }
                input.close();
                p.destroy();
                return output;
            }

            return null;
        } catch (IOException | InterruptedException err) {
            logger.error("", err);
            return null;
        }
    }
}
