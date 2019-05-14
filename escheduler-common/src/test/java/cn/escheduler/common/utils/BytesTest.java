package cn.escheduler.common.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BytesTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testAdd() {
    assertArrayEquals(new byte[] {1, 2, 3, 4},
            Bytes.add(new byte[] {1, 2}, new byte[] {3, 4}));
  }

  @Test
  public void testHashCode() {
    assertEquals(1, Bytes.hashCode(new byte[0], 0, 0));

    final byte[] bytes = {1, 0, 0, 0, -31, 0, 0};
    assertEquals(0, Bytes.hashCode(bytes, 4, 1));
  }

  @Test
  public void testHead() {
    assertNull(Bytes.head(new byte[0], 1));

    assertArrayEquals(new byte[3], Bytes.head(new byte[3], 3));
    assertArrayEquals(new byte[3], Bytes.head(new byte[6], 3));
  }

  @Test
  public void testPadHead() {
    assertArrayEquals(new byte[1], Bytes.padHead(new byte[0], 1));
    assertArrayEquals(new byte[0], Bytes.padHead(new byte[0], 0));
  }

  @Test
  public void testPadTail() {
    assertArrayEquals(new byte[1], Bytes.padTail(new byte[0], 1));
  }

  @Test
  public void testPutBigDecimal() {
    assertEquals(524_314, Bytes.putBigDecimal(null, 524_314, null));
  }

  @Test
  public void testPutByte() {
    final byte[] bytes = new byte[8];

    assertEquals(1, Bytes.putByte(bytes, 0, (byte)3));
    assertArrayEquals(new byte[] {3, 0, 0, 0, 0, 0, 0, 0}, bytes);
  }

  @Test
  public void testPutBytes() {
    byte[] bytes = new byte[3];
    assertEquals(3, Bytes.putBytes(bytes, 0, new byte[] {1, 2, 3}, 0, 3));
    assertArrayEquals(new byte[] {1, 2, 3}, bytes);
  }

  @Test
  public void testPutDoubleException() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.putDouble(new byte[0], 557_073, 0.0);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testPutFloatException() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.putFloat(new byte[0], 557_073, 0.0f);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testPutInt() {
    final byte[] bytes = new byte[9];

    assertEquals(4, Bytes.putInt(bytes, 0, 7));
    assertArrayEquals(new byte[] {0, 0, 0, 7, 0, 0, 0, 0, 0}, bytes);
  }

  @Test
  public void testPutIntException() {
    final byte[] bytes = new byte[9];

    thrown.expect(IllegalArgumentException.class);
    Bytes.putInt(bytes, -2_147_483_639, 0);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testPutLong() {
    final byte[] bytes = new byte[15];

    assertEquals(10, Bytes.putLong(bytes, 2, 4L));
    assertArrayEquals(
            new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0},
            bytes);
  }

  @Test
  public void testPutLongException() {
    final byte[] bytes = new byte[8];

    thrown.expect(IllegalArgumentException.class);
    Bytes.putLong(bytes, -2_147_483_647, 0L);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testPutShort() {
    final byte[] bytes = new byte[9];

    assertEquals(3, Bytes.putShort(bytes, 1, (short)6));
    assertArrayEquals(new byte[] {0, 0, 6, 0, 0, 0, 0, 0, 0}, bytes);
  }

  @Test
  public void testPutShortException() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.putShort(new byte[1], 1, (short)0);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testTail() {
    final byte[] bytes = new byte[5];

    assertArrayEquals(new byte[1], Bytes.tail(bytes, 1));
    assertNull(Bytes.tail(bytes, 6));
  }

  @Test
  public void testToBigDecimal1() {
    assertNull(Bytes.toBigDecimal(new byte[0], 1000, 5));
    assertNull(Bytes.toBigDecimal(new byte[0], 1000, 0));
  }

  @Test
  public void testToBigDecimal2() {
    assertNull(Bytes.toBigDecimal(new byte[1]));
  }

  @Test
  public void testToBigDecimalException() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toBigDecimal(new byte[0], 1000, 2_147_482_648);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToBoolean() {
    assertFalse(Bytes.toBoolean(new byte[1]));
    assertTrue(Bytes.toBoolean(new byte[] {-128}));
  }

  @Test
  public void testToBooleanException() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toBoolean(new byte[] {-128, 0, 0});
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToByteArrays1() {
    assertArrayEquals(new byte[][] {}, Bytes.toByteArrays(new String[] {}));
  }

  @Test
  public void testToByteArrays2() {
    final byte[][] actual = Bytes.toByteArrays(new byte[0]);

    assertEquals(1, actual.length);
    assertArrayEquals(new byte[0], actual[0]);
  }

  @Test
  public void testToByteArrays3() {
    final byte[][] actual = Bytes.toByteArrays(new String[] {"2"});

    assertEquals(1, actual.length);
    assertArrayEquals(new byte[] {50}, actual[0]);
  }

  @Test
  public void testToByteArrays4() {
    final byte[][] actual = Bytes.toByteArrays("1");

    assertEquals(1, actual.length);
    assertArrayEquals(new byte[] {49}, actual[0]);
  }

  @Test
  public void testToBytes1() {
    assertArrayEquals(new byte[1], Bytes.toBytes(false));
    assertArrayEquals(new byte[] {-1}, Bytes.toBytes(true));
  }

  @Test
  public void testToBytes2() {
    assertArrayEquals(new byte[] {49}, Bytes.toBytes("1"));
  }

  @Test
  public void testToBytes3() {
    assertArrayEquals(new byte[2], Bytes.toBytes((short)0));
  }

  @Test
  public void testToBytes4() {
    final byte[] actual = Bytes.toBytes(0.0f);

    assertArrayEquals(new byte[4], actual);
  }

  @Test
  public void testToBytes5() {
    assertArrayEquals(new byte[8], Bytes.toBytes(0L));
  }

  @Test
  public void testToBytes6() {
    assertArrayEquals(new byte[8], Bytes.toBytes(0.0) );
  }

  @Test
  public void testToDouble1() {
    final byte[] bytes = new byte[8];

    assertEquals(0.0, Bytes.toDouble(bytes), 0.0);
  }

  @Test
  public void testToDouble2() {
    assertEquals(0.0, Bytes.toDouble(new byte[0], 2_147_483_641), 0.0);
  }

  @Test
  public void testToDoubleException1() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toDouble(new byte[3]);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToDoubleException2() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toDouble(new byte[0], 57);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToFloat() {
    assertEquals(0.0f, Bytes.toFloat(new byte[0], 2_147_483_645), 0.0f);
  }

  @Test
  public void testToFloatException1() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toFloat(new byte[1]);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToFloatException2() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toFloat(new byte[] {1}, 10);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToInt1() {
    assertEquals(0, Bytes.toInt(new byte[0], 2_147_483_644));
  }

  @Test
  public void testToInt2() {
    assertEquals(0, Bytes.toInt(new byte[0], 2_147_483_646, 4));
    assertEquals(65537, Bytes.toInt(new byte[] {0, 1, 0, 1},0, 4));
  }

  @Test
  public void testToIntException1() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toInt(new byte[1]);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToIntException2() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toInt(new byte[0], 100);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToIntException3() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toInt(new byte[] {0, 1}, 0, 4);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToIntException4() {
    final byte[] bytes = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    thrown.expect(IllegalArgumentException.class);
    Bytes.toInt(bytes, 0, -1_342_177_285);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToLong1() {
    final byte[] bytes = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    assertEquals(0L, Bytes.toLong(bytes, 2_147_483_642));
  }

  @Test
  public void testToLong2() {
    assertEquals(0L, Bytes.toLong(new byte[0], 2_147_483_640, 8));

    final byte[] bytes = {1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0};
    assertEquals(72_057_598_332_895_232L, Bytes.toLong(bytes, 9, 8));
  }

  @Test
  public void testToLongException1() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toLong(new byte[1]);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToLongException2() {
    final byte[] bytes = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    thrown.expect(IllegalArgumentException.class);
    Bytes.toLong(bytes, 100);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToLongException3() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toLong(new byte[0], 16_252_438, 8);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToLongException4() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toLong(new byte[1], -10, -1_193_429_141);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToShort1() {
    assertEquals((short)0, Bytes.toShort(new byte[2]));
  }

  @Test
  public void testToShort2() {
    final byte[] bytes = {1, 1, 0, 1, 1, 1, 1, 1, 0, 0};

    assertEquals((short)0,Bytes.toShort(bytes, 8) );
  }

  @Test
  public void testToShort3() {
    final byte[] bytes = {0, 0, 1, 1, 1, 1, 1, 1, 1, 1};

    assertEquals((short)0,Bytes.toShort(bytes, 0, 2) );
  }

  @Test
  public void testToShortException1() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toShort(new byte[1]);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToShortException2() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toShort(new byte[0], 525);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToShortException3() {
    thrown.expect(IllegalArgumentException.class);
    Bytes.toShort(new byte[0], 0, 2);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToShortException4() {
    final byte[] bytes = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    thrown.expect(IllegalArgumentException.class);
    Bytes.toShort(bytes, 5995, -1_193_429_141);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void testToString1() {
    assertNull(Bytes.toString(null));
    assertEquals("", Bytes.toString(new byte[0]));
  }

  @Test
  public void testToString2() {
    assertEquals("foo", Bytes.toString(new byte[0], "foo", new byte[0]));
    assertEquals("3foo",
            Bytes.toString(new byte[] {'3'}, "foo", new byte[0]));
  }

  @Test
  public void testToString3() {
    assertEquals("", Bytes.toString(new byte[1], 1, 0));
    assertNull(Bytes.toString(null, 1, 0));
  }

  @Test
  public void testToStringException() {
    thrown.expect(StringIndexOutOfBoundsException.class);
    Bytes.toString(new byte[1], 1, 1);
    // Method is not expected to return due to exception thrown
  }

}
