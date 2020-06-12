package org.warp.commonutils.functional;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.warp.commonutils.functional.Unchecked.UncheckedConsumer;

public class TestGenericExceptions {

	@Test
	public void testGenericExceptions() {
		testFunction((number) -> {
			Assertions.assertEquals(number, 1);
		}).done();

		boolean thrown = false;
		try {
			testFunction((number) -> {
				throw new IOException("Test");
			}).throwException(IOException.class);
		} catch (IOException e) {
			thrown = true;
		}
		Assertions.assertEquals(true, thrown, "IOException not thrown");

		boolean thrown2 = false;
		try {
			testFunction((number) -> {
				throw new IOException("Test");
			}).throwException(Exception.class);
		} catch (Exception e) {
			thrown2 = true;
		}
		Assertions.assertEquals(true, thrown2, "Exception not thrown");
	}

	private UncheckedResult testFunction(UncheckedConsumer<Integer> uncheckedConsumer) {
		return Unchecked.wrap(uncheckedConsumer).apply(1);
	}
}
