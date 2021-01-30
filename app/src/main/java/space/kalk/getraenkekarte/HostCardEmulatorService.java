package space.kalk.getraenkekarte;

import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.nio.charset.StandardCharsets;


public class HostCardEmulatorService extends HostApduService {
    // ff (proprietary AID group) + KalkGetränk
    private static final byte[] AID = hexStringToByteArray("ff4b616c6b47657472c3a46e6b");
    private static final String TAG = "KalkGetraenk";

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String commandString = byteArrayToHex(commandApdu);

        Log.d(TAG, "COMMAND: " + commandString);
        // this logic is of course super naive but does the job for now?
        if (commandString.equals("00a404000d" + byteArrayToHex(AID) + "00")) {
            return hexStringToByteArray("9000");
        } else if (commandString.equals("d000000024")) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String uuid = preferences.getString("uuid", "");
            if (uuid.length() != 36) {
                Log.d(TAG, "Internal UUID invalid: " + uuid);
            } else {
                byte[] b = uuid.getBytes(StandardCharsets.US_ASCII);
                return hexStringToByteArray(byteArrayToHex(b) + "9000");
            }
        }
        return hexStringToByteArray("6f00");

    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
    }
}
