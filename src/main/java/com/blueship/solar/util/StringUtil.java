package com.blueship.solar.util;

import org.bukkit.map.MinecraftFont;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public class StringUtil {
    /**
     * Modifies a string to the desired width.
     * Behaves like left-justified text.
     * Fills the left side with spaces.
     *
     * @param word the word to adjust
     * @param width the desired width of the string
     * @return the left-justified string
     */
    public static @NotNull String leftJustify(@NotNull String word, int width) {
       return String.format("%" + width + "s", trimStringIfTooLong(word, width, 4));
    }
    /**
     * Modifies a string to the desired width.
     * Behaves like right-justified text.
     * Fills the left side with spaces.
     *
     * @param word the word to adjust
     * @param width the desired width of the string
     * @return the right-justified string
     */
    public static @NotNull String rightJustify(@NotNull String word, int width) {
        return String.format("%-" + width + "s", trimStringIfTooLong(word, width, 4));
    }

    /**
     * Modifies a string to the desired width.
     * Behaves like center-justified text.
     * Fills the sides with spaces.
     *
     * @param word the word to adjust
     * @param width the desired width of the string
     * @return the center-justified string
     */
    public static @NotNull String centerJustify(@NotNull String word, int width) {
        return centerJustify(word, width, " ");
    }

    /**
     * Modifies a string to the desired width.
     * Behaves like center-justified text.
     * Fills in the sides with the <code>replacement</code>.
     *
     * @param word the word to adjust
     * @param width the desired width of the string
     * @param replacement the string to replace extra space with
     * @return the center-justified string
     */
    public static @NotNull String centerJustify(@NotNull String word, int width, @NotNull String replacement) {
        final int replacementWidth;
        if (replacement.length() == 1) {
            replacementWidth = MinecraftFont.Font.getWidth(replacement) + 1;
        } else {
            replacementWidth = MinecraftFont.Font.getWidth(replacement);
        }
        word = trimStringIfTooLong(word, width, replacementWidth);
        int length = MinecraftFont.Font.getWidth(word);
        if (length == width) {
            return word;
        }
        int spacesToAdd = (width - length);

        ArrayDeque<String> productQueue = new ArrayDeque<>();
        productQueue.addFirst(word);
        while (spacesToAdd >= replacementWidth) {
            productQueue.addFirst(replacement);
            spacesToAdd -= replacementWidth;
            if (spacesToAdd >= replacementWidth) {
                productQueue.addLast(replacement);
                spacesToAdd -= replacementWidth;
            }
        }
        return String.join("", productQueue);
    }

    // ||     world      ||
    // || world_the_end  ||
    // ||  world_nether  ||

    private static final @NotNull String TRIM_REPLACEMENT = ".";
    private static final int TRIM_REPLACEMENT_LENGTH = 2;
    private static final int MAX_TRIM_LENGTH = 3;


    public static @NotNull String trimStringIfTooLong(@NotNull String word, int maxWidth, int replaceWidth) {
        int length = MinecraftFont.Font.getWidth(word);
        if (length > maxWidth || (maxWidth - length) % replaceWidth % TRIM_REPLACEMENT_LENGTH == 0) {
            int desiredTrimLength = Math.abs(maxWidth - length) % replaceWidth / TRIM_REPLACEMENT_LENGTH;
            int index = 0;
            while (index < word.length()) {
                String str = word.substring(0, index + 1);
                int strWidth = MinecraftFont.Font.getWidth(str);
                if (strWidth > maxWidth - (desiredTrimLength * TRIM_REPLACEMENT_LENGTH)) {
                    break;
                }
                ++index;
            }
            desiredTrimLength = Math.min(3, (maxWidth - MinecraftFont.Font.getWidth(word.substring(0, index))) % replaceWidth / 2);

            return word.substring(0, index) + TRIM_REPLACEMENT.repeat(desiredTrimLength);
        }
        return word;
    }
}
