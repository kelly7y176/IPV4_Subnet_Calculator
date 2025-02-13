package com.example.subnetcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText ipAddressInput, subnetMaskInput;
    TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddressInput = findViewById(R.id.ipAddressInput);
        subnetMaskInput = findViewById(R.id.subnetMaskInput);
        resultText = findViewById(R.id.resultText);
        Button calculateButton = findViewById(R.id.calculateButton);
        Button aboutButton = findViewById(R.id.aboutButton);
        Button helpButton = findViewById(R.id.helpButton);

        // Set OnClickListener using lambda expression
        calculateButton.setOnClickListener(v -> calculateSubnet());
        aboutButton.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        helpButton.setOnClickListener(v -> startActivity(new Intent(this, HelpActivity.class)));
    }

    private void calculateSubnet() {
        String ip = ipAddressInput.getText().toString();
        String mask = subnetMaskInput.getText().toString();

        // Validate inputs
        if (!isValidIP(ip) || !isValidSubnetMask(mask)) {
            resultText.setText("Invalid IP Address or Subnet Mask");
            return;
        }

        // Calculate subnet ranges and class
        String results = performSubnetCalculation(ip, mask);

        // Display the results
        resultText.setText(results);
    }

    private String performSubnetCalculation(String ip, String mask) {
        byte[] ipBytes = ipToBytes(ip);
        byte[] maskBytes = ipToBytes(mask);

        // Calculate Network Address
        byte[] networkBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            networkBytes[i] = (byte) (ipBytes[i] & maskBytes[i]);
        }

        // Calculate Broadcast Address
        byte[] broadcastBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            broadcastBytes[i] = (byte) (networkBytes[i] | ~maskBytes[i]);
        }

        // Calculate Usable IP Range
        byte[] firstUsable = networkBytes.clone();
        byte[] lastUsable = broadcastBytes.clone();
        firstUsable[3]++; // First usable IP
        lastUsable[3]--;   // Last usable IP

        String network = bytesToIp(networkBytes);
        String broadcast = bytesToIp(broadcastBytes);
        String firstUsableIp = bytesToIp(firstUsable);
        String lastUsableIp = bytesToIp(lastUsable);

        // Determine Class of the IP Address
        String ipClass = getClassOfIP(ip);

        return String.format("Class: %s\nNetwork: %s\nBroadcast: %s\nUsable Range: %s - %s",
                ipClass, network, broadcast, firstUsableIp, lastUsableIp);
    }

    private byte[] ipToBytes(String ip) {
        String[] octets = ip.split("\\.");
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) Integer.parseInt(octets[i]);
        }
        return bytes;
    }

    private String bytesToIp(byte[] bytes) {
        return (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "." + (bytes[2] & 0xFF) + "." + (bytes[3] & 0xFF);
    }

    private String getClassOfIP(String ip) {
        String[] octets = ip.split("\\.");
        int firstOctet = Integer.parseInt(octets[0]);
        if (firstOctet >= 1 && firstOctet <= 126) return "A";
        if (firstOctet >= 128 && firstOctet <= 191) return "B";
        if (firstOctet >= 192 && firstOctet <= 223) return "C";
        return "D/E (Not Standard)";
    }

    private boolean isValidIP(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) return false;

        for (String octet : octets) {
            try {
                int num = Integer.parseInt(octet);
                if (num < 0 || num > 255) return false;  // Check range
            } catch (NumberFormatException e) {
                return false;  // Not a number
            }
        }
        return true;  // Valid IP address
    }

    private boolean isValidSubnetMask(String mask) {
        String[] octets = mask.split("\\.");
        if (octets.length != 4) return false;

        int[] validMasks = {0, 128, 192, 224, 240, 248, 252, 255};  // Valid mask octets
        for (String octet : octets) {
            try {
                int num = Integer.parseInt(octet);
                if (num < 0 || num > 255 || !isMaskValid(num, validMasks)) return false;
            } catch (NumberFormatException e) {
                return false;  // Not a number
            }
        }
        return true;  // Valid subnet mask
    }

    private boolean isMaskValid(int num, int[] validMasks) {
        for (int mask : validMasks) {
            if (num == mask) return true;
        }
        return false;
    }
}