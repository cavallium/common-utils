package org.warp.commonutils.type;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets.UnmodifiableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class Bytes {
	public final byte[] data;

	public Bytes(@NotNull byte[] data) {
		this.data = data;
	}

	public static Map<? extends Bytes,? extends Bytes> ofMap(Map<byte[], byte[]> oldMap) {
		var newMap = new HashMap<Bytes, Bytes>(oldMap.size());
		oldMap.forEach((key, value) -> newMap.put(new Bytes(key), new Bytes(value)));
		return newMap;
	}

	public static UnmodifiableMap<? extends Bytes,? extends Bytes> ofMap(UnmodifiableIterableMap<byte[], byte[]> oldMap) {
		Bytes[] keys = new Bytes[oldMap.size()];
		Bytes[] values = new Bytes[oldMap.size()];
		IntWrapper i = new IntWrapper(0);
		oldMap.forEach((key, value) -> {
			keys[i.var] = new Bytes(key);
			values[i.var] = new Bytes(value);
			i.var++;
		});
		return UnmodifiableMap.of(keys, values);
	}

	public static List<? extends Bytes> ofList(List<byte[]> oldList) {
		var newList = new ArrayList<Bytes>(oldList.size());
		oldList.forEach((item) -> newList.add(new Bytes(item)));
		return newList;
	}

	public static Set<? extends Bytes> ofSet(Set<byte[]> oldSet) {
		var newSet = new ObjectOpenHashSet<Bytes>(oldSet.size());
		oldSet.forEach((item) -> newSet.add(new Bytes(item)));
		return newSet;
	}

	public static UnmodifiableIterableSet<byte[]> toIterableSet(UnmodifiableSet<Bytes> set) {
		byte[][] resultItems = new byte[set.size()][];
		var it = set.iterator();
		int i = 0;
		while (it.hasNext()) {
			var item = it.next();
			resultItems[i] = item.data;
			i++;
		}
		return UnmodifiableIterableSet.of(resultItems);
	}

	public static byte[][] toByteArray(Collection<Bytes> value) {
		Bytes[] valueBytesArray = value.toArray(Bytes[]::new);
		byte[][] convertedResult = new byte[valueBytesArray.length][];
		for (int i = 0; i < valueBytesArray.length; i++) {
			convertedResult[i] = valueBytesArray[i].data;
		}
		return convertedResult;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Bytes that = (Bytes) o;
		return Arrays.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public String toString() {
		return Arrays.toString(data);
	}
}
