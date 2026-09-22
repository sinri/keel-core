package io.github.sinri.keel.core.cutter;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntravenouslyCutterOnStringTest {
    private static final class Parser extends IntravenouslyCutterOnString {
        Parser() {
            super(value -> Future.succeededFuture());
        }

        List<String> feed(Buffer bytes) {
            getBufferRef().get().appendBuffer(bytes);
            return cut();
        }

        Buffer remaining() {
            return getBufferRef().get();
        }
    }

    @Test
    void supportsEveryLineEndingCombinationAndEverySplit() {
        for (String a : List.of("\n", "\r", "\r\n")) {
            for (String b : List.of("\n", "\r", "\r\n")) {
                for (String c : List.of("\n", "\r", "\r\n")) {
                    // CR followed by LF is one terminator, not an empty line.
                    if (b.equals("\r") && c.equals("\n")) continue;
                    Buffer input = Buffer.buffer("event: message" + a + "data: 中文😀" + b + c
                            + "data: next" + a + a);
                    List<String> expected = List.of("event: message\ndata: 中文😀", "data: next");
                    for (int split = 0; split <= input.length(); split++) {
                        Parser parser = new Parser();
                        List<String> actual = new ArrayList<>(parser.feed(input.getBuffer(0, split)));
                        actual.addAll(parser.feed(input.getBuffer(split, input.length())));
                        assertEquals(expected, actual, "split=" + split + ", endings=" + List.of(a, b, c));
                        assertEquals(0, parser.remaining().length());
                        assertTrue(parser.cut().isEmpty());
                    }
                    Parser parser = new Parser();
                    List<String> actual = new ArrayList<>();
                    for (int i = 0; i < input.length(); i++) {
                        actual.addAll(parser.feed(input.getBuffer(i, i + 1)));
                    }
                    assertEquals(expected, actual);
                    assertEquals(0, parser.remaining().length());
                }
            }
        }
    }

    @Test
    void preservesLegacyLfSplittingIncludingEmptyFragments() {
        for (int length = 0; length <= 10; length++) {
            for (int mask = 0; mask < (1 << length); mask++) {
                StringBuilder input = new StringBuilder();
                for (int i = 0; i < length; i++) input.append((mask & (1 << i)) == 0 ? 'x' : '\n');
                String remainder = input.toString();
                List<String> expected = new ArrayList<>();
                int boundary;
                while ((boundary = remainder.indexOf("\n\n")) >= 0) {
                    expected.add(remainder.substring(0, boundary));
                    remainder = remainder.substring(boundary + 2);
                }
                Parser parser = new Parser();
                List<String> actual = new ArrayList<>();
                for (int i = 0; i < input.length(); i++) {
                    actual.addAll(parser.feed(Buffer.buffer(input.substring(i, i + 1))));
                }
                assertEquals(expected, actual);
                assertEquals(remainder, parser.remaining().toString());
            }
        }
    }

    @Test
    void parsingFailuresStillPropagateToTheCaller() {
        RuntimeException failure = new RuntimeException("parse failed");
        IntravenouslyCutter<String> cutter = new IntravenouslyCutter<>(value -> Future.succeededFuture(), 0) {
            @Override
            protected List<String> cut() {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(RuntimeException.class,
                () -> cutter.acceptFromStream(Buffer.buffer("raw"))));
        assertEquals("raw", cutter.getBufferRef().get().toString());
        assertSame(failure, assertThrows(RuntimeException.class, cutter::stopHere));
    }

    @Test
    void retainsRawUtf8AfterACompleteEvent() {
        Buffer tail = Buffer.buffer("data: 中文😀\r\n\r\n");
        for (int split = 0; split < tail.length() - 3; split++) {
            Parser parser = new Parser();
            assertEquals(List.of("data: first"), parser.feed(
                    Buffer.buffer("data: first\n\n").appendBuffer(tail.getBuffer(0, split))));
            assertArrayEquals(tail.getBytes(0, split), parser.remaining().getBytes());
            assertEquals(List.of("data: 中文😀"), parser.feed(tail.getBuffer(split, tail.length())));
        }
    }

    @Test
    void eofKeepsIncompleteEventsAndDoesNotDuplicateCompleteOnes() {
        for (String ending : List.of("", "\n", "\r", "\r\n")) {
            Parser parser = new Parser();
            Buffer tail = Buffer.buffer("data: unfinished" + ending);
            assertEquals(List.of("data: complete"), parser.feed(
                    Buffer.buffer("data: complete\r\r").appendBuffer(tail)));
            parser.stopHere();
            assertArrayEquals(tail.getBytes(), parser.remaining().getBytes());
            assertTrue(parser.cut().isEmpty());
        }
        Parser parser = new Parser();
        assertEquals(List.of("data: complete"), parser.feed(Buffer.buffer("data: complete\r\r")));
        parser.stopHere();
        assertEquals(0, parser.remaining().length());
        assertTrue(parser.cut().isEmpty());
    }

    @Test
    void preservesBlankFragmentsAndDoesNotTreatSplitCrLfAsAnEmptyLine() {
        Parser parser = new Parser();
        assertTrue(parser.feed(Buffer.buffer("\n")).isEmpty());
        assertEquals(List.of(""), parser.feed(Buffer.buffer("\n")));
        assertTrue(parser.feed(Buffer.buffer("data: first\r")).isEmpty());
        assertTrue(parser.feed(Buffer.buffer("\n")).isEmpty());
        assertEquals(List.of("data: first"), parser.feed(Buffer.buffer("\r")));
        assertEquals(List.of("data: second"), parser.feed(Buffer.buffer("\ndata: second\n\n")));
    }
}
