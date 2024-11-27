package ru.overwrite.protect.bukkit.utils.color;

import ru.overwrite.protect.bukkit.utils.Utils;

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

    private boolean isValidHexCode(char[] chars, int start, int length) {
        for (int i = start; i < start + length; i++) {
            char tmp = chars[i];
            if (!((tmp >= '0' && tmp <= '9') || (tmp >= 'a' && tmp <= 'f') || (tmp >= 'A' && tmp <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidColorCharacter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || c == 'r' || (c >= 'k' && c <= 'o') ||
                (c >= 'A' && c <= 'F') || c == 'R' || (c >= 'K' && c <= 'O');
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
