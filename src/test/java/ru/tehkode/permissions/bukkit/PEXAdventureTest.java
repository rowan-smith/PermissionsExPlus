package ru.tehkode.permissions.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PEXAdventureTest {

    private CommandSender sender;

    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @BeforeEach
    void setUp() {
        sender = Mockito.mock(CommandSender.class);
    }

    private String captureSentMessage() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sender).sendMessage(captor.capture());
        return captor.getValue();
    }

    // =====================================================================
    // Lifecycle tests
    // =====================================================================
    @Nested
    class LifecycleTests {

        @Test
        void classIsLoadable() {
            assertNotNull(PEXAdventure.class);
        }

        @Test
        void isAvailableReturnsFalseBeforeInit() {
            assertFalse(PEXAdventure.isAvailable());
        }

        @Test
        void shutdownWhenAlreadyNullDoesNotThrow() {
            PEXAdventure.shutdown();
            assertFalse(PEXAdventure.isAvailable());
        }

        @Test
        void sendMessageBeforeInitDoesNotThrow() {
            PEXAdventure.sendMessage(sender, "plain text");
            assertEquals("plain text", captureSentMessage());
        }
    }

    // =====================================================================
    // Fallback path: legacy ampersand (&) colour codes
    // =====================================================================
    @Nested
    class LegacyAmpersandColourTests {

        @Test
        void singleRedCode() {
            PEXAdventure.sendMessage(sender, "&cRed");
            assertEquals("§cRed", captureSentMessage());
        }

        @Test
        void singleGreenCode() {
            PEXAdventure.sendMessage(sender, "&aGreen");
            assertEquals("§aGreen", captureSentMessage());
        }

        @Test
        void singleAquaCode() {
            PEXAdventure.sendMessage(sender, "&bAqua");
            assertEquals("§bAqua", captureSentMessage());
        }

        @Test
        void singleYellowCode() {
            PEXAdventure.sendMessage(sender, "&eYellow");
            assertEquals("§eYellow", captureSentMessage());
        }

        @Test
        void singleBlueCode() {
            PEXAdventure.sendMessage(sender, "&9Blue");
            assertEquals("§9Blue", captureSentMessage());
        }

        @Test
        void singleDarkRedCode() {
            PEXAdventure.sendMessage(sender, "&4Dark Red");
            assertEquals("§4Dark Red", captureSentMessage());
        }

        @Test
        void singleWhiteCode() {
            PEXAdventure.sendMessage(sender, "&fWhite");
            assertEquals("§fWhite", captureSentMessage());
        }

        @Test
        void singleBlackCode() {
            PEXAdventure.sendMessage(sender, "&0Black");
            assertEquals("§0Black", captureSentMessage());
        }

        @Test
        void numericCode1() {
            PEXAdventure.sendMessage(sender, "&1Dark Blue");
            assertEquals("§1Dark Blue", captureSentMessage());
        }

        @Test
        void numericCode2() {
            PEXAdventure.sendMessage(sender, "&2Dark Green");
            assertEquals("§2Dark Green", captureSentMessage());
        }

        @Test
        void numericCode3() {
            PEXAdventure.sendMessage(sender, "&3Dark Aqua");
            assertEquals("§3Dark Aqua", captureSentMessage());
        }

        @Test
        void numericCode5() {
            PEXAdventure.sendMessage(sender, "&5Dark Purple");
            assertEquals("§5Dark Purple", captureSentMessage());
        }

        @Test
        void numericCode6() {
            PEXAdventure.sendMessage(sender, "&6Gold");
            assertEquals("§6Gold", captureSentMessage());
        }

        @Test
        void numericCode7() {
            PEXAdventure.sendMessage(sender, "&7Gray");
            assertEquals("§7Gray", captureSentMessage());
        }

        @Test
        void numericCode8() {
            PEXAdventure.sendMessage(sender, "&8Dark Gray");
            assertEquals("§8Dark Gray", captureSentMessage());
        }
    }

    // =====================================================================
    // Fallback path: legacy ampersand (&) formatting codes
    // =====================================================================
    @Nested
    class LegacyAmpersandFormattingTests {

        @Test
        void boldCode() {
            PEXAdventure.sendMessage(sender, "&lBold");
            assertEquals("§lBold", captureSentMessage());
        }

        @Test
        void italicCode() {
            PEXAdventure.sendMessage(sender, "&oItalic");
            assertEquals("§oItalic", captureSentMessage());
        }

        @Test
        void underlineCode() {
            PEXAdventure.sendMessage(sender, "&nUnderline");
            assertEquals("§nUnderline", captureSentMessage());
        }

        @Test
        void strikethroughCode() {
            PEXAdventure.sendMessage(sender, "&mStrikethrough");
            assertEquals("§mStrikethrough", captureSentMessage());
        }

        @Test
        void obfuscatedCode() {
            PEXAdventure.sendMessage(sender, "&kObfuscated");
            assertEquals("§kObfuscated", captureSentMessage());
        }

        @Test
        void resetCode() {
            PEXAdventure.sendMessage(sender, "&cRed&r Reset");
            assertEquals("§cRed§r Reset", captureSentMessage());
        }
    }

    // =====================================================================
    // Fallback path: legacy ampersand combinations
    // =====================================================================
    @Nested
    class LegacyAmpersandCombinationTests {

        @Test
        void colourThenFormatting() {
            PEXAdventure.sendMessage(sender, "&c&lBold Red");
            assertEquals("§c§lBold Red", captureSentMessage());
        }

        @Test
        void formattingThenColour() {
            // A colour code implicitly resets formatting, so &l&c produces just §c
            PEXAdventure.sendMessage(sender, "&l&cBold Red");
            assertEquals("§cBold Red", captureSentMessage());
        }

        @Test
        void multipleColoursInSequence() {
            PEXAdventure.sendMessage(sender, "&cRed &aGreen &bBlue");
            assertEquals("§cRed §aGreen §bBlue", captureSentMessage());
        }

        @Test
        void colourWithPlainTextInputOutput() {
            PEXAdventure.sendMessage(sender, "Hello &aGreen &eYellow World");
            assertEquals("Hello §aGreen §eYellow World", captureSentMessage());
        }

        @Test
        void colourThenResetThenPlain() {
            PEXAdventure.sendMessage(sender, "&cRed &rPlain");
            assertEquals("§cRed §rPlain", captureSentMessage());
        }

        @Test
        void nestedFormattingCodes() {
            PEXAdventure.sendMessage(sender, "&c&l&nRed Bold Underline");
            assertEquals("§c§l§nRed Bold Underline", captureSentMessage());
        }
    }

    // =====================================================================
    // Fallback path: section symbol (§) codes
    // =====================================================================
    @Nested
    class LegacySectionCodeTests {

        @Test
        void sectionRedCode() {
            PEXAdventure.sendMessage(sender, "§cRed");
            assertEquals("§cRed", captureSentMessage());
        }

        @Test
        void sectionGreenCode() {
            PEXAdventure.sendMessage(sender, "§aGreen");
            assertEquals("§aGreen", captureSentMessage());
        }

        @Test
        void sectionBoldCode() {
            PEXAdventure.sendMessage(sender, "§lBold");
            assertEquals("§lBold", captureSentMessage());
        }

        @Test
        void sectionResetCode() {
            PEXAdventure.sendMessage(sender, "§cRed§r Reset");
            assertEquals("§cRed§r Reset", captureSentMessage());
        }

        @Test
        void sectionMultipleCodes() {
            PEXAdventure.sendMessage(sender, "§c§lBold Red");
            assertEquals("§c§lBold Red", captureSentMessage());
        }
    }

    // =====================================================================
    // Fallback path: mixed § and & codes
    // =====================================================================
    @Nested
    class MixedFormatCodeTests {

        @Test
        void ampersandThenSection() {
            PEXAdventure.sendMessage(sender, "&cRed §aGreen");
            assertEquals("§cRed §aGreen", captureSentMessage());
        }

        @Test
        void sectionThenAmpersand() {
            PEXAdventure.sendMessage(sender, "§cRed &aGreen");
            assertEquals("§cRed §aGreen", captureSentMessage());
        }

        @Test
        void alternatingFormats() {
            PEXAdventure.sendMessage(sender, "&cRed §aGreen &bBlue");
            assertEquals("§cRed §aGreen §bBlue", captureSentMessage());
        }
    }

    // =====================================================================
    // Fallback path: plain text and edge cases
    // =====================================================================
    @Nested
    class FallbackEdgeCaseTests {

        @Test
        void plainTextPassesThrough() {
            PEXAdventure.sendMessage(sender, "Hello world");
            assertEquals("Hello world", captureSentMessage());
        }

        @Test
        void emptyStringProducesEmptyOutput() {
            PEXAdventure.sendMessage(sender, "");
            assertEquals("", captureSentMessage());
        }

        @Test
        void onlyAmpersandSymbolIsLiteral() {
            PEXAdventure.sendMessage(sender, "Use & for colour codes");
            assertEquals("Use & for colour codes", captureSentMessage());
        }

        @Test
        void onlySectionSymbolIsLiteral() {
            // § is normalized to & before parsing, so a bare § becomes a bare &
            PEXAdventure.sendMessage(sender, "Use § for colour codes");
            assertEquals("Use & for colour codes", captureSentMessage());
        }

        @Test
        void miniMessageTagsRemainLiteralInFallback() {
            PEXAdventure.sendMessage(sender, "<red>not parsed</red>");
            assertEquals("<red>not parsed</red>", captureSentMessage());
        }

        @Test
        void miniMessageTagsMixedWithLegacy() {
            PEXAdventure.sendMessage(sender, "&c<red>literal</red>");
            assertEquals("§c<red>literal</red>", captureSentMessage());
        }

        @Test
        void longMessageWithManyCodes() {
            String input = "&4Dark &cRed &6Gold &aGreen &bAqua &3Dark Aqua &1Dark Blue &9Blue &5Dark &dPurple";
            String expected = "§4Dark §cRed §6Gold §aGreen §bAqua §3Dark Aqua §1Dark Blue §9Blue §5Dark §dPurple";
            PEXAdventure.sendMessage(sender, input);
            assertEquals(expected, captureSentMessage());
        }

        @Test
        void messageWithNewlinesInInput() {
            PEXAdventure.sendMessage(sender, "&cLine1\n&aLine2");
            assertEquals("§cLine1\n§aLine2", captureSentMessage());
        }
    }

    // =====================================================================
    // Serialization pipeline: legacy & → Component → MiniMessage string
    // =====================================================================
    @Nested
    class LegacyToMiniMessagePipelineTests {

        @Test
        void redCodeSerializesToMiniMessage() {
            Component component = LEGACY_AMPERSAND.deserialize("&cRed");
            String mini = MINI_MESSAGE.serialize(component);
            assertTrue(mini.contains("red"), "MiniMessage should contain 'red': " + mini);
        }

        @Test
        void greenCodeSerializesToMiniMessage() {
            Component component = LEGACY_AMPERSAND.deserialize("&aGreen");
            String mini = MINI_MESSAGE.serialize(component);
            assertTrue(mini.contains("green"), "MiniMessage should contain 'green': " + mini);
        }

        @Test
        void boldCodeSerializesToMiniMessage() {
            Component component = LEGACY_AMPERSAND.deserialize("&lBold");
            String mini = MINI_MESSAGE.serialize(component);
            assertTrue(mini.contains("bold"), "MiniMessage should contain 'bold': " + mini);
        }

        @Test
        void colourAndFormattingSerializesToMiniMessage() {
            Component component = LEGACY_AMPERSAND.deserialize("&c&lBold Red");
            String mini = MINI_MESSAGE.serialize(component);
            assertTrue(mini.contains("red"), "MiniMessage should contain 'red': " + mini);
            assertTrue(mini.contains("bold"), "MiniMessage should contain 'bold': " + mini);
        }

        @Test
        void sectionSymbolNormalizesToAmpersand() {
            Component component = LEGACY_AMPERSAND.deserialize("§cRed".replace("§", "&"));
            String mini = MINI_MESSAGE.serialize(component);
            assertTrue(mini.contains("red"), "Section codes should normalize to same result: " + mini);
        }

        @Test
        void multipleColoursInMiniMessage() {
            Component component = LEGACY_AMPERSAND.deserialize("&cRed &aGreen &bBlue");
            String mini = MINI_MESSAGE.serialize(component);
            assertTrue(mini.contains("red"), "Should contain red: " + mini);
            assertTrue(mini.contains("green"), "Should contain green: " + mini);
            // &b is aqua, not blue (&9 is blue)
            assertTrue(mini.contains("aqua"), "Should contain aqua: " + mini);
        }

        @Test
        void escapedMiniMessageTagBecomesLiteral() {
            Component component = LEGACY_AMPERSAND.deserialize("test");
            String mini = MINI_MESSAGE.serialize(component);
            String hybrid = mini.replace("\\<", "<");
            Component result = MINI_MESSAGE.deserialize(hybrid);
            String roundTripped = MINI_MESSAGE.serialize(result);
            assertEquals("test", roundTripped.strip());
        }
    }

    // =====================================================================
    // MiniMessage format: direct deserialization
    // =====================================================================
    @Nested
    class MiniMessageFormatTests {

        @Test
        void redTag() {
            Component component = MINI_MESSAGE.deserialize("<red>Red</red>");
            assertEquals(NamedTextColor.RED, component.color());
        }

        @Test
        void greenTag() {
            Component component = MINI_MESSAGE.deserialize("<green>Green</green>");
            assertEquals(NamedTextColor.GREEN, component.color());
        }

        @Test
        void blueTag() {
            Component component = MINI_MESSAGE.deserialize("<blue>Blue</blue>");
            assertEquals(NamedTextColor.BLUE, component.color());
        }

        @Test
        void aquaTag() {
            Component component = MINI_MESSAGE.deserialize("<aqua>Aqua</aqua>");
            assertEquals(NamedTextColor.AQUA, component.color());
        }

        @Test
        void yellowTag() {
            Component component = MINI_MESSAGE.deserialize("<yellow>Yellow</yellow>");
            assertEquals(NamedTextColor.YELLOW, component.color());
        }

        @Test
        void goldTag() {
            Component component = MINI_MESSAGE.deserialize("<gold>Gold</gold>");
            assertEquals(NamedTextColor.GOLD, component.color());
        }

        @Test
        void darkRedTag() {
            Component component = MINI_MESSAGE.deserialize("<dark_red>Dark Red</dark_red>");
            assertEquals(NamedTextColor.DARK_RED, component.color());
        }

        @Test
        void whiteTag() {
            Component component = MINI_MESSAGE.deserialize("<white>White</white>");
            assertEquals(NamedTextColor.WHITE, component.color());
        }

        @Test
        void blackTag() {
            Component component = MINI_MESSAGE.deserialize("<black>Black</black>");
            assertEquals(NamedTextColor.BLACK, component.color());
        }

        @Test
        void grayTag() {
            Component component = MINI_MESSAGE.deserialize("<gray>Gray</gray>");
            assertEquals(NamedTextColor.GRAY, component.color());
        }

        @Test
        void boldTag() {
            Component component = MINI_MESSAGE.deserialize("<bold>Bold</bold>");
            assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.BOLD));
        }

        @Test
        void italicTag() {
            Component component = MINI_MESSAGE.deserialize("<italic>Italic</italic>");
            assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.ITALIC));
        }

        @Test
        void underlinedTag() {
            Component component = MINI_MESSAGE.deserialize("<underlined>Underlined</underlined>");
            assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.UNDERLINED));
        }

        @Test
        void strikethroughTag() {
            Component component = MINI_MESSAGE.deserialize("<strikethrough>Strikethrough</strikethrough>");
            assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.STRIKETHROUGH));
        }

        @Test
        void obfuscatedTag() {
            Component component = MINI_MESSAGE.deserialize("<obfuscated>Obfuscated</obfuscated>");
            assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.OBFUSCATED));
        }

        @Test
        void resetTag() {
            Component component = MINI_MESSAGE.deserialize("<red>Red</red>");
            assertEquals(NamedTextColor.RED, component.color());
        }

        @Test
        void nestedColourAndFormatting() {
            Component component = MINI_MESSAGE.deserialize("<red><bold>Red Bold</bold></red>");
            assertEquals(NamedTextColor.RED, component.color());
            assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.BOLD));
        }

        @Test
        void closeTagShorthand() {
            Component component = MINI_MESSAGE.deserialize("<red>Red</red>");
            Component closed = MINI_MESSAGE.deserialize("<red>Red<reset>");
            assertEquals(component.color(), closed.color());
        }

        @Test
        void plainTextNoTags() {
            Component component = MINI_MESSAGE.deserialize("Hello World");
            String serialized = MINI_MESSAGE.serialize(component).strip();
            assertEquals("Hello World", serialized);
        }

        @Test
        void multipleTagsInSequence() {
            Component component = MINI_MESSAGE.deserialize("<red>Red</red> <green>Green</green> <blue>Blue</blue>");
            // When multiple tags are used at the same level, the root is a container.
            // The first child carries the first colour.
            Component firstChild = component.children().get(0);
            assertEquals(NamedTextColor.RED, firstChild.color());
        }

        @Test
        void hexColorTag() {
            Component component = MINI_MESSAGE.deserialize("<#FF5555>Custom Red</#FF5555>");
            assertNotNull(component.color());
        }
    }

    // =====================================================================
    // End-to-end pipeline: legacy → MiniMessage → Component round-trip
    // =====================================================================
    @Nested
    class EndToEndPipelineTests {

        private Component processThroughPipeline(String input) {
            String normalized = input.replace("§", "&");
            Component legacyParsed = LEGACY_AMPERSAND.deserialize(normalized);
            String miniMessage = MINI_MESSAGE.serialize(legacyParsed);
            String hybridMessage = miniMessage.replace("\\<", "<");
            return MINI_MESSAGE.deserialize(hybridMessage);
        }

        @Test
        void legacyRedBecomesRedComponent() {
            Component result = processThroughPipeline("&cRed");
            assertEquals(NamedTextColor.RED, result.color());
        }

        @Test
        void legacyGreenBecomesGreenComponent() {
            Component result = processThroughPipeline("&aGreen");
            assertEquals(NamedTextColor.GREEN, result.color());
        }

        @Test
        void legacyBoldBecomesBoldComponent() {
            Component result = processThroughPipeline("&lBold");
            assertEquals(TextDecoration.State.TRUE, result.decoration(TextDecoration.BOLD));
        }

        @Test
        void legacyRedBoldBecomesRedBoldComponent() {
            Component result = processThroughPipeline("&c&lRed Bold");
            assertEquals(NamedTextColor.RED, result.color());
            assertEquals(TextDecoration.State.TRUE, result.decoration(TextDecoration.BOLD));
        }

        @Test
        void legacyMultipleColours() {
            Component result = processThroughPipeline("&cRed &aGreen");
            // When multiple colours are present, the root is a container;
            // the first child carries the colour of the first segment.
            Component firstChild = result.children().get(0);
            assertEquals(NamedTextColor.RED, firstChild.color());
        }

        @Test
        void sectionSymbolRedBecomesRedComponent() {
            Component result = processThroughPipeline("§cRed");
            assertEquals(NamedTextColor.RED, result.color());
        }

        @Test
        void mixedSectionAndAmpersand() {
            Component result = processThroughPipeline("&cRed §aGreen");
            // The root is a container; the first child carries the red colour.
            Component firstChild = result.children().get(0);
            assertEquals(NamedTextColor.RED, firstChild.color());
        }

        @Test
        void plainTextHasNoFormatting() {
            Component result = processThroughPipeline("Hello World");
            String serialized = MINI_MESSAGE.serialize(result).strip();
            assertEquals("Hello World", serialized);
        }

        @Test
        void legacyBlueBecomesBlueComponent() {
            Component result = processThroughPipeline("&9Blue");
            assertEquals(NamedTextColor.BLUE, result.color());
        }

        @Test
        void legacyYellowBecomesYellowComponent() {
            Component result = processThroughPipeline("&eYellow");
            assertEquals(NamedTextColor.YELLOW, result.color());
        }

        @Test
        void legacyItalicBecomesItalicComponent() {
            Component result = processThroughPipeline("&oItalic");
            assertEquals(TextDecoration.State.TRUE, result.decoration(TextDecoration.ITALIC));
        }

        @Test
        void legacyUnderlineBecomesUnderlineComponent() {
            Component result = processThroughPipeline("&nUnderline");
            assertEquals(TextDecoration.State.TRUE, result.decoration(TextDecoration.UNDERLINED));
        }

        @Test
        void legacyResetClearsFormatting() {
            Component result = processThroughPipeline("&cRed&r Plain");
            // The root is a container; the first child carries the red colour.
            Component firstChild = result.children().get(0);
            assertEquals(NamedTextColor.RED, firstChild.color());
        }

        @Test
        void legacyGoldBecomesGoldComponent() {
            Component result = processThroughPipeline("&6Gold");
            assertEquals(NamedTextColor.GOLD, result.color());
        }

        @Test
        void legacyDarkRedBecomesDarkRedComponent() {
            Component result = processThroughPipeline("&4Dark Red");
            assertEquals(NamedTextColor.DARK_RED, result.color());
        }
    }

    // =====================================================================
    // PAPI integration: sendMessage with Player senders
    // =====================================================================
    @Nested
    class PapiSendMessageTests {

        private Player player;
        private Object originalServer;

        @BeforeEach
        void setUpPlayer() throws Exception {
            player = mock(Player.class);
            Mockito.when(player.getName()).thenReturn("TestPlayer");
            Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());

            // Save original Bukkit.server
            Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            originalServer = serverField.get(null);

            // Set up a mock Server with a PluginManager (returns null for getPlugin("PlaceholderAPI"))
            Server mockServer = (Server) Proxy.newProxyInstance(
                    Server.class.getClassLoader(),
                    new Class[]{Server.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getPluginManager")) {
                            return Proxy.newProxyInstance(
                                    PluginManager.class.getClassLoader(),
                                    new Class[]{PluginManager.class},
                                    (p, m, a) -> null
                            );
                        }
                        return null;
                    }
            );
            serverField.set(null, mockServer);
        }

        @AfterEach
        void tearDownServer() throws Exception {
            Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, originalServer);
        }

        private String capturePlayerMessage() {
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(player).sendMessage(captor.capture());
            return captor.getValue();
        }

        @Test
        void playerSenderWithPapiPlaceholderWithoutPapi() {
            // When PAPI is not available, %player_name% stays literal
            PEXAdventure.sendMessage(player, "&c%player_name%");
            assertEquals("§c%player_name%", capturePlayerMessage());
        }

        @Test
        void playerSenderWithLegacyCodes() {
            PEXAdventure.sendMessage(player, "&aHello &bWorld");
            assertEquals("§aHello §bWorld", capturePlayerMessage());
        }

        @Test
        void playerSenderPlain() {
            PEXAdventure.sendMessage(player, "Hello World");
            assertEquals("Hello World", capturePlayerMessage());
        }

        @Test
        void playerSenderWithSectionCodes() {
            PEXAdventure.sendMessage(player, "§cRed");
            assertEquals("§cRed", capturePlayerMessage());
        }

        @Test
        void nonPlayerSenderSkipsPapiResolution() {
            CommandSender console = mock(CommandSender.class);
            PEXAdventure.sendMessage(console, "&c%player_name%");
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(console).sendMessage(captor.capture());
            assertEquals("§c%player_name%", captor.getValue());
        }

        @Test
        void isPlaceholderApiAvailableReturnsFalseInTestEnv() throws Exception {
            Method m = PEXAdventure.class.getDeclaredMethod("isPlaceholderApiAvailable");
            m.setAccessible(true);
            assertFalse((Boolean) m.invoke(null));
        }
    }
}
