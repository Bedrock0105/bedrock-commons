package org.bedrock.common.code.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * INet 相关工具
 */
@Slf4j
public class INetUtil {

	public static final String LOCAL_HOST = "127.0.0.1";

	/**
	 * 获取 服务器 hostname
	 *
	 * @return hostname
	 */
	public static String getHostName() {
		String hostname;
		try {
			InetAddress address = InetAddress.getLocalHost();
			// force a best effort reverse DNS lookup
			hostname = address.getHostName();
			if (!StringUtils.hasText(hostname)) {
				hostname = address.toString();
			}
		} catch (UnknownHostException ignore) {
			hostname = LOCAL_HOST;
		}
		return hostname;
	}

	/**
	 * 获取 服务器 HostIp
	 *
	 * @return HostIp
	 */
	public static String getHostIp() {
		String hostAddress;
		try {
			InetAddress address = INetUtil.getLocalHostLANAddress();
			// force a best effort reverse DNS lookup
			hostAddress = address.getHostAddress();
			if (!StringUtils.hasText(hostAddress)) {
				hostAddress = address.toString();
			}
		} catch (UnknownHostException ignore) {
			hostAddress = LOCAL_HOST;
		}
		return hostAddress;
	}

	/**
	 * 获取某个网络接口的Mac地址
	 */
	public static String getMacByInetAddress(InetAddress inetAddr) {
		try {
			byte[] mac = NetworkInterface.getByInetAddress(inetAddr).getHardwareAddress();
			StringBuilder stringBuffer = new StringBuilder();

			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					stringBuffer.append("-");
				}

				//将十六进制byte转化为字符串
				String temp = Integer.toHexString(mac[i] & 0xff);
				if (temp.length() == 1) {
					stringBuffer.append("0").append(temp);
				} else {
					stringBuffer.append(temp);
				}
			}

			return stringBuffer.toString().toUpperCase();
		} catch (SocketException e) {
			log.error("An IO exception occurred: {}", e.getMessage());  // 这是安全的做法
			//e.printStackTrace();
		}

		return null;
	}

	/**
	 * 获取当前系统CPU序列，可区分linux系统和windows系统
	 */
	public static String getCpuId() {
		String cpuId;
		// 获取当前操作系统名称
		String os = System.getProperty("os.name");
		os = os.toUpperCase();
		// linux系统用Runtime.getRuntime().exec()执行 dmidecode -t processor 查询cpu序列
		// windows系统用 wmic cpu get ProcessorId 查看cpu序列
		try {
			if ("LINUX".equals(os)) {
				cpuId = getLinuxCpuId("dmidecode -t processor | grep 'ID'", "ID", ":");
			} else {
				cpuId = getWindowsCpuId();
			}
		} catch (Exception e) {
			return "BFEBFBFF000806EA";
		}
		assert cpuId != null;
		return cpuId.toUpperCase().replace(" ", "");
	}

	/**
	 * 获取linux系统CPU序列
	 */
	public static String getLinuxCpuId(String cmd, String record, String symbol) throws Exception {
		String execResult = executeLinuxCmd(cmd);
		String[] infos = execResult.split("\n");
		for (String info : infos) {
			info = info.trim();
			if (info.contains(record)) {
				String s = info.replace(" ", "");
				String[] sn = info.split(symbol);
				return sn[1];
			}
		}
		return null;
	}

	public static String executeLinuxCmd(String cmd) throws Exception {
		Runtime run = Runtime.getRuntime();
		Process process;
		process = run.exec(cmd);
		InputStream in = process.getInputStream();
		StringBuilder out = new StringBuilder();
		byte[] b = new byte[8192];
		for (int n; (n = in.read(b)) != -1; ) {
			out.append(new String(b, 0, n));
		}
		in.close();
		process.destroy();
		return out.toString();
	}

	/**
	 * 获取windows系统CPU序列
	 */
	public static String getWindowsCpuId() throws Exception {
		Process process = Runtime.getRuntime().exec(
			new String[]{"wmic", "cpu", "get", "ProcessorId"});
		process.getOutputStream().close();
		Scanner sc = new Scanner(process.getInputStream());
		sc.next();
		return sc.next();
	}

	/**
	 * <a href="https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java">...</a>
	 *
	 * <p>
	 * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
	 * <p/>
	 * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
	 * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
	 * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
	 * specify the algorithm used to select the address returned under such circumstances, and will often return the
	 * loopback address, which is not valid for network communication. Details
	 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
	 * <p/>
	 * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
	 * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
	 * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
	 * first site-local address if the machine has more than one), but if the machine does not hold a site-local
	 * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
	 * <p/>
	 * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
	 * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
	 * <p/>
	 *
	 * @throws UnknownHostException If the LAN address of the machine cannot be found.
	 */
	private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
		try {
			InetAddress candidateAddress = null;
			// Iterate all NICs (network interface cards)...
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
				NetworkInterface iface = ifaces.nextElement();
				// Iterate all IP addresses assigned to each card...
				for (Enumeration<InetAddress> inetAddress = iface.getInetAddresses(); inetAddress.hasMoreElements(); ) {
					InetAddress inetAddr = inetAddress.nextElement();
					if (!inetAddr.isLoopbackAddress()) {

						if (inetAddr.isSiteLocalAddress()) {
							// Found non-loopback site-local address. Return it immediately...
							return inetAddr;
						} else if (candidateAddress == null) {
							// Found non-loopback address, but not necessarily site-local.
							// Store it as a candidate to be returned if site-local address is not subsequently found...
							candidateAddress = inetAddr;
							// Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
							// only the first. For subsequent iterations, candidate will be non-null.
						}
					}
				}
			}
			if (candidateAddress != null) {
				// We did not find a site-local address, but we found some other non-loopback address.
				// Server might have a non-site-local address assigned to its NIC (or it might be running
				// IPv6 which deprecates the "site-local" concept).
				// Return this non-loopback candidate address...
				return candidateAddress;
			}
			// At this point, we did not find a non-loopback address.
			// Fall back to returning whatever InetAddress.getLocalHost() returns...
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		} catch (Exception e) {
			UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}

	/**
	 * 尝试端口时候被占用
	 *
	 * @param port 端口号
	 * @return 没有被占用：true,被占用：false
	 */
	public static boolean tryPort(int port) {
		try (ServerSocket ignore = new ServerSocket(port)) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 将 ip 转成 InetAddress
	 *
	 * @param ip ip
	 * @return InetAddress
	 */
	public static InetAddress getInetAddress(String ip) {
		try {
			return InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	/**
	 * 判断是否内网 ip
	 *
	 * @param ip ip
	 * @return boolean
	 */
	public static boolean isInternalIp(String ip) {
		return isInternalIp(getInetAddress(ip));
	}

	/**
	 * 判断是否内网 ip
	 *
	 * @param address InetAddress
	 * @return boolean
	 */
	public static boolean isInternalIp(InetAddress address) {
		if (isLocalIp(address)) {
			return true;
		}
		return isInternalIp(address.getAddress());
	}

	/**
	 * 判断是否本地 ip
	 *
	 * @param address InetAddress
	 * @return boolean
	 */
	public static boolean isLocalIp(InetAddress address) {
		return address.isAnyLocalAddress()
			|| address.isLoopbackAddress()
			|| address.isSiteLocalAddress();
	}

	/**
	 * 判断是否内网 ip
	 *
	 * @param addr ip
	 * @return boolean
	 */
	public static boolean isInternalIp(byte[] addr) {
		final byte b0 = addr[0];
		final byte b1 = addr[1];
		//10.x.x.x/8
		final byte section1 = 0x0A;
		//172.16.x.x/12
		final byte section2 = (byte) 0xAC;
		final byte section3 = (byte) 0x10;
		final byte section4 = (byte) 0x1F;
		//192.168.x.x/16
		final byte section5 = (byte) 0xC0;
		final byte section6 = (byte) 0xA8;
		switch (b0) {
			case section1:
				return true;
			case section2:
				if (b1 >= section3 && b1 <= section4) {
					return true;
				}
			case section5:
				if (b1 == section6) {
					return true;
				}
			default:
				return false;
		}
	}

}
