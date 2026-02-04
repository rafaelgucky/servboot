package net.servboot.utils.io;

import java.util.Random;

public class NameGenerator {
    public static String generateName(String extension) {
        Random random = new Random();
        String name = "";

        for(int i = 0; i < 20; i++){
            name += (char) random.nextInt(65, 90);
        }

        return name + "." + extension;
    }
}
