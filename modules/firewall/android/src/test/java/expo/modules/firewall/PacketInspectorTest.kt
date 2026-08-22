package expo.modules.firewall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PacketInspectorTest {
  @Test
  fun parsesIpv4TcpPorts() {
    val packet = ByteArray(40)
    packet[0] = 0x45
    packet[2] = 0
    packet[3] = 40
    packet[9] = 6
    packet[12] = 10
    packet[13] = 0
    packet[14] = 0
    packet[15] = 2
    packet[16] = 8
    packet[17] = 8
    packet[18] = 8
    packet[19] = 8
    packet[20] = 0x1f
    packet[21] = (0x90).toByte()
    packet[22] = 0x01
    packet[23] = 0xbb.toByte()

    val info = PacketInspector.inspect(packet)
    assertNotNull(info)
    assertEquals("10.0.0.2", info!!.sourceAddress)
    assertEquals("8.8.8.8", info.destinationAddress)
    assertEquals(8080, info.sourcePort)
    assertEquals(443, info.destinationPort)
  }

  @Test
  fun rejectsTooShortPacket() {
    assertNull(PacketInspector.inspect(ByteArray(10)))
  }
}
