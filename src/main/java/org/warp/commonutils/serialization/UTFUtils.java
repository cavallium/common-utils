package org.warp.commonutils.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UTFUtils {
	public static final void writeUTF(DataOutputStream out, String utf) throws IOException {
		byte[] bytes = utf.getBytes(StandardCharsets.UTF_8);
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	public static final String readUTF(DataInputStream in) throws IOException {
		int len = in.readInt();
		return new String(in.readNBytes(len), StandardCharsets.UTF_8);
	}
}
