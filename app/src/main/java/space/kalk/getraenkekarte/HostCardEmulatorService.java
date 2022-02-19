package space.kalk.getraenkekarte;

import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


// NFC HCE: https://developer.android.com/guide/topics/connectivity/nfc/hce
public class HostCardEmulatorService extends HostApduService {
    // ff (proprietary AID group) + KalkGetränk
    private static final String AID = "ff4b616c6b47657472c3a46e6b";
    private static final String TAG = "KalkGetraenk";

    private static final byte[] CMD_OK = hexStringToByteArray("9000");
    private static final int CMD_PREFIX_LEN = 5;
    // "00a404000d"
    private static final byte[] APP_HANDSHAKE_PREFIX = new byte[] {
            0x00, // CLA (see 5.4.1)
            (byte)0xa4, // INS
            0x04, 0x00 // P1-P1
    };

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

        // Spec: https://cardwerk.com/smart-card-standard-iso7816-4-section-9-application-independent-card-services/

        // WARN: a byte is 2 string characters!
        String prefix = commandString.substring(0, CMD_PREFIX_LEN*2);
        switch (prefix) {
            case "00a404000d": {
                String aid = commandString.substring(CMD_PREFIX_LEN*2, AID.length());
                if (aid.equals(AID)) {
                    return CMD_OK;
                }
            }
            case "d000000024": {

            }
        }
        // this logic is of course super naive but does the job for now?
        if (commandString.equals("00a404000d" + byteArrayToHex(AID) + "00")) {
            return CMD_OK;
        } else if (commandString.equals("d000000024")) {
            // this command prefix is KalkSpace-specific
            // see https://github.com/kalkspace/getraenkekassengeraete/blob/39248dc3a0c6a3ae1b285e8a3f964cfe81775460/src/nfcservice.rs#L37
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
