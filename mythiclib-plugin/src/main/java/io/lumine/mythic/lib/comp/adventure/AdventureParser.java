package io.lumine.mythic.lib.comp.adventure;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgument;
import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.argument.EmptyArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.resolver.ContextTagResolver;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;
import io.lumine.mythic.lib.comp.adventure.tag.implementation.*;
import io.lumine.mythic.lib.comp.adventure.tag.implementation.decorations.*;
import io.lumine.mythic.lib.util.AdventureUtils;
import io.lumine.mythic.lib.util.Pair;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class AdventureParser {

    /* Constants */
    private static final Pattern TAG_REGEX = Pattern.compile("(?i)(?<=<).*?(?=>)");
    private static final Pattern HEX_REGEX = Pattern.compile("(?i)(#|HEX)[0-9a-f]{6}");

    private final List<AdventureTag> tags = new ArrayList<>();

    @TestOnly
    public AdventureParser(boolean testing) {
    }

    public AdventureParser() {
        // Context
        add(new GradientTag());
        add(new RainbowTag());
        add(new TransitionTag());

        // Normal
        add(new VanillaColorTag());
        add(new HexColorTag());
        add(new AdventureColorTag());
        add(new NewlineTag());
        add(new BoldTag());
        add(new ItalicTag());
        add(new ObfuscatedTag());
        add(new ResetTag());
        add(new StrikethroughTag());
        add(new UnderlineTag());
    }

    /**
     * Parse a string synchronously.
     * Note that if a tag is not found or invalid, it will be replaced by the fallback resolver.
     * Also note that this method will not parse
     * the <newline> tag, you will have to use {@link #parse(Collection)} or {@link #parseAsync(Collection)}.
     *
     * @param src The string to parse.
     * @return The parsed string.
     */
    public @NotNull String parse(@NotNull final String src) {
        String cpy = src;
        Matcher matcher = TAG_REGEX.matcher(cpy);
        while (matcher.find()) {
            final String tag = matcher.group();
            final String tagName = tag.contains(":") ? tag.split(":")[0] : tag;
            final String finalCpy = cpy;
            if (tagName.isEmpty() || tagName.startsWith("/"))
                continue;

            cpy = findByName(tagName)
                    .map(adventureTag -> parseTag(finalCpy, adventureTag, tagName, tag))
                    .orElseGet(() -> {
                        Matcher matcher1 = HEX_REGEX.matcher(tag);
                        if (!matcher1.find())
                            return finalCpy;
                        // Fall back
                        // return finalCpy.replace("<" + tag + ">", fallBackResolver.apply(tag));

                        String prefix = matcher1.group(1);
                        return findByName(prefix)
                                .map(adventureTag -> parseTag(finalCpy, adventureTag, prefix, tag))
                                .orElse(finalCpy);
                        // .orElse(finalCpy.replace("<" + tag + ">", fallBackResolver.apply(tag)));
                    });
        }
        cpy = removeUnparsedAndUselessTags(cpy);
        return minecraftColorization(cpy);
    }

    /**
     * Parse a string asynchronously.
     *
     * @param src The string to parse.
     * @return A {@link CompletableFuture} containing the parsed string.
     */
    public @NotNull CompletableFuture<String> parseAsync(@NotNull final String src) {
        return AdventureUtils.supplyAsync(() -> parse(src));
    }

    /**
     * Parse a collection of strings synchronously.
     *
     * @param src The collection of strings to parse.
     * @return The parsed collection of strings.
     */
    public @NotNull Collection<String> parse(@NotNull final Collection<String> src) {
        List<String> parsed = src.stream()
                .map(this::parse)
                .collect(Collectors.toList());
        List<String> finalList = new ArrayList<>();
        for (final String line : parsed) {
            if (!line.contains("\n")) {
                finalList.add(line);
                continue;
            }
            finalList.addAll(Arrays.asList(line.split("\n")));
        }
        return finalList;
    }

    /**
     * Parse a collection of strings asynchronously.
     *
     * @param src The collection of strings to parse.
     * @return A future containing the parsed collection.
     */
    public @NotNull CompletableFuture<Collection<String>> parseAsync(@NotNull Collection<String> src) {
        return AdventureUtils.supplyAsync(() -> parse(src));
    }

    /**
     * Parse a tag and replace it in the string.
     * Note that if the tag is not found or invalid, it will be replaced by the fallback resolver.
     *
     * @param src           The source string.
     * @param tag           The tag to parse.
     * @param tagIdentifier The tag identifier.
     * @param plainTag      The plain tag.
     * @return The parsed string.
     */
    private @NotNull String parseTag(@NotNull final String src, @NotNull final AdventureTag tag, @NotNull final String tagIdentifier, @NotNull final String plainTag) {
        String cpy = src;
        try {
            int firstArgIndex = plainTag.indexOf(":");
            boolean hasArgs = firstArgIndex != -1;
            boolean isHex = tagIdentifier.equals("#") || tagIdentifier.equalsIgnoreCase("HEX");
            String hexPrefix = tagIdentifier.startsWith("#") ? "#" : "HEX";

            final String rawTag = isHex ? tagIdentifier : plainTag.substring(0, hasArgs ? firstArgIndex : plainTag.length());
            final String rawArgs = isHex ? plainTag.substring(hexPrefix.length()) : (hasArgs ? plainTag.substring(firstArgIndex + 1) : "");
            final String original = String.format("<%s%s>", rawTag, hasArgs ? ':' + rawArgs : rawArgs);

            final AdventureArgumentQueue args = parseArguments(rawArgs);
            boolean hasContext = tag.resolver() instanceof ContextTagResolver;

            String context = hasContext ? getTagContent(cpy, rawTag, original) : null;
            Pair<List<String>, String> contextDecorations = hasContext ? processContextDecorations(context) : null;
            String resolved = hasContext ?
                    ((ContextTagResolver) tag.resolver()).resolve(rawTag, args, contextDecorations.getRight(), contextDecorations.getLeft())
                    : tag.resolver().resolve(rawTag, args);
            cpy = cpy.replace(hasContext ? String.format("%s%s", original, context) : original, resolved != null ? resolved : "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cpy;
    }

    /**
     * Parse the tag context, which is basically everything between the tag and the closing tag.
     * or the end of the string if there is no closing tag.
     *
     * @param src           The source string.
     * @param tagName       The tag name.
     * @param tagIdentifier The tag identifier.
     * @return The tag context.
     */
    private @NotNull String getTagContent(@NotNull String src, @NotNull String tagName, @NotNull String tagIdentifier) {
        // Match closed tags
        final String closeTag = String.format("</%s>", tagName);
        String content = UtilityMethods.substringBetween(src, tagIdentifier, closeTag);
        if (content != null) return content;

        String cpy = src.substring(src.indexOf(tagIdentifier) + tagIdentifier.length());
        int colorIndex = cpy.length();

        // Match colors tags as context end
        Matcher matcher = TAG_REGEX.matcher(cpy);
        int iterations = 0;
        while (matcher.find() && iterations++ < 20) {
            final String rawTag = matcher.group();
            boolean isHex = rawTag.startsWith("#") || rawTag.startsWith("HEX");
            String rawTagName = isHex ? (rawTag.startsWith("#") ? "#" : "HEX") : rawTag.split(":")[0];

            Optional<AdventureTag> optTag = findByName(rawTagName).filter(AdventureTag::color);
            if (optTag.isPresent()) {
                colorIndex = cpy.indexOf(String.format("<%s>", rawTag));
                break;
            }
            matcher = TAG_REGEX.matcher(cpy);
        }
        content = cpy.substring(0, colorIndex);
        return content;
    }

    /**
     * appache common-lang3
     */


    /**
     * Parse tags arguments from a string.
     *
     * @param rawArgs The raw arguments.
     * @return The parsed arguments as {@link AdventureArgumentQueue}.
     */
    private AdventureArgumentQueue parseArguments(@NotNull String rawArgs) {
        String[] unparsedArgs = rawArgs.split(":");
        if (unparsedArgs.length > 0 && unparsedArgs[0].isEmpty())
            unparsedArgs = Arrays.copyOfRange(unparsedArgs, 1, unparsedArgs.length);
        return new AdventureArgumentQueue(
                Arrays.stream(unparsedArgs)
                        .map(AdventureArgument::new)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Remove all unparsed tags and close tags.
     * Note: close tags are replaced with reset character.
     *
     * @param src The source string.
     * @return The parsed string.
     */
    private @NotNull String removeUnparsedAndUselessTags(@NotNull String src) {
        Matcher matcher = TAG_REGEX.matcher(src);
        int iterations = 0;
        while (matcher.find() && iterations++ < 50) {
            final String matched = matcher.group();
            final String original = String.format("<%s>", matched);
            if (matched.startsWith("/"))
                src = src.replace(original, "§r");
            matcher = TAG_REGEX.matcher(src);
        }
        return src;
    }

    /**
     * Minecraft colorization is a feature that allows to use the § character to colorize text.
     *
     * @param src The source string to colorize.
     * @return The colorized string.
     */
    private @NotNull String minecraftColorization(@NotNull final String src) {
        return ChatColor.translateAlternateColorCodes('&', src);
    }

    /**
     * Remove all tags from the string.
     *
     * @param src The source string.
     * @return The string without tags.
     */
    public @NotNull String stripColors(@NotNull final String src) {
        String cpy = minecraftColorization(src);
        Matcher matcher = TAG_REGEX.matcher(src);
        while (matcher.find()) {
            final String tag = matcher.group();
            final String original = String.format("<%s>", tag);
            cpy = cpy.replace(original, "");
        }
        return ChatColor.stripColor(cpy);
    }


    public @NotNull String lastColor(@NotNull final String src, boolean matchDecorations) {
        String cpy = minecraftColorization(src);
        final LinkedList<Map.Entry<AdventureTag, String>> tags = new LinkedList<>();
        final Matcher matcher = TAG_REGEX.matcher(cpy);

        while (matcher.find()) {
            final String tag = matcher.group();
            final String tagName = tag.contains(":") ? tag.split(":")[0] : tag;
            if (tagName.isEmpty() || tagName.startsWith("/"))
                continue;

            Optional<AdventureTag> optTag = findByName(tagName);
            if (optTag.isPresent()) {
                tags.add(new AbstractMap.SimpleEntry<>(optTag.get(), tag));
            } else {
                Matcher matcher1 = HEX_REGEX.matcher(tag);
                if (matcher1.find())
                    findByName(matcher1.group(1))
                            .ifPresent(adventureTag -> tags.add(new AbstractMap.SimpleEntry<>(adventureTag, tag)));
            }
        }

        String vanilla = getLastLegacyColor(cpy, matchDecorations);
        if (tags.isEmpty())
            return vanilla;
        final String lastTag = matchDecorations ? getSurroundingDecorations(src, tags) : getLastColorTag(tags);
        if (lastTag == null)
            return "";
        if (vanilla.isEmpty())
            return lastTag;
        return cpy.indexOf(vanilla) > cpy.indexOf(lastTag) ? vanilla : lastTag;
    }

    private @Nullable String getLastColorTag(final LinkedList<Map.Entry<AdventureTag, String>> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            final Map.Entry<AdventureTag, String> entry = list.get(i);
            if (entry.getKey().color())
                return String.format("<%s>", entry.getValue());
        }
        return null;
    }

    private @Nullable String getSurroundingDecorations(@NotNull final String src, @NotNull final LinkedList<Map.Entry<AdventureTag, String>> list) {
        final String colorTag = getLastColorTag(list);

        // If there is no color tag, search for the last decoration tag
        if (colorTag == null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                final Map.Entry<AdventureTag, String> entry = list.get(i);
                if (!entry.getKey().color())
                    return String.format("<%s>", entry.getValue());
            }
            return null;
        }
        int tagIndex = src.indexOf(colorTag);
        int index;

        // Search for every decoration before the color tag
        List<String> previousTags = new ArrayList<>();
        String before = src.substring(0, tagIndex);
        if (before.length() > 3 && before.charAt(before.length() - 1) == '>') {
            while (before.length() > 3 && before.charAt(before.length() - 1) == '>') {
                index = before.lastIndexOf('<');
                if (index == -1 || findByName(before.substring(index + 1, before.length() - 1).split(":")[0])
                        .filter(AdventureTag::color)
                        .isPresent()
                        || before.substring(index).startsWith("</"))
                    break;
                previousTags.add(0, before.substring(index));
                before = before.substring(0, index);
            }
        }

        // Search for every decoration after the color tag
        List<String> nextTags = new ArrayList<>();
        String after = src.substring(tagIndex + colorTag.length());
        if (after.length() > 3 && after.charAt(0) == '<') {
            while (after.length() > 3 && after.charAt(0) == '<') {
                index = after.indexOf('>');
                if (index == -1 || findByName(after.substring(1, index).split(":")[0])
                        .filter(AdventureTag::color)
                        .isPresent()
                        || after.substring(0, index).startsWith("</"))
                    break;
                nextTags.add(after.substring(0, index + 1));
                after = after.substring(index + 1);
            }
        }

        // Build the final string
        final StringBuilder builder = new StringBuilder();
        previousTags.forEach(builder::append);
        builder.append(colorTag);
        nextTags.forEach(builder::append);
        return builder.toString();
    }

    private @NotNull Pair<List<String>, String> processContextDecorations(@NotNull String context) {
        final Map<String, String> decorations = new HashMap<>();
        final String cpy = minecraftColorization(context);
        final Matcher matcher = TAG_REGEX.matcher(cpy);

        // Find decorations
        while (matcher.find()) {
            final String tag = matcher.group();
            final String tagName = tag.contains(":") ? tag.split(":")[0] : tag;
            if (tagName.isEmpty() || tagName.startsWith("/"))
                continue;


            findByName(tagName)
                    .filter(t -> !t.color())
                    .map(t -> t.resolver().resolve("", new EmptyArgumentQueue()))
                    .ifPresent(s -> decorations.put(tag, s));
        }

        // Replace decorations tags
        for (Map.Entry<String, String> e : decorations.entrySet())
            context = context.replace(String.format("<%s>", e.getKey()), "");

        return Pair.of(new ArrayList<>(decorations.values()), context);
    }

    private @NotNull String getLastLegacyColor(@NotNull final String input, boolean matchDecorations) {
        final StringBuilder builder = new StringBuilder();
        int length = input.length();
        for (int index = length - 1; index > -1; --index) {
            char section = input.charAt(index);
            if (section != 167 || index >= length - 1)
                continue;
            char c = input.charAt(index + 1);
            ChatColor color = ChatColor.getByChar(c);
            if (color == null)
                continue;
            if (color.isFormat() && !matchDecorations)
                continue;

            builder.insert(0, color);
            if (color.isColor() || color.equals(ChatColor.RESET))
                break;
        }
        return builder.toString();
    }


    /**
     * Register a new tag and check if it's compatible with the server
     * current version.
     *
     * @param tag The tag to register.
     */
    public void add(AdventureTag tag) {
        if (tag.backwardsCompatible() && MythicLib.plugin.getVersion().isUnder(1, 16)) {
            MythicLib.plugin.getLogger().warning(String.format("The tag %s is not compatible with your server version.", tag.name()));
            return;
        }
        tags.add(tag);
    }

    /**
     * Force register a tag, without checking for compatibility.
     *
     * @param tag The tag to register.
     */
    public void forceRegister(AdventureTag tag) {
        tags.add(tag);
    }

    /**
     * Remove a registered tag.
     *
     * @param tag The tag to remove
     */
    public void remove(AdventureTag tag) {
        tags.remove(tag);
    }

    /**
     * This method will return an optional containing the tag if it exists.
     *
     * @param name The name of the tag to find.
     * @return An optional containing the tag if it exists.
     */
    public Optional<AdventureTag> findByName(@NotNull String name) {
        return tags.stream()
                .filter(tag -> tag.name().equalsIgnoreCase(name) || tag.aliases().stream().anyMatch(s -> s.equalsIgnoreCase(name)))
                .findFirst();
    }

    /**
     * Get the list of registered tags.
     *
     * @return A list of all registered tags
     */
    public List<AdventureTag> tags() {
        return tags;
    }
}
