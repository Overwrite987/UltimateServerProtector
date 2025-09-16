package ru.overwrite.protect.bukkit.utils.color.impl;

import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.color.Colorizer;

public class LegacyAdvancedColorizer implements Colorizer {

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        final StringBuilder builder = new StringBuilder();
        final char[] messageChars = message.toCharArray();
        boolean isColor = false, isHashtag = false, isDoubleTag = false;

        for (int index = 0; index < messageChars.length; ) {
            final char currentChar = messageChars[index];

            if (isDoubleTag) {
                isDoubleTag = false;
                if (processDoubleTag(builder, messageChars, index)) {
                    index += 3;
                    continue;
                }
                builder.append("&##");
            } else if (isHashtag) {
                isHashtag = false;
                if (currentChar == '#') {
                    isDoubleTag = true;
                    index++;
                    continue;
                }
                if (processSingleTag(builder, messageChars, index)) {
                    index += 6;
                    continue;
                }
                builder.append("&#");
            } else if (isColor) {
                isColor = false;
                if (currentChar == '#') {
                    isHashtag = true;
                    index++;
                    continue;
                }
                if (isValidColorCharacter(currentChar)) {
                    builder.append(Utils.COLOR_CHAR).append(currentChar);
                    index++;
                    continue;
                }
                builder.append('&');
            } else if (currentChar == '&') {
                isColor = true;
                index++;
            } else {
                builder.append(currentChar);
                index++;
            }
        }

        appendRemainingColorTags(builder, isColor, isHashtag, isDoubleTag);
        return builder.toString();
    }

    private boolean processDoubleTag(StringBuilder builder, char[] messageChars, int index) {
        if (index + 3 <= messageChars.length && isValidHexCode(messageChars, index, 3)) {
            builder.append(Utils.COLOR_CHAR).append('x');
            for (int i = index; i < index + 3; i++) {
                builder.append(Utils.COLOR_CHAR).append(messageChars[i]).append(Utils.COLOR_CHAR).append(messageChars[i]);
            }
            return true;
        }
        return false;
    }

    private boolean processSingleTag(StringBuilder builder, char[] messageChars, int index) {
        if (index + 6 <= messageChars.length && isValidHexCode(messageChars, index, 6)) {
            builder.append(Utils.COLOR_CHAR).append('x');
            for (int i = index; i < index + 6; i++) {
                builder.append(Utils.COLOR_CHAR).append(messageChars[i]);
            }
            return true;
        }
        return false;
    }

    private static final boolean[] HEX_CHARS = new boolean[128];

    static {
        for (char c = '0'; c <= '9'; c++) {
            HEX_CHARS[c] = true;
        }
        for (char c = 'a'; c <= 'f'; c++) {
            HEX_CHARS[c] = true;
        }
        for (char c = 'A'; c <= 'F'; c++) {
            HEX_CHARS[c] = true;
        }
    }

    public boolean isValidHexCode(char[] chars, int start, int length) {
        int end = start + length;
        for (int i = start; i < end; i++) {
            char c = chars[i];
            if (!HEX_CHARS[c]) {
                return false;
            }
        }
        return true;
    }

    private static final boolean[] COLOR_CHARS_FLAGS = new boolean[128];

    static {
        for (char c = '0'; c <= '9'; c++) {
            COLOR_CHARS_FLAGS[c] = true;
        }

        for (char c = 'a'; c <= 'f'; c++) {
            COLOR_CHARS_FLAGS[c] = true;
        }

        for (char c = 'A'; c <= 'F'; c++) {
            COLOR_CHARS_FLAGS[c] = true;
        }

        int[] specialSymbols = {
                'r', 'R',
                'k', 'l', 'm', 'n', 'o',
                'K', 'L', 'M', 'N', 'O'
        };

        for (int sym : specialSymbols) {
            COLOR_CHARS_FLAGS[sym] = true;
        }
    }

    private boolean isValidColorCharacter(char c) {
        return COLOR_CHARS_FLAGS[c];
    }

    private void appendRemainingColorTags(StringBuilder builder, boolean isColor, boolean isHashtag, boolean isDoubleTag) {
        if (isColor) {
            builder.append('&');
        } else if (isHashtag) {
            builder.append("&#");
        } else if (isDoubleTag) {
            builder.append("&##");
        }
    }
}
