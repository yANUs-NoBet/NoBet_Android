package kr.hs.anu.nobet.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NoBetVpnService: VpnService() {

    companion object {
        const val ACTION_STOP = "kr.hs.anu.nobet.ACTION_STOP"
        @Volatile var isRunning = false
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    private var lastStartId: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                shutdown()
                // 알림 즉시 내리기
                if (android.os.Build.VERSION.SDK_INT >= 24)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                else
                    stopForeground(true)
                isRunning = false
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // "시작" 경로에서는 반드시 바로 알림 올리기
                startForegroundWithNotification()
                isRunning = true
                startTunReader()
                return START_STICKY
            }
        }
    }


    private fun shutdown() {
        worker?.interrupt()
        try { worker?.join(500) } catch (_: InterruptedException) {}
        worker = null

        try { vpnInterface?.close() } catch (_: Exception) {}
        vpnInterface = null
    }

    private fun startTunReader() {
        if (worker != null) return

        val builder = Builder()
            .setSession("Nobet Vpn Logger")
            .addAddress("10.0.0.2", 32)
            // DNS만 TUN으로 보이게 /32 라우팅
            .addRoute("8.8.8.8", 32)
            .addRoute("1.1.1.1", 32)
            .addDnsServer("8.8.8.8")
            .addDnsServer("1.1.1.1")

        builder.addAllowedApplication("com.android.chrome")            // 크롬
        builder.addAllowedApplication("com.sec.android.app.sbrowser")  // 삼성 인터넷
        builder.addAllowedApplication("com.microsoft.emmx")            // 엣지

        try { builder.addDisallowedApplication(packageName) } catch (_: Exception) {}

        vpnInterface = builder.establish()
        if (vpnInterface == null) {
            Log.e("NoBetLogger", "Failed to establish VPN")
            stopSelf(); return
        }

        worker = Thread {
            try {
                val fd = vpnInterface?.fileDescriptor ?: return@Thread
                val input = FileInputStream(fd)
                val output = FileOutputStream(fd) //응답을 TUN으로 써줄 Output
                val packet = ByteArray(32767)

                while (!Thread.interrupted()) {
                    val len = input.read(packet)
                    if (len <= 0) continue
                    handlePacket(packet, len, output)// 단순 로그 -> 포워딩 + 응답
                }
            } catch (e: Exception) {
                Log.e("NoBetLogger", "Reader error", e)
                stopSelf()
            }
        }.also { it.start() }

    }

    private val recentQueries = object : LinkedHashMap<String, Long>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?) = size > 512
    }

    private fun shouldLogOnce(host: String, windowMs: Long = 5_000): Boolean {
        val now = System.currentTimeMillis()
        synchronized(recentQueries) {
            val last = recentQueries[host]
            if (last != null && now - last < windowMs) return false
            recentQueries[host] = now
            return true
        }
    }

    private fun handlePacket(buf: ByteArray, len: Int, output: FileOutputStream) {
        if (len < 20) return
        val verIhl = buf[0].toInt() and 0xFF
        val version = verIhl ushr 4
        if (version != 4) return //IPv4만 처리
        val ihl = (verIhl and 0x0f) * 4
        if (len < ihl + 8) return

        val protocol = buf[9].toInt() and 0xFF
        if (protocol != 17) return //UDP만

        // IPv4 src/dst
        val srcIp = buf.copyOfRange(12, 16)
        val dstIp = buf.copyOfRange(16, 20)
        val srcIpStr = InetAddress.getByAddress(srcIp).hostAddress
        val dstIpStr = InetAddress.getByAddress(dstIp).hostAddress

        val u = ihl
        val srcPort = ((buf[u].toInt() and 0xFF) shl 8) or (buf[u + 1].toInt() and 0xFF)
        val dstPort = ((buf[u + 2].toInt() and 0xFF) shl 8) or (buf[u + 3].toInt() and 0xFF)
        val udpLen = ((buf[u + 4].toInt() and 0xFF) shl 8) or (buf[u + 5].toInt() and 0xFF)
        val dnsStart = u + 8
        val payloadLen = udpLen - 8
        if (payloadLen <= 0 || dnsStart + payloadLen > len) return

        // DNS만
        if (dstPort != 53) return

        val dnsPayload = buf.copyOfRange(dnsStart, dnsStart + payloadLen)

        // 서버 없이 업스트림으로 포워딩
        val upstream = try { InetAddress.getByAddress(dstIp) } catch (_: Exception) { null }
        val reply = forwardDns(upstream, dnsPayload) ?: return

        //로그
        //dnsPayload는 DNS 메시지 전체(헤더 포함)
        if (dstPort == 53) {
            try {
                if (dnsPayload.size >= 12) {
                    val qdCount = ((dnsPayload[4].toInt() and 0xFF) shl 8) or (dnsPayload[5].toInt() and 0xFF)
                    var p = 12
                    repeat(qdCount) {
                        val (name, nextName) = readDnsName(dnsPayload, p, 0, dnsPayload.size) ?: return@repeat
                        var next = nextName
                        if (next + 4 > dnsPayload.size) return@repeat
                        val qtype = ((dnsPayload[next].toInt() and 0xFF) shl 8) or (dnsPayload[next + 1].toInt() and 0xFF)
                        next += 4
                        p = next

                        if (qtype != 1 && qtype != 28) return@repeat

                        val host = runCatching { java.net.IDN.toUnicode(name) }.getOrElse { name }
                        if (shouldLogOnce(host)) {
                            Log.i("VisitLog", "DNS Query: $host ($srcIpStr -> $dstIpStr)")
                        }
                    }
                }
            } catch (e: Exception) {
                // DNS parse failed
            }
        } else {
            //..
        }

        // 응답을 IP + UDP로 재조립해서 TUN으로 쓰기
        val responsePacket = buildUdpIpResponse(
            srcIp = dstIp, dstIp = srcIp,
            srcPort = dstPort, dstPort = srcPort,
            payload = reply
        )
        output.write(responsePacket)
    }

    private fun forwardDns(upstream: InetAddress?, payload: ByteArray): ByteArray? {
        return try {
            val addr = upstream ?: InetAddress.getByName("8.8.8.8")
            DatagramSocket().use { sock ->
                protect(sock)
                sock.soTimeout = 2000
                sock.send(DatagramPacket(payload, payload.size, InetSocketAddress(addr, 53)))
                val buf = ByteArray(4096)
                val resp = DatagramPacket(buf, buf.size)
                sock.receive(resp)
                buf.copyOf(resp.length)
            }
        } catch (e: Exception) {
            //Forward DNS failed
            null
        }
    }

    // IPv4 + UDP 응답 패킷 생성
    private fun buildUdpIpResponse(
        srcIp: ByteArray,
        dstIp: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val ihl = 20
        val udpHeaderLen = 8
        val totalLen = ihl + udpHeaderLen + payload.size
        val bb = ByteBuffer.allocate(totalLen).order(ByteOrder.BIG_ENDIAN)

        // IPv4 header
        bb.put(((4 shl 4) or (ihl / 4)).toByte())
        bb.put(0)
        bb.putShort(totalLen.toShort())
        bb.putShort(0)
        bb.putShort(0x0000.toShort())
        bb.put(64.toByte())
        bb.put(17.toByte())
        bb.putShort(0)
        bb.put(srcIp)
        bb.put(dstIp)

        //UDP header
        bb.putShort(srcPort.toShort())
        bb.putShort(dstPort.toShort())
        bb.putShort((udpHeaderLen + payload.size).toShort())
        bb.putShort(0)

        // payload
        bb.put(payload)

        // IP checksum
        val ipCsum = ipChecksum(bb.array(), 0, ihl)
        bb.putShort(10, ipCsum)

        // UDP checksum with pseudo header
        val udpCsum = udpChecksum(
            srcIp, dstIp, 17,
            bb.array(), ihl, udpHeaderLen + payload.size
        )
        bb.putShort(ihl + 6, udpCsum)

        return bb.array()
    }

    private fun ipChecksum(data: ByteArray, offset: Int, length: Int): Short {
        var sum = 0
        var i = offset
        while (i < offset + length) {
            if (i == offset + 10 ) { i += 2; continue }
            val v = ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            sum += v
            sum = (sum and 0xFFFF) + (sum ushr 16)
            i += 2
        }
        while (sum ushr 16 != 0) sum = (sum and 0xFFFF) + (sum ushr 16)
        return ((sum.inv()) and 0xFFFF).toShort()
    }

    private fun udpChecksum(
        srcIp: ByteArray, dstIp: ByteArray, proto: Int,
        data: ByteArray, udpOffset: Int, udpLen: Int
    ): Short {
        var sum = 0

        fun add16(a: Int) {
            var s = sum + a
            s = (s and 0xFFFF) + (s ushr 16)
            sum = s
        }

        // pseudo header
        for (i in 0 until 4 step 2) {
            add16(((srcIp[i].toInt() and 0xFF) shl 8) or (srcIp[i + 1].toInt() and 0xFF))
        }
        for (i in 0 until 4 step 2) {
            add16(((dstIp[i].toInt() and 0xFF) shl 8) or (dstIp[i + 1].toInt() and 0xFF))
        }
        add16(proto)
        add16(udpLen)

        // UDP header + data
        var i = 0
        while (i < udpLen) {
            val idx = udpOffset + i
            val v = if (i + 1 < udpLen) {
                ((data[idx].toInt() and 0xFF) shl 8) or (data[idx + 1].toInt() and 0xFF)
            } else {
                ((data[idx].toInt() and 0xFF) shl 8)
            }
            add16(v)
            i += 2
        }
        while (sum ushr 16 != 0) sum = (sum and 0xFFFF) + (sum ushr 16)
        val c = ((sum.inv()) and 0xFFFF)
        return if (c == 0) 0xFFFF.toShort() else c.toShort()
    }

    private fun startForegroundWithNotification() {
        val id = "vpn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(
                    NotificationChannel(id, "VPN", NotificationManager.IMPORTANCE_LOW)
                )
        }
        val n = NotificationCompat.Builder(this, id)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("NoBet Logger VPN")
            .setContentText("Running...")
            .setOngoing(true)
            .build()
        startForeground(1, n)
    }

    private fun readDnsName(
        msg: ByteArray,
        pos: Int,
        base: Int,
        totalLen: Int,
        depth: Int = 0
    ): Pair<String, Int>? {
        if (pos < 0 || pos >= totalLen) return null
        if (depth > 10) return null // 무한루프 방지

        var i = pos
        val labels = ArrayList<String>(8)
        var jumped = false
        var endIndex = -1

        while (i < totalLen) {
            val len = msg[i].toInt() and 0xFF
            if (len == 0) {
                if (!jumped) endIndex = i + 1
                break
            }
            if ((len and 0xC0) == 0xC0) {
                // 압축 포인터: 0xC0 | (오프셋 상위 6비트), 다음 바이트가 하위 8비트
                if (i + 1 >= totalLen) return null
                val ptr = ((len and 0x3F) shl 8) or (msg[i + 1].toInt() and 0xFF)
                val ptrAbs = base + ptr
                val sub = readDnsName(msg, ptrAbs, base, totalLen, depth + 1) ?: return null
                labels += sub.first
                if (!jumped) endIndex = i + 2
                break
            } else {
                if (i + 1 + len > totalLen) return null
                val label = try {
                    String(msg, i + 1, len, Charsets.ISO_8859_1)
                } catch (_: Exception) {
                    return null
                }
                labels += label
                i += 1 + len
                if (!jumped) endIndex = i
            }
        }
        if (endIndex == -1) return null
        return labels.joinToString(".") to endIndex
    }

    override fun onDestroy() {
        shutdown()
        try {
            if (android.os.Build.VERSION.SDK_INT >= 24)
                stopForeground(STOP_FOREGROUND_REMOVE)
            else
                stopForeground(true)
        } catch (_: Exception) {}
        isRunning = false
        super.onDestroy()
    }
}